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
    public static void parseLineFile(String fileName, String dstDepFileName,
                                     String dstPosFileName, boolean filterShortSents) throws IOException {
        String modelPath = DependencyParser.DEFAULT_MODEL;
        String taggerPath = "edu/stanford/nlp/models/pos-tagger/english-left3words/english-left3words-distsim.tagger";
        MaxentTagger tagger = new MaxentTagger(taggerPath);
        DependencyParser parser = DependencyParser.loadFromModelFile(modelPath);

        String line = null;
        BufferedReader reader = IOUtils.bufReader(fileName);
        BufferedWriter depWriter = IOUtils.bufWriter(dstDepFileName);
        BufferedWriter posWriter = IOUtils.bufWriter(dstPosFileName);
        int cnt = 0;
        while ((line = reader.readLine()) != null) {
            if (cnt % 10000 == 0) {
                System.out.println(cnt);
            }
            ++cnt;

            LinkedList<Word> sent = new LinkedList<>();
            String[] words = line.split(" ");
            if (filterShortSents && words.length < 5)
                continue;

            for (String w: words) {
                if (w.equals("("))
                    w = "-LRB-";
                else if (w.equals(")"))
                    w = "-RRB-";
                sent.add(new Word(w));
            }

            List<TaggedWord> tagged = tagger.tagSentence(sent);

            for (TaggedWord tw : tagged) {
                posWriter.write(tw.tag() + "\n");
//                System.out.println(tw.tag());
            }
            posWriter.write("\n");

            GrammaticalStructure gs = parser.predict(tagged);
//            System.out.println(gs);
            Collection<TypedDependency> tdList = gs.typedDependencies();
            for (TypedDependency td : tdList) {
                IndexedWord dep = td.dep(), gov = td.gov();
                depWriter.write(String.format("%s-%d %s-%d %s\n", gov.value(), gov.index(), dep.value(),
                        dep.index(), td.reln()));
//                System.out.println(dep.value() + "-" + dep.index() + " " + gov.value() + "-" + gov.index()
//                        + " " + td.reln());
            }
            depWriter.write("\n");
//            System.out.println();
//            if (cnt > 1000)
//                break;
            // Print typed dependencies
        }

        reader.close();
        depWriter.close();
        posWriter.close();
    }

    public static void parseTest() throws IOException {
        String modelPath = DependencyParser.DEFAULT_MODEL;
        String taggerPath = "edu/stanford/nlp/models/pos-tagger/english-left3words/english-left3words-distsim.tagger";
        MaxentTagger tagger = new MaxentTagger(taggerPath);
        DependencyParser parser = DependencyParser.loadFromModelFile(modelPath);

        String line = "boot time is super fast .";
        LinkedList<Word> sent = new LinkedList<>();
        String[] words = line.split(" ");
        for (String w: words)
            sent.add(new Word(w));

        List<TaggedWord> tagged = tagger.tagSentence(sent);
        GrammaticalStructure gs = parser.predict(tagged);
        System.out.println(tagged);
        Collection<TypedDependency> tdList = gs.typedDependencies();
        for (TypedDependency td : tdList) {
            IndexedWord dep = td.dep(), gov = td.gov();
            System.out.println(String.format("%s-%d %s-%d %s", gov.value(), gov.index(), dep.value(),
                    dep.index(), td.reln()));
//                System.out.println(dep.value() + "-" + dep.index() + " " + gov.value() + "-" + gov.index()
//                        + " " + td.reln());
        }
    }

    public static void main(String[] args) throws IOException {
//        parseTest();
//        parseLineFile("d:/data/aspect/huliu04/sents-text.txt",
//                "d:/data/aspect/huliu04/sents-text-dep.txt",
//                "d:/data/aspect/huliu04/sents-text-pos.txt");

//        parseLineFile("d:/data/aspect/semeval14/laptops_train_texts_tok.txt",
//                "d:/data/aspect/semeval14/laptops-train-rule-dep.txt",
//                "d:/data/aspect/semeval14/laptops-train-rule-pos.txt");

//        parseLineFile("d:/data/aspect/semeval14/laptops_test_texts_tok.txt",
//                "d:/data/aspect/semeval14/laptops-test-rule-dep.txt",
//                "d:/data/aspect/semeval14/laptops-test-rule-pos.txt");

//        parseLineFile("d:/data/amazon/laptops-reivews-sent-tok-text.txt",
//                "d:/data/amazon/laptops-rule-dep.txt",
//                "d:/data/amazon/laptops-rule-pos.txt");

//        parseLineFile("d:/data/aspect/semeval14/restaurant/restaurants_train_texts_tok.txt",
//                "d:/data/aspect/semeval14/restaurant/restaurants-train-rule-dep.txt",
//                "d:/data/aspect/semeval14/restaurant/restaurants-train-rule-pos.txt");

//        parseLineFile("d:/data/aspect/semeval14/restaurant/restaurants_test_texts_tok.txt",
//                "d:/data/aspect/semeval14/restaurant/restaurants-test-rule-dep.txt",
//                "d:/data/aspect/semeval14/restaurant/restaurants-test-rule-pos.txt");

        parseLineFile("d:/data/res/yelp-review-eng-tok-sents-round-9.txt",
                "d:/data/res/yelp-review-round-9-dep.txt",
                "d:/data/res/yelp-review-round-9-pos.txt", false);
    }
}
