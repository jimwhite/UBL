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
 * A lambda calc function application
 */

public class Appl extends Exp {

    public Appl(String input, Map vars){
	LispReader lr = new LispReader(new StringReader(input));	
	String predname = lr.next();
	if (predname.startsWith("!")){
	    pred = new Ana(predname);
	} else {
	    pred = (Var)vars.get(predname);
	}
	if (pred==null)
	    System.err.println("ERROR: unkown var "+predname+" in Appl");
	List argsList = new LinkedList();
	while (lr.hasNext()){
	    argsList.add(Exp.makeExp(lr.next(),vars));
	}
	args = new Exp[argsList.size()];
	for (int i=0; i<args.length; i++){
	    args[i]=(Exp)argsList.get(i);
	}
    }

    public Appl(Var p, int arity){
	args = new Exp[arity];
        pred = p;
    }

    public Appl(Var p, Exp e){
	args = new Exp[1];
	args[0]=e;
        pred = p;
    }

    public Appl(Var p, List<Var> exps){
	args = new Exp[exps.size()];
	for (int i=0; i<args.length; i++)
	    args[i]=exps.get(i);
        pred = p;
    }

    public Exp simplify(List<Var> vars){
	for (int i=0; i<args.length; i++){
	    args[i]=args[i].simplify(vars);
	}
	return this;
    }


    public void setArg(int i, Exp e){
	args[i]=e;
    }

    public Exp replace(Exp olde, Exp newe){
	//System.out.println("Appl replace "+this+" : "+olde+" : "+newe);
	Exp e;
	for (int i=0; i<args.length; i++){
	    if (args[i].equals(olde))
		args[i]=newe;
	    else
		args[i]=args[i].replace(olde,newe);
	    if (args[i]==null) return null;
	}
	if (pred.equals(olde)){
	    if (newe instanceof Funct){
		e = newe;
		//System.out.println("HERE!!!");
		for (int i=0; e!=null && i<args.length; i++){
		    //System.out.println(e+" : "+args[i]);
		    e =  e.apply(args[i]); // loop over argument length
		    //System.out.println(e);
		}
		return e;
	    } 
	    if (newe instanceof Var){
		pred = (Var)newe;
		return this;
	    }
	    if (newe instanceof Appl){
		Appl a = (Appl)newe;
		Appl newa =  new Appl(a.pred,a.args.length+args.length);
		for (int i=0; i<a.args.length; i++)
		    newa.args[i]=a.args[i];
		for (int i=0; i<args.length; i++)
		    newa.args[i+a.args.length]=args[i];
		//System.out.println("newa: "+newa);
		return newa;
	    }
	    return null;
	}
	return this;
    }

    public Exp instReplace(Exp olde, Exp newe){
	//System.out.println("Appl replace "+this+" : "+olde+" : "+newe);
	Exp e;
	if (this==olde) return newe;
	if (pred==olde) pred=(Var)newe;
	for (int i=0; i<args.length; i++){
	    args[i]=args[i].instReplace(olde,newe);
	    if (args[i]==null) return null;
	}
	return this;
    }

    public Exp copy(){
	//System.out.println("Lit copy: "+this);
	Appl l = new Appl(pred, args.length);
	for (int i=0; i<args.length; i++){
	    l.setArg(i,args[i].copy());
	}
	return l;
    }

    public boolean equals(Object o){
	if (o instanceof Appl){
	    Appl l = (Appl) o;
	    if (!pred.equals(l.pred)) return false;
	    if (args.length != l.args.length) return false;
	    for (int i=0; i<args.length; i++)
		if (!args[i].equals(l.args[i]))
		    return false;
	    return true;
	} else return false;
    }

    public boolean equals(int type, Exp o){
	if (o instanceof Appl){
	    Appl l = (Appl) o;
	    if (!pred.equals(l.pred)) return false;
	    if (args.length != l.args.length) return false;
	    for (int i=0; i<args.length; i++)
		if (!args[i].equals(l.args[i]))
		    return false;
	    return true;
	} else return false;
    }

    public int hashCode(){
	int ret = pred.hashCode();
	for (int i=0; i<args.length; i++)
	    ret+=args[i].hashCode();
	return ret;
    }

    public double varPenalty(List varNames){
	double result = pred.varPenalty(varNames);
	for (int i=0; i<args.length; i++)
	    result+=args[i].varPenalty(varNames);
	return result;
    }

    public String toString(List varNames){
	StringBuffer result = new StringBuffer();
	result.append("(").append(pred.toString(varNames));
	for (int i=0; i<args.length; i++){
	    result.append(" ").append(args[i].toString(varNames));
	}
	result.append(")");
	return result.toString();
    }



    public Type inferType(List<Var> vars, List<List<Type>> varTypes){
	Type t = pred.type();
	FType f;
	int i;
	for (i=0; i<args.length && t instanceof FType; i++){
	    f = (FType)t;
	    Type at = args[i].inferType(vars,varTypes);
	    if (at==null || !at.matches(f.domain())){
		inferedType=null; // update cache
		return null;
	    }
	    t = f.range();
	}
	if (i<args.length){
	    inferedType=null; // update cache
	    return null;
	}
	inferedType=t; // update cache
	return t;
    }

