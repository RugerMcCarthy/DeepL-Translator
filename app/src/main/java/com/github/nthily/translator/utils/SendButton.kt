package com.github.nthily.translator.utils

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.github.nthily.translator.UiState

@Composable
fun SendButton(
    viewModel:UiState,
    focus:FocusManager,
    modifier: Modifier = Modifier
){
    IconButton(onClick = {
        viewModel.displayResult = ""
        viewModel.result = ""
        viewModel.temp = viewModel.originWord
        viewModel.getResultWithApi()
        focus.clearFocus()
        viewModel.translating = true
    }, modifier = modifier) {
        Icon(Icons.Filled.Send,null, tint = Color(0xFF0079D3))
    }
}