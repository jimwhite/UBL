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

public class Funct extends Exp {

    public Funct(String input, Map vars){
	LispReader lr = new LispReader(new StringReader(input));
	lr.next(); // read 'lambda';
	String argname = lr.next();
	Type t = Type.makeType(lr.next()); // read type
	
	arg = new Var(t);
	vars.put(argname,arg);
	body = Exp.makeExp(lr.next(),vars);
	//body = body.replace(a,arg);
	/*
	if (!wellTyped()){
	    System.out.println("MISTYPED 6: "+this);
	}
	*/
    }

    public Funct(Var a, Exp b){
		arg = new Var(a.type());
		body = b.copy();
		if (!body.freeVars().contains(a)){
			//System.out.println("ERROR var "+a+" is not used in expression "+b);
			//System.out.println(a.equals(b));
			//System.exit(-1);
		}
		//System.out.println("Funct constr: "+a+" : "+arg+" : "+body);
		body = body.replace(a,arg);
    }

    static public Funct makeFunct(List<Var> vars, Exp b){
	if (vars==null || b==null) return null;
	Funct f = new Funct(vars.get(vars.size()-1),b);
	for (int i=vars.size()-2; i>=0; i--){
	    f = new Funct(vars.get(i),f);
	}
	return f;
    }

    public Funct(Exp a, Type t, Exp b){
	arg = new Var(t);
	body = b;
	body = body.replace(a,arg);
    }

    public Exp apply(Exp input){
	// should copy first...
	//arg.setTempType(arg.type());
	//System.out.println("Funct apply: "+this+" to "+input+" of type "+input.type());
	// check type
	if (input==null || !arg.type().matches(input.type())){
	    //System.out.println("TYPE MISMATCH!!");
	    return null;
	}

	Exp result = body.copy();

	//System.out.println("Funct replace: "+result+" : "+arg+" : "+input);
	result =  result.replace(arg,input);
	//if (result!=null){// && !result.wellTyped()){
	//    System.out.println("MISTYPED: "+result);
	//    return null;
	//}
	//System.out.println("Funct apply result "+result+"\n");
	return result;
    }

    public Exp comp(Exp g){
	if (!(g.type() instanceof FType)){
	    //System.out.println("NULL 1!!!");
	    return null;
	}
	// function composition
	//System.out.println("comp: "+this+ " and "+c);
	FType first = (FType)this.type();
	//System.out.println("first: "+first);
	FType second = (FType)g.type();
	if (first==null || second==null) return null;
	if (second.range()==null || second.domain()==null) return null;
	if (!second.range().matches(first.domain())) return null;
	//System.out.println("second: "+second);
	// (lambda x (f (g x))
	// make a new variable x
	Var x = new Var(second.domain());

	Funct gf = (Funct)g;
	Exp newbody = gf.body.copy().replace(gf.arg,x);
	if (newbody==null) return null;
	Exp old = newbody;
	newbody = body.copy().replace(arg,newbody);
	/*
	Exp e = g.apply(x);
	System.out.println("e: "+e);
	Exp newbody = apply(e);
	System.out.println("newbody: "+newbody);
	*/
	if (newbody==null){
	    //System.out.println("newbody: "+g.apply(x));
	    //System.out.println("newbody: "+x.type());
	    //System.out.println("NULL 2!!!");
	    return null;
	}
	return new Funct(x,newbody);
    }

