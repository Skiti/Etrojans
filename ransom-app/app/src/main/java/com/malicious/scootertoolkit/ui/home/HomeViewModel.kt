package com.malicious.scootertoolkit.ui.home

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.malicious.scootertoolkit.R
import com.malicious.scootertoolkit.ble.BleMain
import com.malicious.scootertoolkit.ui.home.components.DeviceAdapter
import kotlin.coroutines.coroutineContext

class HomeViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Searching for attackable scooters..."
    }
    val text: LiveData<String> = _text

    private lateinit var deviceList: DeviceAdapter

    fun selectDevice(view: View, position: Int) : Boolean? {
        val item = deviceList.getItem(position)
        if (item != null) {
            return BleMain.getInstance()?.createBleDevice(item.getDevice(), item.getDisplayName())
            }
        return false
    }


    fun setDeviceAdapter(context: Context){
        deviceList = DeviceAdapter(context, R.layout.listitem_device, ArrayList())
        BleMain.getInstance()?.getScanner(deviceList)?.startScan()
    }

    fun getdeviceList(): DeviceAdapter{ return deviceList }
}