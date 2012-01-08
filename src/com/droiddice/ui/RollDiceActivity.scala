package com.droiddice.ui

import scala.collection.JavaConversions._
import scala.collection.mutable.ListBuffer
import android.app.Activity
import android.os.Bundle
import android.widget._
import android.view._
import android.view.animation.AnimationUtils
import android.content.Context
import android.util.Log
import android.content.Intent
import com.droiddice._
import com.droiddice.model._
import android.view.View.OnClickListener

class RollDiceActivity extends Activity with ViewFinder with TitleBarHandler {

	val TAG = "RollDiceActivity"
 
	val NEW_DICE_RESULT = 0
  
	var currentDiceSet = new DiceSet("s6+s10")
	var historicDiceSets = Array[DiceSet]()
	
	val HISTORY_ITEMS_DISPLAYED = 3
	
	lazy val historyView = findById[ViewGroup](R.id.dice_set_history)
	lazy val resultTextView = findById[TextView](R.id.dice_result_text)
	lazy val diceLayout = findById[GridView](R.id.dice_layout)

  
	/** Called when the activity is first created. */
	override def onCreate(savedInstanceState: Bundle) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.roll_dice_activity)
		installRollButtonHandler()
		installChangeButtonHandler()
		installNewButtonHandler()
		installPickDicesetButtonHandler()
		bind(currentDiceSet)
		updateResult()
		updateHistory()
	}
  
	def installRollButtonHandler() {
		findById[Button](R.id.roll_activity_roll_button).setOnClickListener(new View.OnClickListener() {
			override def onClick(view: View)  {
				currentDiceSet.roll
				updateResult()
			}
		})
	}
  
	def installChangeButtonHandler() {
		findById[Button](R.id.roll_activity_change_button).setOnClickListener(new View.OnClickListener() {
			override def onClick(view: View)  {
				val changeDiceIntent = new Intent(view.getContext(), classOf[ChangeDiceActivity])
				changeDiceIntent.putExtra("Dice", currentDiceSet.spec)
				changeDiceIntent.putExtra("Name", currentDiceSet.name)
				startActivityForResult(changeDiceIntent, NEW_DICE_RESULT)
			}
		})
	}
  
	def installNewButtonHandler() {
		findById[Button](R.id.roll_activity_new_button).setOnClickListener(new View.OnClickListener() {
			override def onClick(view: View)  {
				val intent = new Intent(view.getContext(), classOf[ChangeDiceActivity])
				startActivityForResult(intent, NEW_DICE_RESULT)
			}
		})
	}
  
	def installPickDicesetButtonHandler() {
		findById[Button](R.id.dice_sets_selection_button).setOnClickListener(new View.OnClickListener() {
			override def onClick(view: View)  {
				val intent = new Intent(view.getContext(), classOf[PickDiceSetActivity])
				startActivityForResult(intent, NEW_DICE_RESULT)
			}
		})
	}
  
	def changeDiceSet(spec: String, name: String) {
	    val diceSet = new DiceSet(spec)
	    diceSet.name = name
	    changeDiceSet(diceSet) 
	}
	
	def changeDiceSet(newDiceSet: DiceSet) {
	    if (historicDiceSets.isEmpty) {
	    	Log.d(TAG, "History has nothing")
	    } else {
	    	Log.d(TAG, "History has:" + historicDiceSets.map(_.name).reduceLeft(_ + "," + _))
	    }
	    if (historicDiceSets.map(_.name).contains(newDiceSet.name)) {
	        if (!historicDiceSets.first.equals(newDiceSet)) {
	            historicDiceSets = Array[DiceSet](newDiceSet) ++ historicDiceSets.filter(_.name != newDiceSet.name)
	            updateHistory()
	        }
	    } else {
	    	Log.d(TAG, "Updating history")
	    	historicDiceSets = Array[DiceSet](newDiceSet) ++ historicDiceSets
	    	updateHistory()
	    }
	    currentDiceSet = newDiceSet
		bind(currentDiceSet)
		updateResult()
	}

	def configureHistoryItem(view: TextView, diceSet: DiceSet) {
       	view.setTag(diceSet)
       	view.setText(diceSet.name)
       	view.setOnClickListener(new OnClickListener {
       		override def onClick(view: View) {
       			changeDiceSet(view.getTag().asInstanceOf[DiceSet])
       		}
       	})
	}
	
	def updateHistory() {
	    for (i <- 0 until HISTORY_ITEMS_DISPLAYED) {
	        val view = historyView.getChildAt(i).asInstanceOf[TextView]
        	Log.d(TAG, "Setting history item " + i + "; visible?" + (i < historicDiceSets.length))
	        if (i < historicDiceSets.length) {
	        	configureHistoryItem(view, historicDiceSets(i))
	            view.setVisibility(View.VISIBLE)
	        } else {
	            view.setVisibility(View.INVISIBLE)
	        }
	    }
	}
	
	override def onActivityResult(requestCode: Int, resultCode: Int, intent: Intent) {
		super.onActivityResult(requestCode, resultCode, intent)
		if (requestCode == NEW_DICE_RESULT && resultCode == Activity.RESULT_OK) {
			val spec = intent.getExtras().getString("Dice").asInstanceOf[String]
			val name = intent.getExtras().getString("Name").asInstanceOf[String]
			changeDiceSet(spec, name)
		}
	}
  
	def updateResult() {
		resultTextView.setText(currentDiceSet.display)
    	
    	val dice: Array[Die] = currentDiceSet.dice.toArray
    	val adapter = new DiceViewAdapter(getApplication(), dice)
    	diceLayout.setAdapter(adapter)
	}
}