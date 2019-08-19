/*
 * Copyright (c) 2019.
 * OOON.ME ALL RIGHTS RESERVED.
 * Licensed under the Mozilla Public License, version 2.0
 * Please visit http://ooon.me or mail to zhaihao@ooon.me
 */

package sia.akka.cluster.cluster_sharding

import akka.actor.{Actor, OneForOneStrategy, Props, SupervisorStrategy}
import akka.cluster.Cluster
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings, ShardRegion}
import akka.persistence.journal.leveldb.{SharedLeveldbJournal, SharedLeveldbStore}
import akka.persistence.{PersistentActor, SnapshotOffer}
import akka.remote.testkit.{MultiNodeConfig, MultiNodeSpec}
import akka.testkit.ImplicitSender
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.StrictLogging
import sia.akka.STMultiNodeSpec

import scala.concurrent.Await
import scala.concurrent.duration._

/**
  * ClusterShardingSpec
  *
  * @author zhaihao
  * @version 1.0
  * @since 2019-08-09 11:13
  */
class ClusterShardingSpec
    extends MultiNodeSpec(ClusterShardingSpec)
    with STMultiNodeSpec
    with ImplicitSender
    with StrictLogging {

  import ClusterShardingSpec._

  override def initialParticipants = roles.size
  implicit val timeout = Timeout(15.seconds)

  "test" in {
    val cluster = Cluster(system)
    cluster.join(node(node1).address) // 手动设置 seed
    enterBarrier("集群上线")

    runOn(node1) {
      val store = system.actorOf(Props[SharedLeveldbStore], "store")
      SharedLeveldbJournal.setStore(store, system)
      enterBarrier("等待启动 store")
      enterBarrier("store 已启动")
    }

    runOn(nodes.filter(_ != node1): _*) {
      enterBarrier("等待启动 store")

      val ref = Await.result(system.actorSelection(node(node1) / "user" / "store").resolveOne(),
                             timeout.duration)

      SharedLeveldbJournal.setStore(ref, system)
      enterBarrier("store 已启动")
    }

    ClusterSharding(system).start(
      typeName        = CalcShard.shardName,
      entityProps     = CalcShard.entityProps,
      settings        = ClusterShardingSettings(system),
      extractEntityId = CalcShard.getEntityId,
      extractShardId  = CalcShard.getShardId
    )

    enterBarrier("shard actor已启动")
    logger.trace("开始执行逻辑")

    runOn(nodes.filter(_ != node1): _*) {
      enterBarrier("finished")
    }

    runOn(node1) {
      val calcRegion = ClusterSharding(system).shardRegion(CalcShard.shardName)

      // 按首字母 sharding，同首字母的在统一节点计算
      calcRegion ! CalcShard.CalcShardMessage("1", Num(1.0))
      calcRegion ! CalcShard.CalcShardMessage("1", Add(0))
      calcRegion ! CalcShard.CalcShardMessage("1", ShowResult)
      calcRegion ! CalcShard.CalcShardMessage("12", Num(2.0))
      calcRegion ! CalcShard.CalcShardMessage("12", Add(0))
      calcRegion ! CalcShard.CalcShardMessage("12", ShowResult)

      calcRegion ! CalcShard.CalcShardMessage("23", Num(3.0))
      calcRegion ! CalcShard.CalcShardMessage("23", Add(0))
      calcRegion ! CalcShard.CalcShardMessage("23", ShowResult)
      calcRegion ! CalcShard.CalcShardMessage("24", Num(4.0))
      calcRegion ! CalcShard.CalcShardMessage("24", Add(0))
      calcRegion ! CalcShard.CalcShardMessage("24", ShowResult)
      calcRegion ! CalcShard.CalcShardMessage("25", Num(5.0))
      calcRegion ! CalcShard.CalcShardMessage("25", Add(0))
      calcRegion ! CalcShard.CalcShardMessage("25", ShowResult)

      calcRegion ! CalcShard.CalcShardMessage("36", Num(6.0))
      calcRegion ! CalcShard.CalcShardMessage("36", Add(0))
      calcRegion ! CalcShard.CalcShardMessage("36", ShowResult)

      calcRegion ! CalcShard.CalcShardMessage("47", Num(7.0))
      calcRegion ! CalcShard.CalcShardMessage("47", Add(0))
      calcRegion ! CalcShard.CalcShardMessage("47", ShowResult)

      calcRegion ! CalcShard.CalcShardMessage("4", Num(8.0))
      calcRegion ! CalcShard.CalcShardMessage("4", Add(0))
      calcRegion ! CalcShard.CalcShardMessage("4", ShowResult)

      //      calcRegion ! CalcShard.CalcShardMessage("2011", Num(10.0)) //shard 2, entity 2012
//      calcRegion ! CalcShard.CalcShardMessage("2011", Mul(3.0))
//      calcRegion ! CalcShard.CalcShardMessage("2011", Div(2.0))
//      calcRegion ! CalcShard.CalcShardMessage("1011", ShowResult)
//      calcRegion ! CalcShard.CalcShardMessage("2011", Div(0.0))

      expectNoMessage(3.seconds)

      enterBarrier("finished")
    }

  }
}

