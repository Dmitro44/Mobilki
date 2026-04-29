package com.example.seabattle.model

import com.example.seabattle.R

enum class AvatarChoice(
    val storageName: String,
    val drawableResId: Int,
    val title: String,
) {
    VRUNGEL(
        storageName = "vrungel",
        drawableResId = R.drawable.vrungel,
        title = "Vrungel"
    ),
    PAPAI(
        storageName = "papai",
        drawableResId = R.drawable.papai,
        title = "Papai"
    )
}

fun avatarChoiceFromStorageName(value: String?): AvatarChoice? {
    return AvatarChoice.entries.firstOrNull { it.storageName == value }
}
