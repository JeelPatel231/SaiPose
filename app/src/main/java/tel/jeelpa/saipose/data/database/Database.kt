package tel.jeelpa.saipose.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Plugin::class], version = 1)
@TypeConverters(PluginTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun pluginDao(): PluginDao
}
