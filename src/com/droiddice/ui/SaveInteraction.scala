package com.droiddice.ui
import android.app.Dialog
import android.widget._
import com.droiddice._
import android.view.KeyEvent
import android.util.Log
import com.droiddice.model._
import com.droiddice.datastore._

/**
 * This class manages the saving of a dice set.
 */
class SaveInteraction(activity: RollActivity) {
    
	val TAG = "RollDiceActivity"

	/**
     * Construct save dialog
     */
	def dialog() : Dialog = {
		val dialog = new Dialog(activity)
	    dialog.setContentView(R.layout.save_dialog)
	    dialog.setTitle("Save Dice Set")
	    dialog
    }
	
	/**
	 * Prepare save dialog before displaying to user
	 */
	def prepare(dialog: Dialog) {
	    val nameEdit = dialog.findViewById(R.id.save_name_edit).asInstanceOf[EditText]
	    nameEdit.setText(activity.diceSet.name)

	    nameEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
	    	override def onEditorAction(view: TextView, actionId: Int, event: KeyEvent): Boolean = {
    	    	activity.diceSet.customName = nameEdit.getText().toString()
	    	    if (activity.diceSet.isSaved) {
	    	    	activity.dataStore.update(activity.diceSet, after)
	    	    } else {
	    	    	activity.dataStore.create(activity.diceSet, after)
	    	    }
	    		dialog.dismiss()
	    		false
	    	}})
	}

	/**
	 * Callback for processing async save
	 */
    private val after = (error: Throwable, id: Long) => {
        if (error == null) {
            processSaveCompleted(id) 
        } else {
    		Log.e(TAG, "Save diceeset failed", error)
    		activity.errorDialog("Failed to save dice set") 
    	}
    }

	/**
	 * Called once a save has been completed
	 */
	private def processSaveCompleted(id: Long) {
		if (!activity.diceSet.isSaved) {
		    Log.e(TAG, "setting id of newly saved dice set " + id)
	       	activity.diceSet = new ObservableDiceSet(new SavedDiceSet(activity.diceSet, id))
	       	activity.showRollView()
		}
	}

}