package com.example.medicatrack.repo.persist.database.daos;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.medicatrack.repo.persist.entities.MedicamentoEntity;

import java.util.List;
import java.util.UUID;


@Dao
public interface MedicamentoDao
{
    @Insert(onConflict = OnConflictStrategy.ABORT)
    void insert(MedicamentoEntity medicamento);

    @Update
    void update(MedicamentoEntity medicamento);

    @Query("SELECT * FROM MedicamentoEntity WHERE id = :id")
    MedicamentoEntity getById(UUID id);

    @Query("SELECT * FROM MedicamentoEntity")
    List<MedicamentoEntity> getAll();

    @Delete
    void delete(MedicamentoEntity medicamento);

}
