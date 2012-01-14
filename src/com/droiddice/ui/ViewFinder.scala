package com.droiddice.ui

import android.app.Activity
import android.support.v4.app.Fragment

trait ViewFinder extends Activity {
  def findById[T](id: Int) = findViewById(id).asInstanceOf[T]
}

trait FragmentViewFinder extends Fragment {
  def findById[T](id: Int) = getActivity().findViewById(id).asInstanceOf[T]
}