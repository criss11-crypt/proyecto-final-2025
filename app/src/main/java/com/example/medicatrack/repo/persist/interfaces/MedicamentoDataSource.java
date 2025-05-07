package com.example.medicatrack.repo.persist.interfaces;

import com.example.medicatrack.model.Medicamento;

import java.util.UUID;

public interface MedicamentoDataSource
{
    void insert(Medicamento medicamento, CallbacksDataSource.InsertCallback callback);
    void update(Medicamento medicamento, CallbacksDataSource.UpdateCallback callback);
    void getById(UUID id, CallbacksDataSource.GetByIdCallback<Medicamento> callback);
    void getAll(CallbacksDataSource.GetAllCallback<Medicamento> callback);
    void delete(Medicamento medicamento, CallbacksDataSource.DeleteCallback callback);
}
