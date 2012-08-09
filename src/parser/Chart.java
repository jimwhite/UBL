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
import lambda.*;

public class Chart {

	public Chart(List<String> t){
		tokens = t;
		init(t.size());
	}

	public Chart(int len){
		init(len);
	}

	/**
	 * create the indexing structures used by the chart.
	 */
	private void init(int len){
		numEntries = 0;
		size = len;
		chart = new Map[len][len];
		minScores = new double[len][len];
		for (int i=0; i<len; i++){
			for (int j=i; j<len; j++){
				chart[i][j]=new LinkedHashMap<Cell,Cell>();
			}
		}
	}

	/**
	 * add cell c to chart 
	 */
	public void add(Cell c){     

		// first of all, check if the semantics of c is allowed
		if (prune(c)){
			return;
		}

		Map<Cell,Cell> l = chart[c.getStart()][c.getEnd()];
		Cell c1 = l.get(c);
		if (c1==null){
			numEntries++;
			addNew(c.getStart(),c.getEnd(),c);
			if (c.getStart()==0 && c.getEnd()==size-1){
				if (c.getCat().equalsNoSem(S) &&	
						c.getCat().getSem().inferType()!=null){	
					c.setIsFull(true);
				}
			}       
		} else {
			c1.addCell(c);
		}
	}

	/**
	 * Add entry to chart and do pruning on the cell that
	 * it's added to if there are pruneN entries already
	 *  there 
	 * @param begin
	 * @param end
	 * @param cin
	 */
	private void addNew(int begin, int end, Cell cin){
		// add it only if it is one of the top N options
		Map<Cell,Cell> l = chart[begin][end];
		int size = l.size();
		if (size<Parser.pruneN){
			if (size==0 || cin.prunescore()<minScores[begin][end]){
				minScores[begin][end]=cin.prunescore();
			}
			l.put(cin,cin);
			return;
		}
		double minScore = minScores[begin][end];
		if (size==Parser.pruneN && cin.prunescore()<=minScore){
			return;
		}

		l.put(cin,cin);
		Iterator<Cell> i = getCellsIterator(begin,end);
		Cell mc = i.next();
		double min = mc.prunescore();
		double prevmin=min;
		while (i.hasNext()){
			Cell c = i.next();
			// never prune out lexical entries
			if (c.hasLexEntry()) continue;
			double s = c.prunescore();
			if (s<min){
				prevmin=min;
				min = s;
				mc = c;
			}
		}
		l.remove(mc);
		minScores[begin][end]=prevmin;
	}

	public Cell getCell(Cell c){
		return (Cell)chart[c.getStart()][c.getEnd()].get(c);
	}

	public void addAll(List c){
		Iterator i = c.iterator();
		while (i.hasNext()){
			add((Cell)i.next());
		}
	}

	public Iterator<Cell> getCellsIterator(int i, int j){
		return chart[i][j].values().iterator();
	}

	public int numCells(int i, int j){
		return chart[i][j].size();
	}

	public List<String> getTokens(){
		return tokens;
	}

	public int length(){
		return size;
	}

	public List<Cell> fullparses(){
		List<Cell> result = new LinkedList<Cell>();
		Iterator k = getCellsIterator(0,size-1);
		while (k.hasNext()){
			Cell c = (Cell)k.next();
			if (c.getCat().equalsNoSem(S))
				result.add(c);
		}
		return result;
	}


	public long numEntries(){
		return numEntries;
	}


	public String toString(){
		StringBuffer result = new StringBuffer();
		for (int i=0; i<size; i++){
			for (int j=i; j<size; j++){
				Iterator k = getCellsIterator(i,j);
				while (k.hasNext()){
					result.append(k.next()).append("\n");
				}
			}
		}
		return result.toString();
	}


	public void setPruningSem(Exp e){
		if (e==null){
			preds = null;
			predCounts = null;
		}
		preds = new LinkedList();
		predCounts = new LinkedList();
		e.allPreds(-1,preds);
		Iterator i = preds.iterator();
		while (i.hasNext()){
			Object p = i.next();
			predCounts.add(new Integer(e.predCount(p)));
		}
		theCount=e.expCount(Exp.THE);
		existsCount=e.expCount(Exp.EXISTS);
	}


