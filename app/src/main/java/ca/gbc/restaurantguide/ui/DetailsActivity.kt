package ca.gbc.restaurantguide.ui
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ca.gbc.restaurantguide.R
import ca.gbc.restaurantguide.databinding.ActivityAddEditBinding
import ca.gbc.restaurantguide.databinding.ActivityDetailsBinding

class DetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.topBar)
        supportActionBar?.title = "Restaurant Details"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.topBar.setNavigationOnClickListener { finish()}
    }
}