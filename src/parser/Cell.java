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
import learn.*;

public class Cell implements Comparable<Cell> {

	//< Constructors
	public Cell(Cell c){
		myCat = c.myCat;
		begin = c.begin;
		end = c.end;
		childLists = new LinkedList(c.childLists);
		ruleName = c.ruleName;
		lex = c.lex;
		lexIsMax = c.lexIsMax;
		inside = c.inside;
		maxscore = c.maxscore;
		//cells = new LinkedList(c.cells);
		numParses=c.numParses;
		typeRaisedFrom = c.typeRaisedFrom;
	}


	public Cell(Cat c, int b, int e, List cList){
		myCat = c;
		begin = b;
		end = e;
		childLists = new LinkedList();
		if (cList!=null)
			childLists.add(cList);
		ruleName = null;
		lex = null;
		lexIsMax = false;
		inside = 0;
		maxscore = -Double.MAX_VALUE;
		//cells = new LinkedList();
		//cells.add(this);
		addToInside(cList);
		numParses=0;
		typeRaisedFrom = null;
	}

	public Cell(Cat c, int b, int e, List cList, LexEntry l){
		myCat = c;
		begin = b;
		end = e;
		childLists = new LinkedList();
		if (cList!=null)
			childLists.add(cList);
		ruleName = null;
		lex = l;
		// compute the inside prob for this lexical rewritee
		//System.out.println("LEX: "+lex);

		double score = 0;
		for (LexicalFeatureSet lfs : Globals.lexicalFeatures){
			score+=lfs.score(l,Globals.theta);
		}
		score+=computeParseFeaturesScore(null);
		inside = Math.exp(score);
		maxscore = score;	
		lexIsMax = true;
		numParses=1;

		typeRaisedFrom = null;
		//cells = new LinkedList();
		//cells.add(this);
	}

	public Cell(Cat c, int b, int e, List cList, String rule){
		myCat = c;
		begin = b;
		end = e;
		childLists = new LinkedList();
		if (cList!=null)
			childLists.add(cList);
		ruleName = rule;
		lex = null;
		lexIsMax = false;
		typeRaisedFrom = null;
		inside = 0;
		maxscore = -Double.MAX_VALUE;
		numParses=0;
		//cells = new LinkedList();
		//cells.add(this);
		addToInside(cList);
	}

	public Cell(Cat c, int b, int e, List cList, String rule, boolean rev){
		myCat = c;
		begin = b;
		end = e;
		childLists = new LinkedList();
		if (cList!=null)
			childLists.add(cList);
		ruleName = rule;
		lex = null;
		typeRaisedFrom = null;
		lexIsMax = false;
		inside = 0;
		maxscore = -Double.MAX_VALUE;
		numParses=0;
		revComp=rev;
		//cells = new LinkedList();
		//cells.add(this);
		addToInside(cList);

		/*
	if (b==1 && e==2 && c.equalsNoSem(Cat.makeCat("NP"))){
	    System.out.println("-------");
	    System.out.println(this);
	    System.out.println(computeParseFeaturesScore(cList));
	    System.out.println("-------");
	}
		 */
	}

	public Cell(Cat c, int b, int e, List cList, String rule, Cell typeR){
		myCat = c;
		begin = b;
		end = e;
		childLists = new LinkedList();
		if (cList!=null)
			childLists.add(cList);
		ruleName = rule;
		lex = null;
		typeRaisedFrom = typeR;
		inside = 0;
		maxscore = -Double.MAX_VALUE;
		numParses=0;
		typeRaise=true; 
		//cells = new LinkedList();
		//cells.add(this);
		addToInside(cList);
	}

	public Cell(Cat c, int b, int e, List cList, String rule, Cell typeR,boolean badTR){
		myCat = c;
		begin = b;
		end = e;
		childLists = new LinkedList();
		if (cList!=null)
			childLists.add(cList);
		ruleName = rule;
		lex = null;
		typeRaisedFrom = typeR;
		inside = 0;
		maxscore = -Double.MAX_VALUE;
		numParses=0;
		typeRaise=true; 
		badTypeRaise=badTR;
		//cells = new LinkedList();
		//cells.add(this);
		addToInside(cList);
	}

