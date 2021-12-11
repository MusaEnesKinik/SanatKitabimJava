package com.eneskinik.sanatkitabimjava;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.eneskinik.sanatkitabimjava.databinding.RecyclerRowBinding;

import java.util.ArrayList;

public class ResimAdapter extends RecyclerView.Adapter<ResimAdapter.ResimHolder> {

    ArrayList<Resim> resimArrayList;

    public ResimAdapter(ArrayList<Resim> resimArrayList) {
        this.resimArrayList = resimArrayList;
    }

    @NonNull
    @Override
    public ResimHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerRowBinding recyclerRowBinding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new  ResimHolder(recyclerRowBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull ResimHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.binding.recyclerViewTextView.setText(resimArrayList.get(position).name);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(holder.itemView.getContext(),SanatKitabim.class);
                intent.putExtra("info","eski bir şey gönderilecek");
                intent.putExtra("resimId", resimArrayList.get(position).id);
                holder.itemView.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return resimArrayList.size(); //resimArrayList in içerisinde kaç eleman varsa onu gösterecek
    }

    public class ResimHolder extends RecyclerView.ViewHolder {

        private RecyclerRowBinding binding;

        public ResimHolder(RecyclerRowBinding binding) {
            super(binding.getRoot()); // görünümü aldık
            this.binding = binding; // ResimHolder daki binding i private deki binding e eşitledik
        }
    }

}
