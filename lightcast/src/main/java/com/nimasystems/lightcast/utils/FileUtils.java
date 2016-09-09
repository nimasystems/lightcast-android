package com.nimasystems.lightcast.utils;

import android.webkit.MimeTypeMap;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Random;

public class FileUtils {

    public static void createRandomContentsFile(double fileSize, File file) throws FileNotFoundException, UnsupportedEncodingException {

        Random random = new Random();
        long start = System.currentTimeMillis();
        PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8")), false);
        int counter = 0;

        while (true) {
            String sep = "";
            for (int i = 0; i < 100; i++) {
                int number = random.nextInt(1000) + 1;
                writer.print(sep);
                writer.print(number / 1e3);
                sep = " ";
            }
            writer.println();
            //Check to see if the current size is what we want it to be
            if (++counter == 500) {
                System.out.printf("Size: %.3f GB%n", file.length() / 1e9);
                if (file.length() >= fileSize * 1e9) {
                    writer.close();
                    break;
                } else {
                    counter = 0;
                }
            }
        }
        long time = System.currentTimeMillis() - start;
        System.out.printf("Took %.1f seconds to create a file of %.3f GB", time / 1e3, file.length() / 1e9);
    }

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
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                mimeTypeMap);
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
                try {
                    channel.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } finally {
            try {
                fileInputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return ret;
    }

}
