package com.example.medicatrack.viewmodels;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.medicatrack.model.Medicamento;
import com.example.medicatrack.model.Registro;

import java.time.ZonedDateTime;
import java.util.List;

public class RegistroViewModel extends ViewModel
{
    //Obtiene los medicamentos a consumir en la fecha y crea registros de ser necesario
    public MutableLiveData<Registro> nuevoRegistro = new MutableLiveData<>();
}
