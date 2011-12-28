package com.droiddice.ui

import android.app.Activity
import android.os.Bundle
import android.app.Application.ActivityLifecycleCallbacks

trait ViewFinder extends Activity {
  def findById[T](id: Int) = findViewById(id).asInstanceOf[T]
}