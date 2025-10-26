package ca.gbc.restaurantguide.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import ca.gbc.restaurantguide.databinding.ActivityAboutBinding

class AboutActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.topBar)
        binding.topBar.setNavigationOnClickListener { finish() }

        val versionName = packageManager.getPackageInfo(packageName, 0).versionName
        binding.tvVersion.text = "Version $versionName"

        binding.tvGitHub.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/zzaki13/GroupAssignment3074")))
        }
        binding.tvEmailZaki.setOnClickListener {
            startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:zaki.mohammed@georgebrown.ca")))
        }
        binding.tvEmailUzma.setOnClickListener {
            startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:uzma.shaikh@georgebrown.ca")))
        }

        binding.contentRoot.alpha = 0f
        binding.contentRoot.translationY = 24f
        binding.contentRoot.animate().alpha(1f).translationY(0f).setDuration(300).setInterpolator(AccelerateDecelerateInterpolator()).start()
    }
}
