package com.mora.snapshots

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.mora.snapshots.databinding.ActivityMainBinding
import java.util.*

class MainActivity : AppCompatActivity() {
    private val RC_SING_IN = 21

    private lateinit var  mBinding: ActivityMainBinding

    private lateinit var mActiveFragment: Fragment
    private lateinit var mFragmentManager: FragmentManager

    private lateinit var mAuthListener: FirebaseAuth.AuthStateListener
    private var mFirebaseAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        setupAuth()
        setupBottomNav()
    }

    private fun setupAuth() {
        mFirebaseAuth = FirebaseAuth.getInstance()
        mAuthListener = FirebaseAuth.AuthStateListener {
            val user = it.currentUser
            if (user == null)
                startActivityForResult(AuthUI.getInstance()
                                        .createSignInIntentBuilder()
                                        .setIsSmartLockEnabled(false)
                                        .setAvailableProviders(
                                            Arrays.asList(AuthUI.IdpConfig.EmailBuilder().build(),
                                                            AuthUI.IdpConfig.GoogleBuilder().build())
                                        ).build(), RC_SING_IN)
        }
    }

    private fun setupBottomNav()
    {
        mFragmentManager = supportFragmentManager

        val homeFragment = HomeFragment()
        val addFragment =  AddFragment()
        val profileFragment = ProfileFragment()

        mActiveFragment = homeFragment

        mFragmentManager.beginTransaction()
            .add(R.id.fragmentHost, profileFragment,ProfileFragment::class.java.name)
            .hide(profileFragment)
            .commit()

        mFragmentManager.beginTransaction()
            .add(R.id.fragmentHost, addFragment,AddFragment::class.java.name)
            .hide(addFragment)
            .commit()

        mFragmentManager.beginTransaction()
            .add(R.id.fragmentHost, homeFragment,HomeFragment::class.java.name)
            .commit()

        mBinding.bottomNav.setOnNavigationItemSelectedListener {
            when(it.itemId){
                R.id.action_home -> {
                    mFragmentManager.beginTransaction().hide(mActiveFragment).show(homeFragment).commit()
                    mActiveFragment = homeFragment
                    true
                }
                R.id.action_add -> {
                    mFragmentManager.beginTransaction().hide(mActiveFragment).show(addFragment).commit()
                    mActiveFragment = addFragment
                    true
                }
                R.id.action_profile -> {
                    mFragmentManager.beginTransaction().hide(mActiveFragment).show(profileFragment).commit()
                    mActiveFragment = profileFragment
                    true
                }

                else -> false
            }
        }

        mBinding.bottomNav.setOnNavigationItemReselectedListener {
            when(it.itemId){
                R.id.action_home -> (homeFragment as HomeAux).gotToTab()

            }
        }
    }


    override fun onResume() {
        super.onResume()
        mFirebaseAuth?.addAuthStateListener ( mAuthListener )
    }

    override fun onPause() {
        super.onPause()
        mFirebaseAuth?.removeAuthStateListener(mAuthListener)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SING_IN){
            if (resultCode == Activity.RESULT_OK){
                Toast.makeText(this, "Bienvenido", Toast.LENGTH_SHORT).show()
            }
            else{

                if (IdpResponse.fromResultIntent(data) == null){
                    finish()
                }

            }
        }
    }
}