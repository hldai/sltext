package ust.dhl;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.SentenceUtils;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.parser.shiftreduce.ShiftReduceParser;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.WordToSentenceProcessor;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.trees.Tree;
import ust.dhl.utils.IOUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Tokenize {
    static class TokenizeThread extends Thread {
        String reviewsFile;
        String dstFile;

        public TokenizeThread(String reviewsFile, String dstFile) {
            this.reviewsFile = reviewsFile;
            this.dstFile = dstFile;
//            this.tf = tf;
        }

        public void run() {
            CoreLabelTokenFactory tf = new CoreLabelTokenFactory();
            try {
                BufferedReader reader = IOUtils.bufReader(reviewsFile);
                BufferedWriter writer = IOUtils.bufWriter(dstFile);
                YelpReview review = null;
                int cnt = 0;
                while ((review = YelpReview.nextReview(reader)) != null) {
                    PTBTokenizer ptbt = new PTBTokenizer<CoreLabel>(new StringReader(review.text), tf,
                            "untokenizable=noneKeep");
                    List<CoreLabel> tokens = ptbt.tokenize();
                    writer.write(String.format("%s\t%d\n", review.reviewId, tokens.size()));
                    for (CoreLabel cl : tokens) {
                        String curWord = review.text.substring(cl.beginPosition(), cl.endPosition());
                        writer.write(String.format("%s\t%d\t%d\n", curWord,
                                cl.beginPosition(), cl.endPosition()));
                    }

                    ++cnt;
                    if (cnt % 10000 == 0)
                        System.out.println(cnt);
//                    if (cnt == 10)
//                        break;
                }

                reader.close();
                writer.close();
//                System.out.println(cnt);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    static class ParseThread extends Thread {
        String reviewsFile;
        String dstFile;
        LexicalizedParser lp;
        CoreLabelTokenFactory tf = new CoreLabelTokenFactory();
        WordToSentenceProcessor<CoreLabel> wordToSentenceProcessor = new WordToSentenceProcessor<CoreLabel>();

        public ParseThread(String reviewsFile, String dstFile, LexicalizedParser lp) {
            this.reviewsFile = reviewsFile;
            this.dstFile = dstFile;
            this.lp = lp;
        }

        private List<List<CoreLabel>> toSentences(String text) {
            List<CoreLabel> tokens = new ArrayList<CoreLabel>();
            PTBTokenizer<CoreLabel> tokenizer = new PTBTokenizer<CoreLabel>(new StringReader(text), tf, "");
            while (tokenizer.hasNext()) {
                tokens.add(tokenizer.next());
            }
            return wordToSentenceProcessor.process(tokens);
        }

        public void run() {
            try {
                BufferedReader reader = IOUtils.bufReader(reviewsFile);
                BufferedWriter writer = IOUtils.bufWriter(dstFile);
                int cnt = 0;
                YelpReview review;
                while ((review = YelpReview.nextReview(reader)) != null) {
                    List<List<CoreLabel>> sents = toSentences(review.text);
                    int numWords = 0;
                    for (List<CoreLabel> sent : sents) {
                        numWords += sent.size();
                    }

                    writer.write(String.format("%s\t%d\n", review.reviewId, numWords));
                    for (List<CoreLabel> sent : sents) {
                        Tree parse = lp.apply(sent);
                        List<Tree> leaves = parse.getLeaves();
                        assert leaves.size() == sent.size();

                        Iterator<CoreLabel> liter = sent.iterator();
                        for (Tree n : leaves) {
                            CoreLabel l = liter.next();
                            writer.write(String.format("%s\t%d\t%d\t%s\n", l.value(), l.beginPosition(),
                                    l.endPosition(), n.parent(parse).value()));
                        }
                    }

                    ++cnt;
                    if (cnt % 1000 == 0)
                        System.out.println(cnt);
//                    if (cnt == 10)
//                        break;
                }

                reader.close();
                writer.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static List<List<TaggedWord>> tagText(String text, MaxentTagger tagger) {
        StringReader stringReader = new StringReader(text);
        List<List<HasWord>> sentences = MaxentTagger.tokenizeText(stringReader);

        List<List<TaggedWord>> taggedSentences = new LinkedList<List<TaggedWord>>();
        for (List<HasWord> sentence : sentences) {
            List<TaggedWord> taggedWords = tagger.apply(sentence);
//			for (TaggedWord w : taggedWords) {
////				System.out.println(String.format("%s\t%s\t%d\t%d", w.value(), w.tag(), w.beginPosition(), w.endPosition()));
//				System.out.println(String.format("%s\t%s", text.substring(w.beginPosition(), w.endPosition()), w.tag()));
//			}
            taggedSentences.add(taggedWords);
        }

        return taggedSentences;
    }

    private static void shiftReduceParse() {
        String text = "We ate at In last night.";

        String modelPath = "d:/lib/models/englishSR.ser.gz";
//		String taggerPath = "d:/lib/stanford-postagger-2016-10-31/models/english-left3words-distsim.tagger";
//      String taggerPath = "d:/lib/models/wsj-0-18-left3words-distsim.tagger";
        String taggerPath = "d:/lib/models/wsj-0-18-bidirectional-distsim.tagger";
//        String taggerPath = "d:/lib/stanford-postagger-2016-10-31/models/english-bidirectional-distsim.tagger";

        MaxentTagger tagger = new MaxentTagger(taggerPath);
//        List<List<HasWord>> sentences = MaxentTagger.tokenizeText(new StringReader(text));
//        for (List<HasWord> sentence : sentences) {
//            List<TaggedWord> tSentence = tagger.tagSentence(sentence);
//            System.out.println(SentenceUtils.listToString(tSentence, false));
//        }
        ShiftReduceParser model = ShiftReduceParser.loadModel(modelPath);

//        for (List<HasWord> sentence : tokenizer) {
//            List<TaggedWord> tagged = tagger.tagSentence(sentence);
//            Tree tree = model.apply(tagged);
//            log.info(tree);
//        }
        List<List<TaggedWord>> taggedSentences = tagText(text, tagger);
        for (List<TaggedWord> sentence : taggedSentences) {
            Tree tree = model.apply(sentence);
            System.out.println(tree.toString());
            for (TaggedWord tw : sentence) {
                System.out.println(String.format("%s\t%s", tw.value(), tw.tag()));
            }
        }
    }

    private static void lexicalizedParse() throws IOException {
//        String[] sent = { "This", "is", "an", "easy", "sentence", "." };
//        String[] sent = { "We", "ate", "at", "In", "today", "." };
        String[] texts = {"We ate at In last night.", "This is an easy sentence."};

        LexicalizedParser lp = LexicalizedParser.loadModel("d:/lib/models/englishPCFG.ser.gz");

        CoreLabelTokenFactory tf = new CoreLabelTokenFactory();

        for (String text : texts) {
            PTBTokenizer<CoreLabel> ptbt = new PTBTokenizer<CoreLabel>(new StringReader(text), tf,
                    "untokenizable=noneKeep");
            List<CoreLabel> sent = ptbt.tokenize();
            for (CoreLabel cl : sent) {
                System.out.println(cl.value() + "\t" + cl.beginPosition() + "\t" + cl.endPosition());
            }

            Tree parse = lp.apply(sent);
            List<Tree> leaves = parse.getLeaves();
            for (Tree n : leaves) {
                System.out.println(n.value() + "\t" + n.parent(parse).label().value());
            }
//        parse.pennPrint();
        }
    }

    private static void parseMT() throws Exception {
        LexicalizedParser lp = LexicalizedParser.loadModel("d:/lib/models/englishPCFG.ser.gz");

        final int numThreads = 4;
        for (int i = 0; i < numThreads; ++i) {
            String reviewsFile = String.format("e:/data/yelp/split/yelp_reviews_%d.txt", i);
            String dstFile = String.format("e:/data/yelp/split/yelp_reviews_pos_%d.txt", i);

            ParseThread parseThread = new ParseThread(reviewsFile, dstFile, lp);
            parseThread.start();
        }
    }

    private static void tokenizeMT() throws Exception {
//        LexicalizedParser lp = LexicalizedParser.loadModel("d:/lib/models/englishPCFG.ser.gz");
//        CoreLabelTokenFactory tf = new CoreLabelTokenFactory();

        final int numThreads = 4;
        for (int i = 0; i < numThreads; ++i) {
//            if (i == 0)
//                continue;
            String reviewsFile = String.format("d:/data/yelp/split/yelp_reviews_%d.txt", i);
            String dstFile = String.format("d:/data/yelp/split/yelp_reviews_tok_%d.txt", i);

            TokenizeThread tokenizeThread = new TokenizeThread(reviewsFile, dstFile);
            tokenizeThread.start();
        }
    }

    public static void tokenizeEveryLine() throws Exception {
//        String filename = "d:/data/yelp/tmp/biz_names.txt";
//        String dstFile = "d:/data/yelp/tmp/biz_names_tokenized.txt";
        String filename = "d:/data/yelp/tmp/mention_name_str.txt";
        String dstFile = "d:/data/yelp/auxiliary/mention_name_str_tokenized.txt";

        CoreLabelTokenFactory tf = new CoreLabelTokenFactory();
        BufferedReader reader = IOUtils.bufReader(filename);
        BufferedWriter writer = IOUtils.bufWriter(dstFile);
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
        tokenizeEveryLine();
//        parseMT();
//        tokenizeMT();
//        lexicalizedParse();
    }
}
