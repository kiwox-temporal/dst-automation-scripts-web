package net.kiwox.dst.script.enums;

public enum EnumAppTests {

    DST_ENTEL_PERU_APP("Entel Peru App Movil","Entel Peru App Movil"),
    DST_ENTEL_FACEBOOK_APP("Facebook App Movil","Facebook App Movil");

    String nameApp;
    String descriptionApp;

    EnumAppTests(String nameApp, String descriptionApp) {
        this.nameApp = nameApp;
        this.descriptionApp = descriptionApp;
    }

    public String getNameApp() {
        return nameApp;
    }

    public String getDescriptionApp() {
        return descriptionApp;
    }
}
