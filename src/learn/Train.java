/***************************  LICENSE  *******************************
* This file is part of UBL.
* 
* UBL is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as 
* published by the Free Software Foundation, either version 3 of the 
* License, or (at your option) any later version.
* 
* UBL is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public 
* License along with UBL.  If not, see <http://www.gnu.org/licenses/>.
***********************************************************************/



package learn;

import java.util.*;
import java.io.*;
import lambda.*;
import parser.*;

public class Train { 
	public Train(){
	}

	// this function returns a set of lexical entries from 
	// Split and Merge operations on the maximum scoring 
	// correct parse.
	public List<LexEntry> makeLexEntriesChart(String words, Exp sem, Parser parser){
		String mes = null;
		if (verbose) mes = "MakeLex";
		parser.parseTimed(words,sem,mes);
		return parser.getChart().splitAndMergeLex(sem);
	}		

	boolean noAnswer;

	public boolean isCorrect(String words, Exp sem, Parser parser){
		List<ParseResult> parses = parser.bestParses();
		if (parses.size()>0){
			noAnswer = false;
		} else {
			noAnswer = true;
		}
		if (parses.size()==1){
			ParseResult p = parses.get(0);
			Exp e = p.getExp();
			e = e.copy();
			e.simplify();
			List l = p.getLexEntries();
			parsed++;
			if (e.equals(sem)){
				if (verbose){
					System.out.println("CORRECT");
					printLex(l);
				}
				int lits = sem.allLitsCount();
				correctParses++;		    

				return true;
			} else {
				// one parse, it was wrong... oh well...
				if (verbose){
					System.out.println("WRONG");
					System.out.println(parses.size()+" parses: "+parses);
					printLex(l);
				}
				wrongParses++;

				boolean hasCorrect = parser.hasParseFor(sem);
				if (verbose){
					System.out.println("Had correct parse: "+hasCorrect);
					System.out.print("Feats: ");
					Exp eb = parser.bestSem();
					Chart c = parser.getChart();
					HashVector h = c.computeExpFeatVals(eb);
					h.divideBy(c.computeNorm(eb));
					h.dropSmallEntries();
					System.out.println(h);
				}
			}
		} else { 
			noParses++;
			if (parses.size()>1){
				// There are more than one equally high scoring 
				// logical forms. If this is the case, we abstain
				// from returning a result.
				if (verbose){
					System.out.println("too many parses");
					System.out.println(parses.size()+" parses: "+parses);
				}
				Exp e = parses.get(0).getExp();
				ParseResult p = parses.get(0);
				boolean hasCorrect = parser.hasParseFor(sem);
				if (verbose) System.out.println("Had correct parse: "+hasCorrect);
			}else {
				// no parses, potentially reparse with word skipping
				if (verbose) System.out.println("no parses");
				if (emptyTest){
					List<LexEntry> emps = new LinkedList<LexEntry>();
					for (int j=0; j<Globals.tokens.size(); j++){
						List l = Globals.tokens.subList(j,j+1);
						LexEntry le = new LexEntry(l,Cat.EMP);
						emps.add(le);
					}

					parser.setTempLexicon(new Lexicon(emps));
					String mes=null;
					if (verbose) mes = "EMPTY";
					parser.parseTimed(words,null,mes);
					parser.setTempLexicon(null);
					parses = parser.bestParses();
					if (parses.size()==1){
						ParseResult p = parses.get(0);
						List l = p.getLexEntries();
						Exp e = p.getExp();
						e = e.copy();
						e.simplify();
						int noEmpty = p.noEmpty();
						if (e.equals(sem)){
							if (verbose){
								System.out.println("CORRECT");
								printLex(l);
							}
							emptyCorrect++;

						} else {
							// one parse, but wrong
							if (verbose){
								System.out.println("WRONG: "+e);
								printLex(l);
								boolean hasCorrect = parser.hasParseFor(sem);
								System.out.println("Had correct parse: "+hasCorrect);
							}
						}
					} else {
						// too many parses or no parses
						emptyNoParses++;
						if (verbose){
							System.out.println("WRONG:"+parses);
							boolean hasCorrect = parser.hasParseFor(sem);
							System.out.println("Had correct parse: "+hasCorrect);
						}
					}
				}
			}
		}
		return false;
	}

	
	// globals used to count the test statistics
	int correctParses =0;
	int noParses =0;
	int tooManyParses =0;
	int zeroParses =0;
	int parsed = 0;
	int wrongParses =0;
	int testSize=0;
	int emptyCorrect=0;
	int emptyWrong=0;
	int emptyNoParses=0;

	public double test(Parser p){
		double score = test(p,false);
		return score;
	}

