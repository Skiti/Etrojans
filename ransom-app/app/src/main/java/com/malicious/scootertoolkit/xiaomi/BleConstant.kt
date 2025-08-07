package com.malicious.scootertoolkit.xiaomi

import java.util.UUID

object BleConstant {

    val UART_service: UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e")
    val UART_RX_characteristic: UUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e")//write
    val UART_TX_characteristic: UUID = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e")//read

    val AUTH_service: UUID = UUID.fromString("0000fe95-0000-1000-8000-00805f9b34fb")
    val AUTH_UNK1_characteristic: UUID = UUID.fromString("00000004-0000-1000-8000-00805f9b34fb")
    val AUTH_UPNP_characteristic: UUID = UUID.fromString("00000010-0000-1000-8000-00805f9b34fb")
    val AUTH_AVDTP_characteristic: UUID = UUID.fromString("00000019-0000-1000-8000-00805f9b34fb")

    const val NO_SECURITY = 0  //for clear protocols (1)
    const val XOR_SECURITY = 1  //for xiaomi xored 55AB protocol (3)
    const val ENCRYPT_SECURITY = 2 //for ninebot 5AA5 (4) and xiaomi 55AB (5) encrypted protocols
}