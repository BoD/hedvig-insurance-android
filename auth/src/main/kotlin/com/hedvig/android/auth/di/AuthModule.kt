package com.hedvig.android.auth.di

import com.hedvig.android.auth.AuthTokenService
import com.hedvig.android.auth.AuthTokenServiceImpl
import com.hedvig.android.auth.interceptor.ExistingAuthTokenAppendingInterceptor
import org.koin.dsl.module

@Suppress("RemoveExplicitTypeArguments")
val authModule = module {
  single<AuthTokenService> { AuthTokenServiceImpl(get(), get(), get()) }
  single<ExistingAuthTokenAppendingInterceptor> { ExistingAuthTokenAppendingInterceptor(get()) }
}
