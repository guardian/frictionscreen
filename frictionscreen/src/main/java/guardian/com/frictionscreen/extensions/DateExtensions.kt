package guardian.com.frictionscreen.extensions

import java.util.*
import java.util.Calendar.DATE
import java.util.concurrent.TimeUnit
import kotlin.math.floor

fun Date.addDays(int: Int): Date {
    val calendar = Calendar.getInstance().apply {
        time = this@addDays
        add(DATE, int)
    }
    return calendar.time
}

fun Date.getDatesDiffInDays(comparison: Date): Int {
    val millisecondDifference = time - comparison.time
    return floor((millisecondDifference / TimeUnit.HOURS.toMillis(24)).toDouble()).toInt()
}
