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

public class Lang {

    public static boolean hasPred(String name){
	Iterator i = preds.iterator();
	while (i.hasNext()){
	    Pred p = (Pred)i.next();
	    if (p.getName().equals(name))
		return true;
	}
	return false;
    }

    //< functions for getting predicates defined in this language
    // NOTE:  This might return the wrong Pred if two exist that vary
    //        only in their argument types.
    public static Pred getPred(String name, int arity){
	Iterator i = preds.iterator();
	while (i.hasNext()){
	    Pred p = (Pred)i.next();
	    if (p.getName().equals(name) && p.arity()==arity)
		return p;
	}
	//System.out.println("ERROR: unknown pred "+name+"/"+arity);
	//System.out.println(preds);
	return null;
    }

    //>
    /*
    public static List arityTwoWithSecondType(PType t){
	List ret = new LinkedList();
	Iterator i = preds.iterator();
	while (i.hasNext()){
	    Pred p = (Pred)i.next();
	    if (p.arity()==2 && !p.getType(1).equals(PType.E) &&
		!p.getType(1).equals(PType.I) &&
		p.getType(1).matches(t) )
		ret.add(p);
	}
	return ret;
    }
    */
    public static List predsWithArity(int a){
	List ret = new LinkedList();
	Iterator i = preds.iterator();
	while (i.hasNext()){
	    Pred p = (Pred)i.next();
	    if (p.arity()==a){
		ret.add(p);
	    }
	}
	return ret;
    }
    public static List allPreds(){
	return preds;
    }

    //< loadLang(String) -- loads the language from a string 
    public static void loadLang(String input){
	preds = new LinkedList();

	LispReader lr = new LispReader(new StringReader(input));
	while (lr.hasNext()){
	    String pred = lr.next();
	    LispReader lrp = new LispReader(new StringReader(pred));
	    int arity = 0;
	    String name = lrp.next();
	    //System.err.println("NAME: "+name);
	    while (lrp.hasNext()){
		arity++;
		lrp.next();
	    }
	    arity--;
	    //System.err.println("ARITY: "+arity);

	    Pred p = getPred(name,arity);
	    if (p==null) {
		preds.add(new Pred(pred));
	    } else {
		//System.err.println("HERE: "+p);
		p.addTuple(pred);
	    }
	}
    }
    //>

    //< loadLangFromFile(String) -- loads the language from a file 
    public static void loadLangFromFile(String filename){
	StringBuffer lang= new StringBuffer();
	try{
	    BufferedReader in = new BufferedReader(new FileReader(filename));
	    String line = in.readLine();
	    while (line!=null){  // for each line in the file
		line.trim();
		line = line.split("\\s*//")[0];
		if (!line.equals("")){
		    lang.append(line).append(" ");
		}
		line = in.readLine();
	    }

	} catch(IOException e){ System.err.println(e); }
	//System.out.println("LANG STRING: "+lang.toString());
	loadLang(lang.toString());
    }
    //>

    // the predicates that are allowed in this language
    static List preds;

    public static void main(String[] args){
	PType.addTypesFromFile("atis.types");
	Lang.loadLangFromFile("atis.lang");
    }

}
