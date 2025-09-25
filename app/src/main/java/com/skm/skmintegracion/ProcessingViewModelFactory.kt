package com.skm.skmintegracion

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.skm.skmintegracion.hiopos.data.FinalizeTransactionUseCase
import com.skm.skmintegracion.hiopos.data.HioposSettingsManager

class ProcessingViewModelFactory(
    private val finalizeTransactionUseCase: FinalizeTransactionUseCase,
    private val settingsManager: HioposSettingsManager
) : ViewModelProvider.Factory {
    private val savedStateHandle: SavedStateHandle = SavedStateHandle()

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProcessingViewModel::class.java)) {
            // Si el sistema pide un ProcessingViewModel, lo creamos aquí
            // pasándole la dependencia que necesita.
            @Suppress("UNCHECKED_CAST")
            return ProcessingViewModel(savedStateHandle,finalizeTransactionUseCase,settingsManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}