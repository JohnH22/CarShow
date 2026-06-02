package gr.ihu.ict.carshow

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import androidx.room.util.TableInfo
import coil.compose.AsyncImage
import gr.ihu.ict.carshow.auth.TokenStore
import gr.ihu.ict.carshow.data.local.DatabaseProvider
import gr.ihu.ict.carshow.data.model.CarCategory
import gr.ihu.ict.carshow.data.model.CarEntry
import gr.ihu.ict.carshow.data.model.SellerType
import gr.ihu.ict.carshow.data.repository.CarRepository
import gr.ihu.ict.carshow.data.repository.RestCarRepository
import gr.ihu.ict.carshow.data.rest.RetrofitInstance
import gr.ihu.ict.carshow.ui.components.AddReviewDialog
import gr.ihu.ict.carshow.ui.components.ReviewItem
import gr.ihu.ict.carshow.ui.components.StarRatingBar
import gr.ihu.ict.carshow.ui.components.YoutubePlayer
import gr.ihu.ict.carshow.ui.viewmodel.CarDetailViewModel
import gr.ihu.ict.carshow.ui.viewmodel.CarListViewModel
import gr.ihu.ict.carshow.ui.viewmodel.auth.AuthViewModel
import gr.ihu.ict.carshow.ui.components.extractYoutubeVideoId
import java.io.File


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

        }
    }
}


@kotlinx.serialization.Serializable
object Home

@kotlinx.serialization.Serializable
data class Vehicles(val category: String)

@kotlinx.serialization.Serializable
data class VehicleDetail(val vehicleId: Int)

@kotlinx.serialization.Serializable
object AddVehicle

@kotlinx.serialization.Serializable
object Login

@kotlinx.serialization.Serializable
object Register






@Composable
fun CarShowApp() {
    // Navigation Controller: Manages app screens and backstack
    val navController = rememberNavController()

    // App Context needed for AndroidViewModels and Database
    // Casting LocalContext to Application to ensure global scope
    val context = LocalContext.current.applicationContext as Application


    // Room Database: Saves data locally on the device
    // "remember" ensures database instance created once and reused across recompositions
    val database = remember {
        DatabaseProvider.getDatabase(context)
    }


    // Retrofit API Service: Handles network communication with Django backend
    // This instance includes AuthInterceptor and TokenAuthenticator
    val api = remember {
        RetrofitInstance.buildApi(context)
    }


    // Repository: Coordinates data between the API and Local Room Database
    val repository = remember {
        RestCarRepository(
            api,
            database.carEntryDao()
        )
    }


    // AuthViewModel: Manages Login/Logout and User Authentication logic
    // Passing application context and API service as dependencies
    val authViewModel = remember {
        AuthViewModel(context, api)
    }


    // Determine where to send User upon app launch
    // If Access Token exists it skips the Login Screen
    val startDestination = if (TokenStore.getAccess(context) != null) Home else Login


    // Navigation Host: Defines the UI structure and routes of the app
    // Passing all the shared dependencies (repository,authViewModel) into the NavHost
    CarShowNavHost(navController, repository, authViewModel, startDestination )
}




