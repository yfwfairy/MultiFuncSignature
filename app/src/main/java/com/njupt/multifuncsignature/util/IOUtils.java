package com.njupt.multifuncsignature.util;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IOUtils {

    /**
     * 将文本保存至文件中
     * @param data 文本
     * @param file 文件
     * @throws IOException
     */
    public static void writeFile(String data, File file) throws IOException {
        OutputStream out = null;
        try {
            out = new FileOutputStream(file);
            out.write(data.getBytes());
            out.flush();
        } finally {
            close(out);
        }
    }

    /**
     * 将文件字节数组保存至文件中
     * @param data 文本
     * @param file 文件
     * @throws IOException
     */
    public static void writeFileBytes(byte[] data, File file) throws IOException {
        OutputStream out = null;
        try {
            out = new FileOutputStream(file);
            out.write(data);
            out.flush();
        } finally {
            close(out);
        }
    }

    /**
     * 从文件中读取字符串
     * @param file
     * @return
     * @throws IOException
     */
    public static String readFile(File file) throws IOException {
        InputStream in = null;
        ByteArrayOutputStream out = null;
        try {
            in = new FileInputStream(file);
            out = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int len = -1;
            while ((len = in.read(buf)) != -1) {
                out.write(buf, 0, len);
            }
            out.flush();
            byte[] data = out.toByteArray();
            return new String(data);
        } finally {
            close(in);
            close(out);
        }
    }

    /**
     * 从文件中读取字节数组
     * @param file
     * @return
     * @throws IOException
     */
    public static byte[] readFileBytes(File file) throws IOException {
        InputStream in = null;
        ByteArrayOutputStream out = null;
        try {
            in = new FileInputStream(file);
            out = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int len = -1;
            while ((len = in.read(buf)) != -1) {
                out.write(buf, 0, len);
            }
            out.flush();
            return out.toByteArray();
        } finally {
            close(in);
            close(out);
        }
    }

    /**
     * 从文件中读取字节数组并追加字节数组
     * @param file
     * @return
     * @throws IOException
     */
    public static byte[] readFileBytesAndAppend(File file, byte[] bytes) throws IOException {
        InputStream in = null;
        ByteArrayOutputStream out = null;
        try {
            in = new FileInputStream(file);
            out = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int len = -1;
            while ((len = in.read(buf)) != -1) {
                out.write(buf, 0, len);
            }
            out.write(bytes);
            out.flush();
            return out.toByteArray();
        } finally {
            close(in);
            close(out);
        }
    }

    private static void close(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (IOException e) {
                // nothing
            }
        }
    }
}
