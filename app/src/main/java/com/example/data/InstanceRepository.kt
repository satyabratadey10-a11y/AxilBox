package com.example.data

import kotlinx.coroutines.flow.Flow

class InstanceRepository(private val instanceDao: InstanceDao) {
    val allInstances: Flow<List<Instance>> = instanceDao.getAllInstances()

    fun getInstance(id: Int): Flow<Instance?> = instanceDao.getInstanceById(id)

    suspend fun insert(instance: Instance): Long = instanceDao.insert(instance)

    suspend fun delete(instance: Instance) = instanceDao.delete(instance)

    suspend fun deleteById(id: Int) = instanceDao.deleteById(id)
}
