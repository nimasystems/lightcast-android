package com.nimasystems.lightcast.logging;

public interface LcLogger {

    public void info(String message);

    public void warn(String message);

    public void err(String message);

    public void notice(String message);

    public void crit(String message);

    public void debug(String message);
}
