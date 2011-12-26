package com.droiddice

import android.app.Activity
import android.os.Bundle
import android.widget.TextView

class RollDiceActivity extends Activity {

  var currentDiceSet = new DiceSet("s6+s10")
  
  /** Called when the activity is first created. */
  override def onCreate(savedInstanceState: Bundle) {
	super.onCreate(savedInstanceState)
	setContentView(R.layout.main)
	val tv = findViewById(R.id.diceSetName).asInstanceOf[TextView]
	tv.setText(currentDiceSet.name)
  }
}