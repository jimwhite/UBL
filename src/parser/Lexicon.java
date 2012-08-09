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

/**
 */
public class Lexicon {

	/**
	 * constructor with an existing lexicon
	 **/
	public Lexicon(Collection l){
		lexicon = new LinkedList();
		addLexEntries(l);
	}
	//>
	//< LexRule(filename)
	/**
	 * clear the lexicon and addEntriesFromFile(filename)
	 **/
	public Lexicon(String filename){
		lexicon = new LinkedList();
		addEntriesFromFile(filename);
	}
	//>

	//< addEntriesFromFile(filename) -- read a pre-defined lexicon
	/**
	 * Read entries, one per line, of the form<pre>
	 *  Tokens  :-  Cat
	 * </pre> 
	 * where those are passed to LexEntry(Tokens, Cat)
	 **/
	public void addEntriesFromFile(String filename){
		addEntriesFromFile(filename,false);
	}

	public void addEntriesFromFile(String filename, boolean flag){
		String line;

		try{
			BufferedReader in = new BufferedReader(new FileReader(filename));
			line = in.readLine();
			while (line!=null){  // for each line in the file
				line.trim();
				if (line.equals("") || line.startsWith("//")) {
					// do nothing: ignore blank lines and comments.
				} else {
					// split the tokens from the category
					int split = line.indexOf(":-");
					if (split > 0) {
						// make a new LexEntry by splitting the string
						LexEntry le = new LexEntry(line.substring(0,split),
								line.substring(split+2,
										line.length()));
						le.setDomainSpecific(flag);
						le.loaded=true;
						addLexEntry(le);
					} else {
						//< fail: unrecognized
						throw new IllegalStateException("unrecognized format for lexicon line: ["+line+"] in "+ filename);
						//>
					}
				}
				line = in.readLine();
			}

		} catch(IOException e){ System.err.println(e); }
	}


	public Lexicon(){
		lexicon = new LinkedList();
	}
	/** 
	 * Adds all of the cells to the chart that can 
	 * be created by lexical insertion.
	 *
	 * The work to find valid lexical entries for each split is done
	 * by getLexEntries.
	 * 
	 * @modifies chart
	 **/



	public void addCells(Chart chart){

		List entries = new LinkedList();
		List words = chart.getTokens();
		int numWords = words.size();

		// iterate through all spans of words
		for (int i=0; i<numWords; i++){
			for (int j=i; j<numWords; j++){
				entries.clear();
				getLexEntries(words.subList(i,j+1),entries);
				//for each item in the lexicon
				Iterator k = entries.iterator();
				while (k.hasNext()){
					// add new cells for each match
					LexEntry l = (LexEntry)k.next();
					Cat cat = l.getCat().copy();
					if (cat.getSem()!=null)
						cat.setSem(cat.getSem().copyAna());
					Cell c = new Cell(cat,i,j,null,l);
					//System.out.println("ADDED Lex: "+c);
					//c.setLexEntry(l);
					chart.add(c);
				}	       
			}
		}
	}

	//>

	//< add entries to the lexicon
	/**
	 * addLexEntry creates  anew LexEntry and adds it to the lexicon
	 **/
	public void addLexEntry(List t, Cat c){
		addLexEntry(new LexEntry(t,c));
	}

