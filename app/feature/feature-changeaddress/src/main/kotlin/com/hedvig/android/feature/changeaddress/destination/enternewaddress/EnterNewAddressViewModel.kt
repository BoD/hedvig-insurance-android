package com.hedvig.android.feature.changeaddress.destination.enternewaddress

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.core.ui.ValidatedInput
import com.hedvig.android.feature.changeaddress.DatePickerUiState
import com.hedvig.android.feature.changeaddress.data.ChangeAddressRepository
import com.hedvig.android.feature.changeaddress.data.HousingType.VILLA
import com.hedvig.android.feature.changeaddress.data.MoveQuote
import com.hedvig.android.feature.changeaddress.destination.createQuoteInput
import com.hedvig.android.feature.changeaddress.destination.enternewaddress.EnterNewAddressEvent.ChangeCoInsured
import com.hedvig.android.feature.changeaddress.destination.enternewaddress.EnterNewAddressEvent.ChangeIsStudent
import com.hedvig.android.feature.changeaddress.destination.enternewaddress.EnterNewAddressEvent.ChangeMoveDate
import com.hedvig.android.feature.changeaddress.destination.enternewaddress.EnterNewAddressEvent.ChangePostalCode
import com.hedvig.android.feature.changeaddress.destination.enternewaddress.EnterNewAddressEvent.ChangeSquareMeters
import com.hedvig.android.feature.changeaddress.destination.enternewaddress.EnterNewAddressEvent.ChangeStreet
import com.hedvig.android.feature.changeaddress.destination.enternewaddress.EnterNewAddressEvent.DismissErrorDialog
import com.hedvig.android.feature.changeaddress.destination.enternewaddress.EnterNewAddressEvent.SubmitNewAddress
import com.hedvig.android.feature.changeaddress.destination.enternewaddress.EnterNewAddressEvent.ValidateInput
import com.hedvig.android.feature.changeaddress.navigation.NewAddressParameters
import com.hedvig.android.feature.changeaddress.navigation.SelectHousingTypeParameters
import com.hedvig.android.language.LanguageService
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import kotlinx.datetime.LocalDate

internal class EnterNewAddressViewModel(
  previousParameters: SelectHousingTypeParameters,
  languageService: LanguageService,
  changeAddressRepository: ChangeAddressRepository,
) : MoleculeViewModel<EnterNewAddressEvent, EnterNewAddressUiState>(
  initialState = EnterNewAddressUiState(
    datePickerUiState = DatePickerUiState(
      locale = languageService.getLocale(),
      initiallySelectedDate = null,
      minDate = previousParameters.minDate,
      maxDate = previousParameters.maxDate,
    ),
    maxNumberCoInsured = previousParameters.maxNumberCoInsured,
    maxSquareMeters = previousParameters.maxSquareMeters,
  ),
  presenter = EnterNewAddressPresenter(
    previousParameters,
    changeAddressRepository,
  ),
)

internal class EnterNewAddressPresenter(
  private val previousParameters: SelectHousingTypeParameters,
  private val changeAddressRepository: ChangeAddressRepository,
) : MoleculePresenter<EnterNewAddressEvent, EnterNewAddressUiState> {
  @Composable
  override fun MoleculePresenterScope<EnterNewAddressEvent>.present(
    lastState: EnterNewAddressUiState,
  ): EnterNewAddressUiState {

    var currentState by remember {
      mutableStateOf(lastState)
    }

    var dataLoadIteration by remember {
      mutableIntStateOf(0)
    }

    CollectEvents { event ->
      when (event) {
        is ChangeCoInsured -> {
          currentState = currentState.copy(numberInsured = ValidatedInput(event.coInsured))
        }

        is ChangeIsStudent -> {
          currentState = currentState.copy(isStudent = event.isStudent)
        }

        is ChangeMoveDate -> {
          currentState = currentState.copy(movingDate = ValidatedInput(event.movingDate))
        }

        is ChangePostalCode -> {
          currentState = currentState.copy(postalCode = ValidatedInput(event.postalCode))
        }

        is ChangeSquareMeters -> {
          currentState = currentState.copy(squareMeters = ValidatedInput(event.squareMeters))
        }

        is ChangeStreet -> {
          currentState = currentState.copy(street = ValidatedInput(event.street))
        }

        DismissErrorDialog -> {
          currentState = currentState.copy(errorMessage = null)
        }

        SubmitNewAddress -> {
          TODO()

        }

        ValidateInput -> {
          currentState = currentState.validateAddressInput()
          val stateSnapShot = currentState
          val isInputValid = stateSnapShot.isAddressInputValid
          if (isInputValid) {
            if (previousParameters.housingType == VILLA) {
              val params = NewAddressParameters(
                selectHousingTypeParameters = previousParameters,
                street = stateSnapShot.street.input!!,
                postalCode = stateSnapShot.postalCode.input!!,
                squareMeters = stateSnapShot.squareMeters.input!!,
                numberInsured = stateSnapShot.numberInsured.input!!,
                isStudent = stateSnapShot.isStudent,
                movingDate = stateSnapShot.movingDate.input!!, //todo: are we okay with doing it this way if isAddressInputValid does not smart cast?
              )
              currentState = currentState.copy(navParamsForVillaDestination = params)
            } else {
              dataLoadIteration++
            }
          }
        }
      }
    }

    LaunchedEffect(dataLoadIteration) {
      if (dataLoadIteration > 0) {
        val input = createQuoteInput(
          housingType = previousParameters.housingType,
          isStudent = currentState.isStudent,
          moveIntentId = previousParameters.moveIntentId,
          street = currentState.street.input,
          postalCode = currentState.postalCode.input,
          moveFromAddressId = previousParameters.moveFromAddressId,
          movingDate = currentState.movingDate.input,
          numberInsured = currentState.numberInsured.input,
          squareMeters = currentState.squareMeters.input,
          yearOfConstruction = null,
          ancillaryArea = null,
          numberOfBathrooms = null,
          extraBuildings = listOf(),
          isSublet = false,
          //todo: add clearing nav Params!
        )
        currentState = currentState.copy(isLoading = true)
        changeAddressRepository.createQuotes(input).fold(
          ifLeft = { error ->
            currentState = currentState.copy(
              isLoading = false,
              errorMessage = error.message,
            )
          },
          ifRight = { quotes ->
            currentState = currentState.copy(
              isLoading = false,
              navParamsForOfferDestination = quotes,
            )
          },
        )
      }
    }
    return currentState
  }
}

