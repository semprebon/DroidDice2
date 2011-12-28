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

class RollDiceActivity extends Activity with TitleBarHandler with ViewFinder{

  val TAG = "RollDiceActivity"
 
  val NEW_DICE_RESULT = 0
  
  var currentDiceSet = new DiceSet("s6+s10")
  
  /** Called when the activity is first created. */
  override def onCreate(savedInstanceState: Bundle) {
	super.onCreate(savedInstanceState)
	setContentView(R.layout.roll_dice_activity)
	installRollButtonHandler()
	installChangeButtonHandler()
	bind(currentDiceSet)
	installTitleHandlers()
	updateResult()
  }
  
  def installRollButtonHandler() {
    findById[Button](R.id.rollActivityRollButton).setOnClickListener(new View.OnClickListener() {
      override def onClick(view: View)  {
        currentDiceSet.roll
        updateResult()
      }
    })
  }
  
  def installChangeButtonHandler() {
    findById[Button](R.id.rollActivityChangeButton).setOnClickListener(new View.OnClickListener() {
      override def onClick(view: View)  {
        val changeDiceIntent = new Intent(view.getContext(), classOf[ChangeDiceActivity]);
        changeDiceIntent.putExtra("Dice", currentDiceSet.spec);
		startActivityForResult(changeDiceIntent, NEW_DICE_RESULT);
      }
    })
  }
  
  override def onActivityResult(requestCode: Int, resultCode: Int, intent: Intent) {
	super.onActivityResult(requestCode, resultCode, intent)
	Log.d(TAG, "requestCode=" + requestCode + " result=" + resultCode)
	if (requestCode == NEW_DICE_RESULT && resultCode == Activity.RESULT_OK) {
	  Log.d(TAG, "Got returned dice:" + intent.getExtras().getString("Dice").asInstanceOf[String])
	  currentDiceSet = new DiceSet(intent.getExtras().getString("Dice").asInstanceOf[String])
	  bind(currentDiceSet)
	  updateResult()
	}
  }
  
  def updateResult() {
    val childWidth = ViewGroup.LayoutParams.WRAP_CONTENT
    val childHeight = ViewGroup.LayoutParams.WRAP_CONTENT
    
    findById[TextView](R.id.diceResultText).setText(currentDiceSet.display)
    val dice: Array[Die] = currentDiceSet.dice.toArray
    val adapter = new DiceViewAdapter(getApplication(), dice)
    findById[GridView](R.id.diceLayout).setAdapter(adapter)
  }
}