package com.example

import com.example.data.Instance
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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
            name = "Debian 12",
            iconUri = "content://icon/5",
            osImageUri = "content://file/debian.iso",
            ramMb = 4096,
            storageGb = 32
        )
        assertEquals(5, instance.id)
        assertEquals("Debian 12", instance.name)
        assertEquals("content://icon/5", instance.iconUri)
        assertEquals("content://file/debian.iso", instance.osImageUri)
        assertEquals(4096, instance.ramMb)
        assertEquals(32, instance.storageGb)
    }
}
