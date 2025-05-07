package com.example.medicatrack;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.UiModeManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.NotificationManagerCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

public class SettingsFragment extends PreferenceFragmentCompat {
private NotificationManagerCompat  notificationManager;



    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
        setHasOptionsMenu(true);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        menu.clear(); // Para borrar las opciones de la toolbar
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        String[] permisos = {Manifest.permission.POST_NOTIFICATIONS};

        Preference recibirNotificaciones = findPreference("recibir_not");
        Preference tema = findPreference("switch_tema");

        notificationManager = NotificationManagerCompat.from(getContext());

        recibirNotificaciones.setOnPreferenceChangeListener((Preference preference, Object newValue) -> {
            if (newValue.equals(true)) {
                if (!notificationManager.areNotificationsEnabled()){ // Si las notificaciones no estan habiltitadas
                    getActivity().requestPermissions(permisos, MainActivity.PERMISO_NOTIFICACION_CFG); // Solicito
                    return false; // y no cambio la preferencia de valor
                }
                else createNotificationChannel(); // si estan, creo el canal de notificaciones
                return true; // cambio la pref de valor
            } else {
                // Eliminar canal de notificaciones de medicamentos
                notificationManager.deleteNotificationChannel(getString(R.string.channel_id));
                return true;
            }
        });

        tema.setOnPreferenceChangeListener((preference, newValue) ->
        {
            if(newValue.equals(true))
            {
                UiModeManager uiManager = (UiModeManager) getContext().getSystemService(Context.UI_MODE_SERVICE);
                if(BuildConfig.VERSION_CODE > 30) uiManager.setNightMode(UiModeManager.MODE_NIGHT_YES);
                else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                return true;
            }
            else
            {
                UiModeManager uiManager = (UiModeManager) getContext().getSystemService(Context.UI_MODE_SERVICE);
                if(BuildConfig.VERSION_CODE > 30) uiManager.setNightMode(UiModeManager.MODE_NIGHT_NO);
                else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                return true;
            }
        });

    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            String CHANNEL_ID = getString(R.string.channel_id);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getContext().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

}