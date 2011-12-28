package com.droiddice.test

import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import org.scalatest.Spec
import org.scalatest.matchers.ShouldMatchers

import com.droiddice.model._
import com.droiddice.test.Distribution

@RunWith(classOf[JUnitRunner])
class SimpleDieSpec extends Spec with ShouldMatchers {

  describe("A SimpleDie of size 6") {
    val d = new SimpleDie(6)

    val dist = new Distribution(d)
    dist.min should be(1)
    dist.max should be(6)

    it("should return type of 'd6'") {
      d.spec should be("d6")
    }
    it("should display as value of last roll") {
      val r = d.roll
      d.display should be(r.toString)
    }
  }
  describe("A SimpleDie of size 8") {
    val d = new SimpleDie(8)

    val dist = new Distribution(d)
    dist.min should be(1)
    dist.max should be(8)
  }
}