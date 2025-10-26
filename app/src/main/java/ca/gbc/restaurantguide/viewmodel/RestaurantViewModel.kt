package ca.gbc.restaurantguide.viewmodel

import android.app.Application
import androidx.lifecycle.*
import ca.gbc.restaurantguide.data.AppDatabase
import ca.gbc.restaurantguide.data.Restaurant
import ca.gbc.restaurantguide.repository.RestaurantRepository
import kotlinx.coroutines.launch

class RestaurantViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = RestaurantRepository(AppDatabase.get(app))
    val restaurants = repo.all

    private val _query = MutableLiveData("")
    val query: LiveData<String> = _query

    val filtered: LiveData<List<Restaurant>> = MediatorLiveData<List<Restaurant>>().apply {
        fun recompute() {
            val q = _query.value.orEmpty().trim().lowercase()
            val list = restaurants.value.orEmpty()
            value = if (q.isEmpty()) list else list.filter { r ->
                r.name.lowercase().contains(q) || r.tagsCsv.lowercase().contains(q)
            }
        }
        addSource(restaurants) { recompute() }
        addSource(_query) { recompute() }
    }

    fun setQuery(q: String) { _query.value = q }

    fun upsert(r: Restaurant, onDone: (Long) -> Unit = {}) = viewModelScope.launch {
        val id = repo.upsert(r); onDone(id)
    }

    fun delete(r: Restaurant) = viewModelScope.launch { repo.delete(r) }

    fun load(id: Long, onLoaded: (Restaurant?) -> Unit) = viewModelScope.launch {
        onLoaded(repo.get(id))
    }

}

class RestaurantVMFactory(private val app: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RestaurantViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RestaurantViewModel(app) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
