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

import lambda.*;
import utils.*;
import java.util.*;

/** A CCG Category has both a syntactic and semantic component.
 * 	Each instance of this class stores both. 
 */
public abstract class Cat {

	//< makeCat(String) static string constructor.  returns a 
	//  CompCat or SimpCat depending on the input string
	static public Cat makeCat(String cat){
		String c = cat.trim();
		
		int colon = c.indexOf(':'); 
		
		//< everything after the colon is semantics
		Exp e = null;
		if (colon != -1){
			e = Exp.makeExp(c.substring(colon+1,c.length()));
			c = c.substring(0,colon);
		} 
		//>
		
		// CompCats have slashes, SimpCats don't
		if (c.indexOf('\\') != -1 ||
				c.indexOf('/')  != -1 ||
				c.indexOf('|')  != -1){
			Cat cc =  new CompCat(c);
			cc.setSem(e);
			cc.setConj(false);
			cc.setDisj(false);
			cc.setComplete(true);
			return cc;
		} else {
			Cat sc =  new SimpCat(c);
			sc.setSem(e);
			sc.setConj(false);
			sc.setDisj(false);	 
			sc.setComplete(true);   
			return sc;
		}
	}
	//>

	abstract public int numSlashes();

	//< accessor functions for state variables used during parsing
	public boolean isConj(){
		return conj;
	}

	public void setConj(boolean value){
		conj = value;
	}

	public boolean isDisj(){
		return disj;
	}

	public void setDisj(boolean value){
		disj = value;
	}

	public boolean isComplete(){
		return complete;
	}

	public void setComplete(boolean value){
		complete = value;
	}
	//>



	//< Functions for getting and setting the semantics associated
	//  with this Cat.  Each cat should only have one semantics at the
	//  root, even though CompCats have sub Cats on the left and right.
	//  The sub Cats should have null semantics.
	public void setSem(Exp e){
		sem=e;
	}

	public Exp getSem(){
		return sem;
	}
	//>

	//< Abstract functions to be implemented by sub classes
	abstract public Cat copy();


	//< tests for equality of syntax without checking
	// if the lambda expressions are equals
	// used primarily during parsing
	abstract public boolean equalsNoSem(Object o);
	//>
	
	// does just the syntactic component match?
	abstract public boolean matchesNoSem(Cat c);

	// does the full thing match?
	abstract public boolean matches(Cat c);

	
	public int hashCode(){
		if (sem==null)
			return 0;
		return syntaxHash()+sem.hashCode();
	}

	abstract public int syntaxHash();


	/** work out all splits of the syntax that are consistent with
	 *  a split of the semantics in such a way that e becomes the 
	 *  functee.
	 */
	static private List<List<Cat>> allSplits(Cat cat, Exp e, List<Var> vars){

		Exp exp = cat.getSem();
		List<List<Cat>> splits = new LinkedList<List<Cat>>();
		Exp functee = null;
		if (vars.size()>0){
			functee = Funct.makeFunct(vars,e);
		} else {
			functee = e.copy();
		}

		List<Cat> allAppCats = null;
		
		// find all cats that are consistent with the type of 
		// functee. (will only be one with the current grammar).
		allAppCats = functee.type().makeAllCats();

		Funct functor = null;
		Var arg = null;
		Exp emb=null;
		Exp body=null;
		Cat range = null;
		Cat domain = null;
		Cat compcatee = null;
		CompCat cator = null;

		List<Cat> temp = new LinkedList<Cat>();

		for (Cat catee : allAppCats){
			functor = null;
			// first, make the argument variable and the new embedded expression 
			arg = null;
			emb=null;
			if (vars.size()>0){
				arg = new Var(Type.makeType(vars,e.type().makeSuper()));
				emb = new Appl(arg,vars);
			} else {
				arg = new Var(functee.type().makeSuper());
				emb=arg;
			}

			// replace e in exp with emb
			body = exp.instReplace(e,emb);
			functor = new Funct(arg,body);
			exp = exp.instReplace(emb,e);

			catee.setSem(functee);
			range = cat.copy();
			range.setSem(null);
			domain = catee.copy();
			domain.setSem(null);
			
			//< fwd application
			temp = new LinkedList<Cat>();
			cator = new CompCat('/',range,domain);
			cator.setSem(functor);
			temp.add(cator);
			temp.add(catee);

			splits.add(temp);

			if (cator.apply(catee)==null){
				System.out.println("ERROR 1: null in Cat split");
				System.out.println("cat: "+cat);
				System.out.println("functee: "+catee);
				System.out.println("functor: "+cator);
			}
			
			//>
			
			//< back application
			temp = new LinkedList<Cat>();
			cator = new CompCat('\\',range.copy(),domain.copy());
			cator.setSem(functor);
			temp.add(catee);
			temp.add(cator);
			splits.add(temp);

			if (cator.apply(catee)==null){
				System.out.println("ERROR 2: null in Cat split");
				System.out.println("cat: "+cat);
				System.out.println("functee: "+catee);
				System.out.println("functor: "+cator);
			}
			//>

		}
		
		
		// Function Composition.
		// Can only do this if the slashes and lambda terms allow.
		if (exp instanceof Funct &&
				cat instanceof CompCat &&
				((CompCat)cat).getSlash()!='|' &&
				vars.size()>0){	

			Var v = ((Funct)exp).getArg();
			if (vars.get(0).equals(v)){
				CompCat cc = (CompCat)cat;
				List<Var> newVars = new LinkedList<Var>();
				newVars.addAll(vars);
				Funct compfunctee = Funct.makeFunct(newVars,e);
				newVars.remove(v);
				Var comparg = new Var(Type.makeType(newVars,e.type().makeSuper()));
				if (newVars.size()==0)
					emb = comparg;
				else 
					emb = new Appl(comparg,newVars);
				Exp expf = ((Funct)exp).getBody();
				body = expf.instReplace(e,emb);
				functor = new Funct(comparg,body);
				exp = exp.instReplace(emb,e);		   

				if (functor.freeVars().size()==0){
					char slash = cc.getSlash();
					List<Cat> allCompCats = comparg.type().makeAllCats();
					for (Cat sharedCat : allCompCats){
						cator = new CompCat(slash,cc.getLeft().copy(),sharedCat.copy());
						cator.setSem(functor.copy());
						compcatee = new CompCat(slash,sharedCat.copy(),cc.getRight().copy());
						compcatee.setSem(compfunctee.copy());
						temp = new LinkedList<Cat>();
						temp.add(compcatee);

						// don't allow crossing composition
						if (slash=='\\'){
							// cator goes on right
							temp.add(cator);
						}
						else{
							// cator goes on left
							temp.add(0,cator);
						}
						
						splits.add(temp);

						Cat comp = ((CompCat)cator).comp(((CompCat)compcatee));
						if (comp==null){
							System.out.println("ERROR 3: null in Cat split");
							System.out.println("come functor: "+cator);
							System.out.println("come functee: "+compcatee);
						}
					}
				}
			}
		}
		return splits;
	}


	private List<List<Cat>> splitsCache = null;
	
	/** This function returns all splits of both the semantics
	 * 	and syntax of the category. This is returned as a list
	 * 	of pairs of Cats.
	 * 
	 *  First we get all splits of the logical form. Then we get
	 *  all matching splits of the syntactic category.
	 */
	public List<List<Cat>> allSplits(){

		Exp exp = getSem();

		List<List<Cat>> splits = new LinkedList<List<Cat>>();

		List<Exp> allSub = exp.allSubExps();
	
		for (Exp e : allSub){
			List<Var> vars = e.freeVars();
			// make the expression to pull out
			if (!(vars.size()>Exp.maxNumVars)){
				for (List<Var> order : allOrders(vars)){
					splits.addAll(allSplits(this,e,order));
				}
			}
		}
		return splits;
	}

	/**
	 * need to work out all orders in which new lambdas can be.	
	 * @param input
	 * @return
	 */
	static private List<List<Var>> allOrders(List<Var> input){
		List<List<Var>> result = new LinkedList<List<Var>>();

		if (input.size()<2){
			List<Var> copy = new LinkedList<Var>();
			copy.addAll(input);
			result.add(copy);
			return result;
		}
		List<Var> temp = new LinkedList<Var>();
		temp.addAll(input);
		for (int i=0; i<input.size(); i++){
			Var v = input.remove(i);
			for (List<Var> order : allOrders(input)){
				order.add(0,v);
				result.add(order);
			}
			input.add(i,v);
		}
		return result;
	}

	/**
	 * This takes the functor and functee from a split of the semantics
	 * and finds all syntactic splits.
	 * 
	 * @param cat
	 * @param functor
	 * @param functee
	 * @param functorLeft
	 * @return
	 */
	static public List<List<Cat>> makeAllCats(Cat cat, Exp functor, 
			Exp functee, boolean functorLeft){
		Exp exp = cat.getSem();
		List<List<Cat>> splits = new LinkedList<List<Cat>>();
		List<Cat> allAppCats = null;
		List<Cat> temp = null;

		// first, make the argument variable and the new embedded expression 
		Cat range = null;
		Cat domain = null;
		Cat compcatee = null;
		CompCat cator = null;

		if (((Funct)functor).getArg().type().matches(functee.type())){
			allAppCats = functee.type().makeAllCats();
			temp = new LinkedList<Cat>();

			for (Cat catee : allAppCats){
				catee.setSem(functee);
				range = cat.copy();
				range.setSem(null);
				domain = catee.copy();
				domain.setSem(null);

				if (functorLeft){
					cator = new CompCat('/',range,domain);
					cator.setSem(functor);
					temp = new LinkedList<Cat>();
					temp.add(cator);
					temp.add(catee);
					splits.add(temp);

					if (cator.apply(catee)==null){
						System.out.println("ERROR 1: null in Cat split");
						System.out.println("cat: "+cat);
						System.out.println("functee: "+catee);
						System.out.println("functor: "+cator);
					}
				} else {		
					temp = new LinkedList<Cat>();
					cator = new CompCat('\\',range.copy(),domain.copy());
					cator.setSem(functor);
					temp.add(catee);
					temp.add(cator);
					splits.add(temp);

					if (cator.apply(catee)==null){
						System.out.println("ERROR 2: null in Cat split");
						System.out.println("cat: "+cat);
						System.out.println("functee: "+catee);
						System.out.println("functor: "+cator);
					}
				}
			}
		} else {       
			// do the function composition case
			if (cat instanceof CompCat){
				CompCat cc = (CompCat)cat;
				char slash = cc.getSlash();
				if ((slash=='\\' && !functorLeft) || (slash=='/' && functorLeft)){
					List<Cat> allCompCats = ((Funct)functee).getBody()
					.type().makeAllCats();
					// the shared category is the new component in the composition case
					for (Cat sharedCat : allCompCats){
						cator = new CompCat(slash,cc.getLeft().copy(),sharedCat.copy());
						cator.setSem(functor.copy());
						compcatee = new CompCat(slash,sharedCat.copy(),cc.getRight().copy());
						compcatee.setSem(functee.copy());
						temp = new LinkedList<Cat>();
						temp.add(compcatee);
						// don't allow crossing composition
						if (slash=='\\')
							temp.add(cator);
						else
							temp.add(0,cator);
						splits.add(temp);
						Cat comp = ((CompCat)cator).comp(((CompCat)compcatee));
						if (comp==null){
							System.out.println("ERROR 3: null in Cat split");
							System.out.println("come functor: "+cator);
							System.out.println("come functee: "+compcatee);
						}
					}
				}
			}
		}
		return splits;
	}

	public static Cat EMP = Cat.makeCat("EMPTY");

	// the semantics
	Exp sem;

	// state variables used during parsing to handle coordination
	boolean conj;  
	boolean disj;
	boolean complete;

	public static void main(String[] args){
		PType.addTypesFromFile("../experiments/atis/atis.types");
		Lang.loadLangFromFile("../experiments/atis/atis.lang");

		//Cat c1 = Cat.makeCat("S : (lambda $0 e (lambda $1 e (and (to $0 boston:ci) (from $0 dallas:ci) (= (departure_time $0) $1) (> (departure_time $0) 1800:ti) (flight $0))))");
		//Cat c1 = Cat.makeCat("S\\(S|NP) : (lambda $0 <e,t> (argmax $1 ($0 $1) (count $2 (and (river:t $2) (loc:t $2 $1)))))");
		//Cat c1 = Cat.makeCat("S/NP : (lambda $0 i (lambda $1 e (and (flight $1) (or (airline $1 ua:al) (airline $1 nw:al)) (< (arrival_time $1) $0) (stop $1 denver:ci))))");

		Cat c1 = Cat.makeCat("S : (lambda $0 e (and (flight $0) (or (airline $0 ua:al) (airline $0 nw:al)) (< (arrival_time $0) 1200:i) (stop $0 denver:ci)))");
		//Cat c1 = Cat.makeCat("S : (the $1 (and (state:t $1) (loc:t (argmin $2 (and (place:t $2) (loc:t $2 usa:co)) (elevation:i $2)) $1)))");
		//System.out.println("ORIG: "+c1+ " -- "+c1.getSem().inferType());

		for (List<Cat> split : c1.allSplits()){
			System.out.println("-----------");
			System.out.println(split.get(0));
			System.out.println(split.get(1));
		}
	}

}
