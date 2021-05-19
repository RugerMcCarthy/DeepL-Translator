package com.github.nthily.translator.utils

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.nthily.translator.R
import com.github.nthily.translator.UiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@ExperimentalFoundationApi
@Composable
fun HistoryListItems(
    viewModel: UiState,
    i: Int,
    scaffoldState: ScaffoldState,
    scope: CoroutineScope
){

    Surface(

        color = Color.White,
        elevation = 5.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ){
            Column(modifier = Modifier.weight(1f)){
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = {

                            },
                            onLongClick = {
                                viewModel.requestCopy.value = viewModel.searchHistorys[i].sourceWord
                                scope.launch {
                                    scaffoldState.snackbarHostState.showSnackbar("翻译结果已复制到剪贴板")
                                }
                            })
                        .padding(15.dp)
                ){
                    Text(
                        text = viewModel.searchHistorys[i].sourceWord,
                        fontWeight = FontWeight.W700,
                        fontSize = 16.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = {

                            },
                            onLongClick = {
                                viewModel.requestCopy.value = viewModel.searchHistorys[i].targetWord
                                scope.launch {
                                    scaffoldState.snackbarHostState.showSnackbar("翻译结果已复制到剪贴板")
                                }
                            })
                        .padding(15.dp)
                ){
                    Text(
                        text = viewModel.searchHistorys[i].targetWord,
                        fontWeight = FontWeight.W700,
                        fontSize = 16.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            IconButton(onClick = { /*TODO*/ }) {
                Icon(painterResource(id = R.drawable.collection), null)
            }
        }
    }
}