package ust.dhl;

import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import ust.dhl.utils.IOUtils;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

/**
 * Created by p_hliangdai on 2017/4/27.
 */
public class ChineseSegmenter {
    public static CRFClassifier<CoreLabel> initSegmenter() {
        String basedir = "d:/lib/stanford-segmenter-2016-10-31/data";
        Properties props = new Properties();
        props.setProperty("sighanCorporaDict", basedir);
        // props.setProperty("NormalizationTable", "data/norm.simp.utf8");
        // props.setProperty("normTableEncoding", "UTF-8");
        // below is needed because CTBSegDocumentIteratorFactory accesses it
        props.setProperty("serDictionary", basedir + "/dict-chris6.ser.gz");
        props.setProperty("inputEncoding", "UTF-8");
        props.setProperty("sighanPostProcessing", "true");

        CRFClassifier<CoreLabel> segmenter = new CRFClassifier<>(props);
        segmenter.loadClassifierNoExceptions(basedir + "/ctb.gz", props);

        return segmenter;
    }

    static class SegThread extends Thread {
        String articlesFile;
        String dstFile;
        CRFClassifier<CoreLabel> segmenter;

        SegThread(CRFClassifier<CoreLabel> segmenter, String articlesFile, String dstFile) {
            this.segmenter = segmenter;
            this.articlesFile = articlesFile;
            this.dstFile = dstFile;
        }

