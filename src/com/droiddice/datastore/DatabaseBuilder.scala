package com.droiddice.datastore

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

object DatabaseBuilder {
	val DATABASE_NAME = "dice"
    val VERSION = 3
}

class DatabaseBuilder(context: Context)
		extends SQLiteOpenHelper(context, DatabaseBuilder.DATABASE_NAME, null, DatabaseBuilder.VERSION) {

    val TAG = "DatabaseBuilder"
    
    val MIGRATIONS = Map[Int, Migration](
            1 -> new CreateDiceSetTable,
            2 -> new CreateNameIndex,
            3 -> new makeNameNullable
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

	/* Migration 2 */
	class CreateNameIndex extends Migration {
	
		override def up(db: SQLiteDatabase) {
		    db.execSQL("delete from dice_sets;")
		    db.execSQL("create unique index name_index on dice_sets (name)")
		}
	
		override def down(db: SQLiteDatabase) {
		    db.execSQL("drop index name_index")
		}
	}

	/* Migration 3 */
	class makeNameNullable extends Migration {
	
		override def up(db: SQLiteDatabase) {
		    db.execSQL("create table new (" +
		    		"_id integer primary key autoincrement, " +
		    		"name text, " +
		    		"spec text not null," +
		    		"value text" +
		    		")")
		    db.execSQL("insert into new select * from dice_sets")
		    db.execSQL("drop table dice_sets")
		    db.execSQL("alter table new rename to dice_sets")
		    db.execSQL("create unique index name_index on dice_sets (name)")
		    db.execSQL("create index spec_index on dice_sets (spec)")
		}
	
		override def down(db: SQLiteDatabase) {
		    db.execSQL("drop index name_index")
		}
	}
}

