package com.example.grid.model

import androidx.annotation.DrawableRes
import androidx.annotation.IntegerRes
import androidx.annotation.StringRes
import androidx.compose.ui.input.pointer.PointerId

data class Topic(
    @StringRes val stringResourceId: Int,
    val numberOfTopics: Int,
    @DrawableRes val imageResourceId: Int
) {

}
