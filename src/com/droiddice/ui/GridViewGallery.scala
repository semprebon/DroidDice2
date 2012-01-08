package com.droiddice.ui

import android.widget.Gallery
import android.view.ViewParent
import android.content.Context
import android.util.AttributeSet
import android.view.ContextMenu
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.util.Log

class GridViewGallery(context: Context, attrs: AttributeSet) extends Gallery(context, attrs) {

	private val viewConfig = ViewConfiguration.get(context)
	private val SWIPE_MIN_DISTANCE = viewConfig.getScaledTouchSlop()
	private val SWIPE_THRESHOLD_VELOCITY = viewConfig.getScaledMinimumFlingVelocity()
	private val SWIPE_MAX_OFF_PATH = viewConfig.getScaledTouchSlop()

    private var startX: Float = _
    private var startY: Float = _
    
    private val TAG = "GridViewGallery"

    Log.d(TAG, "init min=" + SWIPE_MIN_DISTANCE + "  off_path=" + SWIPE_MAX_OFF_PATH +  "  vel=" + SWIPE_THRESHOLD_VELOCITY)

    def isHorizontalSwipe(startX: Float, startY: Float, end: MotionEvent): Boolean = {
		Log.d(TAG, "HorizontalSwipe")
	    val deltaY = Math.abs(end.getY() - startY)
		if (deltaY > SWIPE_MAX_OFF_PATH) {
			Log.d(TAG, "off path: "  + deltaY)
			return false
		}
		
		val deltaX = Math.abs(end.getX() - startX)
		val velocityX = deltaX * 1000.0 / (end.getEventTime() - end.getDownTime())

		if (deltaX > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
		    return true
		}
		Log.d(TAG, "not enough delta: "  + deltaX + "  velovity:" + velocityX)
		return false
	}
	
    /**
     * This will be called before the intercepted views onTouchEvent is called
     * Return false to keep intercepting and passing the event on to the target view
     * Return true and the target view will recieve ACTION_CANCEL, and the rest of the
     * events will be delivered to our onTouchEvent
     */
    override def onInterceptTouchEvent(ev: MotionEvent): Boolean = {
        val action = ev.getAction()
        action match {
        	case MotionEvent.ACTION_DOWN => {
        	    Log.d(TAG, "Down at " + ev.getX() + "," + ev.getY())
        	    startX = ev.getX()
        	    startY = ev.getY()
        		super.onTouchEvent(ev)
        	}

        	case MotionEvent.ACTION_MOVE => {
        	    Log.d(TAG, "Move at " + ev.getX() + "," + ev.getY() +  " siwpe? " + isHorizontalSwipe(startX, startY, ev))
        	    return isHorizontalSwipe(startX, startY, ev)
        	}

        	case _ => super.onTouchEvent(ev)
        }
        return false
    }

}