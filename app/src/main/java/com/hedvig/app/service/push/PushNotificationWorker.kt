package com.hedvig.app.service.push

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.hedvig.android.owldroid.graphql.RegisterPushTokenMutation
import com.hedvig.app.ApolloClientWrapper
import com.hedvig.app.util.apollo.toDeferred
import com.hedvig.app.util.extensions.getAuthenticationToken
import e
import i
import org.koin.core.KoinComponent
import org.koin.core.inject

class PushNotificationWorker(
    val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {

    private val apolloClientWrapper: ApolloClientWrapper by inject()

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
            val hedvigToken = context.getAuthenticationToken()
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
        val registerPushTokenMutation = RegisterPushTokenMutation(pushToken)
        val query =
            apolloClientWrapper.apolloClient.mutate(registerPushTokenMutation).toDeferred().await()
        val response = runCatching { query }
        if (response.isFailure) {
            response.exceptionOrNull()
                ?.let { e { "Failed to handleExpandWithKeyboard push token: $it" } }
            return
        }
        i { "Successfully registered push token" }
    }

    companion object {
        const val PUSH_TOKEN = "push_token"
    }
}
