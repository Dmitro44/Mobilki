package com.example.seabattle.model

data class Profile(
    val uid: String = "",
    val nickname: String = "",
    val avatarChoice: AvatarChoice = AvatarChoice.VRUNGEL,
) {
    val isComplete: Boolean
        get() = uid.isNotBlank() && nickname.isNotBlank()
}
