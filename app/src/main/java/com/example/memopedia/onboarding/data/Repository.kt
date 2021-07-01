package com.example.memopedia.onboarding.data

import android.util.Log
import com.example.memopedia.util.Resource
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.lang.Exception
import javax.inject.Singleton

@Singleton
class Repository {

    private val _categories: MutableStateFlow<Resource<List<Category>>> = MutableStateFlow(Resource.Loading())
    val categories: StateFlow<Resource<List<Category>>> get() = _categories

    private val _tags: MutableStateFlow<List<String>> = MutableStateFlow(listOf())
    val tags: StateFlow<List<String>> get() = _tags

    private val _tagsMetaData: MutableStateFlow<HashSet<String>> = MutableStateFlow(hashSetOf())
    val tagsMetaData: StateFlow<HashSet<String>> get() = _tagsMetaData

    suspend fun getMemeByCategories() = withContext(Dispatchers.IO) {

        val dbRef = FirebaseFirestore.getInstance().collection("categories")

        try {

            val task = dbRef.get().await()

            val list: MutableList<Category> = mutableListOf()
            val tagList: MutableList<String> = mutableListOf()
            val tagsMeta: HashSet<String> = hashSetOf()

            task.documents.forEach { doc ->

                val snapshot = dbRef.document(doc.id)
                    .collection(doc.id + "-list")
                    .get().await()

                val categoryList = snapshot.toObjects(CategoryItem::class.java)
                categoryList.forEach { it ->
                    tagList += it.tags

                    it.tags.forEach { tag ->

                        tagsMeta.add(tag)
                    }

                    Log.d("adding tags for ", it.name)
                }

                list += Category(doc.id,categoryList)
            }

            _categories.value = Resource.Success(list)
            _tags.value = tagList
            _tagsMetaData.value = tagsMeta

        } catch (e: Exception) {
            Log.d("exception ",e.message.toString())
            _categories.value = Resource.Error("an unknown error occurred")
        }
    }
}