	//>

	//< Code for computing inside probabilisties.  
	//  Called by constructors and addCell
	public void addToInside(List c){ addToInside(c,null); }
	public void addToInside(List c, Cell ce){
		double newmax;
		int newNumParses;
		Cell c0,c1;
		double score = computeParseFeaturesScore(c);
		if (c.size()==1){  // unary parse rule
			c0 = (Cell)c.get(0);
			inside+=c0.inside()*Math.exp(score); 
			newmax = c0.maxscore()+score; 
			newNumParses = c0.numParses;
		} else { // binary parse rule
			c0 = (Cell)c.get(0);
			c1 = (Cell)c.get(1);
			inside+=c0.inside()*c1.inside()*Math.exp(score); 
			newmax = c0.maxscore()+c1.maxscore()+score; 
			newNumParses=c0.numParses*c1.numParses;
		}
		if (newmax==maxscore){
			if (maxchildren==null)
				maxchildren = new LinkedList();
			maxchildren.add(c);
			numParses+=newNumParses;
			if (ce!=null && ce.maxpredpercent>maxpredpercent)
				maxpredpercent=ce.maxpredpercent;
		}
		if (newmax>maxscore){
			maxscore = newmax;
			lexIsMax = false;
			maxchildren = new LinkedList();
			maxchildren.add(c);
			numParses = newNumParses;
			if (ce!=null && ce.maxpredpercent>maxpredpercent)
				maxpredpercent=ce.maxpredpercent;
		} 
	}
	//>

	//< simple accessors
	public int getStart(){
		return begin;
	}

	public int getEnd(){
		return end;
	}

	public Cat getCat(){
		return myCat;
	}

	public List getChildLists(){
		return childLists;
	}


	public double inside(){
		return inside;
	}

	public double outside(){
		return outside;
	}


	public double maxscore(){
		return maxscore;
	}

	public double diffscore(){
		return diffscore;
	}

	public void setDiffScore(double d){
		diffscore=d;
	}

	public double prunescore(){
		return maxscore;
	}


	//>

	//< toString code.  The Latex version is a mess

	public String toString(){
		StringBuffer result = new StringBuffer();
		result.append("[");//.append(isMax).append(" ").append(lex).append(" ");
		result.append(begin).append("-").append(end).append(": ").
		append(myCat).append(" : ").append(childLists.size()).
		append(" : ").append(maxscore).append(": ").
		append(maxchildren).
		append("]");

		return result.toString();
	}


	//< updates Cell to indicate that it has more than one way
	//  of being created during parsing 
	// assumes that other equals this
	// assumes that the new child lists aren't already present (which is 
	//      guaranteed with CKY parsing)
	public void addCell(Cell other){
		//cells.addAll(other.cells);
		childLists.addAll(other.childLists);
		if (other.fromRightComp) fromRightComp = true;
		if (other.fromLeftComp) fromLeftComp = true;
		/*
	if (other.begin==1 && other.end==2 && other.myCat.equalsNoSem(Cat.makeCat("NP"))){
	    System.out.println("-------");
	    System.out.println(this);
	    List l = (List)other.childLists.get(0);
	    System.out.println(l);
	    System.out.println(cells);
	    System.out.println(childListsIndex(l));
	    System.out.println(computeParseFeaturesScore(l));
	    System.out.println("-------");
	}
		 */
		Iterator i = other.childLists.iterator();
		List l;
		while (i.hasNext()){
			l = (List)i.next();
			addToInside(l);	    
		}
	}
	//>

	public int compareTo(Cell c){
		double s = c.prunescore();
		double p = prunescore();
		if (s==p){
			if (c.equals(this)){
				return 0;
			}
			if (c.hashCode()<hashCode())
				return -1;
			else
				return 1;
		}
		if (p<s)
			return -1;
		else
			return 1;
	}

