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
import java.io.*;
import parser.*;

/*
 * A typing system like the one in Heim and Kratzer
 */

public abstract class Type {
	static public Type makeType(String input){
		if (input.indexOf(",")==-1)
			// atomic type
			return new PType(input);
		else
			// complex type
			return new FType(input);
	}

	static public Type makeType(List<Var> inputs, Type output){
		if (inputs.size()==0) return output;
		Type t = new FType(inputs.get(inputs.size()-1).type(),output);
		for (int i=inputs.size()-2; i>=0; i--)
			t = new FType(inputs.get(i).type(),t);
		return t;
	}

	abstract public boolean matches(Type t);
	abstract public boolean subType(Type t);
	abstract public int numCommas();
	abstract public Type commonSuperType(Type t);

	abstract public Cat makeCat();
	abstract public List<Cat> makeAllCats();

	public Type copy(){
		return null;
	}

	abstract public Type makeSuper();

	public static void main(String[] args){
		Type t1 = Type.makeType("<e,t>");
		Type t2 = Type.makeType("<<e,t>,<e,t>>");
		System.out.println(t1);
		System.out.println(t2);
		System.out.println(t2.equals(t1));
	}

	// This sets the maximum complexity of a type. Ignore this
	// constraint by setting to -1	
	public static int maxNumCommas=8;
}
