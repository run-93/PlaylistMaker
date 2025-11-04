package com.practicum.playlistmaker.settings.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.practicum.playlistmaker.databinding.ActivitySettingsBinding
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.common.App
import com.practicum.playlistmaker.main.ui.MainActivity

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonBack.setNavigationOnClickListener{
            val backIntent = Intent(this, MainActivity::class.java)
            backIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(backIntent)
            finish()
        }

        binding.toShare.setOnClickListener{
            toShareApp()
        }

        binding.writeSupport.setOnClickListener{
            sendSupport()
        }

        binding.userAgreement.setOnClickListener{
            writeUserAgreement()
        }

        val app = application as App
        val isDarkTheme = app.darkTheme

        binding.themeSwitcher.isChecked = isDarkTheme

        binding.themeSwitcher.setOnCheckedChangeListener { switcher, checked ->
            (applicationContext as App).switchTheme(checked)
        }
    }

    private fun toShareApp() {
        val toShareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            val shareMessage = getString(R.string.course_android_developer)
            putExtra(Intent.EXTRA_TEXT, shareMessage)
        }
        startActivity(Intent.createChooser(toShareIntent, " "))
    }

    private fun sendSupport() {
        val messageTheme = getString(R.string.message_theme)
        val messageSupport = getString(R.string.message_support)
        val addressSupport = getString(R.string.email_support)
        val writeSupportIntent = Intent(Intent.ACTION_SENDTO)
        writeSupportIntent.data = Uri.parse("mailto:")
        writeSupportIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(addressSupport))
        writeSupportIntent.putExtra(Intent.EXTRA_SUBJECT, messageTheme)
        writeSupportIntent.putExtra(Intent.EXTRA_TEXT, messageSupport)
        startActivity(writeSupportIntent)
    }

    private fun writeUserAgreement(){
        val url = getString(R.string.url_user_agreement)
        val urlIntent = Intent(Intent.ACTION_VIEW)
        urlIntent.data = Uri.parse(url)
        startActivity(urlIntent)
    }
}