	//< equals code.  
	public boolean equals(Object o){
		if (o instanceof Cell){
			Cell c = (Cell)o;
			//System.out.println("CELL EQUALS");
			//System.out.println(this);
			//System.out.println(o);

			if (c.begin==begin && 
					c.end==end &&
					c.isConj==isConj &&
					c.isDisj==isDisj &&
					c.isComplete==isComplete &&
					c.revComp==revComp &&
					c.typeRaise==typeRaise &&
					c.badTypeRaise==badTypeRaise &&
					myCat.equals(c.myCat)){
				return true;
			}
		} 
		return false;
	}


	public int hashCode(){
		int code= begin+3*end+myCat.hashCode();
		if (isConj) code++;
		if (isDisj) code++;
		if (isComplete) code++;
		return code;
	}
	//>

	//< printChildren() -- prints the lists of children Cells
	public void printChildren(){
		System.out.println(childLists);
	}
	//>

	//< expands out the tree of descendant cells using
	//  the first set of children in each cell
	public String makeFirstTree(){
		StringBuffer tree = new StringBuffer();
		tree.append(" [").append(myCat.toString());
		if (childLists.size()>0){
			Iterator i = ((List)childLists.get(0)).iterator();
			while (i.hasNext()){
				tree.append(((Cell)i.next()).makeFirstTree());
			}
		}
		tree.append("] ");
		return tree.toString();
	}
	//>

	public void getMaxLexEntries(List result, boolean addAll){
		//if (isMax){
		//    System.out.println("Cell: "+this);
		//}

		if (isMax){
			if (lexIsMax && !result.contains(lex)){
				//System.out.println("MAX LEX CELL: "+this);
				result.add(lex);
				return;
			}
			if (addAll)
				result.add(makeLexEntry());
		}
	}

	// this only goes one level -- it is not recursive
	// the chart code cycles through all of the cells in
	// a top down manner
	public void propMaxUnary(){
		Iterator i,j;
		List l;
		Cell c;
		if (!getIsMax() || lexIsMax) return;
		if (maxchildren!=null) {
			//System.out.println("max children size: "+maxchildren.size());
			i = maxchildren.iterator();
			while (i.hasNext()){
				l = (List)i.next();
				if (l.size()==1){
					c = (Cell)l.get(0);
					c.setIsMax(true);
					c.propMaxUnary();
				}
			}
		}   
	}

	// this only goes one level -- it is not recursive
	// the chart code cycles through all of the cells in
	// a top down manner
	public void propMaxBinary(){
		Iterator i,j;
		List l;
		Cell c;
		if (!getIsMax() || lexIsMax) return;
		if (maxchildren!=null) {
			//System.out.println("max children size: "+maxchildren.size());
			i = maxchildren.iterator();
			while (i.hasNext()){
				l = (List)i.next();
				if (l.size()==2){
					c = (Cell)l.get(0);
					c.setIsMax(true);
					c = (Cell)l.get(1);
					c.setIsMax(true);
				}
			}
		}   
	}

	//>

	public void computeMaxAvg(HashVector result){
		if (!getIsMax()) return;
		List l;
		Iterator i;
		Cell c;
		if (lex!=null && lexIsMax){
			for (LexicalFeatureSet lfs : Globals.lexicalFeatures){
				lfs.setFeats(lex,result);
			}
			computeParseFeatureVals(null,result);
		}
		if (maxchildren!=null && maxchildren.size()>0) {
			i = maxchildren.iterator();
			while (i.hasNext()){
				l = (List)i.next();		
				// update the parsing feature values
				computeParseFeatureVals(l,result);
			}
		}  
	}

	//< code for computing outside probabilities
	public void computeOutside(Cat c){
		if (getIsFull() && myCat.equals(c))
			outside=1;
		else 
			outside=0;
	}

	//< code for computing outside probabilities
	public void computeOutside(){
		if (getIsFull())
			outside=1;
		else 
			outside=0;
	}

	// assumes that the inside probabilities have already been computed
	public void computeOutsideUnary(){
		if (childLists.size()>0){
			// iterate through the ways of building this cell
			Iterator i = childLists.iterator();
			while (i.hasNext()){
				List c = (List)i.next();
				if (c.size()==1){  // unary parse rule
					double score = Math.exp(computeParseFeaturesScore(c));
					Cell c0 = (Cell)c.get(0);
					c0.outside += outside*score;
				} 
			}
		}
	}

