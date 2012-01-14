package com.droiddice.ui

import com.droiddice.model.DiceSet
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.LightingColorFilter
import android.content.Context
import android.graphics.Rect
import com.droiddice.R
import com.droiddice.model.Die

class DiceSetBitmapGenerator(context: Context, size: Int) {
    
    val dieImager = new DieImager(context, size)
    val diePadding = context.getResources.getDimension(R.dimen.die_padding).toInt
    val paddedDieSize = size + 2*diePadding
    val height = paddedDieSize
    
    def generate(diceSet: DiceSet): Bitmap = {
        val width = (size + diePadding) * diceSet.count + diePadding
       	val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
		val canvas = new Canvas(bitmap)
       	
       	var xOffset = diePadding
       	val yOffset = diePadding
       	diceSet.dice.foreach(die => {
       	    dieImager.drawDie(canvas, die, xOffset, yOffset)
       		xOffset += size + diePadding
       	})
		bitmap
    }
}

class DieImager(context: Context, size: Int) {
    
	private def loadImage(size: Int, die: Die) : Bitmap = {
		if (die.imageId != null) {
			val imageId = DieView.TYPE_TO_IMAGE_ID.get(die.imageId)
			BitmapFactory.decodeResource(context.getResources(), imageId.getOrElse(R.drawable.d6));
		} else {
		    null
		}
	}

    def drawDie(canvas: Canvas, die: Die, xOffset: Int, yOffset: Int) {
        val dieImage = loadImage(size, die)
    	val paint = new Paint()
		paint.setColorFilter(new LightingColorFilter(0xffffd9, 0))
		if (dieImage != null) {
			canvas.drawBitmap(dieImage, null, 
					new Rect(xOffset, yOffset, xOffset + size, yOffset + size), paint);
		}
    }
}