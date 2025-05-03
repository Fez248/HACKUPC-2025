package com.teniaTantoQueDarte.vuelingapp.data.repository

import android.content.Context
import com.teniaTantoQueDarte.vuelingapp.data.db.AppDatabase
import com.teniaTantoQueDarte.vuelingapp.data.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class UserRepository(context: Context) {
    private val userDao = AppDatabase.getDatabase(context).userDao()

    fun getUser(): Flow<User?> {
        return userDao.getUserById()
            .catch { emit(null) }
            .flowOn(Dispatchers.IO)
    }

    suspend fun createUserIfNotExists() {
        withContext(Dispatchers.IO) {
            val user = userDao.getUserById().flowOn(Dispatchers.IO).catch { null }.collect { user ->
                if (user == null) {
                    userDao.insertUser(User(points = 1500))
                }
            }
        }
    }

    suspend fun toggleSharingMode(isSharing: Boolean) {
        withContext(Dispatchers.IO) {
            userDao.updateSharingMode(isSharing = isSharing)
        }
    }
}