package ca.gbc.restaurantguide.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "restaurants")
data class Restaurant(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val address: String,
    val phone: String,
    val description: String,
    val tagsCsv: String,
    val rating: Int,
    val latitude: Double? = null,
    val longitude: Double? = null
) {
    fun tagsList(): List<String> =
        tagsCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }
}
