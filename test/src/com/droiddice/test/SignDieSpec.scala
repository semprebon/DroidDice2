package com.droiddice.test

import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import org.scalatest.Spec
import org.scalatest.matchers.ShouldMatchers

import com.droiddice.model._

@RunWith(classOf[JUnitRunner])
class SignDieSpec extends Spec  with ShouldMatchers {

  describe("A SignDie") {
    val d = new SignDie

    val dist = new Distribution(d)
    dist.min should be(-1)
    dist.max should be(1)

    it("should return type of 'dS'") {
      d.spec should be("dS")
    }
    it("should display as '-1', or '+1' depending on value of last roll") {
      val r = d.roll
      d.display should be(if (r == 1) "+1" else r.toString())
    }
  }

}