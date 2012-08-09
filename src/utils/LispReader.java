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




/**
 * This is one big hackish mess because I hate to write file
 * processing code!
 */

package utils;

import java.io.*;
import java.util.*;

public class LispReader {

    public LispReader(Reader i){
	in = i;
	lastc = ' ';
	lasti=0;
	skipPast('(');
	skipWS();
    }

    public String next(){
	if (lastc == '(')
	    return readList();
	return readWord();
    }

    private String readWord(){
        String result="";
        try {
            while (!Character.isWhitespace(lastc) && lastc!=')' && lasti!=-1){
                result += lastc;
                lasti = in.read();
                lastc = (char)lasti;
            }
            lasti = in.read();
            lastc = (char)lasti;
        } catch(IOException e){ System.out.println(e);};
	skipWS();
        return result;	
    }

    private String readList(){
	String result="(";
	int depth = 1; 
	try {
	    while (depth != 0 && lasti!=-1){
		lasti = in.read();
		lastc = (char)lasti;
		if (lastc == '(') depth++;
		if (lastc == ')') depth--;
		result += lastc;
	    }
            lasti = in.read();
            lastc = (char)lasti;
	} catch(IOException e){ System.out.println(e);};		
	skipWS();
	return result;
    }

    private void skipPast(char seek){
        try {
            while (lastc != seek && lasti!=-1){
                lasti = in.read();
                lastc = (char)lasti;
            }
            lasti = in.read();
            lastc = (char)lasti;
        } catch(IOException e){ System.out.println(e);};
    }

    private void skipWS(){
	try{
	    while (Character.isWhitespace(lastc) || lastc==')'){
		lasti = in.read();
		lastc = (char)lasti;	     
	    } 
	} catch(IOException e){ System.out.println(e);};
    }

    public boolean hasNext(){
	return lasti!=-1;
    }

    Reader in;
    char lastc;
    int lasti;

    /**
     * Testing code
     */

    public static void main(String[] args){
	String s = "(a\nb)";
	LispReader r  = new LispReader( new StringReader(s));
	while (r.hasNext()){
	    System.out.println(r.next());
	}
    }

}
