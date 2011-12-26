package com.droiddice

import com.droiddice.model.DiceSet;

import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import android.widget.Button
import android.view.View

class RollDiceActivity extends Activity {

  var currentDiceSet = new DiceSet("s6+s10")
  
  /** Called when the activity is first created. */
  override def onCreate(savedInstanceState: Bundle) {
	super.onCreate(savedInstanceState)
	setContentView(R.layout.main)
	updateTitleBar()
	installRollButtonHandler()
  }
  
  def updateTitleBar() {
	findViewById(R.id.diceSetName).asInstanceOf[TextView].setText(currentDiceSet.name)
  }
  
  def installRollButtonHandler() {
    findViewById(R.id.rollActivityRollButton).asInstanceOf[Button].setOnClickListener(new View.OnClickListener() {
      override def onClick(view: View)  {
        val newRoll = currentDiceSet.roll
        findViewById(R.id.diceResultText).asInstanceOf[TextView].setText("Hello?")
      }
    })
  }
}