package com.nimasystems.lightcast.fs.vfs;

import java.io.File;
import java.util.Date;

public class VirtualFile {

    public int fileId;

    public String actualFilename;
    public String dirHash;
    public String fileHash;
    public String fileExt;

    public long filesize;

    public Date createdOn;

    @Override
    public String toString() {
        return "VirtualFile (ID: " + fileId + "): Filename: " + this.actualFilename + ", Path: " + getPhysicalPath(null) + ", Size: " + Long.toString(this.filesize);
    }

    public String getPhysicalPath(String baseDir) {
        if (this.dirHash == null || this.fileHash == null) {
            return null;
        }
        return (baseDir != null ? baseDir + File.pathSeparator : "") + this.dirHash + File.pathSeparator + this.fileHash + File.pathSeparator + (this.fileExt != null ? this.fileExt : "");
    }
}
