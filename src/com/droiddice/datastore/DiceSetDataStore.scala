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
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface

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
	val uri = Uri.withAppendedPath(DiceSetProvider.contentUri(activity), "dicesets")
		
    private val TAG = "DiceSetDataStore"
        
    /**
     * Creates a new diceset. Executes after if successful, otherwise displays an error message
     */
	def create(diceSet: SavedDiceSet, after: (Throwable, Long) => Unit) {
   	    val process = new AsyncStoreProcess(after) {
	        override def process(diceSet: SavedDiceSet): Long = {
	        	val values = DiceSetMapper.diceSetToValues(diceSet)

	        	Log.d(TAG, "Adding new DiceSet " + diceSet)
	        	Log.d(TAG, "url for insert is " + DiceSetProvider.contentUri(activity))
	        	val itemUri = contentResolver.insert(DiceSetProvider.contentUri(activity), values)
	        	Log.d(TAG, "Added " + itemUri)
	        	itemUri.getLastPathSegment().toLong
	        }
   	    }
   	    process.execute(diceSet)
	}
	
	def update(diceSet: SavedDiceSet, after: (Throwable, Long) => Unit) {
   	    val process = new AsyncStoreProcess(after) {
	        override def process(diceSet: SavedDiceSet): Long = {
	        	if (!diceSet.isNamed) deleteAnonymousDuplicate(diceSet.spec)
	        	val id = diceSet.id
        		val values = DiceSetMapper.diceSetToValues(diceSet)
        		Log.d(TAG, "Updating DiceSet " + diceSet)
        		val itemUri = DiceSetProvider.uriFor(activity, id)
        		contentResolver.update(itemUri, values, null, null)
        		return id
	        }
   	    }
   	    process.execute(diceSet)
	}
	
	def rename(diceSet: SavedDiceSet, newName: String, after: (Throwable, Long) => Unit) {
	    val process = new AsyncStoreProcess(after) {
	        override def doInBackground(items: Object*) : Long = {
	            val diceSet = items(0).asInstanceOf[SavedDiceSet]
	            val newName = items(1).asInstanceOf[String]
	        	val id = diceSet.id
	        	if (!diceSet.isNamed) deleteAnonymousDuplicate(diceSet.spec)
    			Log.d(TAG, "Renaming DiceSet " + id + " to " + newName)
    			val itemUri = DiceSetProvider.uriFor(activity, id)
    			val values = new ContentValues()
    			values.put(DiceSetProvider.NAME, newName)
    			contentResolver.update(itemUri, values, null, null)
    			return id
	    	}
	    }
	    process.execute(diceSet, newName)
	}

	def delete(diceSet: SavedDiceSet, after: (Throwable, Long) => Unit) {
   	    val process = new AsyncStoreProcess(after) {
	        override def process(diceSet: SavedDiceSet): Long = {
	        	val id = diceSet.id
	        	val values = DiceSetMapper.diceSetToValues(diceSet)
        		Log.d(TAG, "Deleting DiceSet " + id)
        		val itemUri = DiceSetProvider.uriFor(activity, id)
        		contentResolver.delete(itemUri, null, null)
        		return id
	        }
   	    }
   	    process.execute(diceSet)
	}
	
	def fetchByName(name: String, after: (SavedDiceSet) => Unit) {
   	    val process = new AsyncQuery(after) {
	        override def process(selection: String, args: Array[String]): SavedDiceSet = {
	        	Log.d(TAG, "Quering DiceSetProvider for " + name)
	        	val cursor = contentResolver.query(uri, DiceSetProvider.PUBLIC_COLUMNS,  DiceSetProvider.NAME + "=?", Array(name), null)
	        	val diceSet = if (cursor.moveToFirst()) DiceSetMapper.cursorToDiceSet(cursor) else null
	        	cursor.close()
	        	return diceSet
	        }
   	    }
   	    process.execute(DiceSetProvider.NAME + "=?", Array(name))
	}
	
	/**
	 * Determine if a name already exists
	 */
	private def nameExistsAsync(name: String): Boolean = !idForName(name).isEmpty
	
	private def deleteAnonymousDuplicate(spec: String) {
		contentResolver.delete(uri, DiceSetProvider.SPEC + "=? and " + DiceSetProvider.NAME + " is null", Array(spec))
	}
	
	/**
	 * return id for a given name
	 */
	private def idForName(name: String): Option[Long] = {
		Log.d(TAG, "Quering DiceSetProvider for " + name)
		val cursor = activity.managedQuery(uri, 
			Array(DiceSetProvider._ID), DiceSetProvider.NAME + "=?", Array(name), null)
	    val id = if (cursor.moveToFirst()) Some(cursor.getLong(0)) else None
	    cursor.close()
	    return id
	}

	/**
	 * return id for anonymous dice set with given spec
	 */
	private def idForAnonymous(spec: String): Option[Long] = {
		Log.d(TAG, "Quering DiceSetProvider for " + spec)
		val cursor = activity.managedQuery(uri, 
			Array(DiceSetProvider._ID), DiceSetProvider.SPEC + "=? and " + DiceSetProvider.NAME + "=null", Array(spec), null)
	    val id = if (cursor.moveToFirst()) Some(cursor.getLong(0)) else None
	    cursor.close()
	    return id
	}

	/**
	 * Runs a query asynchronously and returns the first dice set in the results to a callback method
	 */
	class AsyncQuery(after: (SavedDiceSet) => Unit) extends AsyncTask[Object, Void, SavedDiceSet] {
	    var dialog: ProgressDialog = _
	    
		override protected def doInBackground(items: Object*): SavedDiceSet = {
	        val selection = items(0).asInstanceOf[String]
	        val args = items(1).asInstanceOf[Array[String]]
	        process(selection, args)
		}
		
	    protected def process(seletion: String, args: Array[String]): SavedDiceSet = { 
	        throw new UnsupportedOperationException("Must override process or doInBackground")
	    }
	    
		override protected def onPreExecute() {
			dialog = new ProgressDialog(activity)
			dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
			dialog.setMessage("Checking...")
			dialog.show()
		}

		override protected def onPostExecute(result: SavedDiceSet) {
			dialog.dismiss()
			after(result)
		}
	}
	
    /**
     * This should really by DiceSet, not Object; but due to bug, scala sometimes has problems 
     * passing varargs; but works with Object... 
     */ 
	class AsyncStoreProcess(after: (Throwable, Long) => Unit) extends AsyncTask[Object, Void, Long] {
	    var dialog: Dialog = _
	    var exception: Throwable = _
	    
		override protected def doInBackground(items: Object*): Long = {
	        try {
	        	process(items(0).asInstanceOf[SavedDiceSet])
	        } catch {
	            case ex: Throwable => { exception = ex }
	            return -1
	        }
		}
		
	    protected def process(diceSet: SavedDiceSet): Long = { 
	        throw new UnsupportedOperationException("Must override process or doInBackground")
	    }
	    
		override protected def onPreExecute() {
		    Log.d(TAG, "onPreExecute")
		        		
			dialog = ProgressDialog.show(activity, null, "Updating...", true)
		}

		override protected def onPostExecute(result: Long) {
		    Log.d(TAG, "onPostExecute")
			dialog.dismiss()
			after(exception, result)
		}
	}


}
