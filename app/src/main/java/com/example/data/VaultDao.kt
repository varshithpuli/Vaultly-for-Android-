package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface VaultDao {
    @Query("SELECT * FROM vault_cards WHERE isDeleted = 0 ORDER BY isFavourite DESC, createdAt DESC")
    fun getActiveCards(): Flow<List<VaultCardEntity>>

    @Query("SELECT * FROM vault_cards WHERE isDeleted = 1 ORDER BY deletedAt DESC")
    fun getDeletedCards(): Flow<List<VaultCardEntity>>

    @Query("SELECT COUNT(*) FROM vault_cards")
    suspend fun getCardCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: VaultCardEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCards(cards: List<VaultCardEntity>)

    @Update
    suspend fun updateCard(card: VaultCardEntity)

    @Query("DELETE FROM vault_cards WHERE id = :id")
    suspend fun deleteCardPermanently(id: Long)

    @Query("SELECT * FROM vault_items WHERE isDeleted = 0 ORDER BY createdAt DESC")
    fun getActiveVaultItems(): Flow<List<VaultItemEntity>>

    @Query("SELECT * FROM vault_items WHERE isDeleted = 1 ORDER BY deletedAt DESC")
    fun getDeletedVaultItems(): Flow<List<VaultItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVaultItem(item: VaultItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVaultItems(items: List<VaultItemEntity>)

    @Update
    suspend fun updateVaultItem(item: VaultItemEntity)

    @Query("DELETE FROM vault_items WHERE id = :id")
    suspend fun deleteVaultItemPermanently(id: Long)

    @Query("SELECT * FROM spend_transactions ORDER BY timestamp DESC")
    fun getAllSpends(): Flow<List<SpendTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpend(spend: SpendTransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpends(spends: List<SpendTransactionEntity>)

    @Query("DELETE FROM spend_transactions WHERE id = :id")
    suspend fun deleteSpend(id: Long)

    @Query("DELETE FROM vault_cards WHERE isDeleted = 1")
    suspend fun emptyDeletedCards()

    @Query("DELETE FROM vault_items WHERE isDeleted = 1")
    suspend fun emptyDeletedVaultItems()
}
