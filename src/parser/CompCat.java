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

import lambda.*;
import java.util.*;
import utils.*;

/**
 * Complex syntactic category
 */
public class CompCat extends Cat {

	//< constructors: default, string, and simple

	public CompCat(){
	}

	public CompCat(String cat){
		// find the outermost slash
		// assumes that one exists
		int depth=0,slash=-1;
		char c;
		cat = cat.trim();
		if (cat.startsWith("(") && cat.endsWith(")")){
			// check if we need to strip them
			boolean trim = true;
			depth=0;
			char ch;
			for (int i=0; i<cat.length()-1; i++){
				c = cat.charAt(i);
				if (c=='(') depth++;
				if (c==')') depth--;
				if (depth==0) trim = false;
			}
			if (trim) cat = cat.substring(1,cat.length()-1);

		}
		depth = 0;
		for (int i=0; i<cat.length(); i++){
			c = cat.charAt(i);
			if (c=='(') depth++;
			if (c==')') depth--;
			if (depth==0 && (c == '\\' || c=='/' || c=='|')){
				slash = i;
				slashDir = c;
			}
		}
		if (slash==-1){
			System.err.println("No outer slash found in "+cat);
		}
		left = Cat.makeCat(cat.substring(0,slash));
		right = Cat.makeCat(cat.substring(slash+1,cat.length()));
	}

	public CompCat(char slash, Cat l, Cat r){
		if (l==null || r==null){ 
			System.out.println("CompCat null: "+l +" : "+r);
			Exception e = new Exception();
			e.printStackTrace();
			System.exit(-1);
		}
		slashDir = slash;
		left = l;
		right = r;
	}
	//>

	//< string printing functions. 
	//  output with and without semantics 

	public String toString(){
		String result="";
		if (isDisj())
			result += "DISJ("; 
		if (right instanceof CompCat)
			result+= left.toString()+slashDir
			+((CompCat)right).toStringRight();	    
		else
			result+= left.toString()+slashDir+right.toString();	    
		if (isConj() || isDisj())
			result += ")"; 
		if (sem != null)
			return result+=" : "+sem;
		return result;
	}

	private String toStringRight(){
		if (right instanceof CompCat)
			return "("+left+slashDir+((CompCat)right).toStringRight()+")";
		else
			return "("+left+slashDir+right+")";	    
	}

	public String toStringNoSem(){
		String result="";
		if (isConj())
			result += "CONJ("; 
		if (isDisj())
			result += "DISJ("; 
		if (right instanceof CompCat)
			result+= left.toString()+slashDir
			+((CompCat)right).toStringRight();	    
		else
			result+= left.toString()+slashDir+right.toString();	    
		if (isConj() || isDisj())
			result += ")"; 
		return result;
	}



	//< accessor functions for the slash
	public boolean hasSlash(char s){
		if (s=='|' || slashDir=='|') return true;
		return s==slashDir;
	}

	public char getSlash(){
		return slashDir;
	}
	//>

	//< getLeft(), getRight() accessor functions for sub Cats
	public Cat getLeft(){
		return left;
	}

	public Cat getRight(){
		return right;
	}

	//>

	public Cat apply(Cat input){
		if (input==null || input.sem==null || sem==null){
			return null;
		}
		if (slashDir == '|'){
			return null;
		}
		if (right.matchesNoSem(input)){
			Exp e = sem.apply(input.sem);
			if (e!=null
					&& e.inferType()!=null){
				Cat newcat;
				newcat = left.copy();
				newcat.sem = e;
				return newcat;
			}
		}
		return null;
	}

	
	
	public Cat comp(CompCat input){
		if (slashDir == '|' || input.slashDir=='|') return null;

		if (useEisnerNormalForm){
			if (slashDir=='\\' && fromLeftComp)
				return null;
			if (slashDir=='/' && fromRightComp)
				return null;
		}

		if (input==null || input.getSem()==null || getSem()==null)
			return null;
		if (right.matchesNoSem(input.left) &&
				getSem() instanceof Funct){
			Exp newSem = ((Funct)getSem()).comp(input.getSem());
			if (newSem!=null
					&& newSem.inferType()!=null){
				CompCat newcat = new CompCat();
				newcat.slashDir = slashDir;
				newcat.right = input.right.copy();
				newcat.left = left.copy();
				newcat.setSem(newSem);
				if (useEisnerNormalForm){
					if (slashDir=='/')
						newcat.fromRightComp=true;
					else 
						newcat.fromLeftComp=true;
				}
				return newcat;
			}
		}
		return null;
	}

