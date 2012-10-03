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
package com.joliciel.talismane.tokeniser.patterns;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

import com.joliciel.talismane.machineLearning.DecisionMaker;
import com.joliciel.talismane.tokeniser.Tokeniser;
import com.joliciel.talismane.tokeniser.TokeniserOutcome;
import com.joliciel.talismane.tokeniser.TokeniserService;
import com.joliciel.talismane.tokeniser.features.TokenFeatureService;
import com.joliciel.talismane.tokeniser.features.TokeniserContextFeature;

class TokeniserPatternServiceImpl implements TokeniserPatternService {
	TokeniserService tokeniserService;
	TokenFeatureService tokenFeatureService;

	@Override
	public TokeniserPatternManager getPatternManager(List<String> patternDescriptors) {
		TokeniserPatternManagerImpl patternManager = new TokeniserPatternManagerImpl(patternDescriptors);
		patternManager.setTokeniserPatternService(this);
		patternManager.setTokeniserService(this.getTokeniserService());
		return patternManager;
	}
	
	@Override
	public TokeniserPatternManager getDefaultPatternManager(
			Locale locale) {
		TokeniserPatternManagerImpl processor = new TokeniserPatternManagerImpl(locale);
		processor.setTokeniserPatternService(this);
		processor.setTokeniserService(this.getTokeniserService());
		return processor;
	}	
	public Tokeniser getPatternTokeniser(TokeniserPatternManager patternManager,
			Set<TokeniserContextFeature<?>> tokeniserContextFeatures, 
			DecisionMaker<TokeniserOutcome> decisionMaker, int beamWidth) {
		PatternTokeniserImpl tokeniser = new PatternTokeniserImpl(patternManager, tokeniserContextFeatures, beamWidth);
		tokeniser.setTokeniserPatternService(this);
		tokeniser.setTokeniserService(this.getTokeniserService());
		tokeniser.setTokenFeatureService(this.getTokenFeatureService());
		tokeniser.setDecisionMaker(decisionMaker);
		return tokeniser;
	}
	
	@Override
	public TokenPattern getTokeniserPattern(String regexp, Pattern separatorPattern) {
		TokenPattern pattern = new TokenPatternImpl(regexp, separatorPattern);
		return pattern;
	}
	
	public TokeniserService getTokeniserService() {
		return tokeniserService;
	}

	public void setTokeniserService(TokeniserService tokeniserService) {
		this.tokeniserService = tokeniserService;
	}

	public TokenFeatureService getTokenFeatureService() {
		return tokenFeatureService;
	}

	public void setTokenFeatureService(TokenFeatureService tokenFeatureService) {
		this.tokenFeatureService = tokenFeatureService;
	}


}