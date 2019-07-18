/*
 * Copyright (c) 2019.
 * OOON.ME ALL RIGHTS RESERVED.
 * Licensed under the Mozilla Public License, version 2.0
 * Please visit http://ooon.me or mail to zhaihao@ooon.me
 */

package sia.scala.collection

import com.typesafe.scalalogging.StrictLogging
import test.BaseSpec

/**
  * ListSpec
  *
  * @author zhaihao
  * @version 1.0
  * @since 2019-07-18 23:19
  */
class ListSpec extends BaseSpec with StrictLogging {
  val list = List(1, 2, 3, 4, 5, 6, 7, 8, 9)
  "apply" in {
    List(1, 2, 3) ==> Cons(1, Cons(2, Cons(3, Nil)))
  }

  "head" in {
    list.head ==> 1
  }

  "tail" in {
    list.tail ==> List(2, 3, 4, 5, 6, 7, 8, 9)
  }

  "take" in {
    list.take(2) ==> List(1, 2)
  }

  "takeWhile" in {
    list.takeWhile(_ < 3) ==> List(1, 2)
  }

  "drop" in {
    list.drop(5) ==> List(6, 7, 8, 9)
  }

  "dropWhile" in {
    list.dropWhile(_ < 8) ==> List(8, 9)
  }

  "++" in {
    List(1, 2) ++ List(2, 3) ==> List(1, 2, 2, 3)
  }

  "length" in {
    list.length ==> 9
  }

  "map" in {
    List(1, 2, 3).map(_.toString) ==> List("1", "2", "3")
  }

  "flatMap" in {
    List(1, 2).flatMap(i => List(i, i)) ==> List(1, 1, 2, 2)
  }

  "filter" in {
    list.filter(_ % 2 == 0) ==> List(2, 4, 6, 8)
  }

  "foldRight" in {
    List(1, 2, 3).foldRight("0") { (a, b) =>
      logger.info(b)
      a + b
    } ==> "1230"

  }

  "foldLeft" in {
    List(1, 2, 3).foldLeft("0") { (b, a) =>
      logger.info(b)
      b + a
    } ==> "0123"
  }

  "reduceRight" in {
    List(1, 2, 3).reduceRight { (a, b) =>
      logger.info(a + b.toString)
      (a.toString + b.toString).toInt
    } ==> 123
  }

}

private[collection] trait List[+A] {

  def head: A = this match {
    case Cons(h, _) => h
    case Nil        => sys.error("Empty List!")
  }

  def tail: List[A] = this match {
    case Cons(_, t) => t
    case Nil        => sys.error("Empty List!")
  }

  def take(n: Int): List[A] = n match {
    case k if k < 0 => sys.error("index < 0 !")
    case 0          => Nil
    case _ =>
      this match {
        case Cons(h, t) => Cons(h, t.take(n - 1))
        case Nil        => Nil
      }
  }

  def takeWhile(f: A => Boolean): List[A] = this match {
    case Nil        => Nil
    case Cons(h, t) => if (f(h)) Cons(h, t.takeWhile(f)) else Nil
  }

  def drop(n: Int): List[A] = n match {
    case k if k < 0 => sys.error("index < 0 !")
    case 0          => this
    case _ =>
      this match {
        case Nil        => Nil
        case Cons(_, t) => t.drop(n - 1)
      }
  }

  def dropWhile(f: A => Boolean): List[A] = this match {
    case Nil        => Nil
    case Cons(h, t) => if (f(h)) t.dropWhile(f) else this
  }

  def ++[B >: A](that: List[B]): List[B] = this match {
    case Nil        => that
    case Cons(h, t) => Cons(h, t ++ that)
  }

  def length: Int = this match {
    case Nil        => 0
    case Cons(h, t) => 1 + t.length
  }

  def map[B](f: A => B): List[B] = this match {
    case Nil        => Nil
    case Cons(h, t) => Cons(f(h), t.map(f))
  }

  def flatMap[B](f: A => List[B]): List[B] = this match {
    case Nil        => Nil
    case Cons(h, t) => f(h) ++ t.flatMap(f)
  }

  def filter(f: A => Boolean): List[A] = this match {
    case Nil        => Nil
    case Cons(h, t) => if (f(h)) Cons(h, t.filter(f)) else t.filter(f)
  }

  def foldRight[B](z: B)(f: (A, B) => B): B = this match {
    case Nil        => z
    case Cons(h, t) => f(h, t.foldRight(z)(f))
  }

  def foldLeft[B](z: B)(f: (B, A) => B): B = this match {
    case Nil        => z
    case Cons(h, t) => t.foldLeft(f(z, h))(f)
  }

  def reduceRight[B >: A](f: (A, B) => B): B = this match {
    case Cons(h, Nil) => h
    case Cons(h, t)   => f(h, t.reduceRight(f))
  }

  // noinspection NotImplementedCode
  // reduceLeft 递归无法直接实现，需要引入累计变量，因为前两个元素计算完后无法传入下次计算
  def reduceLeft[B >: A](f: (B, A) => B): B = ???

}
private[collection] case class Cons[+A](head0: A, tail0: List[A]) extends List[A]
private[collection] case object Nil extends List[Nothing]

object List {

  def apply[A](as: A*): List[A] =
    if (as.isEmpty) Nil
    else Cons(as.head, apply(as.tail: _*))
}
