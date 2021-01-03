package com.eholee.plugin.java;

import org.gradle.api.GradleException;

public class IOUtil {
    public IOUtil() {
    }

    public static void closeAll(AutoCloseable... io) {
        AutoCloseable[] var1 = io;
        int var2 = io.length;

        for(int var3 = 0; var3 < var2; ++var3) {
            AutoCloseable temp = var1[var3];

            try {
                if (null != temp) {
                    temp.close();
                }
            } catch (Exception var6) {
                throw new GradleException(String.format("An error happened:%s IO Stream close failed. error:%s.", temp.getClass().getName(), var6.getMessage()));
            }
        }

    }
}
