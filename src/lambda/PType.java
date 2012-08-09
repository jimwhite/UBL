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
import utils.*;
import java.io.*;
import parser.*;

/*
 * A typing system like the one in Heim and Kratzer
 *  This class represents primitive types. At the
 *  highest level these are: 
 *     e(enitity), t(truth), and i(numeric)
 *  
 *  but it is also possible to introduce a finer grained
 *  typing system to differentiate between different types of 
 *  entities. This should be loaded using addTypesFromFile.
 *  
 *  The type heirarchy should be stored in a file with one type
 *  relation per line using the the following format:
 * 		(
 * 		entity_type1	
 * 		(finer_entity_type1 entity_type1)
 * 		(finer_entity_type2 entity_type1)	
 * 		)
 * 	e.g.
 * 		(
 * 		lo
 * 		(c lo)
 * 		(s lo)
 * 		)
 * 		
 * 		says that locations (lo) are a type of entity (e) and that 
 * 		cities (c) and states (s) are types of locations.
 *  	
 */

public class PType extends Type {

	public PType(String input){
		setVals(input);
		int i = types.indexOf(this);
		if (i==-1){
			System.out.println("ERROR: unknown PType ["+input+"]");
			System.exit(-1);
		}else{
			PType p = (PType)types.get(i);
			sup = p.sup;
		}
	}

	public PType(){
	}

	public Type copy(){
		return new PType(type);
	}
	
	// reads in one line of the type heirarchy file
	public void setVals(String input){
		if (input.indexOf("(")!=-1){
			LispReader lr = new LispReader(new StringReader(input));
			type = lr.next();
			if (lr.hasNext())
				sup = new PType(lr.next());
			if (lr.hasNext())
				System.out.println("ERROR: only one supertype allowed in PType..."+input);
		} else {
			type = input;
			if (type.equals("i"))
				sup=PType.I;
			else 
				sup=PType.E;
		}
	}

	public String toString(){
		return type;
	}

	public boolean equals(Object o){
		if (o == null) return false;
		if (o instanceof PType){
			PType p = (PType)o;
			if (type.equals(p.type))
				return true;
		}
		return false;
	}

	public boolean matches(Type t){
		if (equals(t))
			return true;
		if (t instanceof PType){
			PType p = (PType)t;
			if (subType(p))
				return true;
			if (p.subType(this))
				return true;
		}
		return false;
	}
	
	// check if tin is a superType of this
	public boolean subType(Type tin){
		if (!(tin instanceof PType)){
			return false;
		}
		PType t= (PType)tin;
		PType p = this;
		while (p!=null){
			if (p.equals(t))
				return true;
			p = p.sup;
		}
		return false;
	}
	
	// check if this is a superType of tin
	public boolean ancestor(Type tin){
		if (!(tin instanceof PType)){
			return false;
		}
		PType p = (PType)tin;
		while (p!=null){
			if (equals(p))
				return true;
			p = p.sup;
		}
		return false;
	}
	
	
	static public void addTypes(String l){
		LispReader lr = new LispReader( new StringReader(l));
		while (lr.hasNext()){
			PType p = new PType();
			p.setVals(lr.next());
			types.add(p);
		}
	}

	static public void addTypesFromFile(String filename){
		StringBuffer types= new StringBuffer();
		try{
			BufferedReader in = new BufferedReader(new FileReader(filename));
			String line = in.readLine();
			while (line!=null){  // for each line in the file
				line.trim();
				line = line.split("\\s*//")[0];
				if (!line.equals("")){
					types.append(line).append(" ");
				}
				line = in.readLine();
			}
		} catch(IOException e){ System.err.println(e); }
		addTypes(types.toString());
	}

	static public boolean hasType(String t){
		Iterator i = types.iterator();
		while (i.hasNext()){
			PType p = (PType)i.next();
			if (p.type.equals(t))
				return true; 
		}
		return false;
	}

	public int hashCode(){
		return type.hashCode();
	}

	static public List getPTypes(){
		return types;
	}
	
	// all things of type T have CCG category S
	// all things of type I or E have CCG category NP
	public Cat makeCat(){
		if (matches(T)) return Cat.makeCat("S");
		return Cat.makeCat("NP");
	}

	public List<Cat> makeAllCats(){
		List<Cat> result = new LinkedList<Cat>();
		result.add(makeCat());
		return result;
	}

	public Type commonSuperType(Type tin){
		if (!(tin instanceof PType)){
			return null;
		}
		PType p= (PType)tin;
		while (p!=null){
			if (subType(p))
				return p;
			p = p.sup;
		}
		return null;
	}

	public Type makeSuper(){
		if (subType(T)) return T;
		//if (subType(I)) return I;
		return E;
	}

	public int numCommas(){
		return 0;
	}

	String type;
	PType sup;

	static List types;
	public static PType E;
	public static PType I;
	public static PType T;
	public static PType n;

	static {
		E=new PType();
		E.type="e";
		E.sup=null;
		I=new PType();	
		I.type="i";
		I.sup=E;
		T=new PType();
		T.type="t";
		T.sup=null;
		n = new PType();
		n.type="none";
		n.sup=null;

		types = new LinkedList();
		types.add(E);
		types.add(I);
		types.add(T);
		types.add(n);
	}

	public static void main(String[] args){
		PType.addTypesFromFile("atis.types");
	}    
}
