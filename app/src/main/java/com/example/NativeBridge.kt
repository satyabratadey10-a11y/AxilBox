package com.example

object NativeBridge {
    private var isLoaded = false

    init {
        try {
            System.loadLibrary("native_lib")
            isLoaded = true
        } catch (t: Throwable) {
            isLoaded = false
        }
    }

    external fun stringFromJNI(): String
    external fun startVm(
        qemuPath: String,
        kernelPath: String,
        initrdPath: String,
        cmdline: String,
        memoryMb: Int
    ): Boolean
    external fun readSerialOutput(): String
    external fun sendSerialInput(input: String): Boolean
    external fun stopVm()
    external fun isVmRunning(): Boolean

    fun getNativeStatus(): String {
        return if (isLoaded) {
            try {
                stringFromJNI()
            } catch (t: Throwable) {
                "native call error: ${t.message}"
            }
        } else {
            "native library not loaded"
        }
    }

    fun isNativeLoaded(): Boolean = isLoaded
}
