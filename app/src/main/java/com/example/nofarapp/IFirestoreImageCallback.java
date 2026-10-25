package com.example.nofarapp;

public interface IFirestoreImageCallback<T> {
    void onSuccess(T result);
    void onFailure(Exception error);
}
