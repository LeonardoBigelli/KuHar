package com.example.stepbystep

import android.content.Context
import android.os.Bundle
import android.os.Environment
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.io.OutputStreamWriter


class MainActivity : AppCompatActivity() {
    private lateinit var tfliteModel: TensorFlowLiteModel
    private lateinit var displayTextView: TextView
    private lateinit var myButton: Button
    private lateinit var mSensorReader: SensorReaderHelper

    private val mHandler = SensorDataHandler { sensorData ->
        val sampleSize = 200 // Assuming 200 samples for 2 seconds at 100Hz
        val featuresPerSample = 6 // 3 accelerometer + 3 gyroscope
        val inputData = Array(sampleSize) { FloatArray(featuresPerSample) }

        for (i in 0 until sampleSize) {
            inputData[i][0] = sensorData[i * 6 + 0]
            inputData[i][1] = sensorData[i * 6 + 1]
            inputData[i][2] = sensorData[i * 6 + 2]
            inputData[i][3] = sensorData[i * 6 + 3]
            inputData[i][4] = sensorData[i * 6 + 4]
            inputData[i][5] = sensorData[i * 6 + 5]
        }

      //  requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE,READ_EXTERNAL_STORAGE}, 1)
       // writeFileOnInternalStorage(this, "file.txt", inputData.toString())
        generateNoteOnSD("debug.txt", inputData.toString())
        val inference = tfliteModel.runInference(inputData)
        var res = resultForHuman_11(inference.first)
        var confidence_score = inference.second
        if(confidence_score < 0.5){
            res = resultForHuman_11(20)
            confidence_score = 0F
        }
        val file_w = StringBuilder()
        file_w.append(res).append(confidence_score)
        writeToFile(file_w.toString(), this)

        this@MainActivity.runOnUiThread {
           // displayTextView.text = res
            displayTextView.append("\n")
            displayTextView.append(res)
            displayTextView.append(" ")
            displayTextView.append(confidence_score.toString())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //model initialization
        tfliteModel = TensorFlowLiteModel(this)

        mSensorReader = SensorReaderHelper(
            this,
            mHandler,
            200,
            10
        )
        //handler = android.os.Handler(Looper.getMainLooper())
        //button for inference
        myButton = findViewById(R.id.inference_button)
        //textview
        displayTextView = findViewById(R.id.inference_text)
        displayTextView.setMovementMethod(ScrollingMovementMethod())
        displayTextView.text = "unknown"


        // Imposta un listener per il clic del pulsante
        myButton.setOnClickListener {
            if (mSensorReader.isStarted) {
                mSensorReader.stop()
                myButton.text = resources.getText(R.string.btn_stop)
            } else {
                mSensorReader.start()
                myButton.text = resources.getText(R.string.btn_start)
            }
        }

    }

    private fun resultForHuman(n: Int): String = when (n) {
        1 -> "in piedi"
        2 -> "seduto"
        3 -> "parlo da seduto"
        4 -> "parlo da alzato"
        5 -> "alzalsi e sedersi"
        6 -> "sdraiato"
        7 -> "sdraiarsi e alzarzi"
        8 -> "prendere un oggetto"
        9 -> "saltare"
        10 -> "push-up"
        11 -> "sit-up"
        12 -> "camminare"
        13 -> "camminare all'indietro"
        14 -> "camminare in cerchio"
        15 -> "correre"
        16 -> "salire le scale"
        17 -> "scendere le scale"
        18 -> "ping-pong"
        else -> "unknown"
    }

    private fun resultForHuman_11(n: Int): String = when (n) {
        0 -> "in piedi"
        1 -> "seduto"
        2 -> "parlo da seduto"
        3 -> "alzalsi e sedersi"
        4 -> "sdraiato"
        5 -> "prendere un oggetto"
        6 -> "saltare"
        7 -> "camminare"
        8 -> "camminare all'indietro"
        9 -> "camminare in cerchio"
        10 -> "correre"
        else -> "unknown"
    }

    private fun writeToFile(data: String, context: Context) {
        try {
            val outputStreamWriter =
                OutputStreamWriter(context.openFileOutput("config.txt", MODE_PRIVATE))
            outputStreamWriter.append(data)
            outputStreamWriter.close()
        } catch (e: IOException) {
            Log.e("Exception", "File write failed: $e")
        }
    }

    fun writeFileOnInternalStorage(mcoContext: Context, sFileName: String?, sBody: String?) {
        val dir = File(mcoContext.filesDir, "mydir")
        if (!dir.exists()) {
            dir.mkdir()
        }
        try {
            val gpxfile = File(dir, sFileName)
            val writer = FileWriter(gpxfile)
            writer.append(sBody)
            writer.flush()
            writer.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun generateNoteOnSD(sFileName: String?, sBody: String?) {
        try {
            val root = File(Environment.getExternalStorageDirectory(), "Notes")
            if (!root.exists()) {
                root.mkdirs()
            }
            val gpxfile = File(root, sFileName)
            val writer = FileWriter(gpxfile)
            writer.append(sBody)
            writer.flush()
            writer.close()
            Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            e.printStackTrace()
          //  importError = e.message
         //   iError()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mSensorReader.stop()
        tfliteModel.close()
    }
}
