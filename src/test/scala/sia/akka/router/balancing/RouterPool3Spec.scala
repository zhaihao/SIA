/*
 * Copyright (c) 2019.
 * OOON.ME ALL RIGHTS RESERVED.
 * Licensed under the Mozilla Public License, version 2.0
 * Please visit http://ooon.me or mail to zhaihao@ooon.me
 */

package sia.akka.router.balancing

import akka.actor.{Actor, ActorLogging, ActorSystem, DeadLetter, OneForOneStrategy, Props, SupervisorStrategy}
import akka.routing.{DefaultResizer, RoundRobinPool}
import akka.testkit.TestKitBase
import com.typesafe.config.ConfigFactory
import test.BaseSpec

import scala.concurrent.duration._
import scala.util.Random

/**
  * RouterPool3Spec
  *
  * @author zhaihao
  * @version 1.0
  * @since 2019-07-22 20:02
  */
class RouterPool3Spec extends BaseSpec with TestKitBase {

  override implicit lazy val system = ActorSystem(
    "test",
    ConfigFactory.parseString("""
                                |akka.loggers = ["akka.event.slf4j.Slf4jLogger"]
                                |akka.actor.debug.unhandled = on
                              """.stripMargin)
  )

  "test1" in {

    object FibonacciRoutee {

      sealed trait FibonacciMessage
      case class FibonacciNumber(nbr: Int, msDelay: Int) extends FibonacciMessage
      case class GetAnswer(nbr:       Int) extends FibonacciMessage

      class RouteeException(msg: String) extends Exception(msg)

      def props = Props(new FibonacciRoutee)
    }

    import FibonacciRoutee._
    class FibonacciRoutee extends Actor with ActorLogging {

      import context.dispatcher

      def fibonacci(nbr: Int) = {
        def fib(n: Int, b: Int, a: Int): Int = n match {
          case 0 => a
          case _ => fib(n - 1, a + b, b)
        }

        fib(nbr, 1, 0)
      }

      val testActor = context.actorSelection("/system/testActor-1")

      override def receive = {
        case FibonacciNumber(nbr, msDelay) =>
          context.system.scheduler.scheduleOnce(msDelay.millis, self, GetAnswer(nbr))
        case GetAnswer(nbr) =>
          if (Random.nextBoolean()) {
            throw new RouteeException("some error")
          } else {
            val answer = fibonacci(nbr)
            log.info(s"${self.path.name}'s answer: Fibonacci($nbr)=$answer")
            testActor ! nbr
          }
      }

      override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
        log.info(s"Restarting ${self.path.name} on ${reason.getMessage}")
        message foreach { m =>
          self ! m
        }
        super.preRestart(reason, message)
      }

      override def postRestart(reason: Throwable): Unit = {
        log.info(s"Restarted ${self.path.name} on ${reason.getMessage}")
        super.postRestart(reason)
      }

      override def postStop(): Unit = {
        log.info(s"Stopped ${self.path.name}!")
        super.postStop()
      }
    }

    val routingDecider: PartialFunction[Throwable, SupervisorStrategy.Directive] = {
      case _: FibonacciRoutee.RouteeException => SupervisorStrategy.Restart
    }

    val routerSupervisorStrategy =
      OneForOneStrategy(maxNrOfRetries = 5, withinTimeRange = 5.seconds) {
        routingDecider.orElse(SupervisorStrategy.defaultDecider)
      }

    val resizer = DefaultResizer(
      lowerBound        = 2,
      upperBound        = 5,
      pressureThreshold = 1,
      rampupRate        = 1,
      backoffRate       = 0.25,
      backoffThreshold  = 0.25,
      messagesPerResize = 1
    )

    val router = system.actorOf(
      RoundRobinPool(nrOfInstances      = 2,
                     resizer            = Some(resizer),
                     supervisorStrategy = routerSupervisorStrategy).props(FibonacciRoutee.props),
      "round-robin-pool-router"
    )

    class DeadLetterActor extends Actor with ActorLogging {

      val router = context.actorSelection("/user/round-robin-pool-router")

      override def receive = {
        case DeadLetter(msg, _, _) if msg.isInstanceOf[FibonacciMessage] =>
          log.warning(s"dead letter:$msg")
          router ! msg
      }
    }

    val dead = system.actorOf(Props(new DeadLetterActor), "dead")
    system.eventStream.subscribe(dead, classOf[DeadLetter])

    for (i <- 1 to 20) {
      router ! FibonacciNumber(i, Random.nextInt(1000))
    }

    expectMsgAllOf(30.seconds, 1 to 20: _*)

  }

}
