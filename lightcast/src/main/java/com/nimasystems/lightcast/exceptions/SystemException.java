package com.nimasystems.lightcast.exceptions;

public class SystemException extends Exception {
    public static final long serialVersionUID = 0x01;

    public SystemException(java.lang.String detailMessage) {
        super(detailMessage);
    }
}
