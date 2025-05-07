package com.example.medicatrack.repo.persist.impl;

import android.content.Context;

import com.example.medicatrack.model.enums.Color;
import com.example.medicatrack.model.enums.Forma;
import com.example.medicatrack.model.enums.Unidad;
import com.example.medicatrack.model.Medicamento;
import com.example.medicatrack.model.enums.Frecuencia;
import com.example.medicatrack.repo.persist.database.Database;
import com.example.medicatrack.repo.persist.database.daos.MedicamentoDao;
import com.example.medicatrack.repo.persist.entities.MedicamentoEntity;
import com.example.medicatrack.repo.persist.interfaces.CallbacksDataSource;
import com.example.medicatrack.repo.persist.interfaces.MedicamentoDataSource;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class MedicamentoRoomDataSource implements MedicamentoDataSource
{
    private static MedicamentoDao medicamentoDao;
    private static MedicamentoRoomDataSource INSTANCE = null;

    private MedicamentoRoomDataSource(final Context context)
    {
        Database bd = Database.getInstance(context);
        medicamentoDao = bd.medicamentoDao();
    }

    public synchronized static MedicamentoRoomDataSource getInstance(final Context context)
    {
        if(INSTANCE == null) INSTANCE = new MedicamentoRoomDataSource(context);
        return INSTANCE;
    }

    @Override
    public void insert(Medicamento medicamento, CallbacksDataSource.InsertCallback callback)
    {
        if(medicamento == null) callback.onInsert(false);
        else
        {
            MedicamentoEntity entity = modelToEntity(medicamento);
            try
            {
                medicamentoDao.insert(entity);
                callback.onInsert(true);

            }catch (Exception e)
            {
                callback.onInsert(false);
            }
        }
    }

    @Override
    public void update(Medicamento medicamento, CallbacksDataSource.UpdateCallback callback)
    {
        //To do
    }

    @Override
    public void getById(UUID id, CallbacksDataSource.GetByIdCallback<Medicamento> callback)
    {
        try
        {
            MedicamentoEntity entity = medicamentoDao.getById(id);
            if(entity == null) callback.onGetById(true, null);
            else {
                Medicamento medicamento = entityToModel(entity);
                callback.onGetById(true, medicamento);
            }
        } catch(Exception e)
        {
            callback.onGetById(false,null);
        }
    }

    @Override
    public void getAll(CallbacksDataSource.GetAllCallback<Medicamento> callback)
    {
        try
        {
            List<MedicamentoEntity> entities = new ArrayList<>(medicamentoDao.getAll());
            List<Medicamento> medicamentos = new ArrayList<>();
            entities.forEach(it ->
            {
                medicamentos.add(entityToModel(it));
            });
            callback.onGetAll(true,medicamentos);
        } catch (Exception e)
        {
            callback.onGetAll(false,null);
        }
    }

    @Override
    public void delete(Medicamento medicamento, CallbacksDataSource.DeleteCallback callback) {
        try
        {
            MedicamentoEntity entity = modelToEntity(medicamento);
            medicamentoDao.delete(entity);
            callback.onDelete(true);
        } catch (Exception e)
        {
            callback.onDelete(false);
        }
    }

    public static MedicamentoEntity modelToEntity(Medicamento medicamento){
        MedicamentoEntity entity = new MedicamentoEntity();
        entity.setId(medicamento.getId());
        entity.setColor(medicamento.getColor().name());
        entity.setConcentracion(medicamento.getConcentracion());
        entity.setUnidad(medicamento.getUnidad().name());
        entity.setForma(medicamento.getForma().name());
        entity.setNombre(medicamento.getNombre());
        entity.setFrecuencia(medicamento.getFrecuencia().name());
        entity.setFechaInicio(medicamento.getFechaInicio() != null ? medicamento.getFechaInicio().toInstant().getEpochSecond() : null);
        entity.setDias(medicamento.getDias());
        entity.setHora(medicamento.getHora() != null ? medicamento.getHora().toInstant().getEpochSecond() : null);
        entity.setDescripcion(medicamento.getDescripcion());
        return entity;
    }

    public static Medicamento entityToModel(MedicamentoEntity entity)
    {
        Medicamento medicamento = new Medicamento(entity.getId());
        medicamento.setColor(Color.valueOf(entity.getColor()));
        medicamento.setNombre(entity.getNombre());
        medicamento.setFrecuencia(Frecuencia.valueOf(entity.getFrecuencia()));
        medicamento.setForma(Forma.valueOf(entity.getForma()));
        medicamento.setConcentracion(entity.getConcentracion());
        medicamento.setUnidad(Unidad.valueOf(entity.getUnidad()));
        medicamento.setDias(entity.getDias());
        medicamento.setFechaInicio(entity.getFechaInicio() != null ? ZonedDateTime.ofInstant(Instant.ofEpochSecond(entity.getFechaInicio()),ZoneId.of("America/Argentina/Buenos_Aires")) : null);
        medicamento.setHora(entity.getHora() != null ? ZonedDateTime.ofInstant(Instant.ofEpochSecond(entity.getHora()),ZoneId.of("America/Argentina/Buenos_Aires")) : null);
        medicamento.setDescripcion(entity.getDescripcion());
        return medicamento;
    }
}
