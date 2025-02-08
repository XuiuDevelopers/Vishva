package megh.xuiu.browser

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import megh.xuiu.browser.ui.theme.VishvaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VishvaTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DatabaseScreen()
                }
            }
        }
    }
}

@Composable
fun DatabaseScreen() {
    val db = Room.databaseBuilder(
        LocalContext.current,
        AppDatabase::class.java, "database-name"
    ).build()

    val userDao = db.userDao()
    var users by remember { mutableStateOf<List<User>>(emptyList()) }
    var surname by remember { mutableStateOf<List<User>>(emptyList()) }

    LaunchedEffect(Unit) {
        launch(Dispatchers.IO) {
            try {
                // Example: Insert some users if the database is empty
                if (userDao.getAll().isEmpty()) {
                    userDao.insertAll(
                        User(firstName = "Nishank", lastName = "Badoniya"),
                        User(firstName = "Megh", lastName = "Badoniya")
                    )
                }
                val allUsers = userDao.getAll()
                withContext(Dispatchers.Main) {
                    users = allUsers
                }
            } catch (e: Exception) {
                Log.e("DatabaseError", "Error accessing database", e)
            }
        }
    }

    Scaffold { innerPadding ->
        Text(
            text = if (users.isEmpty()) "Loading..." else "Users: ${users.joinToString { it.firstName ?: "" }}",
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Entity
data class User(
    @PrimaryKey(autoGenerate = true) val uid: Int? = null,
    @ColumnInfo(name = "first_name") val firstName: String?,
    @ColumnInfo(name = "last_name") val lastName: String?
)

@Dao
interface UserDao {
    @Query("SELECT * FROM user")
    fun getAll(): List<User>

    @Query("SELECT * FROM user WHERE uid IN (:userIds)")
    fun loadAllByIds(userIds: IntArray): List<User>

    @Query("SELECT * FROM user WHERE first_name LIKE :first AND last_name LIKE :last LIMIT 1")
    fun findByName(first: String, last: String): User

    @Insert
    fun insertAll(vararg users: User)

    @Delete
    fun delete(user: User)
}

@Database(entities = [User::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    VishvaTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            DatabaseScreen()
        }
    }
}