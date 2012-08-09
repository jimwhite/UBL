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

public class PowerSet<T> extends AbstractCollection<Set<T>> implements Collection<Set<T>> {
    final T[] elts;
    final int size;
    final int hashCode;
    final int n;
 
    public PowerSet (Set<T> source) {
	this.n = source.size();
	this.elts = (T[])source.toArray();
	this.size = 1 << n;
	this.hashCode = (1 << (n-1)) * Arrays.hashCode(this.elts);
    }
 
    public int hashCode () { return this.hashCode; }
 
    public boolean equals (Object e) {
	return false;
    }
 
    public int size () {
	return size;
    }
 
    class BitMaskSet extends AbstractCollection<T> implements Set<T> {
	final int mask;
 
	BitMaskSet (int mask) {
	    this.mask = mask;
	}
 
	public int hashCode () {
	    int hashCode = 0;
 
	    for (int i = 0, mask = this.mask; mask > 0; mask >>>= 1, ++i) {
		if ((mask&1)==1) hashCode += elts[i].hashCode();
	    }
 
	    return hashCode;
	}
 
	public int size () {
	    int size = 0;
 
	    for (int mask = this.mask; mask > 0; mask >>>= 1) {
		size += mask&1;
	    }
 
	    return size;
	}
 
	public Iterator<T> iterator () {
	    return new Iterator<T> () {
		int i = 0;
		int mask = BitMaskSet.this.mask;
 
		public T next () {
		    while ((mask&1)==0) {
			++i;
			mask >>>= 1;
		    }
 
		    final T next = elts[i];
 
		    ++i;
		    mask >>>= 1;
 
		    return next;
		}
 
		public boolean hasNext () {
		    return mask != 0;
		}
 
		public void remove () {
		    throw new UnsupportedOperationException();
		}
	    };
	}
    }
 
    public Iterator<Set<T>> iterator () {
	return new Iterator<Set<T>> () {
	    int i = 0;
 
	    public Set<T> next () {
		return new BitMaskSet(i++);
	    }
 
	    public boolean hasNext () {
		return i < size;
	    }
 
	    public void remove () {
		throw new UnsupportedOperationException();
	    }
	};
    }
};