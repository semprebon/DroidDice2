package com.droiddice.model
import android.util.Log
import com.droiddice.datastore.SavedDiceSet
import java.io.Serializable
import android.content.Intent
import android.os.Bundle

object ObservableDiceSet {
    private val TAG = "ObservableDiceSet"
    
    def saveTo(intent: Intent, diceSet: ObservableDiceSet) {
        intent.putExtra("diceSet", diceSet.toBundle) 
    }
    
	def fetchFrom(intent: Intent): ObservableDiceSet = {
	    fromBundle(intent.getBundleExtra("diceSet"))
	}
	
	def fromBundle(bundle: Bundle): ObservableDiceSet = {
	    if (bundle == null) return null
		val diceSet = new ObservableDiceSet(bundle.getString("spec"), bundle.getLong("id"), bundle.getString("name"))
		diceSet.valuesString = bundle.getString("values")
		return diceSet
	}

	def withDiceSetFrom(bundle: Bundle, f: (ObservableDiceSet) => Unit) {
	    if (bundle != null) f(ObservableDiceSet.fromBundle(bundle))
	}
	

}

class ObservableDiceSet(spec2: String, id2: Long) extends SavedDiceSet(spec2, id2) with Observable[DiceSet] with Serializable {

    private val TAG = ObservableDiceSet.TAG
    
    def this(spec: String, id: Long, name: String) = {
        this(spec, id)
        customName = name
    }
    
    def this(spec: String, name: String) = this(spec, SavedDiceSet.UNSAVED_ID, name)
    
    def this(diceSet: SavedDiceSet) = {
    	this(diceSet.spec, diceSet.id, diceSet.customName)
    	Log.d(TAG, "value= " + valuesString)
    	valuesString = diceSet.valuesString
    }
    
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

	def toBundle(): Bundle = {
	    val bundle  = new Bundle()
        bundle.putString("spec", spec)
        bundle.putString("name", customName)
        bundle.putLong("id", id)
        bundle.putString("values", valuesString)
	    return bundle
	}
}

