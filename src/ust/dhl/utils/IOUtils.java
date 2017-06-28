package ust.dhl.utils;

import java.io.*;

/**
 * Created by p_hliangdai on 2017/4/26.
 */
public class IOUtils {
    public static BufferedReader bufReader(String fileName) throws IOException {
        FileInputStream fstream = new FileInputStream(fileName);
        return new BufferedReader(new InputStreamReader(fstream, "UTF8"));
    }

    public static BufferedWriter bufWriter(String fileName) throws IOException {
        FileOutputStream fos = new FileOutputStream(fileName);
        return new BufferedWriter(new OutputStreamWriter(fos, "UTF8"));
    }
}
