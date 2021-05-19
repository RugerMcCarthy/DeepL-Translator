package com.github.nthily.translator

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@ExperimentalMaterialApi
@Composable
fun SelectLang(
    viewModel:UiState,
    state: ModalBottomSheetState
){
    val listState = rememberLazyListState()
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(state = listState, modifier = Modifier.clip(RoundedCornerShape(10.dp))){
            when(viewModel.langMode){
                SelectLanguageMode.SOURCE -> {
                    for(item in viewModel.allLangs.indices){
                        if(viewModel.allLangs[item] == viewModel.targetLanguage) continue
                        item{
                            ListItem(viewModel.allLangs[item].first,viewModel.allLangs[item].second, viewModel, item, state)
                            if (item != viewModel.allLangs.size) {
                                Divider(thickness = 0.8.dp)
                            }
                        }
                    }
                }
                SelectLanguageMode.TARGET -> {
                    for(item in 1 until viewModel.allLangs.size){
                        if(viewModel.allLangs[item] == viewModel.sourceLanguage) continue
                        item{
                            ListItem(viewModel.allLangs[item].first,viewModel.allLangs[item].second, viewModel, item, state)
                            if (item != viewModel.allLangs.size) {
                                Divider(thickness = 0.8.dp)
                            }
                        }
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
                SelectLanguageMode.SOURCE -> viewModel.sourceLanguage = viewModel.allLangs[index]
                SelectLanguageMode.TARGET -> viewModel.targetLanguage = viewModel.allLangs[index]
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

