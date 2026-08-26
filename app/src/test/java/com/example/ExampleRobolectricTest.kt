package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.Instance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ExampleRobolectricTest {

    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("VM Manager", appName)
    }

    @Test
    fun `insert and retrieve instance from database`() = runBlocking {
        val dao = db.instanceDao()
        val instance = Instance(
            name = "Alpine Linux Test",
            iconUri = null,
            osImageUri = "content://media/external/file/123",
            ramMb = 2048,
            storageGb = 16
        )
        val id = dao.insert(instance)
        val instances = dao.getAllInstances().first()

        assertEquals(1, instances.size)
        assertEquals("Alpine Linux Test", instances[0].name)
        assertEquals(2048, instances[0].ramMb)
        assertEquals(16, instances[0].storageGb)
    }
}
