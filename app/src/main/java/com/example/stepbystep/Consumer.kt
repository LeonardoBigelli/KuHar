package com.example.stepbystep

import android.content.Context
import android.widget.TextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Consumer(context: Context, b: Buffer,textView: TextView) : Runnable{
    private val buffer = b
    private var tfliteModel = TensorFlowLiteModel(context = MainActivity())
    private var displayTextView = textView


    override fun run() {
        var isAlive = true
        CoroutineScope(Dispatchers.Default).launch {
            //  sensorDataFlow.emit(SensorData(accelerometerData.copyOf(), gyroscopeData.copyOf()))
            while (isAlive) {
                try {
                    val sensorData = buffer.remove()
                    val inputData = prepareInputData(sensorData)
                    val result = tfliteModel.runInference(inputData)
                   // displayTextView.text = resultForHuman(result+1)
                }catch (e: InterruptedException){
                    isAlive = false
                }
            }
        }
    }


    private fun prepareInputData(sensorData: List<SensorData>): Array<FloatArray> {
        val sampleSize = 200 // Assuming 200 samples for 2 seconds at 100Hz
        val featuresPerSample = 6 // 3 accelerometer + 3 gyroscope
        val inputData = Array(sampleSize) { FloatArray(featuresPerSample) }

        for ((index, data) in sensorData.withIndex()) {
            if (index >= sampleSize) break
            inputData[index][0] = data.accelerometer[0]
            inputData[index][1] = data.accelerometer[1]
            inputData[index][2] = data.accelerometer[2]
            inputData[index][3] = data.gyroscope[0]
            inputData[index][4] = data.gyroscope[1]
            inputData[index][5] = data.gyroscope[2]
        }

        return inputData
    }

    private fun resultForHuman(n: Int): String{
        var ris: String = ""
        when (n) {
            1 -> ris = "in piedi"
            2 -> ris = "seduto"
            3 -> ris = "parlo da seduto"
            4 -> ris = "parlo da alzato"
            5 -> ris = "in piedi"
            6 -> ris = "alzando e sedendo"
            7 -> ris = "immobile"
            8 -> ris = "alzalsi e sdraiarsi"
            9 -> ris = "prendere un oggetto"
            10 -> ris = "saltare"
            11 -> ris = "push-up"
            12 -> ris = "camminare"
            13 -> ris = "camminare all'indietro"
            14 -> ris = "camminare in cerchio"
            15 -> ris = "correre"
            16 -> ris = "salire le scale"
            17 -> ris = "scendere le scale"
            18 -> ris = "ping-pong"
        }
        return ris
    }
}