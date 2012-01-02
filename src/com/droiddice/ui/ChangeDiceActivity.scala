package com.droiddice.ui

import android.view._
import android.widget._
import android.app.Activity
import android.os.Bundle
import android.content.Context
import android.util.Log
import android.view.View.OnTouchListener
import com.droiddice._
import com.droiddice.model._
import com.droiddice.datastore.DiceSetProvider
import android.os.AsyncTask
import android.net.Uri
import android.content.ContentValues
import android.app.ProgressDialog
import com.droiddice.datastore.DiceSetMapper

class ChangeDiceActivity extends Activity with TitleBarHandler with ViewFinder {

	val TAG = "EditDiceActivity"
 
	val GALLERY_PAGES = Array[GalleryPage](
		new GalleryPage("Standard", Array("d4", "d6", "d8", "d10", "d12", "d20")),
		new GalleryPage("Savage Worlds", Array("s4", "s6", "s8", "s10", "s20")),
		new GalleryPage("Other", Array("dF", "+1", "-1")))
      
	var currentDiceSet: DiceSet = _
	var gestureDetector: GestureDetector = _
  
	/** Called when the activity is first created. */
	override def onCreate(savedInstanceState: Bundle) {
		Log.d(TAG, "onCreate called")
		super.onCreate(savedInstanceState)

		val extras = getIntent().getExtras()
		currentDiceSet = if (extras != null) 
				new DiceSet(extras.get("Dice").asInstanceOf[String], extras.get("Name").asInstanceOf[String])
			else
				new DiceSet("d6")
		
		setContentView(R.layout.change_dice_activity)

		bind(currentDiceSet)
		installTitleHandlers()
		createCurrentSelection()
		Log.d(TAG, "calling createDiceGallery")
		createDiceGallery()
	}
  
	/**
     * Save result for original requester (normally RoleDiceActivity)
     */
	override def onBackPressed() {
		val intent = getIntent()
		intent.putExtra("Dice", currentDiceSet.spec)
		intent.putExtra("Name", currentDiceSet.name)
		setResult(Activity.RESULT_OK, intent)
		
		Log.d(TAG, "Executig async task")
		new createOrUpdateDataStore().execute(currentDiceSet)
		
		//finish()
	}
  
    /* This should really by DiceSet, not Object; but due to bug, scala sometimes has problems 
     * passing varargs; but works with Object... 
     */ 
	class createOrUpdateDataStore extends AsyncTask[Object, Void, Int] {
	    var dialog: ProgressDialog = _
	    
		override protected def doInBackground(diceSets: Object*): Int = {
			val diceSet = diceSets(0).asInstanceOf[DiceSet]
			val values = DiceSetMapper.diceSetToValues(diceSet)
			
			val contentResolver = getContentResolver()
			val uri = DiceSetProvider.CONTENT_URI
		    Log.d(TAG, "Quering DiceSetProvider for " + currentDiceSet.name)
			val cursor = managedQuery(uri, Array(DiceSetProvider._ID), DiceSetProvider.NAME + "=?", Array(currentDiceSet.name), null)
			val id = if (cursor.moveToFirst()) {
					val id = cursor.getInt(0)
					Log.d(TAG, "Updating DiceSet " + id)
					val itemUri = Uri.withAppendedPath(uri, id.toString())
					getContentResolver().update(itemUri, values, null, null)
					id
				} else {
					Log.d(TAG, "Adding new DiceSet " + currentDiceSet.name)
					val itemUri = getContentResolver().insert(DiceSetProvider.CONTENT_URI, values)
					Log.d(TAG, "Added " + itemUri)
					itemUri.getLastPathSegment().toInt
				}
			return id
		}
		
		override protected def onPreExecute() {
		    dialog = ProgressDialog.show(ChangeDiceActivity.this, "Dice", "Updating...", true)
		}

		override protected def onPostExecute(result: Int) {
			dialog.dismiss()
		    Log.d(TAG, "Returning dice:" + currentDiceSet.spec)
		    finish()
		}
	}

	/**
	 * Create view for displaying the current dice set
	 */
	def createCurrentSelection() {
		val selectionView = findById[GridView](R.id.selected_dice)
		selectionView.setAdapter(new DiceViewAdapter(this, currentDiceSet.dice.toArray[Die]))
		selectionView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			override def onItemClick(parent: AdapterView[_], view: View, position: Int, id: Long) {
				currentDiceSet = currentDiceSet.remove(position)
				bind(currentDiceSet)
				createCurrentSelection()
			}
		})
	}

	/**
	 * Create the gallery
	 */
	def createDiceGallery() {
		Log.d(TAG, "Setting up galallery")
		val galleryView = findById[Gallery](R.id.dice_gallery)
		val adapter = new GalleryPageViewAdapter(this, GALLERY_PAGES)
		galleryView.setAdapter(adapter)
	}
  
	/**
	 * This class holds all the information required to generate a gallery page
	 */
	class GalleryPage(val name: String, dieSpecs: Array[String]) {
		val dice = dieSpecs.map(DiceSetHelper.singleDieFactory(_))
	}
  
	/**
	 * This adapter is responsible for building each page in the dice gallery 
	 */
	class GalleryPageViewAdapter(activity: Activity, pages: Array[GalleryPage]) 
			extends ArrayAdapter[GalleryPage](activity, 0,  pages) {
		 
		var itemOnTouchListener: OnTouchListener = _
	    
	    /**
	     * Create the grid of dice to select from
	     */
	    def createDiceGrid(pageView: View, page: GalleryPage) {
	    	val gridView = pageView.findViewById(R.id.dice_gallery_page_grid).asInstanceOf[GridView]
	    	gridView.setAdapter(new DiceViewAdapter(activity, page.dice))
	    	gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
	    		override def onItemClick(parent: AdapterView[_], view: View, position: Int, id: Long) {
	    			val die = view.asInstanceOf[DieView].die
	    			currentDiceSet = currentDiceSet.add(die.spec)
	    			bind(currentDiceSet)
	    			createCurrentSelection()
	    		}
	    	})
	    }
	    
	    /**
	     * Assemble the view for the gallery page
	     */
		override def getView(position: Int, convertView : View, parent: ViewGroup): View = {
			val pageView = if (convertView == null || !convertView.isInstanceOf[LinearLayout]) 
					activity.getLayoutInflater().inflate(R.layout.dice_gallery_page, null) 
				else 
					convertView.asInstanceOf[LinearLayout]
			Log.d(TAG, "Creating gallary page " + position)
			val page = getItem(position)
			val nameView = pageView.findViewById(R.id.dice_gallery_page_name).asInstanceOf[TextView]
			nameView.setText(page.name)
			createDiceGrid(pageView, page)
			return pageView
		}
	}
	    
}

