package guardian.com.frictionscreen

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.type.TypeFactory
import java.lang.RuntimeException
import java.util.*
import java.util.concurrent.TimeUnit

/*
The idea of the friction-screen recorder is to facilitate showing the friction screen (e.g. premium purchase screen)
after X number of articles read (or similar actions, e.g. pressing a specific button) within a given threshold period (e.g. one week).
For example if the number of minimum article read is 3 and threshold period is 7 days then user will see the premium purchase
screen just after 3 article reads within last 7 days. Once user see the premium screen they will not see it again until the threshold
period is over (in this example 7 days). And after the 7 days it will repeat the same logic.
 */

class FrictionScreenRecorder(private val storage: FrictionDataStorage,
                             private val articleReadThreshold: Int,
                             private val minDaysThreshold: Int) {

    var today = Date()
    var articleEntries: MutableMap<String, Date>
    private var mapper: ObjectMapper = ObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)

    private val thresholdDaysAgo: Date
        get() = today.addDays(-minDaysThreshold)

    init {
        articleEntries = convertToMap(storage.readEntries())
    }

    fun recordArticleRead(articleId: String) {
        if (articleEntries.containsKey(articleId))
            return

        articleEntries[articleId] = today
        articleEntries = trimEntries(articleEntries)
        storage.writeEntries(flattenData(articleEntries))
    }

    fun flattenData(articleEntries: MutableMap<String, Date>): String {
        return mapper.writeValueAsString(articleEntries)
    }

    private fun convertToMap(entryStr: String?): MutableMap<String, Date> {
        if (entryStr.isNullOrEmpty())
            return HashMap()

        val type = TypeFactory.defaultInstance().constructMapType(HashMap::class.java, String::class.java, Date::class.java)
        return mapper.readValue<HashMap<String, Date>>(entryStr, type)
    }

    private fun trimEntries(articleEntries: MutableMap<String, Date>): MutableMap<String, Date> {
        return articleEntries.filter { it.value > thresholdDaysAgo }
                .toList()
                .sortedByDescending { (_, date) -> date }
                .take(articleReadThreshold + 1)
                .toMap().toMutableMap()
    }

    fun shouldShowFrictionScreen(): Boolean {
        if (articleEntries.size <= articleReadThreshold)
            return false

        val lastShownDate = storage.getDateOfLastFrictionScreenView() ?: return true // no stored date means user hasn't seen the subs screen at all

        val diff = getDatesDiffInDays(today, lastShownDate)
        return diff >= minDaysThreshold
    }

    fun markAsSubsScreenDisplayed() {
        storage.setDateOfLastFrictionScreenView()
        articleEntries.clear()
        storage.writeEntries(flattenData(articleEntries))
    }

    private fun getDatesDiffInDays(date1: Date, date2: Date): Int {
        val diff = date1.time - date2.time
        return Math.floor((diff / TimeUnit.HOURS.toMillis(24)).toDouble()).toInt()
    }

    class Builder {
        private var minDaysThreshold: Int = 7
        private var minArticleReadThreshold: Int = 3
        private var storage: FrictionDataStorage? = null

        fun setStorage(storage: FrictionDataStorage): Builder {
            this.storage = storage
            return this
        }

        fun setMinDaysThreshold(minDaysThreshold: Int): Builder {
            this.minDaysThreshold = minDaysThreshold
            return this
        }

        fun setMinArticleReadThreshold(minArticleReadThreshold: Int): Builder {
            this.minArticleReadThreshold = minArticleReadThreshold
            return this
        }

        fun build(): FrictionScreenRecorder {
            return FrictionScreenRecorder(this.storage ?: throw RuntimeException("FrictionScreenStorage has not been initialized"),
                    this.minArticleReadThreshold, this.minDaysThreshold)
        }
    }
}

interface FrictionDataStorage {
    fun readEntries(): String?
    fun writeEntries(articleEntries: String)
    fun getDateOfLastFrictionScreenView(): Date?
    fun setDateOfLastFrictionScreenView()
}