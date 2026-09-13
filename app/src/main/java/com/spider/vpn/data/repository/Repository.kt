package com.spider.vpn.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.spider.vpn.data.db.AppDatabase
import com.spider.vpn.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "spider_vpn_prefs")

class Repository(private val context: Context) {
    private val db = AppDatabase.getInstance(context)
    private val sessionDao = db.sessionDao()
    private val subDao = db.subscriptionDao()
    private val configDao = db.configDao()
    private val chatDao = db.chatMessageDao()

    // Sessions
    val sessions: Flow<List<Session>> = sessionDao.getAll()
    suspend fun saveSession(session: Session) = sessionDao.insert(session)
    suspend fun deleteSession(session: Session) = sessionDao.delete(session)
    suspend fun deleteAllSessions() = sessionDao.deleteAll()

    // Subscriptions
    val subscriptions: Flow<List<Subscription>> = subDao.getAll()
    suspend fun saveSub(sub: Subscription) = subDao.insert(sub)
    suspend fun updateSub(sub: Subscription) = subDao.update(sub)
    suspend fun deleteSub(sub: Subscription) = subDao.delete(sub)
    suspend fun deleteAllSubs() = subDao.deleteAll()

    // Configs
    fun getConfigsBySub(subId: Long): Flow<List<Config>> = configDao.getBySubId(subId)
    val allConfigs: Flow<List<Config>> = configDao.getAll()
    suspend fun saveConfig(config: Config) = configDao.insert(config)
    suspend fun updateConfig(config: Config) = configDao.update(config)
    suspend fun deleteConfig(config: Config) = configDao.delete(config)
    suspend fun deleteConfigsBySub(subId: Long) = configDao.deleteBySubId(subId)
    suspend fun deleteAllConfigs() = configDao.deleteAll()

    // Chat
    val chatMessages: Flow<List<ChatMessage>> = chatDao.getAll()
    suspend fun saveChatMessage(message: ChatMessage) = chatDao.insert(message)
    suspend fun deleteAllChatMessages() = chatDao.deleteAll()

    // DataStore preferences
    companion object Keys {
        val RAILWAY_TOKEN = stringPreferencesKey("railway_token")
        val SPIDER_TOKEN = stringPreferencesKey("spider_token")
        val AI_API_URL = stringPreferencesKey("ai_api_url")
        val AI_API_KEY = stringPreferencesKey("ai_api_key")
        val AI_MODEL = stringPreferencesKey("ai_model")
        val ACTIVE_SESSION_ID = longPreferencesKey("active_session_id")
        val DNS_TYPE = stringPreferencesKey("dns_type")
        val CUSTOM_PRIMARY_DNS = stringPreferencesKey("custom_primary_dns")
        val CUSTOM_SECONDARY_DNS = stringPreferencesKey("custom_secondary_dns")
    }

    suspend fun <T> savePref(key: Preferences.Key<T>, value: T) {
        context.dataStore.edit { it[key] = value }
    }

    fun <T> getPref(key: Preferences.Key<T>, default: T): Flow<T> {
        return context.dataStore.data.map { it[key] ?: default }
    }

    suspend fun clearAllData() {
        sessionDao.deleteAll()
        subDao.deleteAll()
        configDao.deleteAll()
        chatDao.deleteAll()
        context.dataStore.edit { it.clear() }
    }
}
