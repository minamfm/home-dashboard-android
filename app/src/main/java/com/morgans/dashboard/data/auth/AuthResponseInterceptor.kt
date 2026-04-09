package com.morgans.dashboard.data.auth

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthResponseInterceptor @Inject constructor(
    private val authEvent: AuthEvent,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())

        if (response.code == 401 || response.code == 403) {
            authEvent.emitExpired()
        }

        return response
    }
}
