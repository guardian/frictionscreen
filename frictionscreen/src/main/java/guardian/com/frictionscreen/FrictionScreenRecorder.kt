package guardian.com.frictionscreen

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.type.TypeFactory
import guardian.com.frictionscreen.extensions.addDays
import guardian.com.frictionscreen.storage.FrictionDataRepository
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * The idea of the friction-screen recorder is to facilitate showing the friction screen (e.g. premium purchase screen)
 * after X number of articles read (or similar actions, e.g. pressing a specific button) within a given threshold period (e.g. one week).
 * For example if the number of minimum article read is 3 and threshold period is 7 days then user will see the premium purchase
 * screen just after 3 article reads within last 7 days. Once user see the premium screen they will not see it again until the threshold
 * period is over (in this example 7 days). And after the 7 days it will repeat the same logic.
 *
 * @param storageRepository a repository used for storing friction data
 * @param articleReadThreshold number of articles to read before threshold is hit
 * @param minDaysThreshold the time between article thresholds can be hit
 */
class FrictionScreenRecorder(
        private val storageRepository: FrictionDataRepository,
        private val articleReadThreshold: Int,
        private val minDaysThreshold: Int,
        var comparisonDate: Date = Date()
) {

    private val mapper: ObjectMapper by lazy {
        ObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    }

    var articleEntries: MutableMap<String, Date>

    private val thresholdDaysAgo: Date
        get() = comparisonDate.addDays(-minDaysThreshold)

    init {
        articleEntries = convertToMap(storageRepository.readEntries())
    }

    fun recordArticleRead(articleId: String) {
        if (articleEntries.containsKey(articleId)) {
            return
        }

        articleEntries[articleId] = comparisonDate
        articleEntries = trimEntries(articleEntries)
        storageRepository.writeEntries(flattenData(articleEntries))
    }

    fun flattenData(articleEntries: MutableMap<String, Date>): String {
        return mapper.writeValueAsString(articleEntries)
    }

    private fun convertToMap(entryStr: String?): MutableMap<String, Date> {
        if (entryStr.isNullOrEmpty()) {
            return HashMap()
        }
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
        if (articleEntries.size <= articleReadThreshold) {
            return false
        }

        val lastShownDate = storageRepository.getDateOfLastFrictionScreenView()
                ?: return true // no stored date means user hasn't seen the subs screen at all

        val diff = getDatesDiffInDays(comparisonDate, lastShownDate)
        return diff >= minDaysThreshold
    }

    fun markAsSubsScreenDisplayed() {
        storageRepository.setDateOfLastFrictionScreenView()
        articleEntries.clear()
        storageRepository.writeEntries(flattenData(articleEntries))
    }

    private fun getDatesDiffInDays(date1: Date, date2: Date): Int {
        val diff = date1.time - date2.time
        return Math.floor((diff / TimeUnit.HOURS.toMillis(24)).toDouble()).toInt()
    }
}


