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
public class BoolBoolOps extends Exp {

	private BoolBoolOps(){
		exps = new LinkedList();
	}

	private BoolBoolOps(int type, Set<Exp> entries){
		op_type=type;
		exps = new LinkedList();
		for (Exp e : entries)
			exps.add(e);
	}

	public BoolBoolOps(String input,Map vars){
		exps = new LinkedList();
		LispReader lr = new LispReader(new StringReader(input));
		String t = lr.next();  
		if (t.equals("and"))
			op_type = CONJ;
		else if (t.equals("or"))
			op_type = DISJ;
		else if (t.equals("implies"))
			op_type = IMPL;
		else {
			System.err.println("Unknown BoolBoolOps type "+t);
			System.exit(-1);
		}
		while (lr.hasNext()){
			exps.add(Exp.makeExp(lr.next(),vars));
		}
	}

	static public BoolBoolOps makeConj(Exp one, Exp two){
		BoolBoolOps result = new BoolBoolOps();
		result.op_type = CONJ;
		result.exps.add(one);
		result.exps.add(two);
		return result;
	}

	static public BoolBoolOps makeConj(Exp one){
		BoolBoolOps result = new BoolBoolOps();
		result.op_type = CONJ;
		result.exps.add(one);
		return result;
	}

	static public BoolBoolOps makeDisj(Exp one, Exp two){
		BoolBoolOps result = new BoolBoolOps();
		result.op_type = DISJ;
		result.exps.add(one);
		result.exps.add(two);
		return result;
	}

	static public BoolBoolOps makePair(int type, Exp one, Exp two){
		BoolBoolOps result = new BoolBoolOps();
		result.op_type = type;
		result.exps.add(one);
		result.exps.add(two);
		return result;
	}

	public Exp simplify(List<Var> vars){
		removeTrues();
		for (int i=0; i<exps.size(); i++){
			exps.set(i,((Exp)exps.get(i)).simplify(vars));
		}
		removeDuplicates();
		if (exps.size()==1) return exps.get(0);
		return this;
	}


	public Exp replace(Exp olde, Exp newe){
		//System.out.println("Exps: "+exps);
		Exp e;
		if (equals(olde)) return newe;
		if (olde instanceof BoolBoolOps){
			BoolBoolOps other = (BoolBoolOps)olde;
			if (other.op_type==op_type &&
					other.exps.size()<exps.size() &&
					exps.containsAll(other.exps)){
				exps.removeAll(other.exps);
				exps.add(newe);
			}
		}

		for (int i=0; i<exps.size(); i++){
			e = (Exp)exps.get(i);
			if (e.equals(olde))
				exps.set(i,newe);
			else 
				exps.set(i,e.replace(olde,newe));
			e = (Exp)exps.get(i);
			if (e==null)
				return null;
			// flatten the conjunction of disjunction
			if ((op_type==CONJ || op_type==DISJ)
					&& e instanceof BoolBoolOps){
				BoolBoolOps b = (BoolBoolOps)e;
				if (op_type==b.op_type){
					List<Exp> l = b.exps;
					exps.remove(i);
					for (int j=l.size()-1; j>=0; j--)
						exps.add(i,l.get(j));
				}
			}

		}
		removeTrues();
		//removeDuplicates();
		if (exps.size()==1)
			return (Exp)exps.get(0);
		//return this;
		return raiseDisjDups();
	}

	public Exp instReplace(Exp olde, Exp newe){
		//System.out.println("Exps: "+exps);
		Exp e;
		if (this==olde) return newe;
		if (olde instanceof BoolBoolOps){
			// see if we have a subset of the expressions
			BoolBoolOps other = (BoolBoolOps)olde;
			if (other.op_type==op_type &&
					other.exps.size()<exps.size()){
				boolean foundAll = true;
				for (Exp e1 :  other.exps){
					boolean found=false;
					for (Exp e2 : exps){
						if (e1==e2) {
							found = true;
							break;
						}
					}
					if (!found){
						foundAll=false;
						break;
					}
				}
				if (foundAll){
					exps.removeAll(other.exps);
					exps.add(newe);
				}
			}
		}

		for (int i=0; i<exps.size(); i++){
			e = (Exp)exps.get(i);
			exps.set(i,e.instReplace(olde,newe));
			e = (Exp)exps.get(i);
			if (e==null)
				return null;
			// flatten the conjunction of disjunction
			if ((op_type==CONJ || op_type==DISJ)
					&& e instanceof BoolBoolOps){
				BoolBoolOps b = (BoolBoolOps)e;
				if (op_type==b.op_type){
					List<Exp> l = b.exps;
					exps.remove(i);
					for (int j=l.size()-1; j>=0; j--)
						exps.add(i,l.get(j));
				}
			}
		}
		if (exps.size()==1)
			return (Exp)exps.get(0);
		return this;
	}

