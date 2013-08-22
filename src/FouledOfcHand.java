import org.pokersource.game.Deck;

public class FouledOfcHand extends CachedValueOfcHand {

	int frontSize;
	int middleSize;
	int backSize;
	
	
	public FouledOfcHand(int front, int middle, int back) {
		super();
		frontSize = front;
		middleSize = middle;
		backSize = back;
	}
	
	public FouledOfcHand(OfcHand source) {
		super();
		this.frontSize = source.getFrontSize();
		this.middleSize = source.getMiddleSize();
		this.backSize = source.getBackSize();
	}
	
	@Override
	public FouledOfcHand copy() {
		return new FouledOfcHand(this);
	}

	@Override
	public int getBackSize() {
		return backSize;
	}
	
	@Override
	public int getMiddleSize() {
		return middleSize;
	}
	
	@Override
	public int getFrontSize() {
		return frontSize;
	}

	@Override
	public void addBack(OfcCard card) {
		if (getBackSize() >= BACK_SIZE) {
			throw new IllegalStateException("Back already full");
		}
		backSize++;		
		if (getBackSize() == BACK_SIZE) {
			completeBack();
		}
	}

	@Override
	public void addMiddle(OfcCard card) {
		if (getMiddleSize() >= MIDDLE_SIZE) {
			throw new IllegalStateException("Middle already full");
		}
		middleSize++;
		if (getMiddleSize() == MIDDLE_SIZE) {
			completeMiddle();
		}
	}

	@Override
	public void addFront(OfcCard card) {
		if (getFrontSize() >= FRONT_SIZE) {
			throw new IllegalStateException("Front already full");
		}
		frontSize++;
		if (getFrontSize() == FRONT_SIZE) {
			completeFront();
		}
	}

	@Override
	public boolean isComplete() {
		return getFrontSize() == FRONT_SIZE &&
				getMiddleSize() == MIDDLE_SIZE &&
				getBackSize() == BACK_SIZE;
	}

	@Override
	public boolean willBeFouled() {
		return true;
	}

	@Override
	public boolean isFouled() {
		if (!isComplete()) {
			throw new IllegalStateException("Hand not complete");
		}
		return true;
	}

	@Override
	public long getFrontRank() {
		return 0L;
	}

	@Override
	public long getMiddleRank() {
		return 0L;
	}

	@Override
	public long getBackRank() {
		return 0L;
	}

	@Override
	public int getStreet() {
		return getFrontSize() + getMiddleSize() + getBackSize() + 1;
	}

	@Override
	public String toKeyString() {
		StringBuilder sb = new StringBuilder();
		sb.append("F-");
		sb.append(getFrontSize());
		sb.append("-");
		sb.append(getMiddleSize());
		sb.append("-");
		sb.append(getBackSize());
		return sb.toString();
	}

	public static FouledOfcHand fromKeyString(String s) {
		String[] sizes = s.split("-");
		if (sizes.length == 4) {
			return new FouledOfcHand(Integer.parseInt(sizes[1]),
									 Integer.parseInt(sizes[2]),
									 Integer.parseInt(sizes[3]));
		} else {
			throw new IllegalArgumentException("format incorrect");
		}
	}
	

	@Override
	public String toString() {
		return this.toKeyString();
	}
	
	@Override
	public void setHand(String handString, OfcDeck deck) {
		String[] hands = handString.split("/");
		if (hands.length != 3) {
			throw new IllegalArgumentException("Must have 3 hands");
		}
		frontSize = hands[0].length() / 2;
		middleSize = hands[1].length() / 2;
		backSize = hands[2].length() / 2;

	}
	
	@Override
	public int hashCode() {
	/*
		if (true) {
			return toKeyString().hashCode();
		}
		*/
		int result = 0;
		result |= backSize;
		result |= middleSize << 3;
		result |= frontSize << 6;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof FouledOfcHand)) {
			return false;
		}
		OfcHand other = (OfcHand) obj;
		if (backSize != other.getBackSize())
			return false;
		if (frontSize != other.getFrontSize())
			return false;
		if (middleSize != other.getMiddleSize())
			return false;
		return  true;
	}

}
