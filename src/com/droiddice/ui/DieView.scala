/* Copyright (C) 2009 Andrew Semprebon */
package com.droiddice.ui

import android.view.View
import android.content.Context
import android.util.AttributeSet
import android.view.View
import scala.collection.immutable.HashMap
import com.droiddice.R
import com.droiddice.model._
import android.graphics.Bitmap
import android.text.TextPaint
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.LightingColorFilter
import android.graphics.Canvas
import android.util.Log
import android.graphics.Rect

class DieView(context: Context, attrs: AttributeSet) extends View(context, attrs) {

	var die: Die = new SimpleDie(6)
	
	var dieImager: DieImager = _
	var display: Int = DieView.DISPLAY_VALUE
	var textPaint: TextPaint = _
	var preferredSize = DieView.SIZE_LARGE
	var size = 60
	

	val TAG = "DieView"

	initialize()

	def this(context: Context) = this(context, null) 

	private def initialize() {
		textPaint = new TextPaint();
		textPaint.setAntiAlias(true);
		textPaint.setColor(getResources().getColor(R.color.text_dark));
		textPaint.setTextAlign(Paint.Align.CENTER);
		textPaint.setFakeBoldText(true);
		var padding = getContext().getResources().getDimensionPixelSize(R.dimen.die_padding);
		setPadding(padding, padding, padding, padding);
	}

	override def onDraw(canvas: Canvas) {
		val width = getWidth() - (getPaddingLeft() + getPaddingRight())
		val height = getHeight() - (getPaddingTop() + getPaddingBottom())
		val xOffset = (width - size) / 2 + getPaddingTop()
		val yOffset = (height - size) / 2 + getPaddingLeft()
		val shadowHeight = 14

		dieImager.drawDie(canvas, die, xOffset, yOffset)

		if (display == DieView.DISPLAY_VALUE) {
			drawValue(canvas, size * 0.6F, die.display);
		} else if (display == DieView.DISPLAY_TYPE) {
			drawValue(canvas, size * 0.4F, die.toString());
		}

		super.onDraw(canvas);
	}

	private def drawValue(canvas: Canvas, size: Float, text: String) {
		textPaint.setTextSize(size)
		val width = getWidth() - (getPaddingLeft() + getPaddingRight())
		val height = getHeight() - (getPaddingTop() + getPaddingBottom())
		val xOffset = width / 2 + getPaddingLeft()
		val tHeight = (-textPaint.descent()-textPaint.ascent()).toInt
		val yOffset = (height + tHeight) / 2 + getPaddingTop()
		canvas.drawText(text, xOffset, yOffset, textPaint)
	}

	def preferredWidthWithPadding = preferredSize + getPaddingLeft() + getPaddingRight()
	def preferredHeightWithPadding = preferredSize + getPaddingTop() + getPaddingBottom()
	
	/**
	 * Determines the size (width or height) given a preferred size and measureSpec
	 * 
	 * @param measureSpec A measureSpec packed into an int
	 * @return The size, honoring constraints from measureSpec
	 */
	def determineMeasure(measureSpec: Int, preferredSize: Int): Int = {
	    val specMode = View.MeasureSpec.getMode(measureSpec)
		val specSize = View.MeasureSpec.getSize(measureSpec)
		return if (specMode == View.MeasureSpec.EXACTLY) preferredSize
			else if (specMode == View.MeasureSpec.AT_MOST) Math.min(preferredSize, specSize) 
			else preferredSize
	}
	
	private def measureWidth(measureSpec: Int) = determineMeasure(measureSpec, preferredWidthWithPadding)
	private def measureHeight(measureSpec: Int) = determineMeasure(measureSpec, preferredHeightWithPadding)

	override def onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
		super.onLayout(changed, left, top, right, bottom);
		if (changed) {
		    dieImager = new DieImager(context, findBestSize(right - left, bottom - top))
		}
	}

	private def findBestSize(width: Int, height: Int): Int = {
		val maxSize = if (width < height) width else height
		DieView.DIE_SIZES.foreach { size => if (size <= maxSize) return size }
		return DieView.MIN_DIE_SIZE
	}

	/**
	 * @see android.view.View#measure(int, int)
	 */
	override def onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
		setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec))
	}

}

// static class
object DieView {
	val DISPLAY_BLANK = 0
	val DISPLAY_VALUE = 1
	val DISPLAY_TYPE = 2
	
	val SIZE_LARGE = 60
	val SIZE_MEDIUM = 30
	val SIZE_SMALL = 15

	val DIE_SIZES = Array(SIZE_LARGE, SIZE_MEDIUM, SIZE_SMALL)
	val MIN_DIE_SIZE = SIZE_SMALL

	val TYPE_TO_IMAGE_ID = Map(	
		"d4" -> R.drawable.d4_60,
		"d6" -> R.drawable.d6_60,
		"d8" -> R.drawable.d8_60,
		"d10" -> R.drawable.d10_60,
		"d12" -> R.drawable.d12_60,
		"d20" -> R.drawable.d20_60,
		"s4" -> R.drawable.s4,
		"s6" -> R.drawable.s6,
		"s8" -> R.drawable.s8,
		"s10" -> R.drawable.s10,
		"s12" -> R.drawable.s12,
		"s20" -> R.drawable.s20,
		"adjustment" -> R.drawable.adjustment
		)
}
