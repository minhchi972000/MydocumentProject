package wee.digital.sample.data.db

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import wee.digital.sample.BuildConfig
import wee.digital.sample.app

@Database(
    entities = [CacheDBO::class, SampleDBO::class],
    version = 1,
    exportSchema = false
)
abstract class RoomDB : RoomDatabase() {
    abstract val cache: CacheDBO.DAO
    abstract val sample: SampleDBO.DAO
}

val roomDB by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
    Room.databaseBuilder(app.applicationContext, RoomDB::class.java, BuildConfig.APPLICATION_ID)
        .allowMainThreadQueries()
        .fallbackToDestructiveMigration()
        .build()
}