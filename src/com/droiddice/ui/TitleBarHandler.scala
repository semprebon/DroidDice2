package com.droiddice.ui

import android.app.Activity
import android.widget._
import android.view.animation.AnimationUtils
import android.view._

import com.droiddice.R
import com.droiddice.model.DiceSet

/**
 * Handles interactions on the title bar; updating as the underlying DiceSet is changed, and
 * allowing the user to update the name of the dice set.
 */
trait TitleBarHandler extends Activity {

	var diceSet: DiceSet = _

	lazy val titleDisplay = findViewById(R.id.dice_set_name).asInstanceOf[TextView]
	lazy val titleEdit = findViewById(R.id.dice_set_name_edit).asInstanceOf[EditText]
	lazy val editButton = findViewById(R.id.dice_set_name_edit_button).asInstanceOf[ImageButton]
	lazy val titleView = titleDisplay.getParent().getParent().asInstanceOf[ViewAnimator]
  
	/**
	 * Associate a dice set with the title bar
	 */
	def bind(newDiceSet: DiceSet) {
    	diceSet = newDiceSet
    	updateTitleBar()
  	}
  
  	/**
  	 * Update the display with the current dice set name
  	 */
  	def updateTitleBar() {
  	    if (diceSet != null) {
  	    	titleDisplay.setText(diceSet.name)
  	    } else {
  	        titleDisplay.setText("No Dice")
  	    }
  	}

  	val NO_EDIT = false
  	val EDIT = true
  	
  	/**
  	 * Install UI event handlers
  	 * 
  	 * There are basically two events:
  	 * * when edit button is clicked, switch to edit view and enter edit mode
  	 * * when user done editing, update object and switch back to display view
  	 */
  	def installTitleHandlers() {
  	    editButton.setVisibility(View.VISIBLE)
  		titleView.setInAnimation(AnimationUtils.loadAnimation(getApplication(), R.anim.grow_from_center))
  		titleView.setOutAnimation(AnimationUtils.loadAnimation(getApplication(), R.anim.shrink_to_center))
    
  		editButton.setOnClickListener(new View.OnClickListener() {
  			override def onClick(view: View) {
  				titleView.showNext()
  				titleEdit.requestFocus()
  			}
  		})
    
  		titleEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
  			override def onEditorAction(view: TextView, actionId: Int, event: KeyEvent): Boolean = {
  				diceSet.name = titleEdit.getText().toString()
  				updateTitleBar()
  				titleView.showNext()
  				return false
  			}
  		})
  	}
  
}