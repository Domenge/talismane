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
package com.joliciel.talismane.tokeniser;

import com.joliciel.talismane.TalismaneServiceLocator;

public class TokeniserServiceLocator {
	TokeniserServiceImpl tokeniserService = null;
	private TalismaneServiceLocator talismaneServiceLocator;
	
	public TokeniserServiceLocator(TalismaneServiceLocator talismaneServiceLocator) {
		this.talismaneServiceLocator = talismaneServiceLocator;
	}
	
	public TokeniserService getTokeniserService() {
		if (tokeniserService==null) {
			tokeniserService = new TokeniserServiceImpl();
			tokeniserService.setTokenFeatureService(this.talismaneServiceLocator.getTokeniserFeatureServiceLocator().getTokenFeatureService());
			tokeniserService.setTokeniserPatternService(this.talismaneServiceLocator.getTokenPatternServiceLocator().getTokeniserPatternService());
		}
		return tokeniserService;
	}

	public TalismaneServiceLocator getTalismaneServiceLocator() {
		return talismaneServiceLocator;
	}
}