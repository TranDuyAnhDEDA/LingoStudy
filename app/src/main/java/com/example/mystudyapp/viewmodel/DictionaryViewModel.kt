package com.example.mystudyapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mystudyapp.data.model.DictionaryResult
import com.example.mystudyapp.data.repository.DictionaryRepository
import com.example.mystudyapp.data.repository.DictionaryRepositoryImpl
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DictionaryUiState(
    val searchQuery: String = "",
    val activeQuery: String = "",
    val isLoading: Boolean = false,
    val result: DictionaryResult? = null,
    val errorMessage: String? = null,
    val sampleWords: List<String> = listOf("hello", "companion", "curriculum", "journey", "vocabulary", "explore")
)

class DictionaryViewModel(
    private val repository: DictionaryRepository = DictionaryRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(DictionaryUiState())
    val uiState: StateFlow<DictionaryUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    fun onQueryChanged(newQuery: String) {
        _uiState.update { it.copy(searchQuery = newQuery) }
    }

    fun clearQuery() {
        searchJob?.cancel()
        _uiState.update {
            it.copy(
                searchQuery = "",
                activeQuery = "",
                isLoading = false,
                result = null,
                errorMessage = null
            )
        }
    }

    fun searchWord(queryToSearch: String = _uiState.value.searchQuery) {
        val trimmed = queryToSearch.trim()
        if (trimmed.isBlank()) return

        val currentState = _uiState.value
        if (currentState.isLoading && currentState.activeQuery.equals(trimmed, ignoreCase = true)) {
            return
        }

        searchJob?.cancel()

        _uiState.update {
            it.copy(
                searchQuery = trimmed,
                activeQuery = trimmed,
                isLoading = true,
                errorMessage = null
            )
        }

        searchJob = viewModelScope.launch {
            repository.lookupWord(trimmed).collect { result ->
                result.fold(
                    onSuccess = { dictResult ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                result = dictResult,
                                errorMessage = null
                            )
                        }
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                result = null,
                                errorMessage = error.message ?: "Failed to find dictionary definition."
                            )
                        }
                    }
                )
            }
        }
    }
}

class DictionaryViewModelFactory(
    private val repository: DictionaryRepository = DictionaryRepositoryImpl()
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return DictionaryViewModel(repository) as T
    }
}
