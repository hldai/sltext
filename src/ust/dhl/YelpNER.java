package ust.dhl;

import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.util.Triple;
import ust.dhl.utils.IOUtils;

import java.io.*;
import java.util.List;
import java.util.Properties;

/**
 * Created by p_hliangdai on 2017/4/24.
 */
public class YelpNER {
    //        static final String DEF_SERIALIZEDCLASSIFIER = "d:/lib/stanford-ner-2016-10-31/classifiers/english.all.3class.distsim.crf.ser.gz";
    static final String DEF_SERIALIZEDCLASSIFIER = "d:/lib/stanford-ner-2016-10-31/classifiers/english.conll.4class.distsim.crf.ser.gz";

    public static class Review {
        String reviewId;
        String text;
    }

    static class NERThread extends Thread {
        String reviewsFile;
        String dstFile;
        AbstractSequenceClassifier<CoreLabel> classifier;

        NERThread(AbstractSequenceClassifier<CoreLabel> classifier, String reviewsFile, String dstFile) {
            this.classifier = classifier;
            this.reviewsFile = reviewsFile;
            this.dstFile = dstFile;
        }

        public void run() {
            try {
                FileInputStream fis = new FileInputStream(reviewsFile);
                BufferedReader reader = new BufferedReader(new InputStreamReader(fis, "UTF8"));

                FileOutputStream fos = new FileOutputStream(dstFile);
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos));

                int cnt = 0, mentionCnt = 0;
                Review rev = null;
                while ((rev = nextReview(reader)) != null) {
//            System.out.printf("\n%s\n%s", rev.reviewId, rev.text);

                    List<Triple<String, Integer, Integer>> triples = classifier.classifyToCharacterOffsets(rev.text);

//            writer.write(String.format("%s\t%d\n", rev.reviewId, triples.size()));

                    for (Triple<String, Integer, Integer> trip : triples) {
                        String ns = rev.text.substring(trip.second, trip.third).replaceAll("\\s+", " ");
                        writer.write(String.format("%08d\t%s\t%d\t%d\t%s\t%s\n", ++mentionCnt, rev.reviewId, trip.second,
                                trip.third, trip.first.substring(0, 3), ns));
                    }

                    ++cnt;
//                    if (cnt == 50)
//                        break;
                    if (cnt % 10000 == 0)
                        System.out.println(cnt);
//                    break;
                }

                reader.close();
                writer.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static Review nextReview(BufferedReader reader) throws Exception {
        String line = reader.readLine();
        if (line == null)
            return null;

        Review rev = new Review();

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

    private static void tryNER() throws Exception {
        String serializedClassifier = "d:/lib/stanford-ner-2016-10-31/classifiers/english.conll.4class.distsim.crf.ser.gz";
        AbstractSequenceClassifier<CoreLabel> classifier = CRFClassifier.getClassifier(serializedClassifier);

        String text = "All of us like walnut shrimp from Ping Pang Pong and China Mama. I recommend Retro Ranch, " +
                "Fashion by Robert Black, France's or the new Vintage shop next to Hula's and Lola Coffee on " +
                "Central Ave.";
        List<Triple<String, Integer, Integer>> triples = classifier.classifyToCharacterOffsets(text);

        for (Triple<String, Integer, Integer> trip : triples) {
            String ns = text.substring(trip.second, trip.third).replaceAll("\\s+", " ");
            System.out.printf("%d\t%d\t%s\t%s\n", trip.second, trip.third, trip.first, ns);
        }
    }

    private static void performNER() throws Exception {
        String reviewFile = "e:/data/yelp/yelp_reviews.txt";
        String dstFile = "e:/data/yelp/yelp_review_mentions_4class.txt";

        AbstractSequenceClassifier<CoreLabel> classifier = CRFClassifier.getClassifier(DEF_SERIALIZEDCLASSIFIER);

        FileInputStream fis = new FileInputStream(reviewFile);
        BufferedReader reader = new BufferedReader(new InputStreamReader(fis, "UTF8"));

        FileOutputStream fos = new FileOutputStream(dstFile);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos));

        int cnt = 0, mentionCnt = 0;
        Review rev = null;
        while ((rev = nextReview(reader)) != null) {
//            System.out.printf("\n%s\n%s", rev.reviewId, rev.text);

            List<Triple<String, Integer, Integer>> triples = classifier.classifyToCharacterOffsets(rev.text);

//            writer.write(String.format("%s\t%d\n", rev.reviewId, triples.size()));

            for (Triple<String, Integer, Integer> trip : triples) {
                String ns = rev.text.substring(trip.second, trip.third).replaceAll("\\s+", " ");
                writer.write(String.format("%08d\t%s\t%d\t%d\t%s\t%s\n", ++mentionCnt, rev.reviewId, trip.second,
                        trip.third, trip.first.substring(0, 3), ns));
            }

            ++cnt;
//            if (cnt == 50)
//                break;
            if (cnt % 10000 == 0)
                System.out.println(cnt);
        }

        reader.close();
        writer.close();
    }

    private static void performNerMP() throws Exception {
        AbstractSequenceClassifier<CoreLabel> classifier = CRFClassifier.getClassifier(DEF_SERIALIZEDCLASSIFIER);
        for (int i = 0; i < 4; ++i) {
            String reviewsFile = String.format("e:/data/yelp/split/yelp_reviews_%d.txt", i);
            String dstFile = String.format("e:/data/yelp/split/yelp_review_mentions_4class_%d.txt", i);

            NERThread nerThread = new NERThread(classifier, reviewsFile, dstFile);
            nerThread.start();
        }
    }

    public static void main(String[] args) throws Exception {
//        performNER();
        performNerMP();
//        tryNER();
    }
}
