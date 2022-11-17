package net.kiwox.dst.script.enums;

public enum EnumCodeTypeTests {

    CATCH_ERROR(500),
    SUCCESS_ACTION(100);

    int codeNumber;

    EnumCodeTypeTests(int codeNumber){
        this.codeNumber = codeNumber;
    }


    public int getCodeNumber() {
        return codeNumber;
    }
}
