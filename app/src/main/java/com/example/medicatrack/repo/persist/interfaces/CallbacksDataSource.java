package com.example.medicatrack.repo.persist.interfaces;


import java.util.List;

public interface CallbacksDataSource
{
    interface InsertCallback
    {
        void onInsert(boolean result);
    }
    interface UpdateCallback
    {
        void onUpdate(boolean result);
    }
    interface GetByIdCallback<T>
    {
        void onGetById(boolean result, final T value);
    }
    interface GetAllCallback<T>
    {
        void onGetAll(boolean result, final List<T> values);
    }
    interface DeleteCallback
    {
        void onDelete(boolean result);
    }
}
