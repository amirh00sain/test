package com.spider.vpn.data.db

import androidx.room.*
import com.spider.vpn.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Query("SELECT * FROM sessions ORDER BY createdAt DESC")
    fun getAll(): Flow<List<Session>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: Session): Long

    @Update
    suspend fun update(session: Session)

    @Delete
    suspend fun delete(session: Session)

    @Query("DELETE FROM sessions")
    suspend fun deleteAll()
}

@Dao
interface SubscriptionDao {
    @Query("SELECT * FROM subscriptions ORDER BY createdAt DESC")
    fun getAll(): Flow<List<Subscription>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(sub: Subscription): Long

    @Update
    suspend fun update(sub: Subscription)

    @Delete
    suspend fun delete(sub: Subscription)

    @Query("DELETE FROM subscriptions")
    suspend fun deleteAll()
}

@Dao
interface ConfigDao {
    @Query("SELECT * FROM configs WHERE subId = :subId")
    fun getBySubId(subId: Long): Flow<List<Config>>

    @Query("SELECT * FROM configs")
    fun getAll(): Flow<List<Config>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(config: Config): Long

    @Update
    suspend fun update(config: Config)

    @Delete
    suspend fun delete(config: Config)

    @Query("DELETE FROM configs WHERE subId = :subId")
    suspend fun deleteBySubId(subId: Long)

    @Query("DELETE FROM configs")
    suspend fun deleteAll()
}

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAll(): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: ChatMessage): Long

    @Query("DELETE FROM chat_messages")
    suspend fun deleteAll()
}