	public void addLexEntry(LexEntry l){
		if (lexicon.contains(l))
			return;
		//System.out.println("Added: "+l);
		lexicon.add(l);
	}
	/**
	 * addLexEntries adds all the entries in List<LexEntry> l to the lexicon.
	 **/
	public void addLexEntries(Collection l){
		Iterator i = l.iterator();
		while (i.hasNext()){
			LexEntry e = (LexEntry)i.next();
			addLexEntry(e);
		}
	}
	//>
	//< hasEntryFor(List<String> words)
	/**
	 * @return true if the lexicon has a LexEntry whose List<String>
	 * words are those passed in.
	 **/
	public boolean hasEntryFor(List words){
		Iterator i = lexicon.iterator();
		while (i.hasNext()){
			LexEntry l = (LexEntry)i.next();
			if (l.hasWords(words))
				return true;
		}
		return false;
	}
	//>
	//< trivial stuff: reset, size, contains, hasEntryFor(words), get(i)
	/**
	 * clear the lexicon.
	 *
	 * <P>should be called clear
	 **/
	public void reset(){
		lexicon.clear();
	}
	/**
	 * @return the size of the lexicon
	 **/
	public int size(){
		return lexicon.size();
	}
	/**
	 * @return true if the lexicon contains e
	 **/
	public boolean contains(LexEntry e){
		return lexicon.contains(e);
	}
	/**
	 * @return the ith lexicon element.  
	 *
	 * REP EXPOSURE: Why is the lexicon exposed this way?  Why isn't
	 * the lexicon a Set, for that matter?  Why are we ordered?  Is it
	 * that we want an iterator?
	 **/
	public LexEntry get(int i){
		return (LexEntry)lexicon.get(i);
	}
	/**
	 * @return a lexical entry per line. 
	 **/


	public void printLexiconWithWeights(){
		Iterator j = lexicon.iterator();
		System.out.println("[LexEntries and scores:");
		while (j.hasNext()){
			LexEntry le = (LexEntry) j.next();
			int index = Globals.lexPhi.indexOf(le);
			System.out.print(le);
			if (index!=-1) System.out.print(" # "+Globals.theta.get("LEX:"+index));

			System.out.println();
		}
		System.out.println("]");
	}




	public String toString(){
		StringBuffer result = new StringBuffer();
		Iterator i = lexicon.iterator();
		while (i.hasNext()){
			result.append(i.next().toString()).append("\n");
		}
		return result.toString();
	}
	/**
	 * REP EXPOSURE: Why are we exposing the lexicon this way? 
	 **/
	public List<LexEntry> getLexicon(){
		return lexicon;
	}
	//> 

	//< getLexEntries(List<String> words, List<String> entries MODIFIED)
	/**
	 * adds and lexentries with words words to the return list of
	 * entries
	 * 
	 * @modifies entries
	 */    
	public void getLexEntries(List words, List entries){
		Iterator i = lexicon.iterator();
		while (i.hasNext()){
			LexEntry e = (LexEntry)i.next();
			if (e.hasWords(words))
				entries.add(e);
		}
	}

	public List<LexEntry> getLexEntries(List<String> words){
		List<LexEntry> entries = new LinkedList();
		int numWords = words.size();

		// iterate through all spans of words
		for (int i=0; i<numWords; i++){
			for (int j=i; j<numWords; j++){
				getLexEntries(words.subList(i,j+1),entries);
			}
		}
		return entries;
	}

	public double initialWeight(LexEntry l){
		int numWordMatches=0;
		int numEntryMatches=0;
		List<String> tokens = l.getTokens();
		for (LexEntry e : lexicon){
			boolean leftMatch = e.startsWith(tokens);
			boolean rightMatch =  e.endsWith(tokens);
			if (leftMatch || rightMatch){
				if (leftMatch) numWordMatches++;
				if (rightMatch) numWordMatches++;
				Cat c = l.getCat();		

				for (List<Cat> split : e.getCat().allSplits()){
					if (leftMatch && split.get(0).equals(c)){
						numEntryMatches++;
					}
					if (rightMatch && split.get(1).equals(c)){
						numEntryMatches++;
					}
				}
			}
		}
		if (numWordMatches==0 || numEntryMatches<2) return 0;
		return (double)(numEntryMatches-1) / (double)lexicon.size();
	}

	//>
	List<LexEntry> lexicon;

	// testing code...
	public static void main(String[] args){
		Lexicon r = new Lexicon(args[0]);
		System.out.println(r);

	}

}

