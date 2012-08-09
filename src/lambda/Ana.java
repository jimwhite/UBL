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

/*
 *  The class represents anaphoric expressions.  
 */

public class Ana extends Var {

    public Ana(Type ty){
	super(ty);
    }

    public Ana(String ty){
	// ignore fist character
	super(Type.makeType(ty.substring(1,ty.length()))); 
    }

    public String toString(List varName){
	return "!"+t;
    }

    public String toLatexString(List varName){
	return "\\!"+t;
    }

    public static boolean instEq=false;
    public boolean equals(Object o){
	if (instEq) return this==o;
	if (o instanceof Ana){
	    Ana a = (Ana)o;
	    return a.t.equals(t);
	} else return false;
    }

    public Exp copy(){
	if (copyAna){
	    //System.out.println("HERE!");
	    return new Ana(t);
	} else {
	    //System.out.println("HERE! 2");
	    return this;
	}
    }
}
