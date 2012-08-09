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

/*
 *  This is a forward/backward composition rule
 *   X/Y Y/Z => X/Z
 *   Y\Z X\Y => X\Z
 */ 
public class CompRule implements BinaryParseRule {

    public void newCellsFrom(Cell left, Cell right, List result){
	if (!left.isComplete || !right.isComplete) return;

	Cat leftc = left.getCat();
	Cat rightc = right.getCat();
	CompCat leftcc,rightcc;
	Cat newcat=null;
	String rulename = "";
	boolean reverse = false;
	Cell newcell;
	if (leftc instanceof CompCat){
	    leftcc = (CompCat)leftc;
	    if (leftcc.hasSlash('/')){
		if (rightc instanceof CompCat){// && !left.fromRightComp){
		    rightcc = (CompCat)rightc;
		    if (rightcc.hasSlash('/')){
			newcat = leftcc.comp(rightcc);
			newcell = addCell(left,right,newcat,"fcomp",reverse,result);
			if (newcell!=null){
			    newcell.fromRightComp=true;
			    result.add(newcell);
			}
		    }
		}

		newcat = leftcc.apply(rightc);
		newcell = addCell(left,right,newcat,"fapply",reverse,result);
		if (newcell!=null) result.add(newcell);
	    }
	}

	if (rightc instanceof CompCat){
	    rightcc = (CompCat)rightc;
	    if (rightcc.hasSlash('\\')){
		if (leftc instanceof CompCat){// && !right.fromLeftComp){
		    leftcc = (CompCat) leftc;
		    if (leftcc.hasSlash('\\')){
			newcat = rightcc.comp(leftcc);
			newcell = addCell(left,right,newcat,"bcomp",reverse,result);
			if (newcell!=null){
			    newcell.fromLeftComp=true;
			    result.add(newcell);
			}
		    }
		}

		newcat = rightcc.apply(leftc);
		newcell = addCell(left,right,newcat,"bapply",reverse,result);
		if (newcell!=null) result.add(newcell);
	    }
	}

    }


    Cell addCell(Cell left, Cell right, Cat newcat, String rulename, 
		 boolean reverse, List result){
	if (newcat!=null && newcat.getSem()!=null){
	    List children = new ArrayList(2);
	    children.add(left);
	    children.add(right);
	    return new Cell(newcat,
			    left.getStart(),
			    right.getEnd(),
			    children,
			    rulename,
			    reverse);
	}		    
	return null;
    }
}

