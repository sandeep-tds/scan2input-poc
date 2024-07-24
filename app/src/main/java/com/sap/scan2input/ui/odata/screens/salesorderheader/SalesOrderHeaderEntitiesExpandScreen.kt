package com.sap.scan2input.ui.odata.screens.salesorderheader

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.sap.scan2input.ui.odata.ScreenType
import com.sap.scan2input.ui.odata.getEntityScreenInfo
import com.sap.scan2input.ui.odata.screenTitle
import com.sap.scan2input.ui.odata.screens.DeleteEntityWithConfirmation
import com.sap.scan2input.ui.odata.screens.OperationScreen
import com.sap.scan2input.ui.odata.screens.OperationScreenSettings
import com.sap.scan2input.ui.odata.screens.getSelectedItemActionsList
import com.sap.scan2input.ui.odata.viewmodel.EntityOperationType
import com.sap.scan2input.ui.odata.viewmodel.EntityUpdateOperationType
import com.sap.scan2input.ui.odata.viewmodel.ODataViewModel
import com.sap.cloud.mobile.kotlin.odata.EntityValue
import com.sap.cloud.mobile.kotlin.odata.NavigationProperty

val SalesOrderHeaderEntitiesExpandScreen:
        @Composable (
            navigateToHome: () -> Unit,
            navigateUp: () -> Unit,
            onNavigateProperty: (EntityValue, NavigationProperty) -> Unit,
            viewModel: ODataViewModel,
        ) -> Unit =
    { navigateToHome, navigateUp, onNavigateProperty, viewModel ->
        val uiState by viewModel.odataUIState.collectAsState()
        Row(modifier = Modifier) {
            Box(modifier = Modifier.weight(1f)) {
                SalesOrderHeaderEntitiesScreen(
                    navigateToHome,
                    navigateUp,
                    viewModel,
                    true
                )
            }
            Box(modifier = Modifier.weight(2f)) {
                when (uiState.entityOperationType) {
                    EntityOperationType.DETAIL -> {
                        SalesOrderHeaderEntityDetailScreen(onNavigateProperty, null, viewModel, true)
                    }

                    EntityOperationType.CREATE, EntityUpdateOperationType.UPDATE_FROM_LIST, EntityUpdateOperationType.UPDATE_FROM_DETAIL -> {
                        SalesOrderHeaderEntityEditScreen(null, viewModel, true)
                    }

                    else -> {
                        SalesOrderHeaderBlankScreen(viewModel)
                    }
                }
            }
        }
    }

val SalesOrderHeaderBlankScreen:
        @Composable (
            viewModel: ODataViewModel,
        ) -> Unit =
    { viewModel ->
    val deleteState = remember {
        mutableStateOf(false)
    }
    OperationScreen(
        screenSettings = OperationScreenSettings(
            title = screenTitle(
                getEntityScreenInfo(viewModel.entityType, viewModel.entitySet),
                ScreenType.Details
            ),
            actionItems = getSelectedItemActionsList(
                viewModel,
                deleteState
            ),
            navigateUp = null,
        ),
        modifier = Modifier,
        viewModel = viewModel
    ) {
        Box(modifier = Modifier)
        DeleteEntityWithConfirmation(viewModel, deleteState)
    }
}
