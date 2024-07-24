package com.sap.scan2input.ui.odata.viewmodel.stock

import android.app.Application
import com.sap.scan2input.ui.odata.viewmodel.ODataViewModel
import com.sap.cloud.android.odata.espmcontainer.ESPMContainerMetadata
import com.sap.cloud.mobile.kotlin.odata.EntityValue
import com.sap.cloud.mobile.kotlin.odata.Property

class StockODataViewModel(
    application: Application,
    private val orderByProperty: Property?,
    override val parent: EntityValue? = null,
    private val navigationPropertyName: String? = null,
) : ODataViewModel(
    application,
    ESPMContainerMetadata.EntityTypes.stock,
    ESPMContainerMetadata.EntitySets.stock,
    orderByProperty,
    parent,
    navigationPropertyName
) {

//    override fun getAvatarText(entity: EntityValue?): String {
//        val customer = entity as Customer?
//        return customer?.let { "${it.lastName?.first() ?: '?'}${it.firstName?.first() ?: '?'}" }
//            ?: "??"
//    }
}
