# Veterinaria Lemas - Actividad evaluada: MedicalAttentionFragment

## Contexto

Este proyecto es una app Android para la gestion de una veterinaria. La
pantalla de **"Asistencia Medica"** permite registrar un paciente (mascota
+ duenio): nombre del duenio, nombre de la mascota, tipo, raza, edad y una
foto. Esa informacion se guarda en **Firebase Firestore**, en la coleccion
`PACIENTES`.

El archivo que debes completar es:

`app/src/main/java/com/example/veterinarialemas/fragments/MedicalAttentionFragment.java`

Dentro de ese archivo hay 5 metodos a los que se les quito el cuerpo (la
implementacion). En su lugar encontraras comentarios
`// TODO (Tarea N - ...)` explicando que se espera. Reglas generales:

- **No cambies la firma de ningun metodo** (nombre, parametros, tipo de
  retorno). El resto del codigo del proyecto llama a estos metodos tal
  como estan; si cambias la firma, la app dejara de compilar.
- Puedes apoyarte en `PacienteListDialogFragment.java`,
  `EditPacienteDialogFragment.java` y `PacientesAdapter.java` para ver como
  se usa Firestore y los dialogos en el resto de la app: siguen el mismo
  patron que debes replicar aqui.
- El objetivo no es solo "que compile", sino que el flujo completo
  funcione: guardar un paciente y verlo reflejado en Firestore y en el
  listado.

---

## Tarea 1 - Validaciones del formulario (2 puntos)

**Metodo:** `validateFieldsNulls(String nameHosts, String namePets, String typePets, String razaPets, String agePets)`

**Que debes hacer:**
Revisar, en orden, que ninguno de los 5 parametros llegue vacio
(`String.isEmpty()`). Apenas encuentres el primer campo vacio:
1. Asigna un mensaje de error al `TextInputLayout` correspondiente usando
   `setError("mensaje")`.
2. Retorna `false` inmediatamente (no sigas validando los demas campos).

Relacion parametro -> TextInputLayout -> mensaje exacto a usar:

| Parametro   | TextInputLayout   | Mensaje de error |
|-------------|--------------------|-------------------|
| `nameHosts` | `tilDuenio`        | `"El campo duenio es obligatorio"` |
| `namePets`  | `tilNombreMascota` | `"El campo nombre es obligatorio"` |
| `typePets`  | `tilTipoMascota`   | `"No ha seleccionado el tipo de mascota"` |
| `razaPets`  | `tilRazaMascota`   | `"El campo raza es obligatorio"` |
| `agePets`   | `tilEdadMascota`   | `"El campo edad es obligatorio"` |

Si los 5 campos tienen contenido, el metodo debe retornar `true`.

**Por que se hace:** este metodo es la primera linea de defensa antes de
gastar recursos (subir una imagen, escribir en Firestore) con datos
incompletos. Es el mismo principio que se usa en cualquier formulario de
produccion: **nunca confiar en que el usuario llenara todo correctamente**,
y dar retroalimentacion clara e inmediata sobre que falta corregir, en el
mismo campo que causa el problema (no con un mensaje generico).

**Como saber que esta bien:** `savePets()` ya llama a este metodo y usa su
resultado (`isFieldFull`) para decidir si continua o se detiene mostrando
los errores. No necesitas tocar `savePets()`.

---

## Tarea 2 - Seleccion y previsualizacion de la imagen (2 puntos)

**Metodos:** `openFileSystem()` y `showImagePreview()`

**Que debes hacer en `openFileSystem()`:**
1. Crear un `Intent` con accion `Intent.ACTION_GET_CONTENT`.
2. Configurar `intent.setType("image/*")` para que solo se puedan elegir
   imagenes.
3. Lanzarlo con `imagePickerLauncher.launch(...)`, envolviendolo en
   `Intent.createChooser(intent, "Selecciona la imagen")` para que el
   usuario elija con que app abrir el selector.

**Que debes hacer en `showImagePreview()`:**
1. Verificar que `imageUri` no sea `null` (solo hay algo que mostrar si el
   usuario ya eligio una imagen).
