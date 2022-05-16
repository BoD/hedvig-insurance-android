package com.hedvig.app.service.push

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.coroutines.await
import com.hedvig.android.owldroid.graphql.RegisterPushTokenMutation
import com.hedvig.app.authenticate.AuthenticationTokenService
import e
import i
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class PushNotificationWorker(
    val context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params), KoinComponent {

    private val apolloClient: ApolloClient by inject()
    private val authenticationTokenService: AuthenticationTokenService by inject()

    override suspend fun doWork(): Result {
        val pushToken = inputData.getString(PUSH_TOKEN) ?: throw Exception("No token provided")
        if (!hasHedvigToken()) {
            return Result.retry()
        }
        registerPushToken(pushToken)

        return Result.success()
    }

    private fun hasHedvigToken(): Boolean {
        try {
            val hedvigToken = authenticationTokenService.authenticationToken
            if (hedvigToken != null) {
                return true
            }
        } catch (exception: Exception) {
            e(exception)
        }
        return false
    }

    private suspend fun registerPushToken(pushToken: String) {
        i { "Registering push token" }

        val response = runCatching {
            apolloClient
                .mutation(RegisterPushTokenMutation(pushToken))
                .execute()
        }
        if (response.isFailure) {
            response.exceptionOrNull()
                ?.let { e { "Failed to register push token: $it" } }
            return
        }
        i { "Successfully registered push token" }
    }

    companion object {
        const val PUSH_TOKEN = "push_token"
    }
}
