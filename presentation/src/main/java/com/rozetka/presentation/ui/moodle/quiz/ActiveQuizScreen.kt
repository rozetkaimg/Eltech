package com.rozetka.presentation.ui.moodle.quiz

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.rozetka.model.QuestionType
import com.rozetka.model.QuizOption
import com.rozetka.model.QuizQuestion
import com.rozetka.presentation.ui.pay.LoadingState
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveQuizScreen(
    attemptUrl: String,
    quizTitle: String,
    onBackClick: () -> Unit,
    viewModel: ActiveQuizViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(attemptUrl) {
        viewModel.loadAttempt(attemptUrl)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(quizTitle, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) }
                }
            )
        },
        bottomBar = {
            if (uiState is ActiveQuizUiState.Active) {
                Surface(tonalElevation = 8.dp, modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { viewModel.submitTest() },
                        modifier = Modifier.padding(16.dp).fillMaxWidth().height(56.dp)
                    ) {
                        Text("Завершить тест", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val state = uiState) {
                is ActiveQuizUiState.Loading -> LoadingState()
                is ActiveQuizUiState.Finished -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Ответы успешно отправлены!", style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = onBackClick) { Text("Вернуться к курсу") }
                    }
                }
                is ActiveQuizUiState.Error -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(state.message, color = MaterialTheme.colorScheme.error)
                        Button(onClick = onBackClick, modifier = Modifier.padding(top = 16.dp)) { Text("Назад") }
                    }
                }
                is ActiveQuizUiState.Active -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(state.attempt.questions) { question ->
                            QuestionCard(
                                question = question,
                                selectedAnswers = state.selectedAnswers,
                                onOptionSelected = { option ->
                                    // Вытаскиваем имя поля для sequencecheck (Moodle формат: q[attempt]:[slot]..._:sequencecheck)
                                    val sqName = option.name.substringBefore("_") + "_:sequencecheck"
                                    viewModel.selectAnswer(option.name, option.value, sqName, question.sequenceCheck)
                                }
                            )
                        }
                        item { Spacer(Modifier.height(80.dp)) } // Отступ для нижней кнопки
                    }
                }
            }
        }
    }
}

@Composable
private fun QuestionCard(
    question: QuizQuestion,
    selectedAnswers: Map<String, String>,
    onOptionSelected: (QuizOption) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = "Вопрос ${question.slot}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = question.text,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))

            question.options.forEach { option ->
                val isSelected = selectedAnswers[option.name] == option.value

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOptionSelected(option) }
                        .padding(vertical = 8.dp)
                ) {
                    if (question.type == QuestionType.SINGLE_CHOICE) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { onOptionSelected(option) },
                            colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                        )
                    } else {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = { onOptionSelected(option) },
                            colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = option.text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}