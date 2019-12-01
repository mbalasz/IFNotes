package com.example.mateusz.ifnotes.domain.entity

/**
 * @param id identifier of the EatingLog. Treat {@code 0} as not-set.
 */
data class EatingLog(
    val id: Int = 0, val startTime: LogDate? = null, val endTime: LogDate? = null) {

    fun isFinished(): Boolean {
        return startTime != null && endTime != null
    }
}
