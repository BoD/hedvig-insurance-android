@file:Suppress("RemoveExplicitTypeArguments")

package com.hedvig.app

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Build
import androidx.work.WorkerParameters
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.decode.SvgDecoder
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.cache.normalized.api.MemoryCacheFactory
import com.apollographql.apollo3.cache.normalized.api.NormalizedCacheFactory
import com.apollographql.apollo3.cache.normalized.normalizedCache
import com.apollographql.apollo3.interceptor.ApolloInterceptor
import com.apollographql.apollo3.network.okHttpClient
import com.apollographql.apollo3.network.ws.SubscriptionWsProtocol
import com.hedvig.android.apollo.di.apolloClientModule
import com.hedvig.android.apollo.giraffe.di.giraffeClient
import com.hedvig.android.apollo.octopus.di.octopusClient
import com.hedvig.android.auth.AccessTokenProvider
import com.hedvig.android.auth.di.authModule
import com.hedvig.android.auth.interceptor.AuthTokenRefreshingInterceptor
import com.hedvig.android.auth.interceptor.MigrateTokenInterceptor
import com.hedvig.android.core.common.android.QuoteCartId
import com.hedvig.android.core.common.di.LogInfoType
import com.hedvig.android.core.common.di.coreCommonModule
import com.hedvig.android.core.common.di.datastoreFileQualifier
import com.hedvig.android.core.common.di.giraffeGraphQLUrlQualifier
import com.hedvig.android.core.common.di.giraffeGraphQLWebSocketUrlQualifier
import com.hedvig.android.core.common.di.isDebugQualifier
import com.hedvig.android.core.common.di.isProductionQualifier
import com.hedvig.android.core.common.di.logInfoQualifier
import com.hedvig.android.core.common.di.octopusGraphQLUrlQualifier
import com.hedvig.android.core.datastore.di.dataStoreModule
import com.hedvig.android.data.travelcertificate.di.claimFlowDataModule
import com.hedvig.android.data.travelcertificate.di.odysseyUrlQualifier
import com.hedvig.android.data.travelcertificate.di.travelCertificateDataModule
import com.hedvig.android.datadog.addDatadogConfiguration
import com.hedvig.android.datadog.di.datadogModule
import com.hedvig.android.feature.businessmodel.di.businessModelModule
import com.hedvig.android.feature.changeaddress.di.changeAddressModule
import com.hedvig.android.feature.claimtriaging.di.claimTriagingModule
import com.hedvig.android.feature.forever.data.ForeverRepository
import com.hedvig.android.feature.home.di.homeModule
import com.hedvig.android.feature.insurances.di.insurancesModule
import com.hedvig.android.feature.odyssey.di.odysseyModule
import com.hedvig.android.feature.terminateinsurance.di.terminateInsuranceModule
import com.hedvig.android.feature.travelcertificate.di.travelCertificateModule
import com.hedvig.android.hanalytics.android.di.appIdQualifier
import com.hedvig.android.hanalytics.android.di.appVersionCodeQualifier
import com.hedvig.android.hanalytics.android.di.appVersionNameQualifier
import com.hedvig.android.hanalytics.android.di.hAnalyticsAndroidModule
import com.hedvig.android.hanalytics.android.di.hAnalyticsUrlQualifier
import com.hedvig.android.hanalytics.di.hAnalyticsModule
import com.hedvig.android.hanalytics.featureflags.di.featureManagerModule
import com.hedvig.android.language.LanguageService
import com.hedvig.android.language.di.languageModule
import com.hedvig.android.market.MarketManager
import com.hedvig.android.market.di.marketManagerModule
import com.hedvig.android.navigation.activity.ActivityNavigator
import com.hedvig.android.navigation.core.di.deepLinkModule
import com.hedvig.android.notification.badge.data.di.notificationBadgeModule
import com.hedvig.android.notification.core.NotificationSender
import com.hedvig.android.notification.firebase.di.firebaseNotificationModule
import com.hedvig.app.authenticate.BankIdLoginViewModel
import com.hedvig.app.authenticate.LogoutUseCase
import com.hedvig.app.authenticate.LogoutUseCaseImpl
import com.hedvig.app.data.debit.PayinStatusRepository
import com.hedvig.app.feature.addressautocompletion.data.GetDanishAddressAutoCompletionUseCase
import com.hedvig.app.feature.addressautocompletion.data.GetFinalDanishAddressSelectionUseCase
import com.hedvig.app.feature.addressautocompletion.ui.AddressAutoCompleteViewModel
import com.hedvig.app.feature.adyen.AdyenRepository
import com.hedvig.app.feature.adyen.ConnectPaymentUseCase
import com.hedvig.app.feature.adyen.ConnectPayoutUseCase
import com.hedvig.app.feature.adyen.payin.AdyenConnectPayinViewModel
import com.hedvig.app.feature.adyen.payin.AdyenConnectPayinViewModelImpl
import com.hedvig.app.feature.adyen.payout.AdyenConnectPayoutViewModel
import com.hedvig.app.feature.adyen.payout.AdyenConnectPayoutViewModelImpl
import com.hedvig.app.feature.chat.data.ChatEventDataStore
import com.hedvig.app.feature.chat.data.ChatEventStore
import com.hedvig.app.feature.chat.data.ChatRepository
import com.hedvig.app.feature.chat.data.UserRepository
import com.hedvig.app.feature.chat.service.ChatNotificationSender
import com.hedvig.app.feature.chat.service.ReplyWorker
import com.hedvig.app.feature.chat.viewmodel.ChatViewModel
import com.hedvig.app.feature.checkout.CheckoutViewModel
import com.hedvig.app.feature.checkout.EditCheckoutUseCase
import com.hedvig.app.feature.connectpayin.ConnectPaymentViewModel
import com.hedvig.app.feature.embark.EmbarkRepository
import com.hedvig.app.feature.embark.EmbarkViewModel
import com.hedvig.app.feature.embark.EmbarkViewModelImpl
import com.hedvig.app.feature.embark.GraphQLQueryUseCase
import com.hedvig.app.feature.embark.ValueStore
import com.hedvig.app.feature.embark.ValueStoreImpl
import com.hedvig.app.feature.embark.passages.addressautocomplete.EmbarkAddressAutoCompleteViewModel
import com.hedvig.app.feature.embark.passages.audiorecorder.AudioRecorderViewModel
import com.hedvig.app.feature.embark.passages.datepicker.DatePickerViewModel
import com.hedvig.app.feature.embark.passages.externalinsurer.ExternalInsurerViewModel
import com.hedvig.app.feature.embark.passages.externalinsurer.GetInsuranceProvidersUseCase
import com.hedvig.app.feature.embark.passages.multiaction.MultiActionItem
import com.hedvig.app.feature.embark.passages.multiaction.MultiActionParams
import com.hedvig.app.feature.embark.passages.multiaction.MultiActionViewModel
import com.hedvig.app.feature.embark.passages.multiaction.add.AddComponentViewModel
import com.hedvig.app.feature.embark.passages.numberactionset.NumberActionParams
import com.hedvig.app.feature.embark.passages.numberactionset.NumberActionViewModel
import com.hedvig.app.feature.embark.passages.textaction.TextActionParameter
import com.hedvig.app.feature.embark.passages.textaction.TextActionViewModel
import com.hedvig.app.feature.embark.ui.EmbarkActivity
import com.hedvig.app.feature.embark.ui.GetMemberIdUseCase
import com.hedvig.app.feature.embark.ui.MemberIdViewModel
import com.hedvig.app.feature.embark.ui.MemberIdViewModelImpl
import com.hedvig.app.feature.embark.ui.TooltipViewModel
import com.hedvig.app.feature.genericauth.GenericAuthViewModel
import com.hedvig.app.feature.genericauth.otpinput.OtpInputViewModel
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.feature.loggedin.ui.LoggedInRepository
import com.hedvig.app.feature.loggedin.ui.ReviewDialogViewModel
import com.hedvig.app.feature.marketing.MarketingActivity
import com.hedvig.app.feature.marketing.MarketingViewModel
import com.hedvig.app.feature.marketing.data.GetInitialMarketPickerValuesUseCase
import com.hedvig.app.feature.marketing.data.GetMarketingBackgroundUseCase
import com.hedvig.app.feature.marketing.data.UpdateApplicationLanguageUseCase
import com.hedvig.app.feature.marketing.data.UploadMarketAndLanguagePreferencesUseCase
import com.hedvig.app.feature.offer.OfferRepository
import com.hedvig.app.feature.offer.OfferViewModel
import com.hedvig.app.feature.offer.OfferViewModelImpl
import com.hedvig.app.feature.offer.SelectedVariantStore
import com.hedvig.app.feature.offer.model.QuoteCartFragmentToOfferModelMapper
import com.hedvig.app.feature.offer.ui.changestartdate.ChangeDateBottomSheetData
import com.hedvig.app.feature.offer.ui.changestartdate.ChangeDateBottomSheetViewModel
import com.hedvig.app.feature.offer.ui.changestartdate.QuoteCartEditStartDateUseCase
import com.hedvig.app.feature.offer.usecase.AddPaymentTokenUseCase
import com.hedvig.app.feature.offer.usecase.EditCampaignUseCase
import com.hedvig.app.feature.offer.usecase.GetQuoteCartCheckoutUseCase
import com.hedvig.app.feature.offer.usecase.ObserveOfferStateUseCase
import com.hedvig.app.feature.offer.usecase.ObserveQuoteCartCheckoutUseCase
import com.hedvig.app.feature.offer.usecase.ObserveQuoteCartCheckoutUseCaseImpl
import com.hedvig.app.feature.offer.usecase.StartCheckoutUseCase
import com.hedvig.app.feature.profile.data.ProfileRepository
import com.hedvig.app.feature.profile.data.ProfileRepositoryImpl
import com.hedvig.app.feature.profile.ui.aboutapp.AboutAppViewModel
import com.hedvig.app.feature.profile.ui.eurobonus.EurobonusViewModel
import com.hedvig.app.feature.profile.ui.myinfo.MyInfoViewModel
import com.hedvig.app.feature.profile.ui.payment.PaymentRepository
import com.hedvig.app.feature.profile.ui.payment.PaymentViewModel
import com.hedvig.app.feature.profile.ui.payment.history.PaymentHistoryViewModel
import com.hedvig.app.feature.profile.ui.tab.GetEurobonusStatusUseCase
import com.hedvig.app.feature.profile.ui.tab.NetworkGetEurobonusStatusUseCase
import com.hedvig.app.feature.profile.ui.tab.ProfileViewModel
import com.hedvig.android.feature.forever.di.foreverModule
import com.hedvig.app.feature.referrals.ui.redeemcode.RedeemCodeViewModel
import com.hedvig.app.feature.settings.ChangeLanguageUseCase
import com.hedvig.app.feature.settings.SettingsViewModel
import com.hedvig.app.feature.swedishbankid.sign.SwedishBankIdSignViewModel
import com.hedvig.app.feature.trustly.TrustlyRepository
import com.hedvig.app.feature.trustly.TrustlyViewModel
import com.hedvig.app.feature.trustly.TrustlyViewModelImpl
import com.hedvig.app.feature.zignsec.SimpleSignAuthenticationViewModel
import com.hedvig.app.service.FileService
import com.hedvig.app.service.push.senders.CrossSellNotificationSender
import com.hedvig.app.service.push.senders.GenericNotificationSender
import com.hedvig.app.service.push.senders.PaymentNotificationSender
import com.hedvig.app.service.push.senders.ReferralsNotificationSender
import com.hedvig.app.util.apollo.DeviceIdInterceptor
import com.hedvig.app.util.apollo.GraphQLQueryHandler
import com.hedvig.app.util.apollo.NetworkCacheManager
import com.hedvig.app.util.apollo.ReopenSubscriptionException
import com.hedvig.app.util.apollo.SunsettingInterceptor
import com.hedvig.app.util.extensions.startChat
import com.hedvig.authlib.AuthEnvironment
import com.hedvig.authlib.AuthRepository
import com.hedvig.authlib.Callbacks
import com.hedvig.authlib.NetworkAuthRepository
import java.io.File
import java.time.Clock
import java.util.*
import kotlin.math.pow
import kotlinx.coroutines.delay
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.androidx.workmanager.dsl.worker
import org.koin.core.parameter.ParametersHolder
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import slimber.log.d
import slimber.log.i
import timber.log.Timber

