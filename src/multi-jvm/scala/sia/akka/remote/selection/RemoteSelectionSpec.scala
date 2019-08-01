/*
 * Copyright (c) 2019.
 * OOON.ME ALL RIGHTS RESERVED.
 * Licensed under the Mozilla Public License, version 2.0
 * Please visit http://ooon.me or mail to zhaihao@ooon.me
 */

package sia.akka.remote.selection

import akka.actor.{Actor, OneForOneStrategy, Props, SupervisorStrategy}
import akka.remote.testkit.{MultiNodeConfig, MultiNodeSpec}
import akka.testkit.ImplicitSender
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.StrictLogging
import sia.akka.STMultiNodeSpec
import sia.akka.remote.MathOps
import sia.akka.remote.MathOps.Op

import scala.concurrent.duration._

/**
  * RemoteSelectionSpec
  *
  * @author zhaihao
  * @version 1.0
  * @since 2019-07-31 19:22
  */
class RemoteSelectionSpec
    extends MultiNodeSpec(RemoteSelectionSpec)
    with STMultiNodeSpec
    with ImplicitSender
    with StrictLogging {
  override def initialParticipants = roles.size

  import RemoteSelectionSpec._

  "test" in {
    runOn(node1) {
      system.actorOf(Props[SupervisorActor], "supervisor")
      enterBarrier("deployed")
    }

    runOn(node2) {
      enterBarrier("deployed")
      import system.dispatcher
      system
        .actorSelection(node(node1) / "user" / "supervisor")
        .resolveOne(3.seconds)
        .foreach { supervisor =>
          logger.warn(s"supervisor: $supervisor")
          supervisor ! MathOps(10, Op.Add)
          supervisor ! MathOps(0, Op.Div)
          supervisor ! MathOps(op = Op.Get)
        }

      expectMsg(10.0)
    }

    enterBarrier("end")
  }
}

class RemoteSelectionSpecMultiJvm1 extends RemoteSelectionSpec
class RemoteSelectionSpecMultiJvm2 extends RemoteSelectionSpec

object RemoteSelectionSpec extends MultiNodeConfig {
  val node1 = role("node1")
  val node2 = role("node2")

  commonConfig(
    ConfigFactory.parseString(
      //language=HOCON
      """
        |akka.actor {
        |  serializers {
        |    proto = akka.remote.serialization.ProtobufSerializer
        |  }
        |  // 配置 trait 或 abstract class 即可
        |  serialization-bindings {
        |    "scalapb.GeneratedMessage" = proto
        |  }
        |}
        |""".stripMargin))

}

class Calculator extends Actor with StrictLogging {

  var result = 0.0

  override def receive: Receive = {
    case MathOps(num, op) =>
      op match {
        case Op.Set => result = num
        case Op.Get =>
          logger.warn(s"sender: ${sender()}")
          logger.warn(s"self: $self")
          sender() ! result
        case Op.Reset => result = 0.0
        case Op.Add   => result += num
        case Op.Sub   => result -= num
        case Op.Mul   => result *= num
        case Op.Div =>
          val _ = result.toInt / num.toInt
          result /= num
        case _ => sys.error("not support op")
      }
  }

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    logger.info(s"Restarting calculator: ${reason.getMessage}")
    super.preRestart(reason, message)
  }
}

class SupervisorActor extends Actor with StrictLogging {
  override def receive = {
    case msg => calc forward msg
  }

  val calc = context.actorOf(Props(new Calculator), "calc")

  override def supervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 5, withinTimeRange = 5.seconds) {
      val value: PartialFunction[Throwable, SupervisorStrategy.Directive] = {
        case _: ArithmeticException => SupervisorStrategy.Resume
      }
      value.orElse(SupervisorStrategy.defaultDecider)
    }
}