	public void removeTrues(){
		if (op_type==CONJ){
			for (int i=0; i<exps.size(); i++){
				if (exps.get(i).equals(T)){
					exps.remove(i);
					i--;
				}
			}
		}
	}

	public void removeDuplicates(){
		for (int i=0; i<exps.size(); i++){
			for (int j=0; j<exps.size(); j++){
				if (i!=j){
					if (exps.get(i).equals(exps.get(j))){
						exps.remove(i);
						i--;
						j = exps.size();
					}
				}
			}
		}
	}

	public void removeDuplicateAppls(){
		for (int i=0; i<exps.size(); i++){
			for (int j=i+1; j<exps.size(); j++){
				Exp e = (Exp)exps.get(i);
				if (e instanceof Appl){
					if (e.equals(exps.get(j))){
						exps.remove(i);
						i--;
						break;
					}
				}
			}
		}
	}

	public BoolBoolOps raiseDisjDups(){
		if (exps.size()==0) return this;
		if (op_type==DISJ){
			for (int i=0; i<exps.size(); i++){
				if (!(exps.get(i) instanceof BoolBoolOps)) return this;		    
				BoolBoolOps b = (BoolBoolOps)exps.get(i);
				if (b.op_type!=CONJ) return this;
			}
			List common = new LinkedList();
			BoolBoolOps one = (BoolBoolOps)exps.get(0);
			for (int i=0; i<one.exps.size(); i++){
				Exp e = (Exp)one.exps.get(i);
				boolean found = true;
				for (int j=1; j<exps.size(); j++){
					BoolBoolOps other = (BoolBoolOps)exps.get(j);
					if (!other.exps.contains(e)){
						found = false;
					}
				}
				if (found){
					common.add(e);
					for (int j=0; j<exps.size(); j++){
						BoolBoolOps b = (BoolBoolOps)exps.get(j);
						b.exps.remove(e);
					}
				}
			}
			if (common.size()>0){
				for (int j=0; j<exps.size(); j++){
					BoolBoolOps b = (BoolBoolOps)exps.get(j);
					if (b.exps.size()==1){
						exps.set(j,b.exps.get(0));
					}
				}
				BoolBoolOps result = new BoolBoolOps();
				result.op_type = CONJ;
				result.exps.add(this);
				result.exps.addAll(common);
				return result;
			}
		}
		return this;
	}

	public boolean hasFunctClash(){
		if (op_type != CONJ) return false;
		for (int i=0; i<exps.size(); i++){
			for (int j=0; j<exps.size(); j++){
				if (i!=j){
					Exp ei = (Exp)exps.get(i);
					Exp ej = (Exp)exps.get(j);
					if (ei instanceof Lit && ej instanceof Lit){
						if (((Lit)ei).functClash((Lit)ej))
							return true;
					}
				}
			}
		}
		return false;
	}

	static Exp T = Exp.makeExp("true:t");

	public Exp copy(){
		BoolBoolOps result = new BoolBoolOps();
		result.op_type = op_type;
		Iterator i = exps.iterator();
		while (i.hasNext()){
			result.exps.add(((Exp)i.next()).copy());
		}
		return result;
	}

	public double varPenalty(List varNames){
		double result = 0.0;
		Iterator i = exps.iterator();
		while (i.hasNext()){
			result+=((Exp)i.next()).varPenalty(varNames);
		}
		return result;
	}

	public String toString(List varNames){
		StringBuffer result = new StringBuffer();
		if (op_type==CONJ)
			result.append("(and");
		else if (op_type==DISJ)
			result.append("(or");
		else if (op_type==IMPL)
			result.append("(implies");
		Iterator i = exps.iterator();
		while (i.hasNext()){
			result.append(" ").append(((Exp)i.next()).toString(varNames));
		}
		result.append(")");
		return result.toString();
	}

	public int hashCode(){
		int ret = (int)op_type;
		Iterator i = exps.iterator();
		while (i.hasNext()){
			ret+=i.next().hashCode();
		}
		return ret;
	}