fun isDebug() = BuildConfig.APPLICATION_ID == "com.hedvig.dev.app" ||
  BuildConfig.APPLICATION_ID == "com.hedvig.test.app" ||
  BuildConfig.DEBUG

private val networkModule = module {
  single { androidApplication() as HedvigApplication }
  single<NormalizedCacheFactory> {
    MemoryCacheFactory(maxSizeBytes = 10 * 1024 * 1024)
  }
  factory<OkHttpClient.Builder> {
    val languageService = get<LanguageService>()
    val builder: OkHttpClient.Builder = OkHttpClient.Builder()
      .addDatadogConfiguration()
      .addInterceptor(get<MigrateTokenInterceptor>())
      .addInterceptor(get<AuthTokenRefreshingInterceptor>())
      .addInterceptor { chain ->
        chain.proceed(
          chain
            .request()
            .newBuilder()
            .header("User-Agent", makeUserAgent(languageService.getLocale()))
            .header("Accept-Language", languageService.getLocale().toLanguageTag())
            .header("hedvig-language", languageService.getLocale().toLanguageTag())
            .header("apollographql-client-name", BuildConfig.APPLICATION_ID)
            .header("apollographql-client-version", BuildConfig.VERSION_NAME)
            .header("X-Build-Version", BuildConfig.VERSION_CODE.toString())
            .header("X-App-Version", BuildConfig.VERSION_NAME)
            .header("X-System-Version", Build.VERSION.SDK_INT.toString())
            .header("X-Platform", "ANDROID")
            .header("X-Model", "${Build.MANUFACTURER} ${Build.MODEL}")
            .build(),
        )
      }
      .addInterceptor(DeviceIdInterceptor(get(), get()))
    if (!get<Boolean>(isProductionQualifier)) {
      val logger = HttpLoggingInterceptor { message ->
        if (message.contains("Content-Disposition")) {
          Timber.tag("OkHttp").v("File upload omitted from log")
        } else {
          Timber.tag("OkHttp").v(message)
        }
      }
      logger.level = HttpLoggingInterceptor.Level.BODY
      builder.addInterceptor(logger)
    }
    builder
  }
  single<OkHttpClient> {
    val okHttpBuilder = get<OkHttpClient.Builder>()
    okHttpBuilder.build()
  }
  single<SunsettingInterceptor> { SunsettingInterceptor(get()) } bind ApolloInterceptor::class
  single<ApolloClient.Builder> {
    val interceptors = getAll<ApolloInterceptor>().distinct()
    val accessTokenProvider = get<AccessTokenProvider>()
    ApolloClient.Builder()
      .okHttpClient(get<OkHttpClient>())
      .webSocketReopenWhen { throwable, reconnectAttempt ->
        if (throwable is ReopenSubscriptionException) {
          return@webSocketReopenWhen true
        }
        if (reconnectAttempt < 5) {
          delay(2.0.pow(reconnectAttempt.toDouble()).toLong()) // Retry after 1 - 2 - 4 - 8 - 16 seconds
          return@webSocketReopenWhen true
        }
        false
      }
      .wsProtocol(
        SubscriptionWsProtocol.Factory(
          connectionPayload = {
            val accessToken = accessTokenProvider.provide()
            d { "Apollo-kotlin: Subscription acquired auth token: $accessToken" }
            val authorizationHeaderValue = if (accessToken != null) {
              "Bearer $accessToken"
            } else {
              null
            }
            mapOf("Authorization" to authorizationHeaderValue)
          },
        ),
      )
      .normalizedCache(get<NormalizedCacheFactory>())
      .addInterceptors(interceptors)
  }
}

