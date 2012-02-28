package com.droiddice.ui

import scala.collection.JavaConversions._
import scala.collection.mutable.ListBuffer
import android.app.Activity
import android.os.Bundle
import android.widget._
import android.view.ViewGroup
import android.view.View
import android.view.animation.AnimationUtils
import android.content.Context
import android.util.Log
import android.content.Intent
import com.droiddice._
import com.droiddice.model._
import android.view.View.OnClickListener
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.view.LayoutInflater
import com.droiddice.datastore.SavedDiceSet
import android.view.MenuItem
import android.view.Menu
import android.app.Dialog
import android.app.AlertDialog
import android.content.DialogInterface
import android.view.KeyEvent
import com.droiddice.datastore.DiceSetDataStore
import com.google.ads.AdView
import com.google.ads.AdSize
import com.google.ads.AdRequest
import android.view.Window
import android.content.res.Configuration
import android.view.animation.Animation

class RollActivity extends FragmentActivity with FragmentActivityViewFinder {

  	val DIALOG_NAME_EXISTS = 1
  	val DIALOG_SPECIFICATION = 2
  	val DIALOG_SAVE = 3

  	private var currentDiceSet = new ObservableDiceSet("2d6", null)
	var duplicateDiceSet: SavedDiceSet = _

	lazy val rollFragment = findFragmentById[RollFragment](R.id.roll_fragment)
	lazy val pickViewAlwaysOn = !(rollFragment.getView().getParent().isInstanceOf[ViewAnimator])
	lazy val saveInteraction = new SaveInteraction(this)
    lazy val dataStore = new DiceSetDataStore(this)
    lazy val framentViewParent = rollFragment.getView().getParent()
    
  	lazy val fragmentInAnimation = Array(
  	        AnimationUtils.loadAnimation(this, R.anim.slide_in_right),
  	        AnimationUtils.loadAnimation(this, R.anim.slide_in_left))
  	lazy val fragmentOutAnimation = Array(
  	        AnimationUtils.loadAnimation(this, R.anim.slide_out_left),
  			AnimationUtils.loadAnimation(this, R.anim.slide_out_right))
  	
	val TAG = "RollDiceActivity"
 
	def diceSet: ObservableDiceSet = { currentDiceSet }
  	
  	def diceSet_=(newDiceSet: ObservableDiceSet) {
	    Log.d(TAG, "changing activity dice set from " + currentDiceSet + " to " + newDiceSet)
		currentDiceSet = newDiceSet
	    rollFragment.bind(currentDiceSet)
	    rollFragment.updateResult()
  	}
  	
  	def variant = {
  	    val name = this.getPackageName()
  	    name.substring(name.lastIndexOf(".")+1)
  	}
  	
	/** Called when the activity is first created. */
	override def onCreate(savedInstanceState: Bundle) {
		super.onCreate(savedInstanceState)
		Log.d(TAG, "variant is " + variant)
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
		    this.requestWindowFeature(Window.FEATURE_NO_TITLE)
		}
		setContentView(R.layout.roll_activity)
		
        // Create the list fragment and add it as content for picklist.
        if (getSupportFragmentManager().findFragmentById(R.id.pick_fragment) == null) {
            val list = new PickFragment()
            getSupportFragmentManager().beginTransaction().add(R.id.pick_fragment, list).commit()
        }
	}
	
  	override def onCreateOptionsMenu(menu: Menu): Boolean = {
  		val inflater = getMenuInflater()
  		inflater.inflate(R.menu.edit_options_menu, menu)
  		return true
  	}    

	def changeDiceSet(newDiceSet: ObservableDiceSet) {
  		diceSet = newDiceSet
	}

  	def rollDice() {
  	    rollFragment.rollDice()
  	}
  	
  	def animateDiceSet(animation: Animation) {
  		rollFragment.diceLayout.setAnimation(animation)
  	}
  	
	override def onOptionsItemSelected(item: MenuItem): Boolean = {
		item.getItemId() match {
			case R.id.specify_menu_item => {
			    showDialog(DIALOG_SPECIFICATION)
			    return true
			}
			case R.id.save_menu_item => {
			    showDialog(DIALOG_SAVE)
			    return true
			}
			case _ => return super.onOptionsItemSelected(item)
		}
	}
	
	/**
	 * Ensure that a given fragment's view is on screen
	 */
	def showFragmentView(index: Int) {
	    rollFragment.getView().getParent() match {
	        case f: ViewAnimator => {
	            f.setInAnimation(fragmentInAnimation(f.getDisplayedChild))
	            f.setOutAnimation(fragmentOutAnimation(f.getDisplayedChild))
	            f.setDisplayedChild(index)
	        }
	        case _ =>
	    }
	}
	
	def isFragmentVisible(index: Int): Boolean = {
	    rollFragment.getView().getParent() match {
	        case f: ViewAnimator => return f.getDisplayedChild == index
	        case _ => return true
	    }
	}
	
	def isPickFragmentVisible() = isFragmentVisible(1)
	def isRollFragmentVisible() = isFragmentVisible(0)
	
	def showPickFragment() = showFragmentView(1)
	def showRollFragment() = showFragmentView(0)

  	def errorDialog(message: String) {
		    val builder = new AlertDialog.Builder(this)
		    builder.setMessage(message)
		    	.setCancelable(false)
		    	.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		    		def onClick(dialog: DialogInterface, id: Int) { dialog.cancel() }
		    	})
		    val alert = builder.create()
		    alert.show()
    }
    
  	def cancelDialog(message: String) {
		    val builder = new AlertDialog.Builder(this)
		    builder.setMessage(message)
		    	.setCancelable(false)
		    	.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		    		def onClick(dialog: DialogInterface, id: Int) { dialog.cancel() }
		    	})
		    val alert = builder.create()
		    alert.show()
    }

	override def onCreateDialog(id: Int): Dialog = {
  	    val dialog = id match {
  		    case DIALOG_SAVE => {
  		        saveInteraction.dialog()
  		    }
  		    case DIALOG_SPECIFICATION => {
  		    	val dialog = new Dialog(this)
  		    	dialog.setContentView(R.layout.specification_dialog)
  		    	dialog.setTitle("Enter Specification")

  		    	dialog
  		    }
  		}
  		return dialog
  	}
	
	override def onPrepareDialog(id: Int, dialog: Dialog) {
	    id match {
	        case DIALOG_SAVE => {
	            saveInteraction.prepare(dialog)
	        }
	        case DIALOG_SPECIFICATION => {
  		    	val specificationEdit = dialog.findViewById(R.id.specification_edit).asInstanceOf[EditText]
  		    	specificationEdit.setText(diceSet.spec)
  		    	specificationEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
  		    		override def onEditorAction(view: TextView, actionId: Int, event: KeyEvent): Boolean = {
  		    		    try {
  		    		    	val newDiceSet = new SavedDiceSet(specificationEdit.getText().toString(), diceSet.id)
			    		    newDiceSet.customName = diceSet.customName
			    			diceSet = new ObservableDiceSet(newDiceSet)
			    		    dialog.dismiss()
  		    		    } catch {
  		    		        case e: InvalidSpecificationException => dialog.dismiss()
  		    		    }
  		    			false
  		    		}})
	        }
	    }
	}
	
	override def onBackPressed() {
	    Log.d(TAG, "back pressed roll vis? " + isRollFragmentVisible() + "  pick? " + isPickFragmentVisible())
		if (isPickFragmentVisible() && !isRollFragmentVisible()) {
		    Log.d(TAG, "showing roll")
		    showRollFragment()
		} else {
		    finish()
		}
	}
	
}

