package com.droiddice.model

import scala.util.Random

class PowerDie(val size: Int, val power: Int) extends Die {

 	val spec = "p" + size + ":" + power
  	override val imageId = "d" + size

	val min: Int = 0
	val max: Int = (size - 1) * power 
	val random: Random = new Random
	var value = min
 
	def roll: Int = { 
	  value = min + random.nextInt(size) * power
	  value
	}
	
}
