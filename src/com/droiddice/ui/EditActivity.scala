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
  	val DIALOG_SPECIFICATION = 2
  	
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
	
  	override def onCreateOptionsMenu(menu: Menu): Boolean = {
  		val inflater = getMenuInflater()
  		inflater.inflate(R.menu.edit_options_menu, menu)
  		return true
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
  
	override def onOptionsItemSelected(item: MenuItem): Boolean = {
		item.getItemId() match {
			case R.id.specify_menu_item => {
			    showDialog(EditActivity.DIALOG_SPECIFICATION)
			    return true
			}
			case _ => return super.onOptionsItemSelected(item)
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
  		    case EditActivity.DIALOG_SPECIFICATION => {
  		    	val dialog = new Dialog(this)
  		    	dialog.setContentView(R.layout.specification_dialog)
  		    	dialog.setTitle("Enter Specification")

  		    	val specificationEdit = dialog.findViewById(R.id.specification_edit).asInstanceOf[EditText]
  		    	specificationEdit.setText(fragment.currentDiceSet.spec)
  		    	specificationEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
  		    		override def onEditorAction(view: TextView, actionId: Int, event: KeyEvent): Boolean = {
  		    		    try {
  		    		    	val diceSet = new SavedDiceSet(specificationEdit.getText().toString(), fragment.currentDiceSet.id)
			    		    diceSet.customName = fragment.currentDiceSet.customName
			    			fragment.currentDiceSet = new ObservableDiceSet(diceSet)
			    		    fragment.createCurrentSelection()
			    			dialog.dismiss()
  		    		    } catch {
  		    		        case e: InvalidSpecificationException => dialog.dismiss()
  		    		    }
  		    			false
  		    		}})
  		    	dialog
  		    }
  		}
  		return dialog
  	}
  	
}

class EditFragment extends Fragment with FragmentViewFinder with TitleBarHandler {
    
	val DICE_PAGES = Map(
	        "Standard" -> Array("d4", "d6", "d8", "d10", "d12", "d20"),
	        "Exploding" -> Array("s4", "s6", "s8", "s10", "s12", "s20"),
	        "Other" -> Array("dF", "+1", "-1", "dS"))
    val DICE_SPECS = Array("d4", "d6", "d8", "d10", "d12", "d20", "s4", "s6", "s8", "s10", "s20", "dF", "+1", "-1", "dS")
	val DICE = DICE_SPECS.map(DiceSetHelper.dieFactory(_).apply(0))

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

	def addDiceToPage(galleryView: ViewGroup, name: String, dice: Array[String]) {
		val itemViewSize = getActivity().getResources().getDimension(R.dimen.die_view_size).toInt
	    val dicePerRow = 3
	    val rowsPerPage = 2
	    for (row <- 0 until rowsPerPage) {
	    	val rowView = galleryView.getChildAt(row).asInstanceOf[ViewGroup]
	    	for (col <- 0 until dicePerRow) {
	    	    val dieIndex = (row * dicePerRow) + col
	    	    val view: View = if (dieIndex < dice.length) {
	    	    		val dieView = new DieView(getActivity())
	    	    		dieView.die = DiceSetHelper.dieFactory(dice(dieIndex))(0)
	    	    		dieView.preferredSize = itemViewSize
	    	    		Log.d(TAG, "adding die to row:" + dieView.die)
	    	    		dieView
	    	    	} else {
	    	    	    new ImageView(getActivity())
	    	    	}
	    	    rowView.addView(view)
	    	}
	    }
	}
	
	/**
	 * Create the gallery
	 */
	def createDiceGallery() {
		val itemViewSize = getActivity().getResources().getDimension(R.dimen.die_view_size).toInt
 
		Log.d(TAG, "Setting up gallery for " + DICE.map(_.toString()).reduceLeft(_ + "," + _))
		val galleryView = findById[ViewGroup](R.id.dice_gallery)
		val row = galleryView.getChildAt(0).asInstanceOf[ViewGroup]
		DICE_PAGES.foreach((t) => { addDiceToPage(galleryView, t._1, t._2)})
//		DICE.foreach(die => {
//		    val view = new DieView(getActivity())
//		    view.die = die
//		    view.preferredSize = itemViewSize
//		    Log.d(TAG, "adding die to row:" + die)
//		
//		    row.addView(view)
//		})
	    Log.d(TAG, "relaying view:")
		galleryView.requestLayout()
	}

}
