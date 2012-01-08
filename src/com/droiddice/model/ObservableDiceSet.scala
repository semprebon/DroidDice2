package com.droiddice.model
import android.util.Log

class ObservableDiceSet(spec: String, name: String) extends DiceSet(spec, name) with Observable[DiceSet] {

    private val TAG = "ObservableDiceSet"
        
    override def roll = {
        super.roll
        notifyObservers
        value
    }
    
    override def add(newSpec: String): DiceSet = {
        Log.d(TAG, "in observable adding " + newSpec)
        super.add(newSpec)
        Log.d(TAG, "notifying " + observers.size + " observers " + newSpec)
        notifyObservers
        this
    }

    override def remove(index: Int): DiceSet = {
        super.remove(index)
        notifyObservers
        this
    }
}

