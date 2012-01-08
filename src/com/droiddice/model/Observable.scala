package com.droiddice.model

import scala.collection.mutable._
import android.util.Log

trait Observer[T] {
    def update(obj: T)
}

trait Observable[T] {
    var observers = new ListBuffer[Observer[T]]()
    private val TAG = "Observable"
    
    def addObserver(observer: Observer[T]) {
        Log.d(TAG, "Adding observer")
        observers += observer
    }
    
    def deleteObserver(observer: Observer[T]) {
        observers -= observer
    }
    
    def notifyObservers {
        observers.foreach(o => o.update(this.asInstanceOf[T]))
    }
    
    def countObservers = observers.size
}

