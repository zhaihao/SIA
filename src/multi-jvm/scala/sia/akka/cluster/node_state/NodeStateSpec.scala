/*
 * Copyright (c) 2019.
 * OOON.ME ALL RIGHTS RESERVED.
 * Licensed under the Mozilla Public License, version 2.0
 * Please visit http://ooon.me or mail to zhaihao@ooon.me
 */

package sia.akka.cluster.node_state

import akka.actor.{Actor, Props}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._
import akka.remote.testkit.{MultiNodeConfig, MultiNodeSpec}
import akka.testkit.ImplicitSender
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.StrictLogging
import sia.akka.STMultiNodeSpec

/**
  * NodeStateSpec
  *
  * @author zhaihao
  * @version 1.0
  * @since 2019-08-05 16:21
  */
class NodeStateSpec
    extends MultiNodeSpec(NodeStateSpec)
    with STMultiNodeSpec
    with StrictLogging
    with ImplicitSender {
  override def initialParticipants = roles.size

  import NodeStateSpec._

  "启动后各节点依次加入" in {
    system.actorOf(Props(new EventListenActor), "eventListener")
    val cluster = Cluster(system)
    enterBarrier("wait to joining")
    // 第一个节点讲成为 seed
    cluster.join(node(node1).address)

    enterBarrier("end")
    expectNoMessage()
  }
}

object NodeStateSpec extends MultiNodeConfig {
  val node1 = role("node1")
  val node2 = role("node2")
  val node3 = role("node3")

  val nodes = Seq(node1, node2, node3)

  nodes.foreach { node =>
    nodeConfig(node)(
      ConfigFactory.parseString(
        //language=HOCON
        s"""
           |akka{
           |  extensions = ["akka.cluster.metrics.ClusterMetricsExtension"]
           |
           |  cluster{
           |    metrics.native-library-extract-folder=target/native/${node.name}
           |  }
           |}
           |""".stripMargin))
  }

  commonConfig(
    ConfigFactory.parseString(
      //language=HOCON
      """
        |akka {
        |  loggers = ["akka.event.slf4j.Slf4jLogger"]
        |  loglevel = "DEBUG"
        |  log-dead-letters-during-shutdown = false
        |  actor {
        |    provider = cluster
        |  }
        |
        |  remote {
        |    artery {
        |      enabled = on
        |      transport = tcp
        |    }
        |  }
        |
        |  cluster {
        |    roles = [compute]
        |  }
        |}
        |""".stripMargin))

}

class NodeStateSpecMultiJvm1 extends NodeStateSpec
class NodeStateSpecMultiJvm2 extends NodeStateSpec
class NodeStateSpecMultiJvm3 extends NodeStateSpec

private[node_state] class EventListenActor extends Actor with StrictLogging {

  val cluster = Cluster(context.system)

  override def preStart() = {
    cluster.subscribe(self, InitialStateAsEvents, classOf[MemberEvent], classOf[UnreachableMember])
  }

  override def receive = {
    case MemberJoined(member) =>
      logger.warn("Member is Joining: {}", member.address)
    case MemberUp(member) =>
      logger.warn("Member is Up: {}", member.address)
    case MemberLeft(member) =>
      logger.warn("Member is Leaving: {}", member.address)
    case MemberExited(member) =>
      logger.warn("Member is Exiting: {}", member.address)
    case MemberRemoved(member, previousStatus) =>
      logger.warn("Member is Removed: {} after {}", member.address, previousStatus)
    case UnreachableMember(member) =>
      logger.warn("Member detected as unreachable: {}", member)
      cluster.down(member.address) //手工驱除，不用auto-down
    case _: MemberEvent => // ignore
  }
}