internal data class EnterNewAddressUiState(
  val isLoading: Boolean = false,
  val errorMessage: String? = null,
  val street: ValidatedInput<String?> = ValidatedInput(null),
  val postalCode: ValidatedInput<String?> = ValidatedInput(null),
  val squareMeters: ValidatedInput<String?> = ValidatedInput(null),
  val numberInsured: ValidatedInput<String?> = ValidatedInput(null),
  val datePickerUiState: DatePickerUiState? = null,
  val isEligibleForStudent: Boolean = false,
  val isStudent: Boolean = false,
  val movingDate: ValidatedInput<LocalDate?> = ValidatedInput(null),
  val maxNumberCoInsured: Int? = null,
  val maxSquareMeters: Int? = null,
  val navParamsForVillaDestination: NewAddressParameters? = null,
  val navParamsForOfferDestination: List<MoveQuote>? = null,
) {
  val isAddressInputValid: Boolean
    get() {
      return street.errorMessageRes == null &&
        postalCode.errorMessageRes == null &&
        squareMeters.errorMessageRes == null &&
        isSquareMetersWithinBounds(squareMeters.input?.toIntOrNull()) &&
        movingDate.errorMessageRes == null &&
        numberInsured.errorMessageRes == null
    }

  fun validateAddressInput(): EnterNewAddressUiState {
    return copy(
      street = street.copy(
        errorMessageRes = if (!street.isPresent || street.input?.isBlank() == true) {
          hedvig.resources.R.string.CHANGE_ADDRESS_STREET_ERROR
        } else {
          null
        },
      ),
      postalCode = postalCode.copy(
        errorMessageRes = if (!postalCode.isPresent || postalCode.input?.isBlank() == true) {
          hedvig.resources.R.string.CHANGE_ADDRESS_POSTAL_CODE_ERROR
        } else {
          null
        },
      ),
      squareMeters = squareMeters.copy(
        errorMessageRes = if (!squareMeters.isPresent || squareMeters.input?.isBlank() == true) {
          hedvig.resources.R.string.CHANGE_ADDRESS_LIVING_SPACE_ERROR
        } else if (squareMeters.isPresent && !isSquareMetersWithinBounds(squareMeters.input!!.toIntOrNull())) {
          hedvig.resources.R.string.CHANGE_ADDRESS_LIVING_SPACE_OVER_LIMIT_ERROR
        } else {
          null
        },
      ),
      movingDate = movingDate.copy(
        errorMessageRes = if (!movingDate.isPresent) {
          hedvig.resources.R.string.CHANGE_ADDRESS_MOVING_DATE_ERROR
        } else {
          null
        },
      ),
      numberInsured = numberInsured.copy(
        errorMessageRes = if (!numberInsured.isPresent) {
          hedvig.resources.R.string.CHANGE_ADDRESS_CO_INSURED_ERROR
        } else if (numberInsured.isPresent && !isNumberCoInsuredWithinBounds(numberInsured.input!!.toIntOrNull())) {
          hedvig.resources.R.string.CHANGE_ADDRESS_CO_INSURED_MAX_ERROR_ALTERNATIVE
        } else {
          null
        },
      ),
    )
  }

  private fun isSquareMetersWithinBounds(squareMeters: Int?): Boolean {
    if (maxSquareMeters == null) {
      return true
    }
    if (squareMeters == null) {
      return false
    }
    return squareMeters <= maxSquareMeters
  }

  private fun isNumberCoInsuredWithinBounds(numberCoInsured: Int?): Boolean {
    if (maxNumberCoInsured == null) {
      return true
    }
    if (numberCoInsured == null) {
      return false
    }
    return numberCoInsured <= maxNumberCoInsured
  }
}

internal sealed interface EnterNewAddressEvent {
  data object DismissErrorDialog : EnterNewAddressEvent

  data class ChangeCoInsured(val coInsured: String) : EnterNewAddressEvent

  data class ChangeSquareMeters(val squareMeters: String) : EnterNewAddressEvent

  data class ChangePostalCode(val postalCode: String) : EnterNewAddressEvent

  data class ChangeStreet(val street: String) : EnterNewAddressEvent

  data class ChangeIsStudent(val isStudent: Boolean) : EnterNewAddressEvent

  data class ChangeMoveDate(val movingDate: LocalDate) : EnterNewAddressEvent

  data object SubmitNewAddress : EnterNewAddressEvent //todo maybe this one should be private actually

  data object ValidateInput : EnterNewAddressEvent
}
