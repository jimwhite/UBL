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

import utils.*;
import java.io.*;
import java.util.*;

public class Count extends Exp {

    public Count(String input, Map vars){
	LispReader lr = new LispReader(new StringReader(input));
	String stype = lr.next(); // read 'count';
	// always count entities
	arg = new Var(PType.E);
	String argname = lr.next();
	vars.put(argname,arg);
	body = Exp.makeExp(lr.next(),vars);
	//body = body.replace(a,arg);
    }

    public Count(Exp a, Exp b){
	arg = new Var(PType.E);
	body = b;
	body = body.replace(a,arg);
    }

    public Exp simplify(List<Var> vars){
	vars.add(arg);
	body=body.simplify(vars);
	vars.remove(arg);
	return this;
    }

    public Exp replace(Exp olde, Exp newe){
	if (equals(olde))
	    return newe;
	if (arg.equals(olde))
	    arg = (Var)newe;
	if (body.equals(olde))
	    body = newe;
	else 
	    body = body.replace(olde,newe);
	if (body==null)
	    return null;
	return this;
    }

    public Exp instReplace(Exp olde, Exp newe){
	if (this==olde)
	    return newe;
	if (arg==olde)
	    arg = (Var)newe;
	body = body.instReplace(olde,newe);
	if (body==null)
	    return null;
	return this;
    }

    public boolean equals(Object o){
	if (o instanceof Count){
	    Count c = (Count)o;
	    arg.setEqualTo(c.arg);
	    c.arg.setEqualTo(arg);
	    if (!c.body.equals(body)){
		arg.setEqualTo(null);
		c.arg.setEqualTo(null);
		return false;
	    } else {
		arg.setEqualTo(null);
		c.arg.setEqualTo(null);
		return true;
	    }
	} else return false;
    }

    public boolean equals(int type, Exp o){
	if (o instanceof Count){
	    Count c = (Count)o;
	    arg.setEqualTo(c.arg);
	    c.arg.setEqualTo(arg);
	    if (!c.body.equals(body)){
		arg.setEqualTo(null);
		c.arg.setEqualTo(null);
		return false;
	    } else {
		arg.setEqualTo(null);
		c.arg.setEqualTo(null);
		return true;
	    }
	} else return false;
    }

    public int hashCode(){
	return arg.hashCode()+body.hashCode();
    }

    public Exp copy(){
	Count c = new Count(arg,body.copy());
	//Var v = new Var(arg.type());
	//c.body = c.body.replace(c.arg,v);
	//c.arg = v;
	return c;
    }

    public double varPenalty(List varNames){
	varNames.add(arg);
	double result = body.varPenalty(varNames);
	varNames.remove(arg);
	return result;
    }

    public String toString(List varNames){
	varNames.add(arg);
	String result;
	result =  "(count "+arg.toString(varNames)+" "
	    +body.toString(varNames)+")";
	varNames.remove(arg);
	return result;
    }


    public Type inferType(List<Var> vars, List<List<Type>> varTypes){
	arg.addTypeSig(vars,varTypes);
	Type t=body.inferType(vars,varTypes);
	arg.removeTypeSig(vars,varTypes);
	if (t==null || !t.subType(PType.T)){
	    inferedType=null; // update cache
	    return null;
	}
	inferedType=PType.I; // update cache
	return PType.I;
    }

    public Type type(){
	// figure out the type of the variable
	if (!wellTyped()) return null;
	return PType.I;
    }

    public boolean wellTyped(){
	arg.setTempType(PType.E);
	if (!body.wellTyped()){
	    arg.setTempType(null);
	    return false;
	}
	arg.setTempType(null);
	return body.type().equals(PType.T);
    }

    public void freeVars(List bound, List free){
	bound.add(arg);
	body.freeVars(bound,free);
	bound.remove(arg);
    }

    public void extractFuncts(List functors, List functees, Exp orig){
	List vars = body.freeVars();
	if (vars.size()==1){
	    Var v = (Var)vars.get(0);
	    Exp functee = new Funct(v,body.copy());
	    Var v2 = new Var(functee.type());
	    Appl a = new Appl(v2,v);
	    Exp e = body;
	    body = a;
	    Exp functorbody = orig.copy();
	    body = e;
	    Exp functor = new Funct(v2,functorbody);
	    functors.add(functor);
	    functees.add(functee);
	}
	body.extractFuncts(functors,functees,orig);
    }

    public double complexity(){
	return arg.complexity()+body.complexity()+1;
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
	body.allLits(arity,result,b);
    }

    public void allSubExps(String type, List result){
	if (arg.getClass().getName().equals(type))
	    result.add(arg);
	if (body.getClass().getName().equals(type))
	    result.add(body);
	body.allSubExps(type,result);
    }

    public void allSubExps(Type type, List result){
	if (type==null || type.equals(type())) result.add(this);
	if (arg.type().equals(type))
	    result.add(arg);
	if (body.type().equals(type))
	    result.add(body);
	body.allSubExps(type,result);
    }

    public void allSubExps(List result){
	result.add(this);
	body.allSubExps(result);
    }

    public void raisableSubExps(List<Exp> result){
	body.raisableSubExps(result);
    }

    public int predCount(Object e){
	return body.predCount(e);
    }

    public int repeatPredCount(int t, Object p){
	return body.repeatPredCount(t,p);
    }

    public int expCount(int id){
	return body.expCount(id);
    }

    public int expCount(int eq, Exp e){
	int count = 0;
	if (equals(eq,e)) count++;
	return count+body.expCount(eq,e);
    }

    public int repeatExpCount(int t, Exp e){
	return body.repeatExpCount(t,e);
    }

    public boolean removeUnscoped(List vars){
	vars.add(arg);
	body.removeUnscoped(vars);
	vars.remove(arg);
	return false;
    }

    public Exp deleteExp(Exp l){
	return new Count(arg,body.deleteExp(l));
    }

    void getOuterRefs(Exp e, List<Exp> refs){
	body.getOuterRefs(e,refs);
    }

    public void getConstStrings(List<String> result){
	result.add("count");
	body.getConstStrings(result);
    }

    public String getHeadString(){
	return "count";
    }

    public double avgDepth(int d){
	return body.avgDepth(d+1);
    }

    Var arg;
    Exp body;
}