// Navigation map for the application
// Defines the connection between routes (Serializable objects) and Composables (Screens)
@Composable
fun CarShowNavHost(
    navController: NavHostController,
    repository: CarRepository,
    authViewModel: AuthViewModel,
    startDestination: Any
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

    // --- LOGIN SCREEN---
        composable<Login> {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = {
                    // Navigate to Home and clear the login screen from the backstack
                    navController.navigate(Home) {
                        popUpTo(Login) { inclusive = true } // clear back stack
                    }
                },
                onNavigateToRegister = {
                    // Navigate to Register
                    navController.navigate(Register)
                }
            )
        }

        // --- REGISTER SCREEN ---
        composable<Register> {
            RegisterScreen(
                viewModel = authViewModel,
                onRegisterSuccess = {
                    // Registration performs auto-login , navigate directly to Home
                    navController.navigate(Home) {
                        // Clear both Login and Register from the backstack
                        popUpTo(Login) { inclusive = true }
                    }
                },
                onBackToLogin = {
                    // Back navigation to Login Screen
                    navController.popBackStack()
                }
            )
        }

    // --- HOME SCREEN---
        composable<Home> {
            HomeScreen(
                viewModel = authViewModel,
                onSelectCategory = { category ->
                    // Pass selected category as a parameter to Vehicles route
                    navController.navigate(Vehicles(category))
                },
                onAddVehicle = {
                    navController.navigate(AddVehicle)
                },
                onLogout = {
                    authViewModel.logout {
                        navController.navigate(Login) {
                            // Remove Login Screen from backstack (so user can't go back to it)
                            popUpTo(0) { inclusive = true } // clear back stack
                        }
                    }
                }
            )
        }

    // --- VEHICLES LIST SCREEN ---
        composable<Vehicles> { backStackEntry ->
            // Each screen "visit" is stored in a backStackEntry
            // Using .toRoute<Vehicles>() to extract the data arguments (like "category")
            // that were passed to this screen during navigation
            val route = backStackEntry.toRoute<Vehicles>()

            // Initializing ViewModel linked to Android Lifecycle
            // It survives configuration changes and screen recompositions perfectly
            val viewModel: CarListViewModel = androidx.lifecycle.viewmodel.compose.viewModel {
                CarListViewModel(repository)
            }


            LaunchedEffect(route.category) {
                viewModel.updateFilters(
                    minPrice = viewModel.uiState.value.filters.minPrice,
                    maxPrice = viewModel.uiState.value.filters.maxPrice,
                    minEngine = viewModel.uiState.value.filters.minEngine,
                    maxEngine = viewModel.uiState.value.filters.maxEngine,
                    minMileage = viewModel.uiState.value.filters.minMileage,
                    maxMileage = viewModel.uiState.value.filters.maxMileage,
                    minHP = viewModel.uiState.value.filters.minHP,
                    maxHP = viewModel.uiState.value.filters.maxHP,
                    minYear = viewModel.uiState.value.filters.minYear,
                    maxYear = viewModel.uiState.value.filters.maxYear,
                    ordering = viewModel.uiState.value.filters.ordering,
                    category = if (route.category == "ALL") null else route.category
                )
            }

            VehiclesScreen(
                category = route.category,
                viewModel = viewModel,
                onVehicleClick = { id ->
                    navController.navigate(VehicleDetail(id))
                },
                onTokenExpired = {
                    // Redirect to Login if the session (refresh token) is dead
                    navController.navigate(Login) { popUpTo(0) { inclusive = true } } // clear back stack
                }
            )
        }

    // --- ADD VEHICLE SCREEN ---
        composable<AddVehicle> {
            // Initializing ViewModel linked to Android Lifecycle
            // It survives configuration changes and screen recompositions perfectly
            val viewModel: CarListViewModel = androidx.lifecycle.viewmodel.compose.viewModel {
                CarListViewModel(repository)
            }

            AddVehicleScreen(
                viewModel = viewModel,
                onSaved = { navController.popBackStack() }, // Go back to the list after creation
                onTokenExpired = {
                    navController.navigate(Login) { popUpTo(0) { inclusive = true } } // clear back stack
                }
            )
        }

    // --- VEHICLE DETAIL SCREEN ---
        composable<VehicleDetail> { backStackEntry ->
            // backStackEntry holds the information for this specific destination
            // Using .toRoute<VehicleDetail>() to extract the "vehicleId" passed when we clicked
            // on a car in the previous screen.
            // This ID essential for ViewModel to know which car's details to fetch from API
            val route = backStackEntry.toRoute<VehicleDetail>()

            // Re-create ViewModel only when the vehicleId changes
            val viewModel = remember(route.vehicleId) {
                CarDetailViewModel(repository)
            }

            VehicleDetailScreen(
                vehicleId = route.vehicleId,
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onBackHome = {
                    navController.navigate(Home) { // Clears stack and takes to Home
                        popUpTo(Home) { inclusive = false } // Keeps Home but clears everything on top of it
                        launchSingleTop = true // If already at Home do not open another one
                    }
                },
                onTokenExpired = {
                    navController.navigate(Login) { popUpTo(0) { inclusive = true } } // clear back stack
                }
            )
        }
    }
}





@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    // Local state to hold user input. "Remember" ensures no values lost during recompositions.
    var username by remember { mutableStateOf("") }
    var password  by remember { mutableStateOf("") }


    // Observe the loginSuccess state from the ViewModel. When its true trigger navigation callback.
    LaunchedEffect(viewModel.loginSuccess) {
        if (viewModel.loginSuccess) {
            viewModel.resetLoginSuccess() // Reset the state for next time.
            onLoginSuccess() // Navigate to Home destination.
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212)), // Dark background.
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.85f) // Set container width 85% of screen.
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp) // Gap between UI elements.
        ) {
            // Car Icon.
            Icon(
                imageVector = Icons.Default.DirectionsCar,
                contentDescription = "App Logo",
                modifier = Modifier.size(64.dp),
                tint = Color(0xFF1976D2)
            )

            Text(
                text = "Sign in to manage your fleet",
                fontSize = 14.sp,
                color = Color.Gray
            )

            // Username input field.
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null ) }
            )

            // Password input field with hidden text transformation(PasswordVisualTransformation).
            OutlinedTextField(
                value = password,
                onValueChange =  { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null ) }
            )

            // Show error text only if errorMessage is not null.
            viewModel.errorMessage?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error, // Display errors in red color
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }

            // Login Button. Disabled if fields are black or during API call (isLoading).
            Button(
                onClick = { viewModel.login(username, password) },
                enabled = username.isNotBlank() && password.isNotBlank() && !viewModel.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
            ) {
                // Show progress indicator while waiting for the server response.
                if (viewModel.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Login", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Link to Registration
            TextButton(
                onClick = onNavigateToRegister,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(
                    text = "Don't have an account? Sign Up",
                    color = Color(0xFF1976D2),
                    fontSize = 14.sp
                )
            }
        }
    }
}



@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onRegisterSuccess: () -> Unit,
    onBackToLogin: () -> Unit
) {
    // rememberSaveable: "Keeps" the input data during configuration changes (Screen rotation, ...)
    var username by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    // Listen to loginSuccess flag, when it becomes "true" it triggers "onRegisterSuccess" callback
    LaunchedEffect(viewModel.loginSuccess) {
        if (viewModel.loginSuccess) {
            // Move the user to the next screen (Home)
            onRegisterSuccess()
            // Reset flag to "false" to avoid re-navigations if the screen recomposes or return to this screen later
            viewModel.resetLoginSuccess()
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .animateContentSize(), // Makes the screen resize smoothly when error messages pop up
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Create Account",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )


        Spacer(modifier = Modifier.height(32.dp))

        // Username text field
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )


        Spacer(modifier = Modifier.height(16.dp))


        // Email text field
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true
        )


        Spacer(modifier = Modifier.height(16.dp))


        // Password text field
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true
        )


        Spacer(modifier = Modifier.height(24.dp))


        // Show green success message if everything went well
        viewModel.successMessage?.let { message ->
            Text(
                text = message,
                color = Color(0xFF2E7D32),
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
        }

        // Red Error message display if registration failed
        viewModel.errorMessage?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .padding(bottom = 16.dp)
            )
        }


        // Register Button: disables itself and shows a loading spinner while waiting for the server
        Button(
            onClick = { viewModel.register(username, email, password) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !viewModel.isLoading,
            shape = MaterialTheme.shapes.medium
        ) {
            if (viewModel.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Sign Up")
            }
        }

        // Navigate back to Login if the user already has credentials
        TextButton(onClick = onBackToLogin) {
            Text("Already have an account? Log In")
        }
    }
}