class ClusterShardingSpecMultiJvm1 extends ClusterShardingSpec
class ClusterShardingSpecMultiJvm2 extends ClusterShardingSpec
class ClusterShardingSpecMultiJvm3 extends ClusterShardingSpec
class ClusterShardingSpecMultiJvm4 extends ClusterShardingSpec

object ClusterShardingSpec extends MultiNodeConfig {

  object CalcShard {
    case class CalcShardMessage(eid: String, msg: Command)

    val shardName = "calcShard"
    def entityProps = Props[CalcSupervisor]

    val getEntityId: ShardRegion.ExtractEntityId = {
      case CalcShardMessage(id, msg) => (id, msg)
    }

    val getShardId: ShardRegion.ExtractShardId = {
      case CalcShardMessage(id, _) => id.head.toString
    }
  }

  val nodes @ Seq(node1, node2, node3, node4) = (1 to 4).map(i => role(s"node$i"))

  nodes.foreach { node =>
    val i    = node.name.last.toString.toInt
    val port = 25500 + i
    nodeConfig(node)(
      ConfigFactory.parseString(
        //language=HOCON
        s"""
           |akka.remote.artery.canonical.hostname = "127.0.0.1"
           |akka.remote.artery.canonical.port = "$port"
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
        |  log-dead-letters = 0
        |  actor {
        |    provider = cluster
        |    warn-about-java-serializer-usage = false
        |  }
        |
        |  remote {
        |    artery {
        |      enabled = on
        |      transport = tcp
        |    }
        |  }
        |
        |
        |  cluster {
        |    roles = [compute]
        |  }
        |  persistence {
        |    journal{
        |      plugin = "akka.persistence.journal.leveldb-shared"
        |      leveldb-shared.store {
        |        native = false
        |        dir = "target/shared-journal"
        |      }
        |    }
        |    snapshot-store{
        |      plugin = "akka.persistence.snapshot-store.local"
        |      local.dir = "target/snapshots"
        |    }
        |  }
        |}
        |""".stripMargin))
}

private[cluster_sharding] sealed trait Command
private[cluster_sharding] case class Num(d: Double) extends Command
private[cluster_sharding] case class Add(d: Double) extends Command
private[cluster_sharding] case class Sub(d: Double) extends Command
private[cluster_sharding] case class Mul(d: Double) extends Command
private[cluster_sharding] case class Div(d: Double) extends Command
private[cluster_sharding] case object ShowResult extends Command
private[cluster_sharding] case object Disconnect extends Command
private[cluster_sharding] case class SetResult(d: Double) extends Command

private[cluster_sharding] class Calculator extends PersistentActor with StrictLogging {

  var state = 0.0
  val cluster = Cluster(context.system)

  override def receiveRecover = {
    case setResult: SetResult => state = setResult.d
    case SnapshotOffer(_, st: Double) => state = st
  }

  def getResult(res: Double, cmd: Command): Double = cmd match {
    case Num(x) => x
    case Add(x) => res + x
    case Sub(x) => res - x
    case Mul(x) => res * x
    case Div(x) =>
      val _ = res.toInt / x.toInt //yield ArithmeticException when /0.00
      res / x
    case _ => throw new ArithmeticException("Invalid Operation!")
  }

  override def receiveCommand = {
    case cmd @ (_: Num | _: Add | _: Sub | _: Mul | _: Div) =>
      persist(cmd)(cmd => state = getResult(state, cmd.asInstanceOf[Command]))
    case ShowResult =>
      persist(ShowResult) { _ =>
        logger.trace(s"Result on ${cluster.selfAddress.hostPort} is: $state")
      }
    case Disconnect =>
      cluster.leave(cluster.selfAddress)

  }

  override def persistenceId = self.path.parent.name + "-" + self.path.name

  override def preRestart(reason: Throwable, message: Option[Any]) = {
    logger.warn(s"Restarting calculator: ${reason.getMessage}")
    super.preRestart(reason, message)
  }
}

private[cluster_sharding] class CalcSupervisor extends Actor {

  val decider: PartialFunction[Throwable, SupervisorStrategy.Directive] = {
    case _: ArithmeticException => SupervisorStrategy.Resume
  }

  override def supervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 5, withinTimeRange = 5.seconds, loggingEnabled = true) {
      decider.orElse(SupervisorStrategy.defaultDecider)
    }

  val calc = context.actorOf(Props[Calculator], "calc")

  override def receive = {
    case msg => calc forward msg
  }
}
