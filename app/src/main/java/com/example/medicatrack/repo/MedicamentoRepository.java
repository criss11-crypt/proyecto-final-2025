package com.example.medicatrack.repo;

import android.content.Context;

import com.example.medicatrack.model.Medicamento;
import com.example.medicatrack.repo.persist.impl.MedicamentoRoomDataSource;
import com.example.medicatrack.repo.persist.interfaces.CallbacksDataSource;
import com.example.medicatrack.repo.persist.interfaces.MedicamentoDataSource;

import java.util.UUID;

public class MedicamentoRepository implements MedicamentoDataSource
{
    private static MedicamentoRepository INSTANCE = null;
    private MedicamentoDataSource dataSource = null;        //Aca podrían haber muchas implementaciones (SQL, No SQL, etc)

    public synchronized static MedicamentoRepository getInstance(final Context context)
    {
        if(INSTANCE == null) INSTANCE = new MedicamentoRepository(context);
        return INSTANCE;
    }

    private MedicamentoRepository(final Context context)
    {
        dataSource = MedicamentoRoomDataSource.getInstance(context);
    }

    @Override
    public void insert(Medicamento medicamento, CallbacksDataSource.InsertCallback callback)
    {
        dataSource.insert(medicamento, callback);
    }

    @Override
    public void update(Medicamento medicamento, CallbacksDataSource.UpdateCallback callback)
    {
        dataSource.update(medicamento, callback);
    }

    @Override
    public void getById(UUID id, CallbacksDataSource.GetByIdCallback<Medicamento> callback)
    {
        dataSource.getById(id, callback);
    }

    @Override
    public void getAll(CallbacksDataSource.GetAllCallback<Medicamento> callback)
    {
        dataSource.getAll(callback);
    }

    @Override
    public void delete(Medicamento medicamento, CallbacksDataSource.DeleteCallback callback) {
        dataSource.delete(medicamento, callback);
    }
}
