package com.droiddice.ui

import android.app.Activity
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity

trait ViewFinder extends Activity {
  def findById[T](id: Int) = findViewById(id).asInstanceOf[T]
}

trait FragmentActivityViewFinder extends FragmentActivity with ViewFinder {
  def findFragmentById[T](id: Int) = getSupportFragmentManager().findFragmentById(id).asInstanceOf[T]
}

trait FragmentViewFinder extends Fragment {
  def findById[T](id: Int) = getActivity().findViewById(id).asInstanceOf[T]
}