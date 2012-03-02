package com.droiddice.datastore

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import android.database.Cursor
import android.content.ContentValues

object DatabaseBuilder {
	val DATABASE_NAME = "data"
    val VERSION = 6
}

class DatabaseBuilder(context: Context)
		extends SQLiteOpenHelper(context, DatabaseBuilder.DATABASE_NAME, null, DatabaseBuilder.VERSION) {

    val TAG = "DatabaseBuilder"
    
    val MIGRATIONS = Map[Int, Migration](
            6 -> new CreateDiceSetTable
    	)
    	
    def open(): SQLiteDatabase = getWritableDatabase()
    
    /**
     * Create new database by migrating up from 0th version
     */
    override def onCreate(db: SQLiteDatabase) {
        onUpgrade(db, 0, DatabaseBuilder.VERSION)
    }
    
    override def onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        for (version <- (oldVersion+1) to newVersion) {
            MIGRATIONS.get(version).foreach {
            	Log.d(TAG, "Runnning up migration " + version)
            	_.up(db)
            }
        }
    }

    override def onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        for (version <- oldVersion to (newVersion+1) by -1) {
            val migration = MIGRATIONS(version)
            if (migration != None) {
                Log.d(TAG, "Runnning down migration " + version)
                migration.down(db)
            }
        }
    }

	abstract class Migration {
	    def up(db: SQLiteDatabase)
	    def down(db: SQLiteDatabase)
	    
	    def eachRow(cursor: Cursor, visitor: (Cursor) => Any) {
	        cursor.moveToFirst()
	        while (!cursor.isAfterLast()) {
	            visitor.apply(cursor)
	            cursor.moveToNext()
		    }
	        cursor.close()
	    }
	    
	}
	
	/************************************************************************/

	/* Migration 6 */
	class CreateDiceSetTable extends Migration {
	    var db: SQLiteDatabase = _
	    
	    def tableHasColumn(db: SQLiteDatabase, table: String, column: String): Boolean = {
	        val cursor = db.rawQuery("pragma table_info (dice_sets)",null)
	        cursor.moveToFirst()
	        var found = false

	        val checkForColumn = (cursor: Cursor) => {
	        	if (cursor.getString(1).equals(column)) {
	        		found = true
	        	}
	        }
	        eachRow(cursor, checkForColumn)
	        
	        return found
	    }
	    
	    def isOldVersion(db: SQLiteDatabase): Boolean = tableHasColumn(db, "dice_sets", "dice")
	        
	    def convertSpec(old: String): String = {
	        old.replaceAll("d100", "p10:10+p10:1")
	    }
	    
	    def uniqueName(name: String) = uniqueNameWithSuffix(name, 0)
	    
	    def uniqueNameWithSuffix(name: String, suffix: Int) : String = {
	        val newName = if (suffix == 0) name else name + suffix
	    	val cursor = db.rawQuery("select name from dice_Sets where name=?", Array(newName))
	    	val found = cursor.getCount() > 0
	    	cursor.close()
	    	return if (found) uniqueNameWithSuffix(name, suffix+1) else newName 
	    }
	    
    	val insertNewRow = (cursor: Cursor) => {
    		val data = new ContentValues()
    		data.put("_id", cursor.getInt(0).asInstanceOf[java.lang.Integer])
    		data.put("name", uniqueName(cursor.getString(1)))
    		data.put("spec", convertSpec(cursor.getString(2)))
    		data.putNull("value")
    		Log.d(TAG, "inserting new dice set " + data.getAsString("name") + ":" + data.getAsString("spec"))
		    val rowId = db.insert("dice_sets", null, data)
		    Log.d(TAG, "Inserted " + rowId)
    	}

    	override def up(db: SQLiteDatabase) {
    		this.db = db
    		val updateOld = isOldVersion(db) 
		    if (updateOld) {
		        Log.d(TAG, "renaming old table")
		    	db.execSQL("alter table dice_sets rename to old_dice_sets")
		    }
		    
		    Log.d(TAG, "creating dice_sets")
	    	db.execSQL("create table dice_sets (" +
	    		"_id integer primary key autoincrement, " +
	    		"name text, " +
	    		"spec text not null," +
	    		"value text" +
	    		")")
		    db.execSQL("create unique index name_index on dice_sets (name)")
		    db.execSQL("create index spec_index on dice_sets (spec)")
		    		
		    if (updateOld) {
		    	Log.d(TAG, "copying old dice to new table")
		    	val cursor = db.rawQuery("select _id, name, dice from old_dice_Sets", Array[String]())
		    	eachRow(cursor, insertNewRow)
		    	Log.d(TAG, "dropping old table")
		    	db.execSQL("drop table old_dice_sets")
		    }
		    Log.d(TAG, "up migration complete")	
    	}
    	
    	override def down(db: SQLiteDatabase) {
    	    
    	}
	}
	
}

