package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    fun getUserById(userId: Long): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserByIdSync(userId: Long): UserEntity?

    @Query("SELECT * FROM users WHERE mobileNumber = :mobile LIMIT 1")
    suspend fun getUserByMobile(mobile: String): UserEntity?

    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    suspend fun getAllUsersSync(): List<UserEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET isSuspended = :isSuspended WHERE id = :userId")
    suspend fun updateSuspensionStatus(userId: Long, isSuspended: Boolean)

    @Query("UPDATE users SET availableBalance = availableBalance + :balanceDelta, totalEarnings = totalEarnings + :earningsDelta, todayEarnings = todayEarnings + :todayEarningsDelta WHERE id = :userId")
    suspend fun adjustUserBalance(userId: Long, balanceDelta: Double, earningsDelta: Double, todayEarningsDelta: Double)

    @Query("UPDATE users SET depositStatus = :status, approvedDepositAmount = :amount WHERE id = :userId")
    suspend fun updateDepositStatus(userId: Long, status: String, amount: Double)

    @Query("SELECT COUNT(*) FROM users")
    fun getUserCount(): Flow<Int>
}
