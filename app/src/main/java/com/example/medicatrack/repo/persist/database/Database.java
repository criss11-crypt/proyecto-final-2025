package com.example.medicatrack.repo.persist.database;

import android.content.Context;

import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.medicatrack.repo.persist.database.daos.MedicamentoDao;
import com.example.medicatrack.repo.persist.database.daos.RegistroDao;
import com.example.medicatrack.repo.persist.entities.MedicamentoEntity;
import com.example.medicatrack.repo.persist.entities.RegistroEntity;

@androidx.room.Database(entities = {MedicamentoEntity.class, RegistroEntity.class},version = 3,exportSchema = false)

public abstract class Database extends RoomDatabase
{
    public abstract MedicamentoDao medicamentoDao();
    public abstract RegistroDao registroDao();

    private static Database INSTANCE = null;

    public synchronized static Database getInstance(final Context context)
    {
        if (INSTANCE == null)   //Singleton
        {
            INSTANCE = buildDatabase(context);
        }
        return INSTANCE;
    }

    private static Database buildDatabase(final Context context)
    {
        return Room.databaseBuilder(context, Database.class, "MedicaTrack_Database")
                .fallbackToDestructiveMigration()
                .enableMultiInstanceInvalidation()  //Si 2 o más instancias se estan ejecutando en hilos o procesos separados,
                                                    // este método bloquea los recursos compartidos cuando una instancia los está usando.
                .allowMainThreadQueries()
                .build();
    }

}
