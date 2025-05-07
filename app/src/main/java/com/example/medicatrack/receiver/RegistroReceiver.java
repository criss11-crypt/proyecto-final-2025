package com.example.medicatrack.receiver;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;


import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.medicatrack.MainActivity;
import com.example.medicatrack.R;
import com.example.medicatrack.model.Medicamento;

import com.example.medicatrack.model.Registro;
import com.example.medicatrack.model.enums.RegistroEstado;
import com.example.medicatrack.repo.MedicamentoRepository;
import com.example.medicatrack.repo.RegistroRepository;
import com.example.medicatrack.service.MedicamentoService;
import com.example.medicatrack.utilities.ResourcesUtility;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;


public class RegistroReceiver extends BroadcastReceiver {

    public static final String NOTIFICAR = "com.example.medicatrack.receiver.EVENTO_NOTIFICAR";
    public static final String REGISTRAR_TOMADO = "com.example.medicatrack.receiver.EVENTO_REGISTRAR_TOMADO";
    public static final String REGISTRAR_NO_TOMADO = "com.example.medicatrack.receiver.EVENTO_REGISTRAR_NO_TOMADO";

    public static final String REGISTRAR_PENDIENTE = "com.example.medicatrack.receiver.EVENTO_REGISTRAR_PENDIENTE";

    public static final String REGISTRAR = "com.example.medicatrack.receiver.EVENTO_REGISTRAR";
    public static final String NUEVA_ALARMA = "com.example.medicatrack.receiver.EVENTO_NUEVA_ALARMA";


    @Override
    public void onReceive(Context context, Intent intent) {

        switch (intent.getAction()) {
            case NOTIFICAR: {
                Medicamento medicamento = intent.getExtras().getParcelable("Medicamento");

                // Ver si esta en la bd (no fue eliminado)
                MedicamentoRepository.getInstance(context).getById(medicamento.getId(), ((result, value) -> {
                    if(result && value != null)
                    {
                        Registro registro = intent.getExtras().getParcelable("Registro");

                        // Notificacion
                        String CHANNEL_ID = context.getString(R.string.channel_id);
                        int _id = UUID.randomUUID().hashCode();

                        int requestCodeActivity = UUID.randomUUID().hashCode();
                        int requestCodeServiceTomado = UUID.randomUUID().hashCode();
                        int requestCodeServiceNoTomado = UUID.randomUUID().hashCode();
                        //Todos los requestCode deben ser distintos para que no se sobreescriban mutuamente

                        // Actividad de destino
                        Intent destinoAct = new Intent(context, MainActivity.class); // CAMBIAR POR LA QUE HAYA CREADO EL EZE
                        destinoAct.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        destinoAct.setAction(REGISTRAR); // Con esta action se va a preguntar por el intent
                        destinoAct.putExtra("Medicamento", medicamento); // Se pasa el medicamento a registrar
                        destinoAct.putExtra("Registro", registro); // Se pasa el registro

                        PendingIntent pendingIntentAct = PendingIntent.getActivity(context, requestCodeActivity, destinoAct, PendingIntent.FLAG_IMMUTABLE);
                        // ---------------------

                        // MODIFICO EL REGISTRO PENDIENTE A POSPUESTO (todavia no se confirmo ni cancelo)
                        registro.setEstado(RegistroEstado.POSPUESTO);
                        RegistroRepository.getInstance(context).update(registro, resultado -> {
                            if (resultado)
                                System.out.println("Registro (estado = POSPUESTO) actualizado para el medicamento " + medicamento.getNombre());
                        });
                        // ---------------------------

                        // Para el boton de TOMADO
                        Intent btnTomado = new Intent(context, MedicamentoService.class);
                        btnTomado.setAction(REGISTRAR_TOMADO);
                        btnTomado.putExtra("Registro", registro); // Se pasa el registro creado

                        btnTomado.putExtra("idNot", _id);
                        PendingIntent piBtnTomado = PendingIntent.getService(context, requestCodeServiceTomado, btnTomado, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
                        // ---------------------

                        // Para el boton de NO TOMADO
                        Intent btnNoTomado = new Intent(context, MedicamentoService.class);
                        btnNoTomado.setAction(REGISTRAR_NO_TOMADO);
                        btnNoTomado.putExtra("Registro", registro); // Se pasa el registro creado

                        btnNoTomado.putExtra("idNot", _id);
                        PendingIntent piBtnNoTomado = PendingIntent.getService(context, requestCodeServiceNoTomado, btnNoTomado, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
                        // ---------------------

                        // Crear notificacion
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                                .setSmallIcon(R.drawable.pastillas_default)
                                .setLargeIcon(getImageMed(medicamento, context))
                                .setContentTitle("Recordatorio")
                                .setContentText("Debes consumir: " + medicamento.getNombre() + " • " + medicamento.getConcentracion() + " " + ResourcesUtility.enumToText(medicamento.getUnidad()))
                                .setAutoCancel(true)
                                .setPriority(NotificationCompat.PRIORITY_HIGH)
                                .setContentIntent(pendingIntentAct)
                                .addAction(R.drawable.pastillas_default, "Tomado", piBtnTomado)
                                .addAction(R.drawable.pastillas_default, "No tomado", piBtnNoTomado);
                        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);

                        // ---------------------------
                        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return;
                        }
                        notificationManagerCompat.notify(_id, builder.build()); // el UUID.randomUUID().hashCode() es para que coloque distintos id, y se agrupen las notificaciones
                        // ---------------------

                        // Crear siguiente alarma
                        Intent intentAlarma = new Intent(context, MedicamentoService.class);
                        intentAlarma.setAction(NUEVA_ALARMA);
                        intentAlarma.putExtra("Medicamento", medicamento);
                        context.startService(intentAlarma);
                        // ---------------------
                    }
                }));

            }
            break;
            case Intent.ACTION_BOOT_COMPLETED:
            {
                // Cuando el dispositivo se termine de encender, hay que reestablecer todas las alarmas (porque se borran)
                // TODO
            }
            break;
        }
    }

