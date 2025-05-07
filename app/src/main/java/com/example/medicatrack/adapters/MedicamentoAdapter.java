package com.example.medicatrack.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medicatrack.databinding.MedicamentoViewlistBinding;
import com.example.medicatrack.model.Medicamento;
import com.example.medicatrack.model.Registro;
import com.example.medicatrack.utilities.FechaFormat;
import com.example.medicatrack.utilities.ResourcesUtility;
import com.example.medicatrack.viewmodels.MedicamentoViewModel;

import java.util.ArrayList;
import java.util.List;

public class MedicamentoAdapter extends ListAdapter<Medicamento,MedicamentoAdapter.MedicamentoViewHolder>
{

    private MedicamentoViewModel viewModel;

    public MedicamentoAdapter(MedicamentoViewModel viewModel)
    {
        super(new MedicamentoDifference());
        this.viewModel = viewModel;
    }


    @NonNull
    @Override
    public MedicamentoAdapter.MedicamentoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        return new MedicamentoAdapter.MedicamentoViewHolder(MedicamentoViewlistBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MedicamentoAdapter.MedicamentoViewHolder holder, int position)
    {
        holder.bind(getCurrentList().get(position),viewModel);
    }

    @Override
    public int getItemCount()
    {
        return getCurrentList().size();
    }

    public void setData(ArrayList<Medicamento> medicamentos)
    {
        submitList((List<Medicamento>) medicamentos.clone());
    }

    //Clase Viewholder - se encarga de mostrar los datos en la UI
    public static class MedicamentoViewHolder extends RecyclerView.ViewHolder
    {
        private MedicamentoViewlistBinding binding;

        public MedicamentoViewHolder(@NonNull MedicamentoViewlistBinding binding)
        {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Medicamento medicamento, MedicamentoViewModel viewModel)
        {
            binding.nombreTextView.setText(medicamento.getNombre());
            binding.frecuenciaTextView.setText(ResourcesUtility.enumToText(medicamento.getFrecuencia()));
            binding.tipoMedicamentoTextView.setText(ResourcesUtility.enumToText(medicamento.getForma()) + " de " +
                    String.format("%.2f", medicamento.getConcentracion()) + " " + ResourcesUtility.enumToText(medicamento.getUnidad()));
            binding.medicamentoImage.setImageResource(ResourcesUtility.getMedicamentoImage(medicamento));
            binding.getRoot().setOnClickListener(view ->
            {
                //Abrir
                viewModel.navegarInfo.setValue(true);
                viewModel.medicamentoSeleccionado = medicamento;
            });

            binding.infoButton.setOnClickListener(view ->
            {
                viewModel.navegarInfo.setValue(true);
                viewModel.medicamentoSeleccionado = medicamento;
            });
        }
    }


    //Obtiene las diferencias entre 2 listas distintas
    public static class MedicamentoDifference extends DiffUtil.ItemCallback<Medicamento>
    {

        @Override
        public boolean areItemsTheSame(@NonNull Medicamento oldItem, @NonNull Medicamento newItem) {
            return oldItem.equals(newItem);
        }

        @Override
        public boolean areContentsTheSame(@NonNull Medicamento oldItem, @NonNull Medicamento newItem) {
            return oldItem.equals(newItem) || (oldItem.getNombre().equals(newItem.getNombre())
                    && oldItem.getFrecuencia().equals(newItem.getFrecuencia())
                    && oldItem.getConcentracion() == newItem.getConcentracion() && oldItem.getForma().equals(newItem.getForma()));
        }
    }
}