    public Type type(){
	Type t = pred.type();
	FType f;
	int i;
	for (i=0; i<args.length && t instanceof FType; i++){
	    f = (FType)t;
	    if (!args[i].type().matches(f.domain())){
		return null;
	    }
	    t = f.range();
	}
	if (i<args.length){
	    return null;
	}
	return t;
    }

    public boolean wellTyped(){
	Type t = pred.type();
	FType f;
	int i;
	for (i=0; i<args.length && (t instanceof FType); i++){
	    f = (FType)t;
	    if (args[i]==null) return false;
        
        // I'm not sure why this would
        // happen but it's breaking here, Tom
        if (f==null) return false;
        
	if (!args[i].wellTyped())
	    return false;
	if (args[i].type() == null)
	    return false;
	if (!args[i].type().matches(f.domain())){
	    return false;
	}
	t = f.range();
	}
	if (i<args.length){
	    return false;
	}
	return true;
    }

    public void freeVars(List bound, List free){
	if (!bound.contains(pred) && !free.contains(pred))
	    free.add(pred);
	for (int i=0; i<args.length; i++){
	    args[i].freeVars(bound,free);
	}
    }

    public void extractFuncts(List functors, List functees, Exp orig){
	for (int i=0; i<args.length; i++){
	    args[i].extractFuncts(functors,functees,orig);
	}
    }

    public double complexity(){
	double result = 0.0;
	for (int i=0; i<args.length; i++){
	    result+=args[i].complexity();
	}
	result+=pred.complexity();
	return result;
    }

    public List merge(Exp e, Exp top){
	return null;
    }

    public List merge(List e, Exp top){
	return null;
    }

    public void extractPTypeExps(List l){
	for (int i=0; i<args.length; i++){
	    args[i].extractPTypeExps(l);
	}
    }

    public void allPreds(int arity, List result){
	for (int i=0; i<args.length; i++){
	    args[i].allPreds(arity,result);
	}
    }

    public void allLits(int arity, List result, boolean b){
	for (int i=0; i<args.length; i++){
	    args[i].allLits(arity,result,b);
	}
    }

    public void allSubExps(String type, List result){
	if (pred.getClass().getName().equals(type))
	    result.add(pred);
	for (int i=0; i<args.length; i++){
	    if (args[i].getClass().getName().equals(type))
		result.add(args[i]);
	    args[i].allSubExps(type,result);
	}
    }

    public void allSubExps(Type type, List result){
	//if (pred.type().equals(type))
	//    result.add(pred);
	for (int i=0; i<args.length; i++){
	    if (args[i].type().equals(type))
		result.add(args[i]);
	    args[i].allSubExps(type,result);
	}
    }

    public void allSubExps(List result){
	for (int i=0; i<args.length; i++){
	    args[i].allSubExps(result);
	}
    }

	
    public void raisableSubExps(List<Exp> result){
	result.add(this);
    }

    public int predCount(Object e){
	int result = 0;
	if (pred.equals(e)) result++;
	for (int i=0; i<args.length; i++){
	    result+=args[i].predCount(e);
	}
	return result;
    }


    public int repeatPredCount(int t, Object p){
	int result = 0;
	for (int i=0; i<args.length; i++){
	    if (args[i]!=null)
		result+=args[i].repeatPredCount(t,p);
	}	
	return result;
    }

    public int expCount(int id){
	int result = 0;
	for (int i=0; i<args.length; i++){
	    result+=args[i].expCount(id);
	}
	return result;
    }

    public int expCount(int eq, Exp e){
	int count = 0;
	if (equals(eq,e)) count++;
	for (int i=0; i<args.length; i++){
	    if (args[i]!=null)
		count+=args[i].expCount(eq,e);
	}	
	return count;
    }

    public int repeatExpCount(int t, Exp e){
	int result = 0;
	for (int i=0; i<args.length; i++){
	    if (args[i]!=null)
		result+=args[i].repeatExpCount(t,e);
	}	
	return result;
    }

    public boolean removeUnscoped(List vars){
	if (pred.removeUnscoped(vars))
	    return true;
	for (int i=0; i<args.length; i++)
	    if (args[i].removeUnscoped(vars))
		return true;
	return false;
    }

    public Exp deleteExp(Exp e){
	//System.out.println("Lit copy: "+this);
	Appl l = new Appl(pred, args.length);
	for (int i=0; i<args.length; i++){
	    l.setArg(i,args[i].deleteExp(e));
	}
	return l;
    }

    void getOuterRefs(Exp e, List<Exp> refs){	
	for (int i=0; i<args.length; i++)
	    args[i].getOuterRefs(e,refs);
    }

    public void getConstStrings(List<String> result){
	for (int i=0; i<args.length; i++)
	    args[i].getConstStrings(result);
    }

    public String getHeadString(){
	return "appl";
    }

    public double avgDepth(int d){
	double total=d;
	for (int i=0; i<args.length; i++)
	    total+=args[i].avgDepth(d+1);
	return total/(args.length+1);
    }

    Var pred; 
    Exp[] args;

}
