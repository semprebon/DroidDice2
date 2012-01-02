package com.droiddice.ui

import android.app.Activity

trait ViewFinder extends Activity {
  def findById[T](id: Int) = findViewById(id).asInstanceOf[T]
}