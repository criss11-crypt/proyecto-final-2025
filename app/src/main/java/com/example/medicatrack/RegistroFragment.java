package com.example.medicatrack;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medicatrack.adapters.RegistroAdapter;
import com.example.medicatrack.databinding.FragmentRegistroBinding;
import com.example.medicatrack.model.Medicamento;
import com.example.medicatrack.model.Registro;
import com.example.medicatrack.model.enums.Frecuencia;
import com.example.medicatrack.model.enums.RegistroEstado;
import com.example.medicatrack.repo.MedicamentoRepository;
import com.example.medicatrack.repo.RegistroRepository;
import com.example.medicatrack.utilities.FechaFormat;
import com.example.medicatrack.utilities.ResourcesUtility;
import com.example.medicatrack.viewmodels.MedicamentoViewModel;
import com.example.medicatrack.viewmodels.RegistroViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class RegistroFragment extends Fragment {

    private FragmentRegistroBinding binding;
    private RegistroRepository registroRepo;
    private MedicamentoRepository medicamentoRepo;
    private MedicamentoViewModel medicamentoViewModel;
    private final ArrayList<Registro> registros = new ArrayList<>();
    private final ArrayList<Medicamento> medicamentos = new ArrayList<>();
    private RegistroAdapter adapter;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState)
    {
        binding = FragmentRegistroBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        registroRepo = RegistroRepository.getInstance(requireContext());
        medicamentoRepo = MedicamentoRepository.getInstance(requireContext());
        medicamentoViewModel = new ViewModelProvider(requireActivity()).get(MedicamentoViewModel.class);
        RegistroViewModel registroViewModel = new ViewModelProvider(requireActivity()).get(RegistroViewModel.class);

        adapter = new RegistroAdapter(requireContext());

        binding.titleText.setText("Registro del día");

        binding.subtitleText.setText("Medicamentos a consumir");

        ZonedDateTime ahora = ZonedDateTime.now(ZoneId.of("America/Argentina/Buenos_Aires")).truncatedTo(ChronoUnit.DAYS);

        final ZonedDateTime[] fechaSeleccionada = new ZonedDateTime[1]; //Cuando se selecciona pasado o futuro
        fechaSeleccionada[0] = ahora;
        final boolean[] esPasado = new boolean[1];
        final boolean[] esFuturo = new boolean[1];

        binding.chipHoy.setChecked(true);
        setNuevosRegistros(ahora,esPasado[0],esFuturo[0]);

        binding.recyclerView.setAdapter(adapter);

        medicamentoRepo.getAll((result, values) ->
        {
            if(result) if(values.isEmpty()) binding.necesidadButton.setVisibility(MaterialButton.GONE);
            else binding.necesidadButton.setVisibility(MaterialButton.VISIBLE);
        });


        binding.chipAtras.setOnClickListener(view1 ->
        {

            CalendarConstraints.Builder constraints = new CalendarConstraints.Builder();
            constraints.setValidator(DateValidatorPointBackward.before(ahora.minusDays(3).toInstant().toEpochMilli()));
            MaterialDatePicker.Builder<Long> materialDateBuilder = MaterialDatePicker.Builder.datePicker();
            materialDateBuilder.setTitleText("Seleccione una fecha");
            materialDateBuilder.setSelection(ahora.minusDays(3).toInstant().toEpochMilli());
            materialDateBuilder.setCalendarConstraints(constraints.build());
            MaterialDatePicker<Long> picker = materialDateBuilder.build();
            picker.addOnPositiveButtonClickListener(selection ->
            {
                fechaSeleccionada[0] = ZonedDateTime.ofInstant(Instant.ofEpochMilli(selection),ZoneId.of("America/Argentina/Buenos_Aires"));
                fechaSeleccionada[0] = fechaSeleccionada[0].plusDays(1); //Por alguna razon la fecha devuelta esta un dia atrasada
                StringBuilder text = new StringBuilder()
                        .append(fechaSeleccionada[0].getDayOfMonth())
                        .append("/").append(fechaSeleccionada[0].getMonthValue()).append("/").append(fechaSeleccionada[0].getYear());
                binding.chipAtras.setText(text.toString());
                binding.chipMasAdelante.setText("Más adelante");
                esPasado[0] = true;
                esFuturo[0] = false;
                setNuevosRegistros(fechaSeleccionada[0],true,false);
                StringBuilder text2 = new StringBuilder().append("Registro del ").append(fechaSeleccionada[0].getDayOfWeek().name().toLowerCase()).append(" ").append(text);
                binding.titleText.setText(text2.toString());
                binding.subtitleText.setText("Medicamentos consumidos");

            });
            picker.show(getParentFragmentManager(),"Fecha");
        });

        binding.chipMasAdelante.setOnClickListener(view1 ->
        {
            CalendarConstraints.Builder constraints = new CalendarConstraints.Builder();
            constraints.setValidator(DateValidatorPointForward.from(ahora.plusDays(2).toInstant().toEpochMilli()));
            MaterialDatePicker.Builder<Long> materialDateBuilder = MaterialDatePicker.Builder.datePicker();
            materialDateBuilder.setTitleText("Seleccione una fecha");
            materialDateBuilder.setSelection(ahora.plusDays(3).toInstant().toEpochMilli());
            materialDateBuilder.setCalendarConstraints(constraints.build());
            MaterialDatePicker<Long> picker = materialDateBuilder.build();
            picker.addOnPositiveButtonClickListener(selection ->
            {
                fechaSeleccionada[0] = ZonedDateTime.ofInstant(Instant.ofEpochMilli(selection),ZoneId.of("America/Argentina/Buenos_Aires"));
                fechaSeleccionada[0] = fechaSeleccionada[0].plusDays(1); //Por alguna razon la fecha devuelta esta un dia atrasada
                StringBuilder text = new StringBuilder()
                        .append(fechaSeleccionada[0].getDayOfMonth())
                        .append("/").append(fechaSeleccionada[0].getMonthValue()).append("/").append(fechaSeleccionada[0].getYear());
                binding.chipMasAdelante.setText(text.toString());
                binding.chipAtras.setText("Más atrás");
                esPasado[0] = false;
                esFuturo[0] = true;
                setRegistrosFuturos(fechaSeleccionada[0]);
                StringBuilder text2 = new StringBuilder().append("Registro del ").append(ResourcesUtility.enumToText(fechaSeleccionada[0].getDayOfWeek())).append(" ").append(text);
                binding.titleText.setText(text2.toString());
                binding.subtitleText.setText("Medicamentos a consumir");

            });
            picker.show(getParentFragmentManager(),"Fecha");
        });

        binding.chipHoy.setOnClickListener(view1 ->
        {
            binding.titleText.setText("Registro del día");
            binding.subtitleText.setText("Medicamentos a consumir");
            binding.chipMasAdelante.setText("Más adelante");
            binding.chipAtras.setText("Más atrás");
            fechaSeleccionada[0] = ahora;
            esPasado[0] = false;
            esFuturo[0] = false;

            setNuevosRegistros(ahora,esPasado[0],esFuturo[0]);
        });

        binding.chipAyer.setOnClickListener(view1 ->
        {
            binding.titleText.setText("Registro de ayer");
            binding.subtitleText.setText("Medicamentos consumidos");
            binding.chipMasAdelante.setText("Más adelante");
            binding.chipAtras.setText("Más atrás");
            fechaSeleccionada[0] = ahora.minusDays(1);
            esPasado[0] = true;
            esFuturo[0] = false;
            setNuevosRegistros(fechaSeleccionada[0],esPasado[0],esFuturo[0]);
        });

        binding.chipAnteayer.setOnClickListener(view1 ->
        {
            binding.titleText.setText("Registro de anteayer");
            binding.subtitleText.setText("Medicamentos consumidos");
            binding.chipMasAdelante.setText("Más adelante");
            binding.chipAtras.setText("Más atrás");
            fechaSeleccionada[0] = ahora.minusDays(2);
            esPasado[0] = true;
            esFuturo[0] = false;
            setNuevosRegistros(fechaSeleccionada[0],esPasado[0],esFuturo[0]);
        });

        binding.chipManiana.setOnClickListener(view1 ->
        {
            binding.titleText.setText("Registro de mañana");
            binding.subtitleText.setText("Medicamentos a consumir");
            binding.chipMasAdelante.setText("Más adelante");
            binding.chipAtras.setText("Más atrás");
            fechaSeleccionada[0] = ahora.plusDays(1);
            esPasado[0] = false;
            esFuturo[0] = true;
            setRegistrosFuturos(fechaSeleccionada[0]);
        });

        binding.chipPasadoManiana.setOnClickListener(view1 ->
        {
            binding.titleText.setText("Registro de pasado mañana");
            binding.subtitleText.setText("Medicamentos a consumir");
            binding.chipMasAdelante.setText("Más adelante");
            binding.chipAtras.setText("Más atrás");

            fechaSeleccionada[0] = ahora.plusDays(2);
            esPasado[0] = false;
            esFuturo[0] = true;
            setRegistrosFuturos(fechaSeleccionada[0]);
        });

        medicamentoViewModel.nuevoMedicamento.observe(requireActivity(),medicamento ->
        {
            if(medicamento != null)
            {
                binding.necesidadButton.setVisibility(MaterialButton.VISIBLE);
                registroRepo.getAllFrom(medicamento.getId(),(result, values) ->
                {
                    if(result)
                    {
                        values.forEach(it ->
                        {
                            if(ChronoUnit.DAYS.between(it.getFecha(),fechaSeleccionada[0]) == 0)
                            {
                                if(esFuturo[0]) setRegistrosFuturos(fechaSeleccionada[0]);
                                else setNuevosRegistros(fechaSeleccionada[0],esPasado[0],esFuturo[0]);
                            }
                        });
                    }
                });
            }
        });

        registroViewModel.nuevoRegistro.observe(requireActivity(),registro ->
        {
            if(registro != null)
            {
                if(esFuturo[0]) setRegistrosFuturos(fechaSeleccionada[0]);
                else setNuevosRegistros(fechaSeleccionada[0],esPasado[0],esFuturo[0]);
                registroViewModel.nuevoRegistro.postValue(null);
            }
        });

        //Boton en necesidad
        binding.necesidadButton.setOnClickListener(view1 ->
        {

            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireActivity());
            ArrayList<Medicamento> todos = new ArrayList<>();
            todos.clear();

            medicamentoRepo.getAll((result, values) ->
            {
                if(result) todos.addAll(values);
            });

            int[] pos = new int[1];
            pos[0] = 0;

            builder.setTitle("Selecciona un medicamento");
            builder.setIcon(R.drawable.icon_medication);

            CharSequence[] lista = new CharSequence[todos.size()];
            for(int i = 0; i< lista.length; i++)
            {
                lista[i] = todos.get(i).getNombre();
            }

            builder.setSingleChoiceItems(lista,0,(dialogInterface, i) -> {pos[0] = i;});

            // Add the buttons
            builder.setPositiveButton("Añadir", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id)
                {
                    if(pos[0] < 0)
                    {
                        Toast toast = Toast.makeText(requireActivity(), "Debes seleccionar un medicamento", Toast.LENGTH_SHORT);
                        toast.show();
                        dialog.dismiss();
                    } else
                    {
                        Medicamento seleccionado = todos.get(pos[0]);
                        Registro nuevoRegistro = new Registro(UUID.randomUUID());
                        nuevoRegistro.setEstado(RegistroEstado.CONFIRMADO);
                        nuevoRegistro.setMedicamento(seleccionado);
                        nuevoRegistro.setFecha(ZonedDateTime.now(ZoneId.of("America/Argentina/Buenos_Aires")));
                        registroRepo.insert(nuevoRegistro,result -> {});
                        if(binding.chipHoy.isChecked()) setNuevosRegistros(ahora,false,false);
                        dialog.dismiss();
                    }
                }
            });
            builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id)
                {
                    // User cancelled the dialog
                }
            });

            builder.create().show();

        });

    }

    public void setNuevosRegistros(ZonedDateTime fecha, boolean esPasado, boolean esFuturo)
    {
        registros.clear();
        registroRepo.getAllFromDate(fecha,(result, values) ->
        {
            if(result) registros.addAll(values);
        });

/*        medicamentos.clear();
        medicamentos.addAll(registros.stream().map(Registro::getMedicamento).collect(Collectors.toList()));*/

        adapter.setData(registros,esPasado,esFuturo);

        if(registros.isEmpty()) {
            binding.recyclerView.setVisibility(RecyclerView.GONE);
            binding.layoutVacio.setVisibility(LinearLayoutCompat.VISIBLE);
        }
        else {
            binding.layoutVacio.setVisibility(LinearLayoutCompat.GONE);
            binding.recyclerView.setVisibility(RecyclerView.VISIBLE);
        }

    }

    public void setRegistrosFuturos(ZonedDateTime fecha) //Muestra registros futuros pero NO LOS GUARDA, de eso se encarga la alarma. Esto es solo para visualizar
    {
        medicamentos.clear();
        List<Medicamento> aux = new ArrayList<>();
        registros.clear();

        medicamentoRepo.getAll((result, values) ->
        {
            if(result) aux.addAll(values);
        });
        for (Medicamento medicamento : aux)
        {
            if(medicamento.getFrecuencia().equals(Frecuencia.NECESIDAD) || medicamento.getFechaInicio().isAfter(fecha)) continue;
            switch (medicamento.getFrecuencia())
            {
                case INTERVALO_REGULAR:
                    long dias = ChronoUnit.DAYS.between(medicamento.getFechaInicio(),fecha);
                    if(dias % Integer.parseInt(medicamento.getDias()) == 0)
                    {
                        Registro registro = new Registro(UUID.randomUUID());
                        registro.setMedicamento(medicamento);
                        registro.setEstado(RegistroEstado.PENDIENTE);
                        registro.setFecha(medicamento.getHora());
                        registros.add(registro);
                    }
                    break;

                case DIAS_ESPECIFICOS:
                    List<DayOfWeek> diasSemana = FechaFormat.toDiasSemana(medicamento.getDias());
                    if(diasSemana.contains(fecha.getDayOfWeek()))
                    {
                        Registro registro = new Registro(UUID.randomUUID());
                        registro.setMedicamento(medicamento);
                        registro.setEstado(RegistroEstado.PENDIENTE);
                        registro.setFecha(medicamento.getHora());
                        registros.add(registro);
                    }
                    break;

                case TODOS_DIAS:
                    Registro registro = new Registro(UUID.randomUUID());
                    registro.setMedicamento(medicamento);
                    registro.setEstado(RegistroEstado.PENDIENTE);
                    registro.setFecha(medicamento.getHora());
                    registros.add(registro);
                    break;
            }
        }

        adapter.setData(registros,false,true);

        if(registros.isEmpty()) {
            binding.recyclerView.setVisibility(RecyclerView.GONE);
            binding.layoutVacio.setVisibility(LinearLayoutCompat.VISIBLE);
        }
        else {
            binding.layoutVacio.setVisibility(LinearLayoutCompat.GONE);
            binding.recyclerView.setVisibility(RecyclerView.VISIBLE);
        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}