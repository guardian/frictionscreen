package guardian.com.frictionscreen.extensions

import java.util.*
import java.util.Calendar.DATE

fun Date.addDays(int: Int): Date {
    val calendar = Calendar.getInstance().apply {
        time = this@addDays
        add(DATE, int)
    }
    return calendar.time
}