private val apolloClientUrlsModule = module {
  single<String>(giraffeGraphQLUrlQualifier) { get<Context>().getString(R.string.GRAPHQL_URL) }
  single<String>(giraffeGraphQLWebSocketUrlQualifier) { get<Context>().getString(R.string.WS_GRAPHQL_URL) }
  single<String>(octopusGraphQLUrlQualifier) { get<Context>().getString(R.string.OCTOPUS_GRAPHQL_URL) }
}

fun makeUserAgent(locale: Locale): String = buildString {
  append(BuildConfig.APPLICATION_ID)
  append(" ")
  append(BuildConfig.VERSION_NAME)
  append(" ")
  append("(Android")
  append(" ")
  append(Build.VERSION.RELEASE)
  append("; ")
  append(Build.BRAND)
  append(" ")
  append(Build.MODEL)
  append("; ")
  append(Build.DEVICE)
  append("; ")
  append(locale.language)
  append(")")
}

private val viewModelModule = module {
  viewModel { ChatViewModel(get(), get(), get()) }
  viewModel { (quoteCartId: QuoteCartId?) -> RedeemCodeViewModel(quoteCartId, get(), get()) }
  viewModel { BankIdLoginViewModel(get(), get(), get(), get(), get()) }
  viewModel {
    SettingsViewModel(
      hAnalytics = get(),
      changeLanguageUseCase = get(),
      marketManager = get(),
      languageService = get(),
      settingsDataStore = get(),
    )
  }
  viewModel { DatePickerViewModel() }
  viewModel { params ->
    SimpleSignAuthenticationViewModel(params.get(), get(), get(), get(), get(), get())
  }
  viewModel { (data: MultiActionParams) -> MultiActionViewModel(data) }
  viewModel { (componentState: MultiActionItem.Component?, multiActionParams: MultiActionParams) ->
    AddComponentViewModel(
      componentState,
      multiActionParams,
    )
  }
  viewModel { (quoteCartId: QuoteCartId) ->
    SwedishBankIdSignViewModel(quoteCartId, get(), get())
  }
  viewModel { AudioRecorderViewModel(get()) }
  viewModel { GenericAuthViewModel(get(), get()) }
  viewModel<OtpInputViewModel> { (verifyUrl: String, resendUrl: String, credential: String) ->
    OtpInputViewModel(
      verifyUrl,
      resendUrl,
      credential,
      get(),
      get(),
      get(),
    )
  }
  viewModel { parametersHolder: ParametersHolder ->
    EmbarkAddressAutoCompleteViewModel(
      parametersHolder.getOrNull(),
    )
  }
  viewModel { parametersHolder ->
    AddressAutoCompleteViewModel(
      parametersHolder.getOrNull(),
      get(),
      get(),
    )
  }
  viewModel { TooltipViewModel(get()) }
  viewModel { MyInfoViewModel(get(), get()) }
  viewModel { AboutAppViewModel(get()) }
  viewModel { MarketingViewModel(get<MarketManager>().market, get(), get(), get(), get(), get()) }
  viewModel<ReviewDialogViewModel> { ReviewDialogViewModel(get()) }
}

