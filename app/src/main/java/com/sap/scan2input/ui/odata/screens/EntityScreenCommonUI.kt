package com.sap.scan2input.ui.odata.screens

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.sap.scan2input.R
import com.sap.scan2input.ui.AlertDialogComponent
import com.sap.scan2input.ui.odata.ActionItem
import com.sap.scan2input.ui.odata.OverflowMode
import com.sap.scan2input.ui.odata.viewmodel.ODataViewModel
import com.sap.cloud.mobile.kotlin.odata.EntityValue
import com.sap.cloud.mobile.kotlin.odata.Property
import com.sap.cloud.mobile.fiori.compose.avatar.model.FioriAvatarData
import com.sap.cloud.mobile.fiori.compose.common.FioriIcon
import com.sap.cloud.mobile.fiori.compose.common.FioriImage
import com.sap.cloud.mobile.fiori.compose.objectheader.model.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DeleteEntityWithConfirmation(viewModel: ODataViewModel, confirmState: MutableState<Boolean>) {
    val context = LocalContext.current

    if (confirmState.value) {
        AlertDialogComponent(title = context.getString(R.string.delete_dialog_title),
            text = context.getString(R.string.delete_one_item),
            onNegativeButtonClick = { confirmState.value = false },
            positiveButtonText = context.getString(R.string.delete),
            onPositiveButtonClick = {
                confirmState.value = false
                viewModel.onDeleteAction()
            })
    }
}

@Composable
fun LeaveEditorWithConfirmation(onLeave: () -> Unit, confirmState: MutableState<Boolean>) {
    val context = LocalContext.current
    if(confirmState.value) {
        AlertDialogComponent(
            title = context.getString(R.string.before_navigation_dialog_title),
            text = context.getString(R.string.before_navigation_dialog_message),
            onNegativeButtonClick = { confirmState.value = false },
            positiveButtonText = context.getString(R.string.before_navigation_dialog_positive_button),
            negativeButtonText = context.getString(R.string.before_navigation_dialog_negative_button),
            onPositiveButtonClick = {
                confirmState.value = false
                onLeave()
            }
        )
    }
}

@Composable
fun getSelectedItemActionsList(
    navigateToHome: () -> Unit,
    viewModel: ODataViewModel,
    deleteState: MutableState<Boolean>
): List<ActionItem> {

    val uiState = viewModel.odataUIState.collectAsState()

    val context = LocalContext.current
    var imageFile by remember { mutableStateOf<File?>(null) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess: Boolean ->
            if (isSuccess) {
                viewModel.sendDocToDoxService(imageFile!!)

                // You can define better experiences for the user while the image is being processed on BTP without disrupting the app flow.
                val alertDialog = AlertDialog.Builder(context)
                    .setTitle("Processing Image")
                    .setMessage("Please wait while we extract details from the image. This may take up to 30 seconds. Once complete, the create screen will open with the pre-filled data.")
                    .setPositiveButton("OK", null)
                    .create()
                alertDialog.show()
            } else {
                // Handle image capture failure
            }
        }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            imageFile = createImageFile(context)
            imageUri = FileProvider.getUriForFile( // Get URI for the file
                context,
                "${context.packageName}.provider",
                imageFile!!
            )
            imageUri?.let { launcher.launch(it) } // Launch with the URI
        } else {
            // Permission denied, show a message
            Toast.makeText(
                context,
                "This feature will not work without camera permissions.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    return when (uiState.value.selectedItems.size) {
        0 -> listOf(
            ActionItem(
                nameRes = R.string.menu_home,
                iconRes = R.drawable.ic_sap_icon_home,
                overflowMode = OverflowMode.IF_NECESSARY,
                doAction = navigateToHome
            ), ActionItem(
                nameRes = R.string.menu_refresh,
                iconRes = R.drawable.ic_sap_icon_refresh,
                overflowMode = OverflowMode.IF_NECESSARY,
                doAction = viewModel::refreshEntities
            ), ActionItem(
                nameRes = com.sap.cloud.mobile.fiori.compose.R.string.scan_button,
                iconRes =com.sap.cloud.mobile.fiori.theme.R.drawable.ic_sap_icon_scan,
                doAction = {
                    // Request camera permission
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                },
                overflowMode = OverflowMode.IF_NECESSARY
            )
        )

        else -> getSelectedItemActionsList(
            viewModel,
            deleteState
        )
    }
}

// Helper function to create a Uri for the photo file
fun createImageFile(context: Context): File {
    val timeStamp = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault()).format(Date())
    val imageFileName = "DOX_POC_" + timeStamp + "_"
    val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    return File.createTempFile(imageFileName, ".jpg", storageDir)
}