	public void computeOutsideBinary(){
		if (childLists.size()>0){
			// iterate through the ways of building this cell
			Iterator i = childLists.iterator();
			while (i.hasNext()){
				List c = (List)i.next();
				if (c.size()==2){  // binary parse rule
					double score = Math.exp(computeParseFeaturesScore(c));
					Cell c0 = (Cell)c.get(0);
					Cell c1 = (Cell)c.get(1);
					c0.outside += outside*c1.inside*score;
					c1.outside += outside*c0.inside*score;
				}
			}
		}
	}

	private double computeParseFeaturesScore(List c){
		if (Globals.parseFeatures==null ||
				Globals.parseFeatures.size()==0)
			return 0;
		double score = 0;
		Iterator j = Globals.parseFeatures.iterator();
		while (j.hasNext()){
			ParseFeatureSet pf = (ParseFeatureSet)j.next();
			score+=pf.score(this,c,Globals.theta);
		}
		return featScale*score;
	}

	private void computeParseFeatureVals(List c, HashVector result){
		if (Globals.parseFeatures==null ||
				Globals.parseFeatures.size()==0)
			return;
		Iterator j = Globals.parseFeatures.iterator();
		while (j.hasNext()){
			ParseFeatureSet pf = (ParseFeatureSet)j.next();
			pf.setFeats(this,c,result);
		}
	}

	public void resetOutside(){
		outside = 0.0;
	}
	//>

	//< Helper code for computing the gradient of the LLM
	// assumes that the inside and outside probabilities 
	// have already been computed
	public void updateGradient(HashVector gradient){
		/*
	// first update the lexical features
	if (lex!=null){
	    int phi = Globals.lexPhi.indexOf(lex);
	    if (phi==-1){
		System.err.println("ERROR: No Lex Feature for: "+lex);
	    }
            double theta = Globals.theta.get(phi);
	    double score = Math.exp(featScale*theta);
            //System.out.println("outside: "+outside);
            //System.out.println("score: "+score);
            gradient.set(phi,(float)(gradient.get(phi)+score*outside));  
	}

	// now update the parse features
	if (childLists.size()>0){
	    // iterate through the ways of building this cell
	    Iterator i = childLists.iterator();
	    while (i.hasNext()){
		List c = (List)i.next();
		Iterator j = Globals.parseFeatures.iterator();
		while (j.hasNext()){
		    ParseFeatureSet pf = (ParseFeatureSet)j.next();
		    int index = pf.index(this,c);
		    if (index!=-1){	
			//System.out.println("HERE: "+index);
			double theta = Globals.theta.get(index);
			double value = pf.value(this,c);
			double score = Math.exp(theta*value);
			double pinside = 1;
			if (c.size()==1){  // unary parse rule
			    Cell c0 = (Cell)c.get(0);
			    pinside=c0.inside;
			} else { // binary parse rule
			    Cell c0 = (Cell)c.get(0);
			    Cell c1 = (Cell)c.get(1);
			    pinside=c0.inside*c1.inside;
			}
			gradient.set(index,(float)(gradient.get(index)+score*pinside*outside)); 
		    }
		}
	    }
	}
		 */
	}
	//>

	public double getLexProb(){
		if (lex==null) return 0.0;
		double score = 0.0;
		for (LexicalFeatureSet lfs : Globals.lexicalFeatures){
			score+=lfs.score(lex,Globals.theta);
		}
		return Math.exp(score)*outside;
	}

	public double getExpProb(){
		double lexscore = 0.0, score=0.0,pinside;
		for (LexicalFeatureSet lfs : Globals.lexicalFeatures){
			lexscore+=lfs.score(lex,Globals.theta);
		}
		lexscore+=computeParseFeaturesScore(null);
		score+=Math.exp(lexscore)*outside;

		// now update the parse features
		if (childLists.size()>0){
			// iterate through the ways of building this cell
			Iterator i = childLists.iterator();
			while (i.hasNext()){
				List c = (List)i.next();
				if (c.size()==1){  // unary parse rule
					Cell c0 = (Cell)c.get(0);
					pinside=c0.inside;
				} else { // binary parse rule
					Cell c0 = (Cell)c.get(0);
					Cell c1 = (Cell)c.get(1);
					pinside=c0.inside*c1.inside;
				}
				score+=outside*pinside*computeParseFeaturesScore(c);
			}
		}
		return score;
	}

