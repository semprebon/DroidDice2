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

class PickActivity extends FragmentActivity {
    override protected def onCreate(savedInstanceState: Bundle) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pick_dice_set_activity)

        // Create the list fragment and add it as our sole content.
        if (getSupportFragmentManager().findFragmentById(R.id.pick_fragment) == null) {
            val list = new PickFragment()
            getSupportFragmentManager().beginTransaction().add(R.id.pick_fragment, list).commit()
        }
    }
}
    
class PickFragment extends ListFragment with FragmentViewFinder with LoaderManager.LoaderCallbacks[Cursor] {
//class PickDiceSetActivity extends Activity with ViewFinder with LoaderManager.LoaderCallbacks[Cursor] {

    lazy val dataStore = new DiceSetDataStore(getActivity())
    
    var adapter: SimpleCursorAdapter = _
    
    val TAG = "PickDiceSetActivity"
       
    override def onActivityCreated(savedInstanceState: Bundle) {
    	super.onActivityCreated(savedInstanceState)
    	setEmptyText("No dice sets created")
    	Log.d(TAG, "onCreate creating adapter")
		adapter = new DiceSetCursorAdapter(getActivity())
		Log.d(TAG, "onCreate seting adapter")
		setListAdapter(adapter)
		Log.d(TAG, "onCreate initializing loader")
		getActivity().getSupportLoaderManager().initLoader(0, null, this)
		Log.d(TAG, "onCreate setting click handler")
		getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
			override def onItemClick(parent: AdapterView[_], view: View, position: Int, id: Long) {
	    		val diceSet = view.getTag().asInstanceOf[DiceSet]
	    		Log.d(TAG, "clicked on " + diceSet.name + " (" + diceSet.spec + ")")
	    		val intent = getActivity().getIntent()
	    		intent.putExtra("Dice", diceSet.spec)
	    		intent.putExtra("Name", diceSet.name)
	    		getActivity().setResult(Activity.RESULT_OK, intent)
	    		getActivity().finish()
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
		val diceSet = info.targetView.getTag().asInstanceOf[DiceSet]
		item.getItemId() match {
			case R.id.edit_dice_set => { 
			    	startActivity(
			            EditActivity.intent(getActivity(), null))
			        true 
			    }
			case R.id.delete_dice_set => { deleteDiceSet(diceSet); true }
			case _ => super.onContextItemSelected(item)
		}
	}

	def deleteDiceSet(diceSet: DiceSet) {
	    dataStore.delete(diceSet, () => {
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
        val baseUri = DiceSetProvider.CONTENT_URI
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
    