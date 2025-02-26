package com.example.playlistmaker

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import android.content.res.Configuration
import android.net.Uri
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Back button
        val backButton = findViewById<ImageView>(R.id.settings_back_button)
        backButton.setOnClickListener {
            finish()
        }

        // Theme switch
        val themeSwitch = findViewById<SwitchCompat>(R.id.settings_theme_switch)
        themeCheck(themeSwitch)
        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        // Share app
        val shareApp = findViewById<TextView>(R.id.settings_share)
        shareApp.setOnClickListener {
            val shareText = getString(R.string.settings_share_text)
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, shareText)
                type = "text/plain"
            }
            startActivity(Intent.createChooser(shareIntent, getString(R.string.settings_share)))
        }

        // Support
        val support = findViewById<TextView>(R.id.settings_support)
        support.setOnClickListener {
            val email = getString(R.string.settings_support_email)
            val subject = getString(R.string.settings_support_subject)
            val body = getString(R.string.settings_support_text)

            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, body)
            }

            val chooserIntent = Intent.createChooser(emailIntent,
                getString(R.string.settings_support_choose_email_service))
            if (chooserIntent.resolveActivity(packageManager) != null) {
                startActivity(chooserIntent)
            }
            else {
                Toast.makeText(this,
                    getString(R.string.settings_support_email_service_error),
                    Toast.LENGTH_SHORT).show()
            }
        }

        // User agreement
        val userAgreement = findViewById<TextView>(R.id.settings_user_agreement)
        userAgreement.setOnClickListener {
            val url = getString(R.string.settings_user_agreement_url)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(url)
            }
            startActivity(intent)
        }

    }
    private fun themeCheck(switch: SwitchCompat) {
            val currentTheme = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            switch.isChecked = (currentTheme == Configuration.UI_MODE_NIGHT_YES)
    }
}