package com.hedvig.android.feature.travelcertificate.ui.generatewhen

import androidx.compose.material3.DatePickerState
import androidx.compose.material3.DisplayMode
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.core.common.android.validation.validateEmail
import com.hedvig.android.data.travelcertificate.GetTravelCertificateSpecificationsUseCase
import com.hedvig.android.feature.travelcertificate.data.CreateTravelCertificateUseCase
import com.hedvig.android.feature.travelcertificate.data.TravelCertificateUrl
import com.hedvig.android.feature.travelcertificate.navigation.TravelCertificateDestination
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import hedvig.resources.R
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

internal class TravelCertificateDateInputViewModel(
  contractId: String?,
  getTravelCertificateSpecificationsUseCase: GetTravelCertificateSpecificationsUseCase,
  createTravelCertificateUseCase: CreateTravelCertificateUseCase,
) : MoleculeViewModel<TravelCertificateDateInputEvent, TravelCertificateDateInputUiState>(
    initialState = TravelCertificateDateInputUiState.Loading,
    presenter = TravelCertificateDateInputPresenter(
      contractId,
      getTravelCertificateSpecificationsUseCase,
      createTravelCertificateUseCase,
    ),
    sharingStarted = SharingStarted.WhileSubscribed(5.seconds),
  )

internal class TravelCertificateDateInputPresenter(
  private val contractId: String?,
  private val getTravelCertificateSpecificationsUseCase: GetTravelCertificateSpecificationsUseCase,
  private val createTravelCertificateUseCase: CreateTravelCertificateUseCase,
) : MoleculePresenter<TravelCertificateDateInputEvent, TravelCertificateDateInputUiState> {
  @Composable
  override fun MoleculePresenterScope<TravelCertificateDateInputEvent>.present(
    lastState: TravelCertificateDateInputUiState,
  ): TravelCertificateDateInputUiState {
    var loadIteration by remember { mutableIntStateOf(0) }

    var createTravelCertificateData by remember { mutableStateOf<CreateTravelCertificateData?>(null) }

    var screenContent by remember { mutableStateOf(
      when (lastState) {
        is TravelCertificateDateInputUiState.Success -> DateInputScreenContent.Success(
          DateInputScreenContent.Success.SpecificationsDetails(
            contractId = lastState.contractId,
            email = lastState.email,
            travelDate = lastState.travelDate,
            datePickerState = lastState.datePickerState,
            dateValidator = lastState.dateValidator,
            daysValid = lastState.daysValid,
            hasCoInsured = lastState.hasCoInsured,
          )
        )
        TravelCertificateDateInputUiState.Failure -> DateInputScreenContent.Failure
        TravelCertificateDateInputUiState.Loading -> DateInputScreenContent.Loading
        is TravelCertificateDateInputUiState.UrlFetched -> {
          logcat(LogPriority.ERROR) { "TravelCertificateDateInputUiState is UrlFetched, should be impossible" }
          DateInputScreenContent.Loading
        }
      })
    }

//    var travelDate by remember {
//      mutableStateOf( lastState.safeCast<TravelCertificateDateInputUiState.Success>()?.travelDate ?:
//        Clock.System.now()
//          .toLocalDateTime(TimeZone.currentSystemDefault()).date,
//      )
//    }
//
//    var contractIdFromBackend by remember { mutableStateOf<String?>(lastState.safeCast<TravelCertificateDateInputUiState.Success>()?.contractId) }
//
//    var email by remember {
//      mutableStateOf<String?>(lastState.safeCast<TravelCertificateDateInputUiState.Success>()?.email)
//    }

    var primaryInput by remember {
      mutableStateOf<TravelCertificateDestination.TravelCertificateTravellersInput.TravelCertificatePrimaryInput?>(null)
    }

    var errorMessage by remember { mutableStateOf<Int?>(null) }

    CollectEvents { event ->

      fun validateInputAndContinue() {
        val successScreenContent = screenContent as? DateInputScreenContent.Success ?: return
        if (successScreenContent.details.email != null && validateEmail(successScreenContent.details.email).isSuccessful) {
          if (successScreenContent.details.hasCoInsured) {
              val travelCertificatePrimaryInput = TravelCertificateDestination.TravelCertificateTravellersInput.TravelCertificatePrimaryInput(
                successScreenContent.details.email,
                successScreenContent.details.travelDate,
                successScreenContent.details.contractId,
              )
            primaryInput = travelCertificatePrimaryInput
          } else {
              createTravelCertificateData = CreateTravelCertificateData(
                successScreenContent.details.contractId,
                successScreenContent.details.travelDate,
                successScreenContent.details.email,)

          }
          errorMessage = null
        } else {
          errorMessage = if (successScreenContent.details.email.isNullOrEmpty()) {
            R.string.travel_certificate_email_empty_error
          } else {
            R.string.PROFILE_MY_INFO_INVALID_EMAIL
          }
        }
      }

      when (event) {
        is TravelCertificateDateInputEvent.ChangeEmailInput -> {
          val successScreenContent = screenContent as? DateInputScreenContent.Success ?: return@CollectEvents
          screenContent = successScreenContent.copy(
            details = successScreenContent.details.copy(
              email = event.email
            )
          )
        }

        is TravelCertificateDateInputEvent.ChangeDateInput -> {
          val successScreenContent = screenContent as? DateInputScreenContent.Success ?: return@CollectEvents
          screenContent = successScreenContent.copy(
            details = successScreenContent.details.copy(
             travelDate = event.localDate
            )
          )
        }

        TravelCertificateDateInputEvent.RetryLoadData -> {
          loadIteration++
        }

        TravelCertificateDateInputEvent.NullifyPrimaryInput -> {
          primaryInput = null
        }

        is TravelCertificateDateInputEvent.Submit -> {
          validateInputAndContinue()
        }
      }
    }

    LaunchedEffect(createTravelCertificateData) {
      val currentCreateTravelCertificateData = createTravelCertificateData ?: return@LaunchedEffect
      screenContent = DateInputScreenContent.Loading
      createTravelCertificateUseCase.invoke(
        contractId = currentCreateTravelCertificateData.contractId,
        startDate = currentCreateTravelCertificateData.travelDate,
        isMemberIncluded = true,
        coInsured = listOf(),
        email = currentCreateTravelCertificateData.email,
      ).fold(
        ifLeft = { _ ->
          screenContent = DateInputScreenContent.Failure
        },
        ifRight = { url ->
          screenContent = DateInputScreenContent.UrlFetched(url)
        },
      )
      createTravelCertificateData = null
    }

    LaunchedEffect(loadIteration) {
      if (screenContent is DateInputScreenContent.Success) {
        return@LaunchedEffect
      }
      screenContent = DateInputScreenContent.Loading
      getTravelCertificateSpecificationsUseCase
        .invoke(contractId)
        .fold(
          ifLeft = { _ ->
            screenContent = DateInputScreenContent.Failure
          },
          ifRight = { travelCertificateData ->
            val travelSpecification = travelCertificateData.travelCertificateSpecification
            val datePickerState = DatePickerState(
              initialSelectedDateMillis = null,
              initialDisplayedMonthMillis = null,
              yearRange = travelSpecification.dateRange.start.year..travelSpecification.dateRange.endInclusive.year,
              initialDisplayMode = DisplayMode.Picker,
            )
            screenContent = DateInputScreenContent.Success(
              DateInputScreenContent.Success.SpecificationsDetails(
                datePickerState = datePickerState,
                dateValidator = { date ->
                  val selectedDate =
                    Instant.fromEpochMilliseconds(date).toLocalDateTime(TimeZone.currentSystemDefault()).date
                  travelSpecification.dateRange.contains(selectedDate)
                },
                daysValid = travelSpecification.maxDurationDays,
                email = travelSpecification.email,
                  contractId = travelSpecification.contractId,
                hasCoInsured = travelSpecification.numberOfCoInsured > 0,
                travelDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
              ),
            )
          },
        )
    }
    return when (val currentContent = screenContent) {
      DateInputScreenContent.Failure -> TravelCertificateDateInputUiState.Failure
      DateInputScreenContent.Loading -> TravelCertificateDateInputUiState.Loading
      is DateInputScreenContent.Success -> {

        TravelCertificateDateInputUiState.Success(
          email = currentContent.details.email,
          travelDate = currentContent.details.travelDate,
          contractId = currentContent.details.contractId,
          hasCoInsured = currentContent.details.hasCoInsured,
          datePickerState = currentContent.details.datePickerState,
          dateValidator = currentContent.details.dateValidator,
          daysValid = currentContent.details.daysValid,
          primaryInput = primaryInput,
          errorMessageRes = errorMessage,
        )
      }

      is DateInputScreenContent.UrlFetched -> {
        TravelCertificateDateInputUiState.UrlFetched(currentContent.travelCertificateUrl)
      }
    }
  }
}

