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
public class ChineseSegmenter {
    private CRFClassifier<CoreLabel> segmenter;
    private Set<String> excludeWords = new HashSet<>();

    public ChineseSegmenter(String excludeWordsFile, String dataBaseDir) {
        initSegmenter(dataBaseDir);
        initExcludeWords(excludeWordsFile);
    }

    private void initExcludeWords(String excludeWordsFile) {
        try {
            BufferedReader reader = IOUtils.bufReader(excludeWordsFile);
            String line = null;
            while ((line = reader.readLine()) != null) {
//                System.out.println(line);
                excludeWords.add(line);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initSegmenter(String dataBaseDir) {
//        String basedir = "d:/lib/stanford-segmenter-2016-10-31/data";
        Properties props = new Properties();
        props.setProperty("sighanCorporaDict", dataBaseDir);
        // props.setProperty("NormalizationTable", "data/norm.simp.utf8");
        // props.setProperty("normTableEncoding", "UTF-8");
        // below is needed because CTBSegDocumentIteratorFactory accesses it
        props.setProperty("serDictionary", dataBaseDir + "/dict-chris6.ser.gz");
        props.setProperty("inputEncoding", "UTF-8");
        props.setProperty("sighanPostProcessing", "true");

        segmenter = new CRFClassifier<>(props);
        segmenter.loadClassifierNoExceptions(dataBaseDir + "/ctb.gz", props);
    }

    public List<String> segmentString(String text) {
        return segmentString(text, false);
    }

    public List<String> segmentString(String text, boolean extraProcess) {
        if (!extraProcess) {
            return segmenter.segmentString(text);
        }

        List<String> segmented = segmenter.segmentString(text);
        LinkedList<String> segmentedNew = new LinkedList<>();
        for (String w : segmented) {
            if (w.equals("市卫")) {
                segmentedNew.add(w.substring(0, 1));
                segmentedNew.add(w.substring(1));
                continue;
            }

            if (w.length() <= 2 || excludeWords.contains(w)) {
//                if (w.length() <= 2 && w.startsWith("市")) {
//                    System.out.println(w);
//                }
                segmentedNew.add(w);
            } else if (w.endsWith("省") || w.endsWith("市") || w.endsWith("乡")
                    || w.endsWith("镇") || w.endsWith("区") || w.endsWith("县") || w.endsWith("局")
                    || w.endsWith("厅") || w.endsWith("会")) {
                segmentedNew.add(w.substring(0, w.length() - 1));
                segmentedNew.add(w.substring(w.length() - 1));
            } else if (w.startsWith("省") || w.startsWith("市") || w.startsWith("乡")
                    || w.startsWith("区")) {
//                System.out.println("in");
                segmentedNew.add(w.substring(0, 1));
                segmentedNew.add(w.substring(1));
            } else {
                segmentedNew.add(w);
            }
        }

        return segmentedNew;
    }
}
