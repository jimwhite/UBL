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

public class Const extends Exp{

    public Const(String n){
	//System.out.println("Const "+n);
	int colon = n.indexOf(':');
	if (colon==-1){
	    System.err.println("WARNING: null type const "+n); 
	    t = null;
	    name = n;
	} else {
	    name = n.substring(0,colon);
	    t = new PType(n.substring(colon+1,n.length()));
	}
	

	if (name.indexOf('.')!=-1 && name.indexOf('\\')!=-1){
	    System.err.println("WARNING: \\ and . found in const.");
	}
    }

    public Const(){
	name = "##con"+String.valueOf(uniqueID++);
    }

    public Const(String n, Type typ){
	name = n;
	t = typ;
    }

    public Exp simplify(List<Var> vars){
	return this;
    }

    public double varPenalty(List varNames){
	return 0.0;
    }
    
    public String toString(List varNames){
	if (t==null)
	    return name;
	else
	    return name+":"+t;
    }

    public String toSlotsString(boolean outer){
	if (outer)
	    return "info = "+toString()+"\n";
	else
	    return "";
    }

    public String toLatexString(List varNames){
	return "\\mbox{{\\it "+name+"}}";
    }

    public Exp copy(){
	return new Const(name,t);
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

    public boolean equals(Object o){
	if (o instanceof Const){
	    Const c = (Const) o;
	    if (!t.equals(c.t))
		return false;
	    if (!c.name.equals(name))
		return false;
	    return true;
	} else return false;
    }

    public boolean equals(int type, Exp o){
	if (type==NO_VARS && (o instanceof Var)) return true;
	if (type==NO_VARS_CONSTS) return true;
	if (o instanceof Const){
	    Const c = (Const) o;
	    if (!t.equals(c.t))
		return false;
	    if (!c.name.equals(name))
		return false;
	    return true;
	} else return false;
    }

    public boolean wellTyped(){
	if (t!=null)
	    return true;
	else 
	    return false;
    }

    public Type type(){
	return t;
    }

    public Type inferType(List<Var> vars, List<List<Type>> varTypes){
	inferedType=t; // update cache
	return t;
    }

    public void extractFuncts(List functors, List functees, Exp orig){
    }

    public void freeVars(List bound, List free){}

    public double complexity(){
	return 1.0;
    }

    public List merge(Exp e, Exp top){
	return null;
    }

    public List merge(List e, Exp top){
	return null;
    }

    public void extractPTypeExps(List l){
	l.add(this);
    }

    public void allPreds(int arity, List result){
	if (arity==0 || arity==-1)
	    if (!result.contains(this))
		result.add(this);
    }

    public void allLits(int arity, List result, boolean b){}
    public void allSubExps(String type, List result){}
    public void allSubExps(Type type, List result){
	if (type==null || type().equals(type)) result.add(this);
    }

    public void allSubExps(List result){
	result.add(this);
    }

    public void raisableSubExps(List<Exp> result){
    }

    public int predCount(Object e){
	if (equals(e))
	    return 1;
	else
	    return 0;
    }

    public int repeatPredCount(int t, Object p){
	return 0;
    }

    public int expCount(int id){
	return 0;
    }

    public int expCount(int eq, Exp e){
	return 0;
    }

    public int repeatExpCount(int t, Exp e){
	return 0;
    }

    public int hashCode(){
	return name.hashCode()+t.hashCode();
    }

    public boolean removeUnscoped(List vars){
	return false;
    }

    public Exp deleteExp(Exp l){
	return new Const(name,t);
    }

    void getOuterRefs(Exp e, List<Exp> refs){
    }

    public void getConstStrings(List<String> result){
	result.add(toString(null));
    }

    public String getHeadString(){
	return toString(null);
    }

    public double avgDepth(int d){
	return d;
    }

    String name;
    Type t;
    static int uniqueID = 0;
}
