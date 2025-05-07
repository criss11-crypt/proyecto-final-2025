package com.example.medicatrack.creacion;

import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.example.medicatrack.R;
import com.example.medicatrack.model.enums.Color;
import com.example.medicatrack.model.enums.Forma;
import com.example.medicatrack.model.enums.Unidad;
import com.example.medicatrack.creacion.utilities.FormaColorAdapter;
import com.example.medicatrack.creacion.utilities.Utilities;
import com.example.medicatrack.creacion.viewmodels.CreacionViewModel;
import com.example.medicatrack.databinding.FragmentDatosMedicamentoBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DatosMedicamentoFragment extends Fragment {

    private FragmentDatosMedicamentoBinding binding;
    private CreacionViewModel viewModel;

    // Recycler
    private RecyclerView recyclerViewForma;
    private RecyclerView recyclerViewColor;
    private RecyclerView.Adapter adapterForma;
    private RecyclerView.Adapter adapterColor;
    private RecyclerView.LayoutManager layoutManager, layoutManager2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(getActivity()).get(CreacionViewModel.class);

        // Observando ante el cambio de forma
        viewModel.getForma().observe(this, forma -> {
            String str = "@drawable/"+forma.toLowerCase()+"_generica";
            int imageResource = getResources().getIdentifier(str, null, getActivity().getPackageName());
            Drawable imagenDra = ContextCompat.getDrawable(getContext(), imageResource);
            binding.imgDefault.setImageDrawable(imagenDra);
            binding.errForma.setVisibility(View.GONE);
            if(!binding.errColor.getText().equals("Obligatorio")) binding.errColor.setVisibility(View.GONE); // siempre que se seleccione una forma, desaparece el label del error "Seleccione una forma"
        });

        // Observando ante el cambio de color
        viewModel.getColor().observe(this, color -> {
            if(viewModel.getForma().getValue() == null) { // Si no hay forma seleccionada
                binding.errColor.setText("Seleccione una forma");
                binding.errColor.setVisibility(View.VISIBLE);
            }else if(color != null){
                binding.errColor.setVisibility(View.GONE);
                binding.errColor.setText(R.string.obligatorio);
                String str = "@drawable/"+viewModel.getForma().getValue().toLowerCase()+"_"+color.toLowerCase();
                int imageResource = getResources().getIdentifier(str, null, getActivity().getPackageName());
                Drawable imagenDra = ContextCompat.getDrawable(getContext(), imageResource);
                binding.imgDefault.setImageDrawable(imagenDra);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDatosMedicamentoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Cambio del nombre del medicamento
        binding.editNombre.addTextChangedListener(new Utilities.TextWatcherExtender() {
            @Override
            public void afterTextChanged(Editable editable) {
                if(!binding.editNombre.getText().toString().equals("")){
                    binding.txtNombre.setText(binding.editNombre.getText().toString());
                    viewModel.setNombreMed(binding.editNombre.getText().toString());
                    binding.errNombre.setVisibility(View.GONE);
                }else{
                    binding.txtNombre.setText("Nombre del medicamento");
                    viewModel.setNombreMed("Medicamento");
                }
            }
        });
        /* En caso de querer actualizarlo solo cuando se termine de escribir (i.e. pierda el foco)
        binding.editNombre.setOnFocusChangeListener((v, hasFocus) -> {
            if(!hasFocus && !binding.editNombre.getText().toString().equals("")){
                binding.txtNombre.setText(binding.editNombre.getText().toString());
                viewModel.setNombreMed(binding.editNombre.getText().toString());
                binding.errNombre.setVisibility(View.GONE);
            }else{
                binding.txtNombre.setText("Nombre del medicamento");
                viewModel.setNombreMed("Medicamento");
            }
        });
         */


        layoutManager = new LinearLayoutManager(this.getContext(),RecyclerView.HORIZONTAL,false);
        // Inicializar recycler forma
        recyclerViewForma = binding.recyclerViewForma;
        recyclerViewForma.setHasFixedSize(true);
        recyclerViewForma.setLayoutManager(layoutManager);
        adapterForma = new FormaColorAdapter(Arrays.stream(Forma.values()).collect(Collectors.toList()), this);
        recyclerViewForma.setAdapter(adapterForma);

        layoutManager2 = new LinearLayoutManager(this.getContext(),RecyclerView.HORIZONTAL,false);
        // Inicializar recycler color
        recyclerViewColor = binding.recyclerViewColor;
        recyclerViewColor.setHasFixedSize(true);
        recyclerViewColor.setLayoutManager(layoutManager2);
        adapterColor = new FormaColorAdapter(this, Arrays.stream(Color.values()).collect(Collectors.toList()));
        recyclerViewColor.setAdapter(adapterColor);


        // Inicializar dropdown unidades
        List<Unidad> unidades = Arrays.stream(Unidad.values()).collect(Collectors.toList());
        List<String> unidadesStr = new ArrayList<>();
        for (Unidad u: unidades) unidadesStr.add(u.toString() == "PORCENTAJE" ? "%" : u.toString().toLowerCase());
        ArrayAdapter<String> adapterUnidades = new ArrayAdapter<>(getContext(), R.layout.lista_dropdown, R.id.txtLista,unidadesStr);
        binding.dropUnidad.setAdapter(adapterUnidades);


        // Actualizo la concentracion y unidad del medicamento
        binding.editConcentracion.addTextChangedListener(new Utilities.TextWatcherExtender() {
            @Override
            public void afterTextChanged(Editable editable) {
                if(!binding.editConcentracion.getText().toString().equals("")){
                    viewModel.setConcentracion(Float.parseFloat(binding.editConcentracion.getText().toString()));
                    if(!binding.dropUnidad.getText().toString().equals("unidad")) binding.errConcentracion.setVisibility(View.GONE);
                }else viewModel.setConcentracion(null);
            }
        });

        binding.dropUnidad.setOnItemClickListener((parent, vista, position, id) -> {
            viewModel.setUnidad(binding.dropUnidad.getText().toString());
            if(!binding.editConcentracion.getText().toString().equals("")) binding.errConcentracion.setVisibility(View.GONE);
        });


        // Actualizo el valor de la descripcion
        binding.editDescripcion.addTextChangedListener(new Utilities.TextWatcherExtender() {
            @Override
            public void afterTextChanged(Editable editable) {
                viewModel.setDescripcion(binding.editDescripcion.getText().toString());
            }
        });


        // Boton siguiente, lleva al fragmento de FrecuenciaMedicamento
        binding.fab.setOnClickListener(v -> {
            if(verificarDatos()){
                NavHostFragment.findNavController(DatosMedicamentoFragment.this)
                        .navigate(R.id.action_datosMedicamentoFragment_to_frecuenciaMedicamentoFragment);
            }
        });

    }

    private boolean verificarDatos(){

        boolean correctos = true;

        if(binding.editNombre.getText().toString().equals("")){
            binding.errNombre.setVisibility(View.VISIBLE);
            correctos = false;
        }else binding.errNombre.setVisibility(View.GONE);

        if(viewModel.getForma().getValue() == null){
            binding.errForma.setVisibility(View.VISIBLE);
            correctos = false;
        }else binding.errForma.setVisibility(View.GONE);

        if(viewModel.getColor().getValue() == null){
            binding.errColor.setText(R.string.obligatorio);
            binding.errColor.setVisibility(View.VISIBLE);
            correctos = false;
        }else binding.errColor.setVisibility(View.GONE);

        if(binding.editConcentracion.getText().toString().equals("") || binding.dropUnidad.getText().toString().equals("unidad")){
            binding.errConcentracion.setVisibility(View.VISIBLE);
            correctos = false;
        }else binding.errConcentracion.setVisibility(View.GONE);

        return correctos;
    }

}