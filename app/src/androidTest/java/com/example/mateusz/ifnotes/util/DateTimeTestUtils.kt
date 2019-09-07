package com.example.mateusz.ifnotes.util

import com.example.mateusz.ifnotes.lib.DateTimeUtils.Companion.getDayOfMonthFromMillis
import com.example.mateusz.ifnotes.lib.DateTimeUtils.Companion.getHourFromMillis
import com.example.mateusz.ifnotes.lib.DateTimeUtils.Companion.getMinuteFromMillis
import com.example.mateusz.ifnotes.lib.DateTimeUtils.Companion.getMonthFromMillis
import com.example.mateusz.ifnotes.lib.DateTimeUtils.Companion.getYearFromMillis
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat

class DateTimeTestUtils {
    companion object {
        fun assertThatMsAreEqualToDateTime(
            millis: Long, day: Int, month: Int, year: Int, hour: Int, minute: Int) {
            assertThat(getYearFromMillis(millis), equalTo(year))
            assertThat(getMonthFromMillis(millis), equalTo(month))
            assertThat(getDayOfMonthFromMillis(millis), equalTo(day))
            assertThat(getHourFromMillis(millis), equalTo(hour))
            assertThat(getMinuteFromMillis(millis), equalTo(minute))
        }

        fun assertThatMsAreEqualToTime(millis: Long, hour: Int, minute: Int) {
            assertThat(getHourFromMillis(millis), equalTo(hour))
            assertThat(getMinuteFromMillis(millis), equalTo(minute))
        }
    }
}