	public boolean equals(Object o){
		if (o instanceof BoolBoolOps){
			BoolBoolOps c = (BoolBoolOps)o;
			return op_type==c.op_type 
			&& exps.size()==c.exps.size()
			&& exps.containsAll(c.exps) 
			&& c.exps.containsAll(exps);
		}
		return false;
	}

	public boolean equals(int type, Exp o){
		if (o instanceof BoolBoolOps){
			BoolBoolOps c = (BoolBoolOps)o;
			return op_type==c.op_type 
			&& exps.size()==c.exps.size()
			&& exps.containsAll(c.exps) 
			&& c.exps.containsAll(exps);
		}
		return false;
	}

	public Type type(){
		return PType.T;
	}

	public Type inferType(List<Var> vars, List<List<Type>> varTypes){
		if (op_type==DISJ) return PType.T;
		for (Exp e : exps){
			Type t=e.inferType(vars,varTypes);
			if (t==null || !t.subType(PType.T)){
				inferedType=null; // update cache
				return null;
			}
		}
		inferedType=PType.T; // update cache
		return PType.T; // could we infer subtypes here?
	}

	public boolean wellTyped(){
		Iterator i = exps.iterator();
		Exp e;
		while (i.hasNext()){
			e = (Exp)i.next();
			if (e==null || !e.wellTyped()){
				return false;
			}
			if (!PType.T.equals(e.type())){
				return false;
			}
		}
		return true;
	}

	public void freeVars(List bound, List free){
		Iterator i = exps.iterator();
		while (i.hasNext()){
			((Exp)i.next()).freeVars(bound,free);
		}
	}

