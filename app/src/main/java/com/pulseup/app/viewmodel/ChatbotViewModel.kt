package com.pulseup.app.viewmodel

import android.app.Application
import android.util.Log // Import Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.RequestOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.pulseup.app.data.local.PulseUpDatabase
import com.pulseup.app.data.repository.UserRepository
import com.pulseup.app.data.local.entity.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChatMessage(val message: String, val isUser: Boolean, val timestamp: Long = System.currentTimeMillis())

data class ChatbotState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val currentUser: User? = null,
    val error: String? = null
)

class ChatbotViewModel(application: Application) : AndroidViewModel(application) {

    private val database = PulseUpDatabase.getDatabase(application)
    private val userRepository = UserRepository(database.userDao())
    private val auth = Firebase.auth

    private val _chatState = MutableStateFlow(ChatbotState())
    val chatState = _chatState.asStateFlow()

    // ⚠️ PENTING: Ganti dengan API KEY kamu sendiri dari aistudio.google.com
    // Key yang lama mungkin sudah kadaluarsa/limit
    private val API_KEY = "AIzaSyCAEJYrDZxhMsbtnUMiQo13pGZEpYUUbGg"

    private val generativeModel by lazy {
        GenerativeModel(
            modelName = "gemini-2.5-flash",
            apiKey = API_KEY,
            requestOptions = RequestOptions(apiVersion = "v1beta") // Coba ubah versi jika perlu
        )
    }

    init {
        viewModelScope.launch {
            val email = auth.currentUser?.email
            if (email != null) {
                userRepository.getUserByEmail(email).collect { user ->
                    _chatState.update { it.copy(currentUser = user) }
                }
            }
        }
        addBotMessage("Halo! Saya Pulse AI. Ada yang bisa saya bantu? 💪")
    }

    fun sendMessage(userMessage: String) {
        if (userMessage.isBlank()) return

        // 1. Tampilkan pesan user di UI
        _chatState.update {
            it.copy(
                messages = it.messages + ChatMessage(userMessage, true),
                isLoading = true,
                error = null
            )
        }

        viewModelScope.launch {
            try {
                Log.d("ChatbotDebug", "Mengirim pesan ke AI: $userMessage")

                val user = _chatState.value.currentUser
                val userContext = if (user != null) {
                    "Identitas: Pulse AI, asisten kesehatan. User: ${user.username}, BMI ${"%.1f".format(user.calculateBMI())}. "
                } else {
                    "Identitas: Pulse AI, asisten kesehatan. "
                }

                val prompt = "$userContext Jawab singkat: $userMessage"

                // Request ke Google Gemini
                val result = generativeModel.generateContent(prompt)
                val aiResponse = result.text ?: "Maaf, respon kosong."

                Log.d("ChatbotDebug", "Respon diterima: $aiResponse")
                addBotMessage(aiResponse)

            } catch (e: Exception) {
                // INI YANG PENTING: Log error ke Logcat
                Log.e("ChatbotDebug", "Error Gemini: ${e.message}", e)

                // Tampilkan pesan error sebagai balasan bot supaya terlihat di HP
                addBotMessage("Error: ${e.localizedMessage}. Cek koneksi internet atau API Key.")

                _chatState.update {
                    it.copy(isLoading = false, error = e.message)
                }
            }
        }
    }

    private fun addBotMessage(message: String) {
        _chatState.update {
            it.copy(
                messages = it.messages + ChatMessage(message, false),
                isLoading = false
            )
        }
    }
}