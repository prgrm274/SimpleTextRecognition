package com.programmer2704.simpletextrecognition

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.programmer2704.simpletextrecognition.databinding.ActivityMainBinding
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private val b: ActivityMainBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(b.root)

        with(b) {
            BTN.setOnClickListener {
                openActivityForResult()
            }
        }
    }

    private fun openActivityForResult() {
        var intent = Intent().apply {
            type = "image/*"
            action = Intent.ACTION_GET_CONTENT
        }
            .putExtra("requestCode", 121)
        resultLauncher.launch(Intent.createChooser(intent, "Pilih gambar/foto"))
    }

    private var resultLaunch = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback {
            if (it.resultCode == RESULT_OK) {
                val intent: Intent = it?.data!!
                //get your "requestCode" here with switch for "SomeUniqueID"

                if (it.data!!.hasExtra("requestCode")) {

                    Log.d(TAG, true.toString())
                }
            }
        }
    )

    private var resultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data

            if (data?.getIntExtra("requestCode", 0) == 121) {

            }

            if (data != null) {
                b.IMG.setImageURI(data.data)

                val image: InputImage
                try {
                    image = InputImage.fromFilePath(applicationContext, data.data!!)
                    val textRecognizer =
                        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                    textRecognizer.process(image)
                        .addOnSuccessListener { visionText ->
                            // Task completed successfully
                            val resultText = visionText.text
                            for (block in visionText.textBlocks) {
                                val blockText = block.text
                                val blockCornerPoints = block.cornerPoints
                                val blockFrame = block.boundingBox
                                for (line in block.lines) {
                                    val lineText = line.text
                                    val lineCornerPoints = line.cornerPoints
                                    val lineFrame = line.boundingBox
                                    for (element in line.elements) {
                                        val elementText = element.text
                                        val elementCornerPoints = element.cornerPoints
                                        val elementFrame = element.boundingBox
                                        b.TEXT.append("${element.text} ")
                                        Log.d(TAG, element.text)
                                    }
                                    b.TEXT.append("\n")
                                }
                                b.TEXT.append("\n")
                            }
                            Log.d(TAG, "addOnSuccesssListener block")
                        }
                        .addOnFailureListener { e ->
                        }
                } catch (io: IOException) {
                    io.printStackTrace()
                }
                Log.d(TAG, "Extra => ${data.getIntExtra("requestCode", 0)}")
            }

        }
    }

    companion object {
        private val TAG = "MainActivity"
    }
}