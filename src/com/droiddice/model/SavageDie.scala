package com.droiddice.model

class SavageDie(override val size: Int) extends SimpleDie(size) {

  	override val spec = "s" + max.toString
  	override val imageId = spec

	override def roll(): Int = { 
		val r = super.roll
		value = if (r != max) r else r + roll
		value
	}

}
