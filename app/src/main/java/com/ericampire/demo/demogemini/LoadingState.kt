package com.ericampire.demo.demogemini

/**
 * A sealed hierarchy describing the state of the text generation.
 */
sealed interface LoadingState {
    data object Initial : LoadingState
    data object Loading : LoadingState
    data class Success(val outputText: String) : LoadingState
    data class Error(val errorMessage: String) : LoadingState
}

data class SummaryState(
    val selectedModel: Model = Model.MistralAi,
    val previousPrompt: String = "",
    val loadingState: LoadingState = LoadingState.Initial
) {
    companion object {
        val Empty = SummaryState()
    }
}