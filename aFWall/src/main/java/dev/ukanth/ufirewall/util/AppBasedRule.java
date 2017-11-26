package dev.ukanth.ufirewall.util;

/**
 * Created by Jabb0 on 09.11.2017.
 */

/**
 * Class for the app based rules (stores information)
 */
public class AppBasedRule {
    String dest;
    String port;
    boolean isEnabled;

    public AppBasedRule(String dest, String port, boolean isEnabled) {
        this.dest = dest;
        this.port = port;
        this.isEnabled = isEnabled;
    }

    @Override
    public String toString() {
        return "AppBasedRule{" +
                "dest='" + dest + '\'' +
                ", port='" + port + '\'' +
                ", isEnabled=" + isEnabled +
                '}';
    }
}
