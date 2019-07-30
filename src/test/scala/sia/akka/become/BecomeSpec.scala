/*
 * Copyright (c) 2019.
 * OOON.ME ALL RIGHTS RESERVED.
 * Licensed under the Mozilla Public License, version 2.0
 * Please visit http://ooon.me or mail to zhaihao@ooon.me
 */

package sia.akka.become

import akka.actor.{Actor, ActorSystem, Props, Stash}
import akka.testkit.{ImplicitSender, TestKitBase}
import com.typesafe.scalalogging.StrictLogging
import test.BaseSpec

/**
  * BecomeSpec
  *
  * @author zhaihao
  * @version 1.0
  * @since 2019-07-29 20:52
  */
class BecomeSpec extends BaseSpec with TestKitBase with ImplicitSender with StrictLogging {
  override implicit lazy val system = ActorSystem("test")

  "没有 unbecome 会导致栈溢出" in {
    case object HowYouFeel
    case object ToSummer
    case object ToSpring
    case object ToWinter

    class FillSeasons extends Actor with StrictLogging {
      override def receive = spring

      def winter: Receive = {
        case HowYouFeel =>
          logger.info("It's freezing cold!")
          sender() ! "1"
        case ToSummer => context.become(summer)
        case ToSpring => context.become(spring)

      }
      def summer: Receive = {
        case HowYouFeel =>
          logger.info("It's hot hot hot!")
          sender() ! "1"
        case ToSpring => context.become(spring)
        case ToWinter => context.become(winter)
      }
      def spring: Receive = {
        case HowYouFeel =>
          logger.info("It feels so good!")
          sender() ! "1"
        case ToSummer => context.become(summer)
        case ToWinter => context.become(winter)
      }
    }
    val feelingsActor = system.actorOf(Props(new FillSeasons), "feelingsActor")

    feelingsActor ! HowYouFeel
    feelingsActor ! ToSummer
    feelingsActor ! HowYouFeel

    feelingsActor ! ToWinter
    feelingsActor ! HowYouFeel
    feelingsActor ! ToSpring
    feelingsActor ! HowYouFeel

    receiveN(4)
  }

  "正确用法" in {
    sealed trait DBOperations
    case class DBWrite(sql: String) extends DBOperations
    case class DBRead(sql:  String) extends DBOperations

    sealed trait DBStates
    case object Connected    extends DBStates
    case object Disconnected extends DBStates

    // Stash 可以存储 db 未连接时的请求
    class DBOActor extends Actor with StrictLogging with Stash {
      override def receive = disconnected

      def disconnected: Receive = {
        case Connected =>
          logger.info("Logon to DB.")
          sender() ! 1
          context.become(connected)
          unstashAll()
        case _ => stash
      }

      def connected: Receive = {
        case Disconnected =>
          logger.info("Logoff from DB.")
          sender() ! 1
          context.unbecome()
        case DBWrite(sql) =>
          logger.info(s"Writing to DB: $sql")
          sender() ! 1
        case DBRead(sql) =>
          logger.info(s"Reading from DB: $sql")
          sender() ! 1
      }
    }

    val dbActor = system.actorOf(Props(new DBOActor), "dbActor")

    dbActor ! DBRead("Select from table x")
    dbActor ! Connected
    dbActor ! DBWrite("Update table x")
    dbActor ! DBRead("Select from table x")
    dbActor ! Disconnected

    receiveN(4)
  }

}
