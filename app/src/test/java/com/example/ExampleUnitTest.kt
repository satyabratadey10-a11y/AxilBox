package com.example

import com.example.data.DownloadState
import com.example.data.Instance
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun instance_creation_default_values() {
        val instance = Instance(
            name = "Test VM"
        )
        assertEquals("Test VM", instance.name)
        assertNull(instance.iconUri)
        assertNull(instance.osImageUri)
        assertEquals(2048, instance.ramMb)
        assertEquals(16, instance.storageGb)
    }

    @Test
    fun instance_custom_values() {
        val instance = Instance(
            id = 5,
            name = "Alpine Linux",
            iconUri = "content://icon/5",
            osImageUri = "content://file/alpine.iso",
            ramMb = 512,
            storageGb = 8
        )
        assertEquals(5, instance.id)
        assertEquals("Alpine Linux", instance.name)
        assertEquals("content://icon/5", instance.iconUri)
        assertEquals("content://file/alpine.iso", instance.osImageUri)
        assertEquals(512, instance.ramMb)
        assertEquals(8, instance.storageGb)
    }

    @Test
    fun guest_download_states() {
        val progressState = DownloadState.Progress("Downloading Alpine minirootfs...", 45)
        assertEquals(45, progressState.percentage)
        assertEquals("Downloading Alpine minirootfs...", progressState.status)

        val readyState = DownloadState.Ready("/data/kernel", "/data/rootfs.tar.gz")
        assertEquals("/data/kernel", readyState.kernelPath)
        assertEquals("/data/rootfs.tar.gz", readyState.rootfsPath)
    }

    @Test
    fun serial_console_log_splitting() {
        val sampleLog = "[    0.000000] Booting Linux on physical CPU 0x0\n[    0.000000] Linux version 6.6.71-0-virt\n"
        val lines = sampleLog.split("\n").filter { it.isNotEmpty() }
        assertEquals(2, lines.size)
        assertTrue(lines[0].contains("Booting Linux"))
        assertTrue(lines[1].contains("Linux version"))
    }
}
