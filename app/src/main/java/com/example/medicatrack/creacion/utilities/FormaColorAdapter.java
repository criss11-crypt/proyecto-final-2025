package com.example.medicatrack.creacion.utilities;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medicatrack.model.enums.Color;
import com.example.medicatrack.model.enums.Forma;
import com.example.medicatrack.creacion.viewmodels.CreacionViewModel;
import com.example.medicatrack.databinding.CardColorFormaBinding;

import java.util.List;

public class FormaColorAdapter extends RecyclerView.Adapter<FormaColorAdapter.FormaColorViewHolder>{

    private List<Forma> listaForma;
    private List<Color> listaColor;
    private Fragment fragment; // Referencia del fragmento para muchas cosas, entre ellas, poder obtener el view model de la actividad
    private CreacionViewModel viewModel; // para poder setear el valor de Color y Forma, y que le anuncie a quienes esten "observando"

    // Constructor
    // Para la forma
    public FormaColorAdapter(List<Forma> data, Fragment frag){
        fragment = frag;
        listaForma = data;
        viewModel = new ViewModelProvider(fragment.getActivity()).get(CreacionViewModel.class);
    }
    // Para el color
    public FormaColorAdapter(Fragment frag, List<Color> data){
        fragment = frag;
        listaColor = data;
        viewModel = new ViewModelProvider(fragment.getActivity()).get(CreacionViewModel.class);
    }

    // Holder
    public static class FormaColorViewHolder extends RecyclerView.ViewHolder{

        private CardColorFormaBinding bindingCard;

        public FormaColorViewHolder(CardColorFormaBinding bind) {
            super(bind.getRoot());
            bindingCard = bind;
        }
    }

    // Una vez por cada card que se visualiza. Obtenemos una vista y la infla
    @NonNull
    @Override
    public FormaColorAdapter.FormaColorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FormaColorViewHolder(CardColorFormaBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    // Por cada card, seteamos los datos
    @Override
    public void onBindViewHolder(@NonNull FormaColorAdapter.FormaColorViewHolder holder, int position) {

        // Setear texto
        String textoMayus = listaForma != null ? listaForma.get(position).toString() : listaColor.get(position).toString();
        String texto = Utilities.fisrtUpperOnly(textoMayus);
        holder.bindingCard.txtCard.setText(texto);

        // Setear imagen
        Drawable imagenDra = null;
        if(listaForma != null){ // Es una forma
            String str = "@drawable/"+texto.toLowerCase()+"_generica";
            int imageResource = fragment.getResources().getIdentifier(str, null, fragment.getContext().getPackageName());
            imagenDra = ContextCompat.getDrawable(fragment.getContext(), imageResource);
        }else{ // Es un color
            String str = "@drawable/"+texto.toLowerCase();
            int imageResource = fragment.getResources().getIdentifier(str, null, fragment.getContext().getPackageName());
            imagenDra = ContextCompat.getDrawable(fragment.getContext(), imageResource);
        }
        holder.bindingCard.imgCard.setImageDrawable(imagenDra);

        // Listener para la card. Cada vez que se presiona, modificamos el nombre de la toolbar (seteando el valor en el ViewModel, y
        // como la actividad esta suscrita a los cambios, se modifica desde alli)
        holder.bindingCard.card.setOnClickListener(view -> {
            if(listaForma != null) viewModel.setForma(texto);
            else viewModel.setColor(texto);
            if(listaColor != null && viewModel.getForma().getValue() == null){
                holder.bindingCard.card.setChecked(false); // Si toco un color, y la forma es nula, no se chequea
                viewModel.setColor(null);
            }
            else holder.bindingCard.card.setChecked(true); // sino, siempre chequeo la card
        });

        // Suscribo al metodo observe, para que cada vez que se cambie el color o la forma, ejecute lo siguiente
        viewModel.getForma().observe(fragment, forma -> {
            if(holder.bindingCard.card.isChecked() // Si la card (sea de Color o Forma) esta checked
                    && !forma.equals(holder.bindingCard.txtCard.getText().toString())) { // y el valor nuevo de forma es distinto que el de la card
                holder.bindingCard.card.setChecked(false); // La deschequeo
                viewModel.setColor(null); // Siempre, al modificar la forma, se deschequea el color. Actualizo el valor a null.
            }
        });
        viewModel.getColor().observe(fragment, color -> {
            if(holder.bindingCard.card.isChecked()  // Si la card (sea de Color o Forma) esta checked
                    && listaColor != null // Aca me aseguro que la card sea de Color
                    && !color.equals(holder.bindingCard.txtCard.getText().toString()) // el valor nuevo de color es distinto al de la card
                    && color != null)
                holder.bindingCard.card.setChecked(false); // La deschequeo
        });

    }

    @Override
    public int getItemCount() {
        return listaForma != null ? listaForma.size() : listaColor.size();
    }
}
