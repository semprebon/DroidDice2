package com.droiddice.test

import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import org.scalatest.Spec
import org.scalatest.matchers.ShouldMatchers

import com.droiddice.model._
import com.droiddice.test.Distribution

@RunWith(classOf[JUnitRunner])
class SavageDieSpec extends Spec with ShouldMatchers {

  describe("A SavageDie of size 6") {
    val d = new SavageDie(6)

    it("should have distribution from 1 on up, with no value at 6") {
      val dist = new Distribution(d)
      dist.min should be(1)
      dist.have(6) should be(false)
      dist.have(7) should be(true)
    }
    it("should return type of 'd6'") {
      d.spec should be("s6")
    }
    it("should display as value of last roll") {
      val r = d.roll
      d.display should be(r.toString)
    }
  }
  describe("A SimpleDie of size 8") {
    val d = new SavageDie(8)

    it("should have distribution from 1 on up, with no value at 8") {
      val dist = new Distribution(d)
      dist.min should be(1)
      dist.have(8) should be(false)
      dist.have(9) should be(true)
    }
  }
}
