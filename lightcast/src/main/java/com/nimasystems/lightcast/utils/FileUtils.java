package com.nimasystems.lightcast.utils;

import android.webkit.MimeTypeMap;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class FileUtils {

    public static boolean deleteDirectoryRecursively(File dir, boolean emptyTopDir) {

        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (String aChildren : children) {
                File child = new File(dir, aChildren);
                if (child.isDirectory()) {
                    deleteDirectoryRecursively(child, false);

                    if (!child.delete()) {
                        return false;
                    }
                } else {
                    if (!child.delete()) {
                        return false;
                    }
                }
            }

            if (!emptyTopDir) {
                if (!dir.delete()) {
                    return false;
                }
            }
        }

        return true;
    }

    public static String getMimetype(String filepath) {
        String extension = filepath.substring(filepath.lastIndexOf("."));
        String mimeTypeMap = MimeTypeMap.getFileExtensionFromUrl(extension);
        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                mimeTypeMap);
        return mimeType;
    }

    public static byte[] getBytesFromInputStream(InputStream is) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        try {
            byte[] buffer = new byte[0xFFFF];

            for (int len; (len = is.read(buffer)) != -1; )
                os.write(buffer, 0, len);

            os.flush();

            return os.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String returnMemoryMappedFileContents(File file, int lines)
            throws IOException {
        FileInputStream fileInputStream = new FileInputStream(file);
        String ret = null;
        //noinspection TryFinallyCanBeTryWithResources
        try {
            FileChannel channel = fileInputStream.getChannel();
            //noinspection TryFinallyCanBeTryWithResources
            try {
                ByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY,
                        0, channel.size());
                buffer.position((int) channel.size());
                int count = 0;
                StringBuilder builder = new StringBuilder();
                for (long i = channel.size() - 1; i >= 0; i--) {
                    char c = (char) buffer.get((int) i);
                    builder.append(c);
                    if (c == '\n') {
                        if (count == lines) {
                            break;
                        }
                        count++;
                        builder.reverse();
                        ret += builder.toString();
                        builder = new StringBuilder();
                    }
                }
            } finally {
                channel.close();
            }
        } finally {
            fileInputStream.close();
        }

        return ret;
    }

}
