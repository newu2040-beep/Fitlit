package com.example

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.local.entity.TodoEntity
import com.example.ui.components.AddEditTodoBottomSheet
import com.example.ui.components.FitlitBottomBar
import com.example.ui.components.GeminiApiKeyBottomSheet
import com.example.ui.components.PermissionsBottomSheet
import com.example.ui.components.PhotoPickerBottomSheet
import com.example.ui.components.ResetDataBottomSheet
import com.example.ui.components.SafetyDisclaimerDialog
import com.example.ui.components.ThemeSelectionBottomSheet
import com.example.ui.screens.fridge.FridgeScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.home.QuickAddSheet
import com.example.ui.screens.plan.MealDetailBottomSheet
import com.example.ui.screens.plan.MealPlanScreen
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.todo.TodoScreen
import com.example.ui.screens.tracking.TrackingScreen
import com.example.ui.screens.welcome.OnboardingScreen
import com.example.ui.screens.welcome.WelcomeScreen
import com.example.ui.theme.FitlitTheme
import com.example.ui.viewmodel.FitlitViewModel
import com.example.ui.viewmodel.FitlitViewModelFactory
import com.example.util.PermissionUtils

class MainActivity : FragmentActivity() {

    private val viewModel: FitlitViewModel by viewModels {
        val app = application as FitlitApplication
        FitlitViewModelFactory(app, app.repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val currentTheme by viewModel.currentTheme.collectAsState()

            FitlitTheme(themeMode = currentTheme) {
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
    val currentTheme by viewModel.currentTheme.collectAsState()
    val stepState by viewModel.stepState.collectAsState()
    val meals by viewModel.mealPlans.collectAsState()
    val nutritionSummary by viewModel.nutritionSummary.collectAsState()
    val weightLogs by viewModel.weightLogs.collectAsState()
    val fridgeItems by viewModel.fridgeItems.collectAsState()
    val todos by viewModel.todos.collectAsState()
    val geminiKeyStatus by viewModel.geminiKeyStatus.collectAsState()
    val customGeminiKey by viewModel.customGeminiKey.collectAsState()
    val isGeneratingPlan by viewModel.isGeneratingPlan.collectAsState()
    val isAnalyzingPhoto by viewModel.isAnalyzingPhoto.collectAsState()
    val suggestedFridgeMeals by viewModel.generatedFridgeMeals.collectAsState()
    val selectedMealDetail by viewModel.selectedMealDetail.collectAsState()
    val notificationMessage by viewModel.notificationMessage.collectAsState()
    val selectedTimeRange by viewModel.timeRangeFilter.collectAsState()

    var showQuickAddSheet by remember { mutableStateOf(false) }
    var showSafetyModal by remember { mutableStateOf(false) }
    var showPermissionsSheet by remember { mutableStateOf(false) }
    var showThemeSheet by remember { mutableStateOf(false) }
    var showPhotoPickerSheet by remember { mutableStateOf(false) }
    var showResetDataSheet by remember { mutableStateOf(false) }
    var showApiKeySheet by remember { mutableStateOf(false) }
    var showAddTodoSheet by remember { mutableStateOf(false) }
    var todoToEdit by remember { mutableStateOf<TodoEntity?>(null) }

    val quickAddSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val mealDetailSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val permissionsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val themeSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val photoPickerSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val resetDataSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val snackbarHostState = remember { SnackbarHostState() }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val grantedCount = results.values.count { it }
        if (grantedCount > 0) {
            viewModel.showNotification("App permissions updated! Full sensor & storage access enabled ✨")
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

    // Main tabs with bottom bar
    val showBottomBar = currentRoute in listOf("home", "plan", "tasks", "tracking", "insight", "profile", "fridge")

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
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = data.visuals.message,
                        color = MaterialTheme.colorScheme.onSurface,
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
            modifier = Modifier.padding(innerPadding),
            enterTransition = { fadeIn(tween(260)) + slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(260)) },
            exitTransition = { fadeOut(tween(200)) + slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(200)) },
            popEnterTransition = { fadeIn(tween(260)) + slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(260)) },
            popExitTransition = { fadeOut(tween(200)) + slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(200)) }
        ) {
            composable("welcome") {
                WelcomeScreen(
                    onGetStartedClick = { navController.navigate("onboarding") }
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
                    stepState = stepState,
                    meals = meals,
                    todos = todos,
                    geminiKeyStatus = geminiKeyStatus,
                    onPlanCardClick = { navController.navigate("plan") },
                    onFridgeCardClick = { navController.navigate("fridge") },
                    onMealClick = { meal -> viewModel.setSelectedMealDetail(meal) },
                    onNotificationClick = { viewModel.showNotification("Daily goal progress: ${nutritionSummary.overallGoalProgressPercent}%! 🌟") },
                    onThemePickerClick = { showThemeSheet = true },
                    onAvatarClick = { showPhotoPickerSheet = true },
                    onToggleLiveWalk = { viewModel.toggleLiveWalkSimulation() },
                    onAddQuickSteps = { count -> viewModel.addQuickSteps(count) },
                    onSeeAllMealsClick = { navController.navigate("plan") },
                    onNavigateToTasks = { navController.navigate("tasks") },
                    onToggleTodo = { todo -> viewModel.toggleTodoCompleted(todo) }
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

            composable("tasks") {
                TodoScreen(
                    todos = todos,
                    isGeneratingSchedule = isGeneratingPlan,
                    geminiKeyStatus = geminiKeyStatus,
                    onAddTodoClick = {
                        todoToEdit = null
                        showAddTodoSheet = true
                    },
                    onEditTodoClick = { todo ->
                        todoToEdit = todo
                        showAddTodoSheet = true
                    },
                    onToggleCompleted = { todo -> viewModel.toggleTodoCompleted(todo) },
                    onDeleteTodo = { id -> viewModel.deleteTodo(id) },
                    onGenerateAiSchedule = { viewModel.generateAiDailySchedule() },
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
                    onCalendarClick = { viewModel.showNotification("Today's workout streak: 6 Days 🔥") },
                    onResetDataClick = { showResetDataSheet = true }
                )
            }

            composable("insight") {
                com.example.ui.screens.insight.InsightScreen(
                    onSendMessage = { query -> viewModel.chatWithInsight(query) }
                )
            }

            composable("profile") {
                ProfileScreen(
                    profile = profile,
                    stepState = stepState,
                    currentTheme = currentTheme,
                    geminiKeyStatus = geminiKeyStatus,
                    onEditGoalClick = { navController.navigate("onboarding") },
                    onShowSafetyDisclaimer = { showSafetyModal = true },
                    onManagePermissionsClick = { showPermissionsSheet = true },
                    onOpenThemePicker = { showThemeSheet = true },
                    onOpenApiKeyManager = {
                        val activity = context as? androidx.fragment.app.FragmentActivity
                        if (activity != null) {
                            com.example.util.BiometricAuth.authenticate(
                                activity = activity,
                                title = "Unlock API Key Manager",
                                subtitle = "Authenticate to view or edit Gemini API Keys",
                                onSuccess = { showApiKeySheet = true },
                                onError = { msg -> viewModel.showNotification(msg) }
                            )
                        } else {
                            viewModel.showNotification("Biometric activity context missing")
                        }
                    },
                    onOpenPhotoPicker = { showPhotoPickerSheet = true },
                    onOpenResetData = { showResetDataSheet = true }
                )
            }
        }

        // Add / Edit Todo Bottom Sheet
        if (showAddTodoSheet) {
            AddEditTodoBottomSheet(
                todoToEdit = todoToEdit,
                onSaveTodo = { title, description, category, priority, dueDateStr, dueTimeStr, reminderMinutes ->
                    if (todoToEdit != null) {
                        viewModel.updateTodo(
                            todoToEdit!!.copy(
                                title = title,
                                description = description,
                                category = category,
                                priority = priority,
                                dueDateStr = dueDateStr,
                                dueTimeStr = dueTimeStr,
                                reminderMinutes = reminderMinutes
                            )
                        )
                    } else {
                        viewModel.addTodo(
                            title = title,
                            description = description,
                            category = category,
                            priority = priority,
                            dueDateStr = dueDateStr,
                            dueTimeStr = dueTimeStr,
                            reminderMinutes = reminderMinutes
                        )
                    }
                    showAddTodoSheet = false
                    todoToEdit = null
                },
                onDismiss = {
                    showAddTodoSheet = false
                    todoToEdit = null
                }
            )
        }

        // Gemini API Key Bottom Sheet
        if (showApiKeySheet) {
            GeminiApiKeyBottomSheet(
                currentStatus = geminiKeyStatus,
                currentCustomKey = customGeminiKey,
                onSaveKey = { key, callback ->
                    viewModel.saveCustomApiKey(key, callback)
                },
                onClearKey = {
                    viewModel.clearCustomApiKey()
                },
                onDismiss = { showApiKeySheet = false }
            )
        }

        // Reset Data & Storage Bottom Sheet
        if (showResetDataSheet) {
            ResetDataBottomSheet(
                sheetState = resetDataSheetState,
                onResetDemoData = {
                    viewModel.resetDemoData()
                    showResetDataSheet = false
                },
                onClearTodayLogs = {
                    viewModel.clearTodayLogs()
                    showResetDataSheet = false
                },
                onClearFridge = {
                    viewModel.clearFridgeInventory()
                    showResetDataSheet = false
                },
                onFactoryReset = {
                    viewModel.fullFactoryReset {
                        navController.navigate("welcome") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                    showResetDataSheet = false
                },
                onDismiss = { showResetDataSheet = false }
            )
        }

        // Theme Selection Bottom Sheet
        if (showThemeSheet) {
            ThemeSelectionBottomSheet(
                sheetState = themeSheetState,
                currentTheme = currentTheme,
                onSelectTheme = { theme ->
                    viewModel.selectTheme(theme)
                    showThemeSheet = false
                },
                onDismiss = { showThemeSheet = false }
            )
        }

        // Profile Photo Picker Bottom Sheet
        if (showPhotoPickerSheet) {
            PhotoPickerBottomSheet(
                sheetState = photoPickerSheetState,
                hasCustomPhoto = !profile?.profilePhotoUri.isNullOrEmpty(),
                onPhotoSelected = { bitmap ->
                    viewModel.updateProfilePhotoBitmap(bitmap)
                    showPhotoPickerSheet = false
                },
                onRemovePhoto = {
                    viewModel.removeProfilePhoto()
                    showPhotoPickerSheet = false
                },
                onDismiss = { showPhotoPickerSheet = false }
            )
        }

        // Permissions Bottom Sheet
        if (showPermissionsSheet) {
            PermissionsBottomSheet(
                sheetState = permissionsSheetState,
                onDismiss = { showPermissionsSheet = false },
                onPermissionsUpdated = {
                    viewModel.showNotification("All permissions active! 🌟")
                }
            )
        }

        // Meal Detail Bottom Sheet
        selectedMealDetail?.let { meal ->
            MealDetailBottomSheet(
                meal = meal,
                sheetState = mealDetailSheetState,
                onDismiss = { viewModel.setSelectedMealDetail(null) },
                onLogAsEaten = {
                    viewModel.logMealAsEaten(meal)
                    viewModel.setSelectedMealDetail(null)
                },
                onSwapMeal = {
                    viewModel.swapMeal(meal.id, meal.mealType)
                    viewModel.setSelectedMealDetail(null)
                }
            )
        }

        // Quick Add Bottom Sheet
        if (showQuickAddSheet) {
            QuickAddSheet(
                sheetState = quickAddSheetState,
                onDismiss = { showQuickAddSheet = false },
                onLogFood = { name, mealType, cals, p, c, f ->
                    viewModel.logCustomFoodItem(name, mealType, cals, p, c, f)
                    showQuickAddSheet = false
                },
                onLogNaturalFood = { text, mealType ->
                    viewModel.parseAndLogNaturalFoodText(text, mealType)
                    showQuickAddSheet = false
                },
                onLogActivity = { name, mins, cals, steps ->
                    viewModel.logActivity(name, mins, cals, steps)
                    showQuickAddSheet = false
                },
                onLogWeight = { w, bf, mm ->
                    viewModel.logWeight(w, bf, mm)
                    showQuickAddSheet = false
                },
                onAddWater = { ml ->
                    viewModel.addWater(ml)
                    showQuickAddSheet = false
                },
                onAnalyzePlate = { bitmap ->
                    viewModel.analyzeFoodPlatePhoto(bitmap) { meal ->
                        viewModel.logCustomFoodItem(
                            name = meal.title,
                            mealType = meal.mealType,
                            calories = meal.calories,
                            protein = meal.proteinG,
                            carbs = meal.carbsG,
                            fats = meal.fatG
                        )
                    }
                    showQuickAddSheet = false
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