@Composable
fun getSelectedItemActionsList(
    viewModel: ODataViewModel,
    deleteState: MutableState<Boolean>
): List<ActionItem> {
    val uiState = viewModel.odataUIState.collectAsState()
    return when (uiState.value.selectedItems.size) {
        0 -> listOf() //should not happen
        1 -> listOf(
            ActionItem(
                nameRes = R.string.menu_update,
                iconRes = R.drawable.ic_sap_icon_edit,
                overflowMode = OverflowMode.IF_NECESSARY,
                doAction = viewModel::onEditAction
            ),
            ActionItem(
                nameRes = R.string.menu_delete,
                iconRes = R.drawable.ic_sap_icon_delete,
                overflowMode = OverflowMode.IF_NECESSARY,
                doAction = { deleteState.value = true }
            ),
        )

        else -> listOf(
            ActionItem(
                nameRes = R.string.menu_delete,
                iconRes = R.drawable.ic_sap_icon_delete,
                overflowMode = OverflowMode.IF_NECESSARY,
                doAction = { deleteState.value = true }
            ),
        )
    }
}

data class StateIcon(@DrawableRes val icon: Int, @StringRes val desc: Int)

fun getEntityStateIcon(it: EntityValue): StateIcon {
    return when {
        it.inErrorState -> StateIcon(
            R.drawable.ic_error_state, R.string.error_state
        )

        it.isUpdated -> StateIcon(
            R.drawable.ic_updated_state, R.string.updated_state
        )
        it.isLocal -> StateIcon(
            R.drawable.ic_local_state, R.string.local_state
        )
        else ->
            StateIcon(
                R.drawable.ic_download_state, R.string.download_state
            )
    }

}

sealed interface ResultOf<out Bitmap> {
    data object Loading : ResultOf<Nothing>
    data class Success(val bitmap: Bitmap) : ResultOf<Bitmap>
    data object Failure : ResultOf<Nothing>
}


fun defaultObjectHeaderData(
    title: String,
    imageByteArray: ByteArray? = null,
    imageUrl: String? = null,
    imageChars: String? = null
): FioriObjectHeaderData {

    val content = FioriObjectHeaderData(
        title
    )

    content.detailImage = imageByteArray?.let {
        if (it.isNotEmpty()) {
            FioriAvatarData(
                FioriImage(
                    bitmap = BitmapFactory.decodeByteArray(it, 0, it.size),
                    contentDescription = "Detail image"
                )
            )
        } else {
            //load image fail
            imageChars?.let {char ->
                FioriAvatarData(
                    text = char,
                    textFontSize = 24.sp,
                )
            }
        }
    } ?: imageUrl?.let {
        FioriAvatarData(
            FioriImage(
                url = it,
                contentDescription = "Detail image"
            )
        )
    } ?: imageChars?.let {
        FioriAvatarData(
            text = it,
            textFontSize = 24.sp,
        )
    } ?: FioriAvatarData(
        FioriImage(
            resId = R.drawable.ic_sync,
            contentDescription = "Loading image"
        )
    )

    content.subtitle = "This is a subtitle that can take up to a maximum of three lines."

    content.accessoryKpi = "accKpi"
    content.accessoryKpiLabel = "accKpiLabel"



    content.status = FioriObjectHeaderStatusData(
        label = "Status",
        icon = FioriIcon(
            resId = R.drawable.ic_sap_icon_home,
            contentDescription = "Positive status icon",
        ),
        type = FioriObjectHeaderStatusType.Positive,
        isIconAtStart = true
    )
    content.subStatus = FioriObjectHeaderStatusData(
        label = "SubStatus",
        icon = FioriIcon(
            resId = R.drawable.ic_error_state,
            contentDescription = "Critical status icon",
        ),
        type = FioriObjectHeaderStatusType.Critical,
        isIconAtStart = false
    )


    content.labelItems = listOf(
        FioriObjectHeaderLabelItemData(
            label = "Attribute1"
        ),
        FioriObjectHeaderLabelItemData(
            label = "Attribute2",
            icon = FioriIcon(
                resId = R.drawable.ic_sap_icon_home
            ),
            isIconAtStart = false
        ),
        FioriObjectHeaderLabelItemData(
            label = "Subtitle"
        ),
        FioriObjectHeaderLabelItemData(
            label = "3/28/2022",
            icon = FioriIcon(
                resId = R.drawable.ic_sap_icon_delete
            )
        )
    )


    content.description =
        "description show here"


    return content
}

data class FieldUIState(
    val value: String,
    val property: Property,
    val isError: Boolean = false,
    val errorMessage: String? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (other !is FieldUIState) return false
        return (this.value == other.value && this.property.name == other.property.name && this.isError == other.isError && errorMessage == other.errorMessage)
    }

    override fun hashCode(): Int {
        var result = value.hashCode()
        result = 31 * result + property.hashCode()
        result = 31 * result + isError.hashCode()
        result = 31 * result + (errorMessage?.hashCode() ?: 0)
        return result
    }
}
