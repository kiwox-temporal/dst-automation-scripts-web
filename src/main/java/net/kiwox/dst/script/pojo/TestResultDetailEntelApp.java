package net.kiwox.dst.script.pojo;

public class TestResultDetailEntelApp {

    private String code;
    private String detail;
    private long time;
    private boolean errorDetected;
    private String description;

    public TestResultDetailEntelApp() {
        this.errorDetected = false;
    }

    public TestResultDetailEntelApp(String code, String detail, long time, boolean errorDetected, String description) {
        this.code = code;
        this.detail = detail;
        this.time = time;
        this.errorDetected = errorDetected;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public TestResultDetailEntelApp setCode(String code) {
        this.code = code;
        return this;
    }

    public String getDetail() {
        return detail;
    }

    public TestResultDetailEntelApp setDetail(String detail) {
        this.detail = detail;
        return this;
    }

    public long getTime() {
        return time;
    }

    public TestResultDetailEntelApp setTime(long time) {
        this.time = time;
        return this;
    }

    public boolean isErrorDetected() {
        return errorDetected;
    }

    public TestResultDetailEntelApp setErrorDetected(boolean errorDetected) {
        this.errorDetected = errorDetected;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public TestResultDetailEntelApp setDescription(String description) {
        this.description = description;
        return this;
    }
}
