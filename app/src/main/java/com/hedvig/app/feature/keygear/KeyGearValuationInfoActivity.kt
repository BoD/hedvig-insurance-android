package com.hedvig.app.feature.keygear

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.util.extensions.observe
import com.hedvig.app.util.extensions.setMarkdownText
import com.hedvig.app.util.interpolateTextKey
import kotlinx.android.synthetic.main.activity_key_gear_valuation_info.*
import org.koin.android.viewmodel.ext.android.viewModel

class KeyGearValuationInfoActivity : BaseActivity(R.layout.activity_key_gear_valuation_info) {

    private val model: KeyGearValuationInfoViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val id = intent.getStringExtra(ITEM_ID)

        model.data.observe(this) { data ->
            //TODO get all data
            val category = data?.fragments?.keyGearItemFragment?.category.toString()
            val purchasePrice = data?.fragments?.keyGearItemFragment?.purchasePrice.toString()

            setPercentage(47)
            //TODO fix correct markdown from * to **
            body.setMarkdownText(
                interpolateTextKey(
                    getString(R.string.KEY_GEAR_ITEM_VIEW_VALUATION_BODY),
                    "ITEM_TYPE" to category,
                    "ITEM_VALUATION" to 70,
                    "PURCHASE_PRICE" to purchasePrice,
                    "VALUATION_PRICE" to 1234
                )
            )
        }
        model.loadItem(id)
    }

    private fun setPercentage(percentage: Int) {
        valuationPercentage.setMarkdownText("$percentage%")
    }

    companion object {
        private const val ITEM_ID = "ITEM_ID"

        fun newInstance(context: Context, id: String) =
            Intent(context, KeyGearValuationInfoActivity::class.java).apply {
                putExtra(ITEM_ID, id)
            }
    }
}


