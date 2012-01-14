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
import android.view.GestureDetector.SimpleOnGestureListener
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.content.Intent
import com.droiddice.datastore.DiceSetDataStore

object EditActivity {
    
    /**
     * Create an intent to edit a dice set with this activity
     */
    def intent(activity: Activity, diceSet: DiceSet): Intent = {
    	val intent = new Intent(activity, classOf[EditActivity])
    	if (diceSet != null) {
    		intent.putExtra("Dice", diceSet.spec)
    		intent.putExtra("Name", diceSet.name)
    	}
		return intent
    }
}

class EditActivity extends Activity with TitleBarHandler with ViewFinder {

    lazy val dataStore = new DiceSetDataStore(this)
    
	val TAG = "EditDiceActivity"
 
	val GALLERY_PAGES = Array[GalleryPage](
		new GalleryPage("Standard", Array("d4", "d6", "d8", "d10", "d12", "d20")),
		new GalleryPage("Savage Worlds", Array("s4", "s6", "s8", "s10", "s20")),
		new GalleryPage("Other", Array("dF", "+1", "-1")))
      
	var currentDiceSet: ObservableDiceSet = _
  
	/** Called when the activity is first created. */
	override def onCreate(savedInstanceState: Bundle) {
		Log.d(TAG, "onCreate called")
		super.onCreate(savedInstanceState)

		val extras = getIntent().getExtras()
		currentDiceSet = if (extras != null) 
				new ObservableDiceSet(extras.get("Dice").asInstanceOf[String], extras.get("Name").asInstanceOf[String])
			else
				new ObservableDiceSet("d6", null)
		
		setContentView(R.layout.change_dice_activity)

		bind(currentDiceSet)
		Log.d(TAG, "should have 1 observer: " + currentDiceSet.countObservers)
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
		dataStore.create(currentDiceSet, () => { finish() })
	}
  
	def createBitmapOfView(view: View) : Bitmap = {
		val bitmap = Bitmap.createBitmap(view.getWidth, view.getHeight, Bitmap.Config.ARGB_8888)
		val canvas = new Canvas(bitmap)
		val paint = new Paint()
		view.draw(canvas)
		bitmap
	}
	
	/**
	 * Create view for displaying the current dice set
	 */
	def createCurrentSelection() {
		val selectionView = findById[GridView](R.id.selected_dice)
		selectionView.setAdapter(new DiceViewAdapter(this, currentDiceSet.dice.toArray[Die]))
		selectionView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			override def onItemClick(parent: AdapterView[_], view: View, position: Int, id: Long) {
				currentDiceSet.remove(position)
				Log.d(TAG, "after update, text is" + titleDisplay.getText())
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
		val adapter = new GalleryPageViewAdapter(this, GALLERY_PAGES, galleryView)
		//galleryView.setSpacing(getResources().getDimension(R.dimen.gallery_spacing).toInt)
		galleryView.setAdapter(adapter)
		galleryView.setSelection(1)
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
	class GalleryPageViewAdapter(activity: Activity, pages: Array[GalleryPage], gallery: Gallery) 
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
	    			Log.d(TAG, "adding " + die.spec + " to " + currentDiceSet)
	    			currentDiceSet.add(die.spec)
	    			Log.d(TAG, "now have" + currentDiceSet)
	    			createCurrentSelection()
	    		}
	    	})
	    }
	    
		def pageViewWidth(parent: View) = 
		    4*getResources().getDimension(R.dimen.die_view_size).toInt

		/**
	     * Assemble the view for the gallery page
	     */
		override def getView(position: Int, convertView : View, parent: ViewGroup): View = {
			val pageView = if (convertView == null || !convertView.isInstanceOf[LinearLayout]) 
					activity.getLayoutInflater().inflate(R.layout.dice_gallery_page, gallery, false) 
				else 
					convertView.asInstanceOf[LinearLayout]
			val page = getItem(position)
			val nameView = pageView.findViewById(R.id.dice_gallery_page_name).asInstanceOf[TextView]
			nameView.setText(page.name)
			createDiceGrid(pageView, page)
			return pageView
		}
	}
	    
}

