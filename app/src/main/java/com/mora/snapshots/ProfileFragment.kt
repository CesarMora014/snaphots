package com.mora.snapshots

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.mora.snapshots.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {


    private lateinit var mBinging: FragmentProfileBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        mBinging = FragmentProfileBinding.inflate(inflater, container, false)

        return mBinging.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mBinging.tvName.text = FirebaseAuth.getInstance().currentUser?.displayName
        mBinging.tvEmail.text = FirebaseAuth.getInstance().currentUser?.email

        mBinging.btnLogout.setOnClickListener { logOut() }
    }

    private fun logOut() {

        context?.let {
            AuthUI.getInstance().signOut(it).addOnCompleteListener{
                Toast.makeText(context, R.string.logout_message, Toast.LENGTH_SHORT).show()
            }
        }


    }
}