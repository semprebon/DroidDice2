package com.droiddice.test

import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import org.scalatest.Spec
import org.scalatest.matchers.ShouldMatchers

import com.droiddice.model._

@RunWith(classOf[JUnitRunner])
class AdjustmentDieSpec extends Spec with ShouldMatchers {

  describe("An adjustmentDie of size 6") {
    val d = new AdjustmentDie(6)

    it("should return result of 6") {
      d.roll should be(6)
    }
    it("should return type of '+6'") {
      d.spec should be("+6")
    }
    it("should display as '+6'") {
      val r = d.roll
      d.display should be("+6")
    }
  }

  describe("An adjustmentDie of size -3") {
    val d = new AdjustmentDie(-3)

    it("should return result of -3") {
      d.roll should be(-3)
    }
    it("should return type of '-3'") {
      d.spec should be("-3")
    }
    it("should display as '-3'") {
      val r = d.roll
      d.display should be("-3")
    }
  }
}