package com.example.memopedia.onboarding.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.memopedia.onboarding.data.Category
import com.example.memopedia.onboarding.data.CategoryItem
import com.example.memopedia.onboarding.data.Repository
import com.example.memopedia.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnBoardingViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    private val _onBoardingState: MutableStateFlow<OnBoardingState> =
        MutableStateFlow(OnBoardingState.None)

    private val _onBoardingtags: MutableStateFlow<List<String>> = MutableStateFlow(listOf())
    val onBoardingtags: StateFlow<List<String>> get() = _onBoardingtags

    private val _tagsMetaData: MutableStateFlow<HashSet<String>> = MutableStateFlow(hashSetOf())
    val tagsMetaData: StateFlow<HashSet<String>> get() = _tagsMetaData

    private val _onSearch: MutableStateFlow<OnBoardingState> =
        MutableStateFlow(OnBoardingState.None)

    private val _selectedCategories: MutableStateFlow<Map<String,State>> = MutableStateFlow(hashMapOf())
    private val selectedCategories: StateFlow<Map<String,State>> get() = _selectedCategories

    private val _query: MutableStateFlow<String> = MutableStateFlow("")

    private val _isActive: MutableStateFlow<Boolean> = MutableStateFlow(false)

    private var isFetched: Boolean = false

    sealed class OnBoardingState {
        data class Success(val data: List<Category>?, val shouldScroll: Boolean) : OnBoardingState()
        object Loading : OnBoardingState()
        object Error : OnBoardingState()
        object None : OnBoardingState()
    }

    enum class State { UNSELECTED, SELECTED }

    val selectedCount = selectedCategories.flatMapLatest { map ->

        flow {

            var count = 0
            val keys = map.keys

            for (key in keys) {

                if (map[key] == State.SELECTED) {
                    count++
                }
            }

            emit(Pair(count,map))
        }
    }

    fun search(query: String) {

        _query.value = query
    }

    fun toggleSearchViewState(isActive: Boolean) {

        _isActive.value = isActive
    }

    fun toggleSelectedState(categoryItem: CategoryItem) {

        toggleSelected(categoryItem)

        if (!_isActive.value) {

           toggleOnBoarding(categoryItem, true)

        } else {

            val onSearchData = (_onSearch.value as OnBoardingState.Success).data

            onSearchData?.let { categList ->

                var dataList = categList
                val onSearchList = categList.toMutableList()
                val categoryItr = onSearchList.listIterator()

                var flag = false
                var categoryToBeSwiched: Category? = null

                while (categoryItr.hasNext() && !flag) {

                    val categoryData = categoryItr.next()

                    val list = categoryData.categories.toMutableList()
                    val itr = list.listIterator()

                    while (itr.hasNext() && !flag) {

                        val item = itr.next()

                        if (item.name == categoryItem.name) {

                            itr.remove()

                            itr.add(
                                CategoryItem(
                                    name = categoryItem.name,
                                    subreddit = categoryItem.subreddit,
                                    tags = categoryItem.tags,
                                    url = categoryItem.url,
                                    isSelected = !categoryItem.isSelected
                                )
                            )

                            flag = true

                            categoryToBeSwiched = Category(
                                name = categoryData.name,
                                categories = list
                            )
                        }
                    }

                    if (flag) {
                        dataList = onSearchList
                        categoryItr.remove()

                        categoryToBeSwiched?.let {
                            categoryItr.add(it)
                        }
                    }
                }

                _onSearch.value = OnBoardingState.Success(dataList, false)
                toggleOnBoarding(categoryItem, false)
            }
        }
    }

    private fun toggleOnBoarding(categoryItem: CategoryItem, shouldResetSearch: Boolean) {

        val onBoardingData = (_onBoardingState.value as OnBoardingState.Success).data

        onBoardingData?.let { categList ->

            var dataList = categList
            val onBoardingDataList = categList.toMutableList()
            val categoryItr = onBoardingDataList.listIterator()

            var flag = false
            var categoryToBeSwiched: Category? = null

            while (categoryItr.hasNext() && !flag) {

                val categoryData = categoryItr.next()

                val list = categoryData.categories.toMutableList()
                val itr = list.listIterator()

                while (itr.hasNext() && !flag) {

                    val item = itr.next()

                    if (item.name == categoryItem.name) {

                        itr.remove()

                        itr.add(
                            CategoryItem(
                                name = categoryItem.name,
                                subreddit = categoryItem.subreddit,
                                tags = categoryItem.tags,
                                url = categoryItem.url,
                                isSelected = !categoryItem.isSelected
                            )
                        )

                        flag = true

                        categoryToBeSwiched = Category(
                            name = categoryData.name,
                            categories = list
                        )
                    }
                }

                if (flag) {
                    dataList = onBoardingDataList
                    categoryItr.remove()

                    categoryToBeSwiched?.let {
                        categoryItr.add(it)
                    }
                }
            }

            _onBoardingState.value = OnBoardingState.Success(dataList, false)

            if (shouldResetSearch) {

                _onSearch.value = _onBoardingState.value
            }
        }
    }

    private fun toggleSelected(categoryItem: CategoryItem) {

        val map = _selectedCategories.value.toMutableMap()

        val keys = map.keys

        var state: State? = State.UNSELECTED

        for (key in keys) {

            if (key == categoryItem.subreddit) {

                state = map[key]
            }
        }

        var stateValue = State.SELECTED

        if (state == State.SELECTED) {

            stateValue = State.UNSELECTED
        }

        map[categoryItem.subreddit] = stateValue

        _selectedCategories.value = map
    }

    val godFlow: Flow<OnBoardingState> =
        _isActive.flatMapLatest { isActive ->

            if (isActive) {

                _onSearch
            } else {

                _onBoardingState
            }
        }

    fun fetchCategories() =
        viewModelScope.launch {

            if (!isFetched) {
                getCategories()
            }

            _query.collectLatest { keyword ->

                val categoryBufferList: MutableList<Category> = mutableListOf()

                var tagFound = false

                _tagsMetaData.value.forEach { tag ->

                    if (tag.contains(keyword)) {

                        tagFound = true

                        if (_onBoardingState.value is OnBoardingState.Success) {

                            val data = (_onBoardingState.value as OnBoardingState.Success).data

                            data?.let { categoryList ->

                                categoryList.forEach { category ->

                                    val categoryItemBufferList: MutableList<CategoryItem> =
                                        mutableListOf()

                                    category.categories.forEach { categoryItem ->

                                        if (categoryItem.tags.contains(tag)) {

                                            if (!categoryItemBufferList.contains(categoryItem)) {

                                                categoryItemBufferList += categoryItem
                                                Log.d("found ",categoryItem.name)
                                            }

                                        }
                                    }

                                    if (categoryItemBufferList.isNotEmpty()) {

                                        var flag = false

                                        val itr = categoryBufferList.iterator()

                                        while (itr.hasNext() && !flag) {

                                            val categ = itr.next()

                                            if (categ.name == category.name) {

                                                itr.remove()

                                                categoryBufferList.add(
                                                    Category(
                                                        name = category.name,
                                                        categories = categoryItemBufferList
                                                    )
                                                )

                                                flag = true
                                            }
                                        }

                                        if (!flag) {

                                            categoryBufferList += Category(
                                                name = category.name,
                                                categories = categoryItemBufferList
                                            )
                                        }

                                    }
                                }

                                _onSearch.value = OnBoardingState.Success(categoryBufferList, false)
                            }
                        }
                    }
                }
                if (!tagFound) {

                    _onSearch.value = OnBoardingState.Success(listOf(),false)
                }
            }
        }

    private fun getCategories() =
        viewModelScope.launch {

            repository.getMemeByCategories()

            async {

                repository.tags.collect {

                    _onBoardingtags.value = it
                }
            }

            async {

                repository.tagsMetaData.collect {

                    _tagsMetaData.value = it
                }
            }

            async {

                repository.categories.collect {

                    when (it) {

                        is Resource.Success -> {

                            _onSearch.value = OnBoardingState.Success(it.data,true)
                            _onBoardingState.value = OnBoardingState.Success(it.data, true)
                            isFetched = true
                            initialize(it.data)
                        }

                        is Resource.Loading -> {

                            _onBoardingState.value = OnBoardingState.Loading
                        }

                        is Resource.Error -> {

                            _onBoardingState.value = OnBoardingState.Error
                        }
                    }
                }
            }
        }

    private fun initialize(items: List<Category>?) {

        val map: MutableMap<String,State> = hashMapOf()

        items?.forEach { category ->

            category.categories.forEach { categoryItem ->

                map[categoryItem.subreddit] = State.UNSELECTED
            }
        }

        _selectedCategories.value = map
    }
}