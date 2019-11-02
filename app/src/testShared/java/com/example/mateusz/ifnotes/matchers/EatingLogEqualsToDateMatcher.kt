package com.example.mateusz.ifnotes.matchers

import com.example.mateusz.ifnotes.lib.DateTimeUtils
import com.example.mateusz.ifnotes.model.data.EatingLog
import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher

class EatingLogEqualsToDate(private val day: Int,
                   private val month: Int,
                   private val year: Int,
                   private val hour: Int,
                   private val minute: Int,
                   private val propertyName: String,
                   private val extractor: (EatingLog) -> Long) : BaseMatcher<EatingLog>() {

    override fun describeTo(description: Description?) {
        description?.appendText(
            "$propertyName of the EatingLog should be equal to " +
                DateTimeUtils.toDateTimeString(day, month, year, hour, minute))
    }

    override fun describeMismatch(item: Any?, description: Description?) {
        description?.appendText("was equal to " +
            DateTimeUtils.toDateTimeString(extractor(item as EatingLog)))
    }

    override fun matches(item: Any?): Boolean {
        return DateTimeUtils.isEqualToDate(
            extractor(item as EatingLog), day, month, year, hour, minute)
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
