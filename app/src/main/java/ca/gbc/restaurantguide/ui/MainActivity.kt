package ca.gbc.restaurantguide.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ca.gbc.restaurantguide.databinding.ActivityMainBinding
import ca.gbc.restaurantguide.data.Restaurant
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

        setSupportActionBar(binding.topAppBar)
        binding.topAppBar.setTitleTextColor(android.graphics.Color.WHITE)
        supportActionBar?.title = "Restaurant Guide"


        adapter = RestaurantAdapter(
            onClick = { item ->
                startActivity(Intent(this, DetailsActivity::class.java).apply {
                    // later: putExtra("id", item.id)
                })
            },
            onEdit = { item ->
                startActivity(Intent(this, AddEditRestaurantActivity::class.java).apply {
                    putExtra("id", item.id)
                })
            }
        )

        binding.recycler.layoutManager = LinearLayoutManager(this)
        binding.recycler.adapter = adapter

        vm.filtered.observe(this) { adapter.submitList(it) }

        (binding.searchView as SearchView).setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(q: String?) = true.also { vm.setQuery(q.orEmpty()) }
            override fun onQueryTextChange(new: String?) = true.also { vm.setQuery(new.orEmpty()) }
        })

        binding.btnAdd.setOnClickListener {
            startActivity(Intent(this, AddEditRestaurantActivity::class.java))
        }
        binding.fabAbout.setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }

        val helper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, t: RecyclerView.ViewHolder) = false
            override fun onSwiped(vh: RecyclerView.ViewHolder, dir: Int) {
                val item = adapter.getItemAt(vh.bindingAdapterPosition)
                lastDeleted = item
                vm.delete(item)
                Snackbar.make(binding.recycler, "Deleted ${item.name}", Snackbar.LENGTH_LONG)
                    .setAction("Undo") { lastDeleted?.let { vm.upsert(it) {} } }
                    .show()
            }
        })
        helper.attachToRecyclerView(binding.recycler)

    }
}
