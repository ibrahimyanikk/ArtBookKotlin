package com.ibrahim.artbookkotlin

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ibrahim.artbookkotlin.databinding.RecyclerRowBinding

class ArtAdapter(val ArtList:ArrayList<art>):RecyclerView.Adapter<ArtAdapter.ArtHolder>(){
    class ArtHolder(val binding: RecyclerRowBinding):RecyclerView.ViewHolder(binding.root){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtHolder {
        val binding=RecyclerRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ArtHolder(binding)
    }

    override fun getItemCount(): Int {
        return ArtList.size
    }

    override fun onBindViewHolder(holder: ArtHolder, position: Int) {
        //bağlanınca ne olacak
        holder.binding.recyclerviewtextvieww.text=ArtList.get(position).name

        //sırada ise tıklanınca ne olacak
        holder.itemView.setOnClickListener{
            val intent=Intent(holder.itemView.context,ActivityDetails::class.java)
            intent.putExtra("info","old")
            intent.putExtra("id",ArtList.get(position).id)//zaten eski ise id yide yolla
            holder.itemView.context.startActivity(intent)
        }
    }
}