2. Obtener un `Bitmap` a partir de `imageUri` con
   `MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), imageUri)`.
   Este metodo puede lanzar `IOException`: debes capturarla (por ejemplo
   relanzandola como `RuntimeException`, igual que se hace en el resto del
   proyecto).
3. Guardar el resultado en el campo `bitmap` de la clase.
4. Cargar ese `bitmap` dentro de `imgCapturaImagen` usando
   `Glide.with(this).load(bitmap).into(imgCapturaImagen)`.

**Por que se hace:** `openFileSystem()` se dispara al tocar la imagen
(`imgCapturaImagen.setOnClickListener(this)` en `onClick`), y
`imagePickerLauncher` ya esta registrado en `onViewCreated` para llamar a
`showImagePreview()` cuando el usuario regresa con una imagen elegida. Es
el flujo estandar de Android para pedir contenido externo (galeria) sin
pedir permisos de almacenamiento explicitos, y usar Glide es el patron que
ya usa el resto de la app para cargar imagenes eficientemente. Ademas, el
campo `bitmap` que llenas aqui es el que luego usa `savePets()` (Tarea 3)
para decidir si hay imagen que subir.

**Como saber que esta bien:** al tocar el icono de imagen debe abrirse el
selector de archivos del sistema, y al elegir una foto debe verse
inmediatamente en el `ImageView` del formulario.

---

## Tarea 3 - Guardado en Firestore (2 puntos)

**Metodo:** `saveDB(String nameHosts, String namePets, String typePets, String razaPets, String agePets, String imageUrl)`

**Que debes hacer:**
1. Crear una instancia de `AsistenciaMedicaModels` pasando `nameHosts`,
   `namePets`, `typePets`, `razaPets` y `agePets` al constructor.
2. Asignarle la URL de la imagen con `asistencia.setUrlImagen(imageUrl)`.
3. Guardarla en Firestore: `db.collection("PACIENTES").add(asistencia)`.
4. En `addOnCompleteListener`:
   - Ocultar `loadingProgressIndicator` (`GONE`) y volver a mostrar
     `btnGuardarMascota` (`VISIBLE`) — la operacion ya termino, con exito o
     sin exito.
   - Si `exitoso.isSuccessful()` es `true`: mostrar un `Toast` de exito
     (`"Se ha guardado la informacion"`) y llamar a `cleanFieldValue()`
     para limpiar el formulario.
   - Si es `false`: mostrar un `Toast` indicando que no se guardo
     (`"NO se ha guardado la informacion"`).
5. En `addOnFailureListener`: imprimir el error (`error.printStackTrace()`)
   y restaurar la visibilidad de `btnGuardarMascota` /
   `loadingProgressIndicator` igual que en el paso 4, para que el usuario
   pueda intentar guardar de nuevo.

**Por que se hace:** este metodo es el paso final del flujo: convierte los
datos del formulario (ya validados en la Tarea 1, con imagen ya subida
gracias a la Tarea 2) en un registro persistente en la base de datos. Se
separa en `onComplete`/`onFailure` porque las operaciones de Firestore son
**asincronas**: la UI debe reaccionar cuando la respuesta realmente llega,
no antes. Restaurar siempre la visibilidad del boton y del indicador de
carga (tanto en exito como en error) evita que la pantalla se quede
"congelada" mostrando el loading para siempre si algo falla.

**Como saber que esta bien:** al guardar un paciente con datos validos,
debe aparecer el mensaje de exito, el formulario debe limpiarse, y el
nuevo documento debe aparecer en la coleccion `PACIENTES` de Firestore
(puedes verificarlo en la consola de Firebase).

---

## Tarea 4 - Ver listado de pacientes (2 puntos)

**Metodo:** `mostrarListadoPaciente()`

**Que debes hacer:**
Mostrar el dialogo `PacienteListDialogFragment` usando:

```java
new PacienteListDialogFragment().show(getParentFragmentManager(), "PacienteListDialogFragment");
```

**Por que se hace:** `configurarMenuToolBar()` (ya implementado) agrega una
opcion al menu del toolbar (`R.id.action_show_patients`, definido en
`res/menu/menu_medical_attention.xml`) y, cuando el usuario la toca, llama
a `mostrarListadoPaciente()`. Sin esta implementacion, la opcion del menu
no hace nada. Se usa `getParentFragmentManager()` (el FragmentManager de la
Activity que aloja este fragment) en vez de `getChildFragmentManager()`
porque el dialogo debe mostrarse por encima de toda la pantalla, no
anidado dentro de este fragment.

