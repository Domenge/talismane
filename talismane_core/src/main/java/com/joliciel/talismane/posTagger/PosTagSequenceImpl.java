package com.joliciel.talismane.posTagger;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.joliciel.talismane.TalismaneService;
import com.joliciel.talismane.machineLearning.Decision;
import com.joliciel.talismane.machineLearning.GeometricMeanScoringStrategy;
import com.joliciel.talismane.machineLearning.MachineLearningService;
import com.joliciel.talismane.machineLearning.ScoringStrategy;
import com.joliciel.talismane.machineLearning.Solution;
import com.joliciel.talismane.tokeniser.Token;
import com.joliciel.talismane.tokeniser.TokenSequence;

class PosTagSequenceImpl extends ArrayList<PosTaggedToken> implements PosTagSequence {
	private static final Logger LOG = LoggerFactory.getLogger(PosTagSequenceImpl.class);
	private static final long serialVersionUID = 5038343676751568000L;
	private TokenSequence tokenSequence;
	private double score = 0.0;
	private boolean scoreCalculated = false;
	private String string = null;
	private List<Decision> decisions = new ArrayList<Decision>();
	private List<Solution> underlyingSolutions = new ArrayList<Solution>();
	@SuppressWarnings("rawtypes")
	private ScoringStrategy scoringStrategy = new GeometricMeanScoringStrategy();

	private PosTaggerServiceInternal posTaggerServiceInternal;
	private TalismaneService talismaneService;
	private MachineLearningService machineLearningService;

	PosTagSequenceImpl(TokenSequence tokenSequence) {
		super(tokenSequence.size());
		this.setTokenSequence(tokenSequence);
	}

	PosTagSequenceImpl(PosTagSequence history) {
		super(history.size() + 1);
		for (PosTaggedToken posTaggedToken : history)
			this.addPosTaggedToken(posTaggedToken);
		this.decisions = new ArrayList<Decision>(history.getDecisions());
		this.setTokenSequence(history.getTokenSequence());
	}

	PosTagSequenceImpl(PosTagSequenceImpl clone, boolean fromClone) {
		this.setTokenSequence(clone.getTokenSequence().cloneTokenSequence());
		this.setPosTaggerServiceInternal(clone.getPosTaggerServiceInternal());
		this.setScoringStrategy(clone.getScoringStrategy());
		int i = 0;
		for (PosTaggedToken posTaggedToken : clone) {
			PosTaggedToken newPosTaggedToken = posTaggedToken.clonePosTaggedToken();
			newPosTaggedToken.setToken(this.getTokenSequence().get(i++));
			this.add(newPosTaggedToken);
		}
		this.decisions = new ArrayList<Decision>(clone.getDecisions());
	}

	@SuppressWarnings("unchecked")
	@Override
	public double getScore() {
		if (!scoreCalculated) {
			score = this.scoringStrategy.calculateScore(this);
			scoreCalculated = true;
		}
		return score;
	}

	@Override
	public TokenSequence getTokenSequence() {
		return tokenSequence;
	}

	@Override
	public Token getNextToken() {
		return this.tokenSequence.get(this.size());
	}

	@Override
	public int compareTo(PosTagSequence o) {
		if (this.getScore() < o.getScore()) {
			return 1;
		} else if (this.getScore() > o.getScore()) {
			return -1;
		} else {
			return 0;
		}
	}

	@Override
	public synchronized String toString() {
		if (string == null) {
			StringBuilder builder = new StringBuilder();
			builder.append("Sequence: ");
			for (PosTaggedToken taggedToken : this) {
				builder.append(taggedToken.toString());
				builder.append(", ");
			}
			string = builder.toString();
		}
		return string;
	}

	@Override
	public PosTaggedToken prependRoot() {
		PosTaggedToken rootToken = null;
		if (this.size() > 0) {
			rootToken = this.get(0);
			if (!rootToken.getTag().equals(PosTag.ROOT_POS_TAG))
				rootToken = null;
		}
		if (rootToken == null) {
			Token emptyToken = tokenSequence.addEmptyToken(0);
			emptyToken.setText("[ROOT]");
			tokenSequence.setWithRoot(true);
			tokenSequence.finalise();

			Decision rootDecision = machineLearningService.createDefaultDecision(PosTag.ROOT_POS_TAG.getCode());
			rootToken = new PosTaggedToken(emptyToken, rootDecision, talismaneService.getTalismaneSession());
			this.add(0, rootToken);
			rootToken.setPosTagSequence(this);
		}
		this.string = null;
		return rootToken;
	}

