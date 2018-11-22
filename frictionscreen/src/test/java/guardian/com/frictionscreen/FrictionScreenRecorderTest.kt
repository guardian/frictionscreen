package guardian.com.frictionscreen

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import junit.framework.Assert.*
import org.junit.Test
import java.util.*

class FrictionScreenRecorderTest {

    @Test
    fun `Test initially entries are empty`() {
        val recorder = createRecorder()
        assertTrue(recorder.articleEntries.isEmpty())
    }

    @Test
    fun `Test recorder is actually recording unique entries`() {
        val recorder = createRecorder()
        recorder.recordArticleRead("article-1")
        assertEquals(1, recorder.articleEntries.size)

        recorder.recordArticleRead("article-1")
        assertEquals(1, recorder.articleEntries.size)

        recorder.recordArticleRead("article-2")
        assertEquals(2, recorder.articleEntries.size)
    }

    @Test
    fun `Test when to show friction screen is correct for inadequate data`() {
        val recorder = createRecorder(minArticleRead = 3)
        recorder.recordArticleRead("article-1")
        recorder.recordArticleRead("article-2")
        recorder.recordArticleRead("article-3")
        assertFalse(recorder.shouldShowFrictionScreen())
    }

    @Test
    fun `Test recorder logic is correct when min articles read requirement have meet`() {
        val recorder = createRecorder(minArticleRead = 3)
        recorder.recordArticleRead("article-1")
        recorder.recordArticleRead("article-2")
        recorder.recordArticleRead("article-3")
        recorder.recordArticleRead("article-4")
        assertTrue(recorder.shouldShowFrictionScreen())
    }

    @Test
    fun `Test logic is correct for not showing more than once within threshold period`() {
        val mockCallback: FrictionDataStorage = mock()
        val recorder = FrictionScreenRecorder(mockCallback, 3, 7)

        recorder.recordArticleRead("article-1")
        recorder.recordArticleRead("article-2")
        recorder.recordArticleRead("article-3")
        recorder.recordArticleRead("article-4")
        assertTrue(recorder.shouldShowFrictionScreen())

        recorder.markAsSubsScreenDisplayed()
        whenever(mockCallback.getDateOfLastFrictionScreenView()).thenReturn(Date())

        recorder.recordArticleRead("article-5")
        recorder.recordArticleRead("article-6")
        recorder.recordArticleRead("article-7")
        recorder.recordArticleRead("article-8")
        assertFalse(recorder.shouldShowFrictionScreen())

        whenever(mockCallback.getDateOfLastFrictionScreenView()).thenReturn(Date().addDays(-2))
        assertFalse(recorder.shouldShowFrictionScreen())

        whenever(mockCallback.getDateOfLastFrictionScreenView()).thenReturn(Date().addDays(-3))
        assertFalse(recorder.shouldShowFrictionScreen())

        whenever(mockCallback.getDateOfLastFrictionScreenView()).thenReturn(Date().addDays(-7))
        assertFalse(recorder.shouldShowFrictionScreen())
    }


    @Test
    fun `Test logic is correct for keeping record of only within the threshold periods`() {
        val mockCallback: FrictionDataStorage = mock()
        val recorder = FrictionScreenRecorder(mockCallback, 3, 3)

        recorder.recordArticleRead("article-1")
        recorder.recordArticleRead("article-2")
        recorder.recordArticleRead("article-3")

        recorder.today = getAdjustedDate(5) // 5 days ahead in future
        recorder.recordArticleRead("article-4")
        recorder.recordArticleRead("article-5")
        assertFalse(recorder.shouldShowFrictionScreen())

        recorder.recordArticleRead("article-7")
        recorder.recordArticleRead("article-8")
        assertTrue(recorder.shouldShowFrictionScreen())
    }

    @Test
    fun `Test friction test callbacks get called`() {
        val mockCallback: FrictionDataStorage = mock()
        val recorder = FrictionScreenRecorder(mockCallback,1,2)

        recorder.recordArticleRead("article-1")
        verify(mockCallback).writeEntries(recorder.flattenData(recorder.articleEntries))

        recorder.markAsSubsScreenDisplayed()
        verify(mockCallback).setDateOfLastFrictionScreenView()
    }

    private fun createRecorder(lastSubsScreenViewDate: Date? = null
                               , minArticleRead: Int = 3, minDaysThreshold: Int = 7): FrictionScreenRecorder {
        val mockCallback: FrictionDataStorage = mock()
        whenever(mockCallback.getDateOfLastFrictionScreenView()).thenReturn(lastSubsScreenViewDate)
        return FrictionScreenRecorder(mockCallback, minArticleRead, minDaysThreshold)
    }

    private fun getAdjustedDate(dayAdjustment: Int): Date {
        return Date().addDays(dayAdjustment)
    }
}
