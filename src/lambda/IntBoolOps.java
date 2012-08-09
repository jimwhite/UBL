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
public class IntBoolOps extends Exp {

    public IntBoolOps(int t, Exp one, Exp two){
	con_type = t;
	left = one;
	right = two;
    }

    public IntBoolOps(String input, Map vars){
	LispReader lr = new LispReader(new StringReader(input));
	String t = lr.next();  // read type
	if (t.equals("="))
	    con_type = EQUALS;
	else if (t.equals("<"))
	    con_type = LESS_THAN;
	else if (t.equals(">"))
	    con_type = GREATER_THAN;
	else System.err.println("Unknown type "+t+"in LogCon");

	left = Exp.makeExp(lr.next(),vars);

	right = Exp.makeExp(lr.next(),vars);
	
	//if (!wellTyped()){
	//    System.out.println("MISTYPED 8: "+this);
	//}
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
	return new IntBoolOps(con_type,left.copy(),right.copy());
    }

    public double varPenalty(List varNames){
	return left.varPenalty(varNames)
	    + right.varPenalty(varNames);
    }

    public String toString(List varNames){
	if (con_type==EQUALS)
	    return "(= "+left.toString(varNames)
		+" "+right.toString(varNames)+")";
	if (con_type==LESS_THAN)
	    return "(< "+left.toString(varNames)
		+" "+right.toString(varNames)+")";
	if (con_type==GREATER_THAN)
	    return "(> "+left.toString(varNames)
		+" "+right.toString(varNames)+")";
	return null;
    }

    public String toSlotsString(boolean outer){
	String pr = null;
	if (left instanceof Lit && right instanceof Const){
	    pr = ((Lit)left).getPred().getName();
	}
	if (pr==null) return "";
	if (con_type==EQUALS){
	    return pr+" = true\n";
	}
	if (con_type==LESS_THAN){
	    return pr + " < " +right+"\n";
	}
	if (con_type==GREATER_THAN){
	    return pr + " > " +right+"\n";
	}
	return "";
    }

    public int hashCode(){
	return (int)con_type+right.hashCode()+left.hashCode();
    }



    public boolean equals(Object o){
	if (o instanceof IntBoolOps){
	    IntBoolOps i = (IntBoolOps)o;
	    return con_type==i.con_type 
		&& left.equals(i.left) && right.equals(i.right);
	}
	return false;
    }

    public boolean equals(int type, Exp o){
	if (o instanceof IntBoolOps){
	    IntBoolOps i = (IntBoolOps)o;
	    if (con_type!=i.con_type) return false;
	    return left.equals(type,i.left) && right.equals(type,i.right);
	}
	return false;
    }

    public Type type(){
	return PType.T;
    }

    public boolean wellTyped(){
		if (!left.wellTyped()){
			return false;
		}
		if (!right.wellTyped()){
			return false;
		}
		if (left instanceof Var && !((Var)left).updateTempType(PType.I)){
			return false;
		}
		if (right instanceof Var && !((Var)right).updateTempType(PType.I)){
			return false;
		}
		if (!left.type().matches(PType.I)){
			//System.out.println("LEFT MISTYPED!");
			return false;
		}
		if (!right.type().matches(PType.I)){
			//System.out.println("RIGHT MISTYPED!");
			return false;
		}
		if (!right.type().matches(left.type())){
			//System.out.println("RIGHT MISTYPED!");
			return false;
		}
		return true;
    }

    public Type inferType(List<Var> vars, List<List<Type>> varTypes){
	Type t=left.inferType(vars,varTypes);
	if (t==null || !t.subType(PType.I)){
	    if (!(left instanceof Appl)){
		inferedType=null; // update cache
		return null;
	    }
	}
	t=right.inferType(vars,varTypes);
	if (t==null || !t.subType(PType.I)){
	    if (!(right instanceof Appl)){
		inferedType=null; // update cache
		return null;
	    }
	}
	inferedType=PType.T; // update cache
	return PType.T;
    }

    public void freeVars(List bound, List free){
		left.freeVars(bound,free);
		right.freeVars(bound,free);
    }

    public void extractFuncts(List functors, List functees, Exp orig){
	left.extractFuncts(functors,functees,orig);
	List vars = left.freeVars();
	if (vars.size()==1){
	    Var v = (Var)vars.get(0);
	    Exp functee = new Funct(v,left.copy());
	    Var v2 = new Var(functee.type());
	    Appl a = new Appl(v2,v);
	    Exp e = left;
	    left = a;
	    Exp functorbody = orig.copy();
	    left = e;
	    Exp functor = new Funct(v2,functorbody);
	    functors.add(functor);
	    functees.add(functee);
	}
	
	right.extractFuncts(functors,functees,orig);
	vars = right.freeVars();
	if (vars.size()==1){
	    Var v = (Var)vars.get(0);
	    Exp functee = new Funct(v,right.copy());
	    Var v2 = new Var(functee.type());
	    Appl a = new Appl(v2,v);
	    Exp e = right;
	    right = a;
	    Exp functorbody = orig.copy();
	    right = e;
	    Exp functor = new Funct(v2,functorbody);
	    functors.add(functor);
	    functees.add(functee);
	}
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
	return new IntBoolOps(con_type,left.deleteExp(l),right.deleteExp(l));
    }

    void getOuterRefs(Exp e, List<Exp> refs){
	left.getOuterRefs(e,refs);
	right.getOuterRefs(e,refs);
    }

    public void getConstStrings(List<String> result){
	if (con_type==EQUALS)
	    result.add("=");
	if (con_type==LESS_THAN)
	    result.add("<");
	if (con_type==GREATER_THAN)
	    result.add(">");
	left.getConstStrings(result);
	right.getConstStrings(result);
    }

    public String getHeadString(){
	if (con_type==EQUALS)
	    return "=";
	if (con_type==LESS_THAN)
	    return "<";
	else
	    return ">";
    }

    public double avgDepth(int d){
	return (left.avgDepth(d+1)+right.avgDepth(d+1))/2.0;
    }

    Exp left;
    Exp right;
    int con_type;

    static public int EQUALS = 0;
    static public int LESS_THAN = 1;
    static public int GREATER_THAN = 2;
}
