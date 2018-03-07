package com.nimasystems.lightcast.logging;

public interface LcLogger {

    void info(String message);

    void warn(String message);

    void err(String message);

    void notice(String message);

    void crit(String message);

    void debug(String message);
}
