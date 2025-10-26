package ca.gbc.restaurantguide.repository

import ca.gbc.restaurantguide.data.AppDatabase
import ca.gbc.restaurantguide.data.Restaurant

class RestaurantRepository(private val db: AppDatabase) {
    val all = db.restaurantDao().observeAll()

    suspend fun upsert(r: Restaurant): Long {
        return if (r.id == 0L) db.restaurantDao().insert(r)
        else { db.restaurantDao().update(r); r.id }
    }

    suspend fun delete(r: Restaurant) = db.restaurantDao().delete(r)
    suspend fun get(id: Long) = db.restaurantDao().getById(id)
}