@Composable
fun HomeScreen(
    viewModel: AuthViewModel,
    onSelectCategory: (String) -> Unit, // Callback for category selection.
    onAddVehicle: () -> Unit, // Callback to navigate to AddVehicle screen.
    onLogout: () -> Unit // Callback to handle user logout.
) {
    // Get the current Android Context to allow showing Toast notification
    val context = LocalContext.current

    // Triggers when "showWelcomeMessage" changes to true
    LaunchedEffect(viewModel.showWelcomeMessage) {
        if (viewModel.showWelcomeMessage) {
            // If username is null for any reason use "User" as a default name
            // If the left side of the ?: (elvis operator) is null execute the right side
            val name = viewModel.username ?: "User"
            // Display a popup message at the bottom of the screen
            android.widget.Toast.makeText(
                context,
                "Welcome back, $name!",
                android.widget.Toast.LENGTH_LONG
            ).show()

            // Reset the flag in the ViewModel
            // Prevents the toast from reappearing during screen recompositions (screen rotation, ...)
            viewModel.resetWelcomeMessageShown()
        }
    }

    // Local list of available categories, defines which buttons will be generated in UI.
    val categories = listOf("Sedan", "SUV", "Electric", "Sport")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212)) // Dark theme background.
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Screen Title.
            Text(
                text = "Vehicle Fleet",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            // Subtitle for guidance.
            Text(
                text = "Select a category to browse",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(40.dp))


            // Dynamic Button Generation: loops through "categories" list and creates button for each one.
            categories.forEach { category ->
                Button(
                    onClick = { onSelectCategory(category) }, // Passes the specific category string.
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1976D2), // Blue color
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = category,
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                Spacer(modifier = Modifier.height(16.dp)) // Spacing between category buttons
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Red Button Logout option for signing out
            TextButton(onClick = onLogout) {
                Text(
                    "Log Out",
                    color = Color(0xFFE53935), // Red for logout
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        // FAB positioned at bottom-right. Stays on top of Column because it's inside a Box.
        FloatingActionButton(
            onClick = onAddVehicle,
            modifier = Modifier
                .padding(24.dp)
                .align(Alignment.BottomEnd),
            containerColor = Color(0xFF1976D2),
            contentColor = Color.White
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Add Vehicle"
            )
        }
    }
}





@OptIn(ExperimentalMaterial3Api::class)
@Composable fun VehiclesScreen(
    category: String,
    viewModel: CarListViewModel,
    onVehicleClick: (Int) -> Unit,
    onTokenExpired: () -> Unit
) {

    // Collect and observe UI state from ViewModel converting to Compose State
    // value state will auto update whenever the UI data changes
    val state by viewModel.uiState.collectAsState()

    // Controls bottom sheet behavior (skipPartiallyExpanded = true ensures it opens directly to full screen)
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Visibility flag to track whether the Bottom Sheet drawer is open (true) or closed (false)
    var showBottomSheet by remember { mutableStateOf(false) }

    // Local state variables holding the raw text typed by the user inside each filter field
    // Initialized with empty strings ("") so the TextFields start blank
    var minPriceInput by remember { mutableStateOf("") }
    var maxPriceInput by remember { mutableStateOf("") }
    var minEngineInput by remember { mutableStateOf("") }
    var maxEngineInput by remember { mutableStateOf("") }
    var minMileageInput by remember { mutableStateOf("") }
    var maxMileageInput by remember { mutableStateOf("") }
    var minHPInput by remember { mutableStateOf("") }
    var maxHPInput by remember { mutableStateOf("") }
    var minYearInput by remember { mutableStateOf("") }
    var maxYearInput by remember { mutableStateOf("") }

    // Dropdown sorting menu control states
    var showSortMenu by remember { mutableStateOf(false) }
    var selectedSortLabel by remember { mutableStateOf("Default") }


    // Map user friendly labels to Django backend filter keys
    val sortingOptions = listOf(
        "Default" to "-id",
        "Price: Low to High" to "price",
        "Price: High to Low" to "-price",
        "Year: Newest First" to "-year",
        "Engine: Smallest First" to "engine"
    )


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Display selected category name as page title
        Text(
            text = category,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 32.sp,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Filter and Sort toggle buttons row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // The filter icon button located in the top row of the screen
            IconButton(
                onClick = {
                    showBottomSheet = true // Trigger do display the ModalBottomSheet drawer on screen
                }
            ) {
                Icon(
                    Icons.Default.FilterList,
                    contentDescription = "Filters",
                    tint = Color.White
                )
            }

            // Sorting Selection Box trigger
            Box {
                Row(
                    modifier = Modifier
                        .clickable { showSortMenu = true }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sort: $selectedSortLabel",
                        color = Color.LightGray,
                        fontSize = 14.sp
                    )
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = Color.LightGray
                    )
                }


                // Sorting dropdown list menu overlay
                DropdownMenu(
                    expanded = showSortMenu,
                    onDismissRequest = { showSortMenu = false }
                ) {
                    sortingOptions.forEach { (label, backendKey) ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                selectedSortLabel = label
                                showSortMenu = false

                                // Send updated ordering choice straight to server API viewmodel routing
                                viewModel.updateFilters(
                                    category = if (category == "ALL") null else category,
                                    minPrice = minPriceInput.toDoubleOrNull(),
                                    maxPrice = maxPriceInput.toDoubleOrNull(),
                                    minEngine = minEngineInput.toIntOrNull(),
                                    maxEngine = maxEngineInput.toIntOrNull(),
                                    minMileage = minMileageInput.toIntOrNull(),
                                    maxMileage = maxMileageInput.toIntOrNull(),
                                    minHP = minHPInput.toIntOrNull(),
                                    maxHP = maxHPInput.toIntOrNull(),
                                    minYear = minYearInput.toIntOrNull(),
                                    maxYear = maxYearInput.toIntOrNull(),
                                    ordering = backendKey,
                                    onTokenExpired = onTokenExpired
                                )
                            }
                        )
                    }
                }
            }
        }


        // Modal Bottom Sheet for Filters
        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState,
                containerColor = Color(0xFF1E1E1E), // Dark UI color
                contentColor = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()) // For smaller mobile devices (enables scrolling)
                        .padding(start = 24.dp, end = 24.dp, bottom = 42.dp, top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Filter Vehicles",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    // Row 1: Price Filter
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = minPriceInput,
                            onValueChange = { minPriceInput = it },
                            label = { Text("Min Price") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                        OutlinedTextField(
                            value = maxPriceInput,
                            onValueChange = { maxPriceInput = it },
                            label = { Text("Max Price") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                    }

                    // Row 2: Engine CC Filter
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = minEngineInput,
                            onValueChange = { minEngineInput = it },
                            label = { Text("Min CC") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                        OutlinedTextField(
                            value = maxEngineInput,
                            onValueChange = { maxEngineInput = it },
                            label = { Text("Max CC") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                    }

                    // Row 3: Mileage Filter
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = minMileageInput,
                            onValueChange = { minMileageInput = it },
                            label = { Text("Min Mileage") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                        OutlinedTextField(
                            value = maxMileageInput,
                            onValueChange = { maxMileageInput = it },
                            label = { Text("Max Mileage") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                    }

                    // Row 4: Horsepower Filter
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = minHPInput,
                            onValueChange = { minHPInput = it },
                            label = { Text("Min HP") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                        OutlinedTextField(
                            value = maxHPInput,
                            onValueChange = { maxHPInput = it },
                            label = { Text("Max HP") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                    }

                    // Production Year Filter
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = minYearInput,
                            onValueChange = { minYearInput = it },
                            label = { Text("Min Year") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                        OutlinedTextField(
                            value = maxYearInput,
                            onValueChange = { maxYearInput = it },
                            label = { Text("Max Year") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Action Buttons Row inside the bottom sheet
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Reset Button to clear all filters and reload data
                        Button(
                            onClick = {
                                // Clear all local UI inputs (wipes out whatever the user typed)
                                minPriceInput = ""
                                maxPriceInput = ""
                                minEngineInput = ""
                                maxEngineInput = ""
                                minMileageInput = ""
                                maxMileageInput = ""
                                minHPInput = ""
                                maxHPInput = ""
                                minYearInput = ""
                                maxYearInput = ""
                                selectedSortLabel = "Default"
                                // Notifies ViewModel to reset active filters and load original list
                                viewModel.clearFilters(onTokenExpired)
                                showBottomSheet = false // close drawer reactively
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.DarkGray
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Clear")
                        }

                        // Apply Button: Sends the typed text as filters to the backend
                        Button(
                            onClick = {
                                // Finds which backend key matches the selected sort option (e.g., "price")
                                val activeSort = sortingOptions.find { it.first == selectedSortLabel }?.second
                                // Convert text inputs to numbers (null if empty) and update the list
                                viewModel.updateFilters(
                                    category = if (category == "ALL") null else category, // "ALL" means no specific category filter
                                    minPrice = minPriceInput.toDoubleOrNull(),
                                    maxPrice = maxPriceInput.toDoubleOrNull(),
                                    minEngine = minEngineInput.toIntOrNull(),
                                    maxEngine = maxEngineInput.toIntOrNull(),
                                    minMileage = minMileageInput.toIntOrNull(),
                                    maxMileage = maxMileageInput.toIntOrNull(),
                                    minHP = minHPInput.toIntOrNull(),
                                    maxHP = maxHPInput.toIntOrNull(),
                                    minYear = minYearInput.toIntOrNull(),
                                    maxYear = maxYearInput.toIntOrNull(),
                                    ordering = activeSort,
                                    onTokenExpired = onTokenExpired
                                )
                                // Trigger to close the filter sheet to show results
                                showBottomSheet = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Apply")
                        }
                    }
                }
            }
        }


        Spacer(modifier = Modifier.height(12.dp))

        // Display error message and retry button if something goes wrong
        state.errorMessage?.let { message ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = message,
                    color = Color.Red
                )
                Button(
                    onClick = { viewModel.refreshData(onTokenExpired) }
                ) {
                    Text("Retry")
                }
            }
        }

        // Show spinner while data being fetched
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF1976D2))
            }
        }

        // Show the list of filtered cars mapped reactively from the backend Room payload synchronization
        if (!state.isLoading && state.errorMessage == null) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp) // Gap between vehicle cards
            ) {
                items(state.items) { car ->
                    VehicleItemCard(
                        car = car,
                        onClick = { onVehicleClick(car.id) }
                    )
                }
            }
        }
    }
}


