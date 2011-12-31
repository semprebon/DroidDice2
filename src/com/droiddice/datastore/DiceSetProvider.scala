package com.droiddice.datastore

import android.content.Context
import android.content.ContentValues
import android.content.res.Resources
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import android.content.ContentValues
import com.droiddice.model.DiceSet
import android.content.ContentProvider
import android.net.Uri
import android.database.sqlite.SQLiteQueryBuilder
import android.content.UriMatcher

object DiceSetProvider {
    val CONTENT_URI = Uri.parse("content://com.droiddice.dicesetprovider");
    
    val _ID = "_id"
    val NAME = "name"
    val SPEC = "spec"
    val VALUES = "values"
        
    val PUBLIC_COLUMNS = Array(_ID, NAME, SPEC, VALUES)
    val DICE_SET_MIME_SUFFIX = "vnd.droidice.diceset"
    
	val URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH)
	val DICE_SETS = 1
	val DICE_SET_ID = 2
	
	URI_MATCHER.addURI("droiddice", "diceset", DICE_SETS)
	URI_MATCHER.addURI("droiddice", "diceset/#", DICE_SET_ID)

}

class DiceSetProvider extends ContentProvider {
    val TAG = "DiceSetDataStore"

    val DICE_SET_TABLE = "dice_sets"
    val ROWID = "_id"

    var db: SQLiteDatabase = _
    
    val ALL_COLUMNS = Array("name", "spec", "value")

    /**
     * Initialize datanase
     */
    override def onCreate(): Boolean = {
        db = new DatabaseBuilder(getContext()).open()
        return true
    }
     
    /**
     * Run query
     */
    def query(uri: Uri, projection: Array[String], selection: String, selectionArgs: Array[String] , 
            sortOrder: String): Cursor = {
        val builder = new SQLiteQueryBuilder()
        builder.setTables(DICE_SET_TABLE)

        // If the query ends in a specific record number, we're
        // being asked for a specific record, so set the
        // WHERE clause in our query.
        if (DiceSetProvider.URI_MATCHER.`match`(uri) == DiceSetProvider.DICE_SET_ID) {
            builder.appendWhere("_id=" + uri.getLastPathSegment());
        }

        // Make the query.
        val cursor = builder.query(db, projection, selection, selectionArgs, null, null, sortOrder)
        cursor.setNotificationUri(getContext().getContentResolver(), uri)
        return cursor
    }
    
    /**
     * Stores a dice set. 
     */
    def insert(uri: Uri, values: ContentValues): Uri = {
	    Log.d(TAG, "inserting DiceSet " + values.getAsString(DiceSetProvider.NAME))
        val id = db.insertOrThrow(DICE_SET_TABLE, null, values)
        return Uri.withAppendedPath(DiceSetProvider.CONTENT_URI, id.toString())
    }

    /**
     * Update the dice set
     */
    def update(uri: Uri, values: ContentValues, selection: String, selectionArgs: Array[String]): Int = {
    	val id = uri.getLastPathSegment()
	    Log.d(TAG, "updatting DiceSet " + id)
        db.update(DICE_SET_TABLE, values, selection, selectionArgs)
    }
    
    def delete(uri: Uri, selection: String, selectionArgs: Array[String]): Int = {
        db.delete(DICE_SET_TABLE, selection, selectionArgs)
    }

    def getType(uri: Uri): String = {
        val uriType = DiceSetProvider.URI_MATCHER.`match`(uri)
        return if (uriType == DiceSetProvider.DICE_SETS) "vnd.android.cursor.dir/" + DiceSetProvider.DICE_SET_MIME_SUFFIX
        	else "vnd.android.cursor.item/"	+ DiceSetProvider.DICE_SET_MIME_SUFFIX 
    }
    
    /**
     * Delete the dice set with the given rowId
     * 
     * @param rowId id of note to delete
     * @return true if deleted, false otherwise
     */
    def deleteDiceSet(name: String) {
        db.delete(DICE_SET_TABLE, "name=?", Array(name))
    }

    def findByName(name: String): DiceSet = {
        val cursor = db.query(true, DICE_SET_TABLE, 
                ALL_COLUMNS,
                "name=?", Array(name),
                null, null, null, null)
        if (cursor != null && cursor.moveToFirst()) {
        	return diceSetAt(cursor)
        } else {
            return null
        }
    }

	def stringValue(cursor: Cursor, column: String) = cursor.getString(cursor.getColumnIndexOrThrow(column))
	
	/**
	 * Create a diceSet object for the cursor's current row
	 */
	def diceSetAt(cursor: Cursor): DiceSet = {
	    val diceSet = new DiceSet(stringValue(cursor, "spec"));
    	diceSet.name = stringValue(cursor, "name")
		return diceSet
	}
	
    /**
     * Return a Cursor over the list of all dice sets in the database
     * 
     * @return Cursor over all dice sets
     */
    def fetchAll(): Cursor = {
        return db.query(DICE_SET_TABLE, ALL_COLUMNS, null, null, null, null, null);
    }

    /**
     * Count rows
     */
    def count(): Int = {
        val cursor =  db.rawQuery("select count(*) from " + DICE_SET_TABLE, null)
        val count = if (cursor != null) {
        		cursor.moveToFirst()
        		cursor.getInt(0)
        	} else {
        	    0
        	}
        cursor.close()
        return count
    }

}
