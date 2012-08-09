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

/**
 *  This is used to skip words by tagging
 *  them with an empty category:
 *   
 *   	X EMP => X
 *   	EMP X => X
 */ 
public class EmptyCatRule implements BinaryParseRule {


    public void newCellsFrom(Cell left, Cell right, List result){
	Cat leftCat = left.getCat();
	Cat rightCat = right.getCat();

	if (leftCat.equals(Cat.EMP) && rightCat.equals(Cat.EMP))
	    return;

	if (leftCat.equals(Cat.EMP)){
	    Cat newCat = rightCat.copy();
	    List children = new LinkedList();
	    children.add(left);
	    children.add(right);
	    result.add(new Cell(newCat,
				left.getStart(),
				right.getEnd(),
				children,
				"empty"));
	    
	}

	if (rightCat.equals(Cat.EMP)){
	    Cat newCat = leftCat.copy();
	    List children = new LinkedList();
	    children.add(left);
	    children.add(right);
	    result.add(new Cell(newCat,
				left.getStart(),
				right.getEnd(),
				children,
				"empty"));
	    
	}

	//if (rightCat.equals(Cat.EMP) || leftCat.equals(Cat.EMP)){
	//System.out.println("HERE: "+leftCat+" : "+rightCat);
	//}
    }
}