private val onboardingModule = module {
  viewModel<MemberIdViewModel> { MemberIdViewModelImpl(get()) }
}

private val offerModule = module {
  single<OfferRepository> { OfferRepository(get<ApolloClient>(giraffeClient), get(), get(), get()) }
  viewModel<OfferViewModel> { parametersHolder: ParametersHolder ->
    OfferViewModelImpl(
      quoteCartId = parametersHolder.get(),
      selectedContractTypes = parametersHolder.get(),
      offerRepository = get(),
      startCheckoutUseCase = get(),
      editCampaignUseCase = get(),
      featureManager = get(),
      addPaymentTokenUseCase = get(),
      getBundleVariantUseCase = get(),
      selectedVariantStore = get(),
      getQuoteCartCheckoutUseCase = get(),
    )
  }
  single { QuoteCartFragmentToOfferModelMapper(get()) }
  single<GetQuoteCartCheckoutUseCase> { GetQuoteCartCheckoutUseCase(get<ApolloClient>(giraffeClient)) }
  single<ObserveQuoteCartCheckoutUseCase> { ObserveQuoteCartCheckoutUseCaseImpl(get()) }
  single<SelectedVariantStore> { SelectedVariantStore() }
}

private val profileModule = module {
  single<ProfileRepository> {
    ProfileRepositoryImpl(
      giraffeApolloClient = get<ApolloClient>(giraffeClient),
      octopusApolloClient = get<ApolloClient>(octopusClient),
    )
  }
  single<GetEurobonusStatusUseCase> { NetworkGetEurobonusStatusUseCase(get<ApolloClient>(octopusClient)) }
  viewModel<ProfileViewModel> { ProfileViewModel(get(), get(), get()) }
  viewModel<EurobonusViewModel> { EurobonusViewModel(get<ApolloClient>(octopusClient)) }
}

