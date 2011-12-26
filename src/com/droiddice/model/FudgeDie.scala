package com.droiddice.model

class FudgeDie extends SimpleDie(3) {

	override val min: Int = -1
	override val max: Int = 1
  	override val spec = "dF"

	override def display: String = if (value == 1) "+1" else value.toString
}
