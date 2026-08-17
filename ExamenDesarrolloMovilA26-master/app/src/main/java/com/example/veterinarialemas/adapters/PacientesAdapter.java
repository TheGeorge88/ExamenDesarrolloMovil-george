package com.example.veterinarialemas.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.veterinarialemas.R;
import com.example.veterinarialemas.models.AsistenciaMedicaModels;

import java.util.List;

public class PacientesAdapter extends RecyclerView.Adapter<PacientesAdapter.PacienteViewHolder> {

    List<AsistenciaMedicaModels> lstPacientes;
    OnEditPacienteListener editListener;

    public PacientesAdapter(List<AsistenciaMedicaModels> lstPacientes, OnEditPacienteListener editListener) {
        this.lstPacientes = lstPacientes;
        this.editListener = editListener;
    }

    @NonNull
    @Override
    public PacienteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.item_paciente, parent, false);
        return new PacienteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PacienteViewHolder holder, int position) {
        AsistenciaMedicaModels objAsistenciaMedica = lstPacientes.get(position);
        holder.tvNombreMascota.setText(objAsistenciaMedica.getNombreMascota());
        holder.tvDuenio.setText(objAsistenciaMedica.getNombreDuenio());
        holder.tvTipoRaza.setText(objAsistenciaMedica.getRazaMascota());
        holder.tvEdad.setText(objAsistenciaMedica.getEdadMascota());

        if (objAsistenciaMedica.getUrlImagen() != null && !objAsistenciaMedica.getUrlImagen().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(objAsistenciaMedica.getUrlImagen())
                    .into(holder.imgPaciente);
        }
        holder.imgEdit.setOnClickListener(v -> {
            if (editListener != null) {
                editListener.onEditPaciente(objAsistenciaMedica);
            }
        });
    }

    @Override
    public int getItemCount() {
        return lstPacientes.size();
    }

    public interface OnEditPacienteListener {
        void onEditPaciente(AsistenciaMedicaModels paciente);
    }

    static class PacienteViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPaciente;
        TextView tvNombreMascota;
        TextView tvDuenio;
        TextView tvTipoRaza;
        TextView tvEdad;
        ImageView imgEdit;

        public PacienteViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPaciente = itemView.findViewById(R.id.img_item_pet);
            tvNombreMascota = itemView.findViewById(R.id.tv_item_name_pets);
            tvDuenio = itemView.findViewById(R.id.tv_item_guest);
            tvTipoRaza = itemView.findViewById(R.id.tv_item_type_pets);
            tvEdad = itemView.findViewById(R.id.tv_item_age);
            imgEdit = itemView.findViewById(R.id.img_update_pet);
        }
    }
}
