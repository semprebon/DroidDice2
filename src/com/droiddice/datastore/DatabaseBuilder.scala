package com.droiddice.datastore

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

object DatabaseBuilder {
	val DATABASE_NAME = "dice"
    val VERSION = 1
}

class DatabaseBuilder(context: Context)
		extends SQLiteOpenHelper(context, DatabaseBuilder.DATABASE_NAME, null, DatabaseBuilder.VERSION) {

    val TAG = "DatabaseBuilder"
    
    val MIGRATIONS = Map[Int, Migration](
            1 -> new CreateDiceSetTable
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
            val migration = MIGRATIONS(version)
            if (migration != None) {
                Log.d(TAG, "Runnning up migration " + version)
                migration.up(db)
            }
        }
    }
    def onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
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
	}
	
	/************************************************************************/
	
	/* Migration 1 */
	class CreateDiceSetTable extends Migration {
	
		override def up(db: SQLiteDatabase) {
		    db.execSQL("create table dice_sets (" +
		    		"_id integer primary key autoincrement, " +
		    		"name text not null, " +
		    		"spec text not null," +
		    		"value text" +
		    		");")
		}
	
		override def down(db: SQLiteDatabase) {
		    db.execSQL("drop table dice_sets;")
		}
	}
}
