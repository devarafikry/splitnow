package com.devara.splitnow

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.devara.splitnow.ai.SplitParser
import com.devara.splitnow.data.PREF_ONBOARDED
import com.devara.splitnow.data.SettingsStore
import com.devara.splitnow.scan.TextRecognizer
import com.devara.splitnow.ui.flow.SplitFlowState
import com.devara.splitnow.ui.screen.describe.DescribeScreen
import com.devara.splitnow.ui.screen.home.HomeScreen
import com.devara.splitnow.ui.screen.loading.LoadingScreen
import com.devara.splitnow.ui.screen.onboarding.OnboardingScreen
import com.devara.splitnow.ui.screen.review.EditChargeModal
import com.devara.splitnow.ui.screen.review.EditItemModal
import com.devara.splitnow.ui.screen.review.ReviewScreen
import com.devara.splitnow.ui.screen.scan.ScanScreen
import com.devara.splitnow.ui.screen.settings.EditPaymentScreen
import com.devara.splitnow.ui.screen.settings.PaymentMethodsScreen
import com.devara.splitnow.ui.screen.settings.SettingsScreen
import com.devara.splitnow.ui.screen.share.ShareScreen
import com.devara.splitnow.ui.theme.SplitNowTheme
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

private object Routes {
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val SCAN = "scan"
    const val DESCRIBE = "describe"
    const val LOADING = "loading"
    const val REVIEW = "review"
    const val SHARE = "share"
    const val SETTINGS = "settings"
    const val PAYMENT_METHODS = "payment_methods"
    const val EDIT_PAYMENT = "edit_payment"
    const val EDIT_ITEM = "edit_item"
    const val EDIT_CHARGE = "edit_charge"
}

@Composable
fun App() {
    SplitNowTheme {
        val nav = rememberNavController()
        val settings = koinInject<SettingsStore>()
        val flow = koinInject<SplitFlowState>()
        val recognizer = koinInject<TextRecognizer>()
        val parser = koinInject<SplitParser>()
        val scope = rememberCoroutineScope()
        val onboarded = remember { settings.getBoolean(PREF_ONBOARDED, false) }
        val startDest = if (onboarded) Routes.HOME else Routes.ONBOARDING

        // Holds modal context: itemId|null + presetAssignedTo.
        var editItemArgs by remember { mutableStateOf<Pair<Long?, String?>>(null to null) }
        var editChargeId by remember { mutableStateOf<Long?>(null) }
        var editPaymentId by remember { mutableStateOf<Long?>(null) }

        Box(modifier = Modifier.fillMaxSize()) {
            NavHost(navController = nav, startDestination = startDest) {
                composable(Routes.ONBOARDING) {
                    OnboardingScreen(onGetStarted = {
                        settings.putBoolean(PREF_ONBOARDED, true)
                        nav.navigate(Routes.HOME) { popUpTo(Routes.ONBOARDING) { inclusive = true } }
                    })
                }
                composable(Routes.HOME) {
                    HomeScreen(
                        onSettings = { nav.navigate(Routes.SETTINGS) },
                        onNewSplit = {
                            flow.reset()
                            nav.navigate(Routes.SCAN)
                        },
                        onOpenSplit = { _ -> /* History detail deferred */ },
                    )
                }
                composable(Routes.SCAN) {
                    ScanScreen(
                        onClose = { nav.popBackStack() },
                        onCaptured = { bytes ->
                            flow.capturedImage = bytes
                            // OCR runs in the background; user goes straight to Describe.
                            scope.launch {
                                flow.ocrText = recognizer.recognize(bytes)
                            }
                            nav.navigate(Routes.DESCRIBE)
                        },
                    )
                }
                composable(Routes.DESCRIBE) {
                    DescribeScreen(
                        onBack = { nav.popBackStack() },
                        onSplit = { description ->
                            flow.description = description
                            nav.navigate(Routes.LOADING)
                            scope.launch {
                                runCatching {
                                    val parsed = parser.parse(flow.ocrText, description, flow.currency)
                                    val quad = parser.toDomain(parsed, flow.currency)
                                    flow.currency = quad.currency
                                    flow.items.clear()
                                    quad.items.forEachIndexed { i, it -> flow.items.add(it.copy(id = (i + 1).toLong())) }
                                    flow.charges.clear()
                                    quad.charges.forEachIndexed { i, c -> flow.charges.add(c.copy(id = (i + 1).toLong())) }
                                    flow.people.clear()
                                    flow.people.addAll(quad.people.ifEmpty { listOf("You", "Friend") })
                                    flow.restaurantName = parsed.restaurantName.ifBlank { "Receipt" }
                                    nav.navigate(Routes.REVIEW) { popUpTo(Routes.SCAN) { inclusive = true } }
                                }.onFailure { e ->
                                    flow.error = e.message
                                    flow.items.clear(); flow.charges.clear()
                                    flow.people.clear()
                                    flow.people.addAll(listOf("You", "Friend"))
                                    nav.navigate(Routes.REVIEW) { popUpTo(Routes.SCAN) { inclusive = true } }
                                }
                            }
                        },
                    )
                }
                composable(Routes.LOADING) { LoadingScreen() }
                composable(Routes.REVIEW) {
                    ReviewScreen(
                        onBack = { nav.popBackStack() },
                        onShare = { nav.navigate(Routes.SHARE) },
                        onEditItem = { id -> editItemArgs = id to null; nav.navigate(Routes.EDIT_ITEM) },
                        onAddItem = { who -> editItemArgs = null to who; nav.navigate(Routes.EDIT_ITEM) },
                        onAddShared = { editItemArgs = null to "Shared"; nav.navigate(Routes.EDIT_ITEM) },
                        onEditCharge = { id -> editChargeId = id; nav.navigate(Routes.EDIT_CHARGE) },
                        onAddCharge = { editChargeId = null; nav.navigate(Routes.EDIT_CHARGE) },
                    )
                }
                composable(Routes.EDIT_ITEM) {
                    EditItemModal(
                        itemId = editItemArgs.first,
                        presetAssignedTo = editItemArgs.second,
                        onCancel = { nav.popBackStack() },
                        onSaved = { nav.popBackStack() },
                    )
                }
                composable(Routes.EDIT_CHARGE) {
                    EditChargeModal(
                        chargeId = editChargeId,
                        onCancel = { nav.popBackStack() },
                        onSaved = { nav.popBackStack() },
                    )
                }
                composable(Routes.SHARE) {
                    ShareScreen(onBack = { nav.popBackStack() })
                }
                composable(Routes.SETTINGS) {
                    SettingsScreen(
                        onBack = { nav.popBackStack() },
                        onPaymentMethods = { nav.navigate(Routes.PAYMENT_METHODS) },
                        onCurrency = {},
                        onTheme = {},
                        onPrivacy = {},
                        onAbout = {},
                    )
                }
                composable(Routes.PAYMENT_METHODS) {
                    PaymentMethodsScreen(
                        onBack = { nav.popBackStack() },
                        onAdd = { editPaymentId = null; nav.navigate(Routes.EDIT_PAYMENT) },
                        onEdit = { id -> editPaymentId = id; nav.navigate(Routes.EDIT_PAYMENT) },
                    )
                }
                composable(Routes.EDIT_PAYMENT) {
                    EditPaymentScreen(
                        methodId = editPaymentId,
                        onBack = { nav.popBackStack() },
                        onSaved = { nav.popBackStack() },
                    )
                }
            }
        }
    }
}
