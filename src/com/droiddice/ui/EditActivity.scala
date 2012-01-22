package com.droiddice.ui

import android.view._
import android.widget._
import android.app.Activity
import android.os.Bundle
import android.content.Context
import android.util.Log
import android.view.View.OnTouchListener
import android.view.animation.AnimationUtils
import android.os.AsyncTask
import android.net.Uri
import android.content.ContentValues
import android.app.ProgressDialog
import android.view.GestureDetector.SimpleOnGestureListener
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.content.Intent
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity

import com.droiddice._
import com.droiddice.model._
import com.droiddice.datastore.DiceSetProvider
import com.droiddice.datastore.DiceSetMapper
import com.droiddice.datastore.DiceSetDataStore
import com.droiddice.datastore.SavedDiceSet

object EditActivity {
    
  	val DIALOG_NAME_EXISTS = 1
  	
    /**
     * Create an intent to edit a dice set with this activity
     */
    def intent(activity: Activity, diceSet: ObservableDiceSet): Intent = {
    	val intent = new Intent(activity, classOf[EditActivity])
    	if (diceSet != null) {
    		ObservableDiceSet.saveTo(intent, diceSet)
    	}
		return intent
    }
    
    
}

class EditActivity extends FragmentActivity {

    lazy val dataStore = new DiceSetDataStore(this)
    
    lazy val fragment = getSupportFragmentManager.findFragmentById(R.id.edit_fragment).asInstanceOf[EditFragment]
    
	private val TAG = "EditDiceActivity"
 
	override def onCreate(savedInstanceState: Bundle) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.edit_activity)
	}
	
  	def errorDialog(message: String) {
		    val builder = new AlertDialog.Builder(this)
		    builder.setMessage(message)
		    	.setCancelable(false)
		    	.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		    		def onClick(dialog: DialogInterface, id: Int) { dialog.cancel() }
		    	})
		    val alert = builder.create()
		    alert.show()
    }
    
  	def cancelDialog(message: String) {
		    val builder = new AlertDialog.Builder(this)
		    builder.setMessage(message)
		    	.setCancelable(false)
		    	.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		    		def onClick(dialog: DialogInterface, id: Int) { dialog.cancel() }
		    	})
		    val alert = builder.create()
		    alert.show()
    }

  	/**
     * Save result for original requester (normally RoleDiceActivity)
     */
	override def onBackPressed() {
		val intent = getIntent()
		Log.d(TAG, "onBackPressed:" + fragment.currentDiceSet)
		ObservableDiceSet.saveTo(intent, fragment.currentDiceSet)
		setResult(Activity.RESULT_OK, intent)
		
		Log.d(TAG, "Executing async task")
		val after = (error: Throwable) => { 
			    	if (error == null) finish() else {
			    	    Log.e(TAG, "Save diceeset failed", error)
			    	    errorDialog("Failed to save dice set") 
			    	}
				}
		if (fragment.currentDiceSet.isSaved) {
		    Log.d(TAG, "saving existing diceset " + fragment.currentDiceSet)
			dataStore.update(fragment.currentDiceSet, after)
		} else {
			dataStore.create(fragment.currentDiceSet, after)
		}
	}
  
  	override def onCreateDialog(id: Int): Dialog = {
  	    val dialog = id match {
  		    case EditActivity.DIALOG_NAME_EXISTS => {
  		    	new AlertDialog.Builder(this).setMessage("Name aleady used. Overwrite?")
  		    		.setCancelable(false)
  		    		.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
  		    				def onClick(dialog: DialogInterface, id: Int) { 
  		    				    fragment.currentDiceSet = new ObservableDiceSet(
  		    				            new SavedDiceSet(fragment.currentDiceSet, fragment.duplicateDiceSet.id))
  		    				    fragment.updateName()
  		    				}
  		    			})
  		    		.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
  		    				def onClick(dialog: DialogInterface, id: Int) { dialog.cancel() }
  		    			})
  		    		.create()
  		    }
  		}
  		return dialog
  	}
  	
}

class EditFragment extends Fragment with FragmentViewFinder with TitleBarHandler {
    
