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

import utils.*;
import java.util.*;

public class SimpCat extends Cat {

    //< SimpCat(String) constructor
    public SimpCat(String name){
	this.name = name.trim();//.intern();
	complete = true;
    }
    //>

    private SimpCat(){
	complete = true;
    }

    //< toString functions.
    //  with and without semantics
    public String toString(){
	if (sem == null)
	    return name;
	else
	    return name+" : "+sem;
    }

    //>

    //< copy() and equals() functions
    public boolean equalsNoSem(Object o){
	if (!(o instanceof SimpCat)) 
	    return false;
	SimpCat sc = (SimpCat)o;
	if (conj!=sc.conj) return false;
	if (disj!=sc.disj) return false;
	if (complete!=sc.complete) return false;
	return sc.name.equals(name);
    }

    public boolean matchesNoSem(Cat o){
	if (!(o instanceof SimpCat)) 
	    return false;
	SimpCat sc = (SimpCat)o;
	return sc.name.equals(name);
    }

    public boolean equals(Object o){
	if (!(o instanceof SimpCat)) 
	    return false;
	SimpCat sc = (SimpCat)o;
	if (sem!=null && sc.sem!=null && !sem.equals(sc.sem)) return false;
	if (conj!=sc.conj) return false;
	if (disj!=sc.disj) return false;
	if (complete!=sc.complete) return false;
	return sc.name.equals(name);
    }

    public boolean matches(Cat ot){
	return equals(ot);
    }

    public Cat copy(){
	SimpCat c = new SimpCat();
	c.name = name;
	c.sem = sem;
	return c;
    }
    //>


    public int syntaxHash(){
	return name.hashCode();
    }

    public int numSlashes(){
	return 0;
    }

    // the name of this atomic category
    String name;

}
