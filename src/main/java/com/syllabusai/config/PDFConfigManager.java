package com.syllabusai.config;

public class PDFConfigManager {

    private static volatile PDFConfigManager instance;
    private final PDFBoxConfig config;

    private PDFConfigManager() {
        config = new PDFBoxConfig();
    }

    public static PDFConfigManager getInstance() {
        if (instance == null) {
            synchronized (PDFConfigManager.class) {
                if (instance == null) {
                    instance = new PDFConfigManager();
                }
            }
        }
        return instance;
    }

    public PDFBoxConfig getConfig() {
        return config;
    }
}
