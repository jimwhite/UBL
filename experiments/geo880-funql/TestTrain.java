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



import tom_kwiatkowski.ubl.learn.*;
import tom_kwiatkowski.ubl.lambda.*;
import tom_kwiatkowski.ubl.parser.*;

public class TestTrain extends Train {

	public static void main(String[] args){

		PType.addTypesFromFile("geo-funql.types");
		Lang.loadLangFromFile("geo-funql.lang");

		Train.emptyTest = true;

		DataSet train = new DataSet("data/geo880-funql.train");
		DataSet test = new DataSet("data/geo880-funql.test");

		System.out.println("Train Size: "+train.size());
		System.out.println("Test Size: "+test.size());
	
		LexiconFeatSet.loadCoOccCounts("data/geo-funql.dev.giza_probs");

		String fixedlex = args[0];

		Lexicon fixed = new Lexicon();
		if (!fixedlex.equals("none")){
			fixed.addEntriesFromFile(fixedlex);
		}

		Train.EPOCHS=20;

		Train.alpha_0 = 1.0;
		Train.c = 0.00001;
		Train.verbose=true;
		Train.maxSentLen=50;
		
		LexiconFeatSet.initWeightMultiplier = 10.0;
		LexiconFeatSet.initLexWeight = 10.0;

		Parser.pruneN=200;

		System.out.println("alpha_0 = "+Train.alpha_0);
		System.out.println("C = "+Train.c);
		System.out.println("initialMultiplier = "+LexiconFeatSet.initWeightMultiplier);
		System.out.println("NP init = "+LexiconFeatSet.initLexWeight);
		System.out.println("Parser beam  = "+Parser.pruneN);

		Train t = new Train();
		t.setDataSet(train);
		t.setTestSet(test);
		
		Parser p = new Parser(fixed);
		p.makeFeatures();

		t.stocGradTrain(p,true);

		t.test(p,true);

	}


}
