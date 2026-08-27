package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.NativeBridge
import com.example.data.DownloadState
import com.example.data.GuestOSManager
import com.example.data.Instance
import com.example.ui.theme.OutlineDark
import com.example.ui.theme.PureBlack
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.util.UriUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BootScreen(
    instance: Instance?,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val instanceName = instance?.name ?: "Alpine Linux"
    val ramMb = instance?.ramMb ?: 512
    val hasCustomOs = !instance?.osImageUri.isNullOrEmpty() && !instance?.kernelUri.isNullOrEmpty()
    val hasPartialCustom = (!instance?.osImageUri.isNullOrEmpty() && instance?.kernelUri.isNullOrEmpty()) ||
        (instance?.osImageUri.isNullOrEmpty() && !instance?.kernelUri.isNullOrEmpty())

    val logLines = remember { mutableStateListOf<String>() }
    var isRunning by remember { mutableStateOf(false) }
    var bootStatusText by remember { mutableStateOf("Initializing QEMU aarch64 runtime...") }
    val guestManager = remember { GuestOSManager(context) }

    fun startBootSequence() {
        scope.launch {
            logLines.clear()

            // Case A: Custom OS with both Kernel & RootFS provided
            if (hasCustomOs) {
                bootStatusText = "Preparing custom OS kernel and rootfs..."
                val kernelLocalPath = UriUtils.copyUriToCache(context, instance?.kernelUri!!, "custom_kernel_${instance.id}")
                val rootfsLocalPath = UriUtils.copyUriToCache(context, instance.osImageUri!!, "custom_rootfs_${instance.id}")

                if (kernelLocalPath == null || rootfsLocalPath == null) {
                    bootStatusText = "Error resolving custom OS files from storage."
                    return@launch
                }

                val qemuBinPath = "${context.applicationInfo.nativeLibraryDir}/libqemu-system-aarch64.so"
                val qemuFile = File(qemuBinPath)

                if (!qemuFile.exists() || !qemuFile.canExecute()) {
                    bootStatusText = "QEMU engine not available — native VM launch not yet implemented in this build"
                    return@launch
                }

                bootStatusText = "Launching custom guest kernel via JNI..."
                val started = NativeBridge.startVm(
                    qemuPath = qemuBinPath,
                    kernelPath = kernelLocalPath,
                    initrdPath = rootfsLocalPath,
                    cmdline = "root=/dev/ram0 rw quiet",
                    memoryMb = ramMb
                )

                if (started) {
                    isRunning = true
                    bootStatusText = "Guest VM running (console=ttyAMA0)"
                } else {
                    bootStatusText = "Failed to launch native QEMU process."
                }
                return@launch
            }

            // Case B: Partial custom config missing either Kernel or RootFS
            if (hasPartialCustom) {
                val missingComponent = if (instance?.kernelUri.isNullOrEmpty()) "Kernel Image" else "RootFS/Initrd Image"
                bootStatusText = "Custom OS configuration incomplete: missing $missingComponent. Both kernel and rootfs are required for custom OS."
                return@launch
            }

            // Case C: Quick start default path (Alpine Linux bundled kernel + rootfs)
            bootStatusText = "Checking Alpine Linux bundled kernel & minirootfs..."
            guestManager.prepareGuestOS { state ->
                when (state) {
                    is DownloadState.Progress -> {
                        bootStatusText = "${state.status} (${state.percentage}%)"
                    }
                    is DownloadState.Ready -> {
                        val qemuBinPath = "${context.applicationInfo.nativeLibraryDir}/libqemu-system-aarch64.so"
                        val qemuFile = File(qemuBinPath)

                        if (!qemuFile.exists() || !qemuFile.canExecute()) {
                            bootStatusText = "QEMU engine not available — native VM launch not yet implemented in this build"
                            return@prepareGuestOS
                        }

                        bootStatusText = "Launching Alpine guest via JNI..."
                        val started = NativeBridge.startVm(
                            qemuPath = qemuBinPath,
                            kernelPath = state.kernelPath,
                            initrdPath = state.rootfsPath,
                            cmdline = "root=/dev/ram0 rw quiet",
                            memoryMb = ramMb
                        )

                        if (started) {
                            isRunning = true
                            bootStatusText = "Guest VM running (console=ttyAMA0)"
                        } else {
                            bootStatusText = "Failed to launch native QEMU process."
                        }
                    }
                    is DownloadState.Error -> {
                        bootStatusText = state.message
                    }
                    else -> Unit
                }
            }
        }
    }

    // Polling real serial console output from native JNI pipe
    LaunchedEffect(isRunning) {
        if (isRunning) {
            while (isActive && NativeBridge.isVmRunning()) {
                val output = NativeBridge.readSerialOutput()
                if (output.isNotEmpty()) {
                    val lines = output.split("\n")
                    lines.forEach { line ->
                        if (line.isNotEmpty()) {
                            logLines.add(line)
                        }
                    }
                }
                delay(30)
            }
            if (!NativeBridge.isVmRunning() && isRunning) {
                isRunning = false
                bootStatusText = "Guest execution completed."
            }
        }
    }

    // Auto-scroll to bottom of serial terminal as lines arrive
    LaunchedEffect(logLines.size) {
        if (logLines.isNotEmpty()) {
            listState.animateScrollToItem(logLines.size - 1)
        }
    }

    // Start boot on first entry
    LaunchedEffect(Unit) {
        startBootSequence()
    }

    // Clean up native VM process on exit
    DisposableEffect(Unit) {
        onDispose {
            NativeBridge.stopVm()
        }
    }

    Scaffold(
        containerColor = PureBlack,
        contentColor = PureWhite,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = instanceName.uppercase(),
                        color = PureWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 2.sp,
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("boot_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = PureWhite
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            NativeBridge.stopVm()
                            startBootSequence()
                        },
                        modifier = Modifier.testTag("boot_restart_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Refresh,
                            contentDescription = "Restart VM",
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
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(PureBlack)
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Specs and Serial Console Status
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                color = if (isRunning) PureWhite else TextTertiary,
                                shape = CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isRunning) "TTY: ACTIVE (ttyAMA0)" else "TTY: HALTED",
                        color = PureWhite,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Memory,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$ramMb MB",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Live Serial Console Terminal Display
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.dp, OutlineDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("os_serial_terminal_display")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                ) {
                    // Terminal Top Bar
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Terminal,
                                contentDescription = null,
                                tint = PureWhite,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "SERIAL CONSOLE [ttyAMA0]",
                                color = PureWhite,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }

                        Text(
                            text = "115200 8N1",
                            color = TextTertiary,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // Log output scroll
                    if (logLines.isEmpty()) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            Text(
                                text = "No serial stream output available.",
                                color = TextTertiary,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .testTag("serial_log_lazy_column")
                        ) {
                            items(logLines) { line ->
                                Text(
                                    text = line,
                                    color = PureWhite,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    lineHeight = 15.sp,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Live Status Text
            Text(
                text = bootStatusText,
                color = TextSecondary,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
                    .testTag("boot_status_text")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons (Start/Stop + Return)
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isRunning) {
                    OutlinedButton(
                        onClick = {
                            NativeBridge.stopVm()
                            isRunning = false
                            bootStatusText = "VM stopped by user"
                        },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, OutlineDark),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = PureBlack,
                            contentColor = PureWhite
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("boot_stop_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Pause,
                            contentDescription = null,
                            tint = PureWhite,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Stop Guest",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    Button(
                        onClick = {
                            startBootSequence()
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PureWhite,
                            contentColor = PureBlack
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("boot_start_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.PlayArrow,
                            contentDescription = null,
                            tint = PureBlack,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Boot Guest",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                OutlinedButton(
                    onClick = onBackClick,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, OutlineDark),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = PureBlack,
                        contentColor = PureWhite
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("boot_screen_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = null,
                        tint = PureWhite,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Main Menu",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}
