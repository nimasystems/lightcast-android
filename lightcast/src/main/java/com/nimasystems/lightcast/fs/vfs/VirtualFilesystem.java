package com.nimasystems.lightcast.fs.vfs;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.nimasystems.lightcast.encoding.MD5;
import com.nimasystems.lightcast.exceptions.InvalidParamsException;
import com.nimasystems.lightcast.exceptions.NotAvailableException;
import com.nimasystems.lightcast.logging.LcLogger;
import com.nimasystems.lightcast.utils.DbUtils;
import com.nimasystems.lightcast.utils.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

public class VirtualFilesystem {

    public static final int DEFAULT_FILES_PER_DIR = 200;
    private int mFilesPerDir = VirtualFilesystem.DEFAULT_FILES_PER_DIR;
    private boolean mInitialized;
    private String mBasePath;
    private String mCurrentDir;
    private int mCurrentFileCount;
    private SQLiteDatabase mDb;
    private LcLogger mLogger;

    public String getBasePath() {
        return mBasePath;
    }

    public void initialize() throws InvalidParamsException, IOException {
        if (mInitialized) {
            return;
        }

        if (StringUtils.isNullOrEmpty(mBasePath)) {
            throw new InvalidParamsException("Invalid base path");
        }

        if (mDb == null || mLogger == null) {
            throw new InvalidParamsException("No database / logger specified");
        }

        if (this.mFilesPerDir < 1) {
            throw new InvalidParamsException("Files per dir config is invalid");
        }

        // try to create the base dir
        File d = new File(mBasePath);

        if (!d.isDirectory() || !d.canWrite() || !d.canRead()) {
            throw new IOException(
                    "Directory not valid or not readable / writable");
        }

        //noinspection ResultOfMethodCallIgnored
        d.mkdirs();

        String currentDir = null;
        int currentFilesCount = 0;

        Cursor c = mDb.rawQuery("SELECT dir_hash FROM filesystem ORDER BY file_id DESC LIMIT 1", null);

        if (c != null && c.getCount() > 0) {
            c.moveToFirst();
            String dirHash = c.getString(c.getColumnIndex("dir_hash"));
            c.close();

            if (!StringUtils.isNullOrEmpty(dirHash)) {

                currentDir = dirHash;

                c = mDb.rawQuery("SELECT Count(filesystem.file_id) AS counted FROM filesystem WHERE dir_hash = ?", new String[]{
                        dirHash
                });

                if (c != null && c.getCount() > 0) {
                    c.moveToFirst();
                    int counted = c.getInt(c.getColumnIndex("counted"));

                    if (counted + 1 > this.mFilesPerDir) {
                        currentFilesCount = 0;
                        currentDir = null;
                    } else {
                        currentFilesCount = counted;
                    }

                    c.close();
                }
            }
        }

        if (StringUtils.isNullOrEmpty(currentDir)) {
            currentDir = this.getRandomHash();
        }

        this.mCurrentDir = currentDir;
        this.mCurrentFileCount = currentFilesCount;

        mInitialized = true;

        mLogger.info("Virtual filesystem initialized with base path: " + this.mBasePath);
    }

    private String getRandomHash() {
        Random rn = new Random();
        String r1 = Integer.toString(rn.nextInt());
        String r2 = Integer.toString(rn.nextInt());
        return MD5.md5Hash(r1 + r2);
    }

    private String getFullFilename(String dirHash, String fileHash,
                                   String fileExt) {
        return mBasePath + File.pathSeparator + dirHash
                + File.pathSeparator + fileHash + File.pathSeparator + fileExt;
    }

