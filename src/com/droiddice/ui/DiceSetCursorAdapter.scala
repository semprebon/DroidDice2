package com.droiddice.ui

import android.content.Context
import android.view._
import android.widget._
import android.database.Cursor
import android.support.v4.widget.SimpleCursorAdapter
import android.util.Log
import com.droiddice.datastore._
import com.droiddice.model._
import com.droiddice._
import android.graphics.drawable._

class DiceSetCursorAdapter(context: Context) extends SimpleCursorAdapter(
            context, R.layout.list_dice_set_item, null, DiceSetProvider.PUBLIC_COLUMNS, 
            Array(R.id.dice_set_item_text), 0) {

    val TAG = "DiceSetCursorAdapter"
    val size = context.getResources.getDimension(R.dimen.list_view_die_size).toInt
	val generator = new DiceSetBitmapGenerator(context, size)
        
    def populateView(layoutView: ViewGroup, diceSet: DiceSet) {
		Log.d(TAG, "binding cursor to view")
		
		layoutView.setTag(diceSet)

		val nameView = layoutView.getChildAt(0).asInstanceOf[TextView]
		nameView.setText(diceSet.name)
            
		val imageView = layoutView.getChildAt(1).asInstanceOf[ImageView]
		imageView.setImageBitmap(generator.generate(diceSet))
    }
    
	override def newView(context: Context, cursor: Cursor, parent: ViewGroup): View = {
		val c = getCursor()
		val inflater = LayoutInflater.from(context)
		val layoutView = inflater.inflate(R.layout.list_dice_set_item, parent, false).asInstanceOf[ViewGroup]
		val diceSet = DiceSetMapper.cursorToDiceSet(cursor)
		populateView(layoutView, diceSet)
        return layoutView
	}
	
    override def bindView(view: View, context: Context, cursor: Cursor) {
		val layoutView = view.asInstanceOf[ViewGroup]
		val diceSet = DiceSetMapper.cursorToDiceSet(cursor)
		populateView(layoutView, diceSet)
	}
}