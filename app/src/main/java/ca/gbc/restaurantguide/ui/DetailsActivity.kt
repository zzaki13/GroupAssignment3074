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

        setSupportActionBar(binding.topBar)
        supportActionBar?.title = "Restaurant Details"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.topBar.setNavigationOnClickListener { finish() }

        val restaurantId = intent.getLongExtra("id", 0L)
        if (restaurantId == 0L) {
            Toast.makeText(this, "No restaurant selected", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

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

        binding.imgMap.setImageResource(R.drawable.ic_map_preview)

        loadStaticMapPreview(r)
    }

    private fun loadStaticMapPreview(r: Restaurant) {
        if (r.address.isBlank()) return

        // Google Maps Static API key
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
                // If anything fails just keep the vector icon
            }
        }.start()
    }

    private fun setupButtons() {
        binding.btnEdit.setOnClickListener {
            currentRestaurant?.let { r ->
                val intent = Intent(this, AddEditRestaurantActivity::class.java)
                intent.putExtra("id", r.id)
                startActivity(intent)
            }
        }

        binding.btnOpenMap.setOnClickListener { openInMaps() }
        binding.imgMap.setOnClickListener { openInMaps() }

        binding.btnShareFacebook.setOnClickListener {
            shareOnFacebook()
        }
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
            startActivity(Intent(Intent.ACTION_VIEW, geoUri))
        }
    }

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

        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, text)
        }

        try {
            startActivity(Intent.createChooser(emailIntent, "Send restaurant info via email"))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show()
        }
    }


    private fun shareOnFacebook() {
        val r = currentRestaurant
        if (r == null) {
            Toast.makeText(this, "Restaurant not loaded yet", Toast.LENGTH_SHORT).show()
            return
        }

        val text = buildString {
            appendLine("Check out ${r.name}!")
            appendLine("Address: ${r.address}")
            if (r.phone.isNotBlank()) appendLine("Phone: ${r.phone}")
            if (r.description.isNotBlank()) appendLine("Description: ${r.description}")
            val tags = r.tagsList().joinToString(", ")
            if (tags.isNotBlank()) appendLine("Tags: $tags")
            appendLine("Rating: ${r.rating} / 5")
        }

        val fbIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            setPackage("com.facebook.katana")
        }

        try {
            startActivity(fbIntent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(
                this,
                "Facebook app not found. Please install Facebook to share.",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