private sealed interface DateInputScreenContent {
  data object Loading : DateInputScreenContent

  data object Failure : DateInputScreenContent

  data class Success(val details: SpecificationsDetails) : DateInputScreenContent {
    data class SpecificationsDetails(
      val contractId: String,
      val email: String?,
      val travelDate: LocalDate,
      val datePickerState: DatePickerState,
      val dateValidator: (Long) -> Boolean,
      val daysValid: Int,
      val hasCoInsured: Boolean,
    )
  }

  data class UrlFetched(val travelCertificateUrl: TravelCertificateUrl) : DateInputScreenContent
}

internal sealed interface TravelCertificateDateInputEvent {
  data object RetryLoadData : TravelCertificateDateInputEvent

  data class ChangeDateInput(val localDate: LocalDate) : TravelCertificateDateInputEvent
  data class ChangeEmailInput(val email: String) : TravelCertificateDateInputEvent
  data object Submit : TravelCertificateDateInputEvent

  data object NullifyPrimaryInput : TravelCertificateDateInputEvent
}

internal sealed interface TravelCertificateDateInputUiState {
  data object Loading : TravelCertificateDateInputUiState

  data object Failure : TravelCertificateDateInputUiState

  data class UrlFetched(val travelCertificateUrl: TravelCertificateUrl) : TravelCertificateDateInputUiState

  data class Success(
    val contractId: String,
    val email: String?,
    val travelDate: LocalDate,
    val hasCoInsured: Boolean,
    val datePickerState: DatePickerState,
    val dateValidator: (Long) -> Boolean,
    val daysValid: Int,
    val primaryInput: TravelCertificateDestination.TravelCertificateTravellersInput.TravelCertificatePrimaryInput?,
    val errorMessageRes: Int?,
  ) : TravelCertificateDateInputUiState
}

private data class CreateTravelCertificateData(
  val contractId: String,
  val travelDate: LocalDate,
  val email: String,
)
