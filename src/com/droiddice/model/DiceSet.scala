package com.droiddice.model

import java.util.regex._
import java.io.Serializable
import android.util.Log
import scala.util.Sorting
import scala.collection.immutable.StringOps

class DiceSet(var dice: RandomAccessSeq[Die], var strategy: Strategy, newName: String) extends Serializable {

    var value = results
    var customName: String = _
    
    def count = dice.size
	def spec = DiceSetHelper.specForDice(strategy, dice)

	def this(dice: RandomAccessSeq[Die], newName: String) = this(dice, new AddStrategy(null), newName)
	def this(dice: RandomAccessSeq[Die]) = this(dice, null)
    def this(s: String) = this(DiceSetHelper.diceStringToArray(s), DiceSetHelper.diceStringToStrategy(s), null)
    def this(s: String, newName: String) = { 
        this(s)
        this.name = newName 
    }

	def results:RandomAccessSeq[Int] = {
	    if (dice.isEmpty) Array(0) 
	    else strategy.results(values)
	}

    def display = if (dice.isEmpty) "" else results.map(_.toString()).reduceLeft(_ + " " + _)

    def name = if (customName != null) customName else spec
    
    def isCustomName(newName: String) = {
        (newName != null) && (newName.length != 0) && !spec.equals(newName)
    }
    
    def name_=(newName: String) { 
    	customName = if (isCustomName(newName)) newName else null 
    }
	
    def isNamed = customName != null
    
	def roll() = { 
	    dice.foreach(die => die.roll)
	    value = results
	    value
	}

	def values() = dice.map(_.value)
	
	def valuesString: String = if (dice.isEmpty) "" else values.map(_.toString()).reduceLeft(_ + "," + _)
	
	def valuesString_=(s: String) = {
	    if (s != null && s.length() > 0) {
    		dice.zip(s.split(",")).foreach {
	        	case (die, value) => die.value = value.toInt
	        	case _ => throw new IllegalArgumentException("Missing value")
			}
    	}
	}
	
	def typeOf(index: Int): String = dice(index).spec
	def apply(index: Int): Die = dice(index)
	
	def countOf(spec: String): Int = {
	    dice.foldLeft(0) ((count, die) => if (die.spec == spec) count+1 else count)
	}
	
	def canCombineAdjustment(newDie: Die): Boolean
		= !dice.isEmpty && dice.last.isInstanceOf[AdjustmentDie] && newDie.isInstanceOf[AdjustmentDie]	
	
	def add(newSpec: String): DiceSet = {
	    val newDie = DiceSetHelper.dieFactory(newSpec)(0)
	    val newDice = if (canCombineAdjustment(newDie)) {
	        val adjustmentDie = new AdjustmentDie(dice.last.value + newDie.value)
	        dice.slice(0, dice.length-1) ++ List(adjustmentDie)
	    } else {
	        dice ++ List(newDie)
	    }
	    dice = newDice
	    this
	}
	
	def remove(index: Int): DiceSet = {
	    dice = dice.take(index) ++ dice.drop(index+1)
	    this
	}
	
	override def toString = customName + "(" + spec + ")=(" + valuesString + ")" 
	
}

class DiceSetHelper {}

object DiceSetHelper {
	val diePattern = Pattern.compile("([dsp+])?(-?\\d+|F|S)(?:\\:(\\d+))?")
    val leadingNumber = Pattern.compile("""^(\d*)(.*)$""")
    
    def split(spec: String, regex: Pattern): Tuple2[String,String] = {
        val matcher = regex.matcher(spec)
        if (matcher.matches) (matcher.group(1), matcher.group(2)) else ("","")
    }
    
    def split3(spec: String, regex: Pattern): Tuple3[String,String,String] = {
        val matcher = regex.matcher(spec)
        if (matcher.matches) (matcher.group(1), matcher.group(2), matcher.group(3)) else ("","", "")
    }
    
	def singleDieFactory(spec: String): Die = {
	    try {
		    val (t, size, degree) = split3(spec, diePattern)
		    System.err.println("extracted die parameters t=" + t + "; size=" + size + "; degree=" + degree)
			if (t == "d" && size == "F") new FudgeDie
			else if (t == "d" && size == "S") new SignDie()
			else if (t == "s") new SavageDie(size.toInt)
			else if (t == "p") new PowerDie(size.toInt, degree.toInt)
			else if (t == "d") new SimpleDie(size.toInt)
			else new AdjustmentDie(size.toInt)
	    } catch {
	        case e: NumberFormatException => 
	            throw new InvalidSpecificationException("\"" + spec + "\" is not a valid specification")
	    }
	}

