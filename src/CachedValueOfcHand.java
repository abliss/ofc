import java.util.Set;

import com.google.common.collect.Sets;

public abstract class CachedValueOfcHand implements OfcHand {

	protected static final long UNSET = Long.MIN_VALUE;
	protected long backValue = UNSET;
	protected long middleValue = UNSET;
	protected long frontValue = UNSET;

	protected boolean willBeFouled = false;

	protected CachedValueOfcHand() {
		backValue = UNSET;
		middleValue = UNSET;
		frontValue = UNSET;
		willBeFouled = false;
	}
	
	// copy constructor
	protected CachedValueOfcHand(CachedValueOfcHand source) {
		this.backValue = source.backValue;
		this.middleValue = source.middleValue;
		this.frontValue = source.frontValue;
		this.willBeFouled = source.willBeFouled;
	}
	
	protected void completeBack() {
		backValue = getBackRank();
		if (middleValue != UNSET && backValue <= middleValue) {
			willBeFouled = true;
		}
		if (frontValue != UNSET && backValue < frontValue) {
			willBeFouled = true;
		}
	}
		
	protected void completeMiddle() {
		middleValue = getMiddleRank();
		if (frontValue != UNSET && middleValue < frontValue) {
			willBeFouled = true;
		}
		if (backValue != UNSET && backValue <= middleValue) {
			willBeFouled = true;
		}
	}
	
	protected void completeFront() {
		frontValue = getFrontRank();
		if (middleValue != UNSET && middleValue < frontValue) {
			willBeFouled = true;
		}
		if (backValue != UNSET && backValue < frontValue) {
			willBeFouled = true;
		}
	}
	
	/*
	 * TODO: again, AbstractOfcHand
	 * @see OfcHand#generateHands(OfcCard)
	 */
	@Override
	public Set<OfcHand> generateHands(OfcCard card) {
		Set<OfcHand> hands = Sets.newHashSetWithExpectedSize(3);
		if (getBackSize() < BACK_SIZE) {
			OfcHand copy = this.copy();
			copy.addBack(card);
			hands.add(copy);
			if (willBeFouled()) {
				// exit early if foul is guaranteed
				return hands;
			}
		}
		if (getMiddleSize() < MIDDLE_SIZE) {
			OfcHand copy = this.copy();
			copy.addMiddle(card);
			hands.add(copy);
			if (willBeFouled()) {
				return hands;
			}
		}
		if (getFrontSize() < FRONT_SIZE) {
			OfcHand copy = this.copy();
			copy.addFront(card);
			hands.add(copy);
		}
		return hands;
	}

}