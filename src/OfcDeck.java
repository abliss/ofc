import java.util.List;

import org.pokersource.game.Deck;

import com.google.common.collect.Lists;

public class OfcDeck {

	private long cardMask;
    public static final int FULL_SIZE = 52;
	
	public OfcDeck() {
		cardMask = 0L;
	}
	
	// copy a deck from a card mask
	public OfcDeck(long cardMask) {
		this.cardMask = cardMask;
	}
	
    public OfcDeck initialize() {
		for (String rank : Lists.newArrayList("2", "3", "4", "5", "6", "7", "8", "9", "T", "J", "Q", "K", "A")) {
			for (String suit : Lists.newArrayList("c", "d", "h", "s")) {
				cardMask |= Deck.parseCardMask(rank + suit);
			}
		}
        return this;
	}

	public long getMask() {
		return cardMask;
	}
		
	public long withoutCard(OfcCard card) {
		return withoutCard(card.mask);
	}

	public long withoutCard(long cardBit) {
		if ((cardMask & cardBit) == 0) {
            throw new IllegalArgumentException(
                "Card " + Deck.cardMaskString(cardBit) + " not in deck");
		}

		return cardMask ^ cardBit;
	}
	
	public OfcCard removeCard(String cardString) {		
		OfcCard card = new OfcCard(Deck.parseCardMask(cardString));
		cardMask = withoutCard(card);
		return card;
	}
	
	public OfcCard[] asList() {
		return CardSetUtils.asCards(cardMask);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (cardMask ^ (cardMask >>> 32));
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
		OfcDeck other = (OfcDeck) obj;
		if (cardMask != other.cardMask)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return Deck.cardMaskString(cardMask);
	}
    
    public static OfcDeck fromString(String deckString) {
        return new OfcDeck(Deck.parseCardMask(deckString));
    }

    public int getSize() {
        return Long.bitCount(cardMask);
    }
}
