package guardian.com.frictionscreen.storage

import android.content.Context
import android.content.SharedPreferences
import java.util.*

/**
 * An implementation of [FrictionDataRepository] that uses the devices [SharedPreferences] to store
 * friction data.
 *
 * @param context context required to create a share preferences object
 */
class SharedPreferencesDataRepository(
        private val context: Context
) : FrictionDataRepository {

    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    }

    override fun getDateOfLastFrictionScreenView(): Date? {
        val lastTime = sharedPreferences.getLong(KEY_LAST_DATE, -1L)
        return if (lastTime > 0) Date(lastTime) else null
    }

    override fun setDateOfLastFrictionScreenView() {
        sharedPreferences.edit().putLong(KEY_LAST_DATE, Date().time).apply()
    }

    override fun writeEntries(articleEntries: String) {
        sharedPreferences.edit().putString(KEY_ENTRIES, articleEntries).apply()
    }

    override fun readEntries(): String? {
        return sharedPreferences.getString(KEY_ENTRIES, null)
    }

    companion object {
        private const val KEY_LAST_DATE = "date_of_last_subs_view"
        private const val KEY_ENTRIES = "key_entries"
        private const val PREFERENCES_NAME = "friction_screen_pref"
    }
}