	val GALLERY_PAGES = Array[GalleryPage](
		new GalleryPage("Standard", Array("d4", "d6", "d8", "d10", "d12", "d20")),
		new GalleryPage("Savage Worlds", Array("s4", "s6", "s8", "s10", "s20")),
		new GalleryPage("Other", Array("dF", "+1", "-1")))
      
	var currentDiceSet: ObservableDiceSet = _
	var duplicateDiceSet: SavedDiceSet = _
 
	private val TAG = "EditFragment"
 
	def showBundleKeys(desc: String, bundle: Bundle) {
        if (bundle != null) {
            if (bundle.keySet() != null) {
                if (bundle.keySet().toArray() != null) {
                	bundle.keySet().toArray().foreach(t => {
                		Log.d(TAG, desc + " Key:" + t + " =>" + bundle.get(t.toString()))
                	})
                }
            }
        }
    }
    
	/** Called when the activity is first created. */
	override def onActivityCreated(savedInstanceState: Bundle) {
		super.onActivityCreated(savedInstanceState)
		restoreInstanceState(savedInstanceState)
		
		currentDiceSet = ObservableDiceSet.fetchFrom(getActivity().getIntent())
		if (currentDiceSet == null) currentDiceSet = new ObservableDiceSet("d6", null)
		
		bind(currentDiceSet)
		Log.d(TAG, "should have 1 observer: " + currentDiceSet.countObservers)
		installTitleHandlers()
		createCurrentSelection()
		Log.d(TAG, "calling createDiceGallery")
		createDiceGallery()
	}
  
	override def onCreateView(inflater: LayoutInflater, container: ViewGroup, state: Bundle): View = {
		inflater.inflate(R.layout.edit_fragment, container)
	}
	
	/**
	 * Save current dice set
	 */
	override def onSaveInstanceState(state: Bundle) {
	    super.onSaveInstanceState(state)
	    val bundle = currentDiceSet.toBundle()
	    state.putBundle("diceSet", bundle)
	}
	
	/**
	 * Restore current dice set
	 */
	def restoreInstanceState(state: Bundle) {
	    if (state == null) return
	    ObservableDiceSet.withDiceSetFrom(state.getBundle("diceSet"), currentDiceSet = _)
		createCurrentSelection()
	}
	
	/**
  	 * Install UI event handlers
  	 * 
  	 * There are basically two events:
  	 * * when edit button is clicked, switch to edit view and enter edit mode
  	 * * when user done editing, update object and switch back to display view
  	 */
  	def installTitleHandlers() {
  	    editButton.setVisibility(View.VISIBLE)
  		titleView.setInAnimation(AnimationUtils.loadAnimation(getActivity().getApplication(), R.anim.grow_from_center))
  		titleView.setOutAnimation(AnimationUtils.loadAnimation(getActivity().getApplication(), R.anim.shrink_to_center))
  		titleView.setDisplayedChild(0)
  		
  		// only will trigger it if no physical keyboard is open
  		editButton.setOnClickListener(new View.OnClickListener() {
  			override def onClick(view: View) {
  			    titleEdit.setText(diceSet.name)
  				titleView.setDisplayedChild(1)
  				titleView.post(new Runnable { def run { titleEdit.requestFocusFromTouch } })
  			}
  		})
    
  		titleEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
  			override def onEditorAction(view: TextView, actionId: Int, event: KeyEvent): Boolean = {
  			    val name = titleEdit.getText().toString()
  			    val dataStore = getActivity.asInstanceOf[EditActivity].dataStore
  			    dataStore.fetchByName(name, (existing: SavedDiceSet) => {
  			        duplicateDiceSet = existing
  			        if (existing != null) getActivity().showDialog(EditActivity.DIALOG_NAME_EXISTS) else updateName()
  			    })
  			    false
  			}
  		})
  	}

  	def updateName() {
       	currentDiceSet.name = titleEdit.getText().toString()
       	updateTitleBar(currentDiceSet)
       	titleView.setDisplayedChild(0)
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
		selectionView.setAdapter(new DiceViewAdapter(getActivity(), currentDiceSet.dice.toArray[Die]))
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
		val adapter = new GalleryPageViewAdapter(getActivity(), GALLERY_PAGES, galleryView)
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
