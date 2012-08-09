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

public class DevTrain extends Train {

	public static void main(String[] args){

	PType.addTypesFromFile("geo-lambda.types");
	Lang.loadLangFromFile("geo-lambda.lang");
	
	int runNum = Integer.parseInt(args[0]);
	int splitNum = Integer.parseInt(args[1]);
	String language = args[2];
	
	DataSet train = new DataSet("data/"+language+"/run-"+runNum+"/fold-"+splitNum+"/train");
	System.out.println("Train Size: "+train.size());
	
	DataSet test = new DataSet("data/"+language+"/run-"+runNum+"/fold-"+splitNum+"/test");
	System.out.println("Test Size: "+test.size());

	String giza_probs = "data/"+language+"/run-"+runNum+"/fold-"+splitNum+"/w-c.giza_probs";
	LexiconFeatSet.loadCoOccCounts(giza_probs);

	String fixedlex =null;
	fixedlex =  language+"-np-fixedlex.geo";
	
	Lexicon fixed = new Lexicon();
	fixed.addEntriesFromFile(fixedlex,true);

	Train.EPOCHS=10;
	Train.alpha_0 = 1.0;
	Train.c = 0.00001;
	Train.maxSentLen=50;
	
	LexiconFeatSet.initWeightMultiplier = 10.0;
	LexiconFeatSet.initLexWeight = 10.0;
	Parser.pruneN=200;
	
	System.out.println("alpha_0 = "+Train.alpha_0);
	System.out.println("C = "+Train.c);
	System.out.println("initialMultiplier = "+LexiconFeatSet.initWeightMultiplier);
	System.out.println("NP init = "+LexiconFeatSet.initLexWeight);
	System.out.println("Parser beam  = "+Parser.pruneN);

	Parser p = new Parser(fixed);
	p.makeFeatures();

	Train t = new Train();
	t.setDataSet(train);
	t.setTestSet(test);

	Train.verbose = true;
	t.stocGradTrain(p,true);	

	t.test(p,Train.pruneLex);

	}
}

