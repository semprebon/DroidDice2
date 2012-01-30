import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import org.scalatest.Spec
import org.scalatest.matchers.ShouldMatchers
import com.droiddice.model._
import scala.collection.mutable.ArrayBuffer
import com.droiddice.test.DiceSetDistribution

@RunWith(classOf[JUnitRunner])
class HighestDiceSetSpec extends Spec with ShouldMatchers {

  // Creation Tests
    
  describe("A dice set created from 'highest(d4,d6)' string") {
    val d = new DiceSet("max(d4,d6)")
    it("should me made up of d4,dd6dice") {
      d.count should be(2)
      d.apply(0).spec should be("d4")
      d.apply(1).spec should be("d6")
    }
    it("should have a strategy of max	") {
      d.strategy should be("max")
    }
    it("should have a spec of the original string") {
      d.spec should be("max(d4,d6)")
    }
    it("should roll values between 1 and 6") {
      val dist = new DiceSetDistribution(d)
      dist.min should be(1)
      dist.max should be(6)
    }
//    it("should have countOf('d4') of 2") {
//      d.countOf("d4") should be(2)
//    }
//    it("should have strategy of Add") {
//        System.out.println("Starting test")
//        d.valuesString = "1,2,3"
//        d.results should be(Array[Int](5))
//    }
  }
}
