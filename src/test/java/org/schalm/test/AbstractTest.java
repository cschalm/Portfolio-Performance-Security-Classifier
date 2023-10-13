package org.schalm.test;

import org.junit.BeforeClass;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.LogManager;

public class AbstractTest {

    @BeforeClass
    public static void setupLogging() throws IOException {
        LogManager logManager = LogManager.getLogManager();
        logManager.readConfiguration(new FileInputStream("src/main/resources/logging.properties"));
    }

}
