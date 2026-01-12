package com.example.myapplication.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.myapplication.ui.theme.ErrorRed
import com.example.myapplication.ui.viewmodel.RegistrationViewModel
import com.example.myapplication.utils.createImageFile
import com.example.myapplication.utils.getImageUri
import com.example.myapplication.utils.hasCameraPermission
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationScreen(
    viewModel: RegistrationViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var imageFile by remember { mutableStateOf<File?>(null) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    
    var shouldLaunchCamera by remember { mutableStateOf(false) }
    
    // Camera permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            try {
                imageFile = createImageFile(context)
                imageUri = getImageUri(context, imageFile!!)
                shouldLaunchCamera = true
            } catch (e: Exception) {
                // Handle file creation error
            }
        }
    }
    
    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && imageFile != null) {
            viewModel.updatePhotoPath(imageFile!!.absolutePath)
        }
        shouldLaunchCamera = false
    }
    
    // Open camera when clicked (with permission check)
    val openCamera = {
        if (hasCameraPermission(context)) {
            try {
                imageFile = createImageFile(context)
                imageUri = getImageUri(context, imageFile!!)
                shouldLaunchCamera = true
            } catch (e: Exception) {
                // Handle file creation error
            }
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
    
    // Launch camera when permission is granted and file is ready
    LaunchedEffect(shouldLaunchCamera, imageUri) {
        if (shouldLaunchCamera && imageUri != null && hasCameraPermission(context)) {
            cameraLauncher.launch(imageUri!!)
        }
    }
    
    // Conference info button - opens website
    val openConferenceWebsite = {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.example-conference.com"))
        context.startActivity(intent)
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // Header Section
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primaryContainer,
            tonalElevation = 2.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp, horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "New Registration",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(Modifier.height(8.dp))
                TextButton(
                    onClick = openConferenceWebsite,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Conference Website", fontSize = 14.sp)
                }
            }
        }
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Photo Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                            .clickable { openCamera() },
                        contentAlignment = Alignment.Center
                    ) {
                        if (uiState.photoPath != null) {
                            Image(
                                painter = rememberAsyncImagePainter(model = uiState.photoPath),
                                contentDescription = "Profile Photo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Take Photo",
                                    modifier = Modifier.size(56.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "Add Photo",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }
            }
            
            // Form Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // User ID Input
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "User ID",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        OutlinedTextField(
                            value = uiState.userId,
                            onValueChange = viewModel::updateUserId,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            isError = uiState.duplicateIdWarning != null,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                        if (uiState.duplicateIdWarning != null) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = ErrorRed.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = uiState.duplicateIdWarning!!,
                                    modifier = Modifier.padding(12.dp),
                                    color = ErrorRed,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                    
                    // Full Name Input
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Full Name",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        OutlinedTextField(
                            value = uiState.fullName,
                            onValueChange = viewModel::updateFullName,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                    }
                    
                    // Title Dropdown
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Title",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        var titleExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = titleExpanded,
                            onExpandedChange = { titleExpanded = !titleExpanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = uiState.title,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = titleExpanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = titleExpanded,
                                onDismissRequest = { titleExpanded = false }
                            ) {
                                listOf("Prof.", "Dr.", "Student").forEach { title ->
                                    DropdownMenuItem(
                                        text = { Text(title) },
                                        onClick = {
                                            viewModel.updateTitle(title)
                                            titleExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    
                    // Registration Type
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            "Registration Type",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            RegistrationTypeOption(
                                type = 1,
                                label = "Full",
                                selected = uiState.registrationType == 1,
                                onSelect = { viewModel.updateRegistrationType(1) },
                                modifier = Modifier.weight(1f)
                            )
                            RegistrationTypeOption(
                                type = 2,
                                label = "Student",
                                selected = uiState.registrationType == 2,
                                onSelect = { viewModel.updateRegistrationType(2) },
                                modifier = Modifier.weight(1f)
                            )
                            RegistrationTypeOption(
                                type = 3,
                                label = "None",
                                selected = uiState.registrationType == 3,
                                onSelect = { viewModel.updateRegistrationType(3) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
            
            // Error/Success Messages
            if (uiState.errorMessage != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = ErrorRed.copy(alpha = 0.15f),
                    tonalElevation = 2.dp
                ) {
                    Text(
                        text = uiState.errorMessage!!,
                        modifier = Modifier.padding(16.dp),
                        color = ErrorRed,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            if (uiState.successMessage != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                    tonalElevation = 2.dp
                ) {
                    Text(
                        text = uiState.successMessage!!,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            // Register Button
            Button(
                onClick = viewModel::registerParticipant,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(18.dp),
                enabled = !uiState.isLoading,
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(28.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        "Register Participant",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
            
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun RegistrationTypeOption(
    type: Int,
    label: String,
    selected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(50.dp)
            .clickable { onSelect() },
        shape = RoundedCornerShape(14.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = if (selected) 4.dp else 1.dp
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                label,
                fontSize = 14.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
