package ust.dhl;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.parser.nndep.DependencyParser;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.TypedDependency;
import edu.stanford.nlp.util.logging.Redwood;
import ust.dhl.utils.IOUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Collection;

public class DepParse {
    public static void parseLineFile(String fileName, String dstFileName) throws IOException {
        String modelPath = DependencyParser.DEFAULT_MODEL;
        String taggerPath = "edu/stanford/nlp/models/pos-tagger/english-left3words/english-left3words-distsim.tagger";
        MaxentTagger tagger = new MaxentTagger(taggerPath);
        DependencyParser parser = DependencyParser.loadFromModelFile(modelPath);

        String line = null;
        BufferedReader reader = IOUtils.bufReader(fileName);
        BufferedWriter writer = IOUtils.bufWriter(dstFileName);
        int cnt = 0;
        while ((line = reader.readLine()) != null) {
            LinkedList<Word> sent = new LinkedList<>();
            String[] words = line.split(" ");
            for (String w: words)
                sent.add(new Word(w));

            List<TaggedWord> tagged = tagger.tagSentence(sent);
            GrammaticalStructure gs = parser.predict(tagged);
//            System.out.println(gs);
            Collection<TypedDependency> tdList = gs.typedDependencies();
            for (TypedDependency td : tdList) {
                IndexedWord dep = td.dep(), gov = td.gov();
                writer.write(String.format("%s-%d %s-%d %s\n", gov.value(), gov.index(), dep.value(), dep.index(),
                        td.reln()));
//                System.out.println(dep.value() + "-" + dep.index() + " " + gov.value() + "-" + gov.index()
//                        + " " + td.reln());
            }
            writer.write("\n");
//            System.out.println();
            if (cnt % 1000 == 0) {
                System.out.println(cnt);
            }

            ++cnt;
            // Print typed dependencies
        }

        reader.close();
        writer.close();
    }

    public static void main(String[] args) throws IOException {
        parseLineFile("d:/data/aspect/huliu04/sents-text.txt",
                "d:/data/aspect/huliu04/sents-text-dep.txt");
    }
}
