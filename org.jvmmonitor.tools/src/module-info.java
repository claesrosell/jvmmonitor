/**
 * Defines interfaces to access <tt>com.sun.tools</tt> in the module <tt>jdk.attach</tt>, and to execute jmap
 * implemented at <tt>sun.tools</tt>.
 */
module org.jvmmonitor.tools {
    requires transitive jdk.attach;
}