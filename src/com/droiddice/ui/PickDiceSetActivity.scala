package com.droiddice.ui

import android.view._
import android.content._
import android.app.Activity
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.view.View.OnClickListener
import android.support.v4.app.LoaderManager
import android.database.Cursor
import android.widget.ListView
import android.util.Log
import android.support.v4.content.Loader
import android.support.v4.content.CursorLoader
import android.support.v4.app.FragmentActivity
import android.support.v4.widget.SimpleCursorAdapter
import scala.collection.JavaConversions._
import scala.collection.mutable.ListBuffer
import com.droiddice._
import com.droiddice.model._
import com.droiddice.datastore.DiceSetProvider
import android.widget.AdapterView
import com.droiddice.datastore.DiceSetMapper
import android.widget.TextView
import android.widget.LinearLayout
import android.widget.ImageView

class PickDiceSetActivity extends FragmentActivity with ViewFinder with LoaderManager.LoaderCallbacks[Cursor] {
//class PickDiceSetActivity extends Activity with ViewFinder with LoaderManager.LoaderCallbacks[Cursor] {

    var adapter: SimpleCursorAdapter = _
    lazy val diceSetListView = findById[ListView](R.id.dice_set_selection_list)
    
    val TAG = "PickDiceSetActivity"
        
   	override def onCreate(savedInstanceState: Bundle) {
		super.onCreate(savedInstanceState)
		Log.d(TAG, "onCreate steting content view")
		setContentView(R.layout.pick_dice_set_activity)
		Log.d(TAG, "onCreate creating adapter")
		adapter = new DiceSetCursorAdapter(this)
		Log.d(TAG, "onCreate seting adapter")
		diceSetListView.setAdapter(adapter)
		Log.d(TAG, "onCreate initializing loader")
		getSupportLoaderManager().initLoader(0, null, this)
		Log.d(TAG, "onCreate setting click handler")
		diceSetListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			override def onItemClick(parent: AdapterView[_], view: View, position: Int, id: Long) {
	    		val diceSet = view.getTag().asInstanceOf[DiceSet]
	    		Log.d(TAG, "clicked on " + diceSet.name + " (" + diceSet.spec + ")")
	    		val intent = getIntent()
	    		intent.putExtra("Dice", diceSet.spec)
	    		intent.putExtra("Name", diceSet.name)
	    		setResult(Activity.RESULT_OK, intent)
	    		finish()
	    	}
		})

	}
    
    /**
     * Called when new loader needs to be created
     */
    def onCreateLoader(id: Int, args: Bundle): Loader[Cursor] = {
        Log.d(TAG, "onCreateLoader")
        val baseUri = DiceSetProvider.CONTENT_URI
        return new CursorLoader(this, baseUri,
                DiceSetProvider.PUBLIC_COLUMNS, null, null,
                DiceSetProvider.NAME + " ASC")
    }

    def onLoadFinished(loader: Loader[Cursor], data: Cursor) {
        Log.d(TAG, "onLoadFinished")
        adapter.swapCursor(data);
    }

    def onLoaderReset(loader: Loader[Cursor]) {
        adapter.swapCursor(null);
    }

}
    