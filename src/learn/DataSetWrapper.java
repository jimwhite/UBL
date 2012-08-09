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



package learn;

import java.util.*;
import java.io.*;
import lambda.*;
import parser.*;

public class DataSetWrapper {


	public DataSetWrapper(String directory){
		// get a list of files in the directory
		//System.out.println("Dir: "+directory);
		File dir = new File(directory);
		String[] files = dir.list();
		Arrays.sort(files);

		data = new LinkedList<DataSet>();
		filenames = new LinkedList<String>();
		// load each one
		int count = 0;
		for (int i=0; i<files.length; i++){
			if (files[i].endsWith(".log")){
				count++;
				//System.out.println(count+": "+files[i]);
				filenames.add(files[i]);
				data.add(new DataSet(directory+files[i]));
			}
		}
	}

	public DataSetWrapper(DataSet d){
		data = new LinkedList<DataSet>();
		data.add(d);
		filenames = new LinkedList<String>();
		filenames.add("0");
	}

	public String getFilename(int i){
		return filenames.get(i);
	}

	public DataSet getDataSet(int i){
		return data.get(i);
	}

	public int size(){
		return data.size();
	}

	List<String> filenames;
	List<DataSet> data;

	public static void main(String[] args){
		PType.addTypesFromFile(args[0]);
		Lang.loadLangFromFile(args[1]);

		DataSetWrapper d = new DataSetWrapper(args[2]);
	}
}
