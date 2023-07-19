package com.programmer2704.simpletextrecognition

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class RedCameraViewModel : ViewModel() {

    interface TextRecognitionCallback {
        fun onTextRecognitionComplete(expression: String?, result: Int?)
        fun onTextRecognitionFailed(error: String)
    }

    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    fun processImage(savedUri: Uri, applicationContext: Context, callback: TextRecognitionCallback) {
        val image = try {
            InputImage.fromFilePath(applicationContext, savedUri)
        } catch (io: Exception) {
            callback.onTextRecognitionFailed("Error creating InputImage: ${io.message}")
            return
        }

        textRecognizer.process(image)
            .addOnSuccessListener { visionText ->
                var result: Int? = null
                var expression: String? = null

                for (block in visionText.textBlocks) {
                    val blockText = block.text
                    expression = extractExpression(blockText)
                    if (expression != null) {
                        result = calculateExpression(expression)
                        break // Stop processing further blocks if we find an expression
                    }
                }

                callback.onTextRecognitionComplete(expression, result)
            }
            .addOnFailureListener { e ->
                callback.onTextRecognitionFailed("Text recognition failed: ${e.message}")
            }
    }



    // Helper function to extract the first simple arithmetic expression from the text
    private fun extractExpression(text: String): String? {
        val pattern = Regex("""\b(\d+)\s*([+\-*\/])\s*(\d+)\b""")
        val matchResult = pattern.find(text)

        if (matchResult != null) {
            return matchResult.value
        }

        // Try matching expression without spaces between operands and the operator
        val patternWithoutSpaces = Regex("""\b(\d+)([+\-*\/])(\d+)\b""")
        val matchResultWithoutSpaces = patternWithoutSpaces.find(text)

        return matchResultWithoutSpaces?.value
    }

    // Helper function to calculate the result of a simple arithmetic expression
    private fun calculateExpression(expression: String): Int {
        val pattern = Regex("""(\d+)([+\-*\/])(\d+)""")
        val matchResult = pattern.find(expression)

        if (matchResult != null) {
            val operand1 = matchResult.groupValues[1].toInt()
            val operator = matchResult.groupValues[2]
            val operand2 = matchResult.groupValues[3].toInt()

            return when (operator) {
                "+" -> operand1 + operand2
                "-" -> operand1 - operand2
                "*" -> operand1 * operand2
                "/" -> operand1 / operand2
                else -> throw IllegalArgumentException("Invalid operator")
            }
        }

        throw IllegalArgumentException("Invalid expression format")
    }
}

