package com.example.stepbystep

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
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
    private lateinit var modelWeight: TensorFlowLiteModel
    private lateinit var modelHeight: TensorFlowLiteModel
    private lateinit var modelAge: TensorFlowLiteModel
    private lateinit var displayTextView: TextView
    private lateinit var displayWeight: TextView
    private lateinit var displayHeight: TextView
    private lateinit var displayAge: TextView
    private lateinit var myButton: Button
    private lateinit var removeButton: ImageButton
    private lateinit var mSensorReader: SensorReaderHelper

    //array per calcolare la media dei risultati
    private var listWeight = ArrayList<Float>()
    private var listHeight = ArrayList<Float>()
    private var listAge = ArrayList<Float>()

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

        //codice per scrittura finestra su file
        val time = Instant.now().toString()
        val txt = "campioni"
        val txt_final = ".txt"
        val txt_stamp = StringBuilder()
        txt_stamp.append(txt).append(time).append(txt_final)
        scriviArraySuFile(this, inputData, txt_stamp.toString())

        //inferenza attivita'
        val inference = tfliteModel.runInference(inputData)
        var res = resultForHuman_10(inference.first.toInt())
        var confidence_score = inference.second
        if(confidence_score < 0.2){
            res = resultForHuman_11(20)
            confidence_score = 0F
        }

        //calcolo inferenza
   /*     val weight = modelWeight.runInference(reshapeInputData(inputData))
        val height = modelHeight.runInference(reshapeInputData(inputData))
        val age = modelAge.runInference(reshapeInputData(inputData))
        //aggiunta valori nelle relative liste
        listWeight.add(weight.first)
        listHeight.add(height.first)
        listAge.add(age.first) */

        this@MainActivity.runOnUiThread {
            //scrittura dell'inferenza sulla attivita'
           displayTextView.append("\n")
            displayTextView.append(res)
            displayTextView.append(" ")
            displayTextView.append(confidence_score.toString())

            //scrittura dell'inferenza del peso se si sta camminando o correndo
           /* if(inference.first.toInt() == 7 || inference.first.toInt() == 10 || inference.first.toInt() == 8 || inference.first.toInt() == 9){
                //inferenza peso
                val weight = modelWeight.runInference(reshapeInputData(inputData))
                displayWeight.text = weight.first.toString()
            } */

            //scrittura peso, altezza e eta'
   /*         displayWeight.text = "%.2f".format(listWeight.average()).toString()
            displayHeight.text = "%.2f".format(listHeight.average()).toString()
            displayAge.text = listAge.average().toInt().toString()*/
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //model initialization
        tfliteModel = TensorFlowLiteModel(this, "model_act_10_classes.tflite", 10)
     /*   modelWeight = TensorFlowLiteModel(this, "model_weight(mse2_2).tflite", 1)
        modelHeight = TensorFlowLiteModel(this, "model_height(mse5_8).tflite", 1)
        modelAge = TensorFlowLiteModel(this, "model_age(mse1_5).tflite", 1)*/


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
            10 //periodo = 1000(1s) / Hz . tempo tra un campione e l'altro
        )
        //handler = android.os.Handler(Looper.getMainLooper())
        //button for inference
        myButton = findViewById(R.id.inference_button)
        removeButton = findViewById(R.id.remove)
        //textview per inferenza attivita'
        displayTextView = findViewById(R.id.inference_text)
        displayTextView.setMovementMethod(ScrollingMovementMethod())
        displayTextView.text = "unknown"

        //textview per peso
        displayWeight = findViewById(R.id.textWeight)
        //textview altezza
        displayHeight = findViewById(R.id.textHeight)
        //textview eta'
        displayAge = findViewById(R.id.textAge)

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

        //codice bottone per svuotare le liste
        removeButton.setOnClickListener {
            listWeight.clear()
            listHeight.clear()
            listAge.clear()
            displayAge.text = "Età"
            displayWeight.text = "Peso"
            displayHeight.text = "Altezza"
        }

    }

    private fun reshapeInputData(inputData: Array<FloatArray>): Array<FloatArray> {
        var data = Array(50) { FloatArray(6) }
       for(i in data.indices){
            data[i][0] = inputData[i * 4][0]
            data[i][1] = inputData[i * 4][1]
            data[i][2] = inputData[i * 4][2]
            data[i][3] = inputData[i * 4][3]
            data[i][4] = inputData[i * 4][4]
            data[i][5] = inputData[i * 4][5]
        } // commentato per usare motionsense

        var data_finale = Array(50) { FloatArray(6) }
        //inverto assi acceleromentro e giroscopio
        for(i in data.indices){
            data_finale[i][0] = data[i][3]
            data_finale[i][1] = data[i][4]
            data_finale[i][2] = data[i][5]
            data_finale[i][3] = data[i][0]
            data_finale[i][4] = data[i][1]
            data_finale[i][5] = data[i][2]
        }

        return data_finale
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

    private fun resultForHuman_10(n: Int): String = when (n) {
        0 -> "in piedi"
        1 -> "seduto"
        2 -> "parlo da seduto"
        3 -> "alzalsi e sedersi"
        4 -> "sdraiato"
        5 -> "saltare"
        6 -> "camminare"
        7 -> "camminare all'indietro"
        8 -> "camminare in cerchio"
        9 -> "correre"
        else -> "unknown"
    }

    //funzione per il modello di realWorld 2016
    private fun resultForHuman_realWorld2016(n: Int): String = when (n) {
        0 -> "stare in piedi"
        1 -> "stare sdraiato"
        2 -> "stare seduto"
        3 -> "saltare"
        4 -> "arrampicarsi verso l'alto"
        5 -> "arrampicarsi verso il basso"
        6 -> "camminare"
        7 -> "correre"
        else -> "unknown"
    }

    //funzione per motionsense
    private fun resultForHuman_motionSense(n: Int): String = when (n) {
        0 -> "corsa"
        1 -> "camminata"
        2 -> "scendere le scale"
        3 -> "salire le scale"
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
        modelHeight.close()
        modelAge.close()
        modelWeight.close()

    }
}
