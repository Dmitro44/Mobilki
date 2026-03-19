package com.example.timer.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.example.timer.R
import com.example.timer.data.local.preferences.FontSize
import com.example.timer.data.local.preferences.Language
import com.example.timer.ui.viewmodel.SettingsViewModel

/**
 * Settings screen for user preferences
 * 
 * Features:
 * - Dark theme toggle
 * - Font size selection (Small, Medium, Large, Extra Large)
 * - Language selection (English, Ukrainian)
 * - Reset to defaults button
 * 
 * @param viewModel ViewModel managing settings state
 * @param onNavigateBack Callback when user navigates back
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? com.example.timer.MainActivity
    val preferences by viewModel.userPreferences.collectAsStateWithLifecycle()
    var showResetDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = stringResource(R.string.settings),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Theme Section
            SettingsSection(
                title = stringResource(R.string.appearance),
                icon = Icons.Default.DarkMode
            ) {
                ThemeToggle(
                    isDarkTheme = preferences.isDarkTheme,
                    onToggle = viewModel::toggleDarkTheme
                )
            }
            
            Divider()
            
            // Font Size Section
            SettingsSection(
                title = stringResource(R.string.font_size),
                icon = Icons.Default.TextFields
            ) {
                FontSizeSelector(
                    selectedFontSize = preferences.fontSize,
                    onFontSizeSelected = viewModel::setFontSize
                )
            }
            
            Divider()
            
            // Language Section
            SettingsSection(
                title = stringResource(R.string.language),
                icon = Icons.Default.Language
            ) {
                LanguageSelector(
                    selectedLanguage = preferences.language,
                    onLanguageSelected = { language -> 
                        viewModel.setLanguage(context, language) {
                            activity?.recreateWithAnimation()
                        }
                    }
                )
            }
            
            Divider()
            
            // Reset Section
            OutlinedButton(
                onClick = { showResetDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.reset_to_defaults))
            }
        }
    }
    
    // Reset confirmation dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text(stringResource(R.string.reset_settings)) },
            text = { Text(stringResource(R.string.reset_confirmation)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetToDefaults()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.reset))
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

/**
 * Section header with icon
 */
@Composable
private fun SettingsSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        content()
    }
}

/**
 * Dark theme toggle switch
 */
@Composable
private fun ThemeToggle(
    isDarkTheme: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = stringResource(R.string.dark_theme),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = if (isDarkTheme) stringResource(R.string.dark_mode_enabled) else stringResource(R.string.light_mode_enabled),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Switch(
                checked = isDarkTheme,
                onCheckedChange = onToggle
            )
        }
    }
}

/**
 * Font size radio button selector
 */
@Composable
private fun FontSizeSelector(
    selectedFontSize: FontSize,
    onFontSizeSelected: (FontSize) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .selectableGroup(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FontSize.entries.forEach { fontSize ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = fontSize == selectedFontSize,
                        onClick = { onFontSizeSelected(fontSize) },
                        role = Role.RadioButton
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = if (fontSize == selectedFontSize) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = when (fontSize) {
                                FontSize.SMALL -> stringResource(R.string.font_size_small)
                                FontSize.MEDIUM -> stringResource(R.string.font_size_medium)
                                FontSize.LARGE -> stringResource(R.string.font_size_large)
                                FontSize.EXTRA_LARGE -> stringResource(R.string.font_size_extra_large)
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = stringResource(R.string.font_scale_label, (fontSize.scale * 100).toInt()),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    RadioButton(
                        selected = fontSize == selectedFontSize,
                        onClick = null // Handled by Card selectable
                    )
                }
            }
        }
    }
}

/**
 * Language radio button selector
 */
@Composable
private fun LanguageSelector(
    selectedLanguage: Language,
    onLanguageSelected: (Language) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .selectableGroup(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Language.entries.forEach { language ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = language == selectedLanguage,
                        onClick = { onLanguageSelected(language) },
                        role = Role.RadioButton
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = if (language == selectedLanguage) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = language.displayName,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = language.code,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    RadioButton(
                        selected = language == selectedLanguage,
                        onClick = null // Handled by Card selectable
                    )
                }
            }
        }
    }
}
