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

public class Quant extends Exp {

    public Quant(String input, Map vars){
	LispReader lr = new LispReader(new StringReader(input));
	String stype = lr.next(); 
	if (stype.equals("forall"))
	    quant_type = FORALL;
	else if (stype.equals("exists"))
	    quant_type = EXISTS;
	else{ 
	    System.err.println("ERROR: bad type "+stype+" in Quant(String)");
	    System.exit(-1);
	}
	    
	// also quantify over entities
	arg = new Var(PType.E);
	String argname = lr.next();
	vars.put(argname,arg);
	body = Exp.makeExp(lr.next(),vars);
	//body = body.replace(a,arg);
    }

    public Quant(int t, Exp a, Exp b){
	quant_type = t;
	arg = new Var(a.type());
	body = b;
	body = body.replace(a,arg);
    }

    public Exp simplify(List<Var> vars){
	vars.add(arg);

	// try to raise subexpressions that are well scoped
	if (body.type().equals(PType.T)){
	    List<Exp> raisable = new LinkedList<Exp>();
	    body.raisableSubExps(raisable);
	    for (Exp e : raisable){
		if (e!=body && vars.containsAll(e.freeVars())){
		    body.instReplace(e,BoolBoolOps.T);
		    if (!(body instanceof BoolBoolOps)){
			BoolBoolOps b = BoolBoolOps.makeConj(body);
			body = b;
		    }
		    BoolBoolOps b = (BoolBoolOps)body;
		    b.addExp(e);
		}
	    }
	}

	body = body.simplify(vars);
	vars.remove(arg);
	return this;
    }

    public Exp replace(Exp olde, Exp newe){
	//System.out.println("Funct: "+olde+"  :  "+newe);
	//System.out.println("Body: "+body);
	if (equals(olde)) return newe;
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
	if (o instanceof Quant){
	    Quant f = (Quant)o;
	    if (quant_type!=f.quant_type) return false;
	    arg.setEqualTo(f.arg);
	    f.arg.setEqualTo(arg);
	    if (!f.body.equals(body)){
		arg.setEqualTo(null);
		f.arg.setEqualTo(null);
		return false;
	    } else {
		arg.setEqualTo(null);
		f.arg.setEqualTo(null);
		return true;
	    }
	} else return false;
    }

    public boolean equals(int type, Exp o){
	if (o instanceof Quant){
	    Quant f = (Quant)o;
	    if (quant_type!=f.quant_type) return false;
	    arg.setEqualTo(f.arg);
	    f.arg.setEqualTo(arg);
	    if (!f.body.equals(body)){
		arg.setEqualTo(null);
		f.arg.setEqualTo(null);
		return false;
	    } else {
		arg.setEqualTo(null);
		f.arg.setEqualTo(null);
		return true;
	    }
	} else return false;
    }

    public int hashCode(){
	return arg.hashCode()+body.hashCode();
    }

    public Exp copy(){
	Quant q = new Quant(quant_type,arg,body.copy());
	return q;
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
	if (quant_type == FORALL)
	    result =  "(forall "+arg.toString(varNames)+" "
		+body.toString(varNames)+")";
	else
 	    result =  "(exists "+arg.toString(varNames)+" "+
		body.toString(varNames)+")";
	varNames.remove(arg);
	return result;
    }

    // body must be well types and boolean typed
    public boolean wellTyped(){
	arg.setTempType(PType.E);
	if (!body.wellTyped()){
	    arg.setTempType(null);
	    return false;
	}
	arg.setTempType(null);
	return body.type().equals(PType.T);
    }

    public Type type(){
	if (!wellTyped()) return PType.n;
	return PType.T;
    }

    public Type inferType(List<Var> vars, List<List<Type>> varTypes){
	arg.addTypeSig(vars,varTypes);
	Type t=body.inferType(vars,varTypes);
	arg.removeTypeSig(vars,varTypes);
	if (t==null || !t.subType(PType.T)){
	    inferedType=null; // update cache
	    return null;
	}
	inferedType=PType.T; // update cache
	return PType.T;
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

    public void allSubExps(List result){
	result.add(this);
	body.allSubExps(result);
    }

    public void allSubExps(Type type, List result){
	if (type==null) result.add(this);
	Type t = type();
	if (t!=null && t.equals(type)) result.add(this);
	//if (type==null || type().equals(type)) result.add(this);
	if (arg.type().equals(type))
	    result.add(arg);
	if (body.type().equals(type))
	    result.add(body);
	if (FType.ET.equals(type)){
	    Funct f = new Funct(arg,body);
	    f.removeUnscoped();
	    //System.out.println("f: "+f+ " : "+f.wellTyped());
	    result.add(f);
	}
	body.allSubExps(type,result);
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

    public int expCount(int eq, Exp e){
	int count = 0;
	if (equals(eq,e)) count++;
	return count+body.expCount(eq,e);
    }

    public int repeatExpCount(int t, Exp e){
	return body.repeatExpCount(t,e);
    }

    public int expCount(int id){
	if (quant_type==EXISTS && id==Exp.EXISTS)	    
	    return body.expCount(id)+1;
	else
	    return body.expCount(id);
    }

    public boolean removeUnscoped(List vars){
	vars.add(arg);
	body.removeUnscoped(vars);
	vars.remove(arg);
	return false;
    }

    public Exp deleteExp(Exp l){
	return new Quant(quant_type,arg,body.deleteExp(l));
    }

    void getOuterRefs(Exp e, List<Exp> refs){
	// first, add the new function
	if (body instanceof BoolBoolOps){
	    BoolBoolOps b = (BoolBoolOps)body;
	    b.addExp(0,new Appl(Var.ET,arg));
	    refs.add(new Funct(Var.ET,e));
	    b.removeExp(0);
	}

	// now, recurse
	body.getOuterRefs(e,refs);
    }

    public void getConstStrings(List<String> result){
	if (quant_type == FORALL)
	    result.add("forall");
	else
	    result.add("exists");
	body.getConstStrings(result);
    }

    public String getHeadString(){
	if (quant_type == FORALL)
	    return "forall";
	else
	    return "exists";
    }

    public double avgDepth(int d){
	return body.avgDepth(d+1);
    }

    int quant_type;
    Var arg;
    Exp body;

    public static int FORALL = 0;
    public static int EXISTS = 1;

}
