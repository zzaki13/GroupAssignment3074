package ca.gbc.restaurantguide.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface RestaurantDao {
    @Query("SELECT * FROM restaurants ORDER BY name COLLATE NOCASE ASC")
    fun observeAll(): LiveData<List<Restaurant>>

    @Query("SELECT * FROM restaurants WHERE id = :id")
    suspend fun getById(id: Long): Restaurant?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: Restaurant): Long

    @Update
    suspend fun update(item: Restaurant)

    @Delete
    suspend fun delete(item: Restaurant)
}