    private Bitmap getImageMed(Medicamento medicamento, Context context) {

        int img = 0;

        switch (medicamento.getForma()) {
            case CREMA: {
                switch (medicamento.getColor()) {
                    case AZUL:
                        img = R.drawable.crema_azul;
                        break;
                    case ROJO:
                        img = R.drawable.crema_rojo;
                        break;
                    case VERDE:
                        img = R.drawable.crema_verde;
                        break;
                    case CELESTE:
                        img = R.drawable.crema_celeste;
                        break;
                    case NARANJA:
                        img = R.drawable.crema_naranja;
                        break;
                    case AMARILLO:
                        img = R.drawable.crema_amarillo;
                        break;
                }
            }
            break;
            case LIQUIDO: {
                switch (medicamento.getColor()) {
                    case AZUL:
                        img = R.drawable.liquido_azul;
                        break;
                    case ROJO:
                        img = R.drawable.liquido_rojo;
                        break;
                    case VERDE:
                        img = R.drawable.liquido_verde;
                        break;
                    case CELESTE:
                        img = R.drawable.liquido_celeste;
                        break;
                    case NARANJA:
                        img = R.drawable.liquido_naranja;
                        break;
                    case AMARILLO:
                        img = R.drawable.liquido_amarillo;
                        break;
                }
            }
            break;
            case PILDORA: {
                switch (medicamento.getColor()) {
                    case AZUL:
                        img = R.drawable.pildora_azul;
                        break;
                    case ROJO:
                        img = R.drawable.pildora_rojo;
                        break;
                    case VERDE:
                        img = R.drawable.pildora_verde;
                        break;
                    case CELESTE:
                        img = R.drawable.pildora_celeste;
                        break;
                    case NARANJA:
                        img = R.drawable.pildora_naranja;
                        break;
                    case AMARILLO:
                        img = R.drawable.pildora_amarillo;
                        break;
                }
            }
            break;
            case REDONDO: {
                switch (medicamento.getColor()) {
                    case AZUL:
                        img = R.drawable.redondo_azul;
                        break;
                    case ROJO:
                        img = R.drawable.redondo_rojo;
                        break;
                    case VERDE:
                        img = R.drawable.redondo_verde;
                        break;
                    case CELESTE:
                        img = R.drawable.redondo_celeste;
                        break;
                    case NARANJA:
                        img = R.drawable.redondo_naranja;
                        break;
                    case AMARILLO:
                        img = R.drawable.redondo_amarillo;
                        break;
                }
            }
            break;
            case PASTILLA: {
                switch (medicamento.getColor()) {
                    case AZUL:
                        img = R.drawable.pastilla_azul;
                        break;
                    case ROJO:
                        img = R.drawable.pastilla_rojo;
                        break;
                    case VERDE:
                        img = R.drawable.pastilla_verde;
                        break;
                    case CELESTE:
                        img = R.drawable.pastilla_celeste;
                        break;
                    case NARANJA:
                        img = R.drawable.pastilla_naranja;
                        break;
                    case AMARILLO:
                        img = R.drawable.pastilla_amarillo;
                        break;
                }
            }
            break;
        }

        Drawable drawable = ContextCompat.getDrawable(context, img);
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);

        return bitmap;
    }
}
