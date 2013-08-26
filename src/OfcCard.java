import org.pokersource.game.Deck;

import com.google.common.annotations.VisibleForTesting;

public class OfcCard {

	// One bit should be set, as defined in Deck
	public final long mask;
	public final int suit;
	public final int rank;
	
	public OfcCard(long mask) {
		if (Long.bitCount(mask) != 1) {
			throw new IllegalArgumentException("Only one bit should be set");
		}
		this.mask = mask;
		this.suit = getSuit();
		this.rank = getRank();
	}

	@VisibleForTesting
	OfcCard(String cardString) {
		this(Deck.parseCardMask(cardString));
	}
	
	// actually this is the index from the right side
	private int getIndex() {
		return Long.numberOfTrailingZeros(mask);
	}
	
	public int getRank() {
		switch (getIndex() % 13) {
			case 0:
				return Deck.RANK_2;
			case 1:
				return Deck.RANK_3;
			case 2:
				return Deck.RANK_4;
			case 3:
				return Deck.RANK_5;
			case 4:
				return Deck.RANK_6;
			case 5:
				return Deck.RANK_7;
			case 6:
				return Deck.RANK_8;
			case 7:
				return Deck.RANK_9;
			case 8:
				return Deck.RANK_TEN;
			case 9:
				return Deck.RANK_JACK;
			case 10:
				return Deck.RANK_QUEEN;
			case 11:
				return Deck.RANK_KING;
			case 12:
				return Deck.RANK_ACE;
			default:
				throw new IllegalStateException("No rank");
		}
	}
	
	public int getSuit() {
		switch (getIndex() / 13) {
			case 0:
				return Deck.SUIT_HEARTS;
			case 1:
				return Deck.SUIT_DIAMONDS;
			case 2:
				return Deck.SUIT_CLUBS;
			case 3:
				return Deck.SUIT_SPADES;
			default:
				throw new IllegalStateException("No suit");
		}
	}

	public String toString() {
		return Deck.cardMaskString(mask);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (mask ^ (mask >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		OfcCard other = (OfcCard) obj;
		if (mask != other.mask)
			return false;
		return true;
	}
	
}