        public void run() {
//            CRFClassifier<CoreLabel> segmenter = initSegmenter();
            try {
                BufferedReader reader = IOUtils.bufReader(articlesFile);
                BufferedWriter writer = IOUtils.bufWriter(dstFile);
                String line = null;
                int cnt = 0;
                while ((line = reader.readLine()) != null) {
                    List<String> segmented = segmenter.segmentString(line);
                    String segmentedText = String.join("\t", segmented);
                    writer.write(String.format("%s\n", segmentedText));

                    ++cnt;
//                    if (cnt == 10)
//                        break;
                    if (cnt % 1000 == 0)
                        System.out.println(cnt);
                }
                reader.close();
                writer.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void segmentParagraphs() throws Exception {
        CRFClassifier<CoreLabel> segmenter = initSegmenter();
        String paragraphsFile = "e:/data/wechat/sel_articles_contents.txt";
        String dstFile = "e:/data/wechat/sel_articles_contents_seg.txt";

        BufferedReader reader = IOUtils.bufReader(paragraphsFile);
        BufferedWriter writer = IOUtils.bufWriter(dstFile);
        String line = null;
        int cnt = 0;
        while ((line = reader.readLine()) != null) {
            writer.write(String.format("%s\n", line));

            String content = reader.readLine();
            List<String> segmented = segmenter.segmentString(content);
            String segmentedText = String.join("\t", segmented);
            writer.write(String.format("%s\n", segmentedText));

            ++cnt;
//            if (cnt == 10)
//                break;
            if (cnt % 1000 == 0)
                System.out.println(cnt);
        }
        reader.close();
        writer.close();
    }

    private static void segmentTextFile() {
        CRFClassifier<CoreLabel> segmenter = initSegmenter();
        for (int i = 0; i < 5; ++i) {
            String articlesFile = String.format("e:/data/wechat/tmp/public_articles_cleaned_%d.txt", i);
            String dstFile = String.format("e:/data/wechat/tmp/public_articles_seg_%d.txt", i);

            SegThread segThread = new SegThread(segmenter, articlesFile, dstFile);
            segThread.start();
        }
    }

    private static void segmentStdin() {
        CRFClassifier<CoreLabel> segmenter = initSegmenter();
        Scanner in = new Scanner(System.in);
        System.out.println("waiting for input:\n");
        while (in.hasNext()) {
            String text = in.next();
            List<String> segmented = segmenter.segmentString(text);
            String segmentedText = String.join(" ", segmented);
            System.out.println(segmentedText);
        }
    }

    private static void segmentNicknames() throws Exception {
        String nicknameFile = "e:/data/wechat/account_nickname.csv";
        String dstFile = "e:/data/wechat/account_nicknames_seg.txt";
        CRFClassifier<CoreLabel> segmenter = initSegmenter();

        BufferedReader reader = IOUtils.bufReader(nicknameFile);
        BufferedWriter writer = IOUtils.bufWriter(dstFile);
        String line = reader.readLine();
        int cnt = 0;
        while ((line = reader.readLine()) != null) {
//            System.out.println(line);
            String[] vals = line.split(",");

            List<String> segmented = segmenter.segmentString(vals[1]);
            String segmentedText = String.join(" ", segmented);
//            System.out.println(segmentedText);

            writer.write(String.format("%s\t%s\n", vals[0], segmentedText));
            ++cnt;
//            if (cnt == 100)
//                break;
            if (cnt % 10000 == 0)
                System.out.println(cnt);
        }
        reader.close();
        writer.close();
    }

    private static void segmentRedirects() throws Exception {
        String redirectsFile = "e:/data/res/wiki/redirects_cn.txt";
        String dstFile = "e:/data/res/wiki/redirects_cn_seg.txt";
        CRFClassifier<CoreLabel> segmenter = initSegmenter();

        BufferedReader reader = IOUtils.bufReader(redirectsFile);
        BufferedWriter writer = IOUtils.bufWriter(dstFile);
        String line = null;
        int cnt = 0;
        while ((line = reader.readLine()) != null) {
//            System.out.println(line);
            String[] vals = line.split("\t");

            List<String> segmented = segmenter.segmentString(vals[0]);
            String segmentedText0 = String.join("\t", segmented);

            segmented = segmenter.segmentString(vals[1]);
            String segmentedText1 = String.join("\t", segmented);
//            System.out.println(segmentedText);

            writer.write(String.format("%s\n%s\n", segmentedText0, segmentedText1));
            ++cnt;
//            if (cnt == 100)
//                break;
            if (cnt % 10000 == 0)
                System.out.println(cnt);
        }
        reader.close();
        writer.close();
    }

    private static void segmentOrgNames() throws Exception {
        String orgNamesFile = "e:/data/wechat/mentioned_org_names.txt";
        String dstFile = "e:/data/wechat/mentioned_org_names_seg.txt";
        CRFClassifier<CoreLabel> segmenter = initSegmenter();

        BufferedReader reader = IOUtils.bufReader(orgNamesFile);
        BufferedWriter writer = IOUtils.bufWriter(dstFile);
        String line = null;
        int cnt = 0;
        while ((line = reader.readLine()) != null) {
//            System.out.println(line);
            String[] vals = line.split("\t");

            List<String> segmented = segmenter.segmentString(vals[0]);
            String segmentedText = String.join("\t", segmented);
//            System.out.println(segmentedText);

            writer.write(String.format("%s\n", segmentedText));
            ++cnt;
//            if (cnt == 100)
//                break;
            if (cnt % 10000 == 0)
                System.out.println(cnt);
        }
        reader.close();
        writer.close();
    }

    public static void segmentTCP() throws Exception {
        CRFClassifier<CoreLabel> segmenter = initSegmenter();
        ServerSocket ss = new ServerSocket(7131);
        final String ENDSTR = "DHLDHLDHLEND";

        while (true) {
            Socket connSocket = ss.accept();
//            System.out.println("BEG");
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(
                    connSocket.getInputStream(), "UTF8"));

            StringBuilder sb = new StringBuilder();
            char[] buf = new char[5];
            while (true) {
                int n = inFromClient.read(buf);
//                System.out.println(n);
                sb.append(buf, 0, n);
                if (sb.length() > ENDSTR.length() && sb.substring(sb.length() - ENDSTR.length()).equals(ENDSTR)) {
                    break;
                }
            }
            String recvText = sb.substring(0, sb.length() - ENDSTR.length());
//            System.out.println("Received: " + recvText);
            List<String> segmented = segmenter.segmentString(recvText);
            String segmentedText = String.join(" ", segmented);

            DataOutputStream outToClient = new DataOutputStream(connSocket.getOutputStream());
            String result = segmentedText + ENDSTR;
//            capitalizedSentence.getBytes("UTF8");
            outToClient.write(result.getBytes("UTF8"));
            connSocket.close();
//            break;
        }
    }

    public static void main(String[] args) throws Exception {
//        segmentStdin();
//        segmentTextFile();
//        segmentNicknames();
//        segmentRedirects();
//        segmentOrgNames();
        segmentTCP();
//        segmentParagraphs();
    }
}
