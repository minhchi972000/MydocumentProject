package wee.digital.sample.data.db

import androidx.room.*
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import wee.digital.library.extension.toJsonArray
import wee.digital.library.extension.toJsonObject
import java.nio.ByteBuffer

@Entity(tableName = "caches")
class CacheDBO {

    @PrimaryKey
    @ColumnInfo(name = "cache_key")
    var key: String

    @ColumnInfo(name = "cache_user")
    var user: String?

    @ColumnInfo(name = "cache_value", typeAffinity = ColumnInfo.BLOB)
    var value: ByteArray? = null

    @Dao
    interface DAO : BaseDAO<CacheDBO> {

        @Query("SELECT * FROM caches")
        fun all(): List<CacheDBO>

        @Query("SELECT * FROM caches WHERE cache_user = :user AND cache_key = :key")
        fun get(user: String?, key: String): CacheDBO

        @Query("SELECT cache_value FROM caches WHERE cache_user = :user AND cache_key = :key")
        fun getValue(user: String?, key: String): ByteArray?
    }

    constructor(user: String?, key: String) {
        this.key = key
        this.user = user
    }

    companion object {

        fun byteArray(user: String?, key: String): ByteArray? {
            val value = roomDB.cache.getValue(user, key) ?: return null
            if (value.isEmpty()) return null
            return value
        }

        fun save(user: String?, key: String, value: Any?) {
            val dbo = CacheDBO(user, key)
            if (value != null) when (value) {
                is String -> {
                    dbo.value = value.toByteArray(Charsets.UTF_8)
                }
                is Long -> {
                    dbo.value = ByteBuffer.allocate(Long.SIZE_BYTES).putLong(value).array()
                }
                is Int -> {
                    dbo.value = ByteBuffer.allocate(Long.SIZE_BYTES).putLong(value.toLong()).array()
                }
                is Double -> {
                    dbo.value = ByteBuffer.allocate(Double.SIZE_BYTES).putDouble(value).array()
                }
                is Float -> {
                    dbo.value =
                        ByteBuffer.allocate(Double.SIZE_BYTES).putDouble(value.toDouble()).array()
                }
                is JsonElement -> {
                    dbo.value = value.toString().toByteArray(Charsets.UTF_8)
                }
            }
        }

        fun string(user: String?, key: String): String? {
            val value = byteArray(user, key) ?: return null
            return String(value, Charsets.UTF_8)
        }

        fun long(user: String?, key: String): Long {
            val value = byteArray(user, key) ?: return -1
            val buffer = ByteBuffer.allocate(Long.SIZE_BYTES).put(value)
            buffer.flip()
            return buffer.long
        }

        fun double(user: String?, key: String): Double {
            val value = byteArray(user, key) ?: return -1.0
            val buffer = ByteBuffer.allocate(Double.SIZE_BYTES).put(value)
            buffer.flip()
            return buffer.double
        }

        fun jsonObject(user: String?, key: String): JsonObject? {
            val value = byteArray(user, key) ?: return null
            return String(value, Charsets.UTF_8).toJsonObject()
        }

        fun jsonArray(user: String?, key: String): JsonArray? {
            val value = byteArray(user, key) ?: return null
            return String(value, Charsets.UTF_8).toJsonArray()
        }

    }

}