package com.droiddice.test

import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import org.scalatest.Spec
import org.scalatest.matchers.ShouldMatchers

import com.droiddice.FudgeDie;
import com.droiddice.test.Distribution;

@RunWith(classOf[JUnitRunner])
class FudgeDieSpec extends Spec with ShouldMatchers {

  describe("A FudgeDie") {
    val d = new FudgeDie

    val dist = new Distribution(d)
    dist.min should be(-1)
    dist.max should be(1)

    it("should return type of 'dF'") {
      d.spec should be("dF")
    }
    it("should display as '-1', '0',or '+1' depending on value of last roll") {
      val r = d.roll
      d.display should be(if (r == 1) "+1" else r.toString())
    }
  }
}
