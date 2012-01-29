package com.droiddice.test

import com.droiddice.model._
import scala.collection.mutable._

class Distribution(val die: Rollable) {

  val times = new Range(1, 1000, 1)
  val rolls: HashMap[Int, Int] = {
    var map = new HashMap[Int, Int];
    times.foreach(i => {
      val roll = die.roll
      map.put(roll, map.getOrElse(roll, 0) + 1);
    })
    map
  }

  def min = rolls.keys.reduceLeft { (a, b) => if (a < b) a else b }
  def max = rolls.keys.reduceLeft { (a, b) => if (a > b) a else b }
  def have(roll: Int) = { rolls.contains(roll) }
  def probabilityOf(roll: Int) = { rolls.getOrElse(roll, 0) / times.length }

  override def toString = rolls.keySet.map(k => k + "->" + rolls.get(k)).mkString(" ")
}

class DiceSetDistribution(val diceSet: DiceSet) {

  val times = new Range(1, 1000, 1)
  val rolls: HashMap[Int, Int] = {
    var map = new HashMap[Int, Int];
    times.foreach(i => {
      val roll = diceSet.roll.apply(0)
      map.put(roll, map.getOrElse(roll, 0) + 1);
    })
    map
  }

  def min = rolls.keys.reduceLeft { (a, b) => if (a < b) a else b }
  def max = rolls.keys.reduceLeft { (a, b) => if (a > b) a else b }
  def have(roll: Int) = { rolls.contains(roll) }
  def probabilityOf(roll: Int) = { rolls.getOrElse(roll, 0) / times.length }

  override def toString = rolls.keySet.map(k => k + "->" + rolls.get(k)).mkString(" ")
}
