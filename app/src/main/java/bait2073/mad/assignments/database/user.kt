package bait2073.mad.assignments.database

data class UserDB(
    var userId: String = "", // Changed to var
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val weight: Float = 0.0f,
    val height: Float = 0.0f,
    val dateOfBirth: String = "",
    val gender: String = "",
    var profileImageName: String = "" // Changed to var and renamed to profileImageName
)