package com.prac.data.source

import com.prac.data.repository.model.TokenModel

internal interface TokenLocalDataSource {
    suspend fun setToken(token: TokenModel)

    suspend fun isLoggedIn() : Boolean
}