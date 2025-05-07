package com.example.medicatrack;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.medicatrack.adapters.MainContentAdapter;
import com.example.medicatrack.databinding.FragmentMainBinding;
import com.example.medicatrack.viewmodels.MedicamentoViewModel;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MainFragment extends Fragment {

    private FragmentMainBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {

        binding = FragmentMainBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        MedicamentoViewModel viewModel = new ViewModelProvider(requireActivity()).get(MedicamentoViewModel.class);

        binding.viewpager.setUserInputEnabled(false);

        binding.viewpager.setAdapter(new MainContentAdapter(this,viewModel));

        new TabLayoutMediator(binding.tabLayout, binding.viewpager,
                (tab, position) -> tab.setText(position == 0 ? "Registro" : "Mis medicamentos")
        ).attach();

        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                                                       @Override
                                                       public void onTabSelected(TabLayout.Tab tab)
                                                       {
                                                           if(tab.getPosition() == 1) viewModel.activarFab.setValue(true);
                                                           else viewModel.activarFab.setValue(false);
                                                       }

                                                       @Override
                                                       public void onTabUnselected(TabLayout.Tab tab) {

                                                       }

                                                       @Override
                                                       public void onTabReselected(TabLayout.Tab tab) {

                                                       }
                                                   }
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}