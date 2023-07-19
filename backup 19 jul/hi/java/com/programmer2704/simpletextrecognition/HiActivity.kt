package com.programmer2704.simpletextrecognition

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.programmer2704.simpletextrecognition.databinding.ActivityHiBinding

class HiActivity : AppCompatActivity() {
    private val b: ActivityHiBinding by lazy { ActivityHiBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(b.root)
        with(b) {
            tv.text
        }
        Toast.makeText(this, "HiActivity", Toast.LENGTH_SHORT).show()

    }
}