package com.example.medicatrack.model;


import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.example.medicatrack.model.enums.Color;
import com.example.medicatrack.model.enums.Forma;
import com.example.medicatrack.model.enums.Frecuencia;
import com.example.medicatrack.model.enums.Unidad;
import com.example.medicatrack.utilities.FechaFormat;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.Objects;
import java.util.UUID;

public class Medicamento implements Parcelable, Comparator<Medicamento>
{

    private UUID id;
    private String nombre;
    private Color color;
    private Forma forma;
    private float concentracion;
    private Frecuencia frecuencia;
    private String dias;
    private ZonedDateTime hora;
    private String descripcion;
    private Unidad unidad;
    private ZonedDateTime fechaInicio;

    private int cantidad;   //agregado, revisar parcel

    public Medicamento(){};

    public Medicamento(UUID id)
    {
        this.id = id;
    }


    public UUID getId()
    {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Forma getForma() {
        return forma;
    }

    public void setForma(Forma forma) {
        this.forma = forma;
    }

    public float getConcentracion() {
        return concentracion;
    }

    public void setConcentracion(float concentracion) {
        this.concentracion = concentracion;
    }

    public Frecuencia getFrecuencia() {
        return frecuencia;
    }

    public void setFrecuencia(Frecuencia frecuencia) {
        this.frecuencia = frecuencia;
    }

    public String getDias() {
        return dias;
    }

    public void setDias(String dias) {
        this.dias = dias;
    }

    public ZonedDateTime getHora() {
        return hora;
    }

    public void setHora(ZonedDateTime hora) {
        this.hora = hora;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Unidad getUnidad() {
        return unidad;
    }

    public void setUnidad(Unidad unidad) {
        this.unidad = unidad;
    }

    public ZonedDateTime getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(ZonedDateTime fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(this.id.toString());
        dest.writeString(this.nombre);
        dest.writeString(this.color.name());
        dest.writeString(this.forma.name());
        dest.writeFloat(this.concentracion);
        dest.writeString(this.unidad.name());
        dest.writeString(this.frecuencia.name());
        dest.writeString(this.dias);
        dest.writeLong(this.fechaInicio != null ? this.fechaInicio.toInstant().getEpochSecond() : -1); // -1 porque Parcel trabaja con tipo de datos primitivos, y no puede ser null
        dest.writeLong(this.hora != null ? this.hora.toInstant().getEpochSecond() : -1); // -1 porque Parcel trabaja con tipo de datos primitivos, y no puede ser null
        dest.writeString(this.descripcion);
    }

    private Medicamento(Parcel in){
        this.id = UUID.fromString(in.readString());
        this.nombre = in.readString();
        this.color = Color.valueOf(in.readString());
        this.forma = Forma.valueOf(in.readString());
        this.concentracion = in.readFloat();
        this.unidad = Unidad.valueOf(in.readString());
        this.frecuencia = Frecuencia.valueOf(in.readString());
        this.dias = in.readString();
        Long segFecha = in.readLong();
        this.fechaInicio = segFecha != -1 ? ZonedDateTime.ofInstant(Instant.ofEpochSecond(segFecha), ZoneId.of("America/Argentina/Buenos_Aires")) : null;
        Long segHora = in.readLong();
        this.hora = segHora != -1 ? ZonedDateTime.ofInstant(Instant.ofEpochSecond(segHora), ZoneId.of("America/Argentina/Buenos_Aires")) : null;
        this.descripcion = in.readString();
    }

    public static final Parcelable.Creator<Medicamento> CREATOR  = new Parcelable.Creator<Medicamento>() {
        @Override
        public Medicamento createFromParcel(Parcel source) {
            return new Medicamento(source);
        }

        @Override
        public Medicamento[] newArray(int size) {
            return new Medicamento[0];
        }
    };


    @Override
    public int compare(Medicamento medicamento, Medicamento t1) //0 m == t1, -1 m < t1 , 1 t1 < m
    {
        return FechaFormat.greaterTime(medicamento.getHora(),t1.getHora());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Medicamento that = (Medicamento) o;
        boolean comparacion = this.id.equals(that.getId()) && this.nombre.equals(that.getNombre()) && this.unidad.equals(that.getUnidad()) && this.forma.equals(that.getForma())
                && this.color.equals(that.getColor()) && this.frecuencia.equals(that.getFrecuencia()) && this.concentracion == that.getConcentracion();

        if(this.dias != null && that.getDias() != null)
        {
            comparacion = comparacion && this.dias.equals(that.getDias());
        }
        if(this.fechaInicio != null && that.getFechaInicio() != null)
        {
            comparacion = comparacion && this.fechaInicio.equals(that.getFechaInicio());
        }
        if(this.hora != null && that.getHora() != null)
        {
            comparacion = comparacion && this.hora.equals(that.getHora());
        }
        if(this.descripcion != null && that.getDescripcion() != null)
        {
            comparacion = comparacion && this.descripcion.equals(that.getDescripcion());
        }
        return comparacion;
    }

    public boolean sameContent(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Medicamento that = (Medicamento) o;
        boolean comparacion = this.nombre.equals(that.getNombre()) && this.unidad.equals(that.getUnidad()) && this.forma.equals(that.getForma())
                && this.color.equals(that.getColor()) && this.frecuencia.equals(that.getFrecuencia()) && this.concentracion == that.getConcentracion();

        if(this.dias != null && that.getDias() != null)
        {
            comparacion = comparacion && this.dias.equals(that.getDias());
        }
        if(this.fechaInicio != null && that.getFechaInicio() != null)
        {
            comparacion = comparacion && this.fechaInicio.equals(that.getFechaInicio());
        }
        if(this.hora != null && that.getHora() != null)
        {
            comparacion = comparacion && this.hora.equals(that.getHora());
        }
        if(this.descripcion != null && that.getDescripcion() != null)
        {
            comparacion = comparacion && this.descripcion.equals(that.getDescripcion());
        }
        return comparacion;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}


