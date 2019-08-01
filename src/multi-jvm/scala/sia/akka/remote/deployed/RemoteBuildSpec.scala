/*
 * Copyright (c) 2019.
 * OOON.ME ALL RIGHTS RESERVED.
 * Licensed under the Mozilla Public License, version 2.0
 * Please visit http://ooon.me or mail to zhaihao@ooon.me
 */

package sia.akka.remote.deployed

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
  * RemoteBuildSpec
  *
  * @author zhaihao
  * @version 1.0
  * @since 2019-08-02 11:47
  */
class RemoteBuildSpec
    extends MultiNodeSpec(RemoteBuildSpec)
    with STMultiNodeSpec
    with StrictLogging
    with ImplicitSender {
  override def initialParticipants = roles.size

  import RemoteBuildSpec._

  "test" in {
    runOn(node1) {
      enterBarrier("started.")
      val calc = system.actorOf(Props(new SupervisorActor), "supervisor")
      calc ! MathOps(op = Op.Reset)
      calc ! MathOps(10, Op.Set)
      calc ! MathOps(12, Op.Add)
      calc ! MathOps(op = Op.Get)
      expectMsg(22)
    }

    runOn(node2) {
      enterBarrier("started.")
    }

    enterBarrier("finished.")
  }
}

class RemoteBuildSpecMultiJvm1 extends RemoteBuildSpec
class RemoteBuildSpecMultiJvm2 extends RemoteBuildSpec

object RemoteBuildSpec extends MultiNodeConfig {
  val node1 = role("node1")
  val node2 = role("node2")

  deployOn(node1, """/supervisor.remote = "@node2@" """)

  commonConfig(
    ConfigFactory.parseString(
      //language=HOCON
      """
        |akka.actor {
        |  warn-about-java-serializer-usage = false
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
