package pl.ownvision.scorekeeper.db.converters

import androidx.room.TypeConverter
import org.joda.time.DateTime

class DateTimeConverter {
    @TypeConverter
    fun toDateTime(timestamp: Long): DateTime = DateTime(timestamp)

    @TypeConverter
    fun toTimestamp(dateTime: DateTime): Long = dateTime.millis
}