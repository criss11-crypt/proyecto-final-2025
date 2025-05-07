package com.example.medicatrack.service;

import static android.app.AlarmManager.INTERVAL_DAY;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.view.View;

import androidx.core.app.NotificationManagerCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.medicatrack.MainActivity;
import com.example.medicatrack.model.Medicamento;
import com.example.medicatrack.model.Registro;
import com.example.medicatrack.model.enums.RegistroEstado;
import com.example.medicatrack.receiver.RegistroReceiver;
import com.example.medicatrack.repo.RegistroRepository;
import com.example.medicatrack.viewmodels.RegistroViewModel;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.UUID;

public class MedicamentoService extends IntentService {

    public MedicamentoService() {
        super("MedicamentoService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) switch (intent.getAction()){
            case RegistroReceiver.REGISTRAR_TOMADO:
            case RegistroReceiver.REGISTRAR_NO_TOMADO: {
                actualizarRegistro(intent.getParcelableExtra("Registro"), intent.getIntExtra("idNot",-1), intent.getAction());
            } break;
            case RegistroReceiver.NUEVA_ALARMA: {
                crearAlarmaYRegistroPendiente(intent.getParcelableExtra("Medicamento"));
            } break;
        }
        stopSelf(); // Detener el service
    }

    private void actualizarRegistro(Registro registro, int idNotificacion, String action) {

        if(action.equals(RegistroReceiver.REGISTRAR_TOMADO)) registro.setEstado(RegistroEstado.CONFIRMADO);
        else if(action.equals(RegistroReceiver.REGISTRAR_NO_TOMADO)) registro.setEstado(RegistroEstado.CANCELADO);
        //registro.setFecha(ZonedDateTime.now());
        registro.setFecha(ZonedDateTime.now(ZoneId.of("America/Argentina/Buenos_Aires")));

        RegistroRepository.getInstance(getApplicationContext()).update(registro, result -> {
            if(result){
                System.out.println("Registro para el medicamento " + registro.getMedicamento().getNombre() + " asentado (estado = " + registro.getEstado() + ") a la hora y fecha: " + registro.getFecha());
                NotificationManagerCompat.from(getApplicationContext()).cancel(null,idNotificacion); // Cerrar notificacion
            }

        });


    }

    private void crearAlarmaYRegistroPendiente(Medicamento medicamento) {

        Intent intentAlarma = new Intent(getApplicationContext(), RegistroReceiver.class);
        intentAlarma.putExtra("Medicamento", medicamento);
        intentAlarma.setAction(RegistroReceiver.NOTIFICAR);

        // Registro pendiente
        Registro registro = crearRegistro(medicamento);
        intentAlarma.putExtra("Registro", registro);

        // Alarma
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        int _id = UUID.randomUUID().hashCode();
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), _id, intentAlarma, PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("America/Argentina/Buenos_Aires"));
        calendar.set(Calendar.HOUR_OF_DAY, medicamento.getHora().getHour());
        calendar.set(Calendar.MINUTE, medicamento.getHora().getMinute());

        switch (medicamento.getFrecuencia()) {
            case TODOS_DIAS: calendar.setTimeInMillis(System.currentTimeMillis()+INTERVAL_DAY); break;
            case INTERVALO_REGULAR: calendar.setTimeInMillis(System.currentTimeMillis()+(INTERVAL_DAY*Integer.parseInt(medicamento.getDias()))); break;
            case DIAS_ESPECIFICOS: calendar.setTimeInMillis(System.currentTimeMillis()+(7*INTERVAL_DAY)); break;
        }
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);

        System.out.println("NUEVA ALARMA (y registro pendiente) DE " + medicamento.getNombre() + " PROGRAMADA PARA: " + calendar.getTime());
    }

    private Registro crearRegistro(Medicamento medicamento) {

        Registro registro = new Registro(UUID.randomUUID());
        registro.setMedicamento(medicamento);

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("America/Argentina/Buenos_Aires"));
        calendar.set(Calendar.HOUR_OF_DAY, medicamento.getHora().getHour());
        calendar.set(Calendar.MINUTE, medicamento.getHora().getMinute());

        switch (medicamento.getFrecuencia()) {
            case TODOS_DIAS: calendar.setTimeInMillis(System.currentTimeMillis()+INTERVAL_DAY); break;
            case INTERVALO_REGULAR: calendar.setTimeInMillis(System.currentTimeMillis()+(INTERVAL_DAY*Integer.parseInt(medicamento.getDias()))); break;
            case DIAS_ESPECIFICOS: calendar.setTimeInMillis(System.currentTimeMillis()+(7*INTERVAL_DAY)); break;
        }

        registro.setFecha(ZonedDateTime.ofInstant(Instant.ofEpochMilli(calendar.getTimeInMillis()), ZoneId.of("America/Argentina/Buenos_Aires")));

        registro.setEstado(RegistroEstado.PENDIENTE);

        RegistroRepository.getInstance(getApplicationContext()).insert(registro, result -> {

        });
        return registro;
    }


}