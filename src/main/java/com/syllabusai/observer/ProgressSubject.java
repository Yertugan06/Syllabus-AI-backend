package com.syllabusai.observer;

public interface ProgressSubject {
    void attach(ProgressObserver observer);
    void detach(ProgressObserver observer);
    void notifyProgress(int progress, String message);
    void notifyComplete(String result);
    void notifyError(String error);
}