package com.example.memopedia.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.memopedia.R
import com.example.memopedia.databinding.ActivityHomeBinding
import com.example.memopedia.onboarding.datastore.DataStorePreferenceStorage
import com.example.memopedia.ui.memes.MemesViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private val viewModel by viewModels<MemesViewModel>()

    @Inject
    lateinit var datastore: DataStorePreferenceStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launchWhenStarted {

            async {

                datastore.isOnboarded.collect { isLoggedIn ->

                    if (!isLoggedIn) {

                        datastore.save(false)
                        val intent = Intent(this@HomeActivity, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK.or(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        startActivity(intent)
                    }
                }
            }

            async {

                val data = intent.getStringArrayListExtra("subreddits")
                data?.let { dataa ->

                    dataa.add(0,"dankmemes")

                    var set: Set<String> = setOf()
                    val _set: MutableSet<String> = set.toMutableSet()

                    dataa.forEach {
                        _set.add(it)
                    }

                    set = _set

                    if (set.isNotEmpty())
                        datastore.saveTags(set)
                }
            }

            async {

                datastore.tags.collect { tagList ->

                    val chips: ArrayList<Chip> = arrayListOf()

                    tagList.forEach {
                        chips.add(getChip(it))
                    }

                    withContext(Dispatchers.Main) {

                        binding.chipgroup.isSingleSelection = true
                        binding.chipgroup.isSelectionRequired = true
                        binding.chipgroup.check("dankmemes".hashCode())

                        chips.forEach {

                            binding.chipgroup.addView(it)
                        }

                        binding.chipgroup.setOnCheckedChangeListener { group, checkedId ->

                            for (chip in chips) {

                                if (chip.id == checkedId) {

                                    viewModel.setSelected(chip.text.toString())
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun getChip(name: String): Chip {

        val chip = Chip(this)
        chip.text = name
        chip.id = name.hashCode()
        chip.setChipDrawable(
            ChipDrawable.createFromAttributes(
                this,
                null,
                0,
                R.style.CustomChipStyle
            )
        )
        chip.setTextAppearanceResource(R.style.ChipTextColor)

        return chip
    }
}