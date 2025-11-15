package com.syllabusai.observer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


@Slf4j
@Component
public class FileProcessingSubject implements ProgressSubject {

    private final List<ProgressObserver> observers = new CopyOnWriteArrayList<>();

    @Override
    public void attach(ProgressObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
            log.debug("Attached progress observer: {}", observer.getClass().getSimpleName());
        }
    }

    @Override
    public void detach(ProgressObserver observer) {
        observers.remove(observer);
        log.debug("Detached progress observer: {}", observer.getClass().getSimpleName());
    }

    @Override
    public void notifyProgress(int progress, String message) {
        log.debug("Progress update: {}% - {}", progress, message);

        for (ProgressObserver observer : observers) {
            try {
                observer.update(progress, message);
            } catch (Exception e) {
                log.warn("Progress observer failed: {}", e.getMessage());
            }
        }
    }

    @Override
    public void notifyComplete(String result) {
        log.info("Processing complete: {}", result);

        for (ProgressObserver observer : observers) {
            try {
                observer.onComplete(result);
            } catch (Exception e) {
                log.warn("Completion observer failed: {}", e.getMessage());
            }
        }
    }

    @Override
    public void notifyError(String error) {
        log.error("Processing error: {}", error);

        for (ProgressObserver observer : observers) {
            try {
                observer.onError(error);
            } catch (Exception e) {
                log.warn("Error observer failed: {}", e.getMessage());
            }
        }
    }

    public int getObserverCount() {
        return observers.size();
    }
}