package com.example.medicatrack.repo;

import android.content.Context;

import com.example.medicatrack.model.Registro;
import com.example.medicatrack.model.enums.RegistroEstado;
import com.example.medicatrack.repo.persist.impl.RegistroRoomDataSource;
import com.example.medicatrack.repo.persist.interfaces.CallbacksDataSource;
import com.example.medicatrack.repo.persist.interfaces.RegistroDataSource;

import java.time.ZonedDateTime;
import java.util.UUID;

public class RegistroRepository implements RegistroDataSource
{
    private static RegistroRepository INSTANCE = null;
    private RegistroDataSource dataSource = null;        //Aca podrían haber muchas implementaciones (SQL, No SQL, etc)

    public synchronized static RegistroRepository getInstance(final Context context)
    {
        if(INSTANCE == null) INSTANCE = new RegistroRepository(context);
        return INSTANCE;
    }

    private RegistroRepository(final Context context)
    {
        dataSource = RegistroRoomDataSource.getInstance(context);
    }

    @Override
    public void insert(Registro registro, CallbacksDataSource.InsertCallback callback)
    {
        dataSource.insert(registro, callback);
    }

    @Override
    public void update(Registro registro, CallbacksDataSource.UpdateCallback callback)
    {
        dataSource.update(registro,callback);
    }

    @Override
    public void getById(UUID id, CallbacksDataSource.GetByIdCallback<Registro> callback)
    {
        dataSource.getById(id, callback);
    }

    @Override
    public void getAll(CallbacksDataSource.GetAllCallback<Registro> callback)
    {
        dataSource.getAll(callback);
    }

    @Override
    public void getAllFrom(UUID medicamentoId, CallbacksDataSource.GetAllCallback<Registro> callback)
    {
        dataSource.getAllFrom(medicamentoId,callback);
    }

    @Override
    public void getAllFromWhere(UUID medicamentoId, RegistroEstado estado, CallbacksDataSource.GetAllCallback<Registro> callback)
    {
        dataSource.getAllFromWhere(medicamentoId,estado,callback);
    }

    @Override
    public void getAllFromDate(ZonedDateTime fecha, CallbacksDataSource.GetAllCallback<Registro> callback)
    {
        dataSource.getAllFromDate(fecha,callback);
    }

    @Override
    public void deleteAllFromWhere(UUID medicamentoId, CallbacksDataSource.DeleteCallback callback) {
        dataSource.deleteAllFromWhere(medicamentoId, callback);
    }
}
