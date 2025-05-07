package com.example.medicatrack.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.medicatrack.MedicamentosFragment;
import com.example.medicatrack.RegistroFragment;
import com.example.medicatrack.viewmodels.MedicamentoViewModel;

/* Se encarga de cambiar de Fragmento al mover entre Tabs */
public class MainContentAdapter extends FragmentStateAdapter
{
    MedicamentoViewModel viewModel;
    public MainContentAdapter(Fragment fragment, MedicamentoViewModel viewModel)
    {
        super(fragment);
        this.viewModel = viewModel;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position)
    {
       if(position == 0) {
           viewModel.activarFab.setValue(false);
           return new RegistroFragment();
       }
       else {
           viewModel.activarFab.setValue(true);
           return new MedicamentosFragment();
       }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
