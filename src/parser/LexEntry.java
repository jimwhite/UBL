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

/**
 * A Lexical Entry associates a sequence of tokens with a category,
 * and thereby a semantics. 
 * 
 * @specfield List<String> myTokens - what should be recognized as this
 * entry. The fact that it's a list allows multi-word entries.
 * 
 * @specfield Cat myCat - this entry's category.
 **/

public class LexEntry {

    //< public LexEntry(String t, String c)
    /**
     * This constructor uses a StringTokenizer to parse the tokens
     * from t, and makeCat to parse the category from c.  
     *
     * @throws CRASH if c is not a valid category.
     **/
    public LexEntry(String t, String c){
	myTokens = new LinkedList();
	StringTokenizer st = new StringTokenizer(t);
	/* TODO: add regex "Tokens" */
	while (st.hasMoreTokens()){
	    myTokens.add(st.nextToken());
	}
	myCat = Cat.makeCat(c);
	if (myCat.getSem()!=null && !myCat.getSem().wellTyped()){
	    System.err.println("sem "+myCat.getSem()+" is not well typed");
		System.err.println("type is "+myCat.getSem().inferType());
	    System.exit(-1);
	}
    }
    //>
    //< public LexEntry (List t, Cat c) - unprotected constructor, for copy?
    public LexEntry(List t, Cat c){
	myTokens = t;
	myCat = c.copy();
    }
    //>


    //< Cell = makeCellFor(List input, int index) - new Cell or null
    /**
     * @return a new cell if this lexical item is at index in the input, otherwise return null
     */
    public Cell makeCellFor(List input, int index){
	//System.out.println("makeCellFor "+input+" index "+index);
	for (int i=0; i<myTokens.size(); i++){
	    if (i+index==input.size())
		return null;
	    if (!myTokens.get(i).equals(input.get(index+i)))
		return null;
	}

	return new Cell(myCat,index,index+myTokens.size()-1,null);
    }

    //>
    //< toString()
    public String toString(){
	StringBuffer result = new StringBuffer();
	Iterator i = myTokens.iterator();
	while(i.hasNext()){
	    result.append(i.next().toString()).append(" ");
	}
	result.append(" :- ").append(myCat.toString());//.append("\n");
	return result.toString();
    }
    //>

    //< hasWords(List<String>words)
    /**
     * @return true iff the words passed in are exactly my tokens.
     **/
    public boolean hasWords(List words){
	return myTokens.equals(words);
    }

    public boolean hasWord(String s){
	return myTokens.contains(s);
    }

    public boolean startsWith(List<String> tokens){
	if (tokens.size()>myTokens.size()) return false;
	for (int i=0; i<tokens.size(); i++)
	    if (!tokens.get(i).equals(myTokens.get(i)))
		return false;
	return true;
    }

    public boolean endsWith(List<String> tokens){
	if (tokens.size()>myTokens.size()) return false;
	for (int i=1; i<=tokens.size(); i++)
	    if (!tokens.get(tokens.size()-i)
		.equals(myTokens.get(myTokens.size()-1)))
		return false;
	return true;
    }
    //>

    //< getters
    public Cat getCat(){
	return myCat;
    }
    public List<String> getTokens(){
	return myTokens;
    }
    //>
    //< copy factory
    public LexEntry copy(){
	return new LexEntry(new LinkedList(myTokens),myCat.copy());
    }
    //>
    //< equals
    /**
     * A LexEntry's notion of equality is if both tokens and category are equal.
     **/
    public boolean equals(Object o){
	if (! (o instanceof LexEntry))
	    return false;

	LexEntry e = (LexEntry)o;
	if (myTokens.size()!=e.myTokens.size()) return false;
	return myTokens.equals(e.myTokens) && myCat.equals(e.myCat);
    }

    public int hashCode() {
	int hc = 17;
	hc = 37*hc + myCat.hashCode();
	hc = 37*hc + myTokens.hashCode();
	return hc;
    }
    //>

