package ca.gbc.restaurantguide.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ca.gbc.restaurantguide.data.Restaurant
import ca.gbc.restaurantguide.databinding.ActivityMainBinding
import ca.gbc.restaurantguide.viewmodel.RestaurantVMFactory
import ca.gbc.restaurantguide.viewmodel.RestaurantViewModel
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val vm: RestaurantViewModel by viewModels { RestaurantVMFactory(application) }

    private lateinit var adapter: RestaurantAdapter
    private var lastDeleted: Restaurant? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Toolbar title is already set in XML
        setSupportActionBar(binding.topAppBar)

        setupRecycler()
        setupSearch()
        setupButtons()
        setupSwipeToDelete()
    }

    private fun setupRecycler() {
        adapter = RestaurantAdapter(
            onClick = { restaurant ->
                // Open details screen for this restaurant
                val intent = Intent(this, DetailsActivity::class.java)
                intent.putExtra("id", restaurant.id)
                startActivity(intent)
            },
            onEdit = { restaurant ->
                // Open add/edit screen in edit mode
                val intent = Intent(this, AddEditRestaurantActivity::class.java)
                intent.putExtra("id", restaurant.id)
                startActivity(intent)
            }
        )

        binding.recycler.layoutManager = LinearLayoutManager(this)
        binding.recycler.adapter = adapter

        // Observe filtered list from ViewModel
        vm.filtered.observe(this) { list ->
            adapter.submitList(list)
        }
    }

    private fun setupSearch() {
        val searchView: SearchView = binding.searchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                vm.setQuery(query.orEmpty())
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                vm.setQuery(newText.orEmpty())
                return true
            }
        })
    }

    private fun setupButtons() {
        // "+" button to add a new restaurant
        binding.btnAdd.setOnClickListener {
            val intent = Intent(this, AddEditRestaurantActivity::class.java)
            startActivity(intent)
        }

        // "About" floating button
        binding.fabAbout.setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }
    }

    private fun setupSwipeToDelete() {
        val helper = ItemTouchHelper(object :
            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val item = adapter.getItemAt(viewHolder.bindingAdapterPosition)
                lastDeleted = item
                vm.delete(item)

                Snackbar.make(binding.recycler, "Deleted ${item.name}", Snackbar.LENGTH_LONG)
                    .setAction("Undo") {
                        lastDeleted?.let { deleted ->
                            vm.upsert(deleted) { /* nothing extra */ }
                        }
                    }
                    .show()
            }
        })

        helper.attachToRecyclerView(binding.recycler)
    }
}
