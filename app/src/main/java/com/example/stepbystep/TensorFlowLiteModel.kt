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
            e.printStackTrace()
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

    fun runInference(inputData: Array<FloatArray>): Pair<Float, Float> {
        // var pair = Pair(null, null)
        var ris = Pair(0.0.toFloat(), 0.0.toFloat())
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
        val outputData = Array(1) { FloatArray(outputModel) }
        Log.w("Debug", "ok: prima dell'inferenza");//return 600
        interpreter?.run(inputBuffer, outputData)
        Log.w("Debug", "ok: dopo l'inferenza");//return 600
        Log.i("Ris", outputData[0].toString())
        if (outputModel == 10) {
            ris = Pair(argmax(outputData[0]).toFloat(), outputData[0][argmax(outputData[0])])
        } else if(outputModel == 1){
            ris = Pair(outputData[0][0], 1.0.toFloat())
        }
        return ris
    }

    private fun argmax(input: FloatArray): Int {
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
