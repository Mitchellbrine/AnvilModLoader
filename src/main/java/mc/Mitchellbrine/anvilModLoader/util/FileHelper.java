package mc.Mitchellbrine.anvilModLoader.util;

import java.io.*;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by Mitchellbrine on 2015.
 */
public class FileHelper {

    public static void download(InputStream is, int sizeGuess, File target) throws IOException {

            if (!target.getPath().endsWith(".jar") && !target.getPath().endsWith(".zip")) {
                File oldTarget = target;
                target = new File(oldTarget.getPath() + ".jar");
            }

            byte[] buffer = new byte[4096];
            int n = - 1;

            OutputStream output = new FileOutputStream(target);
            while ( (n = is.read(buffer)) != -1)
            {

                output.write(buffer, 0, n);
            }
            output.close();

                /*}
                else
                {
                    throw new RuntimeException(String.format("The downloaded file %s has an invalid checksum %s (expecting %s). The download did not succeed correctly and the file has been deleted. Please try launching again.", target.getName(), cksum, validationHash));
                }*/
    }

    public static void unzip(File zipfile, File directory) throws IOException {
        ZipFile zfile = new ZipFile(zipfile);
        Enumeration<? extends ZipEntry> entries = zfile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            File file = new File(directory, entry.getName());
            if (entry.isDirectory()) {
                file.mkdirs();
            } else {
                file.getParentFile().mkdirs();
                final InputStream in = zfile.getInputStream(entry);
                final String path = directory.getPath();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            try {
                                copy(in, new File(path));
                            } finally {
                                in.close();
                            }
                        } catch (IOException ex) {
                        }
                    }
                }).start();
            }
        }
        zfile.close();
    }

    private static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        while (true) {
            int readCount = in.read(buffer);
            if (readCount < 0) {
                break;
            }
            out.write(buffer, 0, readCount);
        }
    }

    public static void copy(InputStream in, File file) throws IOException {
        OutputStream out = new FileOutputStream(file);
        try {
            copy(in, out);
        } finally {
            out.close();
        }
    }

}
