package wee.digital.sample.data.db

import androidx.room.*

@Entity(tableName = "samples")
class SampleDBO {

    /**
     * Only once constructor if define @PrimaryKey(autoGenerate = true)
     * And default value must be type
     * @PrimaryKey(autoGenerate = true)
     * @ColumnInfo(name = User.ID)
     * var id: Int? = null
     */
    @PrimaryKey
    @ColumnInfo(name = "sample_id")
    var id: Int? = null //  id auto increments

    @ColumnInfo(name = "sample_image", typeAffinity = ColumnInfo.BLOB)
    var image: ByteArray? = null

    @ColumnInfo(name = "sample_key")
    lateinit var key: String

    @ColumnInfo(name = "sample_value")
    var value: String? = null

    @Dao
    interface DAO : BaseDAO<SampleDBO> {

        @Query("SELECT * FROM samples")
        fun all(): List<SampleDBO>

        @Query("SELECT * FROM samples WHERE sample_key = :key")
        fun get(key: String): SampleDBO
    }
}