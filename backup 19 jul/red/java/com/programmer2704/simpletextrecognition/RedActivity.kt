package com.programmer2704.simpletextrecognition

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.programmer2704.simpletextrecognition.databinding.ActivityRedBinding
import com.programmer2704.simpletextrecognition.geeksfor.CameraAct
import com.programmer2704.simpletextrecognition.medium.MediumArticleScanAct
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class RedActivity : AppCompatActivity() {
    private val b: ActivityRedBinding by lazy { ActivityRedBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(b.root)

        with(b) {
            BTN.setOnClickListener {
                openActivityForResult()
            }
//            val text = TVFlavorNType.text
//            TVFlavorNType.text = "$text ${BuildConfig.FLAVOR} ${BuildConfig.BUILD_TYPE}"
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
//                    image = InputImage.fromFilePath(applicationContext, data.data!!)
//                    val textRecognizer =
//                        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
//                    textRecognizer.process(image)
//                        .addOnSuccessListener { visionText ->
//                            // Task completed successfully
//                            val resultText = visionText.text
//                            for (block in visionText.textBlocks) {
//                                val blockText = block.text
//                                val blockCornerPoints = block.cornerPoints
//                                val blockFrame = block.boundingBox
//                                for (line in block.lines) {
//                                    val lineText = line.text
//                                    val lineCornerPoints = line.cornerPoints
//                                    val lineFrame = line.boundingBox
//                                    for (element in line.elements) {
//                                        val elementText = element.text
//                                        val elementCornerPoints = element.cornerPoints
//                                        val elementFrame = element.boundingBox
//                                        b.TEXT.append("${element.text} ")
//                                        Log.d(TAG, element.text)
//                                    }
//                                    b.TEXT.append(" | ")
//                                }
//                                b.TEXT.append(" | ")
//                            }
//                            Log.d(TAG, "addOnSuccesssListener block")
//                        }
//                        .addOnFailureListener { e ->
//                        }

                    image = InputImage.fromFilePath(applicationContext, data.data!!)
                    val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                    textRecognizer.process(image)
                        .addOnSuccessListener { visionText ->
                            var capturedExpression: Pair<String, String>? = null

                            for (block in visionText.textBlocks) {
                                for (line in block.lines) {
                                    val lineText = line.text
                                    val expression = extractExpression(lineText)
                                    if (expression != null) {
                                        capturedExpression = expression
                                        break  // Stop processing further lines
                                    }
                                }

                                if (capturedExpression != null) {
                                    break  // Stop processing further blocks
                                }
                            }

                            if (capturedExpression != null) {
                                val (expression, result) = capturedExpression
                                Log.d(TAG, expression)
                                Log.d(TAG, result)
                                b.TEXT.text = expression
                                b.TEXTResult.text = result
                            } else {
                                Log.d(TAG, "No expression found")
                                b.TEXT.text = "No expression found"
                                b.TEXTResult.text = ""
                            }
                        }
                        .addOnFailureListener { e ->
                            // Handle failure
                        }


                } catch (io: IOException) {
                    io.printStackTrace()
                }
                Log.d(TAG, "Extra => ${data.getIntExtra("requestCode", 0)}")
            }

        }
    }

    private fun extractExpression(text: String): Pair<String, String>? {
        val pattern = Regex("""(\d+)\s*([+\-*\/])\s*(\d+)""")
        val matchResult = pattern.find(text)

        if (matchResult != null) {
            val operand1 = matchResult.groupValues[1]
            val operator = matchResult.groupValues[2]
            val operand2 = matchResult.groupValues[3]

            val expression = "$operand1 $operator $operand2"
            val result = calculateExpression(operand1.toInt(), operator, operand2.toInt()).toString()

            return expression to result
        }

        return null
    }

    private fun calculateExpression(operand1: Int, operator: String, operand2: Int): Int {
        return when (operator) {
            "+" -> operand1 + operand2
            "-" -> operand1 - operand2
            "*" -> operand1 * operand2
            "/" -> operand1 / operand2
            else -> throw IllegalArgumentException("Invalid operator: $operator")
        }
    }


    companion object {
        private val TAG = "MainActivity"
    }
}