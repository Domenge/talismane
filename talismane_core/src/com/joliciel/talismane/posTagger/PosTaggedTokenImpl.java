///////////////////////////////////////////////////////////////////////////////
//Copyright (C) 2012 Assaf Urieli
//
//This file is part of Talismane.
//
//Talismane is free software: you can redistribute it and/or modify
//it under the terms of the GNU Affero General Public License as published by
//the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.
//
//Talismane is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU Affero General Public License for more details.
//
//You should have received a copy of the GNU Affero General Public License
//along with Talismane.  If not, see <http://www.gnu.org/licenses/>.
//////////////////////////////////////////////////////////////////////////////
package com.joliciel.talismane.posTagger;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.joliciel.talismane.lexicon.LexicalEntry;
import com.joliciel.talismane.machineLearning.Decision;
import com.joliciel.talismane.machineLearning.features.Feature;
import com.joliciel.talismane.machineLearning.features.FeatureResult;
import com.joliciel.talismane.tokeniser.TaggedTokenImpl;
import com.joliciel.talismane.tokeniser.Token;

class PosTaggedTokenImpl extends TaggedTokenImpl<PosTag> implements PosTaggedToken {
	private  PosTaggerLexiconService lexiconService;
	private Map<String,FeatureResult<?>> featureResults = new HashMap<String, FeatureResult<?>>();

	private Set<LexicalEntry> entries = null;
	
	public PosTaggedTokenImpl(Token token, Decision<PosTag> decision) {
		super(token, decision);
	}

	@Override
	public Set<LexicalEntry> getLexicalEntries() {
		if (entries==null) {
			entries = this.lexiconService.findLexicalEntries(this.getToken().getText(), this.getTag());
		}
		return entries;
	}

	public PosTaggerLexiconService getLexiconService() {
		return lexiconService;
	}

	public void setLexiconService(PosTaggerLexiconService lexiconService) {
		this.lexiconService = lexiconService;
	}

	@Override
	public String toString() {
		return this.getToken().getText() + "|" + this.getTag() + "|" + this.getToken().getIndex() + "| prob=" + this.getDecision().getProbability();
	}

	@Override
	public LexicalEntry getLexicalEntry() {
		Set<LexicalEntry> lexicalEntries = this.getLexicalEntries();
		LexicalEntry bestLexicalEntry = null;
		if (lexicalEntries.size()>0) {
			bestLexicalEntry = lexicalEntries.iterator().next();
		}
		return bestLexicalEntry;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T,Y> FeatureResult<Y> getResultFromCache(
			Feature<T, Y> feature) {
		FeatureResult<Y> result = null;
	
		if (this.featureResults.containsKey(feature.getName())) {
			result = (FeatureResult<Y>) this.featureResults.get(feature.getName());
		}
		return result;
	}

	
	@Override
	public <T,Y> void putResultInCache(Feature<T, Y> feature,
			FeatureResult<Y> featureResult) {
		this.featureResults.put(feature.getName(), featureResult);	
	}

	@Override
	public PosTaggedToken getPosTaggedToken() {
		return this;
	}


}