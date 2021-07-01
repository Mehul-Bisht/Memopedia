package com.example.memopedia.onboarding.data

import com.example.memopedia.onboarding.ui.OnBoardingViewModel

data class ItemCountMetaData(

    val id: Int,
    val count: Int,
    val subreddits: Map<String, OnBoardingViewModel.State>
)