	//< comp(Cat, List, List, boolean) returns the category 
	// that would be created when c is passed as an argument 
	// to this category
	// 
	// return value is true if a comisition happened and false if application
	// (or nothing at all)
	public boolean gencomp(Cat c, List result, List types, boolean allowFunctComp){

		if (c==null || c.getSem()==null || getSem()==null){
			//|| !isComplete() || !c.isComplete()){
			return false;
		}
		//boolean b = c.isConj();
		//c.setConj(false);
		//boolean b0 = c.isDisj();
		//c.setDisj(false);
		//System.out.println("comp: "+this+ " and "+c);

		// test conditions for Eisner normal-form parsing
		/*
	if (useEisnerNormalForm && c instanceof CompCat){
	    if (slashDir=='\\' && fromLeftComp)
		return;
	    if (slashDir=='/' && fromRightComp)
		return;
	}
		 */
		/*
	boolean frc=false,flc=false;
	if (c instanceof CompCat){
	    CompCat cmp = (CompCat)c;
	    frc = cmp.fromRightComp;
	    flc = cmp.fromLeftComp;
	    cmp.fromRightComp = false;	    
	    cmp.fromLeftComp = false;
	}
		 */

		char cslash=' ';

		boolean isComp = false;

		// cycle over all of the possible inputs
		Cat input = c, domain = right, inputrest=null;
		boolean done = false;
		while (!done){
			if (right.matchesNoSem(input)){
				Cat newcat;
				if (inputrest == null){ // this is the root of c
					newcat = left.copy();
				} else {
					newcat = c.copy();
					isComp = true;		    
					((CompCat)newcat).replaceLeft(input,left.copy());
					/*
		    if (slashDir=='/' && useEisnerNormalForm)
			((CompCat)newcat).fromRightComp = true;
		    if (slashDir=='\\' && useEisnerNormalForm)
			((CompCat)newcat).fromLeftComp = true;
					 */
				}
				//newcat.setConj(false);
				//newcat.setDisj(false);
				//newcat.setComplete(true);

				if (inputrest == null){
					// function application
					//System.out.println("THIS: "+this);
					//System.out.println("c: "+c);
					newcat.sem = ((Funct)sem).apply(c.sem);
				} else {
					if (c.getSem().type() instanceof FType && 
							getSem().type() instanceof FType){
						// function composition
						//System.out.println("comp: "+this+ " and "+c);
						FType first = (FType)c.getSem().type();
						//System.out.println("first: "+first);
						FType second = (FType)getSem().type();
						//System.out.println("second: "+second);
						Var v0 = new Var(first);
						Var v1 = new Var(second);
						Var v2 = new Var(first.domain());
						Appl a0 = new Appl(v0,v2);
						Appl a2 = new Appl(v1,a0);
						Funct f0 = new Funct(v2,a2);
						Funct f1 = new Funct(v1,f0);
						Exp l = new Funct(v0,f1);
						//Exp l = Exp.makeExp("(lambda $f5 "+first+" (lambda $f3 "+
						//			second+" (lambda $z18 "+first.domain()+" ($f3 ($f5 $z18)))))");
						//System.out.println("intial function: "+l+" -- "+c.getSem()+" -- "+c.getSem().type());
						l = l.apply(c.getSem());
						//System.out.println(l+" -- "+getSem()+" -- "+getSem().type());
						if (l!=null) l = l.apply(getSem());
						//System.out.println(l);
						newcat.setSem(l);
						//System.out.println(newcat+"\n");
					}
				}
				if (newcat.getSem()!=null) // && newcat.getSem().wellTyped())
					result.add(newcat);
				if (inputrest == null)
					types.add("apply");
				else
					types.add("comp");

			}
			// try the next possible input
			if (allowFunctComp && input instanceof CompCat){
				//&&((CompCat)input).slashDir==slashDir){
				//if (inputrest!=null && slashDir=='\\'){
				//    done = true;
				//} else {
				inputrest = input;
				input = ((CompCat)input).getLeft();
				//}
			} else { 
				done = true;
			}
		}
		/*
	c.setConj(b);
	c.setDisj(b0);
	if (c instanceof CompCat){
	    CompCat cmp = (CompCat)c;
	    cmp.fromRightComp = frc;	    
	    cmp.fromLeftComp = flc;
	}
		 */

		// debugging print code
		/*
	if (result.size()>0){
	    System.out.println("comp: "+this+ " and "+c);
	    System.out.println("comp result: "+result+"\n\n");
	}
		 */

		return isComp;
	}

