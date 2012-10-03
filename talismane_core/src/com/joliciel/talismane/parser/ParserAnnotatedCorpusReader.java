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
package com.joliciel.talismane.parser;

import java.util.Map;

import com.joliciel.talismane.tokeniser.filters.TokenFilter;

/**
 * An interface for reading ParseConfigurations from sentences in a corpus.
 * @author Assaf Urieli
 *
 */
public interface ParserAnnotatedCorpusReader {
	/**
	 * Is there another sentence to be read?
	 * @return
	 */
	public boolean hasNextConfiguration();
	
	/**
	 * Read the ParseConfiguration from the next sentence in the training corpus.
	 * @return
	 */
	public ParseConfiguration nextConfiguration();
	
	public void addTokenFilter(TokenFilter tokenFilter);
	
	/**
	 * 
	 * @return
	 */
	public Map<String, Object> getAttributes();
}