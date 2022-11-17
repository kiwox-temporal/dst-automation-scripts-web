package net.kiwox.dst.script.pojo;


import net.kiwox.dst.script.enums.EnumCodeProcessTests;
import net.kiwox.dst.script.enums.EnumCodeTypeTests;

import java.util.ArrayList;
import java.util.List;

public class TestResultEntelApp extends TestResult {

    private String errorMessage;
    public List<TestResultDetailEntelApp> details;

    public TestResultEntelApp() {
        this.errorMessage = "";
        super.setError(false);
        super.setTime(0L);
        this.details = new ArrayList<>();
    }

    public TestResultEntelApp setDetails(List<TestResultDetailEntelApp> details) {
        this.errorMessage = "";
        this.details = details;
        return this;
    }

    public void addItemDetail(TestResultDetailEntelApp item) {
        this.details.add(item);
    }

    public List<TestResultDetailEntelApp> getDetails() {
        return details;
    }

    public static TestResultEntelApp castEnumProcessToTestResult(EnumCodeProcessTests processTests) {
        TestResultEntelApp testResultEntelApp = new TestResultEntelApp();
        testResultEntelApp.setError(processTests.getCodeType().equals(EnumCodeTypeTests.CATCH_ERROR));
        testResultEntelApp.setCode(processTests.getCode());
        return testResultEntelApp;
    }

    public void addTime(long newtTime) {
        this.time += newtTime;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
