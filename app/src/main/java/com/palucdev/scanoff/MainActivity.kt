package com.palucdev.scanoff

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.palucdev.scanoff.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain NavController from the NavHostFragment
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        navController = navHostFragment.navController

        // Wire BottomNavigationView to NavController for the 3 non-scan top-level tabs
        binding.bottomNavView.setupWithNavController(navController)

        // Intercept the Scan tab: always push ScannerFragment as a full-screen destination
        // rather than switching to it as a persistent tab. This means the bottom nav is
        // hidden while scanning and back navigates back to wherever the user came from.
        binding.bottomNavView.setOnItemSelectedListener { item ->
            if (item.itemId == R.id.nav_scan) {
                // Navigate to the pushed (non-top-level) ScannerFragment
                navController.navigate(R.id.ScannerFragment)
                true
            } else {
                // For all other tabs delegate to NavigationUI default behaviour
                androidx.navigation.ui.NavigationUI.onNavDestinationSelected(item, navController)
            }
        }

        // Hide bottom nav for full-screen destinations
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.ScannerFragment, R.id.nav_scan, R.id.DocumentFragment -> {
                    binding.bottomNavView.visibility = View.GONE
                }
                else -> {
                    binding.bottomNavView.visibility = View.VISIBLE
                }
            }
        }
    }
}
