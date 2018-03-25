package ust.dhl;

import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import ust.dhl.utils.IOUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * Created by p_hliangdai on 2018/3/24.
 */
public class ChSeg {
    private static final String SEG_BASE_DIR = "d:/lib/stanford-segmenter-2016-10-31/data";

    private static CRFClassifier<CoreLabel> getSegmenter() {
        CRFClassifier<CoreLabel> segmenter;
        Properties props = new Properties();
        props.setProperty("sighanCorporaDict", SEG_BASE_DIR);
        // props.setProperty("NormalizationTable", "data/norm.simp.utf8");
        // props.setProperty("normTableEncoding", "UTF-8");
        // below is needed because CTBSegDocumentIteratorFactory accesses it
        props.setProperty("serDictionary", SEG_BASE_DIR + "/dict-chris6.ser.gz");
        props.setProperty("inputEncoding", "UTF-8");
        props.setProperty("sighanPostProcessing", "true");

        segmenter = new CRFClassifier<>(props);
        segmenter.loadClassifierNoExceptions(SEG_BASE_DIR + "/ctb.gz", props);
        return segmenter;
    }

    public static void segmentLineFile(String fileName, String dstFileName) throws IOException {
        CRFClassifier<CoreLabel> segmenter = getSegmenter();
        BufferedReader reader = IOUtils.bufReader(fileName);
        BufferedWriter writer = IOUtils.bufWriter(dstFileName);
        String line = null;
        int cnt = 0;
        while ((line = reader.readLine()) != null) {
            List<String> segmented = segmenter.segmentString(line);
            Iterator<String> it = segmented.iterator();
            while (it.hasNext()) {
                writer.write(it.next());
                if (it.hasNext())
                    writer.write(" ");
            }
            writer.write("\n");

            ++cnt;
            if (cnt % 1000 == 0)
                System.out.println(cnt);
        }
        reader.close();
        writer.close();
    }

    public static void main(String[] args) throws Exception {
        segmentLineFile("d:/data/indec/docs-14k-content.txt",
                "d:/data/indec/docs-14k-content-seg.txt");
    }
}
