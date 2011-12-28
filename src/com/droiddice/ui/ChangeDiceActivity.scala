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
	super.onCreate(savedInstanceState)

	val extras = getIntent().getExtras()
	currentDiceSet = if (extras != null) 
		new DiceSet(extras.get("Dice").asInstanceOf[String])
	  else
		new DiceSet("d6")
		
	setContentView(R.layout.change_dice_activity)
	bind(currentDiceSet)
	installTitleHandlers()
	createCurrentSelection()
	createDiceGallery()
  }
  
  /**
   * Save result for original requester (normally RoleDiceActivity)
   */
  override def onBackPressed() {
    val intent = getIntent()
    intent.putExtra("Dice", currentDiceSet.spec)
    setResult(Activity.RESULT_OK, intent)
    Log.d(TAG, "Returning dice:" + currentDiceSet.spec)
    finish()
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
    val galleryView = findById[Gallery](R.id.dice_gallery)
    val adapter = new GalleryPageViewAdapter(this, GALLERY_PAGES)
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
      val page = getItem(position)
      val nameView = pageView.findViewById(R.id.dice_gallery_page_name).asInstanceOf[TextView]
	  nameView.setText(page.name)
	  createDiceGrid(pageView, page)
	  return pageView
	}
  }
    
}