	public double test(Parser p, boolean prunedLex){
		HashVector temp=null;
		resetCounts();
		for (int i=0; i<testData.size(); i++){
			if (verbose) System.out.println("\nFile #"+i+": "+testData.getFilename(i));
			test(p,testData.getDataSet(i));
		}
		printStats(prunedLex);
		return 0.0;
	}

	
	private void test(Parser p, DataSet d){

		int dsize = d.size();
		testSize=dsize;

		for (int i=0; i<dsize; i++){

			String words = d.sent(i);
			Exp sem = d.sem(i);
			if (verbose){
				System.out.println(i+": ==================("+correctParses+
						" -- "+wrongParses+")");
				System.out.println(words);
				System.out.println(sem);

			}

			String mes = null;
			if (verbose) mes = "Test";
			p.parseTimed(words,null,mes);
			isCorrect(words,sem,p);

		}

	}

	// these counts are used to hold test statistics
	public void resetCounts(){

		zeroParses =0;
		parsed = 0;
		tooManyParses=0;

		testSize=0;
		emptyCorrect=0;
		emptyNoParses=0;
		emptyWrong=0;
		correctParses=0;
		noParses=0;
		wrongParses=0;
	}

	// print out results after a test run.
	private void printStats(boolean prunedLex){
		String pruned = "";
		if (prunedLex){
			pruned = "pruned";
		}
		System.out.println("-----------------------------");
		double recall = (double)correctParses/(double)testSize;
		double precision = (correctParses/(double)(testSize-noParses));
		double f1 = (2*precision*recall)/(precision+recall);
		System.out.print(pruned+"Recall");
		System.out.println(" : "+correctParses+"/"+testSize+" = "+recall);
		System.out.print(pruned+"Precision");
		System.out.println(" : "+correctParses+"/"+
				(testSize-noParses)+" = "+precision);
		System.out.println(pruned+"F1: "+f1);
		System.out.println("No Parses : "+zeroParses);

		if (emptyTest){
			double emptyRecall = (double)(emptyCorrect+correctParses)/(double)testSize;
			double emptyPrecision = (double)(emptyCorrect+correctParses)/(double)(testSize-emptyNoParses);
			double emptyF1 = (2*emptyPrecision*emptyRecall)/(emptyPrecision+emptyRecall);

			System.out.println(pruned+"EMPTY Recall : "+(emptyCorrect+correctParses)
					+"/"+testSize+" = "+emptyRecall);
			System.out.println(pruned+"EMPTY Precision : "+(emptyCorrect+correctParses)
					+"/"+(testSize-emptyNoParses)+" = "+emptyPrecision);
			System.out.println(pruned+"EMPTY F1: "+emptyF1);
		}
		System.out.println("-----------");	
	}

	
	public void setDataSet(DataSet d){
		trainData = new DataSetWrapper(d);
	}

	public void setTestSet(DataSet d){
		testData = new DataSetWrapper(d);
	}


	public void setFixed(Lexicon l){
		fixedLex = l;
	}
	
	DataSetWrapper trainData;
	DataSetWrapper testData;

	// this is used to hold any initial lexical items 
	// with which the system is primed.
	Lexicon fixedLex;

	
	public void printLex(List l){
		Iterator j = l.iterator();
		System.out.println("[LexEntries and scores:");
		while (j.hasNext()){
			LexEntry le = (LexEntry) j.next();
			int index = Globals.lexPhi.indexOf(le);
			System.out.print(le +" : "+index);
			if (index!=-1) System.out.print(" : "+Globals.theta.get("LEX:"+index));

			System.out.println();
		}
		System.out.println("]");
	}



	/*  The input Parser has all of the features that will
	 *  be used during parsing as well as the initial lexicon.   
	 *  This function updates feature weights and adds new 
	 *  lexical entries.
	 */

	public void stocGradTrain(Parser parser){
		stocGradTrain(parser,false);
	}

