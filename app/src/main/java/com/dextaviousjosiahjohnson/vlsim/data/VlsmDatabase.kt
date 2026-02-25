package com.dextaviousjosiahjohnson.vlsim.data

import android.content.Context
import androidx.room.*
import com.dextaviousjosiahjohnson.vlsim.math.DeviceData
import com.dextaviousjosiahjohnson.vlsim.math.SwitchData
import com.dextaviousjosiahjohnson.vlsim.math.WanData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow

class VlsmConverters {
    private val gson = Gson()

    @TypeConverter
    fun fromStringList(value: List<String>): String = gson.toJson(value)
    @TypeConverter
    fun toStringList(value: String): List<String> = gson.fromJson(value, object : TypeToken<List<String>>() {}.type) ?: emptyList()

    @TypeConverter
    fun fromSwitchList(value: List<SwitchData>): String = gson.toJson(value)
    @TypeConverter
    fun toSwitchList(value: String): List<SwitchData> = gson.fromJson(value, object : TypeToken<List<SwitchData>>() {}.type) ?: emptyList()

    @TypeConverter
    fun fromDeviceList(value: List<DeviceData>): String = gson.toJson(value)
    @TypeConverter
    fun toDeviceList(value: String): List<DeviceData> = gson.fromJson(value, object : TypeToken<List<DeviceData>>() {}.type) ?: emptyList()

    @TypeConverter
    fun fromWanList(value: List<WanData>): String = gson.toJson(value)
    @TypeConverter
    fun toWanList(value: String): List<WanData> = gson.fromJson(value, object : TypeToken<List<WanData>>() {}.type) ?: emptyList()
}

@Entity(tableName = "saved_calculations")
data class SavedCalculation(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val saveName: String,
    val baseNetwork: String,
    val routers: List<String>,
    val switches: List<SwitchData>,
    val devices: List<DeviceData>,
    val wans: List<WanData>,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface CalculationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalculation(calc: SavedCalculation)

    @Query("SELECT * FROM saved_calculations ORDER BY timestamp DESC")
    fun getAllCalculations(): Flow<List<SavedCalculation>>

    @Delete
    suspend fun deleteCalculation(calc: SavedCalculation)
}

@Database(entities = [SavedCalculation::class], version = 3, exportSchema = false)
@TypeConverters(VlsmConverters::class)
abstract class VlsmDatabase : RoomDatabase() {
    abstract fun calculationDao(): CalculationDao

    companion object {
        @Volatile
        private var INSTANCE: VlsmDatabase? = null

        fun getDatabase(context: Context): VlsmDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VlsmDatabase::class.java,
                    "vlsm-database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}