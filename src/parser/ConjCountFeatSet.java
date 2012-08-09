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



package parser;

import java.util.*;
import lambda.*;

public class ConjCountFeatSet {// implements ParseFeatureSet {

    public ConjCountFeatSet(int offset){
	Var v = new Var(PType.E);
	index=offset;	
	List preds = Lang.allPreds();
	exps = new LinkedList<Exp>();
	for (Object p1 : preds){
	    Pred p = (Pred)p1;
	    if (p.type().matches(PType.I) &&
		!p.type().equals(PType.E)){
		//System.out.println("p: "+p);
		Lit l = new Lit(p,v);
		IntBoolOps i = new IntBoolOps(IntBoolOps.LESS_THAN,l,v);
		exps.add(i);
		i = new IntBoolOps(IntBoolOps.GREATER_THAN,l,v);
		exps.add(i);
	    } else {
		Lit l = new Lit(p,p.arity());
		for (int i=0; i<p.arity(); i++)
		    l.setArg(i,v);
		exps.add(l);
	    }
	}
    }

    public int indexOf(List<Exp> l, Exp e){
	int count =0;
	for (Exp e2 : l){
	    if (e2.equals(Exp.NO_VARS,e)){
		return count;
	    }
	    count++;
	}
	return -1;
    }

    public int numFeats(){
	return exps.size()*3;
    }

    public void setFeats(Cell c, List children, PVector feats){
	if (children.size()!=2) return;
	if (!((Cell)children.get(0)).isCoor() && 
	    !((Cell)children.get(1)).isCoor()) return;
	Exp e= c.getCat().getSem();
	double score = 0.0;
	for (Exp exp : e.allSubExps()){
	    int i=indexOf(exps,exp);
	    if (i==-1) continue;
	    if (e!=null && e.repeatExpCount(CONJ_REPEAT,exp)>0)
		feats.set(3*i,1.0);
	    if (e!=null && e.repeatExpCount(DISJ_ONCE,exp)>0)
		feats.set(3*i+1,1.0);
	    if (e!=null && e.repeatExpCount(DISJ_REPEAT,exp)>0)
		feats.set(3*i+2,-1.0);
	    i++;
	}
    }

    public double score(Cell c, List children, PVector theta){
	if (children.size()!=2) return 0;
	if (!((Cell)children.get(0)).isCoor() && 
	    !((Cell)children.get(1)).isCoor()) return 0;
	Exp e= c.getCat().getSem();
	double score = 0.0;
	for (Exp exp : e.allSubExps()){
	    int i=indexOf(exps,exp);
	    if (i==-1) continue;
	    if (e!=null && e.repeatExpCount(CONJ_REPEAT,exp)>0)
		score+=1.0*theta.get(3*i);
	    if (e!=null && e.repeatExpCount(DISJ_ONCE,exp)>0)
		score+=1.0*theta.get(3*i+1);
	    if (e!=null && e.repeatExpCount(DISJ_REPEAT,exp)>0)
		score+=-1.0*theta.get(3*i+2);
	    i++;
	}
	return score;

    }

    static public int CONJ_REPEAT=0;
    static public int DISJ_REPEAT=1;
    static public int DISJ_ONCE=2;
    
    int index;
    List<Exp> exps;

}