    //< accessor functions for state variables used during learning
    public boolean isBegin(){
	return begin;
    }

    public void setBegin(boolean value){
	begin=value;
    }
    boolean begin = false;

    public boolean isDomainSpecific(){
	return domains;
    }

    public void setDomainSpecific(boolean value){
	domains=value;
    }
    boolean domains = false;


    public boolean isNew = false;
    
    //>

    int count=0;
    public int getCount(){ return count; }
    public void incCount(){ count++; }


    public int goodCount=0;
    public int badCount=0;

    public boolean loaded =false;

    boolean expanded =false;

    Cat myCat;
    List myTokens;

    static public List<LexEntry> splitCats(List<Cat> oneSplit, List<Cat> twoSplit,
					   List<String> oneTokens, List<String> twoTokens){
	List<LexEntry> result = new LinkedList<LexEntry>();
	LexEntry temp=null;
	if (oneSplit.get(0).equals(twoSplit.get(0))){
	    // make LexEntries for splitting off the common prefix sequences
	    int i=0;
	    while (i<oneTokens.size()-1 && i<twoTokens.size()-1 &&
		   oneTokens.get(i).equals(twoTokens.get(i))){
		// shared entry
		temp = new LexEntry(oneTokens.subList(0,i+1),oneSplit.get(0));
		if (!result.contains(temp)) result.add(temp);
		// different entries
		temp = new LexEntry(oneTokens.subList(i+1,oneTokens.size()),oneSplit.get(1));
		if (!result.contains(temp)) result.add(temp);
		//result.add(new LexEntry(twoTokens.subList(i+1,twoTokens.size()),
		//			twoSplit.get(1)));
		i++;
	    }
	    
	}

	if (oneSplit.get(1).equals(twoSplit.get(1))){
	    // make LexEntries for splitting off the common suffix sequences
	    int i=1;
	    while (i<oneTokens.size() && i<twoTokens.size() &&
		   oneTokens.get(oneTokens.size()-i)
		   .equals(twoTokens.get(twoTokens.size()-i))){
		// shared entry
		temp = new LexEntry(oneTokens.subList(oneTokens.size()-i,
						      oneTokens.size()),
				    oneSplit.get(1));
		if (!result.contains(temp)) result.add(temp);
		// different entries
		temp = new LexEntry(oneTokens.subList(0,oneTokens.size()-i),
				    oneSplit.get(0));
		if (!result.contains(temp)) result.add(temp);
		//result.add(new LexEntry(twoTokens.subList(0,twoTokens.size()-i),
		//			twoSplit.get(0)));
		i++;
	    }	    
	}
	return result;
    }
   
    
    static public List<LexEntry> splitEntries(LexEntry one, LexEntry two){
	List<String> oneTokens = one.getTokens();
	List<String> twoTokens = two.getTokens();

	//if (one.expanded && two.expanded)
	//    return null;
	if (oneTokens.size()==1 && twoTokens.size()==1){
	    return null;
	}
	if (!oneTokens.get(0).equals(twoTokens.get(0)) && 
	    !oneTokens.get(oneTokens.size()-1)
	      .equals(twoTokens.get(twoTokens.size()-1))){
	    return null;
	}
	if (one.equals(two)){
	    return null;
	}
	//System.out.println("* "+one+" --- "+two);
	//System.out.print("*");
	//System.out.println("is some overlap");
	//System.out.println("* "+one+" --- "+two);
	List<LexEntry> result = new LinkedList<LexEntry>();

	List<List<Cat>> oneSplits = one.getCat().allSplits();
	List<List<Cat>> twoSplits = two.getCat().allSplits();
	for (List<Cat> oneSplit : oneSplits){
	    //System.out.println("split: "+oneSplit);
	    for (List<Cat> twoSplit : twoSplits){
		if (oneSplit.get(0).matches(twoSplit.get(0))){
		    // make LexEntries for splitting off the common prefix sequences
		    //System.out.println("Got a match for :: "+oneSplit+" "+twoSplit);
		    int i=0;
		    while (i<oneTokens.size()-1 && i<twoTokens.size()-1 &&
			   oneTokens.get(i).equals(twoTokens.get(i))){
			// shared entry
			result.add(new LexEntry(oneTokens.subList(0,i+1),
						oneSplit.get(0)));
			// different entries
			result.add(new LexEntry(oneTokens.subList(i+1,oneTokens.size()),
						oneSplit.get(1)));
			//result.add(new LexEntry(twoTokens.subList(i+1,twoTokens.size()),
			//			twoSplit.get(1)));
			i++;
		    }
		}
		if (oneSplit.get(1).matches(twoSplit.get(1))){
		    // make LexEntries for splitting off the common suffix sequences
		    int i=1;
		    while (i<oneTokens.size() && i<twoTokens.size() &&
			   oneTokens.get(oneTokens.size()-i)
			   .equals(twoTokens.get(twoTokens.size()-i))){
			// shared entry
			result.add(new LexEntry(oneTokens.subList(oneTokens.size()-i,
								  oneTokens.size()),
						oneSplit.get(1)));
			// different entries
			result.add(new LexEntry(oneTokens.subList(0,oneTokens.size()-i),
						oneSplit.get(0)));
			//result.add(new LexEntry(twoTokens.subList(0,twoTokens.size()-i),
			//			twoSplit.get(0)));
			i++;
		    }		    
		}
	    }
	}

	
	if (twoTokens.size()<oneTokens.size()){
	    // pull out full entry on the left
	    if (oneTokens.subList(0,twoTokens.size()).equals(twoTokens)){
		for (List<Cat> oneSplit : oneSplits){
		    if (oneSplit.get(0).matches(two.getCat())){
			result.add(new LexEntry(oneTokens.subList(twoTokens.size(),
								  oneTokens.size()),
						oneSplit.get(1)));
		    }
		}
	    }
	    // pull out full entry on the right
	    if (oneTokens.subList(oneTokens.size()-twoTokens.size(),oneTokens.size())
		.equals(twoTokens)){
		//System.out.println("HERE");
		for (List<Cat> oneSplit : oneSplits){
		    if (oneSplit.get(1).matches(two.getCat())){
			result.add(new LexEntry(oneTokens.subList(0,oneTokens.size()-
								  twoTokens.size()),
						oneSplit.get(0)));
		    }
		}
	    }
	}
	
	one.expanded=true;
	return result;
    }

