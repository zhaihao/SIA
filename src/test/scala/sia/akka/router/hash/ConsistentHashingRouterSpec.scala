/*
 * Copyright (c) 2019.
 * OOON.ME ALL RIGHTS RESERVED.
 * Licensed under the Mozilla Public License, version 2.0
 * Please visit http://ooon.me or mail to zhaihao@ooon.me
 */

package sia.akka.router.hash

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.routing.ConsistentHashingPool
import akka.routing.ConsistentHashingRouter.ConsistentHashable
import akka.testkit.{ImplicitSender, TestKitBase}
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.StrictLogging
import test.BaseSpec

/**
  * ConsistentHashingRouterSpec
  *
  * @author zhaihao
  * @version 1.0
  * @since 2019-07-29 16:22
  */
class ConsistentHashingRouterSpec
    extends BaseSpec
    with TestKitBase
    with StrictLogging
    with ImplicitSender {
  override implicit lazy val system =
    ActorSystem(
      "test",
      //language=HOCON
      ConfigFactory.parseString("""
        akka.loggers = ["akka.event.slf4j.Slf4jLogger"]
                                """.stripMargin)
    )

  "test1" in {
    object MoneyCounter {
      sealed trait Counting
      case class OneHand(cur:     String, amt: Double) extends Counting
      case class ReportTotal(cur: String) extends Counting
    }

    class MoneyCounter extends Actor {
      import MoneyCounter._
      var currency: String = "RMB"
      var amount:   Double = 0

      override def receive: Receive = {
        case OneHand(cur, amt) =>
          currency = cur
          amount += amt
          logger.info(s"${self.path.name} received one hand of $amt$cur")
          sender() ! s"received one hand of $amt$cur"
        case ReportTotal(_) =>
          logger.info(s"${self.path.name} has a total of $amount$currency")
          sender() ! s"total of $amount$currency"
      }
    }

    import MoneyCounter._
    def mcHashMapping: PartialFunction[Any, Any] = {
      case OneHand(cur, _)  => cur
      case ReportTotal(cur) => cur
    }

    val router = system.actorOf(
      ConsistentHashingPool(nrOfInstances = 5, hashMapping = mcHashMapping, virtualNodesFactor = 2)
        .props(Props(new MoneyCounter)),
      "moneyCounter"
    )

    router ! OneHand("RMB", 10.00)
    router ! OneHand("USD", 10.00)
    router ! OneHand("HKD", 10.00)
    router ! OneHand("RMB", 10.00)
    router ! OneHand("CHF", 10.00)

    router ! ReportTotal("RMB")
    router ! ReportTotal("USD")

    receiveN(7)
  }

  "test2" in {
    object MoneyCounter {
      sealed class Counting(cur: String) extends ConsistentHashable {
        override def consistentHashKey: Any = cur
      }
      case class OneHand(cur:     String, amt: Double) extends Counting(cur)
      case class ReportTotal(cur: String) extends Counting(cur)
    }

    class MoneyCounter extends Actor with ActorLogging {
      import MoneyCounter._
      var currency: String = "RMB"
      var amount:   Double = 0

      override def receive: Receive = {
        case OneHand(cur, amt) =>
          currency = cur
          amount += amt
          log.info(s"${self.path.name} received one hand of $amt$cur")
          sender() ! s"received one hand of $amt$cur"
        case ReportTotal(_) =>
          log.info(s"${self.path.name} has a total of $amount$currency")
          sender() ! s"total of $amount$currency"
      }
    }

    import MoneyCounter._

    val router = system.actorOf(
      ConsistentHashingPool(nrOfInstances = 5, virtualNodesFactor = 2)
        .props(Props(new MoneyCounter)),
      "moneyCounter"
    )

    router ! OneHand("RMB", 10.00)
    router ! OneHand("USD", 10.00)
    router ! OneHand("HKD", 10.00)
    router ! OneHand("RMB", 10.00)
    router ! OneHand("CHF", 10.00)

    router ! ReportTotal("RMB")
    router ! ReportTotal("USD")

    receiveN(7)
  }

}
