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
    val AUTHORITY = "com.droiddice.dicesetprovider"
    
    val _ID = "_id"
    val NAME = "name"
    val SPEC = "spec"
    val VALUES = "value"
        
    val PUBLIC_COLUMNS = Array(_ID, NAME, SPEC, VALUES)
    val DICE_SET_MIME_SUFFIX = "vnd.droidice.diceset"
    
	val URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH)
	val DICE_SETS = 1
	val DICE_SET_ID = 2
	
	URI_MATCHER.addURI(AUTHORITY, "diceset/*", DICE_SET_ID)
	URI_MATCHER.addURI(AUTHORITY, "dicesets", DICE_SETS)

	def uriFor(id: Long) = Uri.withAppendedPath(DiceSetProvider.CONTENT_URI, "diceset/" + id.toString())
    def uriFor(diceSet: SavedDiceSet): Uri = uriFor(diceSet.id)
}

class DiceSetProvider extends ContentProvider {
    val TAG = "DiceSetDataStore"
    val _ID = DiceSetProvider._ID
    val NAME = DiceSetProvider.NAME
    val SPEC = DiceSetProvider.SPEC
    val VALUES = DiceSetProvider.VALUES

    val DICE_SET_TABLE = "dice_sets"
    
    var db: SQLiteDatabase = _
    
    val ALL_COLUMNS = Array(_ID, NAME, SPEC, VALUES)

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
            builder.appendWhere(_ID + "=" + uri.getLastPathSegment());
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
	    if (values.getAsString(NAME) == null) {
	        val diceSet = findAnonymous(values.getAsString(SPEC))
	        if (diceSet != null) {
	            val uri = DiceSetProvider.uriFor(diceSet)
	            update(uri, values, null, null)
	            return uri
	        }
	    }
        val id = db.insertOrThrow(DICE_SET_TABLE, null, values)
        getContext().getContentResolver().notifyChange(uri, null)
        return DiceSetProvider.uriFor(id)
    }

    def addIdToSelection(selection: String, args: Array[String], uri: Uri): 
    		(String, Array[String]) = {
        Log.d(TAG, "Uri=" + uri)
        if (DiceSetProvider.URI_MATCHER.`match`(uri) == DiceSetProvider.DICE_SET_ID) {
            Log.d(TAG, "Using selction with id " + uri.getLastPathSegment())
            return (_ID + "=?", Array(uri.getLastPathSegment()))
        } else {
            Log.d(TAG, "Using selction unchanged")
            return (selection, args)
    	}
    }
    
    /**
     * Update the dice set
     */
    def update(uri: Uri, values: ContentValues, 
            originalSelection: String, originalArgs: Array[String]): Int = {
        val (selection, args) = addIdToSelection(originalSelection, originalArgs, uri)
        val count = db.update(DICE_SET_TABLE, values, selection, args)
        Log.d(TAG, "updated " + count + " dicesets")
        getContext().getContentResolver().notifyChange(uri, null)
        return count
    }
    
    /**
     * Delete a dice set
     */
    def delete(uri: Uri, originalSelection: String, originalArgs: Array[String]): Int = {
        val (selection, args) = addIdToSelection(originalSelection, originalArgs, uri)
        Log.d(TAG, "deleting " + selection + " " + args)
        args.foreach(arg => Log.d(TAG, "arg=" + arg))
        val count = db.delete(DICE_SET_TABLE, selection, args)
        Log.d(TAG, "deleted " + count + " dicesets")
        getContext().getContentResolver().notifyChange(uri, null)
        return count
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
        Log.d(TAG, "deleted " + name)
    }

    def findByName(name: String): SavedDiceSet = {
        val cursor = db.query(true, DICE_SET_TABLE, 
                ALL_COLUMNS,
                "name=?", Array(name),
                null, null, null, null)
        val diceSet = if (cursor != null && cursor.moveToFirst()) diceSetAt(cursor) else null
        cursor.close()
        return diceSet
    }

    def findAnonymous(spec: String): SavedDiceSet = {
        val cursor = db.query(true, DICE_SET_TABLE, 
                ALL_COLUMNS,
                "spec=? and name is null", Array(spec),
                null, null, null, null)
        val diceSet = if (cursor != null && cursor.moveToFirst()) diceSetAt(cursor) else null
        cursor.close()
        return diceSet
    }

	def stringValue(cursor: Cursor, column: String) = cursor.getString(cursor.getColumnIndexOrThrow(column))
	def intValue(cursor: Cursor, column: String) = cursor.getInt(cursor.getColumnIndexOrThrow(column))
	
	/**
	 * Create a diceSet object for the cursor's current row
	 */
	def diceSetAt(cursor: Cursor): SavedDiceSet = {
	    val diceSet = new SavedDiceSet(stringValue(cursor, "spec"), intValue(cursor, "_id"));
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
