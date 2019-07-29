/*
 * Copyright (c) 2019.
 * OOON.ME ALL RIGHTS RESERVED.
 * Licensed under the Mozilla Public License, version 2.0
 * Please visit http://ooon.me or mail to zhaihao@ooon.me
 */

package sia.akka.router.balancing

import akka.actor.{Actor, ActorLogging, ActorSystem, OneForOneStrategy, Props, SupervisorStrategy}
import akka.routing.BalancingPool
import akka.testkit.TestKitBase
import com.typesafe.config.ConfigFactory
import test.BaseSpec

import scala.concurrent.duration._
import scala.util.Random

/**
  * RouterPool2Spec
  *
  * @author zhaihao
  * @version 1.0
  * @since 2019-07-22 20:02
  */
class RouterPool2Spec extends BaseSpec with TestKitBase {

  override implicit lazy val system = ActorSystem(
    "test",
    ConfigFactory.parseString("""
                                |akka.loggers = ["akka.event.slf4j.Slf4jLogger"]
                                |akka.actor.debug.unhandled = on
                              """.stripMargin)
  )

  import FibonacciRoutee._

  "test1" in {

    val routingDecider: PartialFunction[Throwable, SupervisorStrategy.Directive] = {
      case _: RouteeException => SupervisorStrategy.Restart
    }

    val routerSupervisorStrategy =
      OneForOneStrategy(maxNrOfRetries = 5, withinTimeRange = 5.seconds) {
        routingDecider.orElse(SupervisorStrategy.defaultDecider)
      }

    val router = system.actorOf(
      BalancingPool(nrOfInstances = 3, supervisorStrategy = routerSupervisorStrategy)
        .props(FibonacciRoutee.props),
      "balance-pool-router"
    )

    router ! FibonacciNumber(10, 5000)
    router ! FibonacciNumber(13, 2000)
    router ! FibonacciNumber(15, 3000)
    router ! FibonacciNumber(17, 1000)

    expectMsgAllOf(10.seconds, 610, 233, 55, 1597)
  }

}

private[router] object FibonacciRoutee {

  case class FibonacciNumber(nbr: Int, msDelay: Int)

  case class GetAnswer(nbr: Int)

  class RouteeException(msg: String) extends Exception(msg)

  def props = Props(new FibonacciRoutee)
}

private[router] class FibonacciRoutee extends Actor with ActorLogging {

  import FibonacciRoutee._
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
        testActor ! answer
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
