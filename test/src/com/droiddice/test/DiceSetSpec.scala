package com.droiddice.test

import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import org.scalatest.Spec
import org.scalatest.matchers.ShouldMatchers
import com.droiddice.model._
import scala.collection.mutable.ArrayBuffer

@RunWith(classOf[JUnitRunner])
class DiceSetSpec extends Spec with ShouldMatchers {

  // Creation Tests
    
  describe("a die created from 's10' string") {
    var d = DiceSetHelper.singleDieFactory("s10")
    it("should create savage die") {
      d.spec should be("s10")
    }
  }

  describe("A dice set created from '2d4+d6-1' string") {
    val d = new DiceSet("2d4+d6-1")
    it("should me made up of d4,d4,d6 and -1 dice") {
      d.count should be(4)
      d.apply(0).spec should be("d4")
      d.apply(1).spec should be("d4")
      d.apply(2).spec should be("d6")
      d.apply(3).spec should be("-1")
    }
    it("should have a spec of the original string") {
      d.spec should be("2d4+d6-1")
    }
    it("should have a name the same as spec") {
      d.name should be(d.spec)
    }
    it("should roll values between 2 and 13") {
      val dist = new Distribution(d)
      dist.min should be(2)
      dist.max should be(13)
    }
    it("should have countOf('d4') of 2") {
      d.countOf("d4") should be(2)
    }
  }

  describe("A dice set created from '2d4+s6-3' string") {
    val d = new DiceSet("2d4+s6+dF-3")

    it("should me made up of d4,d4,s6,dF and -3 dice") {
      d.count should be(5)
      d.apply(0).spec should be("d4")
      d.apply(1).spec should be("d4")
      d.apply(2).spec should be("s6")
      d.apply(3).spec should be("dF")
      d.apply(4).spec should be("-3")
    }
    it("should have a spec of the original string") {
      d.spec should be("2d4+s6+dF-3")
    }
    it("should have a name the same as spec") {
      d.name should be(d.spec)
    }
    it("should roll values grater than -1") {
      val dist = new Distribution(d)
      dist.min should be(-1)
    }
  }

  describe("A dice set created from empty string") {
    val d = new DiceSet("")
    it("shouldhave no dice") {
      d.count should be(0)
    }
    it("should have values of empty array") {
    	d.values should be(ArrayBuffer[Int]())
    }
    it("should have values string of empty string") {
        d.valuesString should be("")
    }
  }

  describe("A dice set with just a -10 penalty") {
    val d = new DiceSet("-10")
    it("should have single adjustment of -10") {
      d.count should be(1)
      d.apply(0).spec should be("-10")
      d.apply(0).value should be(-10)
    }
  }

  describe("A dice set with a +1 adjustment") {
    val d = new DiceSet("d6+1")
    it("should have spec of d6+1") {
      d.spec should be("d6+1")
    }
  }

  describe("a dice set with a specific name") {
    val d = new DiceSet("2d6", "test")
    it("should keep name") {
      d.name should be("test")
    }
  }

  // Changing a diceset
  describe("adding a d6 to a dice set with 2d6") {
    val d = new DiceSet("2d6").add("d6")
    it("should give a dice set with 3d6") {
      d.spec should be("3d6")
    }
  }
  describe("adding a d6 to an empty dice set") {
    val d = new DiceSet("").add("d6")
    it("should give a dice set with d6") {
      d.spec should be("d6")
    }
  }
  describe("adding an adjustment +2 to a dice set with a +1 adjustment") {
    it("should give a dice set with adjustment of +3") {
      val d = new DiceSet("d6+1").add("+2")
      d.spec should be("d6+3")
    }
  }
  describe("adding an adjustment -2 to a dice set 2d6") {
    it("should give a dice set with adjustment of -2") {
      val d = new DiceSet("2d6").add("-2")
      d.spec should be("2d6-2")
    }
  }
  describe("removing the 2nd die from a dice set 2d6+d8") {
    it("should give dice set d6+d8") {
      val d = new DiceSet("2d6+8").remove(1)
      d.spec should be("d6+8")
    }
  }
  describe("adding a d6 to a dice set named Test") {
    val d = new DiceSet("2d6", "Test").add("d6")
    it("should give a dice set with same name") {
      d.name should be("Test")
    }
  }
  describe("removing a d6 to a dice set named Test") {
    val d = new DiceSet("2d6", "Test").remove(1)
    it("should give a dice set with same name") {
      d.name should be("Test")
    }
  }
}
