package com.example.veterinarialemas.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.veterinarialemas.R;
import com.example.veterinarialemas.models.AsistenciaMedicaModels;
import com.example.veterinarialemas.utils.ImageUploader;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;


public class MedicalAttentionFragment extends Fragment implements View.OnClickListener {

    TextInputEditText tieDuenio;
    TextInputEditText tieNombreMascota;
    AutoCompleteTextView actTipoMascota;
    TextInputEditText tieRazaMascota;
    TextInputEditText tieEdadMascota;
    MaterialButton btnGuardarMascota;
    CircularProgressIndicator loadingProgressIndicator;
    TextInputLayout tilDuenio;
    TextInputLayout tilNombreMascota;
    TextInputLayout tilTipoMascota;
    TextInputLayout tilRazaMascota;
    TextInputLayout tilEdadMascota;
    ImageView imgCapturaImagen;
    ActivityResultLauncher<Intent> imagePickerLauncher;
    Uri imageUri;
    Bitmap bitmap;


    String[] arregloMascotas = {"Perro", "Gato", "Conejo", "Hamster", "Loro", "Caballo"};

    FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_medical_attention, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tieDuenio = view.findViewById(R.id.tie_duenio);
        tieNombreMascota = view.findViewById(R.id.tie_mascota);
        actTipoMascota = view.findViewById(R.id.act_tipo_mascota);
        tieRazaMascota = view.findViewById(R.id.tie_raza_mascota);
        tieEdadMascota = view.findViewById(R.id.tie_edad_mascota);
        btnGuardarMascota = view.findViewById(R.id.btn_guardar_mascota);
        loadingProgressIndicator = view.findViewById(R.id.loading_progressBar);
        tilDuenio = view.findViewById(R.id.til_duenio);
        tilNombreMascota = view.findViewById(R.id.til_mascota);
        tilTipoMascota = view.findViewById(R.id.til_tipo_mascota);
        tilRazaMascota = view.findViewById(R.id.til_raza_mascota);
        tilEdadMascota = view.findViewById(R.id.til_edad_mascota);
        imgCapturaImagen = view.findViewById(R.id.img_capture_file);
        btnGuardarMascota.setOnClickListener(this);
        imgCapturaImagen.setOnClickListener(this);

        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                                                        resultado -> {
                                                            if (resultado.getData() != null && resultado.getData().getData() != null) {
                                                                imageUri = resultado.getData().getData();
                                                                showImagePreview();
                                                            }
                                                        });

        cleanValuesWithError();

        ArrayAdapter<String> listadoMascota = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, arregloMascotas);
        actTipoMascota.setAdapter(listadoMascota);

        // Configurar el toolBar de la app
        configurarMenuToolBar();

        db = FirebaseFirestore.getInstance();
    }

    private void configurarMenuToolBar() {
        MenuHost menuHost = requireActivity();
        menuHost.addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@org.jspecify.annotations.NonNull Menu menu, @org.jspecify.annotations.NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.menu_medical_attention, menu);
            }

            @Override
            public boolean onMenuItemSelected(@org.jspecify.annotations.NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.action_show_patients) {
                    mostrarListadoPaciente();
                    return true;
                }
                return false;
            }
        });
    }

    private void mostrarListadoPaciente() {
        new PacienteListDialogFragment().show(getParentFragmentManager(), "PacienteListDialogFragment");
    }


    private void showImagePreview() {
        if (imageUri == null) {
            return;
        }
        try {
            bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), imageUri);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Glide.with(this).load(bitmap).into(imgCapturaImagen);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_guardar_mascota) {
            savePets();
        } else if (v.getId() == R.id.img_capture_file) {
            openFileSystem();
        }
    }

    private void openFileSystem() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        imagePickerLauncher.launch(Intent.createChooser(intent, "Selecciona la imagen"));
    }

    private void savePets() {
        tilDuenio.setError(null);
        tilNombreMascota.setError(null);
        tilTipoMascota.setError(null);
        tilRazaMascota.setError(null);
        tilEdadMascota.setError(null);
        btnGuardarMascota.setVisibility(View.GONE);
        loadingProgressIndicator.setVisibility(View.VISIBLE);

        String nameHosts = tieDuenio.getText().toString();
        String namePets = tieNombreMascota.getText().toString();
        String typePets = actTipoMascota.getText().toString();
        String razaPets = tieRazaMascota.getText().toString();
        String agePets = tieEdadMascota.getText().toString();
        boolean isFieldFull = validateFieldsNulls(nameHosts,
                                                    namePets,
                                                    typePets,
                                                    razaPets,
                                                    agePets);
        if (!isFieldFull) {
            btnGuardarMascota.setVisibility(View.VISIBLE);
            loadingProgressIndicator.setVisibility(View.GONE);
            return;
        }
        // Llamado al api para guardar la imagen en el filesystem
        if (bitmap != null) {
            ImageUploader.uploadImage(bitmap, new ImageUploader.UploadCallBack() {
                @Override
                public void onSuccess(String imageUrl) {
                    saveDB(nameHosts,
                            namePets,
                            typePets,
                            razaPets,
                            agePets,
                            imageUrl);
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
                }
            });
        }

    }

    private void saveDB(String nameHosts, String namePets, String typePets, String razaPets, String agePets, String imageUrl) {
        AsistenciaMedicaModels asistencia = new AsistenciaMedicaModels(nameHosts, namePets, typePets, razaPets, agePets);
        asistencia.setUrlImagen(imageUrl);

        db.collection("PACIENTES")
                .add(asistencia)
                .addOnCompleteListener(exitoso -> {
                    loadingProgressIndicator.setVisibility(View.GONE);
                    btnGuardarMascota.setVisibility(View.VISIBLE);
                    if (exitoso.isSuccessful()) {
                        Toast.makeText(getContext(), "Se ha guardado la informacion", Toast.LENGTH_LONG).show();
                        cleanFieldValue();
                    } else {
                        Toast.makeText(getContext(), "NO se ha guardado la informacion", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(error -> {
                    error.printStackTrace();
                    loadingProgressIndicator.setVisibility(View.GONE);
                    btnGuardarMascota.setVisibility(View.VISIBLE);
                });
    }

    private boolean validateFieldsNulls(String nameHosts, String namePets, String typePets, String razaPets, String agePets) {
        if (nameHosts.isEmpty()) {
            tilDuenio.setError("El campo duenio es obligatorio");
            return false;
        }
        if (namePets.isEmpty()) {
            tilNombreMascota.setError("El campo nombre es obligatorio");
            return false;
        }
        if (typePets.isEmpty()) {
            tilTipoMascota.setError("No ha seleccionado el tipo de mascota");
            return false;
        }
        if (razaPets.isEmpty()) {
            tilRazaMascota.setError("El campo raza es obligatorio");
            return false;
        }
        if (agePets.isEmpty()) {
            tilEdadMascota.setError("El campo edad es obligatorio");
            return false;
        }
        return true;
    }


    private void cleanFieldValue() {
        tieDuenio.setText("");
        tieNombreMascota.setText("");
        actTipoMascota.setText("");
        tieRazaMascota.setText("");
        tieEdadMascota.setText("");
        imgCapturaImagen.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.upload_image));
    }

    private void cleanValuesWithError() {
        tieDuenio.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (tilDuenio.getError() != null) {
                    tilDuenio.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        tieNombreMascota.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (tilNombreMascota.getError() != null) {
                    tilNombreMascota.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        actTipoMascota.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (tilTipoMascota.getError() != null) {
                    tilTipoMascota.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        tieRazaMascota.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (tilRazaMascota.getError() != null) {
                    tilRazaMascota.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        tieEdadMascota.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (tilEdadMascota.getError() != null) {
                    tilEdadMascota.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
    }
}