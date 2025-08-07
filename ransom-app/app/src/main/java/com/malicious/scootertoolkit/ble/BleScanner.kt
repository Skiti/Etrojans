package com.malicious.scootertoolkit.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.*
import android.util.Log
import com.malicious.scootertoolkit.ble.components.ScannedDevice
import com.malicious.scootertoolkit.ui.home.components.DeviceAdapter
import java.lang.NullPointerException


@SuppressLint("MissingPermission")
class BleScanner(
    private val bluetoothAdapter: BluetoothAdapter,
    private val deviceAdapter: DeviceAdapter
) {
    companion object{
        private const val TAG = "BleScanner"
        private const val MIN_RSSI = -85
    }

    private var isScanning = false

    private var runnable = Runnable {
        isScanning = false
        bluetoothAdapter.bluetoothLeScanner.stopScan(scanCallback)
        Log.d(TAG, "Scan Timeout")
    }


    fun startScan() {
        isScanning = true
        bluetoothAdapter.bluetoothLeScanner.startScan(scanCallback)
        Log.d(TAG, "Scan is started")
    }

    fun stopScan() {
        isScanning = false
        bluetoothAdapter.bluetoothLeScanner.stopScan(scanCallback)
        Log.d(TAG, "Scan is terminated")
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            result?.let { it ->
                Log.d(TAG, "find : ${result.device}")

                if (it.rssi < MIN_RSSI) {
                    Log.d(TAG, "rssi (${it.rssi}) is too week. return")
                    return
                }

                if (it.scanRecord == null) {
                    Log.d(TAG, "This device does not have scanRecord. return")
                    return
                }

                //TODO(implement automatic attackable scooter detection, use isScooter variable)
                //TODO(implement automatic scooter detection to decide BMS firmware version to flash)

                Log.d(
                    TAG,
                    "device : ${it.device}, scanrecord : ${it.scanRecord}, rssi : ${it.rssi}"
                )

                var seclevel = -1
                try {
                    try {
                        seclevel = it.scanRecord!!.manufacturerSpecificData.valueAt(0)[1].toInt()
                    } catch (_: ArrayIndexOutOfBoundsException) {}
                }catch (_: NullPointerException){}

                var deviceName = it.device.name
                if (deviceName == null)
                    deviceName = ""

                deviceAdapter.update(ScannedDevice(it.device, deviceName, it.rssi, seclevel, true))

            }
        }
    }
}