private val paymentModule = module {
  viewModel<PaymentViewModel> { PaymentViewModel(get(), get(), get()) }
  viewModel<PaymentHistoryViewModel> { PaymentHistoryViewModel(get(), get()) }
}

private val adyenModule = module {
  viewModel<AdyenConnectPayinViewModel> { AdyenConnectPayinViewModelImpl(get(), get()) }
  viewModel<AdyenConnectPayoutViewModel> { AdyenConnectPayoutViewModelImpl(get()) }
}

private val embarkModule = module {
  viewModel<EmbarkViewModel> { (storyName: String) ->
    EmbarkViewModelImpl(
      embarkRepository = get(),
      authTokenService = get(),
      graphQLQueryUseCase = get(),
      valueStore = get(),
      hAnalytics = get(),
      storyName = storyName,
    )
  }
}

private val valueStoreModule = module {
  factory<ValueStore> { ValueStoreImpl() }
}

private val textActionSetModule = module {
  viewModel { (data: TextActionParameter) -> TextActionViewModel(data) }
}

private val activityNavigatorModule = module {
  single<ActivityNavigator> {
    ActivityNavigator(
      application = get(),
      loggedOutActivityClass = MarketingActivity::class.java,
      buildConfigApplicationId = BuildConfig.APPLICATION_ID,
      navigateToChat = { startChat() },
      navigateToEmbark = { storyName: String, storyTitle: String ->
        startActivity(
          EmbarkActivity.newInstance(
            context = this,
            storyName = storyName,
            storyTitle = storyTitle,
          ),
        )
      },
      navigateToLoggedInActivity = { clearBackstack ->
        startActivity(
          LoggedInActivity.newInstance(this, clearBackstack),
        )
      },
    )
  }
}

