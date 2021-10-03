package jp.sugnakys.usbserialconsole.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase

@Database(entities = [LogItem::class], version = 1, exportSchema = false)
abstract class LogItemDatabase : RoomDatabase() {
    abstract fun getDao(): LogItemDao
}

@Entity(tableName = "LogItem", primaryKeys = ["timestamp"])
data class LogItem(
    val timestamp: Long,
    val text: String
)

@Dao
interface LogItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(logItem: LogItem)

    @Query("SELECT * FROM LogItem order by 'timestamp' ASC")
    fun getAllItems(): LiveData<List<LogItem>>

    @Query("DELETE FROM LogItem")
    fun deleteAll()
}