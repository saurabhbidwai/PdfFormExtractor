package com.oi.processor.pdf_form_extractor;


import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.junit.Before;
import org.junit.Test;


public class PdfFormExtractorTest {

    private TestRunner testRunner;

    @Before
    public void init() {
        testRunner = TestRunners.newTestRunner(PdfFormExtractor.class);
    }

    @Test
    public void testProcessor() {

    }

}
