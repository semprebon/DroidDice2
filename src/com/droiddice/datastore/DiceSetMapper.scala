package com.droiddice.datastore
import android.content.ContentValues
import com.droiddice.model.DiceSet
import android.database.Cursor
import java.io.Serializable

object DiceSetMapper {

   	def diceSetToValues(diceSet: DiceSet): ContentValues = {
	    val values = new ContentValues()
        values.put(DiceSetProvider.NAME, diceSet.customName)
	    values.put(DiceSetProvider.SPEC, diceSet.spec)
	    values.put(DiceSetProvider.VALUES, diceSet.valuesString)
	    return values
	}
	
   	def valuesToDiceSet(values: ContentValues): DiceSet = {
   	    val diceSet = new DiceSet(values.getAsString(DiceSetProvider.SPEC))
   	    diceSet.customName = values.getAsString(DiceSetProvider.NAME)
   	    diceSet.valuesString = values.getAsString(DiceSetProvider.VALUES)
   	    return diceSet
   	}

   	val COLUMN_TO_INDEX = DiceSetProvider.PUBLIC_COLUMNS.zipWithIndex.toMap
   	
   	def cursorToDiceSet(cursor: Cursor): SavedDiceSet = {
   	    val spec = cursor.getString(COLUMN_TO_INDEX(DiceSetProvider.SPEC))
   	    val id = cursor.getLong(COLUMN_TO_INDEX(DiceSetProvider._ID))
   	    val diceSet = new SavedDiceSet(spec, id)
   	    diceSet.customName = cursor.getString(COLUMN_TO_INDEX(DiceSetProvider.NAME))
   	    diceSet.valuesString = cursor.getString(COLUMN_TO_INDEX(DiceSetProvider.VALUES))
   	    return diceSet
   	}
}

object SavedDiceSet {
    val UNSAVED_ID = -1
}

class SavedDiceSet(spec: String, val id: Long) extends DiceSet(spec) with Serializable {
    def this(diceSet: DiceSet, id: Long) = {
        this(diceSet.spec, id)
        customName = diceSet.customName
        value       
    }
    
    override def toString() = id + ":" + super.toString()
    
    def isSaved = id != SavedDiceSet.UNSAVED_ID
    
    def withIdentity(id: Long) = {
        new SavedDiceSet(this, id)
    }
}