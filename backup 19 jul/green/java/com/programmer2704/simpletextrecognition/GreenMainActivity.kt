package com.programmer2704.simpletextrecognition

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
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
import com.programmer2704.simpletextrecognition.databinding.ActivityGreenMainBinding
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

// geeks camera
class GreenMainActivity : AppCompatActivity() {
    private val b: ActivityGreenMainBinding by lazy {
        ActivityGreenMainBinding.inflate(
            layoutInflater
        )
    }

    private var imageCapture: ImageCapture? = null
    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(b.root)

        // hide the action bar
        supportActionBar?.hide()

        // Check camera permissions if all permission granted start camera else ask for the permission
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        // set on click listener for the button of capture photo it calls a method implemented below
        b.cameraCaptureButton.setOnClickListener {
            takePhoto()
        }
        outputDirectory = getOutputDirectory()
        cameraExecutor = Executors.newSingleThreadExecutor()

        with(b) {
//            text.text
        }
        Toast.makeText(this, "Green camera", LENGTH_SHORT).show()

    }

    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        // Create time-stamped output file to hold the image
        val photoFile = File(
            outputDirectory,
            SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis()) + ".jpg"
        )

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        // Set up image capture listener, which is triggered after photo has been taken
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)

                    // set the saved uri to the image view
                    b.ivCapture.visibility = View.VISIBLE
                    b.ivCapture.setImageURI(savedUri)

                    val msg = "Photo capture succeeded: $savedUri"
                    Toast.makeText(baseContext, msg, Toast.LENGTH_LONG).show()
                    Log.d(TAG, msg)


                    val image: InputImage
                    try {
                        /*image = InputImage.fromFilePath(applicationContext, savedUri)
                        val textRecognizer =
                            TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                        textRecognizer.process(image).addOnSuccessListener { visionText ->
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
                                        b.TVResult.append("${element.text} ")
                                        Log.d(TAG, element.text)
                                    }
                                    b.TVResult.append("||||")
//                                        b.TVResult.append("\n")
                                }
                                b.TVResult.append("||||")
//                                    b.TVResult.append("\n")
                            }
                            Log.d(TAG, "addOnSuccesssListener block")
                        }.addOnFailureListener { e ->
                        }*/



                        image = InputImage.fromFilePath(applicationContext, savedUri)
                        val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                        textRecognizer.process(image)
                            .addOnSuccessListener { visionText ->
                                val expressions = mutableListOf<String>()

                                for (block in visionText.textBlocks) {
                                    for (line in block.lines) {
                                        val lineText = line.text
                                        val expression = extractExpression(lineText)
                                        if (expression != null) {
                                            expressions.add(expression)
                                            break  // Stop processing further lines
                                        }
                                    }

                                    if (expressions.isNotEmpty()) {
                                        break  // Stop processing further blocks
                                    }
                                }

                                if (expressions.isNotEmpty()) {
                                    val firstExpression = expressions[0]
                                    Log.d(TAG, firstExpression)
                                    b.TVResult.text = firstExpression
                                } else {
                                    Log.d(TAG, "No expression found")
                                    b.TVResult.text = "No expression found"
                                }
                            }
                            .addOnFailureListener { e ->
                                // Handle failure
                            }


                    } catch (io: IOException) {
                        io.printStackTrace()
                    }
                }
            })
    }

    private fun extractExpression(text: String): String? {
        val pattern = Regex("""(\d+)\s*([+\-*\/])\s*(\d+)""")
        val matchResult = pattern.find(text)

        if (matchResult != null) {
            val operand1 = matchResult.groupValues[1]
            val operator = matchResult.groupValues[2]
            val operand2 = matchResult.groupValues[3]

            return "$operand1 $operator $operand2"
        }

        return null
    }


    private fun extractExpression1(text: String): String? {
        // Regular expression pattern to match expressions
        val pattern = Regex("""\b(\d+)\s*([+\-*\/])\s*(\d+)\b""")
        val matchResult = pattern.find(text)

        if (matchResult != null) {
            val operand1 = matchResult.groupValues[1]
            val operator = matchResult.groupValues[2]
            val operand2 = matchResult.groupValues[3]

            return "$operand1 $operator $operand2"
        }

        return null
    }


    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener(Runnable {
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(b.viewFinder.surfaceProvider)
//                    it.setSurfaceProvider(b.viewFinder.createSurfaceProvider())
                }

            imageCapture = ImageCapture.Builder().build()

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )

            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    // creates a folder inside internal storage
    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, "app_name").apply { mkdirs() }
//            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }

    // checks the camera permission
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            // If all permissions granted, then start Camera
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                // If permissions are not granted, present a toast to notify the user that
                // the permissions were not granted.
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT)
                    .show()
                finish()
            }
        }
    }

    companion object {
        private const val TAG = "CameraXGFG"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 20
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}