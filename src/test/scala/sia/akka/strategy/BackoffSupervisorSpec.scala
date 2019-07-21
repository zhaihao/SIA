/*
 * Copyright (c) 2019.
 * OOON.ME ALL RIGHTS RESERVED.
 * Licensed under the Mozilla Public License, version 2.0
 * Please visit http://ooon.me or mail to zhaihao@ooon.me
 */

package sia.akka.strategy

import akka.actor.{
  Actor,
  ActorLogging,
  ActorRef,
  ActorSystem,
  DeadLetter,
  OneForOneStrategy,
  Props,
  SupervisorStrategy
}
import akka.pattern.{BackoffOpts, BackoffSupervisor}
import akka.testkit.TestKit
import com.typesafe.scalalogging.StrictLogging
import test.BaseSpecLike

import scala.concurrent.duration._
import scala.util.Random

/**
  * BackoffSupervisorSpec
  *
  * @author zhaihao
  * @version 1.0
  * @since 2019-07-21 22:48
  */
class BackoffSupervisorSpec extends TestKit(ActorSystem()) with BaseSpecLike {

  "BackoffSupervisor 会自动转发消息" in {
    class InnerChild extends Actor with ActorLogging {

      import InnerChild._

      override def receive = {
        case TestMessage(msg) => log.info(s"Child receive message: $msg")
      }
    }
    object InnerChild {

      case class TestMessage(msg: String)

      class ChildException extends Exception

      def props = Props(new InnerChild())
    }

    object Supervisor {
      def props = {
        def decider: PartialFunction[Throwable, SupervisorStrategy.Directive] = {
          case _: InnerChild.ChildException => SupervisorStrategy.Restart
        }

        val options = BackoffOpts
          .onFailure(InnerChild.props, "innerChild", 1.second, 5.seconds, 0.0)
          .withManualReset
          .withSupervisorStrategy(
            OneForOneStrategy(
              maxNrOfRetries  = 5,
              withinTimeRange = 5.seconds
            )(decider.orElse(SupervisorStrategy.defaultDecider)))

        BackoffSupervisor.props(options)
      }
    }

    object ParentActor {

      case class SendToSupervisor(msg: InnerChild.TestMessage)

      case class SendToInnerChild(msg: InnerChild.TestMessage)

      case class SendToChildSelection(msg: InnerChild.TestMessage)

      def props = Props(new ParentActor)
    }
    class ParentActor extends Actor with ActorLogging {

      import ParentActor._

      val supervisor = context.actorOf(Supervisor.props, "supervisor")
      supervisor ! BackoffSupervisor.GetCurrentChild
      var innerChild: Option[ActorRef] = None
      val selectedChild = context.actorSelection("/user/parent/supervisor/innerChild")

      override def receive = {
        case BackoffSupervisor.CurrentChild(ref) => innerChild = ref
        case SendToSupervisor(msg)               => supervisor ! msg
        case SendToInnerChild(msg)               => innerChild.foreach(_ ! msg)
        case SendToChildSelection(msg)           => selectedChild ! msg
      }
    }

    import InnerChild._
    import ParentActor._
    val parent = system.actorOf(ParentActor.props, "parent")
    Thread.sleep(500) // wait for GetCurrentChild

    parent ! SendToSupervisor(TestMessage("1 to supervisor"))
    parent ! SendToInnerChild(TestMessage("2 to innerChild"))
    parent ! SendToChildSelection(TestMessage("3 to selectedChild"))

    Thread.sleep(500)
  }