@Composable
fun VehicleItemCard(
    car: CarEntry,
    onClick: () -> Unit // Callback triggered when the card is pressed
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }, // Navigation to details
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Card Image container using Coil to load from network URL
            Card(
                modifier = Modifier
                    .size(80.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.DarkGray)
            ) {
                // Extracts the first available image URL from the dynamic list
                val mainImageUrl = car.imageUrls.firstOrNull()

                if (mainImageUrl != null) {
                    AsyncImage(
                        model = mainImageUrl,
                        contentDescription = "${car.brand} ${car.model} Thumbnail",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop // Crops to fill the container
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.DirectionsCar,
                            contentDescription = "No Image Available",
                            tint = Color.LightGray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                // Vehicle Brand & Model
                Text(
                    text = "${car.brand} ${car.model}",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
                // Engine and Fuel Type Specifications Row
                Text(
                    text = "${car.engine} cc | ${car.fuelType}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.LightGray
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Pricing info with color (green)
                Text(
                    text = "Price: ${car.price} €",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF4CAF50)
                )
            }
        }
    }
}




@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun VehicleDetailScreen(
    vehicleId: Int,
    viewModel: CarDetailViewModel,
    onBack: () -> Unit,
    onBackHome: () -> Unit,
    onTokenExpired: () -> Unit
) {
    // Local state to control whether the review dialog pop-up is visible
    var showReviewDialog by remember { mutableStateOf(false) }
    // Local state to control whether the delete confirmation pop-up is visible
    var showDeleteDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current


    // Listens for success messages from the ViewModel to display short pop-up alerts (Toasts)
    LaunchedEffect(viewModel.successMessage) {
        viewModel.successMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            // Clear message immediately after showing it to prevent repeating the Toast on screen rotation
            viewModel.clearMessages()
        }
    }

    // Listens for successful deletion to auto navigate the user back to list
    LaunchedEffect(viewModel.isDeleteSuccess) {
        if (viewModel.isDeleteSuccess) {
            onBack() // Go back to the vehicles list
            viewModel.resetDeleteFlag() // Clear the flag
        }
    }

    // Fetch vehicle details whenever the vehicleId changes or the screen is first composed
    LaunchedEffect(vehicleId) {
        viewModel.getCar(vehicleId, onTokenExpired)
        // Start listening to the local database for real-time review updates
        viewModel.observerReviews(vehicleId)
        viewModel.fetchReviews(vehicleId)
    }

    // Clear the ViewModel state when the user leaves this screen
    // Prevents "ghost" data (showing previous car) when navigating to a new car
    DisposableEffect(Unit) {
        onDispose { viewModel.clearState() }
    }

    // Main container
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212)), // Dark background
        contentAlignment = Alignment.Center
    ) {

        if (showReviewDialog) {
            AddReviewDialog(
                onDismiss = { showReviewDialog = false },
                onConfirm = { rating, comment ->
                    viewModel.addReview(vehicleId, rating, comment)
                    showReviewDialog = false
                }
            )
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete Vehicle") },
                text = { Text("Are you sure you want to permanently delete this vehicle and all of its reviews? This action cannot be undone!") },
                confirmButton = {
                    Button(
                        onClick = {
                            // If the car object is valid, request its deletion
                            viewModel.car?.let { viewModel.deleteVehicle(it, onTokenExpired) }
                            showDeleteDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)) // Red color
                    ) {
                        Text("Delete", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDeleteDialog = false }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }

        when {
            // Displaying a circular progress indicator while fetching data from repository
            viewModel.isLoading && viewModel.car == null -> {
                CircularProgressIndicator(color = Color(0xFF1976D2))
            }

            // Handle API or Database errors showing a message and recovery options
            viewModel.errorMessage != null -> {
                // Copy errorMessage to local variable to prevent the value from changing during composition (Thread Safety)
                val message = viewModel.errorMessage
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .background(Color(0xFF2C1414), RoundedCornerShape(24.dp))
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (message != null) { // Smart cast , "message" is now guaranteed to be non-null
                        Text(
                            text = message,
                            color = Color(0xFFEF5350),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.SemiBold
                        )
                    }


                    Spacer(modifier = Modifier.height(20.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Retry button , attempt to fetch data again
                        Button(
                            onClick = { viewModel.getCar(vehicleId, onTokenExpired) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
                        ) {
                            Text("Retry")
                        }

                        // Navigation button to go back to the previous screen
                        Button(
                            onClick = onBack,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                        ) {
                            Text("Go Back")
                        }
                    }
                }
            }

            // Render vehicle info once the data is successfully loaded
            viewModel.car != null -> {
                // Copy viewModel.car to local variable to prevent the value from changing during composition (Thread Safety)
                val car = viewModel.car

                if (car != null) { // Compiler now knows "car" is non-null for the rest of this block
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .verticalScroll(rememberScrollState()) // Enable scrolling for smaller screens
                            .background(Color(0xFF1E1E1E), RoundedCornerShape(24.dp))
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Dynamic Image Slider (HorizontalPager + Coil)
                        if (car.imageUrls.isNotEmpty()) {
                            // Remember the state of the pager based on the total number of items
                            val pagerState = rememberPagerState(pageCount = { car.imageUrls.size })

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1.5f)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color.DarkGray)
                            ) {
                                // Horizontal Pager allows user to swipe through the images
                                HorizontalPager(
                                    state = pagerState,
                                    modifier = Modifier
                                        .fillMaxSize()
                                ) { page ->
                                    AsyncImage(
                                        model = car.imageUrls[page],
                                        contentDescription = "Vehicle Image ${page + 1}",
                                        modifier = Modifier
                                            .fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }

                                // Page Indicator (e.g., "1 / 3") pinned at the bottom-end corner
                                Card(
                                    colors = CardDefaults.cardColors(contentColor = Color.Black.copy(alpha = 0.6f)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .padding(32.dp)
                                        .align(Alignment.BottomEnd)
                                ) {
                                    Text(
                                        text = "${pagerState.currentPage + 1} / ${car.imageUrls.size}",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        modifier = Modifier
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        } else {
                            // Fallback static Box if the Server returns no image URLs
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1.5f)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color.DarkGray),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DirectionsCar,
                                    contentDescription = "No Image Available",
                                    modifier = Modifier.size(100.dp),
                                    tint = Color.Gray
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Row layout containing the vehicle title on the left and the deletion icon on the right
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Vehicle identity: Brand & Model display
                            Text(
                                text = "${car.brand} ${car.model}",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.weight(1f) // Takes up all remaining space on the left
                            )

                            IconButton(
                                onClick = { showDeleteDialog = true }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Vehicle",
                                    tint = Color(0xFFE53935), // Red color
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        // Show the overall vehicle rating score using stars
                        StarRatingBar(rating = car.rating)

                        Spacer(modifier = Modifier.height(12.dp))


                        // Using modular rows to display key-value pairs of car data
                        DetailedSpecRow(
                            label = "Category",
                            value = car.category.displayName
                        )
                        DetailedSpecRow(
                            label = "Production Year",
                            value = car.year.toString()
                        )
                        DetailedSpecRow(
                            label = "Price Tag",
                            value = "${car.price} €",
                            isHighlight = true // Highlight price with a specific color
                        )
                        DetailedSpecRow(
                            label = "Engine",
                            value = "${car.engine} cc",
                        )
                        DetailedSpecRow(
                            label = "Horsepower",
                            value = "${car.horsepower} hp",
                        )
                        DetailedSpecRow(
                            label = "Fuel Consumption",
                            value = "${car.consumption} l/100km",
                        )
                        DetailedSpecRow(
                            label = "Mileage",
                            value = "${car.mileage} km",
                        )
                        DetailedSpecRow(
                            label = "Transmission",
                            value = car.transmission,
                        )
                        DetailedSpecRow(
                            label = "Drivetrain",
                            value = car.drivetrain,
                        )

                        // Extract the unique 11-character ID from the backend video URL string
                        val videoId = extractYoutubeVideoId(car.videoUrl)

                        // Only render the video player layout section if a valid YouTube ID was found
                        if (videoId != null) {
                            Spacer(modifier = Modifier.height(24.dp))

                            // Title section for the video player area
                            Text(
                                text = "Video Review",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Display the AndroidView YouTube Player component into the Compose
                            // (Display embedded YouTube video player)
                            YoutubePlayer(youtubeVideoId = videoId)
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // Visual line separator before the reviews section starts
                        HorizontalDivider(color = Color.DarkGray, thickness = 1.dp)

                        Spacer(modifier = Modifier.height(16.dp))

                        // Section header showing total number of reviews and a button to open the dialog form
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Reviews (${viewModel.reviews.size})",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )

                            TextButton(
                                onClick = { showReviewDialog = true }
                            ) {
                                Text("Write One", color = Color(0xFF1976D2))
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))


                        // Dynamic review area: Show welcome text if empty, or loop and build list items
                        if (viewModel.reviews.isEmpty()) {
                            Text(
                                text = "No reviews yet. Be the first to share yours!",
                                color = Color.Gray,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 16.dp)
                            )
                        } else {
                            viewModel.reviews.forEach { review ->
                                ReviewItem(
                                    username = review.username,
                                    rating = review.rating,
                                    comment = review.comment,
                                    date = review.createdAt
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))


                        // Returns user to the list view
                        Button(
                            onClick = onBack,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
                        ) {
                            Text("Back to List", style = MaterialTheme.typography.titleMedium)
                        }


                        Spacer(modifier = Modifier.height(12.dp))

                        // Jump back to Home/Categories menu
                        TextButton(onClick = onBackHome) {
                            Text("Main Menu", color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}


// Reusable UI component for displaying a single row of vehicle specs
// Follows "Label: Value" pattern
@Composable
fun DetailedSpecRow(
    label: String,
    value: String,
    isHighlight: Boolean = false
) {
    // Row container to align the label and the value horizontally
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween // Pushes label left and value right
    ) {
        // The Label (Category, Year, ...)
        Text(
            text = label,
            color = Color.Gray, // Label color
            fontSize = 16.sp
        )

        // The value of the property
        Text(
            text = value,
            // Green color for the highlighted values , white for the other ones
            color = if (isHighlight) Color(0xFF4CAF50) else Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddVehicleScreen(
    viewModel: CarListViewModel,
    onSaved: () -> Unit,
    onTokenExpired: () -> Unit
) {
    // Using "remember" to keep input values along recomposition
    // Core inputs
    var brand by remember { mutableStateOf("") }
    var modelName by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var engine by remember { mutableStateOf("") }
    var horsepower by remember { mutableStateOf("") }
    var mileage by remember { mutableStateOf("") }

    // Dropdowns
    var fuelType by remember { mutableStateOf("Gasoline") }
    var showFuelMenu by remember { mutableStateOf(false) }
    val fuelOptions = listOf("Gasoline", "Diesel", "Electric", "Hybrid")

    var selectedCategory by remember { mutableStateOf(CarCategory.SEDAN) }

    // Changed from Bitmap to Uri, keeps track of the chosen image location
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    var description by remember { mutableStateOf("") }
    var priceNegotiable by remember { mutableStateOf(false) }
    var drivetrain by remember { mutableStateOf("") }
    var transmission by remember { mutableStateOf("") }
    var torque by remember { mutableStateOf("") }
    var consumption by remember { mutableStateOf("") }
    var interiorColor by remember { mutableStateOf("") }
    var exteriorColor by remember { mutableStateOf("") }
    var wheelSize by remember { mutableStateOf("") }
    var doors by remember { mutableStateOf("") }
    var passengers by remember { mutableStateOf("") }
    var isRightHandDrive by remember { mutableStateOf(false) }
    var location by remember { mutableStateOf("") }
    var videoUrl by remember { mutableStateOf("") }

    var sellerType by remember { mutableStateOf(SellerType.PRIVATE) }
    var showSellerMenu by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()


    // Prepare a URI where the captured image will be temporarily stored
    val photoUri = remember {
        val file = File(context.cacheDir, "vehicle_${System.currentTimeMillis()}.jpg")
        FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
    }


    // Triggered when the user takes a photo successfully
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            selectedImageUri = photoUri // Save the file URI directly
        }
    }

    // Allows the user to pick an existing photo from the device gallery
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let {
            selectedImageUri = it // Save the gallery URI directly
        }
    }

    // Handles the runtime request for Camera access
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            cameraLauncher.launch(photoUri)
        }
    }

    // Simple check to ensure all required fields are filled and a photo is selected
    // Now checks that an image URI is selected instead of a Bitmap
    val isFormValid = brand.isNotBlank() && modelName.isNotBlank()
            && year.isNotBlank() && price.isNotBlank() && engine.isNotBlank()
            && mileage.isNotBlank() && selectedImageUri != null

    // UI layout
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .verticalScroll(rememberScrollState()) // Scrolling for smaller screens
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Add New Vehicle",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        // Image selection
        Box(
            modifier = Modifier
                .size(200.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF1E1E1E))
                .clickable {
                    // Check for camera permissions before launching the camera
                    val permission = Manifest.permission.CAMERA
                    if (ContextCompat.checkSelfPermission(
                            context,
                            permission
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        cameraLauncher.launch(photoUri)
                    } else {
                        permissionLauncher.launch(permission)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            // If a Uri exists , Coil loads it instantly into the preview box
            selectedImageUri?.let { uri ->
                AsyncImage(
                    model = uri,
                    contentDescription = "Vehicle Preview",
                    modifier = Modifier
                        .fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } ?: run {
                // Display a placeholder with option to choose from the gallery
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.AddAPhoto,
                        contentDescription = "Selected Image",
                        tint = Color.Gray,
                        modifier = Modifier
                            .size(48.dp)
                    )

                    TextButton(
                        onClick = {
                            galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }
                    ) {
                        Text("or Choose from Gallery", color = Color(0xFF1976D2))
                    }
                }
            }
        }


        // Standard TextFields: Using custom VehicleTextField
        VehicleTextField(
            value = brand,
            onValueChange = { brand = it },
            label = "Brand (e.g. BMW)"
        )
        VehicleTextField(
            value = modelName,
            onValueChange = { modelName = it },
            label = "Model (e.g. M4)"
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier.weight(1f)
            ) {
                VehicleTextField(
                    value = year,
                    onValueChange = { year = it },
                    label = "Year",
                    isNumber = true
                )
            }

            Box(
                modifier = Modifier.weight(1f)
            )
            {
                VehicleTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = "Price (€)",
                    isNumber = true
                )
            }
        }

        // Checkbox for Negotiable Price row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = priceNegotiable,
                onCheckedChange = { priceNegotiable = it }
            )
            Text(
                "Price is Negotiable",
                color = Color.White
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier.weight(1f)
            )
            {
                VehicleTextField(
                    value = engine,
                    onValueChange = { engine = it },
                    label = "Engine (CC)",
                    isNumber = true
                )
            }

            Box(
                modifier = Modifier.weight(1f)
            )
            {
                VehicleTextField(
                    value = horsepower,
                    onValueChange = { horsepower = it },
                    label = "Horsepower (HP)",
                    isNumber = true
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        )
        {
            Box(
                modifier = Modifier.weight(1f)
            )
            {
                VehicleTextField(
                    value = mileage,
                    onValueChange = { mileage = it },
                    label = "Mileage (km)",
                    isNumber = true
                )
            }

            Box(
                modifier = Modifier.weight(1f)
            )
            {
                VehicleTextField(
                    value = consumption,
                    onValueChange = { consumption = it },
                    label = "Consumption (l/100km)",
                    isNumber = true
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        )
        {
            Box(
                modifier = Modifier.weight(1f)
            )
            {
                VehicleTextField(
                    value = transmission,
                    onValueChange = { transmission = it },
                    label = "Transmission (e.g. Automatic)"
                )
            }

            Box(
                modifier = Modifier.weight(1f)
            )
            {
                VehicleTextField(
                    value = drivetrain,
                    onValueChange = { drivetrain = it },
                    label = "Drivetrain (e.g. AWD)"
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        )
        {
            Box(
                modifier = Modifier.weight(1f)
            )
            {
                VehicleTextField(
                    value = torque,
                    onValueChange = { torque = it },
                    label = "Torque (Nm)",
                    isNumber = true
                )
            }

            Box(
                modifier = Modifier.weight(1f)
            )
            {
                VehicleTextField(
                    value = wheelSize,
                    onValueChange = { wheelSize = it },
                    label = "Wheel Size (inches)",
                    isNumber = true
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        )
        {
            Box(
                modifier = Modifier.weight(1f)
            )
            {
                VehicleTextField(
                    value = doors,
                    onValueChange = { doors = it },
                    label = "Doors",
                    isNumber = true
                )
            }

            Box(
                modifier = Modifier.weight(1f)
            )
            {
                VehicleTextField(
                    value = passengers,
                    onValueChange = { passengers = it },
                    label = "Passengers Capacity",
                    isNumber = true
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        )
        {
            Box(
                modifier = Modifier.weight(1f)
            )
            {
                VehicleTextField(
                    value = exteriorColor,
                    onValueChange = { exteriorColor = it },
                    label = "Exterior Color",
                    isNumber = true
                )
            }

            Box(
                modifier = Modifier.weight(1f)
            )
            {
                VehicleTextField(
                    value = interiorColor,
                    onValueChange = { interiorColor = it },
                    label = "Interior Color",
                    isNumber = true
                )
            }
        }

        VehicleTextField(
            value = location,
            onValueChange = { location = it },
            label = "Location / City",
            isNumber = true
        )

        VehicleTextField(
            value = videoUrl,
            onValueChange = { videoUrl = it },
            label = "YouTube Video Link Review",
            isNumber = true
        )

        // Large description Box field
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Detailed Vehicle Description") },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
        )

        // Checkbox for Right Hand Drive
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isRightHandDrive,
                onCheckedChange = { isRightHandDrive = it }
            )
            Text(
                "Right Hand Drive (RHD)",
                color = Color.White
            )
        }


        // Fuel Type Dropdown
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "Fuel Type",
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E1E1E), RoundedCornerShape(4.dp))
                    .clickable { showFuelMenu = true }
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = fuelType,
                        color = Color.White
                    )
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = Color.Gray
                    )
                }

                DropdownMenu(
                    expanded = showFuelMenu,
                    onDismissRequest = { showFuelMenu = false },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                ) {
                    fuelOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                fuelType = option
                                showFuelMenu = false
                            }
                        )
                    }
                }
            }
        }


        // Seller Type Dropdown Menu
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "Seller Type",
                color = Color.White
            )

            Spacer(modifier = Modifier.height(4.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E1E1E),
                        RoundedCornerShape(4.dp))
                    .clickable { showSellerMenu = true }
                    .padding(16.dp)
            )
            {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = sellerType.name,
                        color = Color.White
                    )
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = Color.Gray
                    )
                }
                DropdownMenu(
                    expanded = showSellerMenu,
                    onDismissRequest = { showSellerMenu = false },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                ) {
                    SellerType.entries.filter { it != SellerType.UNKNOWN }.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type.name) },
                            onClick = { sellerType = type; showSellerMenu = false }
                        )
                    }
                }
            }

        }



        // Category selection
        Text(
            "Category",
            color = Color.White,
            modifier = Modifier
                .align(Alignment.Start)
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Filter out "UNKNOWN" keeping UI clean
            items(CarCategory.entries.filter { it != CarCategory.UNKNOWN }) { category ->
                val isSelected = selectedCategory == category
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedCategory = category },
                    label = { Text(category.displayName) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF1976D2),
                        selectedLabelColor = Color.White,
                        containerColor = Color(0xFF1E1E1E),
                        labelColor = Color.Gray
                    )
                )
            }
        }

        // Submit Action Button
        Button(
            onClick = {
                // Copy the image URI to local variable to prevent the value from changing during composition (Thread Safety)
                val imageUri = selectedImageUri
                // Ensure the image URI exists before attempting to create CarEntry object
                if (imageUri != null) {
                    // Construct the data object using input values and default properties from CarEntry
                    val newVehicle = CarEntry(
                        id = 0,
                        brand = brand,
                        model = modelName,
                        year = year.toIntOrNull() ?: 2024,
                        price = price.toDoubleOrNull() ?: 0.0,
                        engine = engine.toIntOrNull() ?: 0,
                        horsepower = horsepower.toIntOrNull() ?: 0,
                        mileage = mileage.toIntOrNull() ?: 0,
                        fuelType = fuelType,
                        category = selectedCategory,

                        // Converts the local file/gallery URI to String and passes it inside the list
                        imageUrls = listOf(imageUri.toString()),

                        description = description,
                        priceNegotiable = priceNegotiable,
                        drivetrain = drivetrain,
                        transmission = transmission,
                        torque = torque.toIntOrNull() ?: 0,
                        consumption = consumption.toDoubleOrNull() ?: 0.0,
                        interiorColor = interiorColor,
                        exteriorColor = exteriorColor,
                        wheelSize = wheelSize.toIntOrNull() ?: 0,
                        doors = doors.toIntOrNull() ?: 0,
                        passengers = passengers.toIntOrNull() ?: 0,
                        isRightHandDrive = isRightHandDrive,
                        location = location,
                        sellerType = sellerType,
                        rating = 5.0f,
                        videoUrl = videoUrl
                    )
                    viewModel.addCar(newVehicle, onTokenExpired)
                    onSaved() // Callback to navigate back or reset UI
                }
            },
            // Button enabled only when the form is valid and not currently loading
            enabled = isFormValid && !uiState.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier
                        .size(24.dp)
                )
            } else {
                Text(
                    "Save Vehicle",
                    fontSize = 18.sp
                )
            }
        }
    }
}

// Custom text field (reusable) for vehicle data entry
@Composable
fun VehicleTextField(
    value: String,
    onValueChange: (String) -> Unit, // onValueChange Callback runs when the user types or deletes text
    label: String,
    isNumber: Boolean = false // Determines if the numeric keyboard should be shown
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),

        // Dynamically switches between numeric and text input based on the "isNumber"
        keyboardOptions = KeyboardOptions(
            keyboardType = if (isNumber) KeyboardType.Number
            else KeyboardType.Text),

        // Color customization
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedBorderColor = Color(0xFF1976D2), // Blue when selected
            unfocusedBorderColor = Color.Gray, // Gray when idle
            focusedLabelColor = Color(0xFF1976D2),
            unfocusedLabelColor = Color.Gray
        )

    )
}

