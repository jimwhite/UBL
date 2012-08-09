#!/usr/bin/perl -w

$classpath = "/afs/csail.mit.edu/u/l/lsz/ccg/mallet-0.3.2/class/:/afs/csail.mit.edu/u/l/lsz/ccg/src/:.";

print "javac -classpath $classpath ~/ccg/src/*/*.java TestTrain.java\n";
print `javac -classpath $classpath ~/ccg/src/*/*.java TestTrain.java\n`;
print "java -Xmx1000m -classpath $classpath TestTrain  np-fixedlex.geo > run.test &\n";
print `java -Xmx1000m -classpath $classpath TestTrain  np-fixedlex.geo > run.test &\n`;
