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

// I don't know why this class exists.  I will get rid of it soon.  -Luke

public class Globals {

    static public HashVector theta = null;
    static public LexiconFeatSet lexPhi = new LexiconFeatSet(1);
    static public List<ParseFeatureSet> parseFeatures = new LinkedList<ParseFeatureSet>();
    static public List<LexicalFeatureSet> lexicalFeatures = new LinkedList<LexicalFeatureSet>();
    static public int lastWordIndex;
    static public List<String> tokens;
    static public Lexicon lexRule;

}
