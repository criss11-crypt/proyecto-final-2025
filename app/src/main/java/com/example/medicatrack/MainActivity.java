package com.example.medicatrack;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.UiModeManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.NotificationManagerCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.Preference;


import com.example.medicatrack.databinding.ActivityMainBinding;
import com.example.medicatrack.model.Medicamento;

import com.example.medicatrack.model.Registro;
import com.example.medicatrack.model.enums.RegistroEstado;
import com.example.medicatrack.repo.MedicamentoRepository;
import com.example.medicatrack.repo.RegistroRepository;
import com.example.medicatrack.utilities.ResourcesUtility;
import com.example.medicatrack.viewmodels.MedicamentoViewModel;
import com.example.medicatrack.viewmodels.RegistroViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.example.medicatrack.creacion.CreacionActivity;
import com.example.medicatrack.receiver.RegistroReceiver;

import com.example.medicatrack.repo.persist.database.Database;
import com.google.android.material.snackbar.Snackbar;


public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    public static final int PERMISO_NOTIFICACION_INICIAL = 0;
    public static final int PERMISO_NOTIFICACION_CFG = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // ¿Crear canal de notificaciones?
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String[] permisos = {Manifest.permission.POST_NOTIFICATIONS};

        if (!notificationManager.areNotificationsEnabled()) { // Si las notificaciones del celular no estan habilitadas
            editor.putBoolean("recibir_not", false); // Seteo en falso la preferencia
            requestPermissions(permisos, MainActivity.PERMISO_NOTIFICACION_INICIAL); // Pido que se habiliten

        } else if (sharedPreferences.getBoolean("recibir_not", true)) { // si no, pregunto por el valor actual de la preferencia. Si no existe (defValue == true) o tiene valor true, entonces
            editor.putBoolean("recibir_not", true);
            createNotificationChannel();
        } else {
            notificationManager.deleteNotificationChannel(getString(R.string.channel_id));
            editor.putBoolean("recibir_not", false); // De mas
        }
        editor.commit();
        // -------

        Boolean temaOscuro = sharedPreferences.getBoolean("switch_tema",false);
        if(temaOscuro)
        {
            UiModeManager uiManager = (UiModeManager) this.getSystemService(Context.UI_MODE_SERVICE);
            if(BuildConfig.VERSION_CODE > 30) uiManager.setNightMode(UiModeManager.MODE_NIGHT_YES);
            else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else
        {
            UiModeManager uiManager = (UiModeManager) this.getSystemService(Context.UI_MODE_SERVICE);
            if(BuildConfig.VERSION_CODE > 30) uiManager.setNightMode(UiModeManager.MODE_NIGHT_NO);
            else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);


        MedicamentoViewModel viewModel = new ViewModelProvider(this).get(MedicamentoViewModel.class);

        viewModel.activarFab.observe(this, aBoolean ->
        {
            if (aBoolean) binding.fab.setVisibility(FloatingActionButton.VISIBLE);
            else binding.fab.setVisibility(FloatingActionButton.GONE);
        });

        viewModel.navegarInfo.observe(this, aBoolean ->
        {
            if (aBoolean) {
                navController.navigate(R.id.action_global_medicamentoInfoFragment);
                viewModel.navegarInfo.postValue(false);
            }
        });


        // Obtener resultado de la otra actividad
        ActivityResultLauncher<Intent> creacionLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            Medicamento a = data.getExtras().getParcelable("Medicamento");

                            viewModel.nuevoMedicamento.setValue(a);
                            Toast.makeText(getApplicationContext(), "Se ha agregado un nuevo medicamento.", Toast.LENGTH_LONG).show();
                        }
                    }
                });

        // Boton flotante, lleva a la actividad de Creacion
        binding.fab.setOnClickListener(view -> {
            Intent intent = new Intent(this, CreacionActivity.class);
            creacionLauncher.launch(intent);
        });


        // Se inicia la actividad producto de la notificacion
        if (getIntent().getAction().equals(RegistroReceiver.REGISTRAR))
        {
            RegistroViewModel registroViewModel = new ViewModelProvider(this).get(RegistroViewModel.class);
            Medicamento medicamento = getIntent().getParcelableExtra("Medicamento");
            Registro registro = getIntent().getParcelableExtra("Registro");
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Alerta")
                    .setIcon(ResourcesUtility.getMedicamentoImage(medicamento))
                    .setMessage("¿Has consumido: " + medicamento.getNombre() + " • " + String.format("%.2f", medicamento.getConcentracion()) + " " + ResourcesUtility.enumToText(medicamento.getUnidad()) + "?")
                    .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id)
                        {
                            registro.setEstado(RegistroEstado.CONFIRMADO);
                            RegistroRepository.getInstance(getApplicationContext()).update(registro, result -> {

                            });
                            registroViewModel.nuevoRegistro.setValue(registro);
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id)
                        {
                            registro.setEstado(RegistroEstado.CANCELADO);
                            RegistroRepository.getInstance(getApplicationContext()).update(registro, result -> {

                            });
                            registroViewModel.nuevoRegistro.setValue(registro);
                        }

                    });
            builder.create().show();
            getIntent().setAction("INTENT_TRATADO"); // Cambiar la accion del intent, porque si se recrea la actividad (modo oscuro on/off), vuelve a ejecutar esta parte
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if ((requestCode == PERMISO_NOTIFICACION_INICIAL || requestCode == PERMISO_NOTIFICACION_CFG)
                && grantResults.length > 0 && grantResults[0] == 0) { // Si se concede el permiso
            editor.putBoolean("recibir_not", true);
            editor.commit();
            createNotificationChannel();
            if (requestCode == PERMISO_NOTIFICACION_CFG)
                Toast.makeText(getApplicationContext(), "Ahora puede habilitar las notificaciones para el registro de medicamentos.", Toast.LENGTH_LONG).show();
        }

        if ((requestCode == PERMISO_NOTIFICACION_INICIAL || requestCode == PERMISO_NOTIFICACION_CFG)
                && grantResults.length > 0
                && grantResults[0] == -1 && shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {

            // si no se concede el permiso, pregunto si es necesario pedirle racionalmente que lo conceda

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("NOTIFICACIONES")
                    .setMessage("Para poder notificarte a la hora de tomar tus medicamentos, es necesario que concedas los permisos de notificación.")
                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, MainActivity.PERMISO_NOTIFICACION_INICIAL);
                        }
                    })
                    .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    });
            builder.create().show();
        } else // Request desde settings
            if (requestCode == PERMISO_NOTIFICACION_CFG && grantResults.length > 0 && grantResults[0] == -1)
                Toast.makeText(getApplicationContext(), "Primero habilite las notificaciones de MedicaTrack desde el sistema.", Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
            navController.navigate(R.id.action_global_settingsFragment);
            return true;
        }
        if (id == R.id.action_map) {

            // Buscar por farmacias cercanas
            Uri gmmIntentUri = Uri.parse("geo:0,0?q=Farmacias cercanas");
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            startActivity(mapIntent);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        binding.fab.setVisibility(FloatingActionButton.GONE);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    protected void onDestroy() {
        // Cerrar la conexion de la base de datos
        //Database.getInstance(getApplicationContext()).close();
        super.onDestroy();
    }

    private void createNotificationChannel()
    {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            String CHANNEL_ID = getString(R.string.channel_id);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

}