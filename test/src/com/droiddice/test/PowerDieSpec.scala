package com.droiddice.test

import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import org.scalatest.Spec
import org.scalatest.matchers.ShouldMatchers

import com.droiddice.model._

@RunWith(classOf[JUnitRunner])
class PowerDieSpec extends Spec with ShouldMatchers {

  describe("A PowerDie of size 6,10") {
    val d = new PowerDie(6,10)

    val dist = new Distribution(d)
    dist.min should be(0)
    dist.max should be(50)

    it("should return type of 'd6:10'") {
      d.spec should be("p6:10")
    }
    it("should display as value of last roll") {
      val r = d.roll
      d.display should be(r.toString)
    }
  }
  describe("A PowerDie of size 8:1") {
    val d = new PowerDie(8, 1)

    val dist = new Distribution(d)
    dist.min should be(0)
    dist.max should be(7)
  }
}