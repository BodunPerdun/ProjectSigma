package com.example.projectsigma.data

import com.example.projectsigma.model.User
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val currentUser: StateFlow<User?>
    suspend fun loginWithEmail(email: String, pass: String): Result<User>
    suspend fun registerWithEmail(email: String, pass: String, name: String): Result<User>
    suspend fun signInWithGoogle(): Result<User>
    fun logout()
    fun updateUserEventCount(count: Int)
    fun updateLocationVisibility(isVisible: Boolean)
    fun updateSocialsPublicity(isPublic: Boolean)
    fun updateProfilePhoto(photoUrl: String?)
    fun updateSocialHandles(instagram: String?, telegram: String?)
    fun updateUserBio(bio: String?)
    fun addFriend(friendId: String)
    fun removeFriend(friendId: String)
    fun getDiscoverableNearbyUsers(): List<Pair<User, String>>
}
