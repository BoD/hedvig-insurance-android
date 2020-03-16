package com.hedvig.app.feature.marketpicker

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.app.R
import com.hedvig.app.feature.language.LanguageAndMarketViewModel
import com.hedvig.app.feature.settings.Language
import com.hedvig.app.feature.settings.LanguageModel
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.spring
import d
import kotlinx.android.synthetic.main.language_item_new.view.*

class LanguageAdapterNew(
    private val model: LanguageAndMarketViewModel
) : RecyclerView.Adapter<LanguageAdapterNew.ViewHolder>() {
    private var lastCheckedPos = 0
    private var lastChecked: RadioButton? = null

    var selectedMarket = Market.SE
    var items: List<LanguageModel> = listOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(parent)

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (items[position].selected) {
            animateRadioButton(holder)
            holder.button.background =
                holder.button.context.getDrawable(R.drawable.ic_radio_button_checked)
        } else {
            holder.button.background =
                holder.button.context.getDrawable(R.drawable.ic_radio_button_unchecked)
        }
        when (position) {
            LOCAL -> {
                when (selectedMarket) {
                    Market.SE -> {
                        holder.itemView.apply {
                            language.text =
                                language.resources.getString(R.string.SETTINGS_LANGUAGE_SWEDISH)
                            setHapticClickListener {
                                model.selectLanguage(Language.SV_SE)
                            }
                        }
                    }
                    Market.NO -> {
                        holder.itemView.apply {
                            language.text = language.resources.getString(R.string.norwegian)
                            setHapticClickListener {
                                model.selectLanguage(Language.NB_NO)

                            }
                        }
                    }
                }
            }
            EN -> holder.itemView.apply {
                language.text =
                    language.resources.getString(R.string.SETTINGS_LANGUAGE_ENGLISH)
            }
        }

        if (position == 0 && holder.itemView.radioButton.isChecked) {
            lastChecked = holder.itemView.radioButton
            lastCheckedPos = 0
        }

        holder.itemView.setHapticClickListener { v ->
            when (position) {
                EN -> {
                    when (selectedMarket) {
                        Market.SE -> {
                            model.selectLanguage(Language.EN_SE)
                            d { "EN_SE" }
                        }
                        Market.NO -> {
                            model.selectLanguage(Language.EN_NO)
                            d { "EN_NO" }
                        }
                    }
                }
                LOCAL -> {
                    when (selectedMarket) {
                        Market.SE -> {
                            model.selectLanguage(Language.SV_SE)
                            d { "SV_SE" }
                        }
                        Market.NO -> {
                            model.selectLanguage(Language.NB_NO)
                            d { "NB_NO" }
                        }
                    }
                }
            }
            val rb = v.radioButton
            rb.isChecked = true
            rb.background = rb.context.getDrawable(R.drawable.ic_radio_button_checked)
            animateRadioButton(holder)
            if (rb.isChecked) {
                lastChecked?.let { rb ->
                    if (lastCheckedPos != position) {
                        rb.background =
                            rb.context.getDrawable(R.drawable.ic_radio_button_unchecked)
                        rb.isChecked = false
                        animateRadioButton(holder)
                    }
                }
                lastChecked = rb
                lastCheckedPos = position
            } else lastChecked = null
        }
    }

    private fun animateRadioButton(holder: ViewHolder) {
        holder.button.apply {
            scaleX = 0f
            scaleY = 0f
            spring(
                SpringAnimation.SCALE_X,
                SpringForce.STIFFNESS_HIGH,
                SpringForce.DAMPING_RATIO_NO_BOUNCY
            ).animateToFinalPosition(1f)
            spring(
                SpringAnimation.SCALE_Y,
                SpringForce.STIFFNESS_HIGH,
                SpringForce.DAMPING_RATIO_NO_BOUNCY
            ).animateToFinalPosition(1f)
        }
    }

    class ViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(
            R.layout.language_item_new,
            parent,
            false
        )
    ) {
        val button: RadioButton = itemView.radioButton
    }

    companion object {
        private const val LOCAL = 0
        private const val EN = 1
    }
}