	public LexEntry makeLexEntry(){
		if (lex!=null) return lex;
		return new LexEntry(Globals.tokens.subList(begin,end+1),myCat.copy());
	}

	public void updateExpFeats(HashVector expFeats){
		// first update the lexical features
		HashVector feats = new HashVector();
		if (lex!=null){
			double score = 0;
			for (LexicalFeatureSet lfs : Globals.lexicalFeatures){
				lfs.setFeats(lex,feats);
				score+=lfs.score(lex,Globals.theta);
				//if (score>100){
				//    System.out.println("BIG SCORE: "+score);
				//}
			}
			computeParseFeatureVals(null,feats);
			score+=computeParseFeaturesScore(null);
			feats.addTimesInto(Math.exp(score)*outside,expFeats);
		}

		// now update the parse features
		if (childLists.size()>0){
			// iterate through the ways of building this cell
			Iterator i = childLists.iterator();
			while (i.hasNext()){
				feats.clear();
				List c = (List)i.next();
				computeParseFeatureVals(c,feats);
				double pinside=1.0;
				if (c.size()==1){  // unary parse rule
					Cell c0 = (Cell)c.get(0);
					pinside=c0.inside;
				} else { // binary parse rule
					Cell c0 = (Cell)c.get(0);
					Cell c1 = (Cell)c.get(1);
					pinside=c0.inside*c1.inside;
				}
				double prob = outside*pinside*Math.exp(computeParseFeaturesScore(c));
				feats.addTimesInto(prob,expFeats);
			}
		}
	}

	public boolean isTypeRaised(){
		return typeRaisedFrom!=null;
	}

	public void setTypeRasiedFrom(Cell c){
		typeRaisedFrom = c;
	}

	public Cell getTypeRasiedFrom(){
		return typeRaisedFrom;
	}

	public boolean hasLexEntry(){
		return lex!=null;
	}

	public LexEntry getLexEntry(){
		return lex;
	}

	public boolean getIsMax(){
		return isMax;
	}

	public void setIsMax(boolean val){
		isMax = val;
	}

	public boolean getIsFull(){
		return isFull;
	}

	public void setIsFull(boolean b){
		isFull = b;
	}

	public int getNumParses(){
		return numParses;
	}

	double maxpredpercent=0;
	public void setPredPercent(double d){
		maxpredpercent=d;
	}

	//public Cell getSubCell(int i){
		//	return (Cell)cells.get(i);
	//}

	public int childListsIndex(List l){
		return childLists.indexOf(l);
	}

	public boolean isCoor(){
		return isConj || isDisj;
	}

	public List<String> getWordSpan(){
		return Globals.tokens.subList(begin,end+1);
	}

	// hack for RevGenCompFeatSet
	public boolean revComp=false;
	public boolean typeRaise=false;
	public boolean badTypeRaise=false;

	// the inside, outside, and max scores
	double inside=0;
	double outside=0;
	double maxscore=0;
	double diffscore=0;
	boolean lexIsMax=false;
	List maxchildren;
	boolean isMax;
	boolean isFull=false;
	int numParses;

	List cells;

	// temparary variables used during parsing
	public boolean isConj=false;
	public boolean isDisj=false;
	public boolean isComplete=true;

	// variables for implementing normal form parsing                      
	public static boolean useEisnerNormalForm = true;
	public boolean fromLeftComp=false;
	public boolean fromRightComp=false;

	public static double featScale = 1.0;

	Cell typeRaisedFrom;

	Cat myCat;
	Cat S = Cat.makeCat("S");

	// the lists of Cells and rule that created
	// this cell
	List childLists;

	// the LexEntry used to make this cell 
	LexEntry lex=null;

	// the span of the input string that this covers
	int begin;
	int end;

	String ruleName;

}
