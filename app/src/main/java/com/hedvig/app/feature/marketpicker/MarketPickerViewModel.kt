package com.hedvig.app.feature.marketpicker

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.PreferenceManager
import com.hedvig.app.BaseActivity
import com.hedvig.app.feature.settings.Language
import com.hedvig.app.feature.settings.SettingsActivity
import com.hedvig.app.makeLocaleString
import com.hedvig.app.util.extensions.getLanguage
import com.hedvig.app.util.extensions.getMarket
import kotlinx.coroutines.launch

abstract class MarketPickerViewModel : ViewModel() {
    abstract val data: MutableLiveData<PickerState>
    abstract fun saveIfNotDirty()
    abstract fun uploadLanguage()
    abstract var dirty: Boolean
}

class MarketPickerViewModelImpl(
    private val marketRepository: MarketRepository,
    private val languageRepository: LanguageRepository,
    private val context: Context
) : MarketPickerViewModel() {
    override val data = MutableLiveData<PickerState>()

    override var dirty = false

    init {
        viewModelScope.launch {

            if (context.getMarket() == null) {
                val geo = runCatching { marketRepository.geoAsync().await() }
                geo.getOrNull()?.data?.let {
                    runCatching {
                        val market: Market
                        try {
                            market = Market.valueOf(it.geo.countryISOCode)
                            when (market) {
                                Market.SE -> data.postValue(PickerState(market, Language.EN_SE))
                                Market.NO -> data.postValue(PickerState(market, Language.EN_NO))
                            }
                        } catch (e: Exception) {
                            data.postValue(
                                PickerState(
                                    Market.SE, Language.EN_SE
                                )
                            )
                        }

                    }
                }
            } else {
                context.getMarket()?.let { market ->
                    data.postValue(
                        PickerState(
                            market, context.getLanguage()
                        )
                    )
                }
            }
        }
    }

    @SuppressLint("ApplySharedPref") // We want to apply this right away. It's important
    fun save() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

        data.value?.let { data ->
            sharedPreferences.edit()
                .putString(
                    Market.MARKET_SHARED_PREF,
                    data.market?.name
                )
                .commit()

            sharedPreferences
                .edit()
                .putString(SettingsActivity.SETTING_LANGUAGE, data.language.toString())
                .commit()

            reload()

            dirty = true
        }
    }

    private fun reload() {
        LocalBroadcastManager
            .getInstance(context)
            .sendBroadcast(Intent(BaseActivity.LOCALE_BROADCAST))
    }

    override fun saveIfNotDirty() {
        if (!dirty) {
            save()
        }
    }

    override fun uploadLanguage() {
        data.value?.let { data ->
            data.language?.apply(context)?.let { language ->
                languageRepository.setLanguage(makeLocaleString(language))
            }
        }
    }
}

data class PickerState(
    val market: Market?,
    val language: Language?
)

sealed class Model {
    object Title : Model()
    data class MarketModel(
        val selection: Market?
    ) : Model()

    data class LanguageModel(
        val selection: Language?
    ) : Model()

    object Button : Model()
}

