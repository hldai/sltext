package ust.dhl;

import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import ust.dhl.utils.IOUtils;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

/**
 * Created by p_hliangdai on 2017/4/27.
 */
public class Segmentation {
    private static final String SEG_BASE_DIR = "d:/lib/stanford-segmenter-2016-10-31/data";
    private static final String EXCLUDE_WORDS_FILE = "e:/data/res/seg_exclude_words.txt";

    static class SegThread extends Thread {
        String paragraphsFile;
        String dstFile;
        ChineseSegmenter segmenter;

        SegThread(ChineseSegmenter segmenter, String paragraphsFile, String dstFile) {
            this.segmenter = segmenter;
            this.paragraphsFile = paragraphsFile;
            this.dstFile = dstFile;
        }

        public void run() {
//            CRFClassifier<CoreLabel> segmenter = initSegmenter();
            try {
                segmentParagraphs(segmenter, paragraphsFile, dstFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void segmentParagraphs(ChineseSegmenter segmenter, String paragraphsFile,
                                          String dstFile) throws Exception {
        BufferedReader reader = IOUtils.bufReader(paragraphsFile);
        BufferedWriter writer = IOUtils.bufWriter(dstFile);
        String line = null;
        int cnt = 0;
        while ((line = reader.readLine()) != null) {
            writer.write(String.format("%s\n", line));

            String content = reader.readLine();
            List<String> segmented = segmenter.segmentString(content, false);
            String segmentedText = String.join("\t", segmented);
            writer.write(String.format("%s\n", segmentedText));

            ++cnt;
//            if (cnt == 1000)
//                break;
            if (cnt % 10000 == 0)
                System.out.println(cnt);
        }
        reader.close();
        writer.close();
    }

    private static void segmentParagraphsJob() throws Exception {
//        CRFClassifier<CoreLabel> segmenter = initSegmenter();

//        String paragraphsFile = "e:/data/wechat/sel_articles_contents.txt";
//        String dstFile = "e:/data/wechat/sel_articles_contents_seg.txt";

        ChineseSegmenter segmenter = new ChineseSegmenter(EXCLUDE_WORDS_FILE, SEG_BASE_DIR);
//        String paragraphsFile = "e:/data/wechat/split/content_20w_0.txt";
//        String dstFile = "e:/data/wechat/split/content_20w_0_seg.txt";
//        segmentParagraphs(segmenter, paragraphsFile, dstFile);
        for (int i = 1; i < 4; ++i) {
            String paragraphsFile = String.format("e:/data/wechat/split/content_20w_%d.txt", i);
            String dstFile = String.format("e:/data/wechat/split/content_20w_%d_seg.txt", i);

            segmentParagraphs(segmenter, paragraphsFile, dstFile);
        }
    }

    private static void segmentParagraphsMP() {
        ChineseSegmenter segmenter = new ChineseSegmenter(EXCLUDE_WORDS_FILE, SEG_BASE_DIR);
        for (int i = 0; i < 4; ++i) {
            String paragraphsFile = String.format("e:/data/wechat/split/content_20w_%d.txt", i);
            String dstFile = String.format("e:/data/wechat/split/content_20w_%d_seg.txt", i);

            SegThread segThread = new SegThread(segmenter, paragraphsFile, dstFile);
            segThread.start();
        }
    }

//    private static void segmentTextFile() {
//        CRFClassifier<CoreLabel> segmenter = initSegmenter();
//        for (int i = 0; i < 5; ++i) {
//            String articlesFile = String.format("e:/data/wechat/tmp/public_articles_cleaned_%d.txt", i);
//            String dstFile = String.format("e:/data/wechat/tmp/public_articles_seg_%d.txt", i);
//
//            SegThread segThread = new SegThread(segmenter, articlesFile, dstFile);
//            segThread.start();
//        }
//    }

    private static void segmentStdin() {
        ChineseSegmenter segmenter = new ChineseSegmenter(EXCLUDE_WORDS_FILE, SEG_BASE_DIR);
        Scanner in = new Scanner(System.in);
        System.out.println("waiting for input:\n");
        while (in.hasNext()) {
            String text = in.next();
//            List<String> segmented = segmenter.segmentString(text);
            List<String> segmented = segmenter.segmentString(text);
            String segmentedText = String.join(" ", segmented);
            System.out.println(segmentedText);
        }
    }

    private static void segmentNicknames() throws Exception {
//        String nicknameFile = "e:/data/wechat/account_nickname.csv";
//        String dstFile = "e:/data/wechat/account_nicknames_seg.txt";
        String nicknameFile = "e:/data/wechat/account_nickname_fil.txt";
        String dstFile = "e:/data/wechat/account_nickname_fil_seg.txt";
//        CRFClassifier<CoreLabel> segmenter = initSegmenter();
        ChineseSegmenter segmenter = new ChineseSegmenter(EXCLUDE_WORDS_FILE, SEG_BASE_DIR);

        BufferedReader reader = IOUtils.bufReader(nicknameFile);
        BufferedWriter writer = IOUtils.bufWriter(dstFile);
        String line;
//        reader.readLine();
        int cnt = 0;
        while ((line = reader.readLine()) != null) {
//            System.out.println(line);
//            String[] vals = line.split(",");
            String[] vals = line.split("\t");

            List<String> segmented = segmenter.segmentString(vals[1]);
//            List<String> segmented = adjustedSegment(segmenter, vals[1]);
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
//        CRFClassifier<CoreLabel> segmenter = initSegmenter();
        ChineseSegmenter segmenter = new ChineseSegmenter(EXCLUDE_WORDS_FILE, SEG_BASE_DIR);

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
//        CRFClassifier<CoreLabel> segmenter = initSegmenter();
        ChineseSegmenter segmenter = new ChineseSegmenter(EXCLUDE_WORDS_FILE, SEG_BASE_DIR);

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

    private static void segmentTCP() throws Exception {
//        CRFClassifier<CoreLabel> segmenter = initSegmenter();
        ChineseSegmenter segmenter = new ChineseSegmenter(EXCLUDE_WORDS_FILE, SEG_BASE_DIR);
        ServerSocket ss = new ServerSocket(7131);
        final String ENDSTR = "DHLDHLDHLEND";
        System.out.println("server started.");

        while (true) {
            System.out.println("WAIT");
            Socket connSocket = ss.accept();
            System.out.println("BEG");
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
            System.out.println("Received: " + recvText);
            List<String> segmented = segmenter.segmentString(recvText);
            String segmentedText = String.join(" ", segmented);
            System.out.println("seg: " + segmentedText);

            DataOutputStream outToClient = new DataOutputStream(connSocket.getOutputStream());
            String result = segmentedText + ENDSTR;
//            capitalizedSentence.getBytes("UTF8");
            outToClient.write(result.getBytes("UTF8"));
            System.out.println("send");
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
//        segmentParagraphsJob();
//        segmentParagraphsMP();
    }
}
