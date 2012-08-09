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

public class Pred {

    public Pred(String input){
	typeSigs = new LinkedList<List<Type>>();
	addTuple(input);
    }

    public void addTuple(String input){
	LispReader lr = new LispReader(new StringReader(input));	
	
	name = lr.next();
	List<Type> argsList = new LinkedList<Type>();
	while (lr.hasNext()){
	    argsList.add(Type.makeType(lr.next()));
	}
	typeSigs.add(argsList);
	Type r = argsList.get(argsList.size()-1);
	if (rtype == null)
	    rtype = r;
	else
	    rtype = rtype.commonSuperType(r);

	//System.err.println("added "+name+"="+typeSigs);

	//types = new Type[argsList.size()-1];
	//for (int i=0; i<types.length; i++){
	//    types[i]=(Type)argsList.get(i);
	//}
	//rtype = (Type)argsList.get(argsList.size()-1);
    }
    
    public int arity(){
	return typeSigs.get(0).size()-1;
    }

    public String getName(){
	return name;
    }

    public Type retType(List<Type> inputs){
	// if a single type matches, return its return type
	// if more than one match, return common return type
	// if no matches, return null
	if (inputs.size()!=arity()) return null;
	Type ret = null;
	for (List<Type> tuple : typeSigs){
	    boolean match = true;
	    for (int i=0; i<inputs.size(); i++){
		if (!tuple.get(i).matches(inputs.get(i)))
		    match = false;
	    }
	    if (match){
		if (ret!=null){
		    return type(); // more than one match
		}
		ret = tuple.get(tuple.size()-1); // set return type
	    }
	}
	//if (ret==null){
	//    System.err.println("NO MATCH: "+inputs+" --- "+typeSigs);
	//}
	return ret;  // there was not more than one match
    }

    public Type retType(Exp[] inputs){
	// if a single type matches, return its return type
	// if more than one match, return common return type
	// if no matches, return null
	if (inputs.length!=arity()) return null;
	Type ret = null;
	for (List<Type> tuple : typeSigs){
	    boolean match = true;
	    for (int i=0; i<inputs.length; i++){
		if (!tuple.get(i).matches(inputs[i].type()))
		    match = false;
	    }
	    if (match){
		if (ret!=null) return type(); // more than one match
		ret = tuple.get(tuple.size()-1); // set return type
	    }
	}
	//if (ret==null){
	//    System.err.println("NO MATCH: "+inputs+" --- "+typeSigs);
	//}
	return ret;  // there was not more than one match
    }

    public boolean restrictTypes(Exp[] inputs){
	// first, find the type signatures that are compatible
	List<List<Type>> matchedTuples = new LinkedList<List<Type>>();

	List<Type> inputTypes = new LinkedList<Type>();
	for (int i=0; i<inputs.length; i++){
	    if (inputs[i] instanceof Var){
		Type t = ((Var)inputs[i]).getTempType();
		if (t!=null)
		    inputTypes.add(t);
		else 
		    inputTypes.add(inputs[i].type());
	    } else {
		inputTypes.add(inputs[i].type());
	    }
	}

	for (List<Type> tuple : typeSigs){
	    boolean match = true;
	    for (int i=0; i<inputs.length; i++){
		Type t= tuple.get(i);
		if (!t.matches(inputTypes.get(i))){
		    match = false;
		}
	    }
	    if (match)
		matchedTuples.add(tuple);
	}
	if (matchedTuples.size()==0){
	    /*
	    System.out.println("NO TUPLES for "+this);
	    for (int i=0; i<inputs.length; i++){
		System.out.print(inputs[i].type()+" : "+typeSigs.get(0).get(i)+" ");
	    }
	    System.out.println();
	    */
	    return false;
	}

	// now, compute the common supertypes
	List<Type> superTypes = new LinkedList<Type>();
	superTypes.addAll(matchedTuples.get(0));
	for (int i=0; i<inputs.length; i++){
	    for (int j=1; j<matchedTuples.size(); j++){
		Type newType = superTypes.get(i);
		newType = newType.commonSuperType(matchedTuples.get(j).get(i));
		//System.out.println(i+" : "+matchedTuples.get(j).get(i)+" = "+newType);
		superTypes.set(i,newType);
	    }
	}
	//System.out.println("supertypes: "+superTypes);

	// finally, restrict the variables types
	
	for (int i=0; i<inputs.length; i++){
	    Exp e = inputs[i];
	    if (e instanceof Var){
		Var v = (Var)e;
		//System.out.println("updating: "+superTypes.get(i));
		if (!v.updateTempType(superTypes.get(i))){ 
		    return false;
		}
	    }
	}
	/*
	for (int i=0; i<inputs.length; i++){
	    Exp e = inputs[i];
	    if (e instanceof Var){
		Var v = (Var)e;
		//System.out.println("updating: "+superTypes.get(i));
		if (!v.updateTempType(superTypes.get(i))){ 
		    return false;
		}
	    }
	}
	*/
	return true;
    }

    public Type type(){
	return rtype;
    }

    /*
    public Type getType(int pos){
	return types[pos];
    }

    public Type[] getTypes(){	
	return types;
    }

    */

    public String toString(){
	StringBuffer result = new StringBuffer();
	result.append(name).append("/").append(arity());
	return result.toString();
    }

    public List<List<Type>> getTypeSigs(){
	return typeSigs;
    }

    String name;
    List<List<Type>> typeSigs = new LinkedList<List<Type>>();
    Type rtype;   // the return type

    //Type[] types;  // the argument types
    
}
