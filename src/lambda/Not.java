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

/*
 * right now only conjoins two things.  i should do a 
 *  more general version later.
 */
public class Not extends Exp {

    public Not(String input,Map vars){
	LispReader lr = new LispReader(new StringReader(input));
	String t = lr.next();   // read the 'not'
	body = Exp.makeExp(lr.next(),vars);
    }
    
    Not(Exp e){
	body = e;
    }

    public Exp replace(Exp olde, Exp newe){
	if (body.equals(olde))
	    body = newe;
	else
	    body = body.replace(olde,newe);
	if (body==null) return null;
	return this;
    }

    public Exp instReplace(Exp olde, Exp newe){
	if (this==olde) return newe;
	body = body.instReplace(olde,newe);
	if (body==null) return null;
	return this;
    }

    public Exp simplify(List<Var> vars){
	body = body.simplify(vars);
	return this;
    }

    public Exp copy(){
	return new Not(body.copy());
    }

    public double varPenalty(List varNames){
	return body.varPenalty(varNames);
    }

    public String toString(List varNames){
	return "(not "+body.toString(varNames)+")";
    }

    public boolean equals(Object o){
	if (o instanceof Not){
	    Not n = (Not)o;
	    return body.equals(n.body);
	}
	return false;
    }

    public boolean equals(int type, Exp o){
	if (o instanceof Not){
	    Not n = (Not)o;
	    return body.equals(n.body);
	}
	return false;
    }

    public int hashCode(){
	return 7+body.hashCode();
    }

    public Type type(){
	return PType.T;
    }

    public Type inferType(List<Var> vars, List<List<Type>> varTypes){
	// TODO: handle polarity here ... for example (and (city x) (not (river x))) 
	//       should be well typed but will currently fail
	Type t=body.inferType(vars,varTypes);
	if (t==null || !t.subType(PType.T)){
	    inferedType=null; // update cache
	    return null;
	}
	inferedType=PType.T; // update cache
	return PType.T;
    }

    public boolean wellTyped(){
	if (!body.wellTyped())
	    return false;
	if (!body.type().equals(PType.T))
	    return false;
	return true;
    }

    public void freeVars(List bound, List free){
	body.freeVars(bound,free);
    }

    public void extractFuncts(List functors, List functees, Exp orig){
    }
    /*
    public void subExps(List expsin){
	expsin.add(this);
	Iterator i = exps.iterator(),j;
	Exp one,two;
	// need complex groups of twos thing here...
	while (i.hasNext()){
	    one = (Exp)i.next();
	    j = exps.iterator();
	    while (j.hasNext()){
		two = (Exp)j.next();
		if (!one.equals(two)){
		    Exp e = makePair(op_type,one,two);
		    if (!expsin.contains(e))
			expsin.add(e);
		}
	    }
	    one.subExps(expsin);
	}
    }
    */

    public double complexity(){
	return body.complexity()+1;
    }

    public List merge(Exp e, Exp top){
	return null;
    }

    public List merge(List e, Exp top){
	return null;
    }

    public void extractPTypeExps(List l){
	body.extractPTypeExps(l);
    }

    public void allPreds(int arity, List result){
	body.allPreds(arity,result);
    }

    public void allLits(int arity, List result, boolean b){
	if (b){
	    if (body instanceof Lit){
		Lit l = (Lit) body;
		if (arity==-1 || l.arity()==arity)
		    result.add(this);
	    }
	}
	body.allLits(arity,result,b);
    }

    public void allSubExps(String type, List result){
	if (body.getClass().getName().equals(type))
	    result.add(body);
	body.allSubExps(type,result);
    }

    public void allSubExps(Type type, List result){
	if (type==null || type().equals(type)) result.add(this);
	if (body.type().equals(type))
	    result.add(body);
	body.allSubExps(type,result);
    }

    public void allSubExps(List result){
	result.add(this);
	body.allSubExps(result);
    }

    public void raisableSubExps(List<Exp> result){
    }

    public int predCount(Object e){
	return body.predCount(e);
    }

    public int repeatPredCount(int t, Object p){
	return body.repeatPredCount(t,p);
    }

    public int expCount(int eq, Exp e){
	int count = 0;
	if (equals(eq,e)) count++;
	return count+body.expCount(eq,e);
    }

    public int repeatExpCount(int t, Exp e){
	return body.repeatExpCount(t,e);
    }

    public int expCount(int id){
	return body.expCount(id);
    }

    public Exp getBody(){
	return body;
    }

    public boolean removeUnscoped(List vars){
	return body.removeUnscoped(vars);
    }

    public Exp deleteExp(Exp l){
	return new Not(body.deleteExp(l));
    }

    void getOuterRefs(Exp e, List<Exp> refs){
	body.getOuterRefs(e,refs);
    }

    public void getConstStrings(List<String> result){
	result.add("not");
	body.getConstStrings(result);
    }

    public String getHeadString(){
	return "not";
    }

    public double avgDepth(int d){
	return body.avgDepth(d+1);
    }

    Exp body;

}
