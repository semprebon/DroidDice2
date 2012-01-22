package com.droiddice.model

import java.util.regex._
import java.io.Serializable

class DiceSet(var dice: RandomAccessSeq[Die], newName: String) extends Rollable with Serializable {

    var value = sumDice
    var customName: String = _
    
    def count = dice.size
	def spec = DiceSetHelper.specForDice(dice)
	
	def this(dice: RandomAccessSeq[Die]) = this(dice, null)
    def this(s: String) = this(DiceSetHelper.diceStringToArray(s))
    def this(s: String, newName: String) = { 
        this(s)
        this.name = newName 
    }

	def sumDice:Int = if (dice.isEmpty) 0 else values.reduceLeft(_ + _)

    def display = if (dice.isEmpty) "" else sumDice.toString

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
	    value = sumDice
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
	val diePattern = Pattern.compile("([ds+])?(-?\\d+|F)")
    val leadingNumber = Pattern.compile("""^(\d*)(.*)$""")
    
    def split(spec: String, regex: Pattern): Tuple2[String,String] = {
        val matcher = regex.matcher(spec)
        if (matcher.matches) (matcher.group(1), matcher.group(2)) else ("","")
    }
    
	def singleDieFactory(spec: String): Die = {
	    val (t, size) = split(spec, diePattern)
		if (t == "d" && size == "F") new FudgeDie
		else if (t == "s") new SavageDie(size.toInt)
		else if (t == "d") new SimpleDie(size.toInt)
		else new AdjustmentDie(size.toInt) 
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
    
	def diceStringToArray(s: String): Array[Die] = {
		if (s.length == 0) return new Array[Die](0)
		val dieTypes = s.replace("-", "+-").split("\\+").filter(t => t.length > 0)
		val dice = dieTypes.flatMap(t => dieFactory(t)).toArray
		dice
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
	    specs.reduceLeft((a: String, b:String) => a+"+"+b)
	}
	
	def specForDice(dice: Iterable[Die]): String = mergeDieSpecs(dice).replace("+-", "-").replace("++", "+")
}