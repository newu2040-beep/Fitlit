package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.local.entity.MealPlanEntity
import com.example.ui.components.FitlitBottomBar
import com.example.ui.components.PermissionsBottomSheet
import com.example.ui.components.SafetyDisclaimerDialog
import com.example.ui.screens.fridge.FridgeScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.home.QuickAddSheet
import com.example.ui.screens.plan.MealDetailBottomSheet
import com.example.ui.screens.plan.MealPlanScreen
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.tracking.TrackingScreen
import com.example.ui.screens.welcome.OnboardingScreen
import com.example.ui.screens.welcome.WelcomeScreen
import com.example.ui.theme.FitlitTheme
import com.example.ui.theme.LimePrimaryDark
import com.example.ui.theme.TextOnLime
import com.example.ui.viewmodel.FitlitViewModel
import com.example.ui.viewmodel.FitlitViewModelFactory
import com.example.util.PermissionUtils
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: FitlitViewModel by viewModels {
        val app = application as FitlitApplication
        FitlitViewModelFactory(app.repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            FitlitTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    FitlitAppContent(viewModel = viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FitlitAppContent(viewModel: FitlitViewModel) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "welcome"

    val profile by viewModel.userProfile.collectAsState()
    val meals by viewModel.mealPlans.collectAsState()
    val nutritionSummary by viewModel.nutritionSummary.collectAsState()
    val weightLogs by viewModel.weightLogs.collectAsState()
    val fridgeItems by viewModel.fridgeItems.collectAsState()
    val isGeneratingPlan by viewModel.isGeneratingPlan.collectAsState()
    val isAnalyzingPhoto by viewModel.isAnalyzingPhoto.collectAsState()
    val suggestedFridgeMeals by viewModel.generatedFridgeMeals.collectAsState()
    val selectedMealDetail by viewModel.selectedMealDetail.collectAsState()
    val notificationMessage by viewModel.notificationMessage.collectAsState()
    val selectedTimeRange by viewModel.timeRangeFilter.collectAsState()

    var showQuickAddSheet by remember { mutableStateOf(false) }
    var showSafetyModal by remember { mutableStateOf(false) }
    var showPermissionsSheet by remember { mutableStateOf(false) }

    val quickAddSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val mealDetailSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val permissionsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val grantedCount = results.values.count { it }
        if (grantedCount > 0) {
            viewModel.showNotification("App permissions updated successfully! ✨")
        }
    }

    LaunchedEffect(Unit) {
        if (!PermissionUtils.areAllPermissionsGranted(context)) {
            permissionLauncher.launch(PermissionUtils.getRequiredPermissions())
        }
    }

    // Show toast / notification banner
    LaunchedEffect(notificationMessage) {
        notificationMessage?.let { msg ->
            snackbarHostState.showSnackbar(message = msg, duration = SnackbarDuration.Short)
            viewModel.clearNotification()
        }
    }

    // Determine if bottom bar should show (only on main tabs: home, plan, tracking, profile, fridge)
    val showBottomBar = currentRoute in listOf("home", "plan", "tracking", "profile", "fridge")

    val startDestination = if (profile?.hasCompletedOnboarding == true) "home" else "welcome"

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(16.dp)
            ) { data ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(16.dp))
                        .background(Color(0xFF1E293B), RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = data.visuals.message,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        bottomBar = {
            if (showBottomBar) {
                FitlitBottomBar(
                    currentDestination = currentRoute,
                    onNavigate = { route ->
                        if (currentRoute != route) {
                            navController.navigate(route) {
                                popUpTo("home") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    onQuickAddClick = { showQuickAddSheet = true }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("welcome") {
                WelcomeScreen(
                    onGetStartedClick = { navController.navigate("onboarding") },
                    onLoginClick = { navController.navigate("home") }
                )
            }

            composable("onboarding") {
                OnboardingScreen(
                    initialProfile = profile,
                    onComplete = { updatedProfile ->
                        viewModel.completeOnboarding(updatedProfile)
                        navController.navigate("home") {
                            popUpTo("welcome") { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable("home") {
                HomeScreen(
                    profile = profile,
                    nutritionSummary = nutritionSummary,
                    meals = meals,
                    onPlanCardClick = { navController.navigate("plan") },
                    onFridgeCardClick = { navController.navigate("fridge") },
                    onMealClick = { meal -> viewModel.setSelectedMealDetail(meal) },
                    onNotificationClick = { viewModel.showNotification("You're on track for your daily goals! 🌟") },
                    onSeeAllMealsClick = { navController.navigate("plan") }
                )
            }

            composable("plan") {
                MealPlanScreen(
                    profile = profile,
                    meals = meals,
                    fridgeItems = fridgeItems,
                    isGenerating = isGeneratingPlan,
                    onBack = { navController.navigate("home") },
                    onRegenerateClick = { viewModel.regenerateFullPlan() },
                    onMealClick = { meal -> viewModel.setSelectedMealDetail(meal) },
                    onToggleFavorite = { meal -> viewModel.toggleMealFavorite(meal) },
                    onLogMeal = { meal -> viewModel.logMealAsEaten(meal) },
                    onFilterClick = { navController.navigate("onboarding") }
                )
            }

            composable("fridge") {
                FridgeScreen(
                    fridgeItems = fridgeItems,
                    suggestedMeals = suggestedFridgeMeals,
                    isAnalyzing = isAnalyzingPhoto,
                    isGenerating = isGeneratingPlan,
                    onBack = { navController.popBackStack() },
                    onAddItem = { name, cat -> viewModel.addFridgeItem(name, cat) },
                    onDeleteItem = { id -> viewModel.deleteFridgeItem(id) },
                    onAnalyzePhoto = { bitmap -> viewModel.analyzeFridgePhoto(bitmap) },
                    onGenerateMeals = { viewModel.generateFridgeMealIdeas() }
                )
            }

            composable("tracking") {
                TrackingScreen(
                    profile = profile,
                    summary = nutritionSummary,
                    weightLogs = weightLogs,
                    selectedTimeRange = selectedTimeRange,
                    onTimeRangeChange = { range -> viewModel.setTimeRangeFilter(range) },
                    onAddWater = { ml -> viewModel.addWater(ml) },
                    onLogWeightClick = { showQuickAddSheet = true },
                    onLogActivityClick = { showQuickAddSheet = true },
                    onCalendarClick = { viewModel.showNotification("Today's workout streak: 6 Days 🔥") }
                )
            }

            composable("profile") {
                ProfileScreen(
                    profile = profile,
                    onEditGoalClick = { navController.navigate("onboarding") },
                    onShowSafetyDisclaimer = { showSafetyModal = true },
                    onManagePermissionsClick = { showPermissionsSheet = true }
                )
            }
        }

        // Permissions Bottom Sheet
        if (showPermissionsSheet) {
            PermissionsBottomSheet(
                sheetState = permissionsSheetState,
                onDismiss = { showPermissionsSheet = false },
                onPermissionsUpdated = {
                    viewModel.showNotification("Permissions updated! ✨")
                }
            )
        }

        // Meal Detail Bottom Sheet
        selectedMealDetail?.let { meal ->
            MealDetailBottomSheet(
                meal = meal,
                sheetState = mealDetailSheetState,
                onDismiss = { viewModel.setSelectedMealDetail(null) },
                onLogAsEaten = { viewModel.logMealAsEaten(meal) },
                onSwapMeal = { viewModel.swapMeal(meal.id, meal.mealType) }
            )
        }

        // Quick Add Sheet
        if (showQuickAddSheet) {
            QuickAddSheet(
                sheetState = quickAddSheetState,
                onDismiss = { showQuickAddSheet = false },
                onLogFood = { name, type, cal, p, c, f ->
                    viewModel.logCustomFoodItem(name, type, cal, p, c, f)
                },
                onLogNaturalFood = { text, type ->
                    viewModel.parseAndLogNaturalFoodText(text, type)
                },
                onLogActivity = { name, mins, cal, steps ->
                    viewModel.logActivity(name, mins, cal, steps)
                },
                onLogWeight = { w, bf, mm ->
                    viewModel.logWeight(w, bf, mm)
                },
                onAddWater = { ml ->
                    viewModel.addWater(ml)
                },
                onAnalyzePlate = { bitmap ->
                    viewModel.analyzeFoodPlatePhoto(bitmap) { detected ->
                        viewModel.logCustomFoodItem(
                            name = detected.title,
                            mealType = detected.mealType,
                            calories = detected.calories,
                            protein = detected.proteinG,
                            carbs = detected.carbsG,
                            fats = detected.fatG
                        )
                        showQuickAddSheet = false
                    }
                },
                isAnalyzing = isAnalyzingPhoto
            )
        }

        // Safety Disclaimer Dialog
        if (showSafetyModal) {
            SafetyDisclaimerDialog(
                onDismiss = { showSafetyModal = false },
                onAccept = { showSafetyModal = false }
            )
        }
    }
}
