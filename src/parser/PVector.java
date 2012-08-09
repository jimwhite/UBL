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

/**
 * A parameter weight vector 
 * 
 * @specfield int fixed -- p_i where i &lt; fixed are immutable.
 * (everything before this index is fixed and can not change)
 **/

abstract public class PVector {

    public void setFixed(int n){
	fixed = n;
    }

    public int start(){
	return fixed;
    }

    public void reset(int size, double val){
	reset();
	for (int i=0; i<size; i++)
	    add(val);
    }

    /**
     * Set the ith weight to f
     **/
    abstract public void set(int i, double f);

    /**
     * push the weight f onto the end of the weight vector. 
     **/
    abstract public void add(double f);

    abstract public void add(int i, double f);

    abstract public double get(int i);

    abstract public String toString();

    /**
     * Let p_i = p_i * times + this_i for all i in p.fixed .. min(p.size, this.size)
     * 
     * @modifies p
     **/
    abstract public void addTimesInto(double times, PVector p);

    /**
     * Divide all weights after and including p_fixed by d
     **/
    abstract public void divideBy(double d);

    /**
     * clear() would be a better term.  Clear all entries, even fixed ones. 
     **/
    abstract public void reset();

    abstract public int size();

    abstract public void remove(int i);

    /**
     * checkRep: no this_i is -infty, +infty, or NaN.  Yay for
     * checkReps!  If only it ever got called.  TODO: assert it where
     * it belongs. 
     **/
    abstract public void checkForBadness();

    abstract public boolean isBad();

    public void printNonZeroValues(){
	for (int i=0; i<size(); i++){
	    double d = get(i);
	    if (d!=0.0){
		System.out.print(i+":"+d+", ");
	    }
	}
	System.out.println();
    }

    public double dot(PVector p){
	double result=0.0;
	int end = Math.min(size(),p.size());
	for (int i=0; i<size(); i++){
	    result+=get(i)*p.get(i);
	}
	return result;
    }

    

    int fixed;  // everything before this index can not change

    // The implementing class's list of doubles could certainly be declared here, and probably should be. 
    // Actually, the original idea was to allow different implementing subclasses.
    // For example, one that uses Lists and one that uses arrays.  But, I never 
    // got around to doing the array version.  -Luke
}
