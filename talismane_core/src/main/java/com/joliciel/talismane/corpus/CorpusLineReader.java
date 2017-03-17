///////////////////////////////////////////////////////////////////////////////
//Copyright (C) 2017 Joliciel Informatique
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
package com.joliciel.talismane.corpus;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.joliciel.talismane.TalismaneException;
import com.joliciel.talismane.TalismaneSession;
import com.joliciel.talismane.corpus.CorpusLine.CorpusElement;
import com.joliciel.talismane.lexicon.CompactLexicalEntry;
import com.joliciel.talismane.lexicon.CompactLexicalEntrySupport;
import com.joliciel.talismane.lexicon.LexicalEntryReader;
import com.joliciel.talismane.lexicon.WritableLexicalEntry;

/**
 * A corpus reader that expects one token per line, and analyses the line
 * content based on a regex supplied during construction.<br/>
 * 
 * The regex contains placeholders, indicated by %, the name of a
 * {@link CorpusElement}, and another %. For example: %token% for the
 * {@link CorpusElement#TOKEN}.
 * 
 * Inside this regex, the placeholders will be replaced by
 * {@link CorpusElement#getReplacement()}, prior to applying the regex to each
 * line read.
 * 
 * @author Assaf Urieli
 *
 */
public class CorpusLineReader {
	private final String regex;
	private final Pattern pattern;
	private final TalismaneSession session;
	private final LexicalEntryReader lexicalEntryReader;
	private final CompactLexicalEntrySupport lexicalEntrySupport = new CompactLexicalEntrySupport("");

	private Map<CorpusElement, Integer> placeholderIndexMap = new HashMap<>();

	/**
	 * 
	 * @param regex
	 *            the regex used to read lines, including placeholders as
	 *            described in the class description
	 * @param requiredElements
	 *            any elements whose placeholders have to be found in the regex
	 * @param lexicalEntryReader
	 *            an optional lexical entry reader, for constructing a lexical
	 *            entry out of each line
	 * @param session
	 *            the Talismane session
	 */
	public CorpusLineReader(String regex, CorpusElement[] requiredElements, LexicalEntryReader lexicalEntryReader, TalismaneSession session) {
		this.session = session;
		this.regex = regex;
		this.lexicalEntryReader = lexicalEntryReader;

		Set<CorpusElement> requiredElementSet = new HashSet<>(Arrays.asList(requiredElements));

		Map<Integer, String> placeholderMap = new TreeMap<Integer, String>();
		for (CorpusElement elementType : CorpusElement.values()) {
			String placeholder = "%" + elementType + "%";
			int pos = regex.indexOf(placeholder);
			if (requiredElementSet.contains(elementType) && pos < 0)
				throw new TalismaneException("The regex must contain the string \"" + placeholder + "\": " + regex);
			if (pos >= 0)
				placeholderMap.put(pos, elementType.name());
		}

		for (int j = 0; j < regex.length(); j++) {
			if (regex.charAt(j) == '(') {
				placeholderMap.put(j, "");
			}
		}
		int i = 1;
		for (int placeholderIndex : placeholderMap.keySet()) {
			String placeholderName = placeholderMap.get(placeholderIndex);
			if (placeholderName.length() > 0)
				placeholderIndexMap.put(CorpusElement.valueOf(placeholderName), i);
			i++;
		}

		String regexWithGroups = regex;
		for (CorpusElement elementType : CorpusElement.values()) {
			String placeholder = "%" + elementType + "%";
			regexWithGroups = regexWithGroups.replace(placeholder, elementType.getReplacement());
		}

		this.pattern = Pattern.compile(regexWithGroups, Pattern.UNICODE_CHARACTER_CLASS);
	}

	/**
	 * Read one line out of the corpus, and transform it into a
	 * {@link CorpusLine}
	 * 
	 * @param line
	 *            the line to read
	 * @param lineNumber
	 *            the line number we reached, starting at 1.
	 */
	public CorpusLine read(String line, int lineNumber) {
		Matcher matcher = this.pattern.matcher(line);
		if (!matcher.matches())
			throw new TalismaneException("Didn't match pattern \"" + regex + "\" on line " + lineNumber + ": " + line);

		if (matcher.groupCount() != placeholderIndexMap.size()) {
			throw new TalismaneException("Expected " + placeholderIndexMap.size() + " matches (but found " + matcher.groupCount() + ") on line " + lineNumber);
		}

		CorpusLine corpusLine = new CorpusLine(line, lineNumber);
		for (CorpusElement elementType : CorpusElement.values()) {
			if (placeholderIndexMap.containsKey(elementType)) {
				String value = matcher.group(placeholderIndexMap.get(elementType));
				switch (elementType) {
				case TOKEN:
				case LEMMA:
					value = session.getCoNLLFormatter().fromCoNLL(value);
					break;
				default:
					if ("_".equals(value))
						value = "";
					break;
				}
				corpusLine.setElement(elementType, value);
			}
		}

		if (this.lexicalEntryReader != null) {
			WritableLexicalEntry lexicalEntry = new CompactLexicalEntry(lexicalEntrySupport);
			this.lexicalEntryReader.readEntry(line, lexicalEntry);
			corpusLine.setLexicalEntry(lexicalEntry);
		}

		// TODO: add rules that replace elements thus no longer requiring
		// PennTreebankReader, etc.

		return corpusLine;
	}

	/**
	 * Does this reader know how to find a given element type.
	 */
	public boolean hasPlaceholder(CorpusElement type) {
		return this.placeholderIndexMap.containsKey(type);
	}
}
