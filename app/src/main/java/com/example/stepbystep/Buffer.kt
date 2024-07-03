package com.example.stepbystep

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.Semaphore
import java.util.concurrent.locks.ReentrantLock

class Buffer (context: Context){
    //window
    private val sensorDataList = mutableListOf<SensorData>()
    //mutex per la mutua esclusione
    private val mutex = ReentrantLock()

    //semaforo per bloccare il Produttore
    private val notFull = Semaphore(200)

    //semaforo che tiene bloccato il Consumatore
    private val notEmpy = Semaphore(0)// parte da vuoto

    //funzione per inserire un elemento
    suspend fun put(data: SensorData){
        //acquisisco un permesso
        try{
            withContext(Dispatchers.IO) {
                notFull.acquire()
            }
            //buffer libero
            mutex.lock()
            sensorDataList.add(data)
            if(sensorDataList.size == 200){
                notEmpy.release()
            }
        }catch (e: InterruptedException){

        }finally {
            mutex.unlock()
        }
    }// end put()

    suspend fun remove(): List<SensorData>{
        var data = mutableListOf<SensorData>()
        var i = 200
        //se è pieno il buffer
        try{
            notEmpy.acquire()

            mutex.lock()
            data = sensorDataList.toList().toMutableList()
            sensorDataList.clear()
            //ripristino tutti i permessi, il buffer è libero del tutto
            notFull.release(i)
        }catch (e: InterruptedException){

        }finally {
            mutex.unlock()
        }
        return data
    }
}