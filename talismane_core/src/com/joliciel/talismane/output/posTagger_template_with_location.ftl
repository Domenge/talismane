[#ftl]
[#list sentence as unit]
[#if unit.token.precedingRawOutput??]
${unit.token.precedingRawOutput}
[/#if]
${unit.token.index?c}	[#if unit.token.originalText?length==0]_[#else]${unit.token.originalText?replace(" ","_")}[/#if]	${((unit.lexicalEntry.lemma)!"_")?replace(" ","_")}	${unit.tag.code}	${(unit.lexicalEntry.category)!"_"}	${(unit.lexicalEntry.morphology)!"_"}	${unit.token.fileName}	${(unit.token.lineNumber)?c}	${(unit.token.columnNumber)?c}	
[/#list]