private val numberActionSetModule = module {
  viewModel { (data: NumberActionParams) -> NumberActionViewModel(data) }
}

private val connectPaymentModule = module {
  viewModel { ConnectPaymentViewModel(get(), get(), get()) }
}

private val trustlyModule = module {
  viewModel<TrustlyViewModel> { TrustlyViewModelImpl(get(), get()) }
}

private val changeDateBottomSheetModule = module {
  viewModel { (data: ChangeDateBottomSheetData) -> ChangeDateBottomSheetViewModel(get(), data, get()) }
}

private val stringConstantsModule = module {
  single<String>(hAnalyticsUrlQualifier) { get<Context>().getString(R.string.HANALYTICS_URL) }
  single<String>(odysseyUrlQualifier) { get<Context>().getString(R.string.ODYSSEY_URL) }
  single<String>(appVersionNameQualifier) { BuildConfig.VERSION_NAME }
  single<String>(appVersionCodeQualifier) { BuildConfig.VERSION_CODE.toString() }
  single<String>(appIdQualifier) { BuildConfig.APPLICATION_ID }
  single<Boolean>(isDebugQualifier) { BuildConfig.DEBUG }
  single<Boolean>(isProductionQualifier) {
    BuildConfig.BUILD_TYPE == "release" && BuildConfig.APPLICATION_ID == "com.hedvig.app"
  }
}

private val checkoutModule = module {
  viewModel { (selectedVariantId: String, quoteCartId: QuoteCartId) ->
    CheckoutViewModel(
      selectedVariantId = selectedVariantId,
      quoteCartId = quoteCartId,
      signQuotesUseCase = get(),
      editQuotesUseCase = get(),
      marketManager = get(),
      offerRepository = get(),
      bundleVariantUseCase = get(),
      selectedVariantStore = get(),
    )
  }
}

