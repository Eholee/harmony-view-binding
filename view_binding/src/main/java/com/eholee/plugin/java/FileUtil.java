package com.eholee.plugin.java;

import java.io.File;

public class FileUtil {

    public static boolean deleteAllFile(String filePath) {
        boolean flag = true;
        if (filePath != null) {
            File file = new File(filePath);
            if (file.exists()) {
                File[] filePaths = file.listFiles();
                for (File f : filePaths) {
                    if (f.isFile()) {
                        f.delete();
                    }
                    if (f.isDirectory()) {
                        String fpath = f.getPath();
                        deleteAllFile(fpath);
                        f.delete();
                    }
                }
            }
        } else {
            flag = false;
        }
        return flag;
    }
}
