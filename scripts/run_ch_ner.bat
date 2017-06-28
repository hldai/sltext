set bin_dir=../out/production/textproc
set stanford_segmenter=d:/lib/stanford-segmenter-2016-10-31/stanford-segmenter-3.7.0.jar
set classpath=%bin_dir%;%stanford_segmenter%

set articles_file=e:/data/wechat/public_articles_cleaned_0.txt
set dst_file=e:/data/wechat/public_articles_seg_0.txt
java -Xmx4g -cp %classpath% ust.dhl.ChineseNER %articles_file% %dst_file%
