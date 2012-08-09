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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import lambda.Exp;
import learn.*;

/**
 * The lexical features, also deals with the co-occurence 
 * statistics used for initialisation.
 **/
public class LexiconFeatSet implements LexicalFeatureSet {

	public void setFeats(LexEntry l, HashVector feats){
		int i = indexOf(l);
		if (i!=-1){
			if (feats.get("LEX:"+i)>100) 
				System.out.println("LARGE LEX feats: "+l);
			feats.set("LEX:"+i,feats.get("LEX:"+i)+1.0);
		}
	}

	public double score(LexEntry l, HashVector theta){
		if (l==null) return 0.0;
		int i= indexOf(l);
		if (i!=-1) 
			return theta.get("LEX:"+i);
		if (l.getCat().equalsNoSem(Cat.EMP))
			return -1.0;
		if (Train.CoocInit)
			return initialWeight(l);
		return -0.5;  // unknown. 
	}


	public int numFeats(){
		return size();
	}

	public void setStartIndex(int index){
		offset = index;
	}

	public void addLexEntry(LexEntry l, HashVector theta){
		if (!lexItems.containsKey(l)){
			int i = lexItems.size();
			lexItems.put(l.copy(),i);

			if (Train.CoocInit){
				theta.set("LEX:"+i,initialWeight(l));

			} else {
				theta.set("LEX:"+i,-0.1);
			}
		}
	}

	
	//< constructors

	public LexiconFeatSet(){
		lexItems = new LinkedHashMap<LexEntry,Integer>();
	}

	public LexiconFeatSet(List l, int oset, HashVector theta){
		offset = oset;
		lexItems = new LinkedHashMap<LexEntry,Integer>();
		addLexEntries(l);

		// initialize the parameters for the initial lexical entries
		for (Map.Entry<LexEntry,Integer> me : lexItems.entrySet()){
			LexEntry le = me.getKey();
			int i = me.getValue().intValue();
			theta.set("LEX:"+i,initLexWeight*le.getTokens().size());
		}
	}

	public LexiconFeatSet(int oset){
		offset = oset;
		lexItems = new LinkedHashMap<LexEntry,Integer>();
	}

	public LexiconFeatSet(List l){
		offset = 0;
		lexItems = new LinkedHashMap<LexEntry,Integer>();
		addLexEntries(l);
	}

	//>

	//< addLexEntries
	public void addLexEntries(List l){
		Iterator i = l.iterator();
		while (i.hasNext()){
			add((LexEntry)i.next());
		}
	}
	//>

	//< accessors: contains, add, get, size, clear, indexOf, toString
	public boolean contains(LexEntry l){
		return lexItems.containsKey(l);
	}

	public void setOffset(int i){
		offset = i;
	}

	public void add(LexEntry l){
		if (!lexItems.containsKey(l))
			lexItems.put(l,new Integer(lexItems.size()));
	}

	/**
	 * load co-occurence counts from file
	 */
	public static void loadCoOccCounts(String filename){
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
	
    public static double initialWeight(LexEntry le){
        return initWeightMultiplier*score_wc(le.getTokens(),le.getCat());                                                                
    }



	static private double score_wc(List<String> words, Cat cat){
		Exp sem = cat.getSem();
		double total = 0.0;
		double maxTotal = 0.0;
		List<String> consts = sem.getConstStrings();
		for (String word : words) {	 
			double maxScore = 0.0;
			for (String con : consts){	 
				double score = indexScore(word,con);		 
				total+=score;
				if (score>maxScore)
					maxScore=score;
			}
			maxTotal+=maxScore;
		}
		if (consts.size()==0) {
			// compute score with null
			String con = "null";
			double maxScore = 0.0;
			for (String word : words) {	 
				double score = indexScore(word,con);		 
				total+=score;
				if (score>maxScore)
					maxScore=score;
			}
			maxTotal+=maxScore;
		}

		return total/(words.size()*(consts.size()+1));
	}

	private static double indexScore(String word, String con){
		Double d = pmis.get(word+":"+con);
		if (d==null) return 0.0;
		return d.doubleValue();
	}

	public int size(){
		return lexItems.size();
	}

	public int lastFeatureNum(){
		return lexItems.size()+offset;
	}

	public int firstFeatureNum(){
		return offset;
	}

	public void clear(){
		lexItems.clear();
	}

	public int indexOf(LexEntry l){
		Integer i = lexItems.get(l);
		if (i==null) return -1;
		return i.intValue();
	}

	public boolean hasEntryFor(List words){
		Iterator i = lexItems.keySet().iterator();
		while (i.hasNext()){
			LexEntry le = (LexEntry)i.next();
			if (le.hasWords(words))
				return true;
		}
		return false;
	}

	public String toString(){
		return lexItems.toString();
	}

	// this is the weight by which the co-occurence 
	// statistics get multiplied.
    public static double initWeightMultiplier = 10.0;

    // this is the initial weight assigned to lexical
    // items that are loaded before training
	static public double initLexWeight = 10.0;

	static Map<String,Integer> pairCounts;
	static Map<String,Integer> wordCounts;
	static Map<String,Integer> constCounts;

	static Map<String,Double> pmis = new HashMap<String,Double>();
	static Map<String,Double> lexScores= new HashMap<String,Double>();

	int offset;
	Map<LexEntry,Integer> lexItems;
	//List<LexEntry> lexItems;

}
