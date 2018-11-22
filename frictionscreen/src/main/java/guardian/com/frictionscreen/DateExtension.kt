package guardian.com.frictionscreen

import java.util.*

fun Date.addDays(int: Int): Date {
    val cal = Calendar.getInstance()
    cal.time = this
    cal.add(Calendar.DATE, int)
    return cal.time
}