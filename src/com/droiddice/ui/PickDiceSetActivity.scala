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

class PickDiceSetActivity extends FragmentActivity with ViewFinder with LoaderManager.LoaderCallbacks[Cursor] {
//class PickDiceSetActivity extends Activity with ViewFinder with LoaderManager.LoaderCallbacks[Cursor] {

    var adapter: SimpleCursorAdapter = _
    lazy val diceSetListView = findById[ListView](R.id.dice_set_selection_list)
    
   	override def onCreate(savedInstanceState: Bundle) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.pick_dice_set_activity)
		adapter = new SimpleCursorAdapter(this, R.layout.list_dice_set_item, 
		        null, Array("name"), Array(R.id.dice_set_item, 0))
		diceSetListView.setAdapter(adapter)
		getSupportLoaderManager().initLoader(0, null, this)
	}

    /**
     * Called when new loader needs to be created
     */
    def onCreateLoader(id: Int, args: Bundle): Loader[Cursor] = {
        val baseUri = DiceSetProvider.CONTENT_URI
        return new CursorLoader(this, baseUri,
                DiceSetProvider.PUBLIC_COLUMNS, null, null,
                DiceSetProvider.NAME + " ASC")
    }

    def onLoadFinished(loader: Loader[Cursor], data: Cursor) {
        adapter.changeCursor(data);
    }

    def onLoaderReset(loader: Loader[Cursor]) {
        adapter.changeCursor(null);
    }

}