/*
 * Copyright (c) 2019.
 * OOON.ME ALL RIGHTS RESERVED.
 * Licensed under the Mozilla Public License, version 2.0
 * Please visit http://ooon.me or mail to zhaihao@ooon.me
 */

package sia.akka.remote.MultiNodesTestDemo

import akka.remote.testkit.MultiNodeConfig
import com.typesafe.scalalogging.StrictLogging
import sia.akka.STMultiNodeSpec

object MultiNodeSampleConfig extends MultiNodeConfig {
  val node1 = role("node1")
  val node2 = role("node2")
}
//#config

//#spec
import akka.remote.testkit.MultiNodeSpec
import akka.testkit.ImplicitSender
import akka.actor.{Props, Actor}

// 命名规则 {TestName}MultiJvm%
class MultiNodeSampleMultiJvm1 extends MultiNodeSample
class MultiNodeSampleMultiJvm2 extends MultiNodeSample

object MultiNodeSample {

  class Ponger extends Actor with StrictLogging {

    def receive = {
      case "ping" =>
        logger.info(sender().toString())
        sender() ! "pong"
    }
  }
}

class MultiNodeSample
    extends MultiNodeSpec(MultiNodeSampleConfig)
    with STMultiNodeSpec
    with ImplicitSender
    with StrictLogging {

  import MultiNodeSampleConfig._
  import MultiNodeSample._

  def initialParticipants = roles.size

  "A MultiNodeSample" in {

    // 具体节点执行
    runOn(node1) {
      enterBarrier("deployed") // 所有节点都达到相同 Barrier 再继续执行
      val ponger = system.actorSelection(node(node2) / "user" / "ponger")
      logger.info(ponger.toString())

      ponger ! "ping"
      import scala.concurrent.duration._
      expectMsg(10.seconds, "pong")
    }

    // 具体节点执行
    runOn(node2) {
      system.actorOf(Props[Ponger], "ponger")
      enterBarrier("deployed")
    }

    // 所有节点执行
    enterBarrier("finished")
  }

}
