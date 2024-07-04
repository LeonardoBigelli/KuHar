package com.example.stepbystep

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.time.Instant


class MainActivity : AppCompatActivity() {
    private lateinit var tfliteModel: TensorFlowLiteModel
    private lateinit var displayTextView: TextView
    private lateinit var myButton: Button
    private lateinit var mSensorReader: SensorReaderHelper

    @RequiresApi(Build.VERSION_CODES.O)
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

        val time = Instant.now().toString()
        val txt = "campioni"
        val txt_final = ".txt"
        val txt_stamp = StringBuilder()
        txt_stamp.append(txt).append(time).append(txt_final)
        scriviArraySuFile(this, inputData, txt_stamp.toString())
        val inference = tfliteModel.runInference(inputData)
        var res = resultForHuman_11(inference.first)
        var confidence_score = inference.second
        if(confidence_score < 0.5){
            res = resultForHuman_11(20)
            confidence_score = 0F
        }


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

        val permission = Manifest.permission.WRITE_EXTERNAL_STORAGE
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), 112)
            var msg = "permesso di scrittura concesso"
            Toast.makeText(this, msg, msg.length)
        }else {
            var msg = "permesso di scrittura NON concesso"
            Toast.makeText(this, msg, msg.length)
            verificaPermessiScrittura()
        }

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


    fun scriviArraySuFile(context: Context, arrayDati: Array<FloatArray>, nomeFile: String) {
        val fileOutput =  File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), nomeFile)
        PrintWriter(fileOutput).use { writer ->
            for (riga in arrayDati) {
                val linea = riga.joinToString(" ") { it.toString() }
                writer.println(linea)
            }
        }
        Log.i("write", context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).toString())
    }

    private fun verificaPermessiScrittura() {
        val hasWritePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (hasWritePermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 112)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 112) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permesso concesso, puoi procedere con l'operazione di scrittura
                var msg = "permesso di scrittura concesso"
                Toast.makeText(this, msg, msg.length)
            } else {
                // Permesso negato, mostra un messaggio o gestisci di conseguenza
                var msg = "permesso di scrittura NON concesso"
                Toast.makeText(this, msg, msg.length)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mSensorReader.stop()
        tfliteModel.close()
    }
}
