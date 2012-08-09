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



package utils;

import java.util.*;

public class Subs {

    public Subs(){
	from = new LinkedList();
	to = new LinkedList();
    }
    
    Subs(List f, List t){
	from = f;
	to = t;
    }

    public Object valueFor(Object in){
	int index,old=-1;
	Object o=in;
	for (int i=0;i<from.size(); i++){
	    if (from.get(i).equals(o)){
		o = to.get(i);
	    }
	}
	if (o.equals(in))
	    return null;
	else
	    return o;
    }

    public void addSubs(Object f, Object t){
	from.add(f);
	to.add(t);
    }

    public List from(){
	return from;
    }

    public List to(){
	return from;
    }

    public void clear(){
	from.clear();
	to.clear();
    }

    public Subs copy(){
	return new Subs(new LinkedList(from), new LinkedList(to));
    }

    public String toString(){
	StringBuffer result = new StringBuffer();
	Iterator i = from.iterator();	
	Iterator j = to.iterator();
	result.append("(");
	while (i.hasNext()){
	    result.append("(").append(i.next()).append(",").
		append(j.next()).append(")");
	}
	result.append(")");
	return result.toString();
    }

    List from;
    List to;
    
}
