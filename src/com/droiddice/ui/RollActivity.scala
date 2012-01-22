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

class RollActivity extends FragmentActivity with FragmentActivityViewFinder {

	var currentDiceSet = new ObservableDiceSet("d6", null)
	lazy val rollFragment = findFragmentById[RollFragment](R.id.roll_fragment)
	
	val TAG = "RollDiceActivity"
 
	/** Called when the activity is first created. */
	override def onCreate(savedInstanceState: Bundle) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.roll_activity)
        // Create the list fragment and add it as content for picklist.
        if (getSupportFragmentManager().findFragmentById(R.id.pick_fragment) == null) {
            val list = new PickFragment()
            getSupportFragmentManager().beginTransaction().add(R.id.pick_fragment, list).commit()
        }
	}
	
	def changeDiceSet(newDiceSet: ObservableDiceSet) {
	    currentDiceSet = newDiceSet
		rollFragment.bind(currentDiceSet)
		rollFragment.updateHistory(newDiceSet)
	    rollFragment.updateResult()
	}

	/**
	 * Ensure that a given fragment's view is on screen
	 */
	def showFragmentView(index: Int) {
	    rollFragment.getView().getParent() match {
	        case f: ViewAnimator => f.setDisplayedChild(index)
	    }
	}
	
	def showPickView() = showFragmentView(1)
	def showRollView() = showFragmentView(0)

}

class RollFragment extends Fragment with FragmentViewFinder with TitleBarHandler {
    
	val TAG = "RollDiceActivity"	
 
	val NEW_DICE_RESULT = 0
  
	var historicDiceSets = Array[ObservableDiceSet]()
	
	val HISTORY_ITEMS_DISPLAYED = 3
	
	lazy val historyView = findById[ViewGroup](R.id.dice_set_history)
	lazy val resultTextView = findById[TextView](R.id.dice_result_text)
	lazy val diceLayout = findById[GridView](R.id.dice_layout)
	lazy val activity = getActivity().asInstanceOf[RollActivity]
	
	def currentDiceSet() = activity.currentDiceSet
	
	/** Called when the activity is first created. */
	override def onActivityCreated(savedInstanceState: Bundle) {
		super.onActivityCreated(savedInstanceState)
		restoreInstanceState(savedInstanceState)
		installRollButtonHandler()
		installChangeButtonHandler()
		installNewButtonHandler()
		installPickDicesetButtonHandler()
		bind(activity.currentDiceSet)
		updateResult()
		updateHistory()
	}
	
	override def onCreateView(inflater: LayoutInflater, container: ViewGroup, state: Bundle): View = {
		inflater.inflate(R.layout.roll_fragment, container)
	}
	
	/** 
	 * Save current and recent dice sets
	 */
	 override def onSaveInstanceState(state: Bundle) {
	    Log.d(TAG, "onSaveInstanceState:" + currentDiceSet)
		state.putBundle("diceSet", currentDiceSet.toBundle)
		historicDiceSets.zipWithIndex.foreach[Unit](t => state.putBundle(t._2.toString, t._1.toBundle))
		super.onSaveInstanceState(state)
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
	
	def updateResult() {
		resultTextView.setText(currentDiceSet.display)
    	
    	val dice: Array[Die] = currentDiceSet.dice.toArray
    	val adapter = new DiceViewAdapter(getActivity().getApplication(), dice)
    	diceLayout.setAdapter(adapter)
	}

	def updateHistory(diceSet: ObservableDiceSet) {
	    if (historicDiceSets.isEmpty) {
	    	Log.d(TAG, "History has nothing")
	    } else {
	    	Log.d(TAG, "History has:" + historicDiceSets.map(_.name).reduceLeft(_ + "," + _))
	    }
	    if (historicDiceSets.map(_.name).contains(diceSet.name)) {
	        if (!historicDiceSets.first.equals(diceSet)) {
	            historicDiceSets = Array[ObservableDiceSet](diceSet) ++ historicDiceSets.filter(_.name != diceSet.name)
	            updateHistory()
	        }
	    } else {
	    	Log.d(TAG, "Updating history")
	    	historicDiceSets = Array[ObservableDiceSet](diceSet) ++ historicDiceSets
	    	updateHistory()
	    }
	}

	/**
	 * Restore current and recent dice sets
	 */
	def restoreInstanceState(state: Bundle) {
	    if (state == null) return
	    val bundle = state.getBundle("diceSet")
	    ObservableDiceSet.withDiceSetFrom(state.getBundle("diceSet"), activity.changeDiceSet(_))
		historicDiceSets = Array[ObservableDiceSet]()
	    Log.d(TAG, "onRestoreInstanceState:" + currentDiceSet())
		(0 until 3).foreach(i => { ObservableDiceSet.withDiceSetFrom(state.getBundle(i.toString), updateHistory(_)) })
	}
	
	override def onActivityResult(requestCode: Int, resultCode: Int, intent: Intent) {
		super.onActivityResult(requestCode, resultCode, intent)
		if (requestCode == NEW_DICE_RESULT && resultCode == Activity.RESULT_OK) {
		    val diceSet = ObservableDiceSet.fetchFrom(intent)
		    Log.d(TAG, "got " + diceSet + "from other")
			activity.changeDiceSet(diceSet)
		}
	}
  
	private def installRollButtonHandler() {
		findById[Button](R.id.roll_activity_roll_button).setOnClickListener(new View.OnClickListener() {
			override def onClick(view: View)  {
				currentDiceSet.roll()
				Log.d(TAG, "value= " + currentDiceSet.value + "  dice=" + currentDiceSet)
				updateResult()
			}
		})
	}
  
	private def installChangeButtonHandler() {
		findById[Button](R.id.roll_activity_change_button).setOnClickListener(new View.OnClickListener() {
			override def onClick(view: View) {
			    startActivityForResult(
			            EditActivity.intent(getActivity(), currentDiceSet), NEW_DICE_RESULT)
			} 
		})
	}
  
	private def installNewButtonHandler() {
		findById[Button](R.id.roll_activity_new_button).setOnClickListener(new View.OnClickListener() {
			override def onClick(view: View)  {
			    val intent = EditActivity.intent(getActivity(), null)
				startActivityForResult(intent,  NEW_DICE_RESULT)
			}
		})
	}
  
	private def installPickDicesetButtonHandler() {
		findById[Button](R.id.dice_sets_selection_button).setOnClickListener(new View.OnClickListener() {
			override def onClick(view: View)  {
			    activity.showPickView()
			}
		})
	}
  
	private def configureHistoryItem(view: TextView, diceSet: ObservableDiceSet) {
       	view.setTag(diceSet)
       	view.setText(diceSet.name)
       	view.setOnClickListener(new OnClickListener {
       		override def onClick(view: View) {
       			activity.changeDiceSet(view.getTag().asInstanceOf[ObservableDiceSet])
       		}
       	})
	}
	
}