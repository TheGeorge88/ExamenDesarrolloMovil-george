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
        // TODO (Tarea 4 - Navegacion): mostrar el listado de pacientes registrados.
        // Debe abrir el dialogo PacienteListDialogFragment usando el
        // FragmentManager del padre, con el tag "PacienteListDialogFragment",
        // tal como se hace para el resto de los DialogFragment de la app.
    }


    private void showImagePreview() {
        // TODO (Tarea 2 - Imagen): mostrar en pantalla la imagen seleccionada.
        // 1) Verificar que imageUri no sea nulo.
        // 2) Obtener un Bitmap a partir de imageUri usando
        //    MediaStore.Images.Media.getBitmap(...).
        // 3) Cargar ese Bitmap en imgCapturaImagen usando Glide.
        // 4) Manejar la IOException que pueda lanzar getBitmap(...).
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
        // TODO (Tarea 2 - Imagen): abrir el selector de imagenes del dispositivo.
        // 1) Crear un Intent con accion Intent.ACTION_GET_CONTENT.
        // 2) Configurar el tipo MIME como "image/*".
        // 3) Lanzar el intent con imagePickerLauncher.launch(...), envuelto
        //    en un Intent.createChooser(...) con el titulo "Selecciona la imagen".
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
        // TODO (Tarea 3 - Guardado en Firestore): persistir al paciente.
        // 1) Crear un AsistenciaMedicaModels con nameHosts, namePets, typePets,
        //    razaPets y agePets, y asignarle imageUrl con setUrlImagen(...).
        // 2) Agregarlo a la coleccion "PACIENTES" de Firestore (db.collection(...).add(...)).
        // 3) En addOnCompleteListener: ocultar loadingProgressIndicator, mostrar
        //    btnGuardarMascota, y mostrar un Toast de exito + limpiar el formulario
        //    (cleanFieldValue()) si exitoso.isSuccessful(), o un Toast de error si no.
        // 4) En addOnFailureListener: imprimir el error y restaurar la visibilidad
        //    de btnGuardarMascota / loadingProgressIndicator igual que en el paso 3.
    }

    private boolean validateFieldsNulls(String nameHosts, String namePets, String typePets, String razaPets, String agePets) {
        // TODO (Tarea 1 - Validaciones): validar que ningun campo llegue vacio.
        // Por cada campo vacio, asignar un mensaje de error al TextInputLayout
        // correspondiente con setError("...") y retornar false de inmediato:
        //   nameHosts  -> tilDuenio        ("El campo duenio es obligatorio")
        //   namePets   -> tilNombreMascota ("El campo nombre es obligatorio")
        //   typePets   -> tilTipoMascota   ("No ha seleccionado el tipo de mascota")
        //   razaPets   -> tilRazaMascota   ("El campo raza es obligatorio")
        //   agePets    -> tilEdadMascota   ("El campo edad es obligatorio")
        // Si todos los campos tienen contenido, retornar true.
        return false;
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
        // TODO (Tarea 5 - UX de errores): limpiar el error de cada campo apenas
        // el usuario empiece a corregirlo.
        // Para tieDuenio, tieNombreMascota, actTipoMascota, tieRazaMascota y
        // tieEdadMascota, agregar un TextWatcher (addTextChangedListener) que,
        // dentro de onTextChanged(...), verifique si el TextInputLayout asociado
        // (tilDuenio, tilNombreMascota, tilTipoMascota, tilRazaMascota,
        // tilEdadMascota respectivamente) tiene un error visible
        // (getError() != null) y en ese caso lo limpie con setError(null).
    }
}