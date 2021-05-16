package com.github.nthily.translator

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.nthily.translator.ui.theme.TranslatorTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel:UiState by viewModels()

    @ExperimentalAnimationApi
    @ExperimentalMaterialApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TranslatorTheme {
                Input(viewModel)
            }
        }
    }

}



@ExperimentalAnimationApi
@ExperimentalMaterialApi
@Composable
fun Input(
    viewModel:UiState
){
    val state = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
    val scope = rememberCoroutineScope()

    var currentRotation by remember { mutableStateOf(0f) }
    val rotation = remember { Animatable(currentRotation) }

    val leftOffset by animateDpAsState(targetValue = if(viewModel.requestRotate) 50.dp else 0.dp)
    val rightOffset by animateDpAsState(targetValue = if(viewModel.requestRotate) (-50).dp else 0.dp)
    val color by animateColorAsState(targetValue = if(viewModel.sourceLanguage.second != "") Color(0xFF0079D3) else Color(0xFF123456), tween(800))


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
                item{
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

                    Surface(
                        shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp),
                        color = Color.White,
                        elevation = 5.dp
                    ) {
                        Column{
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
                                        viewModel.langMode = 0
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
                                        viewModel.langMode = 1
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
                            Divider(thickness = (0.8).dp)
                            OutlinedTextField(
                                value = viewModel.originWord,
                                onValueChange = {
                                    viewModel.originWord = it
                                },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = {
                                    Text("点按即可输入文本")
                                },
                                colors = TextFieldDefaults.textFieldColors(
                                    backgroundColor = Color.Black,
                                    focusedIndicatorColor = Color.White,
                                    unfocusedIndicatorColor = Color.White,
                                    cursorColor = Color(0xFF0079D3)
                                ),
                                trailingIcon = {
                                    IconButton(onClick = {
                                        viewModel.originWord.replace(" ", "%20").replace(",", "%2C")
                                        viewModel.getResultWithApi()
                                    }) {
                                        Icon(Icons.Filled.Send, null, tint = Color(0xFF0079D3))
                                    }
                                },
                                textStyle = TextStyle(
                                    fontWeight = FontWeight.W700,
                                    fontSize = 18.sp
                                )
                            )
                        }
                    }

                    Surface(
                        color = Color.White,
                        elevation = 5.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 15.dp)
                    ) {
                        AnimatedVisibility(visible = viewModel.result != "") {
                            SelectionContainer{
                                Text(
                                    text = viewModel.result.trim(),
                                    fontWeight = FontWeight.W700,
                                    style = MaterialTheme.typography.h6,
                                    modifier = Modifier
                                        .padding(15.dp)
                                        .animateContentSize()
                                )
                            }
                        }
                    }
                }
                item{
                    Surface(
                        color = Color(80,94,224),
                        elevation = 5.dp,
                        shape = RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 15.dp)
                            .height(300.dp)
                    ){
                        Column(
                            modifier = Modifier.padding(10.dp)
                        ) {
                            Text(
                                text = "搜索历史",
                                fontWeight = FontWeight.W700,
                                style = MaterialTheme.typography.h6,
                                color = Color.White,
                            )
                        }
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