package com.example.medicatrack.repo.persist.database.daos;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.medicatrack.repo.persist.entities.RegistroEntity;

import java.util.List;
import java.util.UUID;

@Dao
public interface RegistroDao
{
    @Insert(onConflict = OnConflictStrategy.ABORT)
    void insert(RegistroEntity registro);

    @Update
    void update(RegistroEntity registro);

    @Query("SELECT * FROM RegistroEntity WHERE id = :id")
    RegistroEntity getById(UUID id);

    @Query("SELECT * FROM RegistroEntity WHERE fecha_sin_hora = :fecha ORDER BY fecha ASC")
    List<RegistroEntity> getAllFromDate(Long fecha);
    @Query("SELECT * FROM RegistroEntity WHERE (medicamento_id = :medicamentoId AND estado = :estado)")
    List<RegistroEntity> getAllFromWhere(UUID medicamentoId, String estado);

    @Query("SELECT * FROM RegistroEntity WHERE medicamento_id = :medicamentoId ORDER BY fecha ASC")
    List<RegistroEntity> getAllFrom(UUID medicamentoId);

    @Query("SELECT * FROM RegistroEntity ORDER BY fecha ASC")
    List<RegistroEntity> getAll();

    @Query("DELETE FROM RegistroEntity WHERE medicamento_id = :medicamentoId")
    void deleteAllFromWhere(UUID medicamentoId);

}
