package com.example.seabattle.model

import com.example.seabattle.R

enum class AvatarChoice(
    val storageName: String,
    val drawableResId: Int,
    val title: String,
) {
    CAPTAIN(
        storageName = "captain",
        drawableResId = R.drawable.avatar_captain,
        title = "Captain",
    ),
    ANCHOR(
        storageName = "anchor",
        drawableResId = R.drawable.avatar_anchor,
        title = "Anchor",
    ),
}

fun avatarChoiceFromStorageName(value: String?): AvatarChoice? {
    return AvatarChoice.entries.firstOrNull { it.storageName == value }
}
