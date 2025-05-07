package com.example.medicatrack.repo.persist.impl;

import android.content.Context;

import com.example.medicatrack.model.Medicamento;
import com.example.medicatrack.model.Registro;
import com.example.medicatrack.model.enums.RegistroEstado;
import com.example.medicatrack.repo.persist.database.Database;
import com.example.medicatrack.repo.persist.database.daos.RegistroDao;
import com.example.medicatrack.repo.persist.entities.RegistroEntity;
import com.example.medicatrack.repo.persist.interfaces.CallbacksDataSource;
import com.example.medicatrack.repo.persist.interfaces.RegistroDataSource;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RegistroRoomDataSource implements RegistroDataSource
{

    private static RegistroDao registroDao;
    private static RegistroRoomDataSource INSTANCE = null;
    private final Context context;

    private RegistroRoomDataSource(final Context context)
    {
        this.context = context;
        Database bd = Database.getInstance(context);
        registroDao = bd.registroDao();
    }

    public synchronized static RegistroRoomDataSource getInstance(final Context context)
    {
        if(INSTANCE == null) INSTANCE = new RegistroRoomDataSource(context);
        return INSTANCE;
    }

    @Override
    public void insert(Registro registro, CallbacksDataSource.InsertCallback callback)
    {
        if(registro == null) callback.onInsert(false);
        else
        {
            RegistroEntity entity = new RegistroEntity();
            entity.setId(registro.getId());
            entity.setEstado(registro.getEstado().name());
            entity.setFecha(registro.getFecha().toEpochSecond());
            entity.setMedicaId(registro.getMedicamento().getId());
            try
            {
                registroDao.insert(entity);
                callback.onInsert(true);

            }catch (Exception e)
            {
                callback.onInsert(false);
            }
        }
    }

    @Override
    public void update(Registro registro, CallbacksDataSource.UpdateCallback callback)
    {
        try
        {
            RegistroEntity entity = modelToEntity(registro);
            registroDao.update(entity);
            callback.onUpdate(true);
        } catch(Exception e)
        {
            callback.onUpdate(false);
        }
    }

    @Override
    public void getById(UUID id, CallbacksDataSource.GetByIdCallback<Registro> callback)
    {
        try
        {
            RegistroEntity entity = registroDao.getById(id);
            if(entity == null) callback.onGetById(true, null);
            else {
                Registro registro = entityToModel(entity,context);
                callback.onGetById(true,registro);
            }
        } catch(Exception e)
        {
            callback.onGetById(false,null);
        }
    }

    @Override
    public void getAll(CallbacksDataSource.GetAllCallback<Registro> callback)
    {
        //To do
    }

    @Override
    public void getAllFrom(UUID medicamentoId, CallbacksDataSource.GetAllCallback<Registro> callback)
    {
        try
        {
            List<Registro> registros = new ArrayList<>();
            List<RegistroEntity> registrosEntities = new ArrayList<>(registroDao.getAllFrom(medicamentoId));
            registrosEntities.forEach(it ->
            {
                registros.add(entityToModel(it,context));
            });

            callback.onGetAll(true,registros);
        } catch (Exception e)
        {
            callback.onGetAll(false,null);
        }
    }

    @Override
    public void getAllFromWhere(UUID medicamentoId, RegistroEstado estado, CallbacksDataSource.GetAllCallback<Registro> callback) {

    }

    @Override   //Solo evalua que las fechas sean iguales, ignorando la hora
    public void getAllFromDate(ZonedDateTime fecha, CallbacksDataSource.GetAllCallback<Registro> callback)
    {
        try
        {
            List<Registro> registros = new ArrayList<>();
            List<RegistroEntity> registrosEntities = new ArrayList<>(registroDao.getAllFromDate(fecha.toEpochSecond()));
            registrosEntities.forEach(it ->
            {
                registros.add(entityToModel(it,context));
            });

            callback.onGetAll(true,registros);
        } catch (Exception e)
        {
            callback.onGetAll(false,null);
        }
    }

    @Override
    public void deleteAllFromWhere(UUID medicamentoId, CallbacksDataSource.DeleteCallback callback) {
        try
        {
            registroDao.deleteAllFromWhere(medicamentoId);
            callback.onDelete(true);

        } catch (Exception e)
        {
            callback.onDelete(false);
        }
    }

    public static RegistroEntity modelToEntity(Registro registro){
        RegistroEntity entity = new RegistroEntity();
        entity.setId(registro.getId());
        entity.setEstado(registro.getEstado().name());
        entity.setFecha(registro.getFecha().toEpochSecond());
        entity.setMedicaId(registro.getMedicamento().getId());
        return entity;
    }
    public static Registro entityToModel(RegistroEntity entity, Context context)
    {
        Registro registro = new Registro(entity.getId());
        registro.setEstado(RegistroEstado.valueOf(entity.getEstado()));
        registro.setFecha(ZonedDateTime.ofInstant(Instant.ofEpochSecond(entity.getFecha()),ZoneId.of("America/Argentina/Buenos_Aires")));
        final Medicamento[] medicamento = new Medicamento[1];
        MedicamentoRoomDataSource.getInstance(context).getById(entity.getMedicaId(),(result, value) ->
        {
            if(result) medicamento[0] = value;
            else medicamento[0] = null;
        });
        registro.setMedicamento(medicamento[0]);

        return registro;

    }
}
