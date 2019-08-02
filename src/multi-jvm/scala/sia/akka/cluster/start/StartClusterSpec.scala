/*
 * Copyright (c) 2019.
 * OOON.ME ALL RIGHTS RESERVED.
 * Licensed under the Mozilla Public License, version 2.0
 * Please visit http://ooon.me or mail to zhaihao@ooon.me
 */

package sia.akka.cluster.start

import akka.cluster.Cluster
import akka.cluster.ClusterEvent.{CurrentClusterState, MemberJoined, MemberUp}
import akka.remote.testkit.{MultiNodeConfig, MultiNodeSpec}
import akka.testkit.ImplicitSender
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.StrictLogging
import sia.akka.STMultiNodeSpec
import scala.concurrent.duration._

/**
  * StartClusterSpec
  *
  * @author zhaihao
  * @version 1.0
  * @since 2019-08-02 16:55
  */
class StartClusterSpec
    extends MultiNodeSpec(StartClusterSpec)
    with STMultiNodeSpec
    with StrictLogging
    with ImplicitSender {

  override def initialParticipants = roles.size

  import StartClusterSpec._

  "启动集群" in {
    logger.info("----")
    val node1Address1 = node(node1).address
    logger.info(node1Address1.toString)
    val node1Address2 = node(node2).address
    val node1Address3 = node(node3).address

    enterBarrier("abc")

    val cluster = Cluster(system)
    cluster.subscribe(testActor, classOf[MemberUp])
    expectMsgClass(classOf[CurrentClusterState])

    cluster.join(node1Address1) // seed

    receiveN(3, 10.seconds).collect {
      case MemberJoined(m) => logger.info(m.toString()); m.address
      case MemberUp(m)     => logger.info(m.toString()); m.address
    }.toSet ==> Set(node1Address1, node1Address2, node1Address3)

    cluster.unsubscribe(testActor)
    testConductor.enter("all-up")
  }
}

object StartClusterSpec extends MultiNodeConfig {
  val node1 = role("n1")
  val node2 = role("n2")
  val node3 = role("n3")

  def nodes = Seq(node1, node2, node3)

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
        |  actor {
        |    provider = cluster
        |
        |    deployment {
        |      /statsService/singleton/workerRouter {
        |        router = consistent-hashing-pool
        |        cluster {
        |          enabled = on
        |          max-nr-of-instances-per-node = 3
        |          allow-local-routees = on
        |          use-role = compute
        |        }
        |      }
        |    }
        |  }
        |
        |  remote {
        |    enabled-transports = [akka.remote.netty.tcp]
        |  }
        |
        |  cluster {
        |    roles = [compute]
        |  }
        |}
        |""".stripMargin))
}

class StartClusterSpecMultiJvmNode1 extends StartClusterSpec
class StartClusterSpecMultiJvmNode2 extends StartClusterSpec
class StartClusterSpecMultiJvmNode3 extends StartClusterSpec