	// this function prunes entries out of the lexicon
	// if they contain predicates or logical operators 
	// that could not be combined to give the target
	// semantics.
	public boolean prune(Cell c){
		Exp e = c.getCat().getSem();
		if (c.getStart()==0 && c.getEnd()==size-1 && !c.getCat().equalsNoSem(S)) return true;
		if (e==null) return false;
		// check that the semantics is ok                                                                                                                    
		if (preds==null) return false;
		if (e.expCount(Exp.THE)>theCount) return true;
		if (e.expCount(Exp.EXISTS)>existsCount) return true;
		Iterator i = preds.iterator();
		Iterator j = predCounts.iterator();
		int top=0,bottom=0;
		while (i.hasNext()){
			Object p = i.next();
			int count = ((Integer)j.next()).intValue();
			int ce = e.predCount(p);
			if (ce>count){
				//System.out.println("pruning: "+e+" because of: "+p);                                                                                       
				return true;
			}else{
				top+=ce;
				bottom+=count;
			}
			c.setPredPercent((double)top/(double)bottom);	
		}
		return false;
	}

	public void pruneParseResults(int n){
		List<Cell> fullparses = fullparses();
		if (fullparses.size()<=n) return;
		double[] scores = new double[fullparses.size()];
		Iterator<Cell> iter = fullparses.iterator();
		int k=0;
		while (iter.hasNext()){
			Cell c = iter.next();
			scores[k] = c.prunescore();
			k++;
		}

		Arrays.sort(scores);
		double thresh = scores[scores.length-n];

		//collect the cells that survive
		List newCells = new LinkedList();
		iter = fullparses.iterator();
		while (iter.hasNext()){
			Cell c = iter.next();
			if (c.prunescore()<=thresh){
				iter.remove();
			}
		}
	}

	//< checks if Exp e is associated with a full parse in this chart
	public boolean hasExp(Exp e){
		Iterator i = fullparses().iterator();
		Cell c;
		while (i.hasNext()){
			c = (Cell)i.next();
			Exp e2 = (Exp)c.getCat().getSem();
			if (e2.equals(e))
				return true;
		}
		return false;
	}

	//< returns the list of Cats (in result) that are the 
	//  highest scoring complete parses in this chart
	public void getMaxParses(List<Cat> result){
		Iterator i = fullparses().iterator();
		double maxscore = -Double.MAX_VALUE;
		Cell c;
		result.clear();
		while (i.hasNext()){
			c = (Cell)i.next();
			if (c.maxscore()>maxscore){
				maxscore = c.maxscore();
				result.clear();
				result.add(c.getCat());
			} else
				if (c.maxscore()==maxscore){
					if (!result.contains(c.getCat()))
						result.add(c.getCat());
				}
		}
	}



	public void computeMaxAve(Exp sem, HashVector result){
		//System.out.println("Begin MaxAvg");
		// first mark all of the cells that contribute to 
		// max parses that produce cin
		setMaxes(sem);

		// now collect all of the lexical entries that are 
		// in marked cells
		Iterator i;
		Cell c;
		int count=0;
		for (int len = size-1; len>=0; len--){
			for (int begin = 0; begin<size-len; begin++){
				// first do the type raised cells
				//System.out.println("----"+begin+"----"+(begin+len));
				i = getCellsIterator(begin,begin+len);
				while (i.hasNext()){
					c = (Cell)i.next();
					if (begin==0 && len==Globals.lastWordIndex && c.getIsMax()){
						count++;
					}
					c.computeMaxAvg(result);
				}
			}
		}
		result.divideBy(count);
	}

	public void computeMaxAve(List cats, HashVector result){
		for (int i=0; i<cats.size(); i++){
			Exp sem = ((Cat)cats.get(i)).getSem();
			computeMaxAve(sem,result);
			//if (i==0){	      
			//	S.setSem(sem);
			//	printMaxLatexParses(S);
			//}
		}
		result.divideBy(cats.size());
	}


	//< returns the list of Cells (in result) that are the 
	//  highest scoring complete parses in this chart
	public List<Cell> getMaxCells(int beg, int end){
		List<Cell> result = new LinkedList<Cell>();
		double maxscore = 0, score;
		Cell c;
		Iterator i = getCellsIterator(beg,end);
		Cell maxCell = null;
		while (i.hasNext()){
			c = (Cell)i.next();
			score = c.getLexProb();
			if (score == 0) score = c.getExpProb(); 
			if (score>0 && score>=maxscore){
				maxscore=score;
				result.add(c);
			}
		}
		return result;
	}


