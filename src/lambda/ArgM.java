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


public class ArgM extends Exp {

	public ArgM(String input, Map vars){
		LispReader lr = new LispReader(new StringReader(input));
		String stype = lr.next(); 
		if (stype.equals("argmin")){
			mtype = MIN;
		} else if (stype.equals("argmax")){
			mtype = MAX;
		} else
			System.err.println("Bad ArgM min/max type "+stype);
		// always count entities
		arg = new Var(PType.E);
		String argname = lr.next();
		vars.put(argname,arg);
		set = Exp.makeExp(lr.next(),vars);
		body = Exp.makeExp(lr.next(),vars);
		vars.remove(argname);
	}

	public ArgM(int m, Exp a, Exp b, Exp c){
		mtype = m;
		arg = new Var(PType.E);
		set = b;
		body = c;
		body = body.replace(a,arg);
		set = set.replace(a,arg);
	}

	public Exp simplify(List<Var> vars){
		vars.add(arg);
		// try to raise subexpressions that are well scoped
		if (set.type().equals(PType.T)){
			List<Exp> raisable = new LinkedList<Exp>();
			set.raisableSubExps(raisable);
			for (Exp e : raisable){
				if (e!=set && vars.containsAll(e.freeVars())){
					set.instReplace(e,BoolBoolOps.T);
					if (!(set instanceof BoolBoolOps)){
						BoolBoolOps b = BoolBoolOps.makeConj(set);
						set = b;
					}
					BoolBoolOps b = (BoolBoolOps)set;
					b.addExp(e);
				}
			}
		}

		body=body.simplify(vars);
		set=set.simplify(vars);
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
		if (this==olde) return newe;
		if (arg==olde)
			arg = (Var)newe;
		body = body.instReplace(olde,newe);
		set = set.instReplace(olde,newe);
		//System.out.println("after: "+this);
		if (set==null || body==null)
			return null;
		if (body instanceof Var) return null;
		return this;
	}

