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
 * right now only conjoins two things.  i should do a 
 *  more general version later.
 */
public class SetExp extends Exp {

    private SetExp(){
	exps = new LinkedList<Exp>();
    }

    public SetExp(String input,Map vars){
	exps = new LinkedList();
	LispReader lr = new LispReader(new StringReader(input));
	String t = lr.next();  // read the identifier "set"
	if (!t.equals("set")){
	    System.err.println("Bad Set identifier: "+t);
	    System.exit(-1);
	}
	while (lr.hasNext()){
	    exps.add(Exp.makeExp(lr.next(),vars));
	}
    }

    public Exp simplify(List<Var> vars){
	for (int i=0; i<exps.size(); i++){
	    exps.set(i,exps.get(i).simplify(vars));
	}
	return this;
    }

    public Exp replace(Exp olde, Exp newe){
	//System.out.println("Exps: "+exps);
	if (equals(olde)) return newe;
	Exp e;
	for (int i=0; i<exps.size(); i++){
	    e = exps.get(i);
	    if (e.equals(olde))
		exps.set(i,newe);
	    else 
		exps.set(i,e.replace(olde,newe));
	    e = exps.get(i);
	    if (e==null)
		return null;
	}
	return this;
    }


    public Exp instReplace(Exp olde, Exp newe){
	if (this==olde)
	    return newe;

	Exp e;
	for (int i=0; i<exps.size(); i++){
	    e = exps.get(i);
	    exps.set(i,e.instReplace(olde,newe));
	    e = exps.get(i);
	    if (e==null)
		return null;
	}
	return this;
    }

    public Exp copy(){
	SetExp result = new SetExp();
	Iterator<Exp> i = exps.iterator();
	while (i.hasNext()){
	    result.exps.add(i.next().copy());
	}
	return result;
    }

    public double varPenalty(List varNames){
	double result = 0.0;
	Iterator<Exp> i = exps.iterator();
	while (i.hasNext()){
	    result+=i.next().varPenalty(varNames);
	}
	return result;
    }

    public String toString(List varNames){
	StringBuffer result = new StringBuffer();
	result.append("(set");
	Iterator<Exp> i = exps.iterator();
	while (i.hasNext()){
	    result.append(" ").append(i.next().toString(varNames));
	}
	result.append(")");
	return result.toString();
    }


    public int hashCode(){
	int ret = 0;
	Iterator i = exps.iterator();
	while (i.hasNext()){
	    ret+=i.next().hashCode();
	}
	return ret;
    }

    public boolean equals(Object o){
	if (o instanceof SetExp){
	    SetExp c = (SetExp)o;
	    return exps.size()==c.exps.size()
		&& exps.containsAll(c.exps) 
		&& c.exps.containsAll(exps);
	}
	return false;
    }

    public boolean equals(int type, Exp o){
	return equals(o);
    }

    public Type type(){
	return FType.ET;
    }

    public boolean wellTyped(){
	Iterator<Exp> i = exps.iterator();
	Exp e;
	while (i.hasNext()){
	    e = i.next();
	    if (e==null || !e.wellTyped()){
		//System.out.println("not well typed:"+e);
		return false;
	    }
	    if (!PType.E.matches(e.type())){
		//System.out.println("not type T:"+e);
		return false;
	    }
	    //System.out.println("well typed: "+e);
	}
	//System.out.println("conj well typed");
	return true;
    }

    public Type inferType(List<Var> vars, List<List<Type>> varTypes){
	for (Exp e : exps){
	    Type t=e.inferType(vars,varTypes);
	    if (t==null || !t.subType(PType.E)){
		inferedType=null; // update cache
		return null;
	    }
	}
	inferedType=FType.ET; // update cache
	return FType.ET; // could we infer subtypes here?
    }

    public void freeVars(List bound, List free){
	Iterator<Exp> i = exps.iterator();
	while (i.hasNext()){
	    i.next().freeVars(bound,free);
	}
    }

    public void extractFuncts(List functors, List functees, Exp orig){
	for (int i=0; i<exps.size(); i++){
	    Exp e = exps.get(i);
	    e.extractFuncts(functors,functees,orig);
	    List vars = e.freeVars();
	    if (vars.size()==1){
		Var v = (Var)vars.get(0);
		Exp functee = new Funct(v,e.copy());
		Var v2 = new Var(functee.type());
		Appl a = new Appl(v2,v);
	        exps.set(i,a);
		Exp functorbody = orig.copy();
		exps.set(i,e);
		Exp functor = new Funct(v2,functorbody);
		functors.add(functor);
		functees.add(functee);
	    }
	}
    }

    public double complexity(){
	Iterator<Exp> i = exps.iterator();
	double result = 0.0;
	while (i.hasNext()){
	    result+=i.next().complexity();
	}
	return result;
    }

    public List merge(Exp e, Exp top){
	return null;
    }

    public List merge(List e, Exp top){
	return null;
    }

    public void extractPTypeExps(List l){
	Exp e;
	Iterator<Exp> i = exps.iterator();
	while (i.hasNext()){
	    e = i.next();
	    e.extractPTypeExps(l);
	}
    }

    public void allPreds(int arity, List result){
	Exp e;
	Iterator<Exp> i = exps.iterator();
	while (i.hasNext()){
	    e = i.next();
	    e.allPreds(arity,result);
	}
    }

    public void allLits(int arity, List result, boolean b){
	Exp e;
	Iterator<Exp> i = exps.iterator();
	while (i.hasNext()){
	    e = i.next();
	    e.allLits(arity,result,b);
	}
    }

    public void raisableSubExps(List<Exp> result){
    }

    public void allSubExps(String type, List result){
	Exp e;
	Iterator<Exp> i = exps.iterator();
	while (i.hasNext()){
	    e = i.next();
	    if (e.getClass().getName().equals(type))
		result.add(e);	    
	    e.allSubExps(type,result);
	}
    }

    public void allSubExps(Type type, List result){
	if (type==null || type().equals(type)) result.add(this);
	Exp e;
	Iterator<Exp> i = exps.iterator();
	while (i.hasNext()){
	    e = i.next();
	    e.allSubExps(type,result);
	}
    }

    public void allSubExps(List result){
	result.add(this);
	Exp e;
	Iterator<Exp> i = exps.iterator();
	while (i.hasNext()){
	    e = i.next();
	    e.allSubExps(result);
	}
    }

    public int predCount(Object p){
	int result = 0;
	Exp e;
	Iterator<Exp> i = exps.iterator();
	while (i.hasNext()){
	    e = i.next();
	    result+=e.predCount(p);
	}
	return result;
    }

    public int repeatPredCount(int t, Object p){
        int result = 0;                                                                      
        Exp e;       
	Iterator<Exp> i;

        i = exps.iterator();
        while (i.hasNext()){
            e = i.next();
            result+=e.repeatPredCount(t,p);
        }
        return result; 
    }

    public int expCount(int eq, Exp e){
	int count = 0;
	if (equals(eq,e)) count++;
	Iterator<Exp> i = exps.iterator();
	while (i.hasNext()){
	    Exp ex = i.next();
	    count+=ex.expCount(eq,e);
	}
	return count;
    }

    public int repeatExpCount(int t, Exp e){
	int result = 0;
        Exp ex;       
	Iterator<Exp> i;

        i = exps.iterator();
        while (i.hasNext()){
            ex = i.next();
            result+=ex.repeatExpCount(t,e);
        }
	return result;
    }

    public int expCount(int id){
	int result = 0;
        Exp ex;       
	Iterator<Exp> i;

        i = exps.iterator();
        while (i.hasNext()){
            ex = i.next();
            result+=ex.expCount(id);
        }
	return result;
    }


    public boolean removeUnscoped(List vars){
	Iterator<Exp> i = exps.iterator();
	while (i.hasNext()){
	    Exp e = i.next();
	    if (e.removeUnscoped(vars))
		i.remove();
	}
	return false;
    }

    public Exp deleteExp(Exp l){
	SetExp result = new SetExp();
	Iterator<Exp> i = exps.iterator();
	while (i.hasNext()){
	    Exp e = i.next();
	    if (e!=l)
		result.exps.add(e.deleteExp(l));
	}
	return result;
    }

    void getOuterRefs(Exp e, List<Exp> refs){	
	// now, recurse
	Iterator<Exp> i = exps.iterator();
	while (i.hasNext()){
	    Exp ex = i.next();
	    ex.getOuterRefs(e,refs);
	}
    }

    public void addExp(int i, Exp e){
	exps.add(i,e);
    }

    public Exp removeExp(int i){
	return exps.remove(i);
    }

    public void getConstStrings(List<String> result){
	result.add("set");
	for (Exp e : exps)
	    e.getConstStrings(result);
    }

    public String getHeadString(){
	return "set";
    }

    public double avgDepth(int d){
	double total = 0.0;
	for (Exp e : exps)
	    total+=e.avgDepth(d+1);
	return total/exps.size();
    }

    List<Exp> exps;

}