**Como saber que esta bien:** al tocar la opcion correspondiente del menu
del toolbar, debe abrirse un dialogo con la lista de pacientes guardados,
incluyendo los que hayas registrado al probar la Tarea 3.

---

## Tarea 5 - Limpiar errores mientras el usuario escribe (2 puntos)

**Metodo:** `cleanValuesWithError()`

**Que debes hacer:**
Para cada uno de estos 5 campos: `tieDuenio`, `tieNombreMascota`,
`actTipoMascota`, `tieRazaMascota` y `tieEdadMascota`, agregar un
`TextWatcher` con `addTextChangedListener(new TextWatcher() { ... })`.
Dentro del metodo `onTextChanged(CharSequence s, int start, int before, int count)`
de cada uno, verificar si el `TextInputLayout` asociado tiene un error
visible y, si lo tiene, limpiarlo:

| Campo (`TextInputEditText`/`AutoCompleteTextView`) | TextInputLayout a limpiar |
|---|---|
| `tieDuenio` | `tilDuenio` |
| `tieNombreMascota` | `tilNombreMascota` |
| `actTipoMascota` | `tilTipoMascota` |
| `tieRazaMascota` | `tilRazaMascota` |
| `tieEdadMascota` | `tilEdadMascota` |

Ejemplo para un solo campo (replica el mismo patron para los otros 4):

```java
tieDuenio.addTextChangedListener(new TextWatcher() {
    @Override
    public void afterTextChanged(Editable s) { }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (tilDuenio.getError() != null) {
            tilDuenio.setError(null);
        }
    }
});
```

**Por que se hace:** `cleanValuesWithError()` se llama una sola vez, dentro
de `onViewCreated`, para registrar estos listeners cuando se crea la
pantalla. Sin esta implementacion, un mensaje de error puesto por la
Tarea 1 (por ejemplo "El campo duenio es obligatorio") se queda visible
para siempre aunque el usuario ya haya escrito algo en el campo, lo cual
es una mala experiencia de usuario: el error deja de ser verdadero apenas
el usuario empieza a corregirlo, y la interfaz debe reflejar eso al
instante, sin esperar a que vuelva a presionar "Guardar".

**Como saber que esta bien:** provoca un error dejando un campo vacio y
presionando "Guardar"; luego empieza a escribir en ese campo. El mensaje
de error debe desaparecer apenas escribes la primera letra, sin necesidad
de volver a presionar el boton.

---

## Como probar tu trabajo (checklist final)

1. Ejecuta la app y entra a la seccion "Asistencia Medica".
2. Deja campos vacios y presiona "Guardar": deben aparecer los errores,
   uno a la vez, en el campo correcto (Tarea 1).
3. Toca la imagen, selecciona una foto desde la galeria y verifica que se
   muestre en pantalla (Tarea 2).
4. Completa todo el formulario (con imagen) y guarda: debe aparecer el
   mensaje de exito, el formulario debe limpiarse, y el registro debe
   quedar en Firestore, coleccion `PACIENTES` (Tarea 3).
5. Desde el menu del toolbar, abre el listado de pacientes y confirma que
   se muestre el nuevo registro que acabas de guardar (Tarea 4).
6. Provoca un error de nuevo, empieza a corregir ese campo sin presionar
   "Guardar" y verifica que el mensaje de error desaparezca solo (Tarea 5).

## Rubrica (10 puntos)

| Tarea | Puntos | Se evalua |
|---|---|---|
| 1. Validaciones | 2 | Cada campo vacio muestra su error especifico y detiene el guardado |
| 2. Imagen | 2 | Se puede elegir imagen y se previsualiza correctamente |
| 3. Guardado en Firestore | 2 | El paciente queda guardado con todos sus datos e imagen |
| 4. Listado de pacientes | 2 | El menu del toolbar abre el listado con los pacientes guardados |
| 5. Limpieza de errores | 2 | Los mensajes de error desaparecen al corregir el campo, sin re-enviar el formulario |
