package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.OutlineDark
import com.example.ui.theme.OutlineVariantDark
import com.example.ui.theme.PureBlack
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.util.UriUtils
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddInstanceScreen(
    onSaveInstance: (name: String, iconUri: String?, osImageUri: String?, kernelUri: String?, ramMb: Int, storageGb: Int) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf<String?>(null) }
    var selectedIconUri by remember { mutableStateOf<Uri?>(null) }
    var selectedOsImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedKernelUri by remember { mutableStateOf<Uri?>(null) }
    var ramMbValue by remember { mutableFloatStateOf(2048f) }
    var storageGbValue by remember { mutableFloatStateOf(16f) }

    val iconPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedIconUri = uri
        }
    }

    val osFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedOsImageUri = uri
        }
    }

    val kernelFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedKernelUri = uri
        }
    }

    val scrollState = rememberScrollState()

    Scaffold(
        containerColor = PureBlack,
        contentColor = PureWhite,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "NEW INSTANCE",
                        color = PureWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 2.sp
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onCancel,
                        modifier = Modifier.testTag("add_instance_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = PureWhite
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = PureBlack,
                    navigationIconContentColor = PureWhite,
                    titleContentColor = PureWhite
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(PureBlack)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Icon Picker Section
            Text(
                text = "INSTANCE ICON",
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.dp, OutlineDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        iconPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                    .testTag("icon_picker_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(PureBlack)
                    ) {
                        if (selectedIconUri != null) {
                            AsyncImage(
                                model = selectedIconUri,
                                contentDescription = "Selected Icon Preview",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.AddPhotoAlternate,
                                contentDescription = "Pick Icon",
                                tint = TextSecondary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (selectedIconUri != null) "Icon Selected" else "Choose Custom Icon",
                            color = PureWhite,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (selectedIconUri != null) "Tap to change image" else "Select from device storage",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }

                    if (selectedIconUri != null) {
                        IconButton(
                            onClick = { selectedIconUri = null },
                            modifier = Modifier.testTag("clear_icon_button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = "Remove Icon",
                                tint = TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Name Field
            Text(
                text = "INSTANCE NAME *",
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    if (it.trim().isNotEmpty()) {
                        nameError = null
                    }
                },
                placeholder = {
                    Text(text = "e.g. Alpine Linux, Debian aarch64", color = TextTertiary)
                },
                isError = nameError != null,
                supportingText = {
                    if (nameError != null) {
                        Text(text = nameError!!, color = Color(0xFFCF6679))
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SurfaceDark,
                    unfocusedContainerColor = SurfaceDark,
                    errorContainerColor = SurfaceDark,
                    focusedTextColor = PureWhite,
                    unfocusedTextColor = PureWhite,
                    cursorColor = PureWhite,
                    focusedBorderColor = PureWhite,
                    unfocusedBorderColor = OutlineDark,
                    errorBorderColor = Color(0xFFCF6679)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("instance_name_input")
            )

            Spacer(modifier = Modifier.height(20.dp))

            // RootFS / OS Image Picker Section
            Text(
                text = "ROOTFS / INITRD IMAGE (OPTIONAL)",
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.dp, OutlineDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { osFilePickerLauncher.launch("*/*") }
                    .testTag("os_image_picker_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(PureBlack)
                    ) {
                        Icon(
                            imageVector = if (selectedOsImageUri != null) Icons.Outlined.Description else Icons.Outlined.FolderOpen,
                            contentDescription = "OS RootFS File",
                            tint = PureWhite,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        val fileName = if (selectedOsImageUri != null) {
                            UriUtils.getFileName(context, selectedOsImageUri)
                        } else {
                            "Select RootFS / Initrd (or leave blank for default Alpine)"
                        }
                        Text(
                            text = fileName,
                            color = PureWhite,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (selectedOsImageUri != null) "RootFS linked" else "tar.gz, cpio, iso, or img format",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }

                    if (selectedOsImageUri != null) {
                        IconButton(
                            onClick = { selectedOsImageUri = null },
                            modifier = Modifier.testTag("clear_os_image_button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = "Clear OS Image",
                                tint = TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Kernel Image Picker Section
            Text(
                text = "KERNEL IMAGE (OPTIONAL — REQUIRED FOR CUSTOM OS)",
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.dp, OutlineDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { kernelFilePickerLauncher.launch("*/*") }
                    .testTag("kernel_image_picker_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(PureBlack)
                    ) {
                        Icon(
                            imageVector = if (selectedKernelUri != null) Icons.Outlined.Description else Icons.Outlined.FolderOpen,
                            contentDescription = "Kernel File",
                            tint = PureWhite,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        val fileName = if (selectedKernelUri != null) {
                            UriUtils.getFileName(context, selectedKernelUri)
                        } else {
                            "Select aarch64 Kernel Image (vmlinuz/Image)"
                        }
                        Text(
                            text = fileName,
                            color = PureWhite,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (selectedKernelUri != null) "Custom Kernel linked" else "Leave blank to use default bundled Alpine kernel",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }

                    if (selectedKernelUri != null) {
                        IconButton(
                            onClick = { selectedKernelUri = null },
                            modifier = Modifier.testTag("clear_kernel_image_button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = "Clear Kernel Image",
                                tint = TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // RAM Allocation Slider
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.dp, OutlineDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Memory,
                                contentDescription = "RAM",
                                tint = PureWhite,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "RAM Allocation",
                                color = PureWhite,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Text(
                            text = "${ramMbValue.roundToInt()} MB",
                            color = PureWhite,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.testTag("ram_value_label")
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Slider(
                        value = ramMbValue,
                        onValueChange = { ramMbValue = it },
                        valueRange = 512f..4096f,
                        steps = 6,
                        colors = SliderDefaults.colors(
                            thumbColor = PureWhite,
                            activeTrackColor = PureWhite,
                            inactiveTrackColor = OutlineDark
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ram_slider")
                    )

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "512 MB", color = TextTertiary, fontSize = 11.sp)
                        Text(text = "4096 MB", color = TextTertiary, fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Storage Allocation Slider
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.dp, OutlineDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Storage,
                                contentDescription = "Storage",
                                tint = PureWhite,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Storage Allocation",
                                color = PureWhite,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Text(
                            text = "${storageGbValue.roundToInt()} GB",
                            color = PureWhite,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.testTag("storage_value_label")
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Slider(
                        value = storageGbValue,
                        onValueChange = { storageGbValue = it },
                        valueRange = 1f..64f,
                        steps = 62,
                        colors = SliderDefaults.colors(
                            thumbColor = PureWhite,
                            activeTrackColor = PureWhite,
                            inactiveTrackColor = OutlineDark
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("storage_slider")
                    )

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "1 GB", color = TextTertiary, fontSize = 11.sp)
                        Text(text = "64 GB", color = TextTertiary, fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Save Button
            Button(
                onClick = {
                    val trimmedName = name.trim()
                    if (trimmedName.isEmpty()) {
                        nameError = "Instance name is required"
                        return@Button
                    }
                    onSaveInstance(
                        trimmedName,
                        selectedIconUri?.toString(),
                        selectedOsImageUri?.toString(),
                        selectedKernelUri?.toString(),
                        ramMbValue.roundToInt(),
                        storageGbValue.roundToInt()
                    )
                },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PureWhite,
                    contentColor = PureBlack
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("save_instance_button")
            ) {
                Text(
                    text = "Save Instance",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Cancel Button
            OutlinedButton(
                onClick = onCancel,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, OutlineDark),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = PureBlack,
                    contentColor = PureWhite
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("cancel_instance_button")
            ) {
                Text(
                    text = "Cancel",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
