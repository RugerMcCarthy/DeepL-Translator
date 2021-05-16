package com.github.nthily.translator

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.launch

@ExperimentalMaterialApi
@Composable
fun SelectLang(
    viewModel:UiState,
    state: ModalBottomSheetState
){
    val listState = rememberLazyListState()

    LazyColumn(state = listState){
        when(viewModel.langMode){
            0 -> {
                items(viewModel.allLangs.size){ item ->
                    ListItem(viewModel.allLangs[item].first,viewModel.allLangs[item].second, viewModel, item, state)
                }
            }
            else -> {
                for(item in 1 until viewModel.allLangs.size){
                    item{
                        ListItem(viewModel.allLangs[item].first,viewModel.allLangs[item].second, viewModel, item, state)
                    }
                }
            }
        }
    }

    LaunchedEffect(state.currentValue == ModalBottomSheetValue.Hidden){
        listState.animateScrollToItem(0, 0)
    }
}

@ExperimentalMaterialApi
@Composable
fun ListItem(
    first:String,
    second:String,
    viewModel: UiState,
    index: Int,
    state: ModalBottomSheetState
){

    val scope = rememberCoroutineScope()

    ListItem(
        text = {
            Text(
                text = first,
                fontWeight = FontWeight.W700
            )
        },
        modifier = Modifier.clickable {
            when(viewModel.langMode){
                0 -> viewModel.sourceLanguage = viewModel.allLangs[index]
                else -> viewModel.targetLanguage = viewModel.allLangs[index]
            }
            scope.launch {
                state.hide()
            }
        },
        trailing = {
            Text(
                text = second,
                fontWeight = FontWeight.W700
            )
        }
    )
}

