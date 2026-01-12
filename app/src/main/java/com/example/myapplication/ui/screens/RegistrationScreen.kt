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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Conference Info Button
        OutlinedButton(
            onClick = openConferenceWebsite,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(Icons.Default.Info, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Conference Information", fontSize = 16.sp)
        }
        
        // Photo Section
        Box(
            modifier = Modifier
                .size(150.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
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
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Tap to take photo",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        // User ID Input
        OutlinedTextField(
            value = uiState.userId,
            onValueChange = viewModel::updateUserId,
            label = { Text("User ID *") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            isError = uiState.duplicateIdWarning != null
        )
        
        // Duplicate ID Warning
        if (uiState.duplicateIdWarning != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = ErrorRed.copy(alpha = 0.1f)
                )
            ) {
                Text(
                    text = uiState.duplicateIdWarning!!,
                    modifier = Modifier.padding(12.dp),
                    color = ErrorRed,
                    fontSize = 14.sp
                )
            }
        }
        
        // Full Name Input
        OutlinedTextField(
            value = uiState.fullName,
            onValueChange = viewModel::updateFullName,
            label = { Text("Full Name *") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )
        
        // Title Spinner
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
                label = { Text("Title") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = titleExpanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
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
        
        // Registration Type Radio Group
        Text(
            "Registration Type",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            RegistrationTypeOption(
                type = 1,
                label = "Full",
                selected = uiState.registrationType == 1,
                onSelect = { viewModel.updateRegistrationType(1) }
            )
            RegistrationTypeOption(
                type = 2,
                label = "Student",
                selected = uiState.registrationType == 2,
                onSelect = { viewModel.updateRegistrationType(2) }
            )
            RegistrationTypeOption(
                type = 3,
                label = "None",
                selected = uiState.registrationType == 3,
                onSelect = { viewModel.updateRegistrationType(3) }
            )
        }
        
        Spacer(Modifier.weight(1f))
        
        // Error Message
        if (uiState.errorMessage != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = ErrorRed.copy(alpha = 0.1f)
                )
            ) {
                Text(
                    text = uiState.errorMessage!!,
                    modifier = Modifier.padding(12.dp),
                    color = ErrorRed,
                    fontSize = 14.sp
                )
            }
        }
        
        // Success Message
        if (uiState.successMessage != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text(
                    text = uiState.successMessage!!,
                    modifier = Modifier.padding(12.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontSize = 14.sp
                )
            }
        }
        
        // Register Button
        Button(
            onClick = viewModel::registerParticipant,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            enabled = !uiState.isLoading
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Register", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
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
    FilterChip(
        selected = selected,
        onClick = onSelect,
        label = { Text(label) },
        modifier = modifier
    )
}
