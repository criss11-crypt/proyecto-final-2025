package com.example.medicatrack.viewmodels;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.medicatrack.model.Medicamento;


public class MedicamentoViewModel extends ViewModel
{
    public MutableLiveData<Medicamento> nuevoMedicamento = new MutableLiveData<>();

    public Medicamento medicamentoSeleccionado;
    public MutableLiveData<Boolean> activarFab = new MutableLiveData<Boolean>();  //0 Nada, 1 Registro , 2 Medicamento, 3 Info

    public MutableLiveData<Boolean> navegarInfo = new MutableLiveData<Boolean>();


    public MedicamentoViewModel()
    {
        navegarInfo.setValue(false);
        activarFab.setValue(false);
    }


}
