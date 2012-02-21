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
import android.view.ContextMenu.ContextMenuInfo
import android.widget.AdapterView.AdapterContextMenuInfo
import com.droiddice.R
import com.droiddice.datastore.DiceSetDataStore
import android.support.v4.app.ListFragment
import com.droiddice.datastore.SavedDiceSet

class PickFragment extends ListFragment with FragmentViewFinder with LoaderManager.LoaderCallbacks[Cursor] {
//class PickDiceSetActivity extends Activity with ViewFinder with LoaderManager.LoaderCallbacks[Cursor] {

    lazy val dataStore = new DiceSetDataStore(getActivity())
	lazy val activity = getActivity().asInstanceOf[RollActivity]
    
    var adapter: SimpleCursorAdapter = _
    
    val TAG = "PickDiceSetActivity"
       
    override def onActivityCreated(savedInstanceState: Bundle) {
    	super.onActivityCreated(savedInstanceState)
    	setEmptyText("No dice sets created")
		adapter = new DiceSetCursorAdapter(getActivity())
		setListAdapter(adapter)
		getActivity().getSupportLoaderManager().initLoader(0, null, this)
		getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
			override def onItemClick(parent: AdapterView[_], view: View, position: Int, id: Long) {
	    		val diceSet = view.getTag().asInstanceOf[SavedDiceSet]
	    		val intent = getActivity().getIntent()
	    		Log.d(TAG, "intent=" + intent)
	    		Log.d(TAG, "diceSet=" + diceSet)
	    		activity.changeDiceSet(new ObservableDiceSet(diceSet))
	    		activity.showRollView()
	    	}
		})
		registerForContextMenu(getListView())
    }
    
    def updateDisplay() {
    	Log.d(TAG, "notifying list view")
    	adapter.notifyDataSetChanged() 
    }

	override def onCreateContextMenu(menu: ContextMenu, view: View, menuInfo: ContextMenuInfo) {
		super.onCreateContextMenu(menu, view, menuInfo)
		getActivity().getMenuInflater().inflate(R.menu.dice_set_context, menu)
	}
	
	override def onContextItemSelected(item: MenuItem): Boolean = {
		val info = item.getMenuInfo().asInstanceOf[AdapterContextMenuInfo]
		val diceSet = info.targetView.getTag().asInstanceOf[SavedDiceSet]
		item.getItemId() match {
			case R.id.delete_dice_set => { deleteDiceSet(diceSet); true }
			case _ => super.onContextItemSelected(item)
		}
	}

	def deleteDiceSet(diceSet: SavedDiceSet) {
	    dataStore.delete(diceSet, (error: Throwable, id: Long) => {
	        val run = new Runnable() { override def run { updateDisplay() } }
	        Log.d(TAG, "running runable")
	        getActivity().runOnUiThread(run) 
	    })
	}
	
	/**
     * Called when new loader needs to be created
     */
    def onCreateLoader(id: Int, args: Bundle): Loader[Cursor] = {
        Log.d(TAG, "onCreateLoader")
        val baseUri = DiceSetProvider.contentUri(activity)
        return new CursorLoader(getActivity(), baseUri,
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
    