	public boolean equals(Object o){
		if (o instanceof ArgM){
			ArgM c = (ArgM)o;
			if (mtype!=c.mtype) return false;
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
		if (o instanceof ArgM){
			ArgM c = (ArgM)o;
			if (mtype!=c.mtype) return false;
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
		ArgM c = new ArgM(mtype,arg.copy(),set.copy(),body.copy());
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
		String result="";
		if (mtype==MIN){
			result="(argmin ";
		} else {
			result="(argmax ";
		}
		result += arg.toString(varNames)+" "
		+set.toString(varNames)+" "
		+body.toString(varNames)+")";
		varNames.remove(arg);
		return result;
	}


	public Type type(){
		// figure out the type of the variable
		if (!wellTyped()) return PType.n;
		//return computeType();
		return PType.E;
	}

	public Type inferType(List<Var> vars, List<List<Type>> varTypes){
		arg.addTypeSig(vars,varTypes);
		Type t=body.inferType(vars,varTypes);
		if (t==null || !t.subType(PType.I)){// hack because <e,e> matches <e,i>, should fix
			if (!(body instanceof Appl)){ 
				arg.removeTypeSig(vars,varTypes);
				inferedType=null; // update cache
				return null;
			}
		}
		t=set.inferType(vars,varTypes);
		if (t==null || !t.subType(PType.T)){
			arg.removeTypeSig(vars,varTypes);
			inferedType=null; // update cache
			return null;
		}
		// get return type for vairable
		int i = vars.indexOf(arg);
		if (i==-1){
			arg.removeTypeSig(vars,varTypes);
			inferedType=PType.E; // update cache
			return PType.E;
		}
		Type rType = null;
		for (List<Type> tuple : varTypes){
			if (rType==null)
				rType = tuple.get(i);
			else
				rType = rType.commonSuperType(tuple.get(i));
		}
		arg.removeTypeSig(vars,varTypes);
		inferedType=rType; // update cache
		arg.inferedType=rType; // update cache
		return rType;
	}

	/*
    private Type computeType(){
	arg.setTempType(PType.E);
	Type t=null;
	if (set.wellTyped() && body.wellTyped()){
	    //System.out.println("HERE!! "+set.type()+ " -- "+body.type());
	    if (PType.T.matches(set.type())){
		//System.out.println("H1");
		System.out.println(body.type());
		if (body.type().matches(PType.I)){
		    t = arg.getTempType();
		    //System.out.println("H2 "+t);
		}
		if (body instanceof Appl){
		    t = arg.getTempType();
		    //System.out.println("H2 "+t);		    
		}
	    }
	}
	if (body instanceof Var){
	    //System.out.println("h4");
	    t=null;
	}
	//System.out.println("h5 "+t);
	arg.setTempType(null);
	return t;
    }
	 */
	public boolean wellTyped(){
		/*
	arg.setTempType(PType.E);
	if (!set.wellTyped() || !body.wellTyped()){
	    arg.setTempType(null);
	    return false;
	}
		 */
		//System.out.println("*"+arg.getTempType());
		if (body instanceof Var){
			//arg.setTempType(null);
			return false;
		}
		if (body instanceof Appl){
			//arg.setTempType(null);
			return true;
		}
		//arg.setTempType(null);	

		return PType.T.matches(set.type()) &&
		//PType.I.matches(body.type());	   
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
		if (body.getClass().getName().equals(type))
			result.add(body);
		if (set.getClass().getName().equals(type))
			result.add(set);
		body.allSubExps(type,result);
		set.allSubExps(type,result);
	}

	public void allSubExps(Type type, List result){
		if (type==null) result.add(this);
		Type t = type();
		if (t!=null && t.equals(type)) result.add(this);
		//if (type==null || type().equals(type))
			//    result.add(this);
		//if (body.type().equals(type))
		//    result.add(body);
		//if (set.type().equals(type))
		//    result.add(set);
		if (type!=null && FType.ET.equals(type)){
			result.add(new Funct(arg,set));
		}
		body.allSubExps(type,result);
		set.allSubExps(type,result);
	}

	public void allSubExps(List result){
		result.add(this);
		body.allSubExps(result);
		set.allSubExps(result);
	}

	public void raisableSubExps(List<Exp> result){
	}

	public int predCount(Object p){
		return body.predCount(p)+set.predCount(p);
	}

	public int repeatPredCount(int t, Object p){
		return body.repeatPredCount(t,p)+set.repeatPredCount(t,p);
	}

	public int expCount(int id){
		return body.expCount(id)+set.expCount(id);
	}

	public int expCount(int eq, Exp e){
		int count = 0;
		if (equals(eq,e)) count++;
		return count+body.expCount(eq,e)+set.expCount(eq,e);
	}

	public int repeatExpCount(int t, Exp e){
		return body.repeatExpCount(t,e)+set.repeatExpCount(t,e);
	}

	public boolean removeUnscoped(List vars){
		vars.add(arg);
		body.removeUnscoped(vars);
		vars.remove(arg);
		return false;
	}

	public Exp deleteExp(Exp l){
		return new ArgM(mtype,arg,set.deleteExp(l),body.deleteExp(l));
	}

	void getOuterRefs(Exp e, List<Exp> refs){
		// first, add the new function
		if (set instanceof BoolBoolOps){
			BoolBoolOps b = (BoolBoolOps)set;
			b.addExp(0,new Appl(Var.ET,arg));
			refs.add(new Funct(Var.ET,e));
			b.removeExp(0);
		}

		// now, recurse
		body.getOuterRefs(e,refs);
		set.getOuterRefs(e,refs);
	}

	public void getConstStrings(List<String> result){
		if (mtype==MIN)
			result.add("argmin");
		if (mtype==MAX)
			result.add("argmax");
		set.getConstStrings(result);
		body.getConstStrings(result);
	}

	public String getHeadString(){
		if (mtype==MIN)
			return "argmin";
		else
			return "argmax";
	}

	public double avgDepth(int d){
		return (set.avgDepth(d+1)+body.avgDepth(d+1))/2.0;
	}

	public static int MAX=0;
	public static int MIN=1;

	int mtype; // min or max

	Var arg;
	Exp set;
	Exp body;
}
