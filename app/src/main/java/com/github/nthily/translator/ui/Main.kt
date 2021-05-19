package com.github.nthily.translator.ui
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.renderscript.ScriptGroup
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.nthily.translator.R
import com.github.nthily.translator.SelectLang
import com.github.nthily.translator.SelectLanguageMode
import com.github.nthily.translator.UiState
import com.github.nthily.translator.data.TranslateData
import com.github.nthily.translator.ui.theme.TranslatorTheme
import com.github.nthily.translator.utils.HistoryListItems
import com.github.nthily.translator.utils.SendButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch



@ExperimentalFoundationApi
@ExperimentalAnimationApi
@ExperimentalMaterialApi
@Composable
fun Input(
    viewModel: UiState,
    scaffoldState: ScaffoldState
){
    val state = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
    val scope = rememberCoroutineScope()


    ModalBottomSheetLayout(
        sheetState = state,
        sheetContent = {
            SelectLang(viewModel, state)
        }
    ){
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F8F8)),
        ){
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth(),
                contentPadding = PaddingValues(10.dp)
            ){
                var historyLength = if (viewModel.searchHistorys.size > 10) viewModel.searchHistorys.size - 3 else 0

                item{
                    AnimatedVisibility(visible = !viewModel.focusState.isFocused) {
                        Box(
                            contentAlignment = Alignment.TopStart,
                            modifier = Modifier.padding(bottom = 10.dp)
                        ){
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Image(painterResource(id = R.drawable.logo), null)
                                Text(
                                    text = "Deepl 翻译",
                                    fontWeight = FontWeight.W700,
                                    style = MaterialTheme.typography.h5,
                                    color = Color(15, 43, 70),
                                    modifier = Modifier.padding(start = 15.dp)
                                )
                            }
                        }
                    }
                    InputScaffold(viewModel, state, scaffoldState)
                }
                item{
                    Text(
                        text = "历史记录",
                        fontWeight = FontWeight.W700,
                        style = MaterialTheme.typography.h6,
                        modifier = Modifier.padding(8.dp)
                    )
                }
                for (i in viewModel.searchHistorys.size - 1 downTo historyLength) {
                    item{
                        HistoryListItems(viewModel, i, scaffoldState, scope)
                    }
                }
            }
        }
    }


    BackHandler(
        enabled = (state.currentValue == ModalBottomSheetValue.HalfExpanded
                || state.currentValue == ModalBottomSheetValue.Expanded),
        onBack = {
            scope.launch{
                state.hide()
            }
        }
    )
}


