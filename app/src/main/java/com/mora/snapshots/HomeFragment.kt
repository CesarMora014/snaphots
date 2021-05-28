package com.mora.snapshots

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.firebase.ui.database.FirebaseArray
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.firebase.ui.database.SnapshotParser
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.mora.snapshots.databinding.FragmentHomeBinding
import com.mora.snapshots.databinding.ItemSnapshotBinding


class HomeFragment : Fragment(), HomeAux {

    private lateinit var mBinding: FragmentHomeBinding

    private lateinit var mFirebaseAdapter: FirebaseRecyclerAdapter<Snapshot, SnapshotHolder>

    private lateinit var mLayoutManager: RecyclerView.LayoutManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        mBinding = FragmentHomeBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val query = FirebaseDatabase.getInstance().reference.child("snapshots")

        val options = FirebaseRecyclerOptions.Builder<Snapshot>().setQuery(query, SnapshotParser {
                val snapshot = it.getValue(Snapshot::class.java)
                snapshot!!.id = it.key!!
                snapshot
            }).build()

        mFirebaseAdapter = object : FirebaseRecyclerAdapter<Snapshot,SnapshotHolder>(options){

            private lateinit var mContext: Context

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SnapshotHolder {
                mContext = parent.context

                val view = LayoutInflater.from(mContext).inflate(R.layout.item_snapshot, parent, false)

                return SnapshotHolder(view)
            }

            override fun onBindViewHolder(holder: SnapshotHolder, position: Int, model: Snapshot) {
                val snapshot = getItem(position)

                with(holder){
                    setListener(snapshot)

                    binding.tvTitle.text = snapshot.title

                    binding.cbLike.text = snapshot.like_list.keys.size.toString()
                    FirebaseAuth.getInstance().currentUser?.let {
                        binding.cbLike.isChecked = snapshot.like_list.containsKey(it.uid)
                    }


                    Glide.with(mContext)
                        .load(snapshot.photo_url)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerCrop()
                        .into(binding.imgSnapshot)
                }
            }

            override fun onDataChanged() {
                super.onDataChanged()
                mBinding.progressbar.visibility = View.GONE
            }

            override fun onError(error: DatabaseError) {
                super.onError(error)
                Toast.makeText(mContext,error.message, Toast.LENGTH_SHORT).show()
            }

        }

        mLayoutManager = LinearLayoutManager(context)

        mBinding.recyclerview.apply {
            setHasFixedSize(true)
            layoutManager = mLayoutManager
            adapter = mFirebaseAdapter
        }
    }

    private fun confirmDelete(snapshot: Snapshot){

        MaterialAlertDialogBuilder(context!!)
                .setTitle(getString(R.string.dialog_delete_title))
                .setPositiveButton(R.string.dialog_delete_confirm) { dialogInterface, i ->
                    deleteSnapshot(snapshot)

                }
                .setNegativeButton(R.string.dialog_delete_cancel,null)
                .show()
    }

    private fun deleteSnapshot(snapshot: Snapshot){
        val databaseRef = FirebaseDatabase.getInstance().reference.child("snapshots")
        databaseRef.child(snapshot.id).removeValue()
    }

    private fun setLike(snapshot: Snapshot, checked: Boolean){
        val databaseRef = FirebaseDatabase.getInstance().reference.child("snapshots")

        if (checked){
            databaseRef.child(snapshot.id).child("like_list").child(FirebaseAuth.getInstance().currentUser!!.uid).setValue(checked)
        }
        else{
            databaseRef.child(snapshot.id).child("like_list").child(FirebaseAuth.getInstance().currentUser!!.uid).setValue(null)
        }

    }

    override fun onStart() {
        super.onStart()
        mFirebaseAdapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        mFirebaseAdapter.stopListening()
    }

    inner class SnapshotHolder (view:View) : RecyclerView.ViewHolder(view){
        val binding = ItemSnapshotBinding.bind(view)

        fun setListener(snapshot: Snapshot){

            binding.btnDelete.setOnClickListener {
                confirmDelete(snapshot)
            }

            binding.cbLike.setOnCheckedChangeListener { compoundButton, checked ->
                setLike(snapshot,checked)
            }
        }
    }

    override fun gotToTab() {
        mBinding.recyclerview.smoothScrollToPosition(0)
    }
}