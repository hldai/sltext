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
 * Created by p_hliangdai on 2017/4/23.
 */
public class ChineseNER {
    static class NERThread extends Thread{
        String articlesFile;
        String dstFile;
        AbstractSequenceClassifier<CoreLabel> classifier;

        NERThread(AbstractSequenceClassifier<CoreLabel> classifier, String articlesFile, String dstFile) {
            this.classifier = classifier;
            this.articlesFile = articlesFile;
            this.dstFile = dstFile;
        }

        public void run() {
            try {
                BufferedReader reader = IOUtils.bufReader(articlesFile);
                BufferedWriter writer = IOUtils.bufWriter(dstFile);
                String line = null;
                int linecnt = 0;
                while ((line = reader.readLine()) != null) {
                    List<Triple<String, Integer, Integer>> triples = classifier.classifyToCharacterOffsets(line);
                    int cnt = 0;
                    for (Triple<String, Integer, Integer> trip : triples)
                        if (!trip.first.equals("MISC"))
                            ++cnt;

                    writer.write(String.format("%d\t%d\n", linecnt + 1, cnt));
                    for (Triple<String, Integer, Integer> trip : triples) {
                        if (trip.first.equals("MISC"))
                            continue;

                        writer.write(String.format("%d\t%d\t%s\t%s\n",
                                trip.second, trip.third, trip.first(),
                                line.substring(trip.second, trip.third)));
                    }

                    ++linecnt;
//                    if (linecnt == 10)
//                        break;
                    if (linecnt % 1000 == 0)
                        System.out.println(linecnt);
//            writer.write(segmentedText);
//            writer.write("\n");
                }
                reader.close();
                writer.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static final String DEF_SERIALIZEDCLASSIFIER = "d:/lib/chinesener/chinese.misc.distsim.crf.ser.gz";

    public static BufferedReader bufRead(String fileName) throws IOException {
        FileInputStream fstream = new FileInputStream(fileName);
        return new BufferedReader(new InputStreamReader(fstream, "UTF8"));
    }

    private static void lineArticleNER() throws Exception {
        String articlesFile = "e:/data/wechat/tmp.txt";
        String dstFile = "e:/data/wechat/tmp_mentions.txt";

        AbstractSequenceClassifier<CoreLabel> classifier = CRFClassifier.getClassifier(DEF_SERIALIZEDCLASSIFIER);

        BufferedReader reader = IOUtils.bufReader(articlesFile);
        BufferedWriter writer = IOUtils.bufWriter(dstFile);
        String line = null;
        while ((line = reader.readLine()) != null) {
            List<Triple<String, Integer, Integer>> triples = classifier.classifyToCharacterOffsets(line);
            int cnt = 0;
            for (Triple<String, Integer, Integer> trip : triples)
                if (!trip.first.equals("MISC"))
                    ++cnt;

            writer.write(String.format("%d\n", cnt));
            for (Triple<String, Integer, Integer> trip : triples) {
                if (trip.first.equals("MISC"))
                    continue;

                writer.write(String.format("%d\t%d\t%s\t%s\n",
                        trip.second, trip.third, trip.first(),
                        line.substring(trip.second, trip.third)));
            }

//            writer.write(segmentedText);
//            writer.write("\n");
        }
        reader.close();
        writer.close();
    }

    public static void main(String[] args) throws Exception {
        Properties props = new Properties();
        props.put("tokenize.options", "untokenizable=noneKeep");
        AbstractSequenceClassifier<CoreLabel> classifier = CRFClassifier.getClassifier(DEF_SERIALIZEDCLASSIFIER,
                props);
        for (int i = 0; i < 5; ++i) {
            String articlesFile = String.format("e:/data/wechat/tmp/public_articles_seg_%d.txt", i);
            String dstFile = String.format("e:/data/wechat/tmp/public_articles_ner_%d.txt", i);

            NERThread nerThread = new NERThread(classifier, articlesFile, dstFile);
            nerThread.start();
        }

//        lineArticleNER();
    }
}
