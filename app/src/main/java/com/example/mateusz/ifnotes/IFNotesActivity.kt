package com.example.mateusz.ifnotes

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.mateusz.ifnotes.model.EatingLogViewModel
import kotlinx.android.synthetic.main.activity_ifnotes.timeSinceLastActivityChronometer

class IFNotesActivity : AppCompatActivity() {

    val eatingLogViewModel: EatingLogViewModel by lazy {
        ViewModelProviders.of(this).get(EatingLogViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ifnotes)

        eatingLogViewModel.timeSinceLastActivity.observe(this, Observer<Long> {
            time -> if (time != null) timeSinceLastActivityChronometer.base = time
        })


    }
}
