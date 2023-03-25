package com.demo.sensor

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserActivityLogDao {
    @Query("SELECT * FROM useractivitylogs ORDER BY useractivitylogs.id DESC")
    fun getAll(): List<UserActivityLog>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(userActivityLog: UserActivityLog)
}