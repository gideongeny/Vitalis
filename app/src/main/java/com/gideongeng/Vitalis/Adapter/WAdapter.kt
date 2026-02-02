package com.gideongeng.Vitalis.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gideongeng.Vitalis.Adapter.WAdapter.ViewHolder
import com.gideongeng.Vitalis.data_Model.water
import com.gideongeng.Vitalis.databinding.RecWaterBinding
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions

class WAdapter(options:FirestoreRecyclerOptions<water>):FirestoreRecyclerAdapter<water, ViewHolder>(options) {
    class ViewHolder(val binding: RecWaterBinding):RecyclerView.ViewHolder(binding.root)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding=RecWaterBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, model: water) {
        holder.binding.date.text=model.Date
        holder.binding.noGlass.text= model.glass.toString()
    }
}
