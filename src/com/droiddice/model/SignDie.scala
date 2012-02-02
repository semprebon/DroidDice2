package com.droiddice.model

class SignDie extends SimpleDie(2) {
	override val min: Int = -1
	override val max: Int = +1
  	override val spec = "dS"
  	override val imageId = spec

	override def display: String = if (value == 1) "+1" else value.toString

	override def roll: Int = { 
	  value = if (random.nextBoolean()) -1 else +1 
	  value
	}

}