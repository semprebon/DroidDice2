package com.droiddice.ui

import android.app.Activity
import android.widget._
import android.view.animation.AnimationUtils
import android.view._
import com.droiddice.R
import com.droiddice.model._
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.content.Context
import android.support.v4.app.Fragment

/**
 * Handles interactions on the title bar; updating as the underlying DiceSet is changed, and
 * allowing the user to update the name of the dice set.
 */
trait TitleBarHandler extends Fragment with Observer[DiceSet] with FragmentViewFinder {

	lazy val titleDisplay = findById[TextView](R.id.dice_set_name)
	lazy val titleEdit = findById[EditText](R.id.dice_set_name_edit)
	lazy val editButton = findById[ImageButton](R.id.dice_set_name_edit_button)
	
	lazy val titleView = titleDisplay.getParent().getParent().asInstanceOf[ViewAnimator]
	lazy val textColor = getResources().getColor(R.color.text_color)
	lazy val textColorSecondary = getResources().getColor(R.color.text_color_secondary)

	var diceSet: DiceSet = _
	
	private val TAG = "TitleBarHandler"
	    
	override def update(diceSet: DiceSet) = {
	    Log.d(TAG, "updated in titlebar: " + diceSet.spec)
		updateTitleBar(diceSet)
	}
	
	/**
	 * Associate a dice set with the title bar
	 */
	def bind(newDiceSet: DiceSet) {
	    Log.d(TAG, "binding " + newDiceSet)
    	diceSet = newDiceSet
    	diceSet match  {
    	    case observable: ObservableDiceSet => observable.addObserver(this)
    	    case _ => Log.d(TAG, "non-observable dice set: " + diceSet.spec)
    	}
    	updateTitleBar(diceSet)
  	}
  
	def diceSetTextColor(diceSet: DiceSet) = if (diceSet.isNamed) textColor else textColorSecondary
	
  	/**
  	 * Update the display with the current dice set name
  	 */
  	def updateTitleBar(diceSet: DiceSet) {
  	    Log.d(TAG, "updateTitleBar with " + diceSet.spec + "/" + diceSet.name)
  	    if (diceSet != null) {
  	    	titleDisplay.setText(diceSet.name)
  	    	titleDisplay.setTextColor(diceSetTextColor(diceSet))
  	    } else {
  	        titleDisplay.setText("No Dice")
  	    }
  	}

  	val NO_EDIT = false
  	val EDIT = true
  	  
}