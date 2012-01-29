/* Copyright (C) 2009 Andrew Semprebon */
package com.droiddice.ui

import android.view.View
import android.content.Context
import android.util.AttributeSet
import android.widget.AdapterView
import com.droiddice.R
import android.util.Log
import android.widget.Adapter
import scala.reflect._
import android.view.ViewGroup.LayoutParams
import android.view.View.MeasureSpec

class DiceSetView(context: Context, attrs: AttributeSet) extends AdapterView[DiceViewAdapter](context, attrs) {
	val TAG = "DieSetView"

	val viewsPerRow = 5

	def this(context: Context) = this(context, null) 

	private var adapter: DiceViewAdapter = _
	
	def getAdapter(): DiceViewAdapter = {
	    Log.d(TAG, "getting adapter with " + adapter.getCount())
	    adapter 
	}
	
	def setAdapter(adapter: DiceViewAdapter) {
	    this.adapter = adapter
	    removeAllViewsInLayout()
	    requestLayout()
	}

	var position: Int = 0
	
	def getSelectedView(): View = {
	    null
	}
	
	def setSelection(position: Int) {
	    
	}

	override protected def onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
	    super.onLayout(changed, left, top, right, bottom) 
 
	    // if we don't have an adapter, we don't need to do anything
	    if (adapter == null) return
	    Log.d(TAG, "in onLayout getting adapter with " + adapter.getCount())
	    
	    if (getCount() == 0) {
   	        Log.d(TAG, "laying out " + getCount() + " views")
	    	var position = 0
	    	var bottomEdge = 0
	    	var rowPosition = 0
	    	for (index <- 0 until getCount()) {
	    	    if (bottomEdge < getHeight()) {
	    	        Log.d(TAG, "measuring " + index)
	    	    	val newChild = adapter.getView(index, null, this)
		    		addAndMeasureChild(newChild)
		    		rowPosition += 1
		    		if (rowPosition == viewsPerRow) {
		    			bottomEdge += newChild.getMeasuredHeight()
		    			position += 1
		    			rowPosition = 0
		    		}
	    	    }
	    	}
	    } 
 
	    positionItems()
	}
	
	/**
	 * Adds a view as a child view and takes care of measuring it	
	 *
	 * @param child The view to add
	 */
	private def addAndMeasureChild(child: View) {
		var params = child.getLayoutParams()
		if (params == null) {
			params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
		}
		addViewInLayout(child, -1, params, true) 
 
		child.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
} 
 
	/**
	 * Positions the children at the correct positions
	 */
	private def positionItems() {
		var top = 0 
		var rowPosition = 0
		for (index <- 0 until getChildCount()) {
			val child = getChildAt(index) 
 
			val width = child.getMeasuredWidth()
			val height = child.getMeasuredHeight()
			val left = rowPosition * width 
  	        Log.d(TAG, "positioning " + index + " at " + left + "," + top)
			child.layout(left, top, left + width, top + height)
			rowPosition += 1
			if (rowPosition == viewsPerRow) {
				top += height
			    rowPosition = 0
			}
		}
	}
}

