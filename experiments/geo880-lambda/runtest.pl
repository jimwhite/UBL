#!/usr/bin/perl -w

$classpath = "../../src:.";

print "javac -classpath $classpath ../../src/*/*.java TestTrain.java\n";
print `javac -classpath $classpath ../../src/*/*.java TestTrain.java\n`;
print "java -server -Xmx1500m -classpath $classpath TestTrain  np-fixedlex.geo > run.test &\n";
print `java -server -Xmx1500m -classpath $classpath TestTrain  np-fixedlex.geo > run.test &\n`;
