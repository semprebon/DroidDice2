package com.droiddice.ui

import android.widget.ArrayAdapter
import android.content.Context
import android.view._

import com.droiddice.R
import com.droiddice.model.Die

/**
 * An adapter that allows composite views to display DieView views as children
 */
class DiceViewAdapter(context: Context, dice: Array[Die]) extends ArrayAdapter[Die](context, 0,  dice) {
  
  val itemViewSize = context.getResources().getDimension(R.dimen.die_view_size).toInt
  
  override def getView(position: Int, convertView : View, parent: ViewGroup): View = {
    var resultView = if (convertView == null || convertView.isInstanceOf[DieView]) 
  	    new DieView(context) 
      else 
        convertView.asInstanceOf[DieView]
    val die = dice(position)
    resultView.die = die
    resultView.preferredSize = itemViewSize
    return resultView
  }
}