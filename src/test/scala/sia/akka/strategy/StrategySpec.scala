/*
 * Copyright (c) 2019.
 * OOON.ME ALL RIGHTS RESERVED.
 * Licensed under the Mozilla Public License, version 2.0
 * Please visit http://ooon.me or mail to zhaihao@ooon.me
 */

package sia.akka.strategy

import akka.actor.{
  Actor,
  ActorLogging,
  ActorRef,
  ActorSystem,
  OneForOneStrategy,
  Props,
  SupervisorStrategy,
  Terminated
}
import akka.pattern.{AskTimeoutException, BackoffOpts, BackoffSupervisor}
import akka.testkit.TestKit
import akka.util.Timeout
import akka.pattern._
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.duration._
import test.BaseSpecLike

import scala.language.postfixOps
import scala.util.Random

/**
  * StrategySpec
  *
  * @author zhaihao
  * @version 1.0
  * @since 2019-07-21 18:41
  */
class StrategySpec extends TestKit(ActorSystem("cafe")) with BaseSpecLike with StrictLogging {

  /**
    * 这个例子中，只有 postStop 配执行，preRestart 和 postRestart 都没有被执行，说明每次是停掉旧的 actor，创建新的 actor
    */
  "test1" in {

    // 厨子
    object Chef {

      sealed trait Cooking

      case object CookSpecial extends Cooking

      class ChefBusy(msg: String) extends Exception(msg)

      def props = Props(new Chef)
    }

    class Chef extends Actor with ActorLogging {

      import Chef._

      log.info(s"Chef actor created at ${System.currentTimeMillis()}")

      override def receive = {
        case _ => throw new ChefBusy("Chef is busy cooking.")
      }

      override def preRestart(reason: Throwable, message: Option[Any]) = {
        super.preRestart(reason, message)
        log.info(s"Restarting Chef for $message")
      }

      override def postRestart(reason: Throwable) = {
        log.info(s"Chef restarted for ${reason.getMessage}")
        super.postRestart(reason)
      }

      override def postStop() = {
        log.info("Chef stopped!")
      }
    }

    // 厨房
    class Kitchen extends Actor with ActorLogging {
      override def receive: Receive = {
        case x =>
          context.children foreach { child =>
            child ! x
          }
      }
    }

    object Kitchen {
      def kitchenProps = {
        import Chef._
        val options = BackoffOpts
          .onFailure(Chef.props, "chef", 200 millis, 10 seconds, 0.0)
          .withSupervisorStrategy(
            OneForOneStrategy(maxNrOfRetries = 4, withinTimeRange = 30 seconds) {
              case _: ChefBusy => SupervisorStrategy.Restart
            })
        BackoffSupervisor.props(options)
      }
    }

    val kitchen = system.actorOf(Kitchen.kitchenProps, "kitchen")
    logger.info(s"Calling chef at ${System.currentTimeMillis()}")
    kitchen ! "CookCook"
    logger.info(s"Calling chef at ${System.currentTimeMillis()}")
    Thread.sleep(1000)
    logger.info(s"Calling chef at ${System.currentTimeMillis()}")
    kitchen ! "CookCook"
    Thread.sleep(1000)
    kitchen ! "CookCook"
    Thread.sleep(1000)
    kitchen ! "CookCook"
    Thread.sleep(1000)
    kitchen ! "CookCook"

    Thread.sleep(30000)
    system.terminate()
  }

  "test2" in {
    object ChildActor {

      class RndException(msg: String) extends Exception(msg)

      def props = Props(new ChildActor)
    }

    class ChildActor extends Actor with ActorLogging {

      import ChildActor._

      override def receive: Receive = {
        case msg: String =>
          if (Random.nextBoolean())
            throw new RndException("Any Exception!")
          else
            log.info(s"Processed message: $msg !!!")
      }

      override def preStart(): Unit = {
        log.info("ChildActor Started.")
        super.preStart()
      }

      override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
        log.info(s"Restarting ChildActor for ${reason.getMessage}...")
        message match {
          case Some(msg) =>
            log.info(s"Exception message: ${msg.toString}")
            self ! msg //把异常消息再摆放到信箱最后
          case None =>
        }
        super.preRestart(reason, message)
      }

      override def postRestart(reason: Throwable): Unit = {
        super.postRestart(reason)
        log.info(s"Restarted ChildActor for ${reason.getMessage}...")
      }

      override def postStop(): Unit = {
        log.info(s"Stopped ChildActor.")
        super.postStop()
      }

    }

    class Parent extends Actor with ActorLogging {

      def decider: PartialFunction[Throwable, SupervisorStrategy.Directive] = {
        case _: ChildActor.RndException => SupervisorStrategy.Restart
      }

      override val supervisorStrategy =
        OneForOneStrategy(maxNrOfRetries = 30, withinTimeRange = 3 seconds) {
          decider.orElse(SupervisorStrategy.defaultDecider)
        }

      val childActor = context.actorOf(ChildActor.props, "childActor")

      override def receive: Receive = {
        case msg => childActor ! msg
      }

    }

    val parentActor = system.actorOf(Props(new Parent), "parentActor")

    parentActor ! "Hello 1"
    parentActor ! "Hello 2"
    parentActor ! "Hello 3"
    parentActor ! "Hello 4"
    parentActor ! "Hello 5"

    Thread.sleep(5000)
    system.terminate()
  }

  "test3" in {
    object Chef {
      sealed trait Order
      case object MakeSpecial extends Order
      class ChefBusy(msg: String) extends Exception(msg)
      def props = Props(new Chef)
    }
    class Chef extends Actor with ActorLogging {
      import Chef._
      log.info("Chef says: I am ready to work ...")
      var currentSpecial: Cafe.Coffee = Cafe.Original
      var chefBusy:       Boolean     = false

      val specials = Map(0 -> Cafe.Original, 1 -> Cafe.Espresso, 2 -> Cafe.Cappuccino)

      override def receive: Receive = {
        case MakeSpecial =>
          if ((Random.nextInt(6) % 6) == 0) {
            log.info("Chef is busy ...")
            chefBusy = true
            throw new ChefBusy("Busy!")
          } else {
            currentSpecial = randomSpecial
            log.info(s"Chef says: Current special is ${currentSpecial.toString}.")
            sender() ! currentSpecial
          }
      }
      def randomSpecial = specials(Random.nextInt(specials.size))

      override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
        log.info(s"Restarting Chef for ${reason.getMessage}...")
        super.preRestart(reason, message)
      }

      override def postRestart(reason: Throwable): Unit = {
        log.info(s"Restarted Chef for ${reason.getMessage}.")
        context.parent ! BackoffSupervisor.Reset

        super.postRestart(reason)
      }

      override def postStop(): Unit = {
        log.info("Stopped Chef.")
        super.postStop()
      }
    }

    class Kitchen extends Actor with ActorLogging {
      override def receive: Receive = {
        case msg @ _ =>
          context.children foreach (chef => chef forward msg)
      }

      override def postStop(): Unit = {
        log.info("Kitchen close!")
        super.postStop()
      }
    }
    object Kitchen {
      val kitchenDecider: PartialFunction[Throwable, SupervisorStrategy.Directive] = {
        case _: Chef.ChefBusy => SupervisorStrategy.Restart
      }
      def kitchenProps: Props = {
        val option = BackoffOpts
          .onFailure(Chef.props, "chef", 1 seconds, 5 seconds, 0.0)
          .withManualReset
          .withSupervisorStrategy {
            OneForOneStrategy(maxNrOfRetries = 5, withinTimeRange = 5 seconds) {
              kitchenDecider.orElse(SupervisorStrategy.defaultDecider)
            }
          }
        BackoffSupervisor.props(option)
      }
    }

    object ReceiptPrinter {
      case class PrintReceipt(sendTo: ActorRef, receipt: Cafe.Receipt) //print command
      class PaperJamException extends Exception
      def props = Props(new ReceiptPrinter())
    }
    class ReceiptPrinter extends Actor with ActorLogging {
      import ReceiptPrinter._
      var paperJammed: Boolean = false
      override def receive: Receive = {
        case PrintReceipt(customerRef: ActorRef, receipt) => //打印收据并发送给顾客
          if ((Random.nextInt(6) % 6) == 0) {
            log.info("Printer jammed paper ...")
            paperJammed = true
            throw new PaperJamException
          } else {
            log.info(s"Printing receipt $receipt and sending to ${customerRef.path.name}")
            customerRef ! receipt
          }
      }

      override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
        log.info(s"Restarting ReceiptPrinter for ${reason.getMessage}...")
        super.preRestart(reason, message)
      }

      override def postRestart(reason: Throwable): Unit = {
        log.info(s"Started ReceiptPrinter for ${reason.getMessage}.")
        super.postRestart(reason)
      }

      override def postStop(): Unit = {
        log.info("Stopped ReceiptPrinter.")
        super.postStop()
      }
    }

    object Cashier {
      case class RingRegister(cup: Cafe.Coffee, customer: ActorRef) //收款并出具收据

      def props(kitchen: ActorRef) = Props(new Cashier(kitchen))
    }
    class Cashier(kitchen: ActorRef) extends Actor with ActorLogging {
      import Cashier._
      import ReceiptPrinter._

      context.watch(kitchen) //监视厨房。如果打烊了就关门歇业
      val printer = context.actorOf(ReceiptPrinter.props, "printer")
      //打印机卡纸后重启策略
      def cashierDecider: PartialFunction[Throwable, SupervisorStrategy.Directive] = {
        case _: PaperJamException => SupervisorStrategy.Restart
      }
      override def supervisorStrategy: SupervisorStrategy =
        OneForOneStrategy(maxNrOfRetries = 5, withinTimeRange = 5 seconds) {
          cashierDecider.orElse(SupervisorStrategy.defaultDecider)
        }
      val menu = Map[Cafe.Coffee, Double](Cafe.Original -> 5.50,
                                          Cafe.Cappuccino -> 12.95,
                                          Cafe.Espresso   -> 11.80)

      override def receive: Receive = {
        case RingRegister(coffee, customerRef) => //收款并出具收据
          log.info(s"Producing receipt for a cup of ${coffee.toString}...")
          val amt  = menu(coffee) //计价
          val rcpt = Cafe.Receipt(coffee.toString, amt)
          printer ! PrintReceipt(customerRef, rcpt) //打印收据。可能出现卡纸异常
          sender() ! Cafe.Sold(rcpt)             //通知Cafe销售成功  sender === Cafe
        case Terminated(_) =>
          log.info("Cashier says: Oh, kitchen is closed. Let's make the end of day!")
          context.system.terminate() //厨房打烊，停止营业。
      }
    }

    object Cafe {
      sealed trait Coffee
      case object Original   extends Coffee
      case object Espresso   extends Coffee
      case object Cappuccino extends Coffee

      case class Receipt(item: String, amt: Double)

      sealed trait Routine
      case object PlaceOrder extends Routine
      case class Sold(receipt: Receipt) extends Routine
    }
    class Cafe extends Actor with ActorLogging {
      import Cafe._
      import Cashier._

      import context.dispatcher
      implicit val timeout = Timeout(1 seconds)

      var totalAmount: Double = 0.0

      val kitchen = context.actorOf(Kitchen.kitchenProps, "kitchen")
      //Chef可能重启，但path不变。必须直接用chef ? msg，否则经Kitchen转发无法获取正确的sender
      val chef = context.actorSelection("/user/cafe/kitchen/chef")

      val cashier = context.actorOf(Cashier.props(kitchen), "cashier")

      var customer: ActorRef = _ //当前客户

      override def receive: Receive = {

        case Sold(rcpt) =>
          totalAmount += rcpt.amt
          log.info(s"Today's sales is up to $totalAmount")
          customer ! Customer.OrderServed(rcpt) //send him the order
          if (totalAmount > 100.00) {
            log.info("Asking kitchen to clean up ...")
            context.stop(kitchen)
          }
        case PlaceOrder =>
          customer = sender() //send coffee to this customer
          (for {
            item  <- (chef ? Chef.MakeSpecial).mapTo[Coffee]
            sales <- (cashier ? RingRegister(item, sender())).mapTo[Sold]
          } yield Sold(sales.receipt))
            .mapTo[Sold]
            .recover {
              case _: AskTimeoutException => Customer.ComebackLater
            }
            .pipeTo(self) //send receipt to be added to totalAmount

      }
    }

    object Customer {
      sealed trait CustomerOrder
      case object OrderSpecial extends CustomerOrder
      case class OrderServed(rcpt: Cafe.Receipt) extends CustomerOrder
      case object ComebackLater extends CustomerOrder
      def props(cafe: ActorRef) = Props(new Customer(cafe))
    }
    class Customer(cafe: ActorRef) extends Actor with ActorLogging {
      import Customer._
      import context.dispatcher
      override def receive: Receive = {
        case OrderSpecial =>
          log.info("Customer place an order ...")
          cafe ! Cafe.PlaceOrder
        case OrderServed(rcpt) =>
          log.info(s"Customer says: Oh my! got my order ${rcpt.item} for ${rcpt.amt}")
        case ComebackLater =>
          log.info("Customer is not so happy! says: I will be back later!")
          context.system.scheduler.scheduleOnce(1 seconds) { cafe ! Cafe.PlaceOrder }
      }
    }

    import scala.concurrent.ExecutionContext.Implicits.global
    val cafe     = system.actorOf(Props(new Cafe()), "cafe")
    val customer = system.actorOf(Customer.props(cafe), "customer")

    system.scheduler.schedule(1 second, 1 second, customer, Customer.OrderSpecial)

    Thread.sleep(60*1000)
  }
}