    public VirtualFile writeFile(String physicalFilename,
                                 String actualFilename, String fileExtension)
            throws InvalidParamsException, IOException {

        if (!mInitialized) {
            this.initialize();
        }

        if (StringUtils.isNullOrEmpty(actualFilename)
                || StringUtils.isNullOrEmpty(fileExtension)) {
            throw new InvalidParamsException("Invalid filename");
        }

        // try to read the file
        FileInputStream inp = new FileInputStream(physicalFilename);
        byte[] fileData = null;

        //noinspection TryFinallyCanBeTryWithResources
        try {
            //noinspection ConstantConditions
            if (inp.read(fileData) <= 0) {
                throw new IOException("Could not read data");
            }
        } finally {
            try {
                inp.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return this.writeFile(fileData, actualFilename, fileExtension);
    }

    public VirtualFile writeFile(byte[] fileData, String actualFilename,
                                 String fileExtension) throws InvalidParamsException, IOException {

        if (!mInitialized) {
            this.initialize();
        }

        if (fileData == null || fileData.length < 1) {
            throw new InvalidParamsException("Invalid data");
        }

        if (StringUtils.isNullOrEmpty(actualFilename)
                || StringUtils.isNullOrEmpty(fileExtension)) {
            throw new InvalidParamsException("Invalid filename");
        }

        VirtualFile vfile;

        int filesize = fileData.length;
        String fileHash = this.getRandomHash();

        // check if we have reached the maximum files per folder
        if (this.mCurrentFileCount >= this.mFilesPerDir) {

            mLogger.info("File per dir limit reached - changing folder");

            mCurrentDir = this.getRandomHash();
            mCurrentFileCount = 0;
        }

        String fullFilePath = this.getFullFilename(mCurrentDir, fileHash,
                fileExtension);
        File f = new File(fullFilePath);

        // try to create the target dir
        if (!f.exists()) {
            if (!f.mkdirs()) {
                throw new IOException("Could not create target directory");
            }
        }

        vfile = new VirtualFile();
        vfile.fileId = 0;
        vfile.actualFilename = actualFilename;
        vfile.dirHash = mCurrentDir;
        vfile.fileHash = actualFilename;
        vfile.fileExt = fileExtension;
        vfile.filesize = filesize;

        // insert into db
        String sql = "INSERT INTO filesystem (filename, filetype, filesize, created_on, dir_hash, file_hash) VALUES(?, ?, ?, ?, ?, ?)";

        mDb.beginTransaction();

        try {
            mDb.rawQuery(
                    sql,
                    new String[]{vfile.actualFilename, vfile.fileExt,
                            Long.toString(vfile.filesize),
                            DbUtils.DateToSql(vfile.createdOn), vfile.dirHash,
                            vfile.fileHash});

            vfile.fileId = (int) DbUtils.getLastInsertedId(mDb);

            // copy the file now
            FileOutputStream fos = new FileOutputStream(fullFilePath);
            fos.write(fileData);
            fos.close();

            this.mCurrentFileCount++;

            mLogger.info("Virtual file created: " + vfile);

            mDb.setTransactionSuccessful();
        } finally {
            mDb.endTransaction();
        }

        return vfile;
    }

    public VirtualFile getFile(int fileId) throws InvalidParamsException, IOException {

        if (!mInitialized) {
            this.initialize();
        }

        if (fileId < 1) {
            throw new InvalidParamsException("Invalid file id");
        }

        VirtualFile f = null;

        Cursor c = mDb.rawQuery(
                "SELECT filesystem.* FROM filesystem WHERE file_id = ?",
                new String[]{Integer.toString(fileId)});

        if (c != null) {
            if (c.getCount() > 0) {
                c.moveToFirst();

                f = new VirtualFile();
                f.fileId = c.getInt(c.getColumnIndex("file_id"));
                f.actualFilename = c.getString(c.getColumnIndex("filename"));
                f.dirHash = c.getString(c.getColumnIndex("dir_hash"));
                f.fileExt = c.getString(c.getColumnIndex("filetype"));
                f.filesize = c.getLong(c.getColumnIndex("filesize"));
                f.createdOn = DbUtils.SqlStringToDate(c.getString(c
                        .getColumnIndex("created_on")));
            }
            c.close();
        }

        return f;
    }

    public boolean deleteFile(int fileId) throws InvalidParamsException,
            NotAvailableException, IOException {

        if (!mInitialized) {
            this.initialize();
        }

        VirtualFile vf = this.getFile(fileId);

        if (vf == null) {
            throw new NotAvailableException("File is not available");
        }

        mDb.execSQL("DELETE FROM filesystem WHERE file_id = " + fileId);

        // delete the file
        String fp = vf.getPhysicalPath(this.mBasePath);
        File f = new File(fp);

        if (f.exists()) {
            if (!f.delete()) {
                return false;
            }
        }

        return true;
    }
}
