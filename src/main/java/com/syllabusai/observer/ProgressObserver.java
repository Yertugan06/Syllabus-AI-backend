package com.syllabusai.observer;

public interface ProgressObserver {
    void update(int progress, String message);
    void onComplete(String result);
    void onError(String error);
}