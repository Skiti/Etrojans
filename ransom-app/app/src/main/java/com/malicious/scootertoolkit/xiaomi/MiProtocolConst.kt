package com.malicious.scootertoolkit.xiaomi

object MiProtocolConst {

    val HEADER : Array<Byte> = arrayOf<Byte>(0x55.toByte(), 0xaa.toByte())
    val TOBMS : Byte = 0x22.toByte()
    val INIT_UPDATE : Byte = 0x07.toByte()
    val CHUNK_UPDATE : Byte = 0x08.toByte()
    val CHECK_UPDATE : Byte = 0x09.toByte()
    val FINISH_UPDATE : Byte = 0x0a.toByte()

}