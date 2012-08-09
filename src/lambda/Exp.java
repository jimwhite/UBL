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
 * Pretty much everything is an expression...
 */

public abstract class Exp {

	static public Exp makeExp(String input){
		return makeExp(input,new HashMap());
	}

	static public Exp makeExp(String input, Map vars){

		input = input.trim();

		if (input.startsWith("(")){
			// detemine if it is a function application or a literal
			LispReader lr = new LispReader(new StringReader(input));
			String p = lr.next();
			if (Lang.hasPred(p))
				return new Lit(input,vars);
			if (vars.get(p)!=null || p.startsWith("!"))
				return new Appl(input,vars);
		}

		if (input.startsWith("(lambda "))
			return new Funct(input,vars);

		if (input.startsWith("(< ")||
				input.startsWith("(> "))
			return new IntBoolOps(input,vars);

		if (input.startsWith("(= ")){
			return new Equals(input,vars);
		}

		if (input.startsWith("(count ")) 
			return new Count(input,vars);

		if (input.startsWith("(sum ")) 
			return new Sum(input,vars);

		if (input.startsWith("(set ")) 
			return new SetExp(input,vars);


		if (input.startsWith("(intset ")) 
			return new IntSet(input,vars);

		if (input.startsWith("(forall ") ||
				input.startsWith("(exists "))
			return new Quant(input,vars);

		if (input.startsWith("(min ") ||
				input.startsWith("(max "))
			return new MinMax(input,vars);

		if (input.startsWith("(argmax ") ||
				input.startsWith("(argmin "))
			return new ArgM(input,vars);

		if (input.startsWith("(implies ")||
				input.startsWith("(or ")||
				input.startsWith("(and "))
			return new BoolBoolOps(input,vars);

		if (input.startsWith("(not "))
			return new Not(input,vars);

		if (input.startsWith("(the "))
			return new The(input,vars);

		if (input.startsWith("(")){
			// detemine if it is a function application or a literal
			LispReader lr = new LispReader(new StringReader(input));
			String p = lr.next();
			System.out.println("ERROR: unknown function : "+p);
			System.exit(-1);
		}

		if (input.startsWith("!")){
			return new Ana(input);
		}

		Var v = (Var)vars.get(input);
		if (v!=null) return v;

		return new Const(input);

	}

	/*
	 * Overridden by expressions that can have 
	 * subexpressions.  They should replace any 
	 * subexpression the equals olde with newe.
	 */
	public Exp replace(Exp olde, Exp newe){
		return this;
	}

	// uses instance equality instead of .equals()
	abstract public Exp instReplace(Exp olde, Exp newe);

	/* if this expression is a function and the 
	 * typing works out, then apply returns the
	 * new Exp that is created.  
	 */
	public Exp apply(Exp input){
		return null;
	}

	/* 
	 * should be overwritten by all subclasses to 
	 * return a new copy of themselves
	 */
	abstract public Exp copy();

	boolean copyAna=false;
	public Exp copyAna(){
		copyAna=true;
		Exp e = copy();
		copyAna=false;
		return e;
	}

	/*
	 *  All subclasses should know how to check their 
	 *  type.
	 */
	public abstract boolean wellTyped();

	/*
	 *  All subclasses should know their type.
	 */
	public abstract Type type();


	// does inference to determine the type variables can take, based on the
	// places they are used in the expression.   returns null if there is no
	// valid type for one of the variables in the expression
	public Type inferType(){
		List<Var> vars = new LinkedList<Var>();
		List<List<Type>> varTypes = new LinkedList<List<Type>>();
		Type t=  inferType(vars,varTypes);
		inferedType=t;
		return t;
	}
	public Type inferedType = null; // simple cache of last value

	abstract public Type inferType(List<Var> vars, List<List<Type>> varTypes);

	public List freeVars(){
		temp.clear();
		temp2.clear();
		freeVars(temp,temp2);
		return new LinkedList(temp2);
	}

	abstract public void freeVars(List bound, List free);

	public String toString(){
		temp.clear();
		return toString(temp);
	}
	static List temp = new LinkedList();
	static List temp2 = new LinkedList();
	static List temp4 = new LinkedList();

	public double varPenalty(){
		temp4.clear();
		return varPenalty(temp4);
	}

	abstract public String getHeadString();

	abstract public double varPenalty(List varNames);

	abstract public String toString(List varNames);

	abstract public void extractFuncts(List functor, List functee, Exp orig);

	abstract public double complexity();

	public List merge(Exp e){ return merge(e,this); }

	abstract public List merge(Exp e, Exp top);

	public List merge(List e){ return merge(e,this); }

	abstract public List merge(List e, Exp top);

	// WARNING: destructive...
	public void simplify(){
		simplify(new LinkedList<Var>());
	}
	abstract public Exp simplify(List<Var> vars);

	static List temp3 = new LinkedList();
	public List extractPTypeExps(){
		temp3.clear();
		extractPTypeExps(temp3);
		return temp3;
	}

	abstract public void extractPTypeExps(List l);

	public Exp extractFunctor(Exp e){
		Exp n = copy();
		Var v = new Var(e.type());
		n.replace(e,v);
		if (!equals(n))
			return new Funct(v,n);
		else
			return null;
	}

	// if arity==-1, returns preds of all arities
	abstract public void allPreds(int arity, List result);

	// if arity==-1, returns lits of all arities
	abstract public void allLits(int arity, List result, boolean includeFalse);

	// if arity==-1, returns lits of all arities
	public void allLits(int arity, List result){
		allLits(arity,result,false);
	}

	public int allLitsCount(){
		List l = new LinkedList();
		allLits(-1,l,false);
		return l.size();
	}

	public List getAnaphors(){
		return allSubExps("lambda.Ana");
	}

	public Exp applyAna(Ana a, Exp ref){
		Ana.instEq = true;
		Funct f = new Funct(a,this);
		Exp e = f.apply(ref); 
		Ana.instEq = false;
		return e;
	}

	public List allSubExps(String type){
		List result = new LinkedList();
		allSubExps(type,result);
		return result;
	}
	abstract public void allSubExps(String type, List result);

	public List<Exp> allSubExps(Type type){
		List<Exp> result = new LinkedList<Exp>();
		if (type().matches(type))
			result.add(this);
		allSubExps(type,result);
		return result;
	}
	abstract public void allSubExps(Type type, List result);
	public List<Exp> allSubExps() {
		List<Exp> result = new LinkedList<Exp>();
		allSubExps(result);
		return result;
	}

	abstract public void allSubExps(List result);

	abstract public void raisableSubExps(List<Exp> result);

	public int correctLitsCount(Exp ref){
		List l1 = new LinkedList();
		allLits(-1,l1,false);

		List l2 = new LinkedList();
		ref.allLits(-1,l2,false);

		int correct =0;
		for (int i=0; i<l1.size(); i++){
			boolean found = false;
			Lit lit1 = (Lit)l1.get(i);
			for (int j=0; j<l2.size() && !found; j++){
				Lit lit2 = (Lit)l2.get(j);
				if (lit1.equalsNoVars(lit2)) found=true;
			}
			if (found) correct++;
		}
		return correct;
	}

	public boolean isConstFunct(){
		return false;
	}

	abstract public int predCount(Object pred);
	abstract public int expCount(int eq, Exp e);

	abstract public int repeatPredCount(int type, Object pred);
	abstract public int repeatExpCount(int type, Exp e);

	abstract public Exp deleteExp(Exp e);

	abstract public int expCount(int expType);
	static public int THE=0;
	static public int EXISTS=1;

	// destructive: removes any lits with unscoped variables
	// returns true if there are unscoped variables that can't be removed
	abstract public boolean removeUnscoped(List vars);
	public void removeUnscoped(){ removeUnscoped(new LinkedList()); }

	// destructive: removes any lits with unscoped variables
	// returns true if there are unscoped variables that can't be removed
	abstract public void getConstStrings(List<String> result);
	public List<String> getConstStrings(){ 
		List<String> result = new LinkedList<String>(); 
		getConstStrings(result);
		return result;
	}

	public List<Exp> getOuterRefs(){
		LinkedList<Exp> ret = new LinkedList<Exp>();
		getOuterRefs(this,ret);
		return ret;

	}
	abstract void getOuterRefs(Exp e, List<Exp> refs);

	public abstract boolean equals(int type, Exp other);
	static public int NO_VARS=0;
	static public int NO_VARS_CONSTS=1;

	public static int maxNumVars = 2;

	public double score;

	public double avgDepth(){ return avgDepth(0); }
	public abstract double avgDepth(int d);

	public static void main(String[] args){
		PType.addTypesFromFile("../experiments/atis/atis.types");
		Lang.loadLangFromFile("../experiments/atis/atis.lang");
		Exp e1;
		e1 = Exp.makeExp("(lambda $0 e (and (flight $0) (flight $0) (or (day $0 boeing:mf) (day $0 767:ac)) (to $0 iad:ap) (from $0 san_diego:ci)))");
		System.out.println(e1 + " ---- "+e1.inferType());

		e1 = Exp.makeExp("(lambda $0 e (day $0 boeing:mf))");
		System.out.println(e1 + " ---- "+e1.inferType());

		e1 = Exp.makeExp("(lambda $0 e (day $0 767:ac))");
		System.out.println(e1 + " ---- "+e1.inferType());

		e1 = Exp.makeExp("(lambda $0 e (or (day $0 boeing:mf) (day $0 767:ac)))");
		System.out.println(e1 + " ---- "+e1.inferType());

		e1 = Exp.makeExp("(lambda $0 e (and (flight $0) (flight $0) (or (day $0 boeing:mf) (day $0 767:ac)) (to $0 iad:ap) (from $0 san_diego:ci)))");
		System.out.println(e1 + " ---- "+e1.inferType());

		e1 = Exp.makeExp("(lambda $0 e (and (flight $0) (flight $0) (or (day $0 boeing:mf) (day $0 767:ac)) (to $0 iad:ap) (from $0 san_diego:ci)))");
		System.out.println(e1 + " ---- "+e1.inferType());

	}

}
