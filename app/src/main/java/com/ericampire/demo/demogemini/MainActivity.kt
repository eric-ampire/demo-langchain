package com.ericampire.demo.demogemini

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.ai.client.generativeai.GenerativeModel
import com.ericampire.demo.demogemini.ui.theme.DemoaiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DemoaiTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    val generativeModel = GenerativeModel(
                        modelName = "gemini-pro",
                        apiKey = BuildConfig.apiKey
                    )
                    val viewModel = SummarizeViewModel(generativeModel)
                    SummarizeRoute(viewModel)
                }
            }
        }
    }
}

@Composable
internal fun SummarizeRoute(
    summarizeViewModel: SummarizeViewModel = viewModel()
) {
    val summarizeUiState by summarizeViewModel.uiState.collectAsState()

    SummarizeScreen(
        uiState = summarizeUiState,
        onSummarizeClicked = { inputText -> summarizeViewModel.summarize(inputText) },
        onModelSelected = summarizeViewModel::onModelSelected
    )
}

@Composable
fun SummarizeScreen(
    uiState: SummaryState,
    onSummarizeClicked: (String) -> Unit = {},
    onModelSelected: (Model) -> Unit
) {
    var prompt by remember { mutableStateOf("") }
    var isModelSelectorExpended by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .padding(all = 8.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    isModelSelectorExpended = true
                },
            verticalAlignment = Alignment.CenterVertically,
            content = {
                Text(
                    modifier = Modifier.padding(14.dp),
                    text = "Selected Model : ${uiState.selectedModel.llmName}"
                )
                AiModelSelector(
                    isExpended = isModelSelectorExpended,
                    onDismissRequest = { isModelSelectorExpended = false },
                    onModelSelected = {
                        isModelSelectorExpended = false
                        onModelSelected(it)
                    }
                )
            }
        )
        Row {
            OutlinedTextField(
                value = prompt,
                label = { Text(stringResource(R.string.summarize_label)) },
                placeholder = { Text(stringResource(R.string.summarize_hint)) },
                onValueChange = { prompt = it },
                modifier = Modifier
                    .weight(8f)
            )
            TextButton(
                onClick = {
                    if (prompt.isNotBlank()) {
                        onSummarizeClicked(prompt)
                    }
                },

                modifier = Modifier
                    .weight(2f)
                    .padding(all = 4.dp)
                    .align(Alignment.CenterVertically)
            ) {
                Text(stringResource(R.string.action_go))
            }
        }
        when (val loadingState = uiState.loadingState) {
            LoadingState.Initial -> {
                // Nothing is shown
            }

            LoadingState.Loading -> {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .padding(all = 8.dp)
                        .align(Alignment.CenterHorizontally)
                ) {
                    CircularProgressIndicator()
                }
            }

            is LoadingState.Success -> {
                Row(modifier = Modifier.padding(all = 8.dp)) {
                    Icon(
                        Icons.Outlined.Person,
                        contentDescription = "Person Icon"
                    )
                    Text(
                        text = loadingState.outputText,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }

            is LoadingState.Error -> {
                Text(
                    text = loadingState.errorMessage,
                    color = Color.Red,
                    modifier = Modifier.padding(all = 8.dp)
                )
            }
        }
    }
}

enum class Model(val llmName: String) {
    OpenAi(llmName = "Open Ai"),
    Gemini(llmName = "Gemini"),
    MistralAi(llmName = "Mistral Ai")
}

@Composable
fun AiModelSelector(
    modifier: Modifier = Modifier,
    isExpended: Boolean,
    onDismissRequest: () -> Unit,
    onModelSelected: (Model) -> Unit
) {
    DropdownMenu(
        modifier = modifier,
        expanded = isExpended,
        onDismissRequest = onDismissRequest,
        content = {
            Model.entries.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item.llmName) },
                    onClick = {
                        onModelSelected(item)
                    }
                )
            }
        }
    )
}