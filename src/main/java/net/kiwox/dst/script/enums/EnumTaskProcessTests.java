package net.kiwox.dst.script.enums;

public enum EnumTaskProcessTests {

    TEST_TASK("TEST", "TEST", 0, null);

    String taskName;
    String taskDescription;
    int sequence;

    EnumCodeProcessTests process;

    EnumTaskProcessTests(String taskName, String taskDescription, int sequence, EnumCodeProcessTests process) {
        this.taskName = taskName;
        this.taskDescription = taskDescription;
        this.sequence = sequence;
        this.process = process;
    }


    public String getTaskName() {
        return taskName;
    }

    public java.lang.String getTaskDescription() {
        return taskDescription;
    }

    public int getSequence() {
        return sequence;
    }

    public EnumCodeProcessTests getProcess() {
        return process;
    }
}
