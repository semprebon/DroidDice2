package com.droiddice.ui

import com.google.ads.AdListener
import android.app.Activity
import com.google.ads.AdRequest
import com.google.ads.Ad
import android.view.View
import android.widget.ViewAnimator
import com.google.ads.AdView
import com.droiddice.R
import android.util.Log

/**
 * This class is responsible for hnadling ads.
 * 
 * * No ads should show for "pro" variant
 * * If no ads available, show our own "upgrade" ad that goes to android market
 */
class AdSwitcher(fragment: FragmentViewFinder) extends AdListener {
    val adView = fragment.findById[AdView](R.id.ad_view)
	val upgradeAdView = fragment.findById[View](R.id.upgrade_ad_view)
	val adArea = fragment.findById[ViewAnimator](R.id.ad_area)
	
	val TAG = "AdSwitcher"
	    
	def configure(variant: String) {
        Log.d(TAG, "adView from " + R.id.ad_view + " is " + adView)
        if (variant.equals("lite")) {
        	adView.setAdListener(this)
        } else {
            adArea.setVisibility(View.INVISIBLE)
        }
	}
	
	def onReceiveAd(ad: Ad) {
		upgradeAdView.setVisibility(View.INVISIBLE)
		adArea.setDisplayedChild(1)
	}

	def onFailedToReceiveAd(ad: Ad, error: AdRequest.ErrorCode) {}
	def onPresentScreen(ad: Ad) {}
	def onDismissScreen(ad: Ad) {}
	def onLeaveApplication(ad: Ad) {}
}