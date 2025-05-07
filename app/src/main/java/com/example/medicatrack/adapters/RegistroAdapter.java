package com.example.medicatrack.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.RotateAnimation;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medicatrack.R;
import com.example.medicatrack.databinding.RegistroViewlistBinding;
import com.example.medicatrack.model.Medicamento;
import com.example.medicatrack.model.Registro;
import com.example.medicatrack.model.enums.RegistroEstado;
import com.example.medicatrack.repo.RegistroRepository;
import com.example.medicatrack.utilities.FechaFormat;
import com.example.medicatrack.utilities.ResourcesUtility;
import com.google.android.material.button.MaterialButton;

import java.text.DateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;

public class RegistroAdapter extends ListAdapter<Registro,RegistroAdapter.RegistroViewHolder> {

    private Context context;

    public RegistroAdapter(Context context) {
        super(new RegistroDifference());
        this.context = context;
    }

    //Si esta seleccionado un chip
    private boolean esPasado = false;
    private boolean esFuturo = false;

    @NonNull
    @Override
    public RegistroViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RegistroViewHolder(RegistroViewlistBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false), context);
    }

    @Override
    public void onBindViewHolder(@NonNull RegistroViewHolder holder, int position) {
        holder.bind(getCurrentList().get(position), esPasado, esFuturo);
    }

    @Override
    public int getItemCount() {
        return getCurrentList().size();
    }

    public void setData(ArrayList<Registro> registros, boolean esPasado, boolean esFuturo)
    {
        this.esFuturo = esFuturo;
        this.esPasado = esPasado;

        submitList((List<Registro>) registros.clone());
    }

    //Clase Viewholder - se encarga de mostrar los datos en la UI
    public static class RegistroViewHolder extends RecyclerView.ViewHolder {
        private final RegistroViewlistBinding binding;
        private final RegistroRepository registroRepo;

        private int rotationAngle = 0;


        public RegistroViewHolder(@NonNull RegistroViewlistBinding binding, @NonNull Context context) {
            super(binding.getRoot());
            this.binding = binding;
            registroRepo = RegistroRepository.getInstance(context);
        }

        public void bind(Registro registro, boolean esPasado, boolean esFuturo) {
            Medicamento medicamento = registro.getMedicamento();
            binding.tiempoTextView.setText(FechaFormat.formattedHora(registro.getFecha()));

            if (registro.getEstado().equals(RegistroEstado.PENDIENTE)) {
                //start, top, end, bottom argumentos
                binding.tiempoTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.icon_bell_pending, 0, 0, 0);
            } else
                binding.tiempoTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0);

            binding.nombreTextView.setText(medicamento.getNombre());

            binding.iconMedicamento.setImageResource(ResourcesUtility.getMedicamentoImage(medicamento));

            StringBuilder dosis = new StringBuilder();
            dosis.append("Consumir ").append(String.format("%.2f", medicamento.getConcentracion())).append(" ")
                    .append(ResourcesUtility.enumToText(medicamento.getUnidad()));

            binding.dosisTextView.setText(dosis);

            setEstado(registro, esPasado);

            if (binding.buttonsLayout.getVisibility() == LinearLayoutCompat.VISIBLE) {
                binding.buttonsLayout.setVisibility(LinearLayoutCompat.GONE);
                rotationAngle = rotationAngle == 0 ? 180 : 0;  //toggle
                binding.arrow.animate().rotation(rotationAngle).setDuration(300).start();
            }

            binding.getRoot().setOnClickListener(view ->
            {
                if (!esFuturo) onClickOption(3, registro);
            });

            //Listeners
            binding.tomarButton.setOnClickListener(view -> {
                onClickOption(0, registro);
            });

            binding.noTomarButton.setOnClickListener(view -> {
                onClickOption(1, registro);
            });


        }

        public void onClickOption(int opcion, Registro registro)   //0 tomar, 1 no tomar, 2 pendiente
        {
            binding.buttonsLayout.setVisibility(binding.buttonsLayout.getVisibility() == LinearLayoutCompat.GONE ? LinearLayoutCompat.VISIBLE : LinearLayoutCompat.GONE);
            rotationAngle = rotationAngle == 0 ? 180 : 0;  //toggle
            binding.arrow.animate().rotation(rotationAngle).setDuration(300).start();
            if (opcion < 3) {
                updateRegistro(registro, opcion);
                setEstado(registro, false);
            }
        }

        public void updateRegistro(Registro registro, int opcion) {
            registro.setEstado(RegistroEstado.values()[opcion]);
            registroRepo.update(registro, result -> {
            });
        }

        public void setEstado(Registro registro, boolean esPasado) {
            switch (registro.getEstado()) {
                case CONFIRMADO:
                    binding.iconTomado.setImageResource(R.drawable.icon_check_checked);
                    binding.iconNoTomado.setImageResource(R.drawable.icon_close_unchecked);
                    binding.iconPendiente.setImageResource(R.drawable.icon_pending_unchecked);
                    binding.tomarButton.setVisibility(MaterialButton.GONE);
                    binding.noTomarButton.setVisibility(MaterialButton.VISIBLE);
                    binding.estadoTextView.setText("Tomado");
                    break;
                case CANCELADO:
                    binding.iconTomado.setImageResource(R.drawable.icon_check_unchecked);
                    binding.iconNoTomado.setImageResource(R.drawable.icon_close_checked);
                    binding.iconPendiente.setImageResource(R.drawable.icon_pending_unchecked);
                    binding.noTomarButton.setVisibility(MaterialButton.GONE);
                    binding.tomarButton.setVisibility(MaterialButton.VISIBLE);
                    binding.estadoTextView.setText("No tomado");
                    break;
                case PENDIENTE:
                    binding.iconTomado.setImageResource(R.drawable.icon_check_unchecked);
                    binding.iconNoTomado.setImageResource(R.drawable.icon_close_unchecked);
                    binding.iconPendiente.setImageResource(R.drawable.icon_pending_checked);
                    binding.tomarButton.setVisibility(MaterialButton.VISIBLE);
                    binding.noTomarButton.setVisibility(MaterialButton.VISIBLE);
                    if (esPasado) binding.estadoTextView.setText("Alarma perdida");
                    else binding.estadoTextView.setText("Por notificar");
                    break;

                case POSPUESTO:
                    binding.iconTomado.setImageResource(R.drawable.icon_check_unchecked);
                    binding.iconNoTomado.setImageResource(R.drawable.icon_close_unchecked);
                    binding.iconPendiente.setImageResource(R.drawable.icon_pending_checked);
                    binding.tomarButton.setVisibility(MaterialButton.VISIBLE);
                    binding.noTomarButton.setVisibility(MaterialButton.VISIBLE);
                    binding.estadoTextView.setText("Sin elección");
                    break;
            }
        }
    }


    //Obtiene las diferencias entre 2 listas distintas
    public static class RegistroDifference extends DiffUtil.ItemCallback<Registro> {

        @Override   //Instancias iguales?
        public boolean areItemsTheSame(@NonNull Registro oldItem, @NonNull Registro newItem) {
            return oldItem.equals(newItem);
        }

        @Override   //Valores iguales? (Que se ven en la UI)
        public boolean areContentsTheSame(@NonNull Registro oldItem, @NonNull Registro newItem) {
            return oldItem.getFecha().equals(newItem.getFecha()) && oldItem.getEstado().equals(newItem.getEstado()) && oldItem.getMedicamento().sameContent(newItem.getMedicamento());
        }
    }
}

