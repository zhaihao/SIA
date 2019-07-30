/*
 * Copyright (c) 2019.
 * OOON.ME ALL RIGHTS RESERVED.
 * Licensed under the Mozilla Public License, version 2.0
 * Please visit http://ooon.me or mail to zhaihao@ooon.me
 */

package sia.akka.fsm

import akka.actor.{ActorSystem, FSM, Props}
import akka.testkit.{ImplicitSender, TestKitBase}
import com.typesafe.scalalogging.StrictLogging
import test.BaseSpec

import scala.util.Random

/**
  * FSMSpec
  *
  * @author zhaihao
  * @version 1.0
  * @since 2019-07-30 14:43
  */
class FSMSpec extends BaseSpec with TestKitBase with ImplicitSender {
  override implicit lazy val system = ActorSystem("test")

  "test1" in {
    trait Season //States
    case object Spring extends Season
    case object Summer extends Season
    case object Fall   extends Season
    case object Winter extends Season

    case class SeasonInfo(talks: Int, month: Int) //Data

    sealed trait Messages //功能消息
    case object HowYouFeel extends Messages
    case object NextMonth  extends Messages

    class FillSeasons extends FSM[Season, SeasonInfo] with StrictLogging {

      startWith(Spring, SeasonInfo(0, 1))

      when(Spring) { //状态在春季
        case Event(HowYouFeel, seasonInfo) =>
          val numTalks = seasonInfo.talks + 1
          logger.info(s"It's ${stateName.toString}, feel so good! You've asked me ${numTalks}times.")
          stay using seasonInfo.copy(talks = numTalks)
      }
      when(Summer) { //夏季状态
        case Event(HowYouFeel, _) =>
          val numTalks = stateData.talks + 1
          logger.info(s"It's ${stateName.toString}, it's so hot! You've asked me ${numTalks}times")
          stay().using(stateData.copy(talks = numTalks))
      }
      when(Fall) { //秋季状态
        case Event(HowYouFeel, SeasonInfo(tks, mnth)) =>
          val numTalks = tks + 1
          logger.info(s"It's ${stateName.toString}, it's no so bad. You've asked me ${numTalks}times.")
          stay using SeasonInfo(numTalks, mnth)
      }
      when(Winter) { //冬季状态
        case Event(HowYouFeel, si @ SeasonInfo(tks, _)) =>
          val numTalks = tks + 1
          logger.info(
            s"It's ${stateName.toString}, it's freezing cold! You've asked me ${numTalks}times.")
          stay using si.copy(talks = numTalks)
      }

      whenUnhandled { //所有状态未处理的Event
        case Event(NextMonth, seasonInfo) =>
          val mth = seasonInfo.month
          if (mth <= 3) {
            logger.info(s"It's month ${mth + 1} of ${stateName.toString}")
            stay using seasonInfo.copy(month = mth + 1)
          } else {
            goto(nextSeason(stateName)) using SeasonInfo(0, 1)
          }
      }

      onTransition {
        case Spring -> Summer => logger.info("Season changed from Spring to Summer month 1")
        case Summer -> Fall   => logger.info("Season changed from Summer to Fall month 1")
        case Fall   -> Winter => logger.info("Season changed from Fall to Winter month 1")
        case Winter -> Spring => logger.info("Season changed from Winter to Spring month 1")
      }

      initialize()

      logger.info(s"It's month 1 of ${stateName.toString}")

      //季节转换顺序
      def nextSeason(season: Season): Season =
        season match {
          case Spring => Summer
          case Summer => Fall
          case Fall   => Winter
          case Winter => Spring
        }
    }

    val fsmActor = system.actorOf(Props(new FillSeasons), "fsmActor")

    (1 to 15).foreach { _ =>
      (1 to Random.nextInt(3)).foreach { _ =>
        fsmActor ! HowYouFeel
      }
      fsmActor ! NextMonth
    }

    import scala.concurrent.duration._
    expectNoMessage(10.seconds)
  }
}
