module com.udacity.catpoint.securityservice {
    exports com.udacity.catpoint.securityservice;
    exports com.udacity.catpoint.securityservice.data;
    exports com.udacity.catpoint.securityservice.application;

    requires java.desktop;
    requires com.google.common;
    requires com.udacity.catpoint.imageservice;
    requires  org.junit.jupiter.api;
    requires  org.mockito;
    requires  org.junit.platform.commons;
    requires  org.mockito.junit.jupiter;
    requires  org.junit.jupiter.params;

//    opens org.junit.platform.commons.util to org.junit.platform.engine;
}