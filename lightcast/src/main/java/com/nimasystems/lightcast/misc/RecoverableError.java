package com.nimasystems.lightcast.misc;

import android.os.Parcel;
import android.os.Parcelable;

public class RecoverableError implements Parcelable {

    public static final Parcelable.Creator<RecoverableError> CREATOR = new Parcelable.Creator<RecoverableError>() {
        public RecoverableError createFromParcel(Parcel in) {
            return new RecoverableError(in);
        }

        public RecoverableError[] newArray(int size) {
            return new RecoverableError[size];
        }
    };
    public int mErrorCode;
    public String mErrorMessage;

    public RecoverableError() {
        //
    }

    public RecoverableError(String errorMessage, int errorCode) {
        mErrorMessage = errorMessage;
        mErrorCode = errorCode;
    }

    public RecoverableError(String errorMessage) {
        mErrorMessage = errorMessage;
    }

    private RecoverableError(Parcel in) {
        mErrorCode = in.readInt();
        mErrorMessage = in.readString();
    }

    public int getErrorCode() {
        return mErrorCode;
    }

    public String getErrorMessage() {
        return mErrorMessage;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(mErrorCode);
        out.writeString(mErrorMessage);
    }
}
