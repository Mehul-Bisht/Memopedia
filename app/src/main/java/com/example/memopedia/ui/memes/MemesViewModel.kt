package com.example.memopedia.ui.memes

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.memopedia.network.ApiService
import com.example.memopedia.network.Response
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MemesViewModel
@Inject constructor(
    private val service: ApiService
): ViewModel() {

    private val _selectedChip: MutableStateFlow<String> = MutableStateFlow("dankmemes")

    fun setSelected(name: String) {

        _selectedChip.value = name
    }

    fun monitorEvents(): Flow<Response> =
        _selectedChip.flatMapLatest { query ->

            getResult(query)
        }

    private suspend fun getResult(query: String): Flow<Response> =
        withContext(Dispatchers.IO) {

            Log.d("query ", query)

            flow<Response> {

                val response = service.getByName(name = query)

                emit(response)
            }
        }
}