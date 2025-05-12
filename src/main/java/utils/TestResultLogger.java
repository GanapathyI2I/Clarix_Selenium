package utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TestResultLogger {
    private static final String FILE_PATH = "target/test-results.json";
    private static final List<TestResult> results = new ArrayList<>();

    public static synchronized void log(String scenario, String db, String app, String status) {
        results.add(new TestResult(scenario, db, app, status));
        writeResults();
    }

    private static void writeResults() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        try {
            mapper.writeValue(new File(FILE_PATH), results);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class TestResult {
        public String Scenario;
        public String DB;
        public String APP;
        public String Status;

        public TestResult(String scenario, String db, String app, String status) {
            this.Scenario = scenario;
            this.DB = db;
            this.APP = app;
            this.Status = status;
        }
    }
}