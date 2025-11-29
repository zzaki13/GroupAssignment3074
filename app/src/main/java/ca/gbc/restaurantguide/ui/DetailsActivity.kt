package ca.gbc.restaurantguide.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import ca.gbc.restaurantguide.R
import ca.gbc.restaurantguide.data.Restaurant
import ca.gbc.restaurantguide.databinding.ActivityDetailsBinding
import ca.gbc.restaurantguide.viewmodel.RestaurantVMFactory
import ca.gbc.restaurantguide.viewmodel.RestaurantViewModel
import java.net.HttpURLConnection
import java.net.URL

class DetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailsBinding
    private val vm: RestaurantViewModel by viewModels { RestaurantVMFactory(application) }

    private var currentRestaurant: Restaurant? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Toolbar
        setSupportActionBar(binding.topBar)
        supportActionBar?.title = "Restaurant Details"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.topBar.setNavigationOnClickListener { finish() }

        // Get ID from Intent
        val restaurantId = intent.getLongExtra("id", 0L)
        if (restaurantId == 0L) {
            Toast.makeText(this, "No restaurant selected", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Load restaurant from DB
        vm.load(restaurantId) { restaurant ->
            if (restaurant == null) {
                Toast.makeText(this, "Restaurant not found", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                currentRestaurant = restaurant
                bindRestaurant(restaurant)
            }
        }

        setupButtons()
    }

    private fun bindRestaurant(r: Restaurant) {
        binding.tvName.text = r.name
        binding.tvAddress.text = r.address
        binding.tvPhone.text = r.phone
        binding.tvDescription.text = r.description
        binding.tvTags.text = r.tagsList().joinToString(", ")
        binding.ratingBar.rating = r.rating.toFloat()

        // Default static icon so it's never empty
        binding.imgMap.setImageResource(R.drawable.ic_map_preview)

        // Try to load a real Google Static Map preview
        loadStaticMapPreview(r)
    }

    // --- Static Google Map preview in the rectangle ---
    private fun loadStaticMapPreview(r: Restaurant) {
        if (r.address.isBlank()) return

        // 🔑 Put YOUR key here
        val apiKey = "AIzaSyBW5B9E5U1mwY-vLQogmyM7UqZ3pwi-mIU"

        val encodedAddress = Uri.encode(r.address)
        val urlString =
            "https://maps.googleapis.com/maps/api/staticmap" +
                    "?center=$encodedAddress" +
                    "&zoom=15&size=600x300&scale=2" +
                    "&markers=color:red|$encodedAddress" +
                    "&key=$apiKey"

        Thread {
            try {
                val url = URL(urlString)
                val connection = url.openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()

                val input = connection.inputStream
                val bitmap = BitmapFactory.decodeStream(input)

                if (bitmap != null) {
                    runOnUiThread {
                        binding.imgMap.setImageBitmap(bitmap)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // If anything fails we just keep the vector icon
            }
        }.start()
    }

    private fun setupButtons() {
        // Edit button
        binding.btnEdit.setOnClickListener {
            currentRestaurant?.let { r ->
                val intent = Intent(this, AddEditRestaurantActivity::class.java)
                intent.putExtra("id", r.id)
                startActivity(intent)
            }
        }

        // Both the icon and the big image open Google Maps full screen
        binding.btnOpenMap.setOnClickListener { openInMaps() }
        binding.imgMap.setOnClickListener { openInMaps() }
    }

    private fun openInMaps() {
        val r = currentRestaurant
        if (r == null) {
            Toast.makeText(this, "Restaurant not loaded yet", Toast.LENGTH_SHORT).show()
            return
        }
        if (r.address.isBlank()) {
            Toast.makeText(this, "Address not available", Toast.LENGTH_SHORT).show()
            return
        }

        val encodedAddress = Uri.encode(r.address)
        val label = Uri.encode(r.name)
        val geoUri = Uri.parse("geo:0,0?q=$encodedAddress($label)")

        val mapsIntent = Intent(Intent.ACTION_VIEW, geoUri).apply {
            setPackage("com.google.android.apps.maps")
        }

        if (mapsIntent.resolveActivity(packageManager) != null) {
            startActivity(mapsIntent)
        } else {
            // Fallback to any maps-capable app or browser
            startActivity(Intent(Intent.ACTION_VIEW, geoUri))
        }
    }

    // ---- Toolbar menu (Share) ----

    override fun onCreateOptionsMenu(menu: android.view.Menu): Boolean {
        menuInflater.inflate(R.menu.menu_details, menu)
        return true
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_share -> {
                shareRestaurant()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // Generic share (email, messages, etc.)
    private fun shareRestaurant() {
        val r = currentRestaurant
        if (r == null) {
            Toast.makeText(this, "Restaurant not loaded yet", Toast.LENGTH_SHORT).show()
            return
        }

        val subject = "Check out ${r.name}"
        val text = buildString {
            appendLine("Restaurant: ${r.name}")
            appendLine("Address: ${r.address}")
            if (r.phone.isNotBlank()) appendLine("Phone: ${r.phone}")
            if (r.description.isNotBlank()) appendLine("Description: ${r.description}")
            val tags = r.tagsList().joinToString(", ")
            if (tags.isNotBlank()) appendLine("Tags: $tags")
            appendLine("Rating: ${r.rating} / 5")
        }

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, text)
        }

        try {
            startActivity(Intent.createChooser(sendIntent, "Share restaurant via"))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "No app found to share", Toast.LENGTH_SHORT).show()
        }
    }
}
