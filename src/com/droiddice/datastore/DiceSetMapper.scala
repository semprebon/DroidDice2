package com.droiddice.datastore
import android.content.ContentValues
import com.droiddice.model.DiceSet
import android.database.Cursor

object DiceSetMapper {

   	def diceSetToValues(diceSet: DiceSet): ContentValues = {
	    val values = new ContentValues()
	    if (diceSet.isNamed) {
	        values.put(DiceSetProvider.NAME, diceSet.name)
	    }
	    values.put(DiceSetProvider.SPEC, diceSet.spec)
	    values.put(DiceSetProvider.VALUES, diceSet.valuesString)
	    return values
	}
	
   	def valuesToDiceSet(values: ContentValues): DiceSet = {
   	    val diceSet = new DiceSet(values.getAsString(DiceSetProvider.SPEC))
   	    diceSet.name = values.getAsString(DiceSetProvider.NAME)
   	    diceSet.valuesString = values.getAsString(DiceSetProvider.VALUES)
   	    return diceSet
   	}

   	val COLUMN_TO_INDEX = DiceSetProvider.PUBLIC_COLUMNS.zipWithIndex.toMap
   	
   	def cursorToDiceSet(cursor: Cursor): DiceSet = {
   	    val spec = cursor.getString(COLUMN_TO_INDEX(DiceSetProvider.SPEC))
   	    val id = cursor.getInt(COLUMN_TO_INDEX(DiceSetProvider._ID))
   	    val diceSet = new SavedDiceSet(spec, id)
   	    diceSet.name = cursor.getString(COLUMN_TO_INDEX(DiceSetProvider.NAME))
   	    diceSet.valuesString = cursor.getString(COLUMN_TO_INDEX(DiceSetProvider.VALUES))
   	    return diceSet
   	}
}

class SavedDiceSet(spec: String, val id: Int) extends DiceSet(spec) {}