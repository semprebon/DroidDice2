import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import org.scalatest.Spec
import org.scalatest.matchers.ShouldMatchers
import com.droiddice.model._
import scala.collection.mutable.ArrayBuffer
import com.droiddice.test.DiceSetDistribution

@RunWith(classOf[JUnitRunner])
class MaxDiceSetSpec extends Spec with ShouldMatchers {

  // Creation Tests
    
  describe("A dice set created from 'max(d4+d6)' string") {
    val d = new DiceSet("max(d4+d6)")
    it("should me made up of d4,d6 dice") {
      d.count should be(2)
      d.apply(0).spec should be("d4")
      d.apply(1).spec should be("d6")
    }
    it("should have a strategy of max	") {
      d.strategy.name should be("max")
    }
    it("should have a spec of the original string") {
      d.spec should be("max(d4+d6)")
    }
    it("should roll values between 1 and 6") {
      val dist = new DiceSetDistribution(d)
      dist.min should be(1)
      dist.max should be(6)
    }
    it("should have strategy of max") {
        d.valuesString = "1,3"
        d.results.length should be(1)
        d.results.apply(0) should be(3)
    }
  }

  describe("A dice set created from 'max(d4+2d6,2)' string") {
    val d = new DiceSet("max(d4+2d6,2)")
    it("should me made up of d4,d6,d6 dice") {
      d.count should be(3)
      d.apply(0).spec should be("d4")
      d.apply(1).spec should be("d6")
      d.apply(2).spec should be("d6")
    }
    it("should have a strategy of max	") {
      d.strategy.name should be("max")
    }
    it("should have a spec of the original string") {
      d.spec should be("max(d4+2d6,2)")
    }
    it("should roll values between 1 and 6") {
      val dist = new DiceSetDistribution(d)
      dist.min should be(1)
      dist.max should be(6)
    }
    it("should have strategy of max") {
        d.valuesString = "1,5,2"
        d.results.length should be(2)
        d.results.apply(0) should be(5)
        d.results.apply(1) should be(2)
    }
  }
}
