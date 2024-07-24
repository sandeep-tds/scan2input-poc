package com.sap.scan2input.ui.odata.viewmodel

import android.util.Property
import com.sap.scan2input.util.Converter
import com.sap.cloud.mobile.kotlin.odata.*
import com.sap.scan2input.util.Converter.ConvertResult
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

class ScannedDocumentParser {

    companion object {

        fun preFillEntity(emptyEntity: EntityValue, doxResponse: String?): EntityValue {

            val doxHeaderFieldsResponseMap = prepareMapForDoxHeaderFields(doxResponse)
            val propertiesList = emptyEntity.entityType.propertyList.toList()

            propertiesList.forEach { property ->
                val scannedPropertyValue = doxHeaderFieldsResponseMap[property.name] ?: ""

                val convertResult = Converter.convert(property, scannedPropertyValue)
                if (convertResult is ConvertResult.ConvertSuccess) {
                    emptyEntity.setDataValue(property, convertResult.data)
                }
            }
            return emptyEntity // Return the pre-filled entity
        }

        private fun prepareMapForDoxHeaderFields(doxResponse: String?): Map<String?, String?> {
            val doxJsonResponse = doxResponse?.let { Json.decodeFromString<JsonObject>(it) }

            // Create a map for efficient lookup
            val headerFieldMap = doxJsonResponse
                ?.get("extraction")
                ?.jsonObject
                ?.get("headerFields")
                ?.jsonArray
                ?.associate {
                    it.jsonObject["name"]?.jsonPrimitive?.contentOrNull to
                            it.jsonObject["value"]?.jsonPrimitive?.contentOrNull
                } ?: emptyMap()

            return headerFieldMap

        }
    }
}
