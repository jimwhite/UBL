#!/usr/bin/perl -w

$language = $ARGV[0];

$classpath = "../../src/:.";

print "javac -classpath $classpath ../../src/*/*.java ./DevTrain.java\n";
print `javac -classpath $classpath ../../src/*/*.java ./DevTrain.java\n`;

print "rm run.dev.outputs/$language/*\n";
print `rm run.dev.outputs/$language/*\n`;

for ($run_num=0; $run_num<10; $run_num++){
    for ($split_num=0; $split_num<10; $split_num++){
	print `java -Xmx800m -classpath $classpath DevTrain $run_num $split_num $language > run.dev.outputs/$language/run.dev.$run_num.$split_num\n`;
    }
}
