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



import tomkwiatkowski.ubl.learn.*;
import tomkwiatkowski.ubl.lambda.*;
import tomkwiatkowski.ubl.parser.*;

public class TestTrain extends Train {

    public static void main(String[] args){

	PType.addTypesFromFile("geo880.types");
	Lang.loadLangFromFile("geo880.lang");

	Train.extraFeats = false;
	Train.emptyTest = true;

	DataSet train = new DataSet("data/geosents600-typed.ccg.dev");
	//DataSet train = new DataSet("debug.txt");
	System.out.println("Train Size: "+train.size());
	DataSet test = new DataSet("data/geosents280-typed.ccg.test");
	//DataSet test = new DataSet("debug.test");
	System.out.println("Test Size: "+test.size());

	String fixedlex = args[0];

	LexRule fixed = new LexRule("../fixed-lexicon.txt");
	if (!fixedlex.equals("none")){
	    fixed.addEntriesFromFile(fixedlex);
	}

	Train t = new Train();
	t.setDataSet(train);
	t.setTestSet(test);
	t.setFixed(fixed);

	Parser.pruneN=75;
	t.train();

	t.printLexicon();

	Parser.pruneN=100;
	t.test(test);


    }


}
