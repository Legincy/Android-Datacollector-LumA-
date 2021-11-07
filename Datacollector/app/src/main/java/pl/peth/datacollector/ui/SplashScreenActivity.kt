package pl.peth.datacollector.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import pl.peth.datacollector.R
import pl.peth.datacollector.databinding.SplashScreenActivityBinding

class SplashScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen_activity)

        val binding = DataBindingUtil.setContentView<SplashScreenActivityBinding>(
            this, R.layout.splash_screen_activity
        ).apply {
            this.lifecycleOwner
        }
        val mainActivity = MainActivity.buildIntent(this)

        binding.logo.animate().apply {
            duration = 750
            alpha(0.5f)
            scaleXBy(0.05f)
        }.withEndAction {
            binding.logo.animate().apply {
                duration = 750
                alpha(1f)
                scaleXBy(-0.05f)
            }.withEndAction {
                startActivity(mainActivity)
                finish()
            }
        }
    }
}