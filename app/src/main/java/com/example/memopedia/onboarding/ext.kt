package com.example.memopedia.onboarding

import android.view.View
import androidx.appcompat.widget.SearchView
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

fun SearchView.getQueryTextChangeStateFlow(onSubmit: ()-> Unit): StateFlow<String> {

    val query = MutableStateFlow("")

    setOnQueryTextListener(object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String?): Boolean {
            onSubmit()
            return true
        }

        override fun onQueryTextChange(newText: String): Boolean {
            query.value = newText
            return true
        }
    })

    return query
}

fun SearchView.getActiveStateFlow(): StateFlow<Boolean> {

    val isOpen = MutableStateFlow(false)

    setOnSearchClickListener {

        isOpen.value = true
    }

    setOnCloseListener {

        isOpen.value = false
        false
    }

    return isOpen
}