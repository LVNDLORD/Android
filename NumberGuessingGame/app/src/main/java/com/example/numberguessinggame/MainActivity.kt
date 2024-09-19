package com.example.numberguessinggame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Button
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.numberguessinggame.ui.theme.NumberGuessingGameTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NumberGuessingGameTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NumberGuessingGame(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun NumberGuessingGame(modifier: Modifier = Modifier) {

    val playingRange = remember { NumberGame(1..15) }
    var guessInput by remember { mutableStateOf("") }
    val guessInt = guessInput.toIntOrNull() ?: 0
    var guessSubmitted by remember { mutableStateOf<GuessResult?>(null) }
    var isError by remember { mutableStateOf(false) }


    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .statusBarsPadding()
                .padding(horizontal = 40.dp)
                .verticalScroll(rememberScrollState())
                .safeDrawingPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                stringResource(
                    R.string.guess_the_number_in_given_range,
                    playingRange.range.first,
                    playingRange.range.last
                )
            )
            TextField(
                label = { Text(stringResource(R.string.enter_number_label)) },
                value = guessInput,
                onValueChange = { guessInput = it },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = modifier
            )

            Button(
                onClick = {
                    guessSubmitted = playingRange.makeGuess(guessInt)
                    if (guessInt in playingRange.range) {
                        isError = false
                    } else {
                        isError = true
                    }
                    guessInput = ""
                }
            ) {
                Text(
                    stringResource(R.string.make_guess_button),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            Spacer(modifier = Modifier.height(15.dp))

            if (isError) {
                Text(
                    stringResource(
                        R.string.error_number_out_of_range,
                        playingRange.range.first,
                        playingRange.range.last
                    )
                )
            } else {
                guessSubmitted?.let {
                    Text(
                        text = when (it) {
                            GuessResult.HIGH -> stringResource(R.string.guess_result_high)
                            GuessResult.LOW -> stringResource(R.string.guess_result_low)
                            GuessResult.HIT -> {
                                stringResource(R.string.guess_result_hit, playingRange.guesses)
                            }
                        },
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }


        }
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    NumberGuessingGameTheme {
        NumberGuessingGame()
    }
}