package com.example.stepbystep
import android.content.Context
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.io.FileInputStream
import java.io.IOException


class TensorFlowLiteModel(context: MainActivity, name_model: String, out: Int) {

    private var interpreter: Interpreter? = null
    private val outputModel = out

    init {
        try {
            interpreter = Interpreter(loadModelFile(context, name_model, out))
        } catch (e: IOException) {
            e.printStackTrace()// modificare
        }
    }

    @Throws(IOException::class)
    private fun loadModelFile(context: Context, name_model: String, out: Int): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(name_model)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

   /* fun runInference(inputData: Array<FloatArray>): Pair<Float, Float> {
        // var pair = Pair(null, null)
        var ris = Pair(null, null)
        val sampleSize = inputData.size
        val featuresPerSample = inputData[0].size
        val inputBuffer = ByteBuffer.allocateDirect(4 * sampleSize * featuresPerSample)
        inputBuffer.order(ByteOrder.nativeOrder())

        for (i in inputData.indices) {
            for (j in 0 until featuresPerSample) {
                inputBuffer.putFloat(inputData[i][j])
            }
        }
        Log.i("Ris", inputData.size.toString())
        // val outputData = FloatArray(18) // Modifica in base all'output del tuo modello
        val outputData = Array(1) { FloatArray(outputModel) }
        interpreter?.run(inputBuffer, outputData)
        if (outputModel != 1) {
            ris = Pair(argmax(outputData[0]), outputData[0][argmax(outputData[0])])
        } else if(outputModel == 1){
           // ris = Pair(outputData[0][0], 1.0.toFloat())
        }
        return ris
    } */

    fun runInference(inputData: Array<FloatArray>): Pair<Int, Float> {
        // var pair = Pair(null, null)
        val sampleSize = inputData.size
        val featuresPerSample = inputData[0].size
        val inputBuffer = ByteBuffer.allocateDirect(4 * sampleSize * featuresPerSample)
        inputBuffer.order(ByteOrder.nativeOrder())

        for (i in inputData.indices) {
            for (j in 0 until featuresPerSample) {
                inputBuffer.putFloat(inputData[i][j])
            }
        }

        // val outputData = FloatArray(18) // Modifica in base all'output del tuo modello
        val outputData = Array(1) { FloatArray(10) }
        interpreter?.run(inputBuffer, outputData)
        return Pair(argmax(outputData[0]), outputData[0][argmax(outputData[0])])
    }

    private fun argmax(input: FloatArray): Int {
        Log.i("Ris", "${input.asList()}")
        var lastIndex = 0
        var maxElement = input[0]
        for (i in 1 until  input.size){
            Log.i("Ris", "${input[i]} indice: $lastIndex")
            if(input[i] > maxElement){
                maxElement = input[i]
                lastIndex = i
            }
        }
        return lastIndex
    }

    fun close() {
        interpreter?.close()
    }
}
