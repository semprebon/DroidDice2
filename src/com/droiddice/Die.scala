package com.droiddice

trait Die {
	val spec: String
	def imageId: String = spec

	var value: Int
	def roll: Int
	def display: String = value.toString
 
	override def toString: String = spec + "(" + value.toString + ")"
}
