package ca.gbc.restaurantguide.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import ca.gbc.restaurantguide.data.Restaurant
import ca.gbc.restaurantguide.databinding.ActivityAddEditBinding
import ca.gbc.restaurantguide.viewmodel.RestaurantVMFactory
import ca.gbc.restaurantguide.viewmodel.RestaurantViewModel

class AddEditRestaurantActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditBinding
    private val vm: RestaurantViewModel by viewModels { RestaurantVMFactory(application) }

    private var editingId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.topBar)
        binding.topBar.setTitleTextColor(android.graphics.Color.WHITE)
        supportActionBar?.title = "Add / Edit Restaurant"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.topBar.setNavigationOnClickListener { finish() }

        // Are we editing an existing item?
        val idExtra = intent.getLongExtra("id", 0L)
        editingId = if (idExtra > 0) idExtra else null

        if (editingId != null) {
            title = "Edit Restaurant"
            // Load and prefill fields
            vm.load(editingId!!) { r ->
                if (r != null) {
                    binding.etName.setText(r.name)
                    binding.etAddress.setText(r.address)
                    binding.etPhone.setText(r.phone)
                    binding.etDescription.setText(r.description)
                    binding.etTags.setText(r.tagsCsv)
                    binding.rbRating.rating = r.rating.toFloat()
                }
            }
        } else {
            title = "Add Restaurant"
        }

        binding.btnCancel.setOnClickListener { finish() }
        binding.btnSave.setOnClickListener {
            val name = binding.etName.text?.toString().orEmpty().trim()
            val address = binding.etAddress.text?.toString().orEmpty().trim()
            val phone = binding.etPhone.text?.toString().orEmpty().trim()
            val desc = binding.etDescription.text?.toString().orEmpty().trim()
            val tags = binding.etTags.text?.toString().orEmpty().trim()
            val rating = binding.rbRating.rating.toInt().coerceIn(1, 5)

            if (name.isEmpty() || address.isEmpty()) {
                Toast.makeText(this, "Name and Address are required.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // If editing, keep the same id to trigger update
            val item = Restaurant(
                id = editingId ?: 0L,
                name = name,
                address = address,
                phone = phone,
                description = desc,
                tagsCsv = tags,
                rating = rating
            )

            vm.upsert(item) {
                Toast.makeText(this, if (editingId != null) "Updated!" else "Saved!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}