	//< code for pulling the lexical entries out of parses.
	public List<LexEntry> getTopExpProbLexEntries(int topN, Exp sem){

		/*
	List<LexEntry> result = new LinkedList<LexEntry>();
	S.setSem(sem);
	//computeOutsideProbs(S);
	setMaxes(S);
	for (int begin=0; begin<size; begin++){
	    for (int len=0; len<size-begin; len++){
		//for (Cell c : getMaxCells(begin,begin+len)){
		Cell c;
		Iterator i = getCellsIterator(begin,begin+len);
		while (i.hasNext()){
		    c = (Cell)i.next();
		    if (c.getIsMax()){
			LexEntry l = c.getLexEntry();
			if (l==null){
			    l = c.makeLexEntry();
			}
			result.add(l);
		    }
		}
	    }
	}
	return result;
		 */

		Comparator<Cell> comp_lex = new Comparator<Cell>() {
			public int compare(Cell i, Cell j) {
				if (j.getLexProb()>i.getLexProb())
					return 1;
				else 
					return -1;
			}
		};

		List<Cell> cells = new LinkedList<Cell>();

		S.setSem(sem);
		computeOutsideProbs(S);
		// iterate all of the cells in the chart
		Cell c;
		Iterator i;
		for (int begin=0; begin<size; begin++){
			for (int len=0; len<size-begin; len++){
				i = getCellsIterator(begin,begin+len);
				while (i.hasNext()){
					c = (Cell)i.next();
					if (c.getLexEntry()!=null){
						//System.out.println("adding: "+c);
						cells.add(c);
					}
				}
				Collections.sort(cells,comp_lex);
				//System.out.println("===============");
				//for (Cell c1 : cells)
				//    System.out.println(c1 + " --- "+c1.getLexProb());
				//System.out.println("===============");
				cells.subList(Math.min(cells.size(),topN),cells.size()).clear();
			}
		}

		List<LexEntry> result = new LinkedList<LexEntry>();
		for (Cell c1 : cells){
			result.add(c1.getLexEntry());
		}
		return result;

	}

	public List<LexEntry> splitAndMergeLex(Exp sem){
		List<LexEntry> result = new LinkedList<LexEntry>();
		//S.setSem(sem);
		//computeOutsideProbs(S);
		setMaxes(sem);
		double mostImproved = 0.0;
		for (int begin=0; begin<size; begin++){
			for (int len=0; len<size-begin; len++){
				Cell c;
				Iterator i = getCellsIterator(begin,begin+len);
				while (i.hasNext()){
					c = (Cell)i.next();
					if (c.getIsMax()){
						mostImproved = splitMerge(c,begin,begin+len,result,mostImproved);
					}
				}
			}
		}
		return result;
	}

	public List<LexEntry> splitAndMergeLexAll(Exp sem){
		List<LexEntry> result = new LinkedList<LexEntry>();
		setMaxes(sem);
		double mostImproved = 0.0;
		for (int begin=0; begin<size; begin++){
			for (int len=0; len<size-begin; len++){
				Cell c;
				Iterator i = getCellsIterator(begin,begin+len);
				while (i.hasNext()){
					c = (Cell)i.next();
					if (c.getIsMax()){
						LexEntry l = c.makeLexEntry();
						result.add(l);
						for (List<LexEntry> split : l.allSplits()){
							result.addAll(split);
						}
					}
				}
			}
		}
		return result;
	}


