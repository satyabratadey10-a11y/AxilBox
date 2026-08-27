package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.Instance
import com.example.data.InstanceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class VMViewModel(
    application: Application,
    private val repository: InstanceRepository
) : AndroidViewModel(application) {

    val instances: StateFlow<List<Instance>> = repository.allInstances
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun getInstance(id: Int): Flow<Instance?> {
        return repository.getInstance(id)
    }

    fun addInstance(
        name: String,
        iconUri: String?,
        osImageUri: String?,
        kernelUri: String?,
        ramMb: Int,
        storageGb: Int,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            repository.insert(
                Instance(
                    name = name.trim(),
                    iconUri = iconUri,
                    osImageUri = osImageUri,
                    kernelUri = kernelUri,
                    ramMb = ramMb,
                    storageGb = storageGb
                )
            )
            onSuccess()
        }
    }

    fun deleteInstance(instance: Instance) {
        viewModelScope.launch {
            repository.delete(instance)
        }
    }

    fun deleteInstanceById(id: Int) {
        viewModelScope.launch {
            repository.deleteById(id)
        }
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(VMViewModel::class.java)) {
                val db = AppDatabase.getDatabase(application)
                val repo = InstanceRepository(db.instanceDao())
                return VMViewModel(application, repo) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