	public void extractFuncts(List functors, List functees, Exp orig){
		for (int i=0; i<exps.size(); i++){
			Exp e = ((Exp)exps.get(i));
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
		Iterator i = exps.iterator();
		double result = 0.0;
		while (i.hasNext()){
			result+=((Exp)i.next()).complexity();
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
		Iterator i = exps.iterator();
		while (i.hasNext()){
			e = (Exp)i.next();
			e.extractPTypeExps(l);
		}
	}

	public void allPreds(int arity, List result){
		Exp e;
		Iterator i = exps.iterator();
		while (i.hasNext()){
			e = (Exp)i.next();
			e.allPreds(arity,result);
		}
	}

	public void allLits(int arity, List result, boolean b){
		Exp e;
		Iterator i = exps.iterator();
		while (i.hasNext()){
			e = (Exp)i.next();
			e.allLits(arity,result,b);
		}
	}

	public void allSubExps(String type, List result){
		Exp e;
		Iterator i = exps.iterator();
		while (i.hasNext()){
			e = (Exp)i.next();
			if (e.getClass().getName().equals(type))
				result.add(e);	    
			e.allSubExps(type,result);
		}
	}

	public void allSubExps(Type type, List result){
		if (type==null || type().equals(type)){	 
			result.add(this);
			Set<Exp> entries = new HashSet<Exp>();
			entries.addAll(exps);
			Collection<Set<Exp>> subsets = new PowerSet(entries);
			for (Set<Exp> subset : subsets) {
				if (subset.size()>1 
						&& subset.size()<exps.size()
						&& subset.size()<3){
					result.add(new BoolBoolOps(op_type,subset));
				}
			}
		}
		Exp e;
		Iterator i = exps.iterator();
		while (i.hasNext()){
			e = (Exp)i.next();
			e.allSubExps(type,result);
		}
	}

	public void allSubExps(List result){
		result.add(this);
		Set<Exp> entries = new LinkedHashSet<Exp>();
		entries.addAll(exps);
		Collection<Set<Exp>> subsets = new PowerSet(entries);
		for (Set<Exp> subset : subsets) {
			if (subset.size()>1 
					&& subset.size()<exps.size()
					&& subset.size()<3){
				result.add(new BoolBoolOps(op_type,subset));
			}
		}
		Exp e;
		Iterator i = exps.iterator();
		while (i.hasNext()){
			e = (Exp)i.next();
			e.allSubExps(result);
		}
	}

	public void raisableSubExps(List<Exp> result){
		if (op_type!=CONJ) return;
		for (Exp e : exps){
			result.add(e);
			e.raisableSubExps(result);
		}
	}

	public int predCount(Object p){
		int result = 0;
		Exp e;
		Iterator i = exps.iterator();
		while (i.hasNext()){
			e = (Exp)i.next();
			result+=e.predCount(p);
		}
		return result;
	}

	public int repeatPredCount(int t, Object p){
		int result = 0;                                                                      
		Exp e;       
		Iterator i;

		i = exps.iterator();
		while (i.hasNext()){
			e = (Exp)i.next();
			result+=e.repeatPredCount(t,p);
		}

		i = exps.iterator();
		int count=0;
		while (i.hasNext()){
			e = (Exp)i.next();
			if (e.predCount(p)>0){
				count++;
			}
		}
		if (t==ConjCountFeatSet.DISJ_REPEAT && op_type==DISJ
				&& count==exps.size()){
			result+=count;
		} 
		if (t==ConjCountFeatSet.DISJ_ONCE && op_type==DISJ
				&& count>0 && count<exps.size()){
			result+=count;
		} 
		if (t==ConjCountFeatSet.CONJ_REPEAT && op_type==CONJ
				&& count>1){
			result+=count;
		}
		return result; 
	}

	public int expCount(int eq, Exp e){
		int count = 0;
		if (equals(eq,e)) count++;
		Iterator i = exps.iterator();
		while (i.hasNext()){
			Exp ex = (Exp)i.next();
			count+=ex.expCount(eq,e);
		}
		return count;
	}

	public int repeatExpCount(int t, Exp e){
		int result = 0;
		Exp ex;       
		Iterator i;

		i = exps.iterator();
		while (i.hasNext()){
			ex = (Exp)i.next();
			result+=ex.repeatExpCount(t,e);
		}

		i = exps.iterator();
		int count=0;
		while (i.hasNext()){
			ex = (Exp)i.next();
			if (ex.expCount(NO_VARS,e)>0){
				count++;
			}
		}
		if (t==ConjCountFeatSet.DISJ_REPEAT && op_type==DISJ
				&& count==exps.size()){
			result+=count;
		} 
		if (t==ConjCountFeatSet.DISJ_ONCE && op_type==DISJ
				&& count>0 && count<exps.size()){
			result+=count;
		} 
		if (t==ConjCountFeatSet.CONJ_REPEAT && op_type==CONJ
				&& count>1){
			result+=count;
		}

		return result;
	}

	public int expCount(int id){
		int result = 0;
		Exp ex;       
		Iterator i;

		i = exps.iterator();
		while (i.hasNext()){
			ex = (Exp)i.next();
			result+=ex.expCount(id);
		}
		return result;
	}


	public boolean removeUnscoped(List vars){
		Iterator i = exps.iterator();
		while (i.hasNext()){
			Exp e = (Exp)i.next();
			if (e.removeUnscoped(vars))
				i.remove();
		}
		return false;
	}

	public Exp deleteExp(Exp l){
		BoolBoolOps result = new BoolBoolOps();
		result.op_type = op_type;
		Iterator i = exps.iterator();
		while (i.hasNext()){
			Exp e = (Exp)i.next();
			if (e!=l)
				result.exps.add(e.deleteExp(l));
		}
		return result;
	}

	void getOuterRefs(Exp e, List<Exp> refs){	
		// now, recurse
		Iterator i = exps.iterator();
		while (i.hasNext()){
			Exp ex = (Exp)i.next();
			ex.getOuterRefs(e,refs);
		}
	}

	public void addExp(int i, Exp e){
		exps.add(i,e);
	}

	public void addExp(Exp e){
		exps.add(e);
	}

	public Exp removeExp(int i){
		return (Exp)exps.remove(i);
	}

	public void getConstStrings(List<String> result){
		if (op_type==CONJ)
			result.add("and");
		else if (op_type==DISJ)
			result.add("or");
		else if (op_type==IMPL)
			result.add("implies");
		for (Exp e : exps)
			e.getConstStrings(result);
	}

	public String getHeadString(){
		if (op_type==CONJ)
			return "and";
		else if (op_type==DISJ)
			return "or";
		else 
			return "implies";
	}

	public List<String> getHeadPairs(){
		List<String> result = new LinkedList<String>();
		for (int i=0; i<exps.size(); i++){
			String one = exps.get(i).getHeadString();
			for (int j=i+1; j<exps.size(); j++){
				String two = exps.get(j).getHeadString();
				if (one.compareTo(two)<0)
					result.add(one+":"+two);
				else
					result.add(two+":"+one);
			}
		}
		return result;
	}

	public double avgDepth(int d){
		double total = 0.0;
		for (Exp e : exps)
			total+=e.avgDepth(d+1);
		return total/exps.size();
	}

	List<Exp> exps;
	int op_type;

	public static int CONJ = 0;
	public static int DISJ = 1;
	public static int IMPL = 2;


}