	private double splitMerge(Cell c, int begin, int end, List<LexEntry> maxEntries, double mostImproved){
		// try all possible splits and take the one with the great score increase
		//System.out.println("splitMerge: "+c+" b:"+begin+" e:"+end);
		//List<Cell> betterEntries = new LinkedList<Cell>();
		Cat rootCat = c.getCat();
		List<String> rootTokens = c.getWordSpan();
		//System.out.println("root tokens: "+rootTokens);
		double maxImprove = mostImproved;
		double origScore = c.maxscore();
		for (List<Cat> split : rootCat.allSplits()){
			Cat left = split.get(0);
			Cat right = split.get(1);
			//System.out.println("Trying to split as : "+left+" :: "+right);

			for (int sp = begin; sp<end; sp++){
				// make parse cells for the new lexical entries
				LexEntry leftLex = new LexEntry(rootTokens.subList(0,(sp-begin)+1),left);
				LexEntry rightLex = new LexEntry(rootTokens.subList((sp-begin)+1,rootTokens.size()),right);

				Cell leftCell = new Cell(left,begin,sp,null,leftLex);
				Cell rightCell = new Cell(right,sp+1,end,null,rightLex);

				// but, use an existing cell instead of the lexical one if it 
				// has a higher max score
				Cell temp = getCell(leftCell);
				if (temp!=null && temp.maxscore()>=leftCell.maxscore()){
					leftCell = temp;
				}
				temp = getCell(rightCell);
				if (temp!=null && temp.maxscore()>=rightCell.maxscore()){
					rightCell = temp;
				}

				// only consider this split if it adds a new lexentry
				if (!leftCell.lexIsMax && !rightCell.lexIsMax)
					continue;

				// now, make the new root cell 
				List<Cell> children = new LinkedList<Cell>();
				children.add(leftCell);
				children.add(rightCell);
				Cell r = new Cell(rootCat,begin,end,children);
				//System.out.println("SPLIT: "+leftLex+" --- "+rightLex+" = "+r.maxscore());
				// if the score is higher, save the lexical entries
				double improvement = r.maxscore()-origScore;
				if (improvement>=maxImprove){
					if (improvement>maxImprove){
						maxEntries.clear();
						maxImprove = improvement;
					}
					if (leftCell.lexIsMax)
						maxEntries.add(leftCell.getLexEntry());
					if (rightCell.lexIsMax)
						maxEntries.add(rightCell.getLexEntry());
				}
			}
		}

		// finally, consider merging by introducing a new lexical item
		LexEntry l = c.getLexEntry();
		if (l==null){
			l = c.makeLexEntry();
			// if the score is greater than the max inside
			// of the original cell, add it
			Cell ce = new Cell(l.getCat(),begin,end,null,l);
			double improvement = ce.maxscore()-origScore;
			if (improvement>=maxImprove){
				//betterEntries.add(ce);
				if (improvement>maxImprove)
					maxEntries.clear();
				maxEntries.add(l);
				//System.out.println("merged lex: "+l+" -from- "+c);
			}
		}
		//return betterEntries;
		return maxImprove;
	}


	/*
    public List<LexEntry> splitAndMergeLex(Exp sem){
	List<LexEntry> result = new LinkedList<LexEntry>();
	//S.setSem(sem);
	//computeOutsideProbs(S);
	setMaxes(sem);
	for (int begin=0; begin<size; begin++){
	    for (int len=0; len<size-begin; len++){
		Cell c;
		Iterator i = getCellsIterator(begin,begin+len);
		while (i.hasNext()){
		    c = (Cell)i.next();
		    if (c.getIsMax()){
			result.addAll(splitMerge(c,begin,begin+len));
		    }
		}
	    }
	}
	return result;
    }

    private List<LexEntry> splitMerge(Cell c, int begin, int end){
	// try all possible splits and take the one with the great score increase
	//System.out.println("splitMerge: "+c+" b:"+begin+" e:"+end);
	//List<Cell> betterEntries = new LinkedList<Cell>();
	List<LexEntry> maxEntries = new LinkedList<LexEntry>();
	Cat rootCat = c.getCat();
	List<String> rootTokens = c.getWordSpan();
	//System.out.println("root tokens: "+rootTokens);
	double maxScore = c.maxscore();
	double origScore = c.maxscore();
	for (List<Cat> split : rootCat.allSplits()){
	    Cat left = split.get(0);
	    Cat right = split.get(1);
	    //System.out.println("Trying to split as : "+left+" :: "+right);

	    for (int sp = begin; sp<end; sp++){
		// make parse cells for the new lexical entries
		LexEntry leftLex = new LexEntry(rootTokens.subList(0,(sp-begin)+1),left);
		LexEntry rightLex = new LexEntry(rootTokens.subList((sp-begin)+1,rootTokens.size()),right);
		Cell leftCell = new Cell(left,begin,sp,null,leftLex);
		Cell rightCell = new Cell(right,sp+1,end,null,rightLex);

		// but, use an existing cell instead of the lexical one if it 
		// has a higher max score
		Cell temp = getCell(leftCell);
		if (temp!=null && temp.maxscore()>leftCell.maxscore())
		    leftCell = temp;
		temp = getCell(rightCell);
		if (temp!=null && temp.maxscore()>rightCell.maxscore())
		    rightCell = temp;

		// now, make the new root cell 
		List<Cell> children = new LinkedList<Cell>();
		children.add(leftCell);
		children.add(rightCell);
		Cell r = new Cell(rootCat,begin,end,children);
		//System.out.println("SPLIT: "+leftLex+" --- "+rightLex+" = "+r.maxscore());

		// if the score is higher, save the lexical entries
		if (r.maxscore()>=maxScore){
		    if (r.maxscore()>maxScore){
			maxEntries.clear();
			maxScore = r.maxscore();
		    }
		    if (leftCell.getLexEntry()!=null && leftCell.lexIsMax)
			maxEntries.add(leftCell.getLexEntry());
		    if (rightCell.getLexEntry()!=null && rightCell.lexIsMax)
			maxEntries.add(rightCell.getLexEntry());
		}
	    }
	}

	// finally, consider merging by introducing a new lexical item
	LexEntry l = c.getLexEntry();
	if (l==null){
	    l = c.makeLexEntry();
	    // if the score is greater than the max inside
	    // of the original cell, add it
	    Cell ce = new Cell(l.getCat(),begin,end,null,l);
	    if (ce.maxscore()>=maxScore){
		//betterEntries.add(ce);
		if (ce.maxscore()>maxScore)		    
		    maxEntries.clear();
		maxEntries.add(l);
		//System.out.println("merged lex: "+l+" -from- "+c);
	    }
	}
	//return betterEntries;
	return maxEntries;
    }
	 */