	@Override
	public void removeRoot() {
		PosTaggedToken rootToken = null;
		if (this.size() > 0) {
			rootToken = this.get(0);
			if (!rootToken.getTag().equals(PosTag.ROOT_POS_TAG))
				rootToken = null;
		}
		if (rootToken != null) {
			Token emptyToken = rootToken.getToken();
			tokenSequence.removeEmptyToken(emptyToken);
			this.remove(0);
			tokenSequence.setWithRoot(false);
			tokenSequence.finalise();
		}
	}

	public PosTaggerServiceInternal getPosTaggerServiceInternal() {
		return posTaggerServiceInternal;
	}

	public void setPosTaggerServiceInternal(PosTaggerServiceInternal posTaggerServiceInternal) {
		this.posTaggerServiceInternal = posTaggerServiceInternal;
	}

	@Override
	public List<Decision> getDecisions() {
		return this.decisions;
	}

	@Override
	public List<Solution> getUnderlyingSolutions() {
		return this.underlyingSolutions;
	}

	@Override
	public void addDecision(Decision decision) {
		this.decisions.add(decision);
	}

	@Override
	@SuppressWarnings("rawtypes")
	public ScoringStrategy getScoringStrategy() {
		return scoringStrategy;
	}

	@Override
	public void setScoringStrategy(@SuppressWarnings("rawtypes") ScoringStrategy scoringStrategy) {
		this.scoringStrategy = scoringStrategy;
	}

	void setTokenSequence(TokenSequence tokenSequence) {
		this.tokenSequence = tokenSequence;
		if (tokenSequence.getUnderlyingAtomicTokenSequence() != null) {
			this.underlyingSolutions.add(tokenSequence.getUnderlyingAtomicTokenSequence());
		}
		tokenSequence.setPosTagSequence(this);
	}

	@Override
	public void addPosTaggedToken(PosTaggedToken posTaggedToken) {
		this.add(posTaggedToken);
		posTaggedToken.setPosTagSequence(this);
		this.string = null;
	}

	@Override
	public void removeEmptyPosTaggedTokens() {
		boolean haveEmptyTokens = false;
		for (PosTaggedToken posTaggedToken : this) {
			if (posTaggedToken.getToken().isEmpty() && posTaggedToken.getTag().isEmpty()) {
				haveEmptyTokens = true;
				break;
			}
		}

		if (haveEmptyTokens) {
			List<PosTaggedToken> cloneList = new ArrayList<PosTaggedToken>();
			this.tokenSequence = this.tokenSequence.cloneTokenSequence();

			List<Token> emptyTokensToRemove = new ArrayList<Token>();
			int i = 0;
			for (PosTaggedToken posTaggedToken : this) {
				Token token = tokenSequence.get(i++);
				if (posTaggedToken.getToken().isEmpty() && posTaggedToken.getTag().isEmpty()) {
					if (LOG.isDebugEnabled())
						LOG.debug("Removing null empty pos-tagged token at position " + posTaggedToken.getToken().getStartIndex() + ": " + posTaggedToken);
					emptyTokensToRemove.add(token);
				} else {
					PosTaggedToken clonedTaggedToken = posTaggedToken.clonePosTaggedToken();
					cloneList.add(clonedTaggedToken);
				}
			}

			for (Token token : emptyTokensToRemove) {
				this.tokenSequence.removeEmptyToken(token);
			}
			this.tokenSequence.finalise();

			i = 0;
			for (PosTaggedToken posTaggedToken : cloneList) {
				posTaggedToken.setToken(this.tokenSequence.get(i++));
			}

			this.clear();
			this.addAll(cloneList);
			this.string = null;
		}
	}

	@Override
	public PosTagSequence clonePosTagSequence() {
		PosTagSequence clone = new PosTagSequenceImpl(this, true);
		return clone;
	}

	public TalismaneService getTalismaneService() {
		return talismaneService;
	}

	public void setTalismaneService(TalismaneService talismaneService) {
		this.talismaneService = talismaneService;
	}

	public MachineLearningService getMachineLearningService() {
		return machineLearningService;
	}

	public void setMachineLearningService(MachineLearningService machineLearningService) {
		this.machineLearningService = machineLearningService;
	}

}
