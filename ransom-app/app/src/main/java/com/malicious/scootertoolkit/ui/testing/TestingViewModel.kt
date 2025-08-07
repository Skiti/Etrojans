package com.malicious.scootertoolkit.ui.testing

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.malicious.scootertoolkit.ble.BleMain

class TestingViewModel : ViewModel() {

    private val _connected = MutableLiveData<Boolean>().apply {
        value = BleMain.getInstance()?.isConnected()
    }

    private val _registered = MutableLiveData<Boolean>().apply {
        value = BleMain.getInstance()?.isRegistered()
    }

    private val _authenticated = MutableLiveData<Boolean>().apply {
        value = BleMain.getInstance()?.isAuthenticated()
    }

    private val _text = MutableLiveData<String>().apply {
        _connected.postValue(BleMain.getInstance()?.isConnected())
        value = if (BleMain.getInstance()?.isConnected() == true) {
            "Device connected!"

        }else {
            "Device not connected!"
        }
    }

    val connected: LiveData<Boolean> = _connected
    val registered: LiveData<Boolean> = _registered
    val authenticated: LiveData<Boolean> = _authenticated
    val text: LiveData<String> = _text

    fun launchRegister(){
        BleMain.getInstance()?.register(onSuccess =
        { complete ->
            if(complete) {
                _text.postValue("Paired")
                _registered.postValue(true)
            }else
                _text.postValue("Press power button after beep") })
    }

    fun launchAuthentication(){
        BleMain.getInstance()?.authentication(onSuccess =
        { complete ->
            if(complete) {
                _text.postValue("Authenticated!")
                _authenticated.postValue(true)
            }else
                _text.postValue("Authenticating...") })
    }

    fun launchTestCommandSN(){
        BleMain.getInstance()?.testCommandSN(onSuccess =
        { response ->
            _text.postValue("Response packet: $response")
        })
    }
}