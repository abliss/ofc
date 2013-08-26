import java.util.List;

import org.pokersource.game.Deck;

import com.google.common.collect.Lists;


public class CardSetUtils {
	// Cache of cardMask by rank and suit
	private static final long[][] CARD_MASKS;
	private static final OfcCard[][] CARDS;

	static {
		CARD_MASKS = new long[13][4];
		CARDS = new OfcCard[13][4];
		for (int r = Deck.RANK_ACE; r >= 0; r--) {	
			for (int s = Deck.SUIT_COUNT - 1; s >= 0; s--) {
				long mask = Deck.createCardMask(r, s);
				CARD_MASKS[r][s] = mask;
				CARDS[r][s] = new OfcCard(mask);
			}
		}
	}
	
	/**
	 * Convert the mask to arrays of ranks and suits in descending rank order
	 */
	public static void convertToArrays(long mask, int[] ranks, int[] suits) {
		int index = 0;
		for (int r = Deck.RANK_ACE; r >= 0; r--) {	
			for (int s = Deck.SUIT_COUNT - 1; s >= 0; s--) {
				long m = CARD_MASKS[r][s];
				if ((mask & m) != 0) {
					ranks[index] = r;
					suits[index] = s;
					index++;
		        }
			}
		}
	}

	public static OfcCard[] asCards(long mask) {
		OfcCard[] cards = new OfcCard[Long.bitCount(mask)];
		int i = 0;
		for (int r = Deck.RANK_ACE; r >= 0; r--) {	
			for (int s = Deck.SUIT_COUNT - 1; s >= 0; s--) {
				long m = CARD_MASKS[r][s];
				if ((mask & m) != 0) {
					cards[i++] = CARDS[r][s];
		        }
			}
		}
		return cards;
	}
	
}
