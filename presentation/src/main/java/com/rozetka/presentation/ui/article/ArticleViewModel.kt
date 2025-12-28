package com.rozetka.presentation.ui.article

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rozetka.model.ArticleDetail
import com.rozetka.network.MospolytechMethods
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ArticleUiState {
    object Loading : ArticleUiState
    data class Success(val article: ArticleDetail) : ArticleUiState
    data class Error(val message: String) : ArticleUiState
}

class ArticleViewModel(private val repository: MospolytechMethods ) : ViewModel() {

    private val _uiState = MutableStateFlow<ArticleUiState>(ArticleUiState.Loading)
    val uiState: StateFlow<ArticleUiState> = _uiState.asStateFlow()

    fun loadArticle(url: String) {
        viewModelScope.launch {
            _uiState.value = ArticleUiState.Loading
            val result = repository.getExternalNewsDetail(url)
            if (result != null) {
                _uiState.value = ArticleUiState.Success(result)
            } else {
                _uiState.value = ArticleUiState.Error("Не удалось загрузить содержимое статьи")
            }
        }
    }
}