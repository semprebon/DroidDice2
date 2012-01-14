package com.droiddice.datastore
import com.droiddice.model.DiceSet
import android.os.AsyncTask
import android.app.ProgressDialog
import android.content.Context
import android.util.Log
import android.app.Activity
import android.database.SQLException
import android.net.Uri
import android.content.ContentValues

/**
 * This class provides a wrapper around the Dice Set content provider for doing model-level
 * actions to the dice set database. It handles threading and translating application concepts 
 * into content provider actions.
 * 
 * Collaborators:
 * * DiceSetMapper maps DiceSet fields to content provider / database columns
 */
class DiceSetDataStore(activity: Activity) {

	val contentResolver = activity.getContentResolver()
	val uri = Uri.withAppendedPath(DiceSetProvider.CONTENT_URI, "diceset")
		
    private val TAG = "DiceSetDataStore"
        
    /**
     * Creates a new diceset. Executes after if successful, otherwise displays an error message
     */
	def create(diceSet: DiceSet, after: () => Unit) {
   	    val process = new AsyncStoreProcess(after) {
	        override def process(diceSet: DiceSet): Int = {
	        	val values = DiceSetMapper.diceSetToValues(diceSet)

	        	Log.d(TAG, "Adding new DiceSet " + diceSet.name)
	        	val itemUri = contentResolver.insert(DiceSetProvider.CONTENT_URI, values)
	        	Log.d(TAG, "Added " + itemUri)
	        	itemUri.getLastPathSegment().toInt
	        }
   	    }
   	    process.execute(diceSet)
	}
	
	def update(diceSet: DiceSet, after: () => Unit) {
   	    val process = new AsyncStoreProcess(after) {
	        override def process(diceSet: DiceSet): Int = {
	        	val id = idForName(diceSet.name)
	        	id match {
		        	case Some(id) => {
		        		val values = DiceSetMapper.diceSetToValues(diceSet)
		        		Log.d(TAG, "Updating DiceSet " + id)
		        		val itemUri = Uri.withAppendedPath(uri, id.toString())
		        		contentResolver.update(itemUri, values, null, null)
		        		return id
		        	}
		        	case None => throw new SQLException("Updating nonexistant record " + diceSet.name)
		        }
	        }
   	    }
   	    process.execute(diceSet)
	}
	
	def rename(diceSet: DiceSet, newName: String, after: () => Unit) {
	    val process = new AsyncStoreProcess(after) {
	        override def doInBackground(items: Object*) : Int = {
	            val diceSet = items(0).asInstanceOf[DiceSet]
	            val newName = items(1).asInstanceOf[String]
	    		val id = idForName(diceSet.name)
	    		id match {
		    		case Some(id) => {
		    			Log.d(TAG, "Renaming DiceSet " + id + " to " + newName)
		    			val itemUri = Uri.withAppendedPath(uri, id.toString())
		    			val values = new ContentValues()
		    			values.put(DiceSetProvider.NAME, newName)
		    			contentResolver.update(itemUri, values, null, null)
		    			return id
		    		}
		    		case None => throw new SQLException("Updating nonexistant record " + diceSet.name)
	    		}
	    	}
	    }
	    process.execute(diceSet, newName)
	}

	def delete(diceSet: DiceSet, after: () => Unit) {
   	    val process = new AsyncStoreProcess(after) {
	        override def process(diceSet: DiceSet): Int = {
	        	val id = idForName(diceSet.name)
	        	id match {
		        	case Some(id) => {
		        		val values = DiceSetMapper.diceSetToValues(diceSet)
		        		Log.d(TAG, "Deleting DiceSet " + id)
		        		val itemUri = Uri.withAppendedPath(uri, id.toString())
		        		contentResolver.delete(itemUri, null, null)
		        		return id
		        	}
		        	case None => throw new SQLException("Deleting nonexistant record " + diceSet.name)
		        }
	        }
   	    }
   	    process.execute(diceSet)
	}
	
	/**
	 * Determine if a name already exists
	 */
	private def nameExistsAsync(name: String): Boolean = !idForName(name).isEmpty
	
	/**
	 * return id for a given name
	 */
	private def idForName(name: String): Option[Int] = {
		Log.d(TAG, "Quering DiceSetProvider for " + name)
		val cursor = activity.managedQuery(uri, 
			Array(DiceSetProvider._ID), DiceSetProvider.NAME + "=?", Array(name), null)
	    val id = if (cursor.moveToFirst()) Some(cursor.getInt(0)) else None
	    cursor.close()
	    return id
	}

    /**
     * This should really by DiceSet, not Object; but due to bug, scala sometimes has problems 
     * passing varargs; but works with Object... 
     */ 
	class AsyncStoreProcess(after: () => Unit) extends AsyncTask[Object, Void, Int] {
	    var dialog: ProgressDialog = _
	    
		override protected def doInBackground(items: Object*): Int = {
		    process(items(0).asInstanceOf[DiceSet])
		}
		
	    protected def process(diceSet: DiceSet): Int = { 
	        throw new UnsupportedOperationException("Must override process or doInBackground")
	    }
	    
		override protected def onPreExecute() {
		    Log.d(TAG, "onPreExecute")
		        		
			dialog = ProgressDialog.show(activity, "Dice", "Updating...", true)
		}

		override protected def onPostExecute(result: Int) {
		    Log.d(TAG, "onPostExecute")
			dialog.dismiss()
		    after()
		}
	}


}
