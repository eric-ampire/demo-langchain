package com.ericampire.demo.demogemini

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.GenerationConfig
import com.google.auth.oauth2.AccessToken
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.vertexai.VertexAI
import dev.langchain4j.chain.ConversationalChain
import dev.langchain4j.data.message.SystemMessage
import dev.langchain4j.data.message.UserMessage
import dev.langchain4j.memory.chat.MessageWindowChatMemory
import dev.langchain4j.model.mistralai.MistralAiChatModel
import dev.langchain4j.model.openai.OpenAiChatModel
import dev.langchain4j.model.vertexai.VertexAiGeminiChatModel
import dev.langchain4j.model.vertexai.VertexAiGeminiChatModel.VertexAiGeminiChatModelBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SummarizeViewModel(
    private val googleModel: GenerativeModel
) : ViewModel() {

    private val openAiModel = OpenAiChatModel.builder()
        .apiKey(AiToken.OpenAiToken)
        .modelName("gpt-4-turbo-preview")
        .build()

    private val mistralAiModel = MistralAiChatModel.builder()
        .apiKey(AiToken.MistralAiToken)
        .build()

    private val models = mutableMapOf(
        Model.OpenAi to openAiModel,
        Model.MistralAi to mistralAiModel
    )

    private val _uiState = MutableStateFlow(SummaryState.Empty)
    val uiState: StateFlow<SummaryState> = _uiState.asStateFlow()
    private var previousQuestion = ""

    fun summarize(inputText: String) {
        _uiState.update { it.copy(loadingState = LoadingState.Loading) }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val selectedModel = models[uiState.value.selectedModel]
                val memoryWindow = MessageWindowChatMemory.withMaxMessages(10)
                val chain = ConversationalChain.builder()
                    .chatLanguageModel(selectedModel)
                    .chatMemory(memoryWindow)
                    .build()

                    if (previousQuestion.isNotEmpty()) {
                        chain.execute(previousQuestion)
                    }

                selectedModel?.let {
                    previousQuestion = inputText
                    val response = chain.execute(inputText)
                    _uiState.update { it.copy(loadingState = LoadingState.Success(response)) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(loadingState = LoadingState.Error(e.localizedMessage ?: "")) }
            }
        }
    }

    fun onModelSelected(model: Model) {
        _uiState.update { it.copy(selectedModel = model) }
    }
}














//val generativeModel = GenerativeModel(
//    modelName = "gemini-pro",
//    apiKey = AiToken.GoogleAiToken
//)
//
//private val mistralModel = MistralAiChatModel.builder()
//    .apiKey(AiToken.MistralAiToken)
//    .build()
//
//private val openAiModel = OpenAiChatModel.builder()
//    .apiKey(AiToken.OpenAiToken)
//    .modelName("gpt-4-turbo-preview")
//    .build()
//
//private val geminiModel = VertexAiGeminiChatModel.builder()
//    .project("bubbly-dominion-285208")
//    .location("us-central8")
//    .modelName("gemini-pro")
//
//private val models = mutableMapOf(
//    //Model.Gemini to geminiModel,
//    Model.OpenAi to openAiModel,
//    Model.MistralAi to mistralModel
//)
//fun chatMemory() {
//    val model = models[uiState.value.selectedModel]
//    // val prompt = SystemMessage.from("I'm the king of the UK talk to me with respect")
//    val userMessage = UserMessage.from(inputText)
//
//    val chatMemory = MessageWindowChatMemory.withMaxMessages(10)
//    val chain = ConversationalChain.builder()
//        .chatLanguageModel(models[uiState.value.selectedModel])
//        .chatMemory(chatMemory)
//        .build()
//
//    if (previousQuestion.isNotEmpty()) {
//        chain.execute(previousQuestion)
//    }
//    val response = chain?.execute(/*prompt, */inputText)
//    //val response = model?.generate(/*prompt, */inputText)
//
//    response?.let {
//        previousQuestion = it
//        _uiState.update { stats -> stats.copy(loadingState = LoadingState.Success(it)) }
//    }
//}