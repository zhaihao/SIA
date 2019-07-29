/*
 * Copyright (c) 2019.
 * OOON.ME ALL RIGHTS RESERVED.
 * Licensed under the Mozilla Public License, version 2.0
 * Please visit http://ooon.me or mail to zhaihao@ooon.me
 */

package sia.akka.router.balancing

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.routing.FromConfig
import akka.testkit.{ImplicitSender, TestKit}
import com.typesafe.config.ConfigFactory
import test.BaseSpecLike

import scala.concurrent.duration._

/**
  * RouterPool1Spec
  *
  * @author zhaihao
  * @version 1.0
  * @since 2019-07-22 20:02
  */
class RouterPool1Spec
    extends TestKit(
      ActorSystem(
        "test",
        ConfigFactory.parseString(
          """
            |akka {
            |  loggers = ["akka.event.slf4j.Slf4jLogger"]
            |  loglevel = "DEBUG"
            |  logging-filter = "akka.event.slf4j.Slf4jLoggingFilter"
            |  prio-dispatcher {
            |    mailbox-type = "PriorityMailbox"
            |  }
            |  actor {
            |    deployment {
            |      /balance-pool-router {
            |        router = balancing-pool
            |        nr-of-instances = 3
            |        pool-dispatcher {
            |          executor = "fork-join-executor"
            |          # Configuration for the fork join pool
            |          fork-join-executor {
            |            # Min number of threads to cap factor-based parallelism number to
            |            parallelism-min = 3
            |            # Parallelism (threads) ... ceil(available processors * factor)
            |            parallelism-factor = 2.0
            |            # Max number of threads to cap factor-based parallelism number to
            |            parallelism-max = 3
            |          }
            |          # Throughput defines the maximum number of messages to be
            |          # processed per actor before the thread jumps to the next actor.
            |          # Set to 1 for as fair as possible.
            |          throughput = 1
            |        }
            |      }
            |    }
            |  }
            |
            |}
  """.stripMargin)
      ))
    with BaseSpecLike
    with ImplicitSender {

  "test1" in {
    object FibonacciRoutee {
      case class FibonacciNumber(nbr: Int)
      def props = Props(new FibonacciRoutee)
    }

    class FibonacciRoutee extends Actor with ActorLogging {
      import FibonacciRoutee._

      def fibonacci(nbr: Int) = {
        def fib(n: Int, b: Int, a: Int): Int = n match {
          case 0 => a
          case _ => fib(n - 1, a + b, b)
        }
        fib(nbr, 1, 0)
      }

      override def receive = {
        case FibonacciNumber(nbr) =>
          val answer = fibonacci(nbr)
          log.info(s"${self.path.name}'s answer: Fibonacci($nbr)=$answer")
          sender() ! answer
      }
    }

    import FibonacciRoutee._
    val router = system.actorOf(FromConfig.props(FibonacciRoutee.props), "balance-pool-router")

    router ! FibonacciNumber(10)
    router ! FibonacciNumber(13)
    router ! FibonacciNumber(15)

    router ! FibonacciNumber(17)

    expectMsgAllOf(10.seconds, 610, 233, 55, 1597)
  }

}
