package com.github.nthily.translator

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException


class UiState: ViewModel() {

    var originWord by mutableStateOf("")

    val allLangs = listOf(
        Pair("自动检测", ""),
        Pair("中文（简体）", "ZH"),
        Pair("瑞典语", "SV"),
        Pair("斯洛文尼亚", "SL"),
        Pair("斯洛伐克语", "SK"),
        Pair("俄语", "RU"),
        Pair("罗马尼亚语", "RO"),
        Pair("葡萄牙语", "PT"),
        Pair("波兰语", "PL"),
        Pair("荷兰语", "NL"),
        Pair("拉脱维亚语", "LV"),
        Pair("立陶宛语", "LT"),
        Pair("日语", "JA"),
        Pair("意大利语", "IT"),
        Pair("匈牙利语", "HU"),
        Pair("法语", "FR"),
        Pair("芬兰语", "FI"),
        Pair("爱沙尼亚语", "ET"),
        Pair("西班牙语", "ES"),
        Pair("英语", "EN"),
        Pair("希腊语", "EL"),
        Pair("德语", "DE"),
        Pair("丹麦语", "DA"),
        Pair("捷克语", "CS"),
        Pair("保加利亚语", "BG"),
    )

    var sourceLanguage by mutableStateOf(allLangs[0])

    var targetLanguage by mutableStateOf(allLangs[19])

    var result by mutableStateOf("")
    var requestRotate by mutableStateOf(false)

    var langMode by mutableStateOf(0)

    private val client = OkHttpClient()

    // 爬虫方式
    fun getResult(){
        viewModelScope.launch(Dispatchers.IO){

            val body = "{\"jsonrpc\":\"2.0\",\"method\": \"LMT_handle_jobs\",\"params\":{\"jobs\":[{\"kind\":\"default\",\"raw_en_sentence\":\"$originWord\",\"raw_en_context_before\":[],\"raw_en_context_after\":[],\"preferred_num_beams\":4,\"quality\":\"fast\"}],\"lang\":{\"user_preferred_langs\":[\"PL\",\"RU\",\"FR\",\"SL\",\"DE\",\"JA\",\"HU\",\"IT\",\"EN\",\"ZH\",\"ES\"],\"source_lang_user_selected\":\"${sourceLanguage.second}\",\"target_lang\":\"${targetLanguage.second}\"},\"priority\":-1,\"commonJobParams\":{\"formality\":null},\"timestamp\":1621181157844},\"id\":54450008}"
            val mediaType = "application/json; charset=utf-8".toMediaType()

            val request = Request.Builder()
                .url("https://www2.deepl.com/jsonrpc")
                .post(body.toRequestBody(mediaType))
                .build()

            try{
                client.newCall(request).execute().use { response ->
                    if(response.code == 200){
                        if (!response.isSuccessful) throw IOException("Unexpected code $response")
                        val jsonObject = response.body?.let { Json.parseToJsonElement(it.string()) }
                        val results = jsonObject?.jsonObject?.get("result")
                        val translations = results?.jsonObject?.get("translations")

                        val newObject = translations?.jsonArray?.get(0)
                        val beams = newObject?.jsonObject?.get("beams")

                        val resultArray = beams?.jsonArray

                        result = resultArray!!.get(0).jsonObject["postprocessed_sentence"].toString().replace("\"", "")
                    } else result = "出现了错误"
                }
            }catch(e: IOException){

            }
        }
    }

    // API 方式
    fun getResultWithApi(){
        viewModelScope.launch(Dispatchers.IO){

            val url: String = if(sourceLanguage.second == "") "https://api-free.deepl.com/v2/translate?auth_key=&text=$originWord&detected_source_language=auto&target_lang=${targetLanguage.second}"
            else "https://api-free.deepl.com/v2/translate?auth_key=&text=$originWord&source_lang=${sourceLanguage.second}&target_lang=${targetLanguage.second}"
            
            val request = Request.Builder()
                .url(url)
                .build()

            try{
                client.newCall(request).execute().use { response ->
                    if(response.code == 200){
                        val jsonObject = response.body?.let { Json.parseToJsonElement(it.string()) }
                        val text = jsonObject?.jsonObject?.get("translations")?.jsonArray?.get(0)?.jsonObject?.get("text")

                        val source_lang = jsonObject?.jsonObject?.get("translations")?.jsonArray?.get(0)?.jsonObject?.get("detected_source_language").toString().replace("\"", "")

                        if(sourceLanguage.second == ""){
                            for(index in 1 until allLangs.size){
                                if(allLangs[index].second == source_lang){
                                    sourceLanguage = allLangs[index]
                                    break
                                }
                            }
                        }

                        result = text.toString().replace("\"", "")
                    } else{
                        getResult()
                    }
                }
            }catch(e: IOException){

            }
        }
    }
}