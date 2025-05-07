package com.example.medicatrack;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.SharedElementCallback;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.transition.Transition;
import androidx.transition.TransitionInflater;

import com.example.medicatrack.adapters.MainContentAdapter;
import com.example.medicatrack.databinding.MedicamentoInfoBinding;
import com.example.medicatrack.model.Medicamento;
import com.example.medicatrack.model.Registro;
import com.example.medicatrack.model.enums.Frecuencia;
import com.example.medicatrack.model.enums.RegistroEstado;
import com.example.medicatrack.repo.MedicamentoRepository;
import com.example.medicatrack.repo.RegistroRepository;
import com.example.medicatrack.utilities.FechaFormat;
import com.example.medicatrack.utilities.ResourcesUtility;
import com.example.medicatrack.viewmodels.MedicamentoViewModel;
import com.google.android.material.tabs.TabLayoutMediator;

import java.time.DayOfWeek;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class MedicamentoInfoFragment extends Fragment {

    private MedicamentoInfoBinding binding;

    private MedicamentoViewModel viewModel;

    private RegistroRepository repo;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = MedicamentoInfoBinding.inflate(inflater, container, false);

        return binding.getRoot();

    }

    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(MedicamentoViewModel.class);
        repo = RegistroRepository.getInstance(requireContext());

        ZonedDateTime ahora = ZonedDateTime.now(ZoneId.of("America/Argentina/Buenos_Aires"));

        Medicamento medicamento = viewModel.medicamentoSeleccionado;

        List<Registro> registros = new ArrayList<>();
        repo.getAllFrom(medicamento.getId(),(result, values) ->
        {
            if(result) registros.addAll(values);
        });

        //Setear data
        binding.iconMedicamento.setImageResource(ResourcesUtility.getMedicamentoImage(medicamento));
        binding.nameText.setText(medicamento.getNombre());
        binding.descripcion.setText(medicamento.getDescripcion() != null ? medicamento.getDescripcion() : "Sin descripción");
        binding.concentracionValor.setText(String.format("%.2f", medicamento.getConcentracion()) + " " + ResourcesUtility.enumToText(medicamento.getUnidad()));
        binding.formaValor.setText(ResourcesUtility.enumToText(medicamento.getForma()));
        binding.colorValor.setText(ResourcesUtility.enumToText(medicamento.getColor()));
        binding.frecuenciaValor.setText(ResourcesUtility.enumToText(medicamento.getFrecuencia()));

        List<Registro> registrosTomados = registros.stream().filter(registro -> registro.getEstado().equals(RegistroEstado.CONFIRMADO)).collect(Collectors.toList());
        binding.totalValor.setText(registrosTomados.isEmpty() ? "0" : "" + registrosTomados.size());

        if(registrosTomados.isEmpty()) binding.ultimoValor.setText("Nunca");
        else {  //Asume que el ultimo registro es el mas reciente. Se lo podria confirmar en el DAO ordenando ascendente por long? <-- Sii!
            long diasUltimoTomado = ChronoUnit.DAYS.between(registrosTomados.get(registrosTomados.size() - 1).getFecha(), ahora);
            binding.ultimoValor.setText(diasUltimoTomado > 1 ? "Hace " + diasUltimoTomado + " días" : diasUltimoTomado == 0 ? "Hoy" : "Ayer");
        }

        if(medicamento.getFrecuencia().equals(Frecuencia.NECESIDAD))
        {
            binding.proximoValor.setText("A elección");
            binding.horaValor.setText("A elección");
            binding.comienzoValor.setText("A elección");
        }

        //Proximo consumo
        else if(medicamento.getFrecuencia().equals(Frecuencia.INTERVALO_REGULAR))
        {
            int frecuencia = Integer.parseInt(medicamento.getDias());
            if(frecuencia == 1) binding.proximoValor.setText("Cada día"); else binding.proximoValor.setText("Cada " + frecuencia + " días");
            binding.horaValor.setText(FechaFormat.formattedHora(medicamento.getHora()));
            binding.comienzoValor.setText(medicamento.getFechaInicio().getDayOfMonth() + "/"
                    + medicamento.getFechaInicio().getMonthValue() + "/" + medicamento.getFechaInicio().getYear());


        }
        else if(medicamento.getFrecuencia().equals(Frecuencia.DIAS_ESPECIFICOS))
        {
            List<DayOfWeek> diasSemana = FechaFormat.toDiasSemana(medicamento.getDias());
            if(diasSemana.size() == 7) binding.proximoValor.setText("Cada día");
            else
            {
                StringBuilder texto = new StringBuilder();
                for(int i = 0; i < diasSemana.size(); i++)
                {
                    if(i == 0) texto.append(ResourcesUtility.enumToText(diasSemana.get(i)));
                    else
                    {
                        texto.append("\n").append(ResourcesUtility.enumToText(diasSemana.get(i)));
                    }
                }
                binding.proximoValor.setText(texto);
                binding.horaValor.setText(FechaFormat.formattedHora(medicamento.getHora()));
                binding.comienzoValor.setText(medicamento.getFechaInicio().getDayOfMonth() + "/"
                        + medicamento.getFechaInicio().getMonthValue() + "/" + medicamento.getFechaInicio().getYear());
            }
        }
        else if(medicamento.getFrecuencia().equals(Frecuencia.TODOS_DIAS))
        {
            binding.proximoValor.setText("Cada día");
            binding.horaValor.setText(FechaFormat.formattedHora(medicamento.getHora()));
            binding.comienzoValor.setText(medicamento.getFechaInicio().getDayOfMonth() + "/"
                    + medicamento.getFechaInicio().getMonthValue() + "/" + medicamento.getFechaInicio().getYear());
        }

        // Comportamiento boton ELIMINAR
        binding.deleteButton.setOnClickListener(v -> {

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("ELIMINAR")
                    .setMessage("¿Desea eliminar el medicamento y todos sus registros?")
                    .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            MedicamentoRepository.getInstance(getContext()).delete(medicamento, result -> {
                                // Medicamento eliminado
                                if(result){
                                    System.out.println("Medicamento " + medicamento.getNombre() + " eliminado.");
                                    Toast.makeText(getActivity().getApplicationContext(), "Se ha eliminado el medicamento.", Toast.LENGTH_LONG).show();
                                }
                            });
                            RegistroRepository.getInstance(getContext()).deleteAllFromWhere(medicamento.getId(), result -> {
                                // Registros del medicamento eliminados
                                if(result) System.out.println("Registros del medicamento " + medicamento.getNombre() + " eliminados.");
                            });
                            if(viewModel.nuevoMedicamento.getValue() != null && viewModel.nuevoMedicamento.getValue().equals(medicamento)) viewModel.nuevoMedicamento.setValue(null);
                            NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment_content_main);
                            navController.navigateUp();
                        }
                    })
                    .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    });
            builder.create().show();

        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
