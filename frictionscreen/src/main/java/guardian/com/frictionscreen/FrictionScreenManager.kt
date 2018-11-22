package guardian.com.frictionscreen

import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import java.util.*

class FrictionScreenManager
    constructor(private val context: Context,
                private val config: Config) {

    private val pref: SharedPreferences

    init {
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    private val recorder: FrictionScreenRecorder by lazy {

        val storage = object : FrictionDataStorage {

            override fun writeEntries(articleEntries: String) {
                pref.edit().putString(ENTRIES, articleEntries).apply()
            }

            override fun getDateOfLastFrictionScreenView(): Date? {
                val lastTime = pref.getLong(DATE_STORE_KEY, -1L)
                return if (lastTime > 0) Date(lastTime) else null
            }

            override fun setDateOfLastFrictionScreenView() {
                pref.edit().putLong(DATE_STORE_KEY, Date().time).apply()
            }

            override fun readEntries(): String? {
                return pref.getString(ENTRIES, null)
            }
        }

        FrictionScreenRecorder.Builder()
                .setStorage(storage)
                .setMinArticleReadThreshold(config.articleReadThreshold)
                .setMinDaysThreshold(config.minDaysThreshold).build()
    }

    fun recordArticleRead(articleId: String) {
        if (config.isEnabled)
            recorder.recordArticleRead(articleId.hashCode().toString())
    }

    fun shouldShowSubsScreen(): Boolean {
        return config.isEnabled &&
                (!config.showFrictionScreenWhileOnline || haveInternetConnection()) &&
                recorder.shouldShowFrictionScreen()
    }

    fun setSubscriptionScreenDisplayed() {
        recorder.markAsSubsScreenDisplayed()
    }

    private fun haveInternetConnection(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = cm.activeNetworkInfo
        return netInfo != null && netInfo.isAvailable && netInfo.isConnected
    }

    class Config constructor(val articleReadThreshold: Int,
                             val minDaysThreshold: Int,
                             val isEnabled: Boolean,
                             val showFrictionScreenWhileOnline: Boolean)

    companion object {
        private const val DATE_STORE_KEY = "date_of_last_subs_view"
        private const val ENTRIES = "key_entries"
        private const val PREF_NAME = "friction_screen_pref"
    }
}