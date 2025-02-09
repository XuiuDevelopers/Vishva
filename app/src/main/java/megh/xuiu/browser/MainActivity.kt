package megh.xuiu.browser

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import megh.xuiu.browser.ui.theme.VishvaTheme

// DataStore setup
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
var darkMode by mutableStateOf(false)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VishvaTheme (
                darkTheme = darkMode
            ){
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SettingsScreen()
                }
            }
        }
    }
}

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val dataStore = context.dataStore
    val coroutineScope = rememberCoroutineScope()

    // Settings state
    var isNotificationsEnabled by remember { mutableStateOf(false) }
    var stringData by remember { mutableStateOf("") }

    // Load initial settings from DataStore
    LaunchedEffect(Unit) {
        launch(Dispatchers.IO) {
            darkMode = dataStore.getSetting(DARK_MODE_KEY, false)
            isNotificationsEnabled = dataStore.getSetting(NOTIFICATIONS_KEY, false)
            stringData = dataStore.getSetting(STRING_KEY, "")
        }
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            SettingSwitch(
                text = "Dark Mode",
                isChecked = darkMode,
                onCheckedChange = { newValue ->
                    darkMode = newValue
                    coroutineScope.launch(Dispatchers.IO) {
                        dataStore.saveSetting(DARK_MODE_KEY, newValue)
                    }
                }
            )

            SettingSwitch(
                text = "Notifications",
                isChecked = isNotificationsEnabled,
                onCheckedChange = { newValue ->
                    isNotificationsEnabled = newValue
                    coroutineScope.launch(Dispatchers.IO) {
                        dataStore.saveSetting(NOTIFICATIONS_KEY, newValue)
                    }
                }
            )
            TextField(
                value = stringData,
                onValueChange = {
                    stringData = it
                    coroutineScope.launch(Dispatchers.IO) {
                        dataStore.saveSetting(STRING_KEY, it)
                    }
                }
            )
            Text(text = "String Data: $stringData")
        }
    }
}

@Composable
fun SettingSwitch(text: String, isChecked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = text, modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.width(16.dp))
        Switch(checked = isChecked, onCheckedChange = onCheckedChange)
    }
}

// DataStore helper functions
suspend fun <T> DataStore<Preferences>.saveSetting(key: Preferences.Key<T>, value: T) {
    edit { preferences ->
        preferences[key] = value
    }
}


suspend fun <T> DataStore<Preferences>.getSetting(key: Preferences.Key<T>, defaultValue: T): T {
    return data.map { preferences ->
        preferences[key] ?: defaultValue
    }.firstOrNull() ?: defaultValue
}

// DataStore keys
val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
val NOTIFICATIONS_KEY = booleanPreferencesKey("notifications")
val STRING_KEY = stringPreferencesKey("name")