	//< code for pulling the lexical entries out of parses.
	public List<LexEntry> getTopExpNewLexEntries(int topN, Exp sem,Lexicon lexicon){
		Comparator<Cell> comp_lex = new Comparator<Cell>() {
			public int compare(Cell i, Cell j) {
				if (j.getLexProb()>i.getLexProb())
					return 1;
				else 
					return -1;
			}
		};
		List<Cell> cells = new LinkedList<Cell>();
		S.setSem(sem);
		computeOutsideProbs(S);
		// iterate all of the cells in the chart
		Cell c;
		Iterator i;
		for (int begin=0; begin<size; begin++){
			for (int len=0; len<size-begin; len++){
				i = getCellsIterator(begin,begin+len);
				while (i.hasNext()){
					c = (Cell)i.next();
					if (c.getLexEntry()!=null
							&& !Globals.lexPhi.contains(c.getLexEntry())){
						//System.out.println("adding: "+c);
						cells.add(c);
					}
				}
				Collections.sort(cells,comp_lex);
				//System.out.println("===============");
				//for (Cell c1 : cells)
				//    System.out.println(c1 + " --- "+c1.getLexProb());
				//System.out.println("===============");
				cells.subList(Math.min(cells.size(),topN),cells.size()).clear();
			}
		}
		List<LexEntry> result = new LinkedList<LexEntry>();
		for (Cell c1 : cells){
			result.add(c1.getLexEntry());
		}
		return result;
	}

	public List<LexEntry> getMaxLexEntriesFor(Exp sem){
		return getMaxLexEntriesFor(sem,false);
	}

	// checks only in the full parses.  return the lexical entries
	// in the highest scoring parse(s).
	//public List getMaxLexEntriesFor(Cat cin){
	//	return getMaxLexEntriesFor(cin,false);
	//}

	//public List getMaxLexEntriesFor(Exp e, boolean all){
	//	S.setSem(e);
	//	return getMaxLexEntriesFor(S,all);
	//}

	public List<LexEntry> getMaxLexEntriesFor(Exp e, boolean addAll){
		//System.out.println("Begin MaxLex");
		List<LexEntry> result = new LinkedList<LexEntry>();
		Iterator i;

		// first mark all of the cells that contribute to 
		// max parses that produce cin
		setMaxes(e);

		// now collect all of the lexical entries that are 
		// in marked cells
		Cell c;
		for (int begin=0; begin<size; begin++){
			for (int len=0; len<size-begin; len++){
				i = getCellsIterator(begin,begin+len);
				while (i.hasNext()){
					c = (Cell)i.next();
					c.getMaxLexEntries(result,addAll);
				}
			}
		}
		//System.out.println("End MaxLex");
		return result;
	}