  "BackoffSupervisor 再重启子 actor 时，造成异常的消息和重启完成之前发送的消息都会丢失" in {
    class InnerChild extends Actor with ActorLogging {

      import InnerChild._

      override def receive = {
        case x @ TestMessage(msg) =>
          if (Random.nextBoolean()) log.info(s"Child receive message: $msg")
          else throw new ChildException(x)
      }
    }
    object InnerChild {
      case class TestMessage(msg:          String)
      class ChildException(val errMessage: TestMessage) extends Exception
      object ChildException {
        def apply(errMessage: TestMessage):    ChildException      = new ChildException(errMessage)
        def unapply(arg:      ChildException): Option[TestMessage] = Some(arg.errMessage)
      }
      def props = Props(new InnerChild())
    }

    object Supervisor extends StrictLogging {
      def props = {
        def decider: PartialFunction[Throwable, SupervisorStrategy.Directive] = {
          case InnerChild.ChildException(msg) =>
            logger.info(s"Message causing exception: ${msg.msg}")
            SupervisorStrategy.Restart
        }

        val options = BackoffOpts
          .onFailure(InnerChild.props, "innerChild", 1.milli, 5.millis, 0.0)
          .withManualReset
          .withSupervisorStrategy(
            OneForOneStrategy(
              maxNrOfRetries  = 500,
              withinTimeRange = 5.seconds
            )(decider.orElse(SupervisorStrategy.defaultDecider)))

        BackoffSupervisor.props(options)
      }
    }

    object ParentActor {

      case class SendToSupervisor(msg: InnerChild.TestMessage)

      case class SendToInnerChild(msg: InnerChild.TestMessage)

      case class SendToChildSelection(msg: InnerChild.TestMessage)

      def props = Props(new ParentActor)
    }
    class ParentActor extends Actor with ActorLogging {

      val supervisor = context.actorOf(Supervisor.props, "supervisor")
      supervisor ! BackoffSupervisor.GetCurrentChild
      var innerChild: Option[ActorRef] = None
      val selectedChild = context.actorSelection("/user/parent/supervisor/innerChild")

      override def receive = {
        case x => supervisor ! x
      }

    }

    import InnerChild._
    val parent = system.actorOf(ParentActor.props, "parent")

    for (i <- 1 to 20) {
      parent ! TestMessage(i.toString)
      Thread.sleep(300)
    }
  }

  // 1.恢复异常的消息 2.找回丢失的消息
  "解决 BackoffSupervisor 再重启子 actor 时消息丢失的问题" in {

    class InnerChild extends Actor with ActorLogging {

      import InnerChild._

      override def receive = {
        case x @ TestMessage(msg) =>
          if (Random.nextBoolean()) log.info(s"Child receive message: $msg")
          else throw new ChildException(x)
      }

      override def preRestart(reason: Throwable, message: Option[Any]) =
        super.preRestart(reason, message)
    }
    object InnerChild {
      case class TestMessage(msg:          String)
      class ChildException(val errMessage: TestMessage) extends Exception
      object ChildException {
        def apply(errMessage: TestMessage):    ChildException      = new ChildException(errMessage)
        def unapply(arg:      ChildException): Option[TestMessage] = Some(arg.errMessage)
      }
      def props = Props(new InnerChild())
    }
    var parent: ActorRef = null
    object Supervisor extends StrictLogging {
      def props = {
        def decider: PartialFunction[Throwable, SupervisorStrategy.Directive] = {
          case InnerChild.ChildException(msg) =>
            logger.info(s"Message causing exception: ${msg.msg}")
            // 1.恢复异常的消息
            parent ! msg
            SupervisorStrategy.Restart
        }

        val options = BackoffOpts
          .onFailure(InnerChild.props, "innerChild", 1.milli, 5.millis, 0.0)
          .withManualReset
          .withSupervisorStrategy(
            OneForOneStrategy(
              maxNrOfRetries  = 500,
              withinTimeRange = 5.seconds
            )(decider.orElse(SupervisorStrategy.defaultDecider)))

        BackoffSupervisor.props(options)
      }
    }

    object ParentActor {

      case class SendToSupervisor(msg: InnerChild.TestMessage)

      case class SendToInnerChild(msg: InnerChild.TestMessage)

      case class SendToChildSelection(msg: InnerChild.TestMessage)

      def props = Props(new ParentActor)
    }
    class ParentActor extends Actor with ActorLogging {

      val supervisor = context.actorOf(Supervisor.props, "supervisor")
      supervisor ! BackoffSupervisor.GetCurrentChild
      var innerChild: Option[ActorRef] = None
      val selectedChild = context.actorSelection("/user/parent/supervisor/innerChild")

      override def receive = {
        case x => supervisor ! x
      }

    }

    // 2.重发重启时未处理的消息
    class DeadLetterMonitor(receiver: ActorRef) extends Actor with ActorLogging {
      import context.dispatcher
      override def receive = {
        case DeadLetter(msg, _, _) if msg.isInstanceOf[InnerChild.TestMessage] =>
          context.system.scheduler
            .scheduleOnce(1.second, receiver, msg.asInstanceOf[InnerChild.TestMessage])
      }
    }

    parent = system.actorOf(ParentActor.props, "parent")
    val dlm = system.actorOf(Props(new DeadLetterMonitor(parent)), "dlMonitor")
    system.eventStream.subscribe(dlm, classOf[DeadLetter])

    for (i <- 1 to 20) {
      parent ! InnerChild.TestMessage(i.toString)
      Thread.sleep(300)
    }

    Thread.sleep(60 * 1000)
  }
}
