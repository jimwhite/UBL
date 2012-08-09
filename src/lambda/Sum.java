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

public class Sum extends Exp {

    public Sum(String input, Map vars){
	LispReader lr = new LispReader(new StringReader(input));
	String stype = lr.next(); // read 'sum'
	// always sum over sets of entities
	arg = new Var(PType.E);
	String argname = lr.next();
	vars.put(argname,arg);
	set = Exp.makeExp(lr.next(),vars);
	body = Exp.makeExp(lr.next(),vars);
	vars.remove(argname);
    }

    public Sum(Exp a, Exp b, Exp c){
	arg = new Var(PType.E);
	set = b;
	body = c;
	body = body.replace(a,arg);
	set = set.replace(a,arg);
    }

    public Exp simplify(List<Var> vars){
	vars.add(arg);
	body = body.simplify(vars);
	set = set.simplify(vars);
	vars.remove(arg);
	return this;
    }

    public Exp replace(Exp olde, Exp newe){
	if (equals(olde))
	    return newe;
	//System.out.println("before: "+this);
	if (arg.equals(olde))
	    arg = (Var)newe;
	if (body.equals(olde))
	    body = newe;
	else 
	    body = body.replace(olde,newe);
	if (set.equals(olde))
	    set = newe;
	else 
	    set = set.replace(olde,newe);
	//System.out.println("after: "+this);
	if (set==null || body==null)
	    return null;
	return this;
    }

    public Exp instReplace(Exp olde, Exp newe){
	if (this==olde)
	    return newe;
	if (arg==olde)
	    arg = (Var)newe;
	body = body.instReplace(olde,newe);
	set = set.instReplace(olde,newe);
	if (body==null)
	    return null;
	return this;
    }

    public boolean equals(Object o){
	if (o instanceof Sum){
	    Sum c = (Sum)o;
	    arg.setEqualTo(c.arg);
	    c.arg.setEqualTo(arg);
	    if (!c.body.equals(body)){
		arg.setEqualTo(null);
		c.arg.setEqualTo(null);
		return false;
	    } 
	    if (!c.set.equals(set)){
		arg.setEqualTo(null);
		c.arg.setEqualTo(null);
		return false;
	    }
	    arg.setEqualTo(null);
	    c.arg.setEqualTo(null);
	    return true;
	} else return false;
    }

    public boolean equals(int type, Exp o){
	if (o instanceof Sum){
	    Sum c = (Sum)o;
	    arg.setEqualTo(c.arg);
	    c.arg.setEqualTo(arg);
	    if (!c.body.equals(body)){
		arg.setEqualTo(null);
		c.arg.setEqualTo(null);
		return false;
	    } 
	    if (!c.set.equals(set)){
		arg.setEqualTo(null);
		c.arg.setEqualTo(null);
		return false;
	    }
	    arg.setEqualTo(null);
	    c.arg.setEqualTo(null);
	    return true;
	} else return false;
    }

    public int hashCode(){
	return arg.hashCode()+body.hashCode()+set.hashCode();
    }

    public Exp copy(){
	Sum c = new Sum(arg.copy(),set.copy(),body.copy());
	Var v = new Var(arg.type());
	c.set = c.set.replace(c.arg,v);
	c.body = c.body.replace(c.arg,v);
	c.arg = v;
	return c;
    }

    public double varPenalty(List varNames){
	varNames.add(arg);
	double result = set.varPenalty(varNames)
	    + body.varPenalty(varNames);
	varNames.remove(arg);
	return result;
    }

    public String toString(List varNames){
	varNames.add(arg);
	String result="(sum ";
	result += arg.toString(varNames)+" "
	    +set.toString(varNames)+" "
	    +body.toString(varNames)+")";
	varNames.remove(arg);
	return result;
    }


    public Type inferType(List<Var> vars, List<List<Type>> varTypes){
	arg.addTypeSig(vars,varTypes);
	Type t=set.inferType(vars,varTypes);
	if (t==null || !t.subType(PType.T)){
	    arg.removeTypeSig(vars,varTypes);
	    inferedType=null; // update cache
	    return null;
	}
	t=body.inferType(vars,varTypes);
	arg.removeTypeSig(vars,varTypes);
	if (body instanceof Appl) return t;
	if (t==null || !t.subType(PType.I)) {
	    inferedType=null; // update cache
	    return null;
	}
	inferedType=t; // update cache
	return t;
    }

    public Type type(){
	return PType.I;
    }

    public boolean wellTyped(){
	arg.setTempType(PType.E);
	if (!body.wellTyped() || !set.wellTyped()){
	    arg.setTempType(null);
	    return false;
	}
	arg.setTempType(null);
	return PType.T.equals(set.type()) &&
	    PType.I.equals(body.type());	    
    }

    public void freeVars(List bound, List free){
	bound.add(arg);
	set.freeVars(bound,free);
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
	vars = set.freeVars();
	if (vars.size()==1){
	    Var v = (Var)vars.get(0);
	    Exp functee = new Funct(v,set.copy());
	    Var v2 = new Var(functee.type());
	    Appl a = new Appl(v2,v);
	    Exp e = set;
	    set = a;
	    Exp functorbody = orig.copy();
	    set = e;
	    Exp functor = new Funct(v2,functorbody);
	    functors.add(functor);
	    functees.add(functee);
	}
	set.extractFuncts(functors,functees,orig);
    }

    public double complexity(){
	return arg.complexity()+body.complexity()
	    +set.complexity()+1;
    }

    public List merge(Exp e, Exp top){
	return null;
    }

    public List merge(List e, Exp top){
	return null;
    }

    public void extractPTypeExps(List l){
	l.add(this);
	body.extractPTypeExps(l);
	set.extractPTypeExps(l);
    }

    public void allPreds(int arity, List result){
	body.allPreds(arity,result);
	set.allPreds(arity,result);
    }

    public void allLits(int arity, List result, boolean b){
	body.allLits(arity,result,b);
	set.allLits(arity,result,b);
    }

    public void allSubExps(String type, List result){
	if (arg.getClass().getName().equals(type))
	    result.add(arg);
	if (body.getClass().getName().equals(type))
	    result.add(body);
	if (set.getClass().getName().equals(type))
	    result.add(set);
	body.allSubExps(type,result);
	set.allSubExps(type,result);
    }

    public void allSubExps(Type type, List result){
	if (type==null) result.add(this);
	if (type!=null && arg.type().equals(type))
	    result.add(arg);
	if (type!=null && body.type().equals(type))
	    result.add(body);
	if (type!=null && set.type().equals(type))
	    result.add(set);
	body.allSubExps(type,result);
	set.allSubExps(type,result);
    }

    public void allSubExps(List result){
	result.add(this);
	body.allSubExps(result);
	set.allSubExps(result);
    }

    public void raisableSubExps(List<Exp> result){
	body.raisableSubExps(result);
	set.raisableSubExps(result);
    }

    public int predCount(Object p){
	return body.predCount(p)+set.predCount(p);
    }

    public int repeatPredCount(int t, Object p){
	return body.repeatPredCount(t,p)+set.repeatPredCount(t,p);
    }

    public int expCount(int eq, Exp e){
	int count = 0;
	if (equals(eq,e)) count++;
	return count+body.expCount(eq,e)+set.expCount(eq,e);
    }

    public int repeatExpCount(int t, Exp e){
	return body.repeatExpCount(t,e)+set.repeatExpCount(t,e);
    }

    public int expCount(int id){
	return body.expCount(id)+set.expCount(id);
    }

    public boolean removeUnscoped(List vars){
	vars.add(arg);
	set.removeUnscoped(vars);
	body.removeUnscoped(vars);
	vars.remove(arg);
	return false;
    }

    public Exp deleteExp(Exp l){
	return new Sum(arg,set.deleteExp(l),body.deleteExp(l));
    }

    void getOuterRefs(Exp e, List<Exp> refs){
	body.getOuterRefs(e,refs);
	set.getOuterRefs(e,refs);
    }

    public void getConstStrings(List<String> result){
	result.add("sum");
	body.getConstStrings(result);
	set.getConstStrings(result);
    }

    public String getHeadString(){
	return "sum";
    }

    public double avgDepth(int d){
	return (body.avgDepth(d+1)+set.avgDepth(d+1))/2.0;
    }

    Var arg;
    Exp set;
    Exp body;
}
