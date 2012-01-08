package com.droiddice.model

trait Die extends Rollable {
	val spec: String
	val imageId: String = spec

	var value: Int
	def roll: Int
	def display: String = value.toString
 
	override def toString: String = spec + "(" + value.toString + ")"
}
