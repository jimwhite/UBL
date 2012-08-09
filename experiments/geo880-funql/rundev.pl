#!/usr/bin/perl -w

$fold = $ARGV[0];

$classpath = "../../src/:.";

print "javac -classpath $classpath ../../src/*/*.java ./DevTrain.java\n";
print `javac -classpath $classpath ../../src/*/*.java ./DevTrain.java\n`;

#print "java -server -Xmx1900m -classpath $classpath DevTrain $i np-fixedlex.geo > run.dev.$i\n";
print `java -server -Xmx1900m -classpath $classpath DevTrain $fold np-fixedlex-funql.geo > run.dev.$fold & \n`;


