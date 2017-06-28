package ust.dhl;

import java.io.BufferedReader;

/**
 * Created by p_hliangdai on 2017/5/9.
 */
public class YelpReview {
    public String reviewId;
    public String text;

    public static YelpReview nextReview(BufferedReader reader) throws Exception {
        String line = reader.readLine();
        if (line == null)
            return null;

        YelpReview rev = new YelpReview();

        String[] vals = line.split("\t");

        rev.reviewId = vals[0];
//        System.out.println(vals[0]);
//        System.out.println(vals[1]);

        if (vals.length < 2)
            System.out.println(line);

        int numLines = Integer.valueOf(vals[1]);
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < numLines; ++i) {
            stringBuilder.append(reader.readLine());
            stringBuilder.append("\n");
        }
        rev.text = stringBuilder.toString();

        return rev;
    }
}