    public Exp replace(Exp olde, Exp newe){
	//System.out.println("Funct: "+this+" : "+olde+"-->"+newe);

	if (newe==null)
	    return null;
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
	if (newe==null)
	    return null;
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
	if (o instanceof Funct){
	    Funct f = (Funct)o;
	    if (!f.arg.type().equals(arg.type())) return false;
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
	if (o instanceof Funct){
	    Funct f = (Funct)o;
	    if (!f.arg.type().equals(arg.type())) return false;
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

    // clearly this function has one argument; however, we also
    // want to count arguments of funtion contained in this one
    public int numArgs(){
	if (body instanceof Funct){
	    return ((Funct)body).numArgs()+1;
	} else return 1;
    }

    public Exp copy(){
	return new Funct(arg,body); // constructor does the copying
    }

    public double varPenalty(List varNames){
	varNames.add(arg);
	double result = body.varPenalty(varNames);
	varNames.remove(arg);
	return result;
    }

    public String toString(List varNames){
	varNames.add(arg);
	String result = "(lambda ";
	result+=arg.toString(varNames) +" ";
	if (body==null) System.err.println("body==null");
	result+=arg.type()+" "+body.toString(varNames)+")";
	varNames.remove(arg);
	return result;
    }


    public boolean wellTyped(){
	arg.setTempType(PType.E);
	if (!body.wellTyped()){
	    arg.setTempType(null);
	    return false;
	}
	arg.setTempType(null);
	return true;
    }

    public Type specInputType(){
	arg.setTempType(PType.E);
	if (!body.wellTyped()){
	    arg.setTempType(null);
	    return null;
	}
	arg.setTempType(null);
	return arg.getTempType();
    }

    public Type type(){
	return new FType(arg.type(),body.type());
    }

    public Type inferType(List<Var> vars, List<List<Type>> varTypes){
	arg.addTypeSig(vars,varTypes);
	Type at = arg.inferType(vars,varTypes);
	Type rt = body.inferType(vars,varTypes);

	if (at.subType(PType.E)){
	    // get return type for variable
	    int i = vars.indexOf(arg);
	    if (i==-1){
		arg.removeTypeSig(vars,varTypes);
		inferedType=new FType(at,rt); // update cache
		return inferedType;
	    }
	    Type aType = null;
	    for (List<Type> tuple : varTypes){
		if (aType==null)
		    aType = tuple.get(i);
		else
		    aType = aType.commonSuperType(tuple.get(i));
	    }
	    arg.removeTypeSig(vars,varTypes);
	    if (aType==null || rt==null){
		inferedType=null; // update cache
		return null;
	    }
	    arg.inferedType = aType; // update cache, mismatch is intentional...
	}
	inferedType=new FType(at,rt); // update cache
	return inferedType;
    }

    public Var getArg(){
	return arg;
    }

    public Exp getBody(){
	return body;
    }

    public Exp getNonFunctBody(){
	if (body instanceof Funct){
	    return ((Funct)body).getNonFunctBody();
	} else return body;
    }

    public void freeVars(List bound, List free){
	bound.add(arg);
	body.freeVars(bound,free);
	bound.remove(arg);
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

	body=body.simplify(vars);
	
	vars.remove(arg);
	
	return this;

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
	if (e instanceof Funct){
	    Funct f = (Funct)e;
	    if (!f.arg.type().equals(arg.type())) 
		return null;
	    arg.setEqualTo(f.arg);
	    f.arg.setEqualTo(arg);
	    List l = body.merge(f.body,top);
	    arg.setEqualTo(null);
	    f.arg.setEqualTo(null);
	    if (l!=null) return l;
	    if (body.type().equals(f.body.type())){
		List result = new LinkedList();
		// make the new functor
		Var v1 = new Var(new FType(arg.type(),body.type()));
		Var v2 = new Var(arg.type());
		Exp temp = body;
		body = new Appl(v1,v2);
		Exp functor = new Funct(v1,new Funct(v2,body.copy()));
		body = temp;
		result.add(functor);
		result.add(new Funct(arg,body.copy()));
		result.add(new Funct(f.arg,f.body.copy()));
		return result;
	    }
	} 
	return null;
    }

    public List merge(List exps, Exp top){
	return null;
    }

    public void extractPTypeExps(List l){
	body.extractPTypeExps(l);
    }

    // try to create a new funct f' and primitive type
    // exp e' such that f' equals this and f'(e') 
    // equals e
    public Exp extractFunctee(Exp e){
	if (!body.type().equals(e.type())) 
	    return null;
	List l = e.extractPTypeExps();
	Iterator i = l.iterator();
	Exp etemp,ecopy;
	while (i.hasNext()){
	    etemp = (Exp)i.next();
	    if (etemp.type().equals(arg.type())){
		ecopy = e.copy();
		ecopy.replace(etemp,arg);
		if (ecopy.equals(body))
		    return etemp;
	    }
	}
	return null;
    }

    /* 
     * returns true if this function doesn't depend on its input variable
     */
    public boolean isConstFunct(){
	return body.predCount(arg)==0;
    }

    public void allPreds(int arity, List result){
	body.allPreds(arity,result);
    }

    public void allLits(int arity, List result,boolean b){
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
	//if (type==null || type().matches(type))
	//    result.add(this);
	if (type!=null && arg.type().equals(type))
	    result.add(arg);
	if (type!=null && body.type().equals(type))
	    result.add(body);
	body.allSubExps(type,result);
    }

    public void allSubExps(List result){
	//    result.add(this);
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

    public int expCount(int eq, Exp e){
	int count = 0;
	if (equals(eq,e)) count++;
	return count+body.expCount(eq,e);
    }

    public int repeatExpCount(int t, Exp e){
	return body.repeatExpCount(t,e);
    }

    public int expCount(int id){
	return body.expCount(id);
    }

    public boolean removeUnscoped(List vars){
	vars.add(arg);
	body.removeUnscoped(vars);
	vars.remove(arg);
	return false;
    }

    public Exp deleteExp(Exp l){
	return new Funct(arg,body.deleteExp(l));
    }

    void getOuterRefs(Exp e, List<Exp> refs){
	body.getOuterRefs(e,refs);
    }

    public void getConstStrings(List<String> result){
	body.getConstStrings(result);
    }

    public String getHeadString(){
	return body.getHeadString();
    }

    public double avgDepth(int d){
	return body.avgDepth(d+1);
    }

    Var arg;
    Exp body;

}
