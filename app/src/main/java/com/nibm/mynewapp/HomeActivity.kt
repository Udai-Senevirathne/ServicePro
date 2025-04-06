package com.nibm.mynewapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        bottomNavigationView = findViewById(R.id.bottomNavigationView)

        // Check if the activity was launched with an intent to open a specific fragment
        val fragmentName = intent.getStringExtra("FRAGMENT")

        if (savedInstanceState == null) {
            if (fragmentName == "SETTING") {
                loadFragment(SettingFragment())
                bottomNavigationView.selectedItemId = R.id.nav_settings // Highlight Settings item
            } else {
                loadFragment(HomeFragment())
                bottomNavigationView.selectedItemId = R.id.nav_home // Highlight Home item
            }
        }

        // Bottom Navigation Listener
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    loadFragment(HomeFragment())
                    true
                }
                R.id.nav_job -> {
                    loadFragment(JobFragment())
                    true
                }
                R.id.nav_settings -> {
                    loadFragment(SettingFragment())
                    true
                }
                R.id.nav_profile -> {
                    loadFragment(ProfileFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        // Clear back stack to prevent stacking fragments unnecessarily
        supportFragmentManager.popBackStackImmediate(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    // Handle back press to navigate to HomeFragment or exit
    override fun onBackPressed() {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (currentFragment !is HomeFragment) {
            loadFragment(HomeFragment())
            bottomNavigationView.selectedItemId = R.id.nav_home
        } else {
            super.onBackPressed() // Exit the app if already on HomeFragment
        }
    }
}