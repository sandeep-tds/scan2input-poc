package com.sap.scan2input.app

import android.app.Activity
import android.text.SpannedString
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.core.text.getSpans
import com.sap.scan2input.BuildConfig
import com.sap.cloud.mobile.fiori.compose.dialog.FioriAlertDialog
import com.sap.cloud.mobile.fiori.compose.theme.fioriHorizonAttributes
import com.sap.scan2input.R
import com.sap.scan2input.ui.WelcomeActivity
import com.sap.cloud.mobile.flows.compose.ext.CustomStepInsertionPoint
import com.sap.cloud.mobile.flows.compose.ext.FlowActionHandler
import com.sap.cloud.mobile.flows.compose.flows.BaseFlow
import com.sap.cloud.mobile.flows.compose.flows.FlowType
import com.sap.cloud.mobile.foundation.ext.SDKCustomTabsLauncher
import com.sap.cloud.mobile.onboarding.compose.screens.LaunchScreen
import com.sap.cloud.mobile.onboarding.compose.screens.rememberLaunchScreenState
import com.sap.cloud.mobile.onboarding.compose.settings.LocalScreenSettings

class WizardFlowActionHandler(val application: SAPWizardApplication): FlowActionHandler() {
    private var showDemoDialog by mutableStateOf(false)


    @Composable
    private fun getAnnotatedString(rId: Int) : AnnotatedString {
        val context = LocalContext.current
        val settings = LocalScreenSettings.current.launchScreenSettings
        val spannedString = context.getText(rId) as SpannedString
        val annotations = spannedString.getSpans<android.text.Annotation>(0, spannedString.length)
        val annotatedString = buildAnnotatedString {
            append(stringResource(rId))
            annotations.forEach { annotation ->
                val start = spannedString.getSpanStart(annotation)
                val end = spannedString.getSpanEnd(annotation)
                if (annotation.key == "key") {
                    when (annotation.value) {
                        "eula" -> {
                            addStringAnnotation(
                                tag = annotation.value,
                                annotation = settings.eulaUrl,
                                start = start, end = end
                            )
                        }

                        "term" -> {
                            addStringAnnotation(
                                tag = annotation.value,
                                annotation = settings.privacyPolicyUrl,
                                start = start, end = end
                            )
                        }
                    }
                }
                addStyle(
                    SpanStyle(
                        textDecoration = TextDecoration.Underline,
                        color = MaterialTheme.fioriHorizonAttributes.SapFioriColorT4
                    ), start, end
                )
            }
        }

        return annotatedString
    }

    @Composable
    private fun PrivacyDialogContent(annotatedString: AnnotatedString) {
        val context = LocalContext.current
        ClickableText(
            text = annotatedString,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            style = MaterialTheme.fioriHorizonAttributes.textAppearanceBody1.copy(
                textAlign = TextAlign.Start,
                color = MaterialTheme.colorScheme.onBackground
            ),
            onClick = { offset ->
                annotatedString.getStringAnnotations(
                    start = offset, end = offset
                ).firstOrNull()?.also { annotation ->
                    if (SDKCustomTabsLauncher.customTabsSupported(context)) {
                        SDKCustomTabsLauncher.launchCustomTabs(context, annotation.item)
                    }
                }
            },
        )
    }

    override fun getFlowCustomizationSteps(
        flow: BaseFlow,
        insertionPoint: CustomStepInsertionPoint
    ) {
        if (flow.flowName == FlowType.Onboarding.name) {
            when (insertionPoint) {
                CustomStepInsertionPoint.BeforeEula -> {
                    flow.addSingleStep(step_welcome, secure = false) {
                        val context = LocalContext.current
                        val state = rememberLaunchScreenState(
                            showTermLinks = true,
                            defaultAgreeStatus = false
                        )
                        LaunchScreen(
                            primaryViewClickListener = {
                                flow.flowDone(step_welcome)
                            },
                            secondaryViewClickListener = {
                                showDemoDialog = true
                            },
                            state = state,
                            linkClickListener = { url ->
                                if (SDKCustomTabsLauncher.customTabsSupported(context)) {
                                    SDKCustomTabsLauncher.launchCustomTabs(context, url)
                                }
                            }
                        )
                        if (showDemoDialog) {
                            FioriAlertDialog (
                                title = context.getString(R.string.launch_screen_demo_dialog_title),
                                text = context.getString(R.string.launch_screen_demo_dialog_message) ,
                                confirmButtonText = context.getString(R.string.launch_screen_demo_dialog_button_goback),
                                onConfirmButtonClick = {
                                    showDemoDialog = false
                                }
                            )
                        }
                        if (BuildConfig.FLAVOR == "tencentAppStoreforChinaMarket") {
                            var showPrivacyDialog by remember { mutableStateOf(true) }
                            var closeCount by remember { mutableIntStateOf(0) }
                            var title by remember { mutableStateOf("") }
                            var contentText by remember { mutableStateOf(AnnotatedString("")) }
                            var cBtnText by remember { mutableStateOf("") }
                            var dBtnText by remember { mutableStateOf("") }
                            var dBtnVisible by remember { mutableStateOf(false) }
                            if (closeCount == 0) {
                                val annotatedString : AnnotatedString = getAnnotatedString(R.string.privacy_dialog_content)
                                title = context.getString(R.string.launch_screen_dialog_title)
                                contentText = annotatedString
                                cBtnText = context.getString(R.string.launch_screen_dialog_button_agree)
                                dBtnVisible = true
                                dBtnText = context.getString(R.string.launch_screen_dialog_button_disagree)
                            } else {
                                val confirmationAnnotatedString : AnnotatedString = getAnnotatedString(R.string.privacy_confirmation_dialog_content)
                                title = context.getString(R.string.launch_screen_dialog_title)
                                contentText = confirmationAnnotatedString
                                cBtnText = context.getString(R.string.launch_screen_dialog_disagree_confirm)
                                dBtnVisible = false
                            }
                            if (showPrivacyDialog) {
                                val onClose: (Boolean) -> Unit = { agreed ->
                                    if (closeCount == 0) {
                                        state.agreeState.value = agreed
                                        state.showTermLinksState.value = !agreed
                                    }
                                    if (agreed) {
                                        showPrivacyDialog = false
                                    } else {
                                        if (closeCount !=0) showPrivacyDialog = false
                                        closeCount = if (closeCount == 0) 1 else 0
                                    }
                                }
                                FioriAlertDialog (
                                    modifier = Modifier.wrapContentHeight(),
                                    title = title,
                                    text = { PrivacyDialogContent(contentText) },
                                    confirmButtonText = cBtnText,
                                    onConfirmButtonClick = { onClose(true) },
                                    dismissButtonText = if (dBtnVisible) {
                                        dBtnText
                                    } else {
                                        null
                                    },
                                    onDismissButtonClick = { onClose(false) }
                                )
                            }
                        }
                    }
                }
                else -> Unit
            }
        }
    }

    override fun shouldStartTimeoutFlow(activity: Activity): Boolean = when (activity) {
        is WelcomeActivity -> false
        else -> super.shouldStartTimeoutFlow(activity)
    }

    companion object {
        private const val step_welcome = "step_custom_welcome"
    }

}
