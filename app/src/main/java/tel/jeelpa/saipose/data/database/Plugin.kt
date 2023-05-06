package tel.jeelpa.saipose.data.database

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.TypeConverter
import kotlinx.coroutines.flow.Flow

enum class PluginType {
    PARSER,
    EXTRACTOR
}

@Entity
data class Plugin(
    @PrimaryKey
    @ColumnInfo(name = "service_name")
    val serviceName: String,

    @ColumnInfo(name = "jar_name")
    val jarName: String,

    @ColumnInfo(name = "class_name")
    val className: String,

    @ColumnInfo(name = "plugin_type")
    val type: PluginType
)

class PluginTypeConverter {

    @TypeConverter
    fun fromPluginType(type: PluginType): String {
        return type.name
    }

    @TypeConverter
    fun toPriority(type: String): PluginType {
        return PluginType.valueOf(type)
    }

}

@Dao
interface PluginDao {
    @Query("SELECT * FROM plugin")
    fun getAll(): Flow<List<Plugin>>

    @Query("SELECT * FROM plugin WHERE plugin_type = :type")
    suspend fun getPluginByType(type: PluginType): List<Plugin>

    @Query("SELECT * FROM plugin WHERE service_name = :serviceName")
    suspend fun findByName(serviceName: String): Plugin

    @Insert
    suspend fun insertAll(vararg plugins: Plugin)

    @Delete
    suspend fun delete(plugin: Plugin)
}
