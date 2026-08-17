package com.example.veterinarialemas.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.veterinarialemas.R;
import com.example.veterinarialemas.adapters.PacientesAdapter;
import com.example.veterinarialemas.models.AsistenciaMedicaModels;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class PacienteListDialogFragment extends DialogFragment {

    private RecyclerView rvPacientes;
    private TextView tvSinPacientes;
    private CircularProgressIndicator progressIndicator;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(requireContext())
                                .inflate(R.layout.dialog_lista_pacientes, null, false);
        rvPacientes = view.findViewById(R.id.rv_pacientes);
        tvSinPacientes = view.findViewById(R.id.tv_sin_pacientes);
        progressIndicator = view.findViewById(R.id.progress_pets);

        rvPacientes.setLayoutManager(new LinearLayoutManager(requireContext()));

        cargarPaciente();

        return new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.lista_paciente)
                .setView(view)
                //.setPositiveButton()
                .create();
    }

    private void cargarPaciente() {
        progressIndicator.setVisibility(View.VISIBLE);
        rvPacientes.setVisibility(View.GONE);
        tvSinPacientes.setVisibility(View.GONE);

        FirebaseFirestore.getInstance()
                .collection("PACIENTES")
                .get()
                .addOnSuccessListener(consulta -> {
                    progressIndicator.setVisibility(View.GONE);

                    List<AsistenciaMedicaModels> lstPacientes = new ArrayList<>();
                    consulta.forEach(query -> {
                        AsistenciaMedicaModels paciente = query.toObject(AsistenciaMedicaModels.class);
                        paciente.setId(query.getId());
                        lstPacientes.add(paciente);
                    });

                    if (lstPacientes.isEmpty()) {
                        tvSinPacientes.setVisibility(View.VISIBLE);
                    } else {
                        rvPacientes.setVisibility(View.VISIBLE);
                        rvPacientes.setAdapter(new PacientesAdapter(lstPacientes, paciente -> {
                            abrirEdicionPaciente(paciente);
                        }));
                    }
                })
                .addOnFailureListener(error -> {
                    progressIndicator.setVisibility(View.GONE);
                    error.printStackTrace();
                });
    }

    private void abrirEdicionPaciente(AsistenciaMedicaModels paciente) {
        EditPacienteDialogFragment editDialog = EditPacienteDialogFragment.newInstance(paciente);
        editDialog.setOnPacienteActualizarListener(this::cargarPaciente);
        editDialog.show(getParentFragmentManager(), "EditPacienteDialogFrament");
    }
}