	public void stocGradTrain(Parser parser, boolean testEachRound){

		int numUpdates=0;
		
		List<LexEntry> fixedEntries = new LinkedList<LexEntry>();
		fixedEntries.addAll(parser.returnLex().getLexicon());

		// add all sentential lexical entries.
		for (int l=0;l<trainData.size(); l++){
			parser.addLexEntries(trainData.getDataSet(l).makeSentEntries());
		}
		parser.setGlobals();
		
		DataSet data=null;
		// for each pass over the data
		for (int j=0; j<EPOCHS; j++){
			System.out.println("Training, iteration "+j);
			int total=0, correct=0, wrong=0, looCorrect=0, looWrong=0;
			for (int l=0;l<trainData.size(); l++){
				
				// the variables to hold the current training example
				String words=null;
				Exp sem = null;

				data = trainData.getDataSet(l);
				if (verbose) System.out.println("---------------------");
				String filename = trainData.getFilename(l);
				if (verbose) System.out.println("DataSet: "+filename);
				if (verbose) System.out.println("---------------------");

				// loop through the training examples
				// try to create lexical entries for each training example
				for (int i=0; i<data.size(); i++){
					// print running stats
					if (verbose){
						if (total!=0) { 
							double r = (double)correct/total; 
							double p = (double)correct/(correct+wrong);
							System.out.print(i+": =========== r:"+r+" p:"+p);
							System.out.println(" (epoch:"+j+" file:"+l+" "+filename+")");
						} else System.out.println(i+": ===========");
					}

					// get the training example
					words = data.sent(i);	    
					sem = data.sem(i);
					if (verbose){
						System.out.println(words);
						System.out.println(sem);
					}

					List<String> tokens = Parser.tokenize(words);

					if (tokens.size()>maxSentLen) continue;
					total++;

					String mes = null;
					boolean hasCorrect=false;

					// first, get all possible lexical entries from
					// a manipulation of the best parse.
					List<LexEntry> lex = 			
						makeLexEntriesChart(words,sem,parser);
					
					if (verbose){
						System.out.println("Adding:");
						for (LexEntry le : lex){
							System.out.println(le+" : "
									+LexiconFeatSet.initialWeight(le));
						}
					}
					
					parser.addLexEntries(lex);		   

					if (verbose) System.out.println("Lex Size: "+parser.returnLex().size());

					// first parse to see if we are currently correct
					if (verbose) mes = "First";
					parser.parseTimed(words,null,mes);

					Chart firstChart = parser.getChart();
					Exp best = parser.bestSem();
					
					// this just collates and outputs the training 
					// accuracy.
					if (sem.equals(best)){ 
						//System.out.println(parser.bestParses().get(0));
						if (verbose){ 
							System.out.println("CORRECT:"+best);
							lex = parser.getMaxLexEntriesFor(sem);
							System.out.println("Using:");
							printLex(lex);
							if (lex.size()==0){
								System.out.println("ERROR: empty lex");
							}
						}
						correct++;
					} else {
						if (verbose){
							System.out.println("WRONG: "+best);
							lex = parser.getMaxLexEntriesFor(best);
							System.out.println("Using:");
							printLex(lex);
							if (best!=null && lex.size()==0){
								System.out.println("ERROR: empty lex");
							}
						}
						wrong++;
					}

					
					// compute first half of parameter update:
					// subtract the expectation of parameters 
					// under the distribution that is conditioned 
					// on the sentence alone.
					double norm = firstChart.computeNorm();
					HashVector update = new HashVector();
					HashVector firstfeats=null, secondfeats=null;
					if (norm!=0.0){
						firstfeats = firstChart.computeExpFeatVals();
						firstfeats.divideBy(norm);
						firstfeats.dropSmallEntries();
						firstfeats.addTimesInto(-1.0,update);
					} else continue;
					firstChart=null;

					
					if (verbose) mes = "Second";
					parser.parseTimed(words,sem,mes);
					hasCorrect = parser.hasParseFor(sem);
					
					// compute second half of parameter update:
					// add the expectation of parameters 
					// under the distribution that is conditioned 
					// on the sentence and correct logical form.
					if (!hasCorrect) continue;
					Chart secondChart = parser.getChart();
					double secnorm = secondChart.computeNorm(sem);
					if (norm!=0.0){
						secondfeats = secondChart.computeExpFeatVals(sem);
						secondfeats.divideBy(secnorm);
						secondfeats.dropSmallEntries();
						secondfeats.addTimesInto(1.0,update);
						lex = parser.getMaxLexEntriesFor(sem);
						data.setBestLex(i,lex);
						if (verbose){
							System.out.println("Best LexEntries:");
							printLex(lex);
							if (lex.size()==0){
								System.out.println("ERROR: empty lex");
							}
						}
					} else continue;

					// now do the update
					double scale = alpha_0/(1.0+c*numUpdates);
					if (verbose) System.out.println("Scale: "+scale);
					update.multiplyBy(scale);
					update.dropSmallEntries();

					numUpdates++;
					if (verbose) {
						System.out.println("Update:");
						System.out.println(update);
					}
					if (!update.isBad()) {
						if (!update.valuesInRange(-100,100)){
							System.out.println("WARNING: large update");
							System.out.println("first feats: "+firstfeats);
							System.out.println("second feats: "+secondfeats);
						}
						parser.updateParams(update);
					} else {
						System.out.println("ERROR: Bad Update: "+update
								+" -- norm: "+norm
								+" -- feats: ");
						parser.getParams().printValues(update);
						System.out.println();
					}		 

				}  // end for each training example
			}  // end for each data set

			double r = (double)correct/total; 

			// we can prune the lexical items that were not used
			// in a max scoring parse. 
			if (pruneLex){
				Lexicon cur = new Lexicon();
				cur.addLexEntries(fixedEntries);
				cur.addLexEntries(data.getBestLex());
				parser.setLexicon(cur);
			}

			if (testEachRound){
				System.out.println("Testing");
				test(parser,false);
			}
		} // end epochs loop
	}

	int numUpdates;
	HashVector totals = null;

	// global parameters for how to learn
	public static int maxSentLen=50;
	public static int EPOCHS=10;

	// add empty words during test?
	public static boolean emptyTest=true;

	// use co-ocurrence counts to initialise 
	// parameters for lexical entries?
	public static boolean CoocInit=true;

	// print out everything that's going on?
	public static boolean verbose=false;

	// these are used to define the temperature
	// of parameter updates. 
	// temp = alpha_0/(1+c*tot_number_of_training_instances)
	public static double alpha_0=0.1;
	public static double c=0.0001;

	// prune the lexicon at the end of each training epoch?
	public static boolean pruneLex=false;
}
