package com.example.seabattle.model

data class Profile(
    val uid: String = "",
    val nickname: String = "",
    val avatarChoice: AvatarChoice = AvatarChoice.CAPTAIN,
) {
    val isComplete: Boolean
        get() = uid.isNotBlank() && nickname.isNotBlank()
}
