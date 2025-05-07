package com.example.medicatrack.creacion.utilities;

import android.text.Editable;
import android.text.TextWatcher;

public class Utilities {
    public abstract static class TextWatcherExtender implements TextWatcher
    {

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public abstract void afterTextChanged(Editable editable);
    }

    public static String fisrtUpperOnly(String str) {
        return str.toUpperCase().charAt(0) + str.substring(1).toLowerCase();
    }


    public static Long getMiliseconds(int hora, int minutos){
        return Long.parseLong(String.valueOf((hora*3600000) + (minutos*60000) + 10800000));
        // +3 horas para que coincida la hora seleccionada con la hora guardada (ARG - UTC-3)
    }

}
