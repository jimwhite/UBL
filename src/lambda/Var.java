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

public class Var extends Exp{

    public Var(Type ty){
		
	t = ty;
    }

    public Exp simplify(List<Var> vars){
	return this;
    }

    public double varPenalty(List varNames){
	//int i = varNames.indexOf(this);
	//if (i==-1)
	//    return 0;
	//return 1.0/(i+1);
	return 0.0;
    }
    
    public String toString(List varName){
	return "$"+varName.indexOf(this);
    }

    public String toSlotsString(boolean outer){
	return "";
    }

    public String toLatexString(List varName){
	return "\\$"+varName.indexOf(this);
    }

    // WARNING: vars don't copy
    public Exp copy(){
	return this;
    }

    public Exp replace(Exp olde, Exp newe){
	if (equals(olde)) 
	    return newe;
	else
	    return this;
    }

    public Exp instReplace(Exp olde, Exp newe){
	if (this==olde) 
	    return newe;
	else
	    return this;
    }


    public void setEqualTo(Object o){
	equalo=o;
    }

    public boolean equals(Object o){
	if (equalo!=null && o==equalo) return true;
	return this==o;
	//if (o instanceof Var){
	//    Var v = (Var) o;
	//    return v.name.equals(name);
	//} else return false;
    }

    public boolean equals(int type, Exp o){
	if (type==NO_VARS) return true;
	if (type==NO_VARS_CONSTS && (o instanceof Const)) return true;
	if (equalo!=null && o==equalo) return true;
	return this==o;
	//if (o instanceof Var){
	//    Var v = (Var) o;
	//    return v.name.equals(name);
	//} else return false;
    }

    public int hashCode(){
    	return 0;
    }

    public boolean wellTyped(){
	return true;
    }

    public Type type(){
	return t;
    }

    public Type inferType(List<Var> vars, List<List<Type>> varTypes){
	inferedType=t; // update cache
	return t;
    }

    public void addTypeSig(List<Var> vars, List<List<Type>> varTypes){
	if (!vars.contains(this)){
	    vars.add(this);
	    if (varTypes.size()==0)
		varTypes.add(new LinkedList<Type>());
	    for (List<Type> types : varTypes)
		types.add(type());
	}
    }

    public void removeTypeSig(List<Var> vars, List<List<Type>> varTypes){
	int i = vars.indexOf(this);
	if (i==-1) return;
	vars.remove(i);
	for (List<Type> types : varTypes){
	    types.remove(i);
	}
    }

    public void freeVars(List bound, List free){
		if (!bound.contains(this) && !free.contains(this))
			free.add(this);
    }

    public void extractFuncts(List functors, List functees, Exp orig){
    }

    public double complexity(){
	return 0.0;
    }

    public List merge(Exp e, Exp top){
	return null;
    }

    public List merge(List e, Exp top){
	return null;
    }

    public void extractPTypeExps(List l){
    }

    public void allPreds(int arity, List result){}

    public void allLits(int arity, List result, boolean b){}

    public void allSubExps(String type, List result){}
    public void allSubExps(Type type, List result){}
    public void allSubExps(List result){}
    public void raisableSubExps(List<Exp> result){}

    public int predCount(Object e){
	if (equals(e))
	    return 1;
	else
	    return 0;
    }

    public int repeatPredCount(int t, Object p){
	return 0;
    }

    public int expCount(int eq, Exp e){
	return 0;
    }

    public int repeatExpCount(int t, Exp e){
	return 0;
    }

    public Type getTempType(){
	if (t instanceof FType) return t;
	return tempType;
	//return type();
    }

    public void setTempType(PType t){
	//System.out.println("setting tempType: "+t+ " from "+tempType);
	/*
	if ("e from c".equals(t+ " from "+tempType)){
	    Exception e = new Exception();
	    e.printStackTrace();
	    System.exit(-1);
	}
	*/
	tempType = t;
    }

    public int expCount(int id){
	return 0;
    }

    public boolean updateTempType(Type t){
	if (tempType==null) return true;
	if (t instanceof PType){
	    PType p = (PType)t;
	    if (tempType==null){
		tempType = p;
		return true;
	    }
	    if (tempType.subType(p)){
		//System.out.println("NO CHANGE: "+tempType+" : "+p);
		//tempType=p;
		return true;
	    } else if (p.subType(tempType)){
		//System.out.println(p+" is subType of "+tempType);
		tempType=p;
		//System.out.println("new temp type: "+tempType);
		return true;
	    } else {
		//System.out.println(p+" is not a subType of "+tempType);
		//System.out.println(tempType+" doesn't match "+p);
		return false;
	    }
	} else return true;
    }

    public boolean removeUnscoped(List vars){
	if (vars.contains(this))
	    return false;
	else {
	    //System.out.println("UNSCOPED!!! "+vars);
	    return true;
	}
    }

    public Exp deleteExp(Exp l){
	return copy();
    }

    void getOuterRefs(Exp e, List<Exp> refs){
    }

    public void getConstStrings(List<String> result){
    }

    public String getHeadString(){
	return "var";
    }

    public double avgDepth(int d){
	return d;
    }

    Object equalo;
    Type t;
    PType tempType=null;

    public static Var ET = new Var(FType.ET);

}
