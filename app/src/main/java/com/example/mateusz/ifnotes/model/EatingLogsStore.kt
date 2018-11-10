package com.example.mateusz.ifnotes.model

import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

class EatingLogsStore {
    private lateinit var eatingLogs: MutableList<EatingLog>
    private val listSubject = BehaviorSubject.create<List<EatingLog>>()

    fun setEatingLogs(eatingLogs: List<EatingLog>) {
        this.eatingLogs = eatingLogs.toMutableList()
        publish()
    }

    fun observe(): Observable<List<EatingLog>> {
        return listSubject
    }

    fun deleteEatingLog(eatingLog: EatingLog) {
        if (eatingLogs.remove(eatingLog)) {
            publish()
        }
    }

    fun insertEatingLog(eatingLog: EatingLog) {
        eatingLogs.add(eatingLog)
        publish()
    }

    private fun publish() {
        listSubject.onNext(eatingLogs)
    }
}