package guardian.com.frictionscreen.storage

import java.util.*

interface FrictionDataRepository {
    fun readEntries(): String?
    fun writeEntries(articleEntries: String)
    fun getDateOfLastFrictionScreenView(): Date?
    fun setDateOfLastFrictionScreenView()
}
