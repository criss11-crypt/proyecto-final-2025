package com.example.medicatrack.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.example.medicatrack.model.enums.RegistroEstado;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

public class Registro implements Parcelable
{
    private UUID id;
    private Medicamento medicamento;
    private ZonedDateTime fecha;
    private RegistroEstado estado;

    public Registro(UUID id)
    {
        this.id = id;
    }

    public Registro(){};

    public UUID getId() {
        return id;
    }

    public Medicamento getMedicamento() {
        return medicamento;
    }

    public void setMedicamento(Medicamento medicamento) {
        this.medicamento = medicamento;
    }

    public ZonedDateTime getFecha() {
        return fecha;
    }

    public void setFecha(ZonedDateTime fecha) {
        this.fecha = fecha;
    }

    public RegistroEstado getEstado() {
        return estado;
    }

    public void setEstado(RegistroEstado estado) {
        this.estado = estado;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(this.id.toString());
        dest.writeParcelable(this.medicamento, 0);
        dest.writeLong(this.fecha.toInstant().getEpochSecond());
        dest.writeString(this.estado.toString());
    }

    protected Registro(Parcel in) {
        this.id = UUID.fromString(in.readString());
        this.medicamento = in.readParcelable(Medicamento.class.getClassLoader());
        this.fecha = ZonedDateTime.ofInstant(Instant.ofEpochSecond(in.readLong()), ZoneId.of("America/Argentina/Buenos_Aires"));
        this.estado = RegistroEstado.valueOf(in.readString());
    }

    public static final Creator<Registro> CREATOR = new Creator<Registro>() {
        @Override
        public Registro createFromParcel(Parcel in) {
            return new Registro(in);
        }

        @Override
        public Registro[] newArray(int size) {
            return new Registro[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Registro registro = (Registro) o;
        return id.equals(registro.getId()) && medicamento.equals(registro.getMedicamento()) && fecha.equals(registro.getFecha()) && estado.equals(registro.getEstado());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