	/*
	 * replace the leftmost instance of old with new
	 * this is only used by the comp(..) function above
	 */
	public Cat replaceLeft(Cat oldc, Cat newc){
		if (equals(oldc))
			return newc;
		if (left.equals(oldc)){
			left = newc;
			return this;
		} else if (left instanceof CompCat)
			return ((CompCat)left).replaceLeft(oldc,newc);
		else
			return null;
	}

	//>

	//< copy and equals functions
	/*
	 * returns a copy of this CompCat
	 */
	public Cat copy(){
		CompCat cop = new CompCat();
		//System.out.println("compcat copy: "+this);
		cop.left = left.copy();
		cop.right = right.copy();
		cop.slashDir = slashDir;
		cop.sem = sem;
		//cop.fromLeftComp = fromLeftComp;
		//cop.fromRightComp = fromRightComp;
		return cop;
	}

	public boolean matchesNoSem(Cat other){
		if (!(other instanceof CompCat)) return false;
		CompCat cc = (CompCat) other;
		if (cc.slashDir != slashDir
				&& slashDir != '|' && cc.slashDir!='|')
			return false;
		if (!left.matchesNoSem(cc.left)) return false;
		if (!right.matchesNoSem(cc.right)) return false;
		return true;
	}

	public boolean matches(Cat other){
		if (!(other instanceof CompCat)) return false;
		CompCat cc = (CompCat) other;
		if (cc.slashDir != slashDir
				&& slashDir != '|' && cc.slashDir!='|')
			return false;
		if (!matchesNoSem(other)) return false;
		if (sem!=null && other.sem!=null && !sem.equals(other.sem)) return false;
		return true;
	}

	public boolean equalsNoSem(Object other){
		if (!(other instanceof CompCat)) return false;
		CompCat cc = (CompCat) other;
		//if( fromLeftComp != cc.fromLeftComp) return false;
		//if (fromRightComp != cc.fromRightComp) return false;
		if (cc.slashDir != slashDir)
			//&& slashDir != '|' && cc.slashDir!='|')
			return false;
		//if (conj!=cc.conj) return false;
		//if (disj!=cc.disj) return false;
		//if (complete!=cc.complete) return false;
		if (!left.equalsNoSem(cc.left)) return false;
		if (!right.equalsNoSem(cc.right)) return false;
		//System.out.println("EQUALS NO SEM: "+this+" : "+other);
		return true;
	}


	public boolean equals(Object other){
		if (!(other instanceof CompCat)) return false;
		CompCat cc = (CompCat) other;
		if (!equalsNoSem(other)) return false;
		if (sem!=null && cc.sem!=null && !sem.equals(cc.sem)) return false;
		return true;
	}
	//>

	//< accessor functions for normal form parsing
	/*
    public boolean fromRightComp(){
	return fromRightComp;
    }
	 */
	//>

	public int syntaxHash(){
		return (int)slashDir+left.syntaxHash()+3*right.syntaxHash();
	}

	public int numSlashes(){
		return left.numSlashes()+right.numSlashes()+1;
	}

	// the slash
	char slashDir;

	// sub Cats
	Cat left;
	Cat right;

	// variables for implementing normal form parsing
	public static boolean useEisnerNormalForm = true;
	boolean fromLeftComp=false;
	boolean fromRightComp=false;

	//< main(String[]) testing code
	public static void main(String[] args){

		Lang.loadLang("((foo:t e t)"+
		" (bar:t e t))");

		CompCat cc1 = (CompCat)Cat.makeCat("N\\N : (lambda x <e,t> (lambda y e (and (foo:t y) (x y))))");
		CompCat cc2 = (CompCat) Cat.makeCat("N\\N : (lambda x <e,t> (lambda y e (and (bar:t y) (x y))))");
		Cat c1 = Cat.makeCat("N : (lambda x e (foo:t x))");
		System.out.println(cc1);
		System.out.println(cc2);
		System.out.println(cc1.comp(cc2));
		System.out.println(cc2.apply(c1));

	}
	//>

}