private val externalInsuranceModule = module {
  viewModel { ExternalInsurerViewModel(get(), get()) }
}

private val serviceModule = module {
  single<FileService> { FileService(get()) }
}

private val repositoriesModule = module {
  single { ChatRepository(get<ApolloClient>(giraffeClient), get(), get()) }
  single { PayinStatusRepository(get<ApolloClient>(giraffeClient)) }
  single { UserRepository(get<ApolloClient>(giraffeClient)) }
  single { AdyenRepository(get<ApolloClient>(giraffeClient), get()) }
  single { EmbarkRepository(get<ApolloClient>(giraffeClient), get()) }
  single { ForeverRepository(get<ApolloClient>(giraffeClient), get()) }
  single { LoggedInRepository(get<ApolloClient>(giraffeClient), get()) }
  single { TrustlyRepository(get<ApolloClient>(giraffeClient)) }
  single { GetMemberIdUseCase(get<ApolloClient>(giraffeClient)) }
  single { PaymentRepository(get<ApolloClient>(giraffeClient), get()) }
}

private val notificationModule = module {
  single { PaymentNotificationSender(get(), get(), get(), get()) } bind NotificationSender::class
  single { CrossSellNotificationSender(get(), get()) } bind NotificationSender::class
  single { ChatNotificationSender(get()) } bind NotificationSender::class
  single { ReferralsNotificationSender(get()) } bind NotificationSender::class
  single { GenericNotificationSender(get()) } bind NotificationSender::class
}

private val clockModule = module {
  single<Clock> { Clock.systemDefaultZone() }
  single<kotlinx.datetime.Clock> { kotlinx.datetime.Clock.System }
}

private val useCaseModule = module {
  single { StartCheckoutUseCase(get<ApolloClient>(giraffeClient), get(), get()) }
  single<LogoutUseCase> {
    LogoutUseCaseImpl(get(), get<ApolloClient>(giraffeClient), get(), get(), get(), get(), get(), get())
  }
  single { GraphQLQueryUseCase(get()) }
  single { GetInsuranceProvidersUseCase(get<ApolloClient>(giraffeClient), get(), get(isProductionQualifier)) }
  single<GetDanishAddressAutoCompletionUseCase> {
    GetDanishAddressAutoCompletionUseCase(get<ApolloClient>(giraffeClient))
  }
  single<GetFinalDanishAddressSelectionUseCase> { GetFinalDanishAddressSelectionUseCase(get()) }
  single {
    UploadMarketAndLanguagePreferencesUseCase(
      apolloClient = get<ApolloClient>(giraffeClient),
      languageService = get(),
    )
  }
  single { GetMarketingBackgroundUseCase(get<ApolloClient>(giraffeClient), get()) }
  single {
    UpdateApplicationLanguageUseCase(
      marketManager = get(),
      languageService = get(),
    )
  }
  single { GetInitialMarketPickerValuesUseCase(get<ApolloClient>(giraffeClient), get(), get(), get()) }
  single<EditCheckoutUseCase> {
    EditCheckoutUseCase(
      languageService = get(),
      graphQLQueryHandler = get(),
    )
  }
  single<QuoteCartEditStartDateUseCase> { QuoteCartEditStartDateUseCase(get<ApolloClient>(giraffeClient), get()) }
  single<EditCampaignUseCase> { EditCampaignUseCase(get<ApolloClient>(giraffeClient), get()) }
  single<AddPaymentTokenUseCase> { AddPaymentTokenUseCase(get<ApolloClient>(giraffeClient)) }
  single<ConnectPaymentUseCase> { ConnectPaymentUseCase(get(), get(), get()) }
  single<ConnectPayoutUseCase> { ConnectPayoutUseCase(get(giraffeClient), get()) }
  single<ObserveOfferStateUseCase> { ObserveOfferStateUseCase(get(), get()) }
  single<ChangeLanguageUseCase> {
    ChangeLanguageUseCase(
      apolloClient = get<ApolloClient>(giraffeClient),
      languageService = get(),
      cacheManager = get(),
    )
  }
}

