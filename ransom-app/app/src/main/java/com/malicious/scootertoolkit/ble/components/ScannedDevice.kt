package com.malicious.scootertoolkit.ble.components

import android.bluetooth.BluetoothDevice

class ScannedDevice(device: BluetoothDevice, displayName: String, rssi: Int, securityLevel: Int, isScooter: Boolean) {

    private val device: BluetoothDevice
    private var rssi: Int
    private val displayName: String
    private val securityLevel: Int
    private val isScooter: Boolean

    init {
        this.device = device
        this.displayName = displayName
        this.rssi = rssi
        this.securityLevel = securityLevel
        this.isScooter = isScooter
    }

    fun getDevice(): BluetoothDevice{ return this.device }
    fun getRssi(): Int { return this.rssi }
    fun setRssi(rssi: Int) { this.rssi = rssi }
    fun getDisplayName(): String { return this.displayName }
    fun getSecurityLevel(): Int { return this.securityLevel }
    fun isScooter(): Boolean { return this.isScooter }
}