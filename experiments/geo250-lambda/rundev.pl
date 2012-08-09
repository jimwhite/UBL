#!/usr/bin/perl -w

$language = $ARGV[0];
$split_num = $ARGV[1];
#$run_num = $ARGV[1];
$run_num = 0;


$classpath = "../../src/:.";

print "javac -classpath $classpath ../../src/*/*.java ./DevTrain.java\n";
print `javac -classpath $classpath ../../src/*/*.java ./DevTrain.java\n`;
print "java -Xmx1500m -classpath $classpath DevTrain $run_num $split_num $language  > run.dev.$language.$run_num.$split_num &\n";
print `java -Xmx1500m -classpath $classpath DevTrain $run_num $split_num $language  > run.dev.$language.$run_num.$split_num &\n`;


