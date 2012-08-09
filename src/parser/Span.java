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





 package parser;

 import java.util.*;
 import java.io.*;
 import lambda.*;
 import learn.*;

 public class Span {

     public Span(List<String> words, Cat c){
	 begin=0;
	 end=words.size()-1;
	 tokens = new LinkedList<String>();
	 tokens.addAll(words);
	 cat=c;
	 subSpans = new LinkedList<List<Span>>();
	 /*
	 List<String> consts = c.getSem().getConstStrings();
	 for (String word : words) {	 
	     for (String con : consts){	 
		 System.out.println(word +" : "+con+" : "+indexScore(word,con));
	     }
	 }	 
	 */
     }

     public Span(int b, int e, List<String> words, Cat c){
	 begin=b;
	 end=e;
	 tokens = new LinkedList<String>();
	 tokens.addAll(words);
	 cat=c;
	 subSpans = new LinkedList<List<Span>>();
     }

     static List<String> allWords=null;
     static List<String> allConsts=null;

     static public List<LexEntry> expandNTimes(Span input, int n){
	 PriorityQueue<Span> pq =
	     new PriorityQueue<Span>(10,new Comparator<Span>() {
		     public int compare(Span i, Span j) {
			 return (int)(j.bound()-i.bound());
		     }
		 });
	 
	 pq.offer(input);
	 List<LexEntry> lex = new LinkedList<LexEntry>();
	 List<Span> finished = new LinkedList<Span>();

	 // set globals...
	 allWords = input.tokens;
	 allConsts = input.cat.getSem().getConstStrings();

	 for (int j=0; j<n; j++){
	     Span s =  null;
	     do {
		 s=pq.poll();
		 if (s==null) return lex;
	     } while(finished.contains(s));
	     finished.add(s);
	     lex.add(s.getLexEntry());
	     //System.out.println(s);
	     //  enumerate all pairs of subspans
	     //System.out.println("spliting: "+s);
	     List<List<Cat>> possibleSplits = s.cat.allSplits();
	     int numSplits = possibleSplits.size();
	     for (int splitNum = 0; splitNum<numSplits; splitNum++){
		 List<Cat> split = possibleSplits.get(splitNum);
		 for (int i = 0; i<s.tokens.size()-1; i++){
		     Span s1 = new Span(s.begin,s.begin+i,s.tokens.subList(0,i+1),split.get(0));
		     Span s2 = new Span(s.begin+i+1,s.end,s.tokens.subList(i+1,s.tokens.size()),split.get(1));
		     if (!finished.contains(s1)){
			 //lex.add(s1.getLexEntry());
			 pq.offer(s1);
		     }
		     if (!finished.contains(s2)){
			 //lex.add(s2.getLexEntry());
			 //System.out.println("adding: "+s1+" -- "+s2);
			 pq.offer(s2);
		     }
		 }
	     }		     	     
	     //System.out.println("size "+j+": "+pq.size());
	 }

	 /*
	 System.out.println("##############");
	 for (LexEntry l : popped){
	     if (initialWeight(l)>0.0){
		 System.out.println(l);
	     }
	 }
	 System.out.println("##############");
	 */
	 return lex;
     }

     public void sampleSubSpan(Parser parser,Boolean useScores){ 
	 // parser is ignored right now...
	 if (begin==end) return;
	 //System.out.println("BEGIN: "+this);

	 // first, enumerate all pairs of subspans
	 
	 // this version does not sample all pairs of categories
	 List<List<Cat>> possibleSplits = cat.allSplits();
	 int numSplits = possibleSplits.size();	 
	 // this returns all splits of the logical form
	 // want to put these in the list of possible subspans
	 //List<List<Exp>> possibleExpSplits = Cat.allExpSplits(cat);

	 	 
	 List<List<Span>> possibleSubSpans = new LinkedList<List<Span>>();
	 //	 List<List<Span>> seenSubSpans = new LinkedList<List<Span>>();
	 
	 //List<List<Cat>> seen_splits = new LinkedList<List<Cat>>;
	 for (int splitNum = 0; splitNum<numSplits; splitNum++){
		 List<Cat> split = possibleSplits.get(splitNum);
		 //for (int seenSplitNum = 0; seenSplitNum<seen_splits.size(); seenSplitNum++){
		 //	 List<Cat> seenSplit = seen_splits.get(seenSplitNum);
		 //	 if 
		 //}
	     for (int i = 0; i<tokens.size()-1; i++){
		 List<Span> tempSpan = new LinkedList<Span>();
		 tempSpan.add(new Span(begin,begin+i,
				       tokens.subList(0,i+1),split.get(0)));
		 tempSpan.add(new Span(begin+i+1,end,
				       tokens.subList(i+1,tokens.size()),split.get(1)));
		 possibleSubSpans.add(tempSpan);
		 //System.out.println("option: "+tempSpan);
	    }
	 }	
	 
	 //System.out.println("poss sub spans size: "+possibleSubSpans.size());
	 if (possibleSubSpans.size()==0) return;
	 
	 // compute the scores, normalize, and sample one
	 double[] scores = new double[possibleSubSpans.size()];
	 double total = 0.0;
	 
	 for (int i=0; i<scores.length; i++){
	    List<Span> split = possibleSubSpans.get(i);
		int seen_l_matches = 0;
		int seen_r_matches = 0;
		Span span_l = split.get(0);
		Span span_r = split.get(1);
		double score_l = span_l.score();
		double score_r = span_r.score();
		if (!useScores){
		    score_l = 0.0;
		    score_r = 0.0;
		}
		    
		if (sampleWithChart && parser.chart!=null){
		    Iterator c_l = parser.chart.getCellsIterator(span_l.begin,span_l.end);
		    while (c_l.hasNext()){
			Cell chart_c = (Cell)c_l.next();
			// need to match cell.myCat.getSem
			if (chart_c.myCat.equals(span_l.cat)){
			    //if (seen_l_matches > 0)
				//	System.out.println("MORE THAN ONE MATCH IN CHART FOR THIS");
				//seen_l_matches ++ ;
				//System.out.println("Got match for "+span_l);
				//System.out.println("From split "+split);
				if (chart_c.maxscore > score_l)
				    score_l = chart_c.maxscore;
			}
		    }
		    Iterator c_r = parser.chart.getCellsIterator(span_r.begin,span_r.end);
		    while (c_r.hasNext()){
			Cell chart_c = (Cell)c_r.next();
			// need to match cell.myCat.getSem
			if (chart_c.myCat.equals(span_r.cat)){
			    //if (seen_r_matches > 0)
			    //	System.out.println("MORE THAN ONE MATCH IN CHART FOR THIS");
			    //seen_r_matches ++ ;
			    //System.out.println("Got match for "+span_r);
			    //System.out.println("From split "+split);
			    if (chart_c.maxscore > score_r)
				score_r = chart_c.maxscore;
			}
		    }
		}
		scores[i]=score_l+score_r;
	    //if (scores[i]<score()) scores[i]=0.0;
	    total+=scores[i];
	}
	//if (total<0.1) return;

	Random rand = new Random();
	double samp = rand.nextDouble();
	samp*=total;
	total = 0;
	int index = 0;
	double maxScore = 0;
	for (int i=0; i<scores.length; i++){
	    total+=scores[i];
	    if (sample && total>samp){
		index=i;
		break;
	    }
	    if (!sample && scores[i]>maxScore){
		index=i;
		maxScore=scores[i];
	    }
	}

	// recurse
	List<Span> newSpans = possibleSubSpans.get(index);
	
	
	subSpans.add(newSpans);
	//System.out.println("sampled: "+newSpans);
	//System.out.println("###########################");
	newSpans.get(0).sampleSubSpan(parser,useScores);
	newSpans.get(1).sampleSubSpan(parser,useScores);
     }



     public void getLexEntries(List<LexEntry> result){
	 //if (subSpans.size()==0){
	 LexEntry l = new LexEntry(tokens,cat);
	 if (!result.contains(l)){
	     //System.out.println("Span added: "+l);
	     result.add(l);
	 }
	 //}
	 for (List<Span> sp : subSpans){
	     for (Span s : sp){
		 s.getLexEntries(result);
	     }
	 }
     }

     public double score(){
	 //return Math.exp(score(tokens,cat));
	 return score(tokens,cat);
	 //return upperBound(tokens,cat.getSem().getConstStrings());
     }

     public double bound(){
	 List<String> otherWords = new LinkedList<String>();
	 otherWords.addAll(allWords);
	 otherWords.removeAll(tokens);
	 List<String> otherConsts = new LinkedList<String>();
	 List<String> myConsts = cat.getSem().getConstStrings();
	 otherConsts.addAll(allConsts);
	 otherConsts.removeAll(myConsts);
	 return upperBound(tokens,myConsts)+upperBound(otherWords,otherConsts);
     }

     static private double upperBound(List<String> words, List<String> consts){
	 //System.out.print("*");
	 //if (pmis==null) return 1;
	 double total = 0.0;
	 double maxTotal = 0.0;
	 for (String word : words) {	 
	     double maxScore = -10000.0;
	     for (String con : consts){	 
		 // System.out.println(word +" : "+con);
		 double score = indexScoreBound(word,con);		 
		 total+=score;
		 if (score>maxScore)
		     maxScore=score;
	     }
	     double nullScore = indexScoreBound(word,"null");
	     if (nullScore>maxScore) maxScore= nullScore;
	     
	     maxTotal+=maxScore;
	 }
	 return maxTotal;
     }

     public static double initialWeight(LexEntry le){
	 double complexityScore = 1.0
	     -0.1*le.getCat().numSlashes()
	     -0.1*le.getCat().getSem().getConstStrings().size();
	 
	 //return complexityScore;
	 return initWeightMultiplier*score_wc(le.getTokens(),le.getCat());//+complexityScore;
	 //	 return 10*score_wc(le.getTokens(),le.getCat());//+complexityScore;

     }
     
     public LexEntry getLexEntry(){
	 return new LexEntry(tokens,cat);
     }
     
     static private double score_wc(List<String> words, Cat cat){
	 Exp sem = cat.getSem();
	 //if (pmis==null) return 1;
	 double total = 0.0;
	 double maxTotal = 0.0;
	 List<String> consts = sem.getConstStrings();
	 for (String word : words) {	 
	     double maxScore = 0.0;
	     for (String con : consts){	 
		 // System.out.println(word +" : "+con);
		 double score = indexScore(word,con);		 
		 total+=score;
		 if (score>maxScore)
		     maxScore=score;
	     }
	     maxTotal+=maxScore;
	 }
	 //if (consts.size()==0) return 0;
	 if (consts.size()==0) {
	     // compute score with null
	     String con = "null";
	     double maxScore = 0.0;
	     for (String word : words) {	 
		 // System.out.println(word +" : "+con);
		 double score = indexScore(word,con);		 
		 total+=score;
		 if (score>maxScore)
		     maxScore=score;
	     }
	     maxTotal+=maxScore;
	 }
	 //double score = 0;
	 //if (consts.size()==0)
	 //score = total/words.size();
	 //else 
	 return total/(words.size()*(consts.size()+1));
	 //score = score*10.0; // - 0.1*cat.numSlashes();
	 //score = total;
	 //return Math.max(0.0,score);
	 //return score;
	 //return total-1.0*cat.numSlashes();
	 //return Math.min(score,10.0);
	 //return 10.0*maxTotal/words.size();
	 //return 10.0*total/words.size();
	 //return maxTotal;
     }

     static private double score(List<String> words, Cat cat){
	 Exp sem = cat.getSem();
	 double total = 0.0;
	 double maxTotal = 0.0;
	 List<String> consts = sem.getConstStrings();
	 for (String con : consts){	 	    
	     double maxScore = 0.0;
	     for (String word : words) {	 
// System.out.println(word +" : "+con);
		 double score = indexScore(word,con);		 
		 total+=score;
		 if (score>maxScore)
		     maxScore=score;
	     }
	     maxTotal+=maxScore;
	 }
	 if (consts.size()==0) return 0;
	 //return total - (0.1*consts.size()*words.size());
	 return maxTotal/consts.size();
     }

     private static double indexScore(String word, String con){
	 //Integer top = pairCounts.get(word+":"+con);
	 //if (top==null) return 0.0;
	 //return (double)top.intValue();
	 //Double d = lexScores.get(word+":"+con);
	 Double d = pmis.get(word+":"+con);
	 if (d==null) return 0.0;
	 return d.doubleValue();//*10.0-3.0;
	 //System.out.println("HERE!!!");
     }

     private static double indexScoreBound(String word, String con){
	 //Integer top = pairCounts.get(word+":"+con);
	 //if (top==null) return 0.0;
	 //return (double)top.intValue();
	 Double d = lexScores.get(word+":"+con);
	 //Double d = pmis.get(word+":"+con);
	 if (d==null) return 0.0;
	 return d.doubleValue();
     }

     public boolean equals(Object o){
	 if (o instanceof Span){
	     Span s = (Span)o;
	     return begin==s.begin && end==s.end && cat.equals(s.cat);
	 } 
	 return false;
     }
     
     public void printPairScores(){
	 List<String> consts = cat.getSem().getConstStrings();
	 for (String con : consts){	 
	     double maxScore = 0.1;
	     for (String word : tokens) {	 
		 System.out.println(word +" : "+con+" = "+indexScore(word,con));
	     }
	 }
     }
     
     public String toString(){
	 return "{"+tokens+" : "+cat+" : "+begin+" : "+end+"} = "+score();
     }

     public static void loadPMIs(String filename){
	 pmis = new HashMap<String,Double>();	
	 try{
	     BufferedReader in = new BufferedReader(new FileReader(filename));
	     String line = in.readLine();
	     while (line!=null){  // for each line in the file
		 line.trim();
		 line = line.split("\\s*//")[0];
		 if (!line.equals("")){
		     String[] tokens = line.split("..\\:\\:..");
		     String id = tokens[1]+":"+tokens[0];
		     double score = Double.parseDouble(tokens[2]);
		     pmis.put(id,new Double(score));
		 }
		 line = in.readLine();
	     }
	     
	 } catch(IOException e){ System.err.println(e); }
	 lexScores = new HashMap<String,Double>();
	 lexScores.putAll(pmis);
     }

     static public void updateLexScore(List<LexEntry> lexicon){
	 lexScores = new HashMap<String,Double>();
	 lexScores.putAll(pmis);
	 for (LexEntry le : lexicon){
	     for (String word : le.getTokens()){
		 for (String con : le.getCat().getSem().getConstStrings()){
		     int index = Globals.lexPhi.indexOf(le);		     
		     Double score = Globals.theta.get("LEX:"+index);
		     if (score==null) continue;
		     String label = word+":"+con;
		     Double oldMax = lexScores.get(label);
		     if (oldMax==null || score.doubleValue()>oldMax.doubleValue())
			 lexScores.put(label,score);
		 }
	     }
	 }
     }


     static public void count(DataSet d){
	 pairCounts = new HashMap<String,Integer>();
	 wordCounts = new HashMap<String,Integer>();
	 constCounts = new HashMap<String,Integer>();
	 Integer c;

	 for (int i=0; i<d.size(); i++){
	     //System.out.println("HEREH!!!");
	     List<String> cons = d.sem(i).getConstStrings();
	     for (String con : cons){
		 c = constCounts.get(con);
		 if (c==null)
		     constCounts.put(con, new Integer(0));
		 else 
		     constCounts.put(con, new Integer(c.intValue()+1));
	     }

	     for (String word : (List<String>)Parser.tokenize(d.sent(i))){

		 c = wordCounts.get(word);
		 if (c==null)
		     wordCounts.put(word, new Integer(0));
		 else 
		     wordCounts.put(word, new Integer(c.intValue()+1));
		 for (String con : cons){
		     String index = word+":"+con;
		     c = pairCounts.get(index);
		     if (c==null)
			 pairCounts.put(index, new Integer(0));
		     else {
			 //System.out.println("index: "+index);
			 pairCounts.put(index, new Integer(c.intValue()+1));
		     }
		 }
	     }
	 }
     }

     static Map<String,Integer> pairCounts;
     static Map<String,Integer> wordCounts;
     static Map<String,Integer> constCounts;

     static Map<String,Double> pmis = new HashMap<String,Double>();
     static Map<String,Double> lexScores= new HashMap<String,Double>();

     public static boolean sample=true;  // sample vs. return max
     public static boolean sampleWithChart=true;  // sample vs. return max


     public static double initWeightMultiplier = 10.0;

     int begin;
     int end;
     Cat cat;
     List<String> tokens;
     List<List<Span>> subSpans;
     
     public static void main(String[] args){
	 PType.addTypesFromFile("../experiments/geo880/geo880.types");
	 Lang.loadLangFromFile("../experiments/geo880/geo880.lang");
	 Span.loadPMIs("../experiments/geo880/w-c.giza_probs");

	 //DataSet train = new DataSet("../experiments/clang/data/clang300.ccg");
	 //Span.count(train);

	 //what is the state with the lowest point
	 //(lambda $0 e (and (state:t $0) (loc:t (argmin $1 (place:t $1) (elevation:i $1)) $0)))

	 //which state has the highest peak in the country
	 //(lambda $0 e (and (state:t $0) (loc:t (argmax $1 (and (mountain:t $1) (loc:t $1 usa:co)) (elevation:i $1)) $0)))

	 Cat c = Cat.makeCat("S : (argmax $0 (and (place:t $0) (loc:t $0 (argmax $1 (state:t $1) (count $2 (and (river:t $2) (loc:t $2 $1)))))) (elevation:i $0))");
	 List<String> tokens = Parser.tokenize("what is the highest point in the state with the most rivers");
	 Span s = new Span(tokens,c);
	 //s.printPairScores();
	 //for (int i=0; i<100; i++)
	 Span.sample = false;
	 s.sampleSubSpan(new Parser(),true);

	 List<LexEntry> lex = new LinkedList<LexEntry>(); //Span.expandNTimes(s,10);
	 s.getLexEntries(lex);
	 double score=0.0;
	 for (LexEntry l : lex){
	     double sc = Span.initialWeight(l);
	     System.out.println(l+ " : "+sc);
	     score+=sc;
	 }
	 System.out.println("total score: "+score);


     }
     
}
