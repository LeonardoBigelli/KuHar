package com.example.stepbystep

import java.util.LinkedList

class LowPassFilter(private val windowSize: Int) {
    private val predictions: LinkedList<Int> = LinkedList()

    fun addPrediction(prediction: Int): Int {
        // Aggiungi la nuova predizione alla lista
        predictions.add(prediction)

        // Mantieni la finestra della dimensione specificata
        if (predictions.size > windowSize) {
            predictions.poll()
        }

        // Calcola la modalità delle predizioni nella finestra
        return getMode(predictions)
    }

    //funzione che restituisce il risultato filtrato
     fun getMode(predictions: List<Int>): Int {
        // Mappa per contare la frequenza di ogni predizione
        val frequencyMap = mutableMapOf<Int, Int>()

        for (prediction in predictions) {
            frequencyMap[prediction] = frequencyMap.getOrDefault(prediction, 0) + 1
        }

        // Trova la predizione con la frequenza maggiore
        var mode = predictions[0]
        var maxCount = 0

        for ((prediction, count) in frequencyMap) {
            if (count > maxCount) {
                maxCount = count
                mode = prediction
            }
        }

        return mode
    }
}