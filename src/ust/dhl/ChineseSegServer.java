package ust.dhl;

import py4j.GatewayServer;

import java.util.List;

/**
 * Created by p_hliangdai on 2017/8/11.
 */
public class ChineseSegServer {
    private static final String SEG_BASE_DIR = "d:/lib/stanford-segmenter-2016-10-31/data";
    private static final String EXCLUDE_WORDS_FILE = "e:/data/res/seg_exclude_words.txt";
    private ChSegmenterELWS segmenter = new ChSegmenterELWS(EXCLUDE_WORDS_FILE, SEG_BASE_DIR);

    public String segment(String text) {
        List<String> segmented = segmenter.segmentString(text);
        String segmentedText = String.join(" ", segmented);
        return segmentedText;
    }

    public static void main(String[] args) {
        ChineseSegServer chineseSegServer = new ChineseSegServer();
        GatewayServer server = new GatewayServer(chineseSegServer);
        System.out.println("Start server");
        server.start();
    }
}
