package com.syllabusai.config;

import org.apache.pdfbox.text.PDFTextStripper;

public class PDFBoxConfig {
    private final int startPage = 1;
    private final int endPage = Integer.MAX_VALUE;

    public PDFTextStripper createTextStripper() throws Exception {
        PDFTextStripper stripper = new PDFTextStripper();
        stripper.setStartPage(startPage);
        stripper.setEndPage(endPage);
        return stripper;
    }

}