    public List<List<LexEntry>> allSplits(){
	List<List<LexEntry>> splits = new LinkedList<List<LexEntry>>();
	for (List<Cat> catsplit : myCat.allSplits()){
			System.out.println("split is "+catsplit);
	    Cat left = catsplit.get(0);
	    Cat right = catsplit.get(1);
	
	    for (int sp = 0 ; sp<myTokens.size(); sp++){
		// make parse cells for the new lexical entries
		List<LexEntry> split = new ArrayList<LexEntry>(2);
		split.add(new LexEntry(myTokens.subList(0,sp+1),left));
		split.add(new LexEntry(myTokens.subList(sp+1,myTokens.size()),right));
		splits.add(split);
	    }
	}
	return splits;
    }


    public static void main(String[] args){
	PType.addTypesFromFile("../experiments/geo880/geo880.types");
	Lang.loadLangFromFile("../experiments/geo880/geo880.lang");

	LexEntry one = new LexEntry("what states border texas","S : (lambda $0 e (and (state:t $0) (next_to:t $0 texas:s)))");
	//LexEntry two = new LexEntry("what rivers cross texas","S : (lambda $0 e (and (river:t $0) (loc:t $0 texas:s)))");
	LexEntry two = new LexEntry("what","S|NP : (lambda x e (state:t x))");

	System.out.println(one);
	System.out.println(two);
	System.out.println("==================");

	for (LexEntry l : splitEntries(one,two)){
	    System.out.println(l);
	}

    }

}

