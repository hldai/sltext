package ust.dhl;

import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.util.Triple;

import java.io.*;
import java.util.List;

public class LineNER {
    private static void tryNER() throws Exception {
        String serializedClassifier = "D:/lib/stanford-ner-2018-02-27/classifiers/english.conll." +
                "4class.distsim.crf.ser.gz";
        AbstractSequenceClassifier<CoreLabel> classifier = CRFClassifier.getClassifier(serializedClassifier);

        String text = "The Obamas are now worth 30 times more than when they entered the White House. Here’s what they do with their millions https://t.co/8N00alwvae";
        List<Triple<String, Integer, Integer>> triples = classifier.classifyToCharacterOffsets(text);

        for (Triple<String, Integer, Integer> trip : triples) {
            String ns = text.substring(trip.second, trip.third).replaceAll("\\s+", " ");
            System.out.printf("%d\t%d\t%s\t%s\n", trip.second, trip.third, trip.first, ns);
        }
    }

    private static void processFile(String fileName, String dstFile) throws Exception {
        String serializedClassifier = "D:/lib/stanford-ner-2018-02-27/classifiers/english.conll." +
                "4class.distsim.crf.ser.gz";
        AbstractSequenceClassifier<CoreLabel> classifier = CRFClassifier.getClassifier(serializedClassifier);

        FileInputStream fis = new FileInputStream(fileName);
        BufferedReader reader = new BufferedReader(new InputStreamReader(fis, "UTF8"));

        FileOutputStream fos = new FileOutputStream(dstFile);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos, "UTF8"));

        String line = null;
        int idx = 1;
        while ((line = reader.readLine()) != null) {
            List<Triple<String, Integer, Integer>> triples = classifier.classifyToCharacterOffsets(line);

            for (Triple<String, Integer, Integer> trip : triples) {
                String ns = line.substring(trip.second, trip.third).replaceAll("\\s+", " ");
//                System.out.printf("%d\t%d\t%s\t%s\n", trip.second, trip.third, trip.first, ns);
                writer.write(String.format("%d\t%d\t%d\t%s\t%s\n", idx, trip.second, trip.third, trip.first, ns));
            }

            if (idx % 10000 == 0)
                System.out.println(idx);
            ++idx;
        }

        reader.close();
        writer.close();
    }

    public static void main(String[] args) throws Exception {
//        String textFile = "d:/data/quora/answer-text.txt";
//        String dstFile = "d:/data/quora/answer-text-ner.txt";
//        String textFile = "d:/data/fet/tweets-09-12-text.txt";
//        String dstFile = "d:/data/fet/tweets-09-12-text-ner.txt";
        String textFile = "d:/data/fet/tweets-1217-nodup-text.txt";
        String dstFile = "d:/data/fet/tweets-1217-nodup-text-ner.txt";
//        tryNER();
        processFile(textFile, dstFile);
    }
}
