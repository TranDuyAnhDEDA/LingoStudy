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

data class PopularWordItem(
    val word: String,
    val illustrationType: String
)

val DEFAULT_POPULAR_WORDS = listOf(
    PopularWordItem("apple", "apple"),
    PopularWordItem("house", "house"),
    PopularWordItem("cat", "cat"),
    PopularWordItem("dog", "dog"),
    PopularWordItem("sun", "sun"),
    PopularWordItem("tree", "tree"),
    PopularWordItem("book", "book"),
    PopularWordItem("school", "school")
)

data class DictionaryUiState(
    val searchQuery: String = "",
    val activeQuery: String = "",
    val isLoading: Boolean = false,
    val result: DictionaryResult? = null,
    val errorMessage: String? = null,
    val sampleWords: List<String> = listOf("apple", "house", "cat", "dog", "sun", "tree", "book", "school"),
    val popularWords: List<PopularWordItem> = DEFAULT_POPULAR_WORDS,
    val searchHistory: List<String> = emptyList(),
    val isFavorite: Boolean = false,
    val showAllHistoryDialog: Boolean = false
)

class DictionaryViewModel(
    private val repository: DictionaryRepository = DictionaryRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(DictionaryUiState())
    val uiState: StateFlow<DictionaryUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        refreshLocalData()
    }

    fun refreshLocalData() {
        val history = repository.getSearchHistory()
        val active = _uiState.value.activeQuery
        val isFav = if (active.isNotBlank()) repository.isFavorite(active) else false
        _uiState.update {
            it.copy(
                searchHistory = history,
                isFavorite = isFav
            )
        }
    }

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
                errorMessage = null,
                isFavorite = false
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
                errorMessage = null,
                isFavorite = repository.isFavorite(trimmed)
            )
        }

        searchJob = viewModelScope.launch {
            repository.lookupWord(trimmed).collect { result ->
                result.fold(
                    onSuccess = { dictResult ->
                        val updatedHistory = repository.getSearchHistory().ifEmpty { listOf(trimmed.lowercase()) }
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                result = dictResult,
                                errorMessage = null,
                                searchHistory = updatedHistory,
                                isFavorite = repository.isFavorite(trimmed)
                            )
                        }
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                result = null,
                                errorMessage = error.message ?: "Không tìm thấy từ '$trimmed' trong từ điển.",
                                isFavorite = false
                            )
                        }
                    }
                )
            }
        }
    }

    fun toggleFavorite() {
        val currentWord = _uiState.value.result?.word ?: _uiState.value.activeQuery
        if (currentWord.isBlank()) return
        val newFavStatus = repository.toggleFavorite(currentWord)
        _uiState.update { it.copy(isFavorite = newFavStatus) }
    }

    fun removeHistoryItem(word: String) {
        repository.removeSearchHistory(word)
        _uiState.update {
            it.copy(searchHistory = repository.getSearchHistory())
        }
    }

    fun clearAllHistory() {
        repository.clearSearchHistory()
        _uiState.update {
            it.copy(searchHistory = emptyList(), showAllHistoryDialog = false)
        }
    }

    fun setShowAllHistoryDialog(show: Boolean) {
        _uiState.update { it.copy(showAllHistoryDialog = show) }
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
