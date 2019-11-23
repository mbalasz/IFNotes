package com.example.mateusz.ifnotes.matchers

import com.example.mateusz.ifnotes.lib.DateTimeUtils
import com.example.mateusz.ifnotes.model.data.EatingLog
import com.example.mateusz.ifnotes.model.data.LogDate
import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher

class EatingLogEqualsToDate(private val day: Int,
                   private val month: Int,
                   private val year: Int,
                   private val hour: Int,
                   private val minute: Int,
                   private val propertyName: String,
                   private val extractor: (EatingLog) -> LogDate?) : BaseMatcher<EatingLog>() {

    override fun describeTo(description: Description?) {
        description?.appendText(
            "$propertyName of the EatingLog should be equal to " +
                DateTimeUtils.toDateTimeString(day, month, year, hour, minute))
    }

    override fun describeMismatch(item: Any?, description: Description?) {
        description?.appendText("was equal to ")
        val logDate = extractor(item as EatingLog)
        logDate?.let {
            description?.appendText(DateTimeUtils.toDateTimeString(it.dateTimeInMillis))
        } ?: run {
            description?.appendText("null")
        }
    }

    override fun matches(item: Any?): Boolean {
        val logDate = extractor(item as EatingLog)
        return logDate?.let {
            DateTimeUtils.isEqualToDate(it.dateTimeInMillis, day, month, year, hour, minute)
        } ?: false
    }

    companion object {
        fun startsOn(day: Int, month: Int, year: Int, hour: Int, minute: Int): Matcher<EatingLog> {
            return EatingLogEqualsToDate(
                day, month, year, hour, minute, "start time") { it.startTime }
        }

        fun endsOn(day: Int, month: Int, year: Int, hour: Int, minute: Int): Matcher<EatingLog> {
            return EatingLogEqualsToDate(
                day, month, year, hour, minute, "end time") { it.endTime }
        }
    }
}
