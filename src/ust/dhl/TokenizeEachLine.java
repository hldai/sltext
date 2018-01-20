package ust.dhl;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;

import java.io.*;
import java.util.List;

/**
 * Created by p_hliangdai on 2018/1/10.
 * Tokenize each line of a text file.
 */
public class TokenizeEachLine {
    public static void tokenizeEveryLine(String srcFileName, String dstFileName) throws Exception {
        CoreLabelTokenFactory tf = new CoreLabelTokenFactory();

        BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(srcFileName), "UTF8"));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(dstFileName), "UTF8"));

        String line = null;
        while ((line = reader.readLine()) != null) {
            PTBTokenizer ptbt = new PTBTokenizer<CoreLabel>(new StringReader(line), tf,
                    "untokenizable=noneKeep");
            List<CoreLabel> tokens = ptbt.tokenize();
            boolean first = true;
            for (CoreLabel cl : tokens) {
                if (first) {
                    writer.write(cl.value());
                    first = false;
                } else {
                    writer.write(String.format(" %s", cl.value()));
                }
            }
            writer.write("\n");
        }
        reader.close();
        writer.close();
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("TokenizeEachLine [src_file] [dst_file]");
            return;
        }

        tokenizeEveryLine(args[0], args[1]);
    }
}
