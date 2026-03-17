package dev.gorban.zentuner.util

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private val Context.reviewDataStore by preferencesDataStore(name = "in_app_review")

class InAppReviewDelegate(private val activity: ComponentActivity) : DefaultLifecycleObserver {

    private val reviewManager by lazy { ReviewManagerFactory.create(activity) }
    private val crashlytics by lazy { FirebaseCrashlytics.getInstance() }

    private companion object {
        val LAUNCH_COUNT = intPreferencesKey("launch_count")
        val REVIEW_REQUESTED = booleanPreferencesKey("review_requested")
        const val REQUIRED_LAUNCHES = 5
    }

    override fun onCreate(owner: LifecycleOwner) {
        activity.lifecycleScope.launch {
            try {
                checkAndRequestReview()
            } catch (e: Exception) {
                crashlytics.log("InAppReview error: ${e.message}")
            }
        }
    }

    private suspend fun checkAndRequestReview() {
        val prefs = activity.reviewDataStore.data.first()
        val currentCount = (prefs[LAUNCH_COUNT] ?: 0) + 1
        val alreadyRequested = prefs[REVIEW_REQUESTED] ?: false

        activity.reviewDataStore.edit { it[LAUNCH_COUNT] = currentCount }

        if (currentCount >= REQUIRED_LAUNCHES && !alreadyRequested) {
            requestReview()
        }
    }

    private fun requestReview() {
        reviewManager.requestReviewFlow().addOnSuccessListener { reviewInfo ->
            reviewManager.launchReviewFlow(activity, reviewInfo).addOnCompleteListener {
                activity.lifecycleScope.launch {
                    activity.reviewDataStore.edit { it[REVIEW_REQUESTED] = true }
                }
            }
        }.addOnFailureListener { e ->
            crashlytics.log("InAppReview requestReviewFlow failed: ${e.message}")
        }
    }
}
