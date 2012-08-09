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

public class HashVector {

    public HashVector(){
	values = new LinkedHashMap<String,Double>();
    }

    public HashVector(HashVector other){
	values = new LinkedHashMap<String,Double>();
	values.putAll(other.values);
    }

    public void set(String s, double f){
	values.put(s,new Double(f));
    }

    public double get(String s){
	Double d = values.get(s);
	if (d==null) return 0.0;
	return d.doubleValue();
    }

    public String toString(){
	return values.toString();
    }

    /**
     * Let p_i = p_i * times + this_i for all i in p.fixed .. min(p.size, this.size)
     * 
     * @modifies p
     **/
    public void addTimesInto(double times, HashVector p){
	for (Map.Entry<String,Double> me : values.entrySet()){
	    String key = me.getKey();
	    p.set(key,(times*me.getValue().doubleValue())+p.get(key));
	}
    }

    /**
     * Divide all weights after and including p_fixed by d
     **/
    public void divideBy(double d){
	for (String s : values.keySet()){
	    values.put(s,values.get(s)/d);
	}
    }

    /**
     * Divide all weights after and including p_fixed by d
     **/
    public void multiplyBy(double d){
	for (String s : values.keySet()){
	    values.put(s,values.get(s)*d);
	}
    }

    public void dropSmallEntries(){
	Iterator<Double> i = values.values().iterator();
	while (i.hasNext()){
	    if (Math.abs(i.next().doubleValue())<NOISE)
		i.remove();
	}
    }

    public void clear(){
	values.clear();
    }

    public int size(){
	return values.size();
    }

    public boolean isBad(){
	for (Double d : values.values())
	    if (d.isNaN() || d.isInfinite())
		return true;
	return false;
    }

    public boolean valuesInRange(double min, double max){
	for (Double d : values.values()){
	    double v = d.doubleValue();
	    if (v<min || v>max)
		return false;
	}
	return true;
    }

    public void printValues(HashVector other){
	System.out.print("{");
	for (String s : other.values.keySet()){
	    System.out.print(s+"="+values.get(s)+"("+other.values.get(s)+"),");
	}
	System.out.print("}");
    }

    Map<String,Double> values;

    public static double NOISE=0.00001;

}
