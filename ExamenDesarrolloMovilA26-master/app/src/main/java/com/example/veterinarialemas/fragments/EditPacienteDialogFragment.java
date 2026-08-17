package com.example.veterinarialemas.fragments;


import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.veterinarialemas.R;
import com.example.veterinarialemas.models.AsistenciaMedicaModels;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

public class EditPacienteDialogFragment extends DialogFragment {

    private static final String ARG_ID = "arg_id";
    private static final String ARG_DUENIO = "arg_duenio";
    private static final String ARG_MASCOTA = "arg_mascota";
    private static final String ARG_TIPO = "arg_tipo";
    private static final String ARG_RAZA = "arg_raza";
    private static final String ARG_EDAD = "arg_edad";

    private final String[] arregloMascota = {"Perro", "Gato", "Conejo", "Hamster", "Loro", "Caballo"};

    public interface OnPacienteActualizarListener {
        void onPacienteActualizado();
    }

    private OnPacienteActualizarListener listener;

    public static EditPacienteDialogFragment newInstance(AsistenciaMedicaModels paciente) {
        EditPacienteDialogFragment fragment = new EditPacienteDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ID, paciente.getId());
        args.putString(ARG_DUENIO, paciente.getNombreDuenio());
        args.putString(ARG_MASCOTA, paciente.getNombreMascota());
        args.putString(ARG_TIPO, paciente.getTipoMascota());
        args.putString(ARG_RAZA, paciente.getRazaMascota());
        args.putString(ARG_EDAD, paciente.getEdadMascota());
        fragment.setArguments(args);
        return fragment;
    }

    public void setOnPacienteActualizarListener(OnPacienteActualizarListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(requireContext())
                                  .inflate(R.layout.dialog_editar_paciente, null, false);

        TextInputEditText tieDuenio = view.findViewById(R.id.tie_edit_duenio);
        TextInputEditText tieMascota = view.findViewById(R.id.tie_edit_mascota);
        AutoCompleteTextView actTipo = view.findViewById(R.id.act_edit_tipo_mascota);
        TextInputEditText tieRaza = view.findViewById(R.id.tie_edit_raza);
        TextInputEditText tieEdad = view.findViewById(R.id.tie_edit_edad);

        ArrayAdapter<String> adapterTipo = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, arregloMascota);
        actTipo.setAdapter(adapterTipo);

        Bundle args = requireArguments();
        String id = args.getString(ARG_ID);
        tieDuenio.setText(args.getString(ARG_DUENIO));
        tieMascota.setText(args.getString(ARG_MASCOTA));
        actTipo.setText(args.getString(ARG_TIPO));
        tieRaza.setText(args.getString(ARG_RAZA));
        tieEdad.setText(args.getString(ARG_EDAD));

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle("EDITAR PACIENTE")
                .setView(view)
                .setPositiveButton("GUARDAR", null)
                .setNegativeButton("CANCELAR", (dialogInterface, which) -> dialogInterface.dismiss())
                .create();

        dialog.setOnShowListener(dialog1 -> {
                    Button btnGuardar = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                    btnGuardar.setOnClickListener(guardar -> actualizarPaciente(
                            id,
                            tieDuenio.getText().toString(),
                            tieMascota.getText().toString(),
                            actTipo.getText().toString(),
                            tieRaza.getText().toString(),
                            tieEdad.getText().toString()
                    ));
                });

        return dialog;
    }

    private void actualizarPaciente(String id, String duenio, String mascota, String tipo, String raza, String edad) {
        if (duenio.isEmpty() || mascota.isEmpty() || tipo.isEmpty() || raza.isEmpty() || edad.isEmpty()) {
            Toast.makeText(getContext(), "Todos los campos son obligatorios", Toast.LENGTH_LONG).show();
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("PACIENTES")
                .document(id)
                .update(
                        "nombreDuenio", duenio,
                        "nombreMascota", mascota,
                        "tipoMascota", tipo,
                        "razaMascota", raza,
                        "edadMascota", edad
                )
                .addOnSuccessListener( exitoso -> {
                    Toast.makeText(getContext(), "Paciente Actualizado", Toast.LENGTH_LONG).show();
                    if (listener!= null) {
                        listener.onPacienteActualizado();
                    }
                    dismiss();
                })
                .addOnFailureListener( error -> {
                    error.printStackTrace();
                    Toast.makeText(requireContext(), "No se pudo actulizar el paciente", Toast.LENGTH_LONG).show();
                });
    }
}