@ExperimentalFoundationApi
@ExperimentalMaterialApi
@ExperimentalAnimationApi
@Composable
fun InputScaffold(
    viewModel: UiState,
    state: ModalBottomSheetState,
    scaffoldState: ScaffoldState
){
    var currentRotation by remember { mutableStateOf(0f) }
    val rotation = remember { Animatable(currentRotation) }

    val leftOffset by animateDpAsState(targetValue = if(viewModel.requestRotate) 50.dp else 0.dp)
    val rightOffset by animateDpAsState(targetValue = if(viewModel.requestRotate) (-50).dp else 0.dp)
    val color by animateColorAsState(targetValue = if(viewModel.sourceLanguage.second != "") Color(0xFF0079D3) else Color(0xFF123456), tween(800))
    val scope = rememberCoroutineScope()
    val focus = LocalFocusManager.current

    Surface(
        shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp, bottomStart = 10.dp, bottomEnd = 10.dp),
        color = Color.White,
        elevation = 5.dp
    ) {
        Column{
            AnimatedVisibility(!viewModel.focusState.isFocused) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ){
                    // 源语言
                    Box(modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable {
                            scope.launch {
                                state.show()
                            }
                            viewModel.langMode = SelectLanguageMode.SOURCE
                        }, contentAlignment = Alignment.CenterStart){
                        Text(
                            text = viewModel.sourceLanguage.first,
                            color = Color(0xFF0079D3),
                            fontWeight = FontWeight.W900,
                            modifier = Modifier
                                .padding(start = 15.dp)
                                .offset(leftOffset)
                        )
                    }

                    CompositionLocalProvider(
                        LocalContentColor provides color
                    ) {
                        IconButton(onClick = {
                            viewModel.requestRotate = true
                        },
                            enabled = viewModel.sourceLanguage.second != ""
                        ) {
                            Icon(
                                painterResource(id = R.drawable.swap),
                                null,
                                modifier = Modifier.rotate(rotation.value)
                            )
                        }
                    }

                    Box(modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable {
                            scope.launch {
                                state.show()
                            }
                            viewModel.langMode = SelectLanguageMode.TARGET
                        }, contentAlignment = Alignment.CenterEnd){
                        Text(
                            text = viewModel.targetLanguage.first,
                            color = Color(0xFF0079D3),
                            fontWeight = FontWeight.W900,
                            modifier = Modifier
                                .padding(end = 15.dp)
                                .offset(rightOffset)
                        )
                    }
                }
            }
            Divider(thickness = (0.8).dp)
            OutlinedTextField(
                value = viewModel.originWord,
                onValueChange = {
                    viewModel.originWord = it
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged {
                        viewModel.focusState = it
                        // if (!it.isFocused && viewModel.originWord != "") viewModel.getResultWithApi()
                    },
                placeholder = {
                    if(viewModel.focusState.isFocused) Text("随便输入点什么吧~") else Text("点按即可输入文本")
                },
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.Black,
                    focusedIndicatorColor = Color.White,
                    unfocusedIndicatorColor = Color.White,
                    cursorColor = Color(0xFF0079D3)
                ),
                trailingIcon = {
                    if(viewModel.originWord != ""){
                        IconButton(onClick = {
                            viewModel.originWord = ""
                        }) {
                            Icon(Icons.Filled.Close,null, tint = Color(0xFF0079D3))
                        }
                    }
                },
                textStyle = TextStyle(
                    fontWeight = FontWeight.W700,
                    fontSize = 18.sp
                ),
                maxLines = 2
            )
            Divider(thickness = 0.8.dp)
            if(!viewModel.focusState.isFocused && viewModel.originWord.isNotEmpty() && (viewModel.translating || viewModel.result.isNotEmpty())){
                LaunchedEffect(key1 = viewModel.translating) {
                    if (!viewModel.translating) {
                        return@LaunchedEffect
                    }
                    while (true) {
                        if (viewModel.result.isNotEmpty()) {
                            viewModel.displayResult = viewModel.result
                            viewModel.translating = false
                            break;
                        }
                        if (viewModel.displayResult == ".....") {
                            viewModel.displayResult = "."
                        } else {
                            viewModel.displayResult += "."
                        }
                        delay(300)
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .combinedClickable(
                        onClick = {
                            Log.d("gzz", "last")
                        },
                        onLongClick = {
                            viewModel.requestCopy.value = viewModel.result
                            scope.launch {
                                scaffoldState.snackbarHostState.showSnackbar("翻译结果已复制到剪贴板")
                            }
                        })
                ){
                    Text(
                        text = viewModel.displayResult.trim(),
                        fontWeight = FontWeight.W700,
                        style = MaterialTheme.typography.h6,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(15.dp)
                            .animateContentSize()
                    )
                    SendButton(viewModel, focus, Modifier.padding(10.dp))
                }
            }
            Divider(thickness = (0.8).dp)


            // 输入预览界面
            if(viewModel.focusState.isFocused && viewModel.originWord.isNotEmpty()){
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = 10.dp)
                ){
                    Text(
                        text = viewModel.originWord,
                        fontWeight = FontWeight.W700,
                        fontSize = 18.sp,
                        color = Color(0xFF0079D3),
                        modifier = Modifier
                            .padding(15.dp)
                            .weight(1f)
                    )
                    if(viewModel.originWord != ""){
                        SendButton(viewModel, focus)
                    }
                }
            }
        }
    }

    // 旋转和交换动画
    LaunchedEffect(viewModel.requestRotate){
        if(viewModel.requestRotate){
            rotation.animateTo(
                targetValue = currentRotation + 360f,
                animationSpec = tween(500)
            ){ currentRotation = value}
            val mid = viewModel.sourceLanguage
            viewModel.sourceLanguage = viewModel.targetLanguage
            viewModel.targetLanguage = mid
        }

        viewModel.requestRotate = false
    }
}
