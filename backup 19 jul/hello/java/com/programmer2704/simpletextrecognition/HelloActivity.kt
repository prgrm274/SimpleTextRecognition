package com.programmer2704.simpletextrecognition

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.programmer2704.simpletextrecognition.databinding.ActivityHelloBinding

class HelloActivity : AppCompatActivity() {
    private val b: ActivityHelloBinding by lazy { ActivityHelloBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(b.root)
        with(b) {
            tv.text
        }
    }
}