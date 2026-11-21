package ai.antigem.ui.studio

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class StudioProject(
    val id: String,
    val name: String,
    val type: String,
    val description: String,
    val localUrl: String = "http://localhost:5173",
    val firebaseUrl: String? = null,
    val status: String = "Ready",
    val timestamp: String = "Just now"
)

enum class BuildStatus { IDLE, INTERVIEW, BUILDING, COMPLETED }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KestrelStudioScreen(
    isVisible: Boolean,
    onClose: () -> Unit,
    onOpenBrowser: (String) -> Unit = {},
    onOpenTerminal: () -> Unit = {}
) {
    if (!isVisible) return

    BackHandler(onBack = onClose)

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var promptText by remember { mutableStateOf("") }
    var showInterviewModal by remember { mutableStateOf(false) }
    var buildStatus by remember { mutableStateOf(BuildStatus.IDLE) }
    var buildStep by remember { mutableIntStateOf(0) }
    var activeTab by remember { mutableIntStateOf(0) } // 0: Creator, 1: My Projects

    // Interview selections
    var targetPlatform by remember { mutableStateOf("React + Vite (Web)") }
    var themeStyle by remember { mutableStateOf("Dark Obsidian") }
    var databaseChoice by remember { mutableStateOf("Firebase Firestore") }
    val selectedFeatures = remember { mutableStateListOf("Search & Filter", "Responsive UI", "Auth & Login") }

    val recentProjects = remember {
        mutableStateListOf(
            StudioProject("1", "Shahid Notes App", "React + Vite", "Rich notes app with Firebase & search", "http://localhost:5173", "https://shahid-notes.web.app", "Deployed", "Today"),
            StudioProject("2", "Crypto Live Tracker", "Web + APK", "Live coins price tracker with coinmarketcap API", "http://localhost:5174", null, "Localhost", "Yesterday"),
            StudioProject("3", "E-Commerce Mini Store", "Full Stack", "Minimal cart, checkout & stripe integration", "http://localhost:5175", "https://kestrel-store.web.app", "Deployed", "Nov 18")
        )
    }

    val quickTemplates = listOf(
        "📝 Notes App with Voice & Search",
        "🪙 Crypto Price Tracker with Live Charts",
        "🛍️ E-Commerce Mini Store with Cart",
        "💬 Realtime Team Chat with Firebase",
        "📊 Personal Finance & Expense Tracker"
    )

    fun startBuilding() {
        showInterviewModal = false
        buildStatus = BuildStatus.BUILDING
        buildStep = 0
        scope.launch {
            delay(1200)
            buildStep = 1 // Scaffolding Vite project
            delay(1500)
            buildStep = 2 // Installing dependencies (React, Tailwind, Firebase)
            delay(1800)
            buildStep = 3 // Generating components & hooks
            delay(1600)
            buildStep = 4 // Starting local dev server
            delay(1000)
            buildStatus = BuildStatus.COMPLETED

            val newProjName = promptText.lines().firstOrNull()?.take(26)?.ifBlank { "Kestrel App" } ?: "Kestrel App"
            recentProjects.add(0, StudioProject(
                id = System.currentTimeMillis().toString(),
                name = newProjName,
                type = targetPlatform,
                description = promptText.take(60),
                localUrl = "http://localhost:5173",
                firebaseUrl = if (databaseChoice.contains("Firebase")) "https://kestrel-${newProjName.lowercase().replace(" ", "-")}.web.app" else null,
                status = "Running",
                timestamp = "Just now"
            ))
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF0C0E14)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // --- TOP APP BAR ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Kestrel Studio",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF00F5D4).copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, Color(0xFF00F5D4).copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "AI BUILDER",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00F5D4),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "Prompt-to-App & Web Architect",
                        fontSize = 11.5.sp,
                        color = Color(0xFF94A3B8)
                    )
                }

                // Tab Switcher between Creator & Projects
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF1E2230))
                        .padding(3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (activeTab == 0) Color(0xFF00F5D4) else Color.Transparent)
                            .clickable { activeTab = 0 }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Creator",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (activeTab == 0) Color.Black else Color(0xFF94A3B8)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (activeTab == 1) Color(0xFF00F5D4) else Color.Transparent)
                            .clickable { activeTab = 1 }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Projects (${recentProjects.size})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (activeTab == 1) Color.Black else Color(0xFF94A3B8)
                        )
                    }
                }
            }

            HorizontalDivider(color = Color(0xFF1E293B), thickness = 0.8.dp)

            if (activeTab == 1) {
                // --- PROJECTS GALLERY TAB ---
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "My Created Apps & Websites",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Manage, test locally, or deploy your generated projects",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    items(recentProjects) { proj ->
                        ProjectCard(
                            project = proj,
                            onPreview = { onOpenBrowser(proj.localUrl) },
                            onDeploy = {
                                Toast.makeText(context, "Deploying ${proj.name} to Firebase...", Toast.LENGTH_SHORT).show()
                            },
                            onTerminal = onOpenTerminal
                        )
                    }
                }
            } else {
                // --- CREATOR TAB ---
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    // Prompt Input Card
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF141722),
                        border = BorderStroke(1.dp, Color(0xFF2E384D)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color(0xFF00F5D4),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Describe your App / Website",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Text(
                                    text = "Voice / Long Prompt",
                                    fontSize = 10.5.sp,
                                    color = Color(0xFF64748B)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 90.dp, max = 180.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF0A0C12))
                                    .padding(10.dp)
                            ) {
                                if (promptText.isEmpty()) {
                                    Text(
                                        text = "Apni bhasha me likho ya detail specs paste karo...\nExample: 'Ek dark-themed Notes app banao jisme audio notes aur search filter ho...'",
                                        color = Color(0xFF556075),
                                        fontSize = 12.5.sp
                                    )
                                }
                                BasicTextField(
                                    value = promptText,
                                    onValueChange = { promptText = it },
                                    textStyle = LocalTextStyle.current.copy(color = Color.White, fontSize = 13.sp),
                                    cursorBrush = SolidColor(Color(0xFF00F5D4)),
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Action buttons: Quick templates & Blueprint Button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = { promptText = "" },
                                    enabled = promptText.isNotEmpty(),
                                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF94A3B8))
                                ) {
                                    Text("Clear", fontSize = 11.5.sp)
                                }

                                Button(
                                    onClick = { showInterviewModal = true },
                                    enabled = promptText.isNotBlank(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF00F5D4),
                                        contentColor = Color.Black
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.RocketLaunch,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Create Blueprint & Interview",
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Quick Template Chips
                    Text(
                        text = "Or choose a quick starter template:",
                        fontSize = 11.5.sp,
                        color = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        quickTemplates.forEach { tpl ->
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color(0xFF1E2230),
                                border = BorderStroke(1.dp, Color(0xFF2E384D)),
                                modifier = Modifier.clickable {
                                    promptText = "Create a modern, full-featured $tpl with responsive UI, smooth animations, and clean modular code."
                                }
                            ) {
                                Text(
                                    text = tpl,
                                    fontSize = 11.sp,
                                    color = Color(0xFFCBD5E1),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Progress / Result Area
                    if (buildStatus == BuildStatus.BUILDING) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFF141722),
                            border = BorderStroke(1.dp, Color(0xFF00F5D4).copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = Color(0xFF00F5D4),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Kestrel Architect Building App...",
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                val steps = listOf(
                                    "Scaffolding project structure (Vite + React)",
                                    "Configuring TailwindCSS & UI components",
                                    "Writing business logic & database schemas",
                                    "Starting local dev server (http://localhost:5173)"
                                )

                                steps.forEachIndexed { idx, desc ->
                                    Row(
                                        modifier = Modifier.padding(vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (buildStep > idx) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = Color(0xFF00F5D4),
                                                modifier = Modifier.size(15.dp)
                                            )
                                        } else if (buildStep == idx) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFF00F5D4))
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFF334155))
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = desc,
                                            fontSize = 11.5.sp,
                                            color = if (buildStep >= idx) Color(0xFFE2E8F0) else Color(0xFF64748B)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (buildStatus == BuildStatus.COMPLETED) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFF141722),
                            border = BorderStroke(1.dp, Color(0xFF00F5D4)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color(0xFF00F5D4),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Project Successfully Built & Running!",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }

                                Text(
                                    text = "Local Dev Server active on http://localhost:5173",
                                    fontSize = 11.5.sp,
                                    color = Color(0xFF94A3B8),
                                    modifier = Modifier.padding(start = 28.dp, top = 2.dp)
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { onOpenBrowser("http://localhost:5173") },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F5D4), contentColor = Color.Black)
                                    ) {
                                        Icon(imageVector = Icons.Outlined.Language, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Live Preview", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            val deployUrl = "https://kestrel-app-shahid.web.app"
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            clipboard.setPrimaryClip(ClipData.newPlainText("Firebase URL", deployUrl))
                                            Toast.makeText(context, "Deploying to Firebase... URL copied!", Toast.LENGTH_LONG).show()
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, Color(0xFFFFA000)),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFFA000))
                                    ) {
                                        Icon(imageVector = Icons.Outlined.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Firebase Deploy", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedButton(
                                    onClick = {
                                        Toast.makeText(context, "Packaging APK: output saved in build/outputs/apk", Toast.LENGTH_LONG).show()
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, Color(0xFF3B82F6)),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF3B82F6))
                                ) {
                                    Icon(imageVector = Icons.Outlined.Android, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Export Android Package (APK)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- SPECIFICATION INTERVIEW MODAL POPUP ---
        if (showInterviewModal) {
            ModalBottomSheet(
                onDismissRequest = { showInterviewModal = false },
                containerColor = Color(0xFF141724),
                dragHandle = { BottomSheetDefaults.DragHandle(color = Color(0xFF475569)) }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "📋 Project Specification Interview",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "AI Architect needs these preferences before generating your project:",
                        fontSize = 11.5.sp,
                        color = Color(0xFF94A3B8)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 1. Target Platform
                    Text(text = "1. Build Target:", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("React + Vite (Web)", "Android APK", "Full Stack (Both)").forEach { target ->
                            val isSelected = targetPlatform == target
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) Color(0xFF00F5D4).copy(alpha = 0.2f) else Color(0xFF1E2230),
                                border = BorderStroke(1.dp, if (isSelected) Color(0xFF00F5D4) else Color(0xFF334155)),
                                modifier = Modifier.clickable { targetPlatform = target }
                            ) {
                                Text(
                                    text = target,
                                    fontSize = 11.5.sp,
                                    color = if (isSelected) Color(0xFF00F5D4) else Color(0xFFCBD5E1),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 2. Database & Storage
                    Text(text = "2. Database / Storage:", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Firebase Firestore", "Offline LocalStorage", "SQLite").forEach { db ->
                            val isSelected = databaseChoice == db
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) Color(0xFF00F5D4).copy(alpha = 0.2f) else Color(0xFF1E2230),
                                border = BorderStroke(1.dp, if (isSelected) Color(0xFF00F5D4) else Color(0xFF334155)),
                                modifier = Modifier.clickable { databaseChoice = db }
                            ) {
                                Text(
                                    text = db,
                                    fontSize = 11.5.sp,
                                    color = if (isSelected) Color(0xFF00F5D4) else Color(0xFFCBD5E1),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 3. UI Theme
                    Text(text = "3. Theme Style:", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Dark Obsidian", "Cyber Neon", "Clean Minimal").forEach { theme ->
                            val isSelected = themeStyle == theme
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) Color(0xFF00F5D4).copy(alpha = 0.2f) else Color(0xFF1E2230),
                                border = BorderStroke(1.dp, if (isSelected) Color(0xFF00F5D4) else Color(0xFF334155)),
                                modifier = Modifier.clickable { themeStyle = theme }
                            ) {
                                Text(
                                    text = theme,
                                    fontSize = 11.5.sp,
                                    color = if (isSelected) Color(0xFF00F5D4) else Color(0xFFCBD5E1),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 4. Feature Checkboxes
                    Text(text = "4. Features to include:", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Authentication (Login)", "Search & Filter", "Voice/Audio Notes", "Data Export").forEach { feat ->
                            val isChecked = selectedFeatures.contains(feat)
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isChecked) Color(0xFF3B82F6).copy(alpha = 0.2f) else Color(0xFF1E2230),
                                border = BorderStroke(1.dp, if (isChecked) Color(0xFF3B82F6) else Color(0xFF334155)),
                                modifier = Modifier.clickable {
                                    if (isChecked) selectedFeatures.remove(feat) else selectedFeatures.add(feat)
                                }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    if (isChecked) {
                                        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color(0xFF3B82F6), modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    Text(
                                        text = feat,
                                        fontSize = 11.5.sp,
                                        color = if (isChecked) Color.White else Color(0xFF94A3B8)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    // Confirm and Build Button
                    Button(
                        onClick = { startBuilding() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F5D4), contentColor = Color.Black)
                    ) {
                        Icon(imageVector = Icons.Outlined.RocketLaunch, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "🚀 Confirm Specifications & Build App",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
private fun ProjectCard(
    project: StudioProject,
    onPreview: () -> Unit,
    onDeploy: () -> Unit,
    onTerminal: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF141724),
        border = BorderStroke(1.dp, Color(0xFF2E384D)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = project.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF00F5D4).copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, Color(0xFF00F5D4).copy(alpha = 0.35f))
                ) {
                    Text(
                        text = project.status,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF00F5D4),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = project.description,
                fontSize = 12.sp,
                color = Color(0xFF94A3B8),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${project.type} • ${project.localUrl}",
                    fontSize = 10.5.sp,
                    color = Color(0xFF64748B)
                )
            }

            if (project.firebaseUrl != null) {
                Text(
                    text = "Live: ${project.firebaseUrl}",
                    fontSize = 10.5.sp,
                    color = Color(0xFFFFA000)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onPreview,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F5D4), contentColor = Color.Black),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    Text("Live Preview", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onDeploy,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFFFFA000)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFFA000)),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    Text("Deploy Firebase", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