	def dieFactory(s : String): Seq[Die] = {
	    if (s.length == 0) return null
		val (countStr, singleDieSpec) = split(s, leadingNumber)
		if (singleDieSpec == "") Array(new AdjustmentDie(countStr.toInt))
		else {
    		val count = if (countStr == "") 1 else countStr.toInt
    		new Range(0, count, 1).map(i => singleDieFactory(singleDieSpec))
		}
	}

    def join(seq: Seq[String], sep: String) = seq.reduceLeft((a:String,b:String) => a + sep + b)
    
    private def seperateStrategy(s: String): ((String, String), String) = {
        val matcher = Pattern.compile("(max|count)\\(([\\w+-]+)(?:,([>=<]?\\d+))?\\)").matcher(s)
        if (matcher.find()) {
        	val strategy = matcher.group(1)
        	val args = matcher.group(3)
        	val dice = matcher.group(2)
            System.out.println("matched strategy:" + strategy + "/" + args + "/" + dice)
        	((strategy, args), dice) 
        } else {
            System.out.println("matched no strategy:" + s)
            (("add",null), s)
        }
    }
    
	def diceStringToArray(s: String): Array[Die] = {
	    val (strategy, diceStr) = seperateStrategy(s)
	    diceStringWithoutStrategyToArray(diceStr)
	}
	
	def diceStringWithoutStrategyToArray(s: String): Array[Die] = {
	    if (s.length == 0) return new Array[Die](0)
		val dieTypes = s.replace("-", "+-").split("\\+").filter(t => t.length > 0)
		val dice = dieTypes.flatMap(t => dieFactory(t)).toArray
		dice
	} 
 
	def diceStringToStrategy(s: String): Strategy = {
	    val ((strategy, args), dice) = seperateStrategy(s)
	    strategy match {
	        case "max" => new MaxStrategy(args)
	        case "add" => new AddStrategy(args)
	        case "count" => new CountStrategy(args)
	        case _ => throw new InvalidSpecificationException(
	                	"Invalid strategy \"" + strategy + "\" in \"" + s + "\".")
	    }
	}
	
    def mergeNextDie(result: List[Tuple2[Int, String]], newDie: Die): List[Tuple2[Int, String]] = {
        val (count, lastDieSpec) = result.last
        if (count == 0) return List((1, newDie.spec))
        else if (lastDieSpec != newDie.spec) return result ::: List((1, newDie.spec))
        else return result.init ::: List((count+1, lastDieSpec))
    }
    
    def specAndCountToSpec(pair: Tuple2[Int, String]): String = { 
        val (count, spec) = pair
        if (count == 1) spec else count.toString + spec 
    }
        
	def mergeDieSpecs(dice: Iterable[Die]): String = {
	    val specList: List[Tuple2[Int, String]] = dice.foldLeft(List((0,""))) (mergeNextDie)
	    val specs: List[String] = specList.map(specAndCountToSpec)
	    specs.reduceLeft(_+"+"+_)
	}
	
	def specForDice(strategy: Strategy, dice: Iterable[Die]): String = {
	    if (strategy.name == "add") {
	        mergeDieSpecs(dice).replace("+-", "-").replace("++", "+")
	    } else {
	        strategy.name + "(" + mergeDieSpecs(dice) + strategy.argsAsString + ")"
	    }
	}
}
	
class InvalidSpecificationException(message: String) extends Exception(message)

abstract class Strategy(val name: String, argsStr: String) {
    def results(values: RandomAccessSeq[Int]) : RandomAccessSeq[Int]
    def argsAsString = if (argsStr != null) "," + argsStr else ""
}

class AddStrategy(args: String) extends Strategy("add", null) {
    def results(values: RandomAccessSeq[Int]) : RandomAccessSeq[Int] = {
    	Array(values.reduceLeft(_ + _))
    }
}

class MaxStrategy(args: String) extends Strategy("max", args) {
    val count: Int = if (args == null) 1 else args.toInt
    
    def results(values: RandomAccessSeq[Int]) : RandomAccessSeq[Int] = {
    	values.sorted.reverse.take(count)
    }
}

class CountStrategy(args: String) extends Strategy("count", args) {
	System.out.println("count args=" + args)
    val matcher = Pattern.compile("([><=])(\\d+)").matcher(args)
    if (!matcher.matches()) throw new InvalidSpecificationException("Invalid count condition \"" + args + "\"")

	def op(value: Int): Boolean = matcher.group(1) match {
        case "<" => value < targetNumber
        case "=" => value == targetNumber
        case ">" => value > targetNumber
        case _ => false
    }
    val targetNumber = matcher.group(2).toInt
    
    def results(values: RandomAccessSeq[Int]) : RandomAccessSeq[Int] = {
    	Array(values.filter((x) => op(x)).size)
    }
}
