package com.malicious.scootertoolkit.xiaomi

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.util.Log
import com.malicious.scootertoolkit.ble.BleManagerAdapter
import com.malicious.scootertoolkit.xiaomi.miauth.IDevice
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.functions.Consumer
import java.util.ArrayList
import java.util.UUID

class ScooterDevice(context: Context, private val device: BluetoothDevice, private val name: String) : IDevice {

    companion object{
        private const val TAG = "ScooterDevice"
    }

    private val manager : BleManagerAdapter = BleManagerAdapter(context)
    private var isRegistered : Boolean = false
    private var isAuthenticated : Boolean = false

    init{
        //manager.connect(device).useAutoConnect(true).enqueue()
        //Only for testing
    }

    override fun prepare() {
        Log.d(TAG,"Prepare function")
    }

    override fun connect(onConnect: Consumer<Boolean>?) {
        Log.d(TAG,"connect function")
        manager.connect(device).useAutoConnect(true).enqueue()
        onConnect?.accept(manager.isConnected)
    }

    fun connect() {
        Log.d(TAG,"connect function")
        manager.connect(device).useAutoConnect(true).enqueue()
    }

    override fun disconnect() {
        Log.d(TAG,"disconnect function")
        manager.disconnect().enqueue()
        //TODO(implement token save)
        isRegistered = false
        isAuthenticated = false
    }

    override fun isConnected(): Boolean {
        Log.d(TAG,"isConnected function")
        return manager.isConnected
    }

    override fun write(uuid: UUID?, data: ByteArray?, onWriteSuccess: Consumer<ByteArray>?) {
        Log.d(TAG,"write function")
        manager.write(uuid, data,onWriteSuccess)
    }

    override fun write(uuid: UUID?, data: ArrayList<ByteArray>?, onWriteSuccess: Consumer<ByteArray>?) {
        Log.d(TAG,"write function")
        manager.write(uuid, data,onWriteSuccess)
    }

    /**
     * Method not used
     */
    override fun read(uuid: UUID?, onReadSuccess: Consumer<ByteArray>?, onReadFail: Consumer<Throwable>?) {
        Log.d(TAG,"read function")
        manager.read(uuid)
    }

    override fun onNotify(uuid: UUID?): Observable<ByteArray> {
        Log.d(TAG,"onNotify function")
        return manager.onNotify(uuid)
    }

    override fun isDisconnected(): Boolean {
        Log.d(TAG,"isDisconnected function")
        return !manager.isConnected
    }

    override fun onDisconnect(onDisconnect: Consumer<Boolean>?) {
        Log.d(TAG,"onDisconnect function")
        if (onDisconnect != null) {
            return onDisconnect.accept(!manager.isConnected)
        }
    }

    override fun getName(): String {
        Log.d(TAG,"getName function")
        return name
    }

    fun setRegistered(){
        Log.d(TAG,"setRegistered function")
        isRegistered = true
    }

    fun isRegistered() : Boolean{
        Log.d(TAG,"isRegistered function")
        return isRegistered
    }

    fun setAuthenticated(){
        Log.d(TAG,"setAuthenticated function")
        isAuthenticated = true
    }

    fun isAuthenticated() : Boolean{
        Log.d(TAG,"isAuthenticated function")
        return isAuthenticated
    }

    fun getAddress() : String{
        return device.address
    }

}