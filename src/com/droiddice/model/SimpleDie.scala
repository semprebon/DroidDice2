package com.droiddice.model

import scala.util.Random

class SimpleDie(val size: Int) extends Die {

 	val spec = "d" + size

	val min: Int = 1
	val max: Int = size
	val random: Random = new Random
	var value = min
 
	def roll: Int = { 
	  value = min + random.nextInt(size) 
	  value
	}
	
}
