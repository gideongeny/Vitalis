package com.gideongeng.Vitalis.Ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardItem

object AdManager {
    private const val TAG = "AdManager"

    // Ad Unit IDs
    private const val REWARDED_INTERSTITIAL_ID = "ca-app-pub-1281448884303417/4379414776"
    private const val BANNER_ID = "ca-app-pub-1281448884303417/2703805799"
    private const val INTERSTITIAL_ID = "ca-app-pub-1281448884303417/1390724124"

    private var interstitialAd: InterstitialAd? = null
    private var rewardedInterstitialAd: RewardedInterstitialAd? = null

    fun initialize(context: Context) {
        MobileAds.initialize(context) { status ->
            Log.d(TAG, "AdMob Initialized: $status")
            loadInterstitial(context)
            loadRewardedInterstitial(context)
        }
    }

    fun loadBanner(adView: AdView) {
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }

    private fun loadInterstitial(context: Context) {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(context, INTERSTITIAL_ID, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(ad: InterstitialAd) {
                interstitialAd = ad
                Log.d(TAG, "Interstitial Ad Loaded")
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                interstitialAd = null
                Log.e(TAG, "Interstitial Ad Failed to Load: ${error.message}")
            }
        })
    }

    fun showInterstitial(activity: Activity) {
        if (interstitialAd != null) {
            interstitialAd?.show(activity)
            loadInterstitial(activity) // Preload next
        } else {
            Log.d(TAG, "Interstitial Ad not ready yet")
            loadInterstitial(activity)
        }
    }

    private fun loadRewardedInterstitial(context: Context) {
        val adRequest = AdRequest.Builder().build()
        RewardedInterstitialAd.load(context, REWARDED_INTERSTITIAL_ID, adRequest, object : RewardedInterstitialAdLoadCallback() {
            override fun onAdLoaded(ad: RewardedInterstitialAd) {
                rewardedInterstitialAd = ad
                Log.d(TAG, "Rewarded Interstitial Ad Loaded")
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                rewardedInterstitialAd = null
                Log.e(TAG, "Rewarded Interstitial Ad Failed to Load: ${error.message}")
            }
        })
    }

    fun showRewardedInterstitial(activity: Activity, onRewardEarned: (RewardItem) -> Unit) {
        if (rewardedInterstitialAd != null) {
            rewardedInterstitialAd?.show(activity) { rewardItem ->
                onRewardEarned(rewardItem)
            }
            loadRewardedInterstitial(activity) // Preload next
        } else {
            Log.d(TAG, "Rewarded Interstitial Ad not ready yet")
            loadRewardedInterstitial(activity)
        }
    }
}
