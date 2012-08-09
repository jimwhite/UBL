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



package lambda;

import java.util.*;
import parser.*;

/*
 * A typing system like the one in Heim and Kratzer
 *  This is the functional type.
 */

public class FType extends Type {
	
	public FType(String input){
		// strip off the otuermost < >
		input = input.trim();
		input = input.substring(1,input.length()-1);

		// find index of the top level comma
		int depth = 0, comma = -1;
		char c;
		for (int i=0; i<input.length(); i++){
			c = input.charAt(i);
			if (c=='<') depth++;
			if (c=='>') depth--;
			if (depth==0 && c==','){
				comma = i;
			}
		}
		if (comma==-1){
			System.err.println("No outer comma found in "+input);
		}

		domain = Type.makeType(input.substring(0,comma));
		range = Type.makeType(input.substring(comma+1,input.length()));
	}

	public FType(Type d, Type r){
		domain = d;
		range = r;
	}


	public Type copy(){
		return new FType(domain.copy(),range.copy());
	}

	public String toString(){
		return "<"+domain+","+range+">";
	}

	public Type domain(){
		return domain;
	}

	public Type range(){
		return range;
	}

	public boolean matches(Type t){
		if (t instanceof FType){
			FType f = (FType)t;
			if (domain==null || range==null) return false;
			return domain.matches(f.domain) && range.matches(f.range);
		} else return false;
	}

	public boolean subType(Type t){
		if (t instanceof FType){
			FType f = (FType)t;
			if (domain==null || range==null) return false;
			return domain.subType(f.domain) && range.subType(f.range);
		} else return false;
	}

	public boolean equals(Object o ){
		if (o instanceof FType){
			FType f = (FType)o;
			if (domain==null || range==null) return false;
			return domain.equals(f.domain) && range.equals(f.range);
		} else return false;
	}

	public int hashCode(){
		return 3*domain.hashCode()+range.hashCode();
	}

	public Cat makeCat(){
		char dir = '|';
		Random r = new Random();
		if (r.nextBoolean())
			dir='\\';
		else 
			dir='/';
		return new CompCat(dir,range.makeCat(),domain.makeCat());
	}
	
	// there is only one category per type as we use vertical
	// slashes. 
	public List<Cat> makeAllCats(){
		List<Cat> result = new LinkedList<Cat>();
		for (Cat l : domain.makeAllCats()){
			for (Cat r : range.makeAllCats()){		
				result.add(new CompCat('|',r.copy(),l.copy()));
			}
		}
		return result;
	}

	public Type makeSuper(){
		return new FType(domain.makeSuper(),range.makeSuper());
	}

	public int numCommas(){
		return domain.numCommas()+range.numCommas()+1;
	}

	public Type commonSuperType(Type t){
		return null;  //TODO: implement this...
	}

	Type domain;
	Type range;

	public static FType ET = new FType(PType.E,PType.T);
	public static FType EET = new FType(PType.E,FType.ET);
	public static FType EE = new FType(PType.E,PType.E);
	public static FType EI = new FType(PType.E,PType.I);

}
