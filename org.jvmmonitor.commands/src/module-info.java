/**
 * Defines interfaces to execute commands as stand alone application.
 */
module org.jvmmonitor.commands {
    requires transitive jdk.attach;
    requires transitive java.management;
}