	public void setMaxes(Exp e){
		// first clear out all of the maxes
		resetMaxes();

		// now find the max parses for the input Cat
		List<Cell> cats = new LinkedList<Cell>();
		double highest = -Double.MAX_VALUE;
		List result = new LinkedList();
		Iterator<Cell> i = fullparses().iterator();
		Cell c;
		while (i.hasNext()){
			c = i.next();
			Cat c2 = c.getCat();
			if (c2.getSem().equals(e)){
				if (c.maxscore()>highest){
					highest = c.maxscore();
					cats.clear();
					cats.add(c);
				}
				if (c.maxscore()==highest){
					cats.add(c);
				}
			}
		}

		// now set them to be the maxes
		i = cats.iterator();
		while (i.hasNext()){
			c = i.next();
			c.setIsMax(true);
		}

		// now propogate the maxes through the chart
		for (int len = size-1; len>=0; len--){
			for (int begin = 0; begin<size-len; begin++){
				// first do the type raised cells
				i = getCellsIterator(begin,begin+len);
				while (i.hasNext()){
					c = i.next();
					c.propMaxUnary();
				}
				// now do the rest of the cells
				i = getCellsIterator(begin,begin+len);		
				while (i.hasNext()){
					c = i.next();
					c.propMaxBinary();
				}
			}
		}
	}

	private void resetMaxes(){
		Iterator i;
		Cell c;
		for (int len = size-1; len>=0; len--){
			for (int begin = 0; begin<size-len; begin++){
				// first do the type raised cells
				i = getCellsIterator(begin,begin+len);
				while (i.hasNext()){
					c = (Cell)i.next();
					c.setIsMax(false);
				}
			}
		}
	}

	//>

	//< code for computing probabilities and gradients
	//  assumes that we are parsing with a probabilistic model
	public void computeOutsideProbs(Cat inputCat){
		Cell c;
		Iterator i;

		// first set all of outside probs to zero
		for (int len = size-1; len>=0; len--){
			for (int begin = 0; begin<size-len; begin++){
				// first do the type raised cells
				i = getCellsIterator(begin,begin+len);
				while (i.hasNext()){
					c = (Cell)i.next();
					c.resetOutside();
					//System.out.println("outside: "+c+" : "+c.outside());
				}
			}
		}


		// first, initalize all of the root level cells
		//System.out.println("computing outside: [0,"+(size-1)+"]");
		i = getCellsIterator(0,size-1);
		while (i.hasNext()){
			c = (Cell)i.next();
			c.computeOutside(inputCat);
		}

		// now do the rest of the cells
		for (int len = size-1; len>=0; len--){
			for (int begin = 0; begin<size-len; begin++){
				// first do the type raised cells
				i = getCellsIterator(begin,begin+len);
				while (i.hasNext()){
					c = (Cell)i.next();
					c.computeOutsideUnary();
					//System.out.println("outside: "+c+" : "+c.outside());
				}
				// now do the rest of the cells
				i = getCellsIterator(begin,begin+len);		
				//System.out.println("computing outside: ["+begin+","+(begin+len)+"]");
				while (i.hasNext()){
					c = (Cell)i.next();
					c.computeOutsideBinary();
					//System.out.println("outside: "+c+" : "+c.outside());
				}
			}
		}
	}

	/**
	 *  code for computing probabilities and gradients
	 */
	public void computeOutsideProbs(){
		Cell c;
		Iterator i;

		// first set all of outside probs to zero
		for (int len = size-1; len>=0; len--){
			for (int begin = 0; begin<size-len; begin++){
				// first do the type raised cells
				i = getCellsIterator(begin,begin+len);
				while (i.hasNext()){
					c = (Cell)i.next();
					c.resetOutside();
					//System.out.println("outside: "+c+" : "+c.outside());
				}
			}
		}

		// first, initalize all of the root level cells
		//System.out.println("computing outside: [0,"+(size-1)+"]");
		i = getCellsIterator(0,size-1);
		while (i.hasNext()){
			c = (Cell)i.next();
			c.computeOutside();
		}

		// now do the rest of the cells
		for (int len = size-1; len>=0; len--){
			for (int begin = 0; begin<size-len; begin++){
				// first do the type raised cells
				i = getCellsIterator(begin,begin+len);
				while (i.hasNext()){
					c = (Cell)i.next();
					c.computeOutsideUnary();
					//System.out.println("outside: "+c+" : "+c.outside());
				}
				// now do the rest of the cells
				i = getCellsIterator(begin,begin+len);		
				//System.out.println("computing outside: ["+begin+","+(begin+len)+"]");
				while (i.hasNext()){
					c = (Cell)i.next();
					c.computeOutsideBinary();
					//System.out.println("outside: "+c+" : "+c.outside());
				}
			}
		}
	}

	/**
	 *  compute expectations of features for parameter updates
	 * @return
	 */
	public HashVector computeExpFeatVals(){
		HashVector exp = new HashVector();
		computeOutsideProbs();
		for (int len = size-1; len>=0; len--){
			for (int begin = 0; begin<size-len; begin++){
				Iterator i = getCellsIterator(begin,begin+len);		
				while (i.hasNext()){
					Cell c = (Cell)i.next();
					c.updateExpFeats(exp);
				}
			}
		}
		return exp;
	}

	/** compute expectations of features for parameter updates
	 *  under distribution conditioned on sentential semantics targetSem
	 */
	public HashVector computeExpFeatVals(Exp targetSem){
		HashVector exp = new HashVector();
		S.setSem(targetSem);
		computeOutsideProbs(S);
		for (int len = size-1; len>=0; len--){
			for (int begin = 0; begin<size-len; begin++){
				Iterator i = getCellsIterator(begin,begin+len);		
				while (i.hasNext()){
					Cell c = (Cell)i.next();
					c.updateExpFeats(exp);
				}
			}
		}
		return exp;
	}

	private void computeGradient(Cat current, HashVector g){
		computeOutsideProbs(current);
		for (int len = size-1; len>=0; len--){
			for (int begin = 0; begin<size-len; begin++){
				Iterator i = getCellsIterator(begin,begin+len);		
				while (i.hasNext()){
					Cell c = (Cell)i.next();
					c.updateGradient(g);
				}
			}
		}
	}


	public double computeNorm(){
		return computeNorm(null);
	}

	/**
	 * compute total probability of all parses with root 
	 * semantics e. (if e = null, compute probability of 
	 * all parses.
	 */
	public double computeNorm(Exp e){
		double norm = 0.0;
		for (Cell c : fullparses()){
			if (e==null || c.getCat().getSem().equals(e))
				norm+=c.inside();
		}
		return norm;
	}



	public List<ParseResult> getParseResults(){
		List<ParseResult> ret = new LinkedList<ParseResult>();
		for (Cell c : fullparses()){
			ret.add(new Parse(c));
		}
		return ret;
	}

	public class Parse implements ParseResult {
		public Parse(Cell c){
			this.c = c;
			e = c.getCat().getSem();
		}
		public Exp getExp(){return e;}
		public Cell getCell(){return c;}
		public List<LexEntry> getLexEntries(){ 
			return getMaxLexEntriesFor(c.getCat().getSem()); 
		}

		public void getFeats(HashVector p){ computeMaxAve(getExp(),p); }
		public double getScore() {return c.maxscore(); }
		public String toString(){ return e.toString(); }

		public int noEmpty(){
			int n = 0;
			for ( LexEntry l : getLexEntries() ){
				if (l.getCat().equalsNoSem(Cat.EMP))
					n += 1;
			}
			return n;
		}
		Cell c;
		Exp e;
	}

	static Cat S = Cat.makeCat("S");
	static Cat NP = Cat.makeCat("NP");

	// the actual entries
	Map[][] chart;
	double[][] minScores; 

	// any entry that spans the entire input sentence is kept here
	List parses;

	// stores entries with syntax "S" that span whole input
	//List<Cell> fullparses;

	// number of words in input sentence
	int size;

	// a running total of the number of unique Cells (chart entires)
	long numEntries;

	// the list of words being parsed
	List<String> tokens;

	// variables used to do pruning based on syntax 
	// and/or semantics.  
	List preds;
	List predCounts;
	int theCount;
	int existsCount;

	public static int pruneN = 50;

	//List allowedCats;

}