private val cacheManagerModule = module {
  single { NetworkCacheManager(get<ApolloClient>(giraffeClient)) }
}

private val sharedPreferencesModule = module {
  single<SharedPreferences> {
    get<Context>().getSharedPreferences(
      "hedvig_shared_preference",
      MODE_PRIVATE,
    )
  }
}

private val datastoreAndroidModule = module {
  single<File>(datastoreFileQualifier) {
    // https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:datastore/datastore/src/main/java/androidx/datastore/DataStoreFile.kt;l=35-36
    get<Context>().applicationContext.filesDir
  }
}

private val logModule = module {
  single<LogInfoType>(logInfoQualifier) {
    ::i
  }
}

private val coilModule = module {
  single<ImageLoader> {
    ImageLoader.Builder(get())
      .okHttpClient(
        // For the OkHttp client used by Coil, we want to re-use the same configuration, but we do not want the token
        // related interceptors to be used, since those images are not behind authentication, and actually fail when
        // requested with an authorization token.
        get<OkHttpClient.Builder>()
          .apply {
            interceptors().removeAll {
              it is MigrateTokenInterceptor || it is AuthTokenRefreshingInterceptor
            }
          }
          .build(),
      )
      .components {
        add(SvgDecoder.Factory())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
          add(ImageDecoderDecoder.Factory())
        } else {
          add(GifDecoder.Factory())
        }
      }
      .build()
  }
}

private val chatEventModule = module {
  single<ChatEventStore> { ChatEventDataStore(get()) }
}

private val graphQLQueryModule = module {
  single<GraphQLQueryHandler> { GraphQLQueryHandler(get(), get(), get(giraffeGraphQLUrlQualifier)) }
}

private val authRepositoryModule = module {
  single<AuthRepository> {
    NetworkAuthRepository(
      environment = if (get(isProductionQualifier)) {
        AuthEnvironment.PRODUCTION
      } else {
        AuthEnvironment.STAGING
      },
      additionalHttpHeaders = mapOf(),
      callbacks = Callbacks("https://hedvig.com?q=success", "https://hedvig.com?q=failure)"), // Not used
    )
  }
}

private val workManagerModule = module {
  worker<ReplyWorker>(named<ReplyWorker>()) {
    ReplyWorker(
      context = get<Context>(),
      params = get<WorkerParameters>(),
      chatRepository = get<ChatRepository>(),
      chatNotificationSender = get<ChatNotificationSender>(),
    )
  }
}

val applicationModule = module {
  includes(
    listOf(
      activityNavigatorModule,
      adyenModule,
      apolloClientModule,
      apolloClientUrlsModule,
      authModule,
      authRepositoryModule,
      businessModelModule,
      cacheManagerModule,
      changeAddressModule,
      changeDateBottomSheetModule,
      chatEventModule,
      checkoutModule,
      claimFlowDataModule,
      claimTriagingModule,
      clockModule,
      coilModule,
      connectPaymentModule,
      coreCommonModule,
      dataStoreModule,
      datadogModule,
      datastoreAndroidModule,
      deepLinkModule,
      embarkModule,
      externalInsuranceModule,
      featureManagerModule,
      firebaseNotificationModule,
      foreverModule,
      graphQLQueryModule,
      hAnalyticsAndroidModule,
      hAnalyticsModule,
      homeModule,
      insurancesModule,
      languageModule,
      logModule,
      marketManagerModule,
      networkModule,
      notificationBadgeModule,
      notificationModule,
      numberActionSetModule,
      odysseyModule,
      offerModule,
      onboardingModule,
      paymentModule,
      profileModule,
      repositoriesModule,
      serviceModule,
      sharedPreferencesModule,
      stringConstantsModule,
      terminateInsuranceModule,
      textActionSetModule,
      travelCertificateDataModule,
      travelCertificateModule,
      trustlyModule,
      useCaseModule,
      valueStoreModule,
      viewModelModule,
      workManagerModule,
    ),
  )
}
