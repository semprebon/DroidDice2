package com.droiddice.ui

import android.widget.ArrayAdapter
import android.content.Context
import android.view._
import com.droiddice.R
import com.droiddice.model.Die
import android.util.Log

/**
 * An adapter that allows composite views to display DieView views as children
 */
class DiceViewAdapter(context: Context, dice: Array[Die]) extends ArrayAdapter[Die](context, 0,  dice) {
  
  val itemViewSize = context.getResources().getDimension(R.dimen.die_view_size).toInt
  val TAG = "DiceViewAdapter"
      
  override def getView(position: Int, convertView : View, parent: ViewGroup): View = {
    var resultView = if (convertView == null || convertView.isInstanceOf[DieView]) 
  	    new DieView(context) 
      else 
        convertView.asInstanceOf[DieView]
    val die = dice(position)
    Log.d(TAG, "Creating die view for " + position + ": " + die) 
    resultView.die = die
    resultView.preferredSize = itemViewSize
    return resultView
  }
}