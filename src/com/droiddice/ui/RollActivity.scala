package com.droiddice.ui

import scala.collection.JavaConversions._
import scala.collection.mutable.ListBuffer
import android.app.Activity
import android.os.Bundle
import android.widget._
import android.view.ViewGroup
import android.view.View
import android.view.animation.AnimationUtils
import android.content.Context
import android.util.Log
import android.content.Intent
import com.droiddice._
import com.droiddice.model._
import android.view.View.OnClickListener
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.view.LayoutInflater
import com.droiddice.datastore.SavedDiceSet
import android.view.MenuItem
import android.view.Menu
import android.app.Dialog
import android.app.AlertDialog
import android.content.DialogInterface
import android.view.KeyEvent
import com.droiddice.datastore.DiceSetDataStore

class RollActivity extends FragmentActivity with FragmentActivityViewFinder {

  	val DIALOG_NAME_EXISTS = 1
  	val DIALOG_SPECIFICATION = 2
  	val DIALOG_SAVE = 3

  	private var currentDiceSet = new ObservableDiceSet("2d6", null)
	var duplicateDiceSet: SavedDiceSet = _

	lazy val rollFragment = findFragmentById[RollFragment](R.id.roll_fragment)
	lazy val pickViewAlwaysOn = !(rollFragment.getView().getParent().isInstanceOf[ViewAnimator])
	lazy val saveInteraction = new SaveInteraction(this)
	
    lazy val dataStore = new DiceSetDataStore(this)
    
	val TAG = "RollDiceActivity"
 
	def diceSet: ObservableDiceSet = { currentDiceSet }
  	
  	def diceSet_=(newDiceSet: ObservableDiceSet) {
	    Log.d(TAG, "changing activity dice set from " + currentDiceSet + " to " + newDiceSet)
		currentDiceSet = newDiceSet
	    rollFragment.bind(currentDiceSet)
	    rollFragment.updateResult()
  	}
  	
	/** Called when the activity is first created. */
	override def onCreate(savedInstanceState: Bundle) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.roll_activity)
		
        // Create the list fragment and add it as content for picklist.
        if (getSupportFragmentManager().findFragmentById(R.id.pick_fragment) == null) {
            val list = new PickFragment()
            getSupportFragmentManager().beginTransaction().add(R.id.pick_fragment, list).commit()
        }
	}
	
  	override def onCreateOptionsMenu(menu: Menu): Boolean = {
  		val inflater = getMenuInflater()
  		inflater.inflate(R.menu.edit_options_menu, menu)
  		return true
  	}    

	def changeDiceSet(newDiceSet: ObservableDiceSet) {
  		diceSet = newDiceSet
	}

	override def onOptionsItemSelected(item: MenuItem): Boolean = {
		item.getItemId() match {
			case R.id.specify_menu_item => {
			    showDialog(DIALOG_SPECIFICATION)
			    return true
			}
			case R.id.save_menu_item => {
			    showDialog(DIALOG_SAVE)
			    return true
			}
			case _ => return super.onOptionsItemSelected(item)
		}
	}
	
	/**
	 * Ensure that a given fragment's view is on screen
	 */
	def showFragmentView(index: Int) {
	    rollFragment.getView().getParent() match {
	        case f: ViewAnimator => f.setDisplayedChild(index)
	        case _ =>
	    }
	}
	
	def showPickView() = showFragmentView(1)
	def showRollView() = showFragmentView(0)

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

	override def onCreateDialog(id: Int): Dialog = {
  	    val dialog = id match {
  		    case DIALOG_SAVE => {
  		        saveInteraction.dialog()
  		    }
  		    case DIALOG_SPECIFICATION => {
  		    	val dialog = new Dialog(this)
  		    	dialog.setContentView(R.layout.specification_dialog)
  		    	dialog.setTitle("Enter Specification")

  		    	dialog
  		    }
  		}
  		return dialog
  	}
	
	override def onPrepareDialog(id: Int, dialog: Dialog) {
	    id match {
	        case DIALOG_SAVE => {
	            saveInteraction.prepare(dialog)
	        }
	        case DIALOG_SPECIFICATION => {
  		    	val specificationEdit = dialog.findViewById(R.id.specification_edit).asInstanceOf[EditText]
  		    	specificationEdit.setText(diceSet.spec)
  		    	specificationEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
  		    		override def onEditorAction(view: TextView, actionId: Int, event: KeyEvent): Boolean = {
  		    		    try {
  		    		    	val newDiceSet = new SavedDiceSet(specificationEdit.getText().toString(), diceSet.id)
			    		    newDiceSet.customName = diceSet.customName
			    			diceSet = new ObservableDiceSet(newDiceSet)
			    		    dialog.dismiss()
  		    		    } catch {
  		    		        case e: InvalidSpecificationException => dialog.dismiss()
  		    		    }
  		    			false
  		    		}})
	        }
	    }
	}
}

class RollFragment extends Fragment with FragmentViewFinder with TitleBarHandler {
    
	val TAG = "RollDiceActivity"	
 
	val NEW_DICE_RESULT = 0
  
	lazy val resultTextView = findById[TextView](R.id.dice_result_text)
	lazy val diceLayout = findById[AdapterView[DiceViewAdapter]](R.id.dice_layout)
	lazy val activity = getActivity().asInstanceOf[RollActivity]
	
	def currentDiceSet() = {
		activity.diceSet
	}
	
	val DICE_PAGES = Map(
	        "Standard" -> Array("d4", "d6", "d8", "d10", "d12", "d20"),
	        "Exploding" -> Array("s4", "s6", "s8", "s10", "s12", "s20"),
	        "Other" -> Array("dF", "+1", "-1", "dS"))
    val DICE_SPECS = Array("d4", "d6", "d8", "d10", "d12", "d20", "s4", "s6", "s8", "s10", "s20", "dF", "+1", "-1", "dS")
	val DICE = DICE_SPECS.map(DiceSetHelper.dieFactory(_).apply(0))

	var duplicateDiceSet: SavedDiceSet = _

	/** Called when the activity is first created. */
	override def onActivityCreated(savedInstanceState: Bundle) {
		super.onActivityCreated(savedInstanceState)
		restoreInstanceState(savedInstanceState)
		installRollActionHandler()
		installPickDicesetButtonHandler()
		installSaveDicesetButtonHandler()
		installNewDicesetButtonHandler()
		bind(activity.diceSet)
		createCurrentSelection()
		updateResult()
		createDiceGallery()
	}
	

  	def updateName() {
       	//currentDiceSet.name = titleEdit.getText().toString()
       	updateTitleBar(activity.diceSet)
       	titleView.setDisplayedChild(0)
  	}

  	override def onCreateView(inflater: LayoutInflater, container: ViewGroup, state: Bundle): View = {
		val view = inflater.inflate(R.layout.roll_fragment, container)
		if (container.isInstanceOf[LinearLayout]) {
			findById[Button](R.id.dice_sets_selection_button).setVisibility(View.GONE)
		}
		view
	}
	
	/** 
	 * Save current and recent dice sets
	 */
	 override def onSaveInstanceState(state: Bundle) {
	    Log.d(TAG, "onSaveInstanceState:" + activity.diceSet)
		state.putBundle("diceSet", activity.diceSet.toBundle)
		super.onSaveInstanceState(state)
	}
	
	def updateResult() {
		resultTextView.setText(activity.diceSet.display.replace(" ", "    "))
    	val layout = getActivity().findViewById(R.id.dice_layout)
    	val dice: Array[Die] = activity.diceSet.dice.toArray
    	val adapter = new DiceViewAdapter(getActivity().getApplication(), dice)
		Log.d(TAG, "adapter has " + adapter.getCount() + "  dice has " + dice.length)
    	diceLayout.setAdapter(adapter)
	}

	/**
	 * Restore current and recent dice sets
	 */
	def restoreInstanceState(state: Bundle) {
	    if (state == null) return
	    val bundle = state.getBundle("diceSet")
	    ObservableDiceSet.withDiceSetFrom(state.getBundle("diceSet"), activity.diceSet = _)
	    Log.d(TAG, "onRestoreInstanceState:" + activity.diceSet)
	}
	
	override def onActivityResult(requestCode: Int, resultCode: Int, intent: Intent) {
		super.onActivityResult(requestCode, resultCode, intent)
		if (requestCode == NEW_DICE_RESULT && resultCode == Activity.RESULT_OK) {
		    val diceSet = ObservableDiceSet.fetchFrom(intent)
		    Log.d(TAG, "got " + diceSet + "from other")
			activity.changeDiceSet(diceSet)
		}
	}
  
	private def installRollActionHandler() {
	    val result = findById[View](R.id.dice_result_text)
		result.setOnClickListener(new View.OnClickListener() {
			override def onClick(view: View)  {
				activity.diceSet.roll()
				updateResult()
			}
		})
		result.setOnFocusChangeListener(new View.OnFocusChangeListener() {
		    val unfocusColor = getResources().getColor(R.color.transparent)
		    val focusColor = getResources().getColor(R.color.focus_color)
			override def onFocusChange(view: View, hasFocus: Boolean)  {
				view.setBackgroundColor(if (hasFocus) focusColor else unfocusColor)
			}
		})
	}
  
	private def installPickDicesetButtonHandler() {
		findById[Button](R.id.dice_sets_selection_button).setOnClickListener(new View.OnClickListener() {
			override def onClick(view: View)  {
			    activity.showPickView()
			}
		})
	}
  
	private def installSaveDicesetButtonHandler() {
		findById[Button](R.id.dice_set_save_button).setOnClickListener(new View.OnClickListener() {
			override def onClick(view: View)  {
				Log.d(TAG, "at click " + activity.diceSet)
				activity.showDialog(activity.DIALOG_SAVE)
			}
		})
	}

	private def installNewDicesetButtonHandler() {
		findById[Button](R.id.dice_set_new_button).setOnClickListener(new View.OnClickListener() {
			override def onClick(view: View)  {
				activity.diceSet = new ObservableDiceSet("2d6", null)
			}
		})
	}

	/**
	 * Create view for displaying the current dice set
	 */
	def createCurrentSelection() {
		diceLayout.setAdapter(new DiceViewAdapter(getActivity(), activity.diceSet.dice.toArray[Die]))
		diceLayout.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			override def onItemClick(parent: AdapterView[_], view: View, position: Int, id: Long) {
				activity.diceSet.remove(position)
   		        activity.changeDiceSet(activity.diceSet)
				createCurrentSelection()
			}
		})
	}

	/**
	 * Add dice to gallery view
	 */
	def addDiceToPage(galleryView: ViewGroup, name: String, dice: Array[String]) {
		val itemViewSize = getActivity().getResources().getDimension(R.dimen.die_view_size).toInt
	    val galleryHeight = getActivity().getResources().getDimension(R.dimen.gallery_height).toInt
	    val rowsPerPage = Math.min(galleryHeight / itemViewSize, 1)
	    val dicePerRow = 6 / rowsPerPage
	    for (row <- 0 until rowsPerPage) {
	    	val rowView = galleryView.getChildAt(row).asInstanceOf[ViewGroup]
	    	for (col <- 0 until dicePerRow) {
	    	    val dieIndex = (row * dicePerRow) + col
	    	    val view: View = if (dieIndex < dice.length) {
	    	    		val dieView = new DieView(getActivity())
	    	    		dieView.die = DiceSetHelper.dieFactory(dice(dieIndex))(0)
	    	    		dieView.preferredSize = itemViewSize
	    	    		dieView.setFocusable(true)
	    	    		Log.d(TAG, "adding die to row:" + dieView.die)
	    	    		dieView.setOnClickListener(new View.OnClickListener() {
	    	    		    def onClick(view: View) {
	    	    		        val die = view.asInstanceOf[DieView].die
	    	    		        activity.diceSet.add(die.spec)
	    	    		        activity.changeDiceSet(activity.diceSet)
	    	    		        createCurrentSelection()
	    	    		    }
	    	    		})
	    	    		dieView
	    	    	} else {
	    	    	    new ImageView(getActivity())
	    	    	}
	    	    Log.d(TAG, "adding focasable gallery die?" + view.isFocusable())
	    	    rowView.addView(view)
	    	}
	    }
		
	}
	
	/**
	 * Create the gallery view
	 */
	def createDiceGallery() {
		val itemViewSize = getActivity().getResources().getDimension(R.dimen.die_view_size).toInt
 
		Log.d(TAG, "Setting up gallery for " + DICE.map(_.toString()).reduceLeft(_ + "," + _))
		val galleryView = findById[ViewGroup](R.id.dice_gallery)
		val row = galleryView.getChildAt(0).asInstanceOf[ViewGroup]
		DICE_PAGES.foreach((t) => { addDiceToPage(galleryView, t._1, t._2)})
	    Log.d(TAG, "relaying view:")
		galleryView.requestLayout()
	}


	
}