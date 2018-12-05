package guardian.com.frictionscreen

import android.content.Context
import android.net.ConnectivityManager
import guardian.com.frictionscreen.storage.FrictionDataRepository
import guardian.com.frictionscreen.storage.SharedPreferencesDataRepository


class FrictionScreenManager(
        private val context: Context,
        private val config: Config,
        private val dataRepository: FrictionDataRepository = SharedPreferencesDataRepository(context)
) {

    private val recorder: FrictionScreenRecorder by lazy {
        FrictionScreenRecorder.Builder()
                .setStorage(dataRepository)
                .setMinArticleReadThreshold(config.articleReadThreshold)
                .setMinDaysThreshold(config.minDaysThreshold).build()
    }

    fun recordArticleRead(articleId: String) {
        if (config.isEnabled) {
            recorder.recordArticleRead(articleId.hashCode().toString())
        }
    }

    fun shouldShowSubsScreen(): Boolean {
        return config.isEnabled
                && (!config.showFrictionScreenWhileOnline || haveInternetConnection())
                && recorder.shouldShowFrictionScreen()
    }

    fun setSubscriptionScreenDisplayed() {
        recorder.markAsSubsScreenDisplayed()
    }

    private fun haveInternetConnection(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isAvailable && networkInfo.isConnected
    }

    data class Config(
            val articleReadThreshold: Int,
            val minDaysThreshold: Int,
            val isEnabled: Boolean,
            val showFrictionScreenWhileOnline: Boolean
    )
}
