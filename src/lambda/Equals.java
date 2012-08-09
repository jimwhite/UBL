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
public class Equals extends Exp {

    public Equals(Exp one, Exp two){
	left = one;
	right = two;
    }

    public Equals(String input, Map vars){
	LispReader lr = new LispReader(new StringReader(input));
	String t = lr.next();  // read type
	if (!t.equals("=")){
	    System.err.println("= not found in Equals constructor");
	    System.exit(-1);
	}

	left = Exp.makeExp(lr.next(),vars);
	right = Exp.makeExp(lr.next(),vars);

	if (!wellTyped()){
	    System.out.println("MISTYPED in Equals.java: "+this);
	}
    }

    public Exp simplify(List<Var> vars){
	left=left.simplify(vars);
	right=right.simplify(vars);
	return this;
    }

    public Exp replace(Exp olde, Exp newe){
	if (equals(olde)) return newe;
	if (left.equals(olde))
	    left = newe;
	else 
	    left = left.replace(olde,newe);
	if (right.equals(olde))
	    right = newe;
	else 
	    right = right.replace(olde,newe);
	if (left==null || right==null)
	    return null;
	return this;
    }

    public Exp instReplace(Exp olde, Exp newe){
	if (this==olde) return newe;
	left = left.instReplace(olde,newe);
	right = right.instReplace(olde,newe);
	if (left==null || right==null)
	    return null;
	return this;
    }

    public Exp copy(){
	return new Equals(left.copy(),right.copy());
    }

    public double varPenalty(List varNames){
	return left.varPenalty(varNames)
	    + right.varPenalty(varNames);
    }

    public String toString(List varNames){
	return "(= "+left.toString(varNames)
	    +" "+right.toString(varNames)+")";
    }



    public int hashCode(){
	return right.hashCode()+left.hashCode();
    }

 
    public boolean equals(Object o){
	if (o instanceof Equals){
	    Equals i = (Equals)o;
	    return left.equals(i.left) && right.equals(i.right);
	}
	return false;
    }

    public boolean equals(int type, Exp o){
	if (o instanceof Equals){
	    Equals i = (Equals)o;
	    return left.equals(i.left) && right.equals(i.right);
	}
	return false;
    }

    public Type inferType(List<Var> vars, List<List<Type>> varTypes){
	Type t1=left.inferType(vars,varTypes);
	Type t2=right.inferType(vars,varTypes);
	if (t1==null || t2==null || !t1.matches(t2)){
	    inferedType=null; // update cache
	    return null;
	}
	inferedType=PType.T; // update cache
	return PType.T;	
    }

    public Type type(){
	return PType.T;
    }

    public boolean wellTyped(){
	if (!left.wellTyped())
	    return false;
	if (!right.wellTyped())
	    return false;
	if (left instanceof Var && !((Var)left).updateTempType(right.type())){
	    return false;
	}
	if (right instanceof Var){
	    //System.out.println(left+" : "+left.type());
	    //System.out.println("Updating right: "+left.type());
	    if(!((Var)right).updateTempType(left.type())){
		return false;
	    }
	}
	if (!left.type().matches(right.type())){
	    return false;
	}
	return true;
    }

    public void freeVars(List bound, List free){
	left.freeVars(bound,free);
	right.freeVars(bound,free);
    }

    public void extractFuncts(List functors, List functees, Exp orig){
    }

    public double complexity(){
	return left.complexity()+right.complexity()+1;
    }

    public List merge(Exp e, Exp top){
	return null;
    }

    public List merge(List e, Exp top){
	return null;
    }

    public void extractPTypeExps(List l){
	left.extractPTypeExps(l);
	right.extractPTypeExps(l);
    }

    public void allPreds(int arity, List result){
	left.allPreds(arity,result);
	right.allPreds(arity,result);
    }

    public void allLits(int arity, List result, boolean b){
	left.allLits(arity,result,b);
	right.allLits(arity,result,b);
    }

    public void allSubExps(String type, List result){
	if (left.getClass().getName().equals(type))
	    result.add(left);
	if (right.getClass().getName().equals(type))
	    result.add(right);
	left.allSubExps(type,result);
	right.allSubExps(type,result);
    }

    public void allSubExps(Type type, List result){
	if (type==null || type().equals(type)) result.add(this);
	if (left.type().equals(type))
	    result.add(left);
	if (right.type().equals(type))
	    result.add(right);
	left.allSubExps(type,result);
	right.allSubExps(type,result);
    }

    public void allSubExps(List result){
	result.add(this);
	left.allSubExps(result);
	right.allSubExps(result);
    }

    public void raisableSubExps(List<Exp> result){
    }

    public int predCount(Object p){
	return left.predCount(p)+right.predCount(p);
    }

    public int repeatPredCount(int t, Object p){
	return left.repeatPredCount(t,p)+right.repeatPredCount(t,p);
    }

    public int expCount(int eq, Exp e){
	int count = 0;
	if (equals(eq,e)) count++;
	return count+left.expCount(eq,e)+right.expCount(eq,e);
    }

    public int repeatExpCount(int t, Exp e){
	return left.repeatExpCount(t,e)+right.repeatExpCount(t,e);
    }

    public int expCount(int id){
	return left.expCount(id)+right.expCount(id);
    }

    public boolean removeUnscoped(List vars){
	boolean result = left.removeUnscoped(vars);
	result = right.removeUnscoped(vars) || result;
	return result;
    }

    public Exp deleteExp(Exp l){
	return new Equals(left.deleteExp(l),right.deleteExp(l));
    }

    void getOuterRefs(Exp e, List<Exp> refs){
	left.getOuterRefs(e,refs);
	right.getOuterRefs(e,refs);
    }

    public void getConstStrings(List<String> result){
	result.add("=");
	left.getConstStrings(result);
	right.getConstStrings(result);
    }

    public String getHeadString(){
	return "=";
    }

    public double avgDepth(int d){
	return (left.avgDepth(d+1)+right.avgDepth(d+1))/2.0;
    }

    Exp left;
    Exp right;
}
