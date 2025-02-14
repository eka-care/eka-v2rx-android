package com.eka.voice2rx_sdk.common

sealed class ResponseState {
    object Loading : ResponseState()
    data class Success(val isCompleted : Boolean) : ResponseState()
    data class Error(val error : String) : ResponseState()
}