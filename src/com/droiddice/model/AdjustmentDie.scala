package com.droiddice.model

class AdjustmentDie(val size: Int) extends Die {

  	val spec = if (size >= 0) "+" + size.toString else size.toString
	//override def imageId = if (size >= 0) "+1" else "-1"
	override val imageId = "adjustment"

  	var value = size
	def roll = size 
	override def display = spec 
}
