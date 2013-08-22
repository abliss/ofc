import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableMap;

public interface OfcHand {

	public static final int BACK_SIZE = 5;
	public static final int MIDDLE_SIZE = 5;
	public static final int FRONT_SIZE = 3;

	public static final int FANTASYLAND_VALUE = 12;

	static final Map<Long, Integer> backRoyaltyMap = ImmutableMap.<Long, Integer>builder()
			.put(StupidEval.STRAIGHT, 2)
			.put(StupidEval.FLUSH, 4)
			.put(StupidEval.FULL_HOUSE, 6)
			.put(StupidEval.QUADS, 8)
			.put(StupidEval.STRAIGHT_FLUSH, 10)
			.put(StupidEval.ROYAL_FLUSH, 20)
			.build();

	static final Map<Long, Integer> backRoyaltyMap2 = ImmutableMap.<Long, Integer>builder()
			.put(StupidEval.STRAIGHT, 2)
			.put(StupidEval.FLUSH, 4)
			.put(StupidEval.FULL_HOUSE, 6)
			.put(StupidEval.QUADS, 10)
			.put(StupidEval.STRAIGHT_FLUSH, 15)
			.put(StupidEval.ROYAL_FLUSH, 25)
			.build();
	
	OfcHand copy();

	Set<OfcHand> generateHands(OfcCard card);

	/**
	 * For a hand on the last street, slot the given card in the only available
	 * location.
	 * @throw IllegalStateException if there is more than one place for the card to go.
	 */
	OfcHand generateOnlyHand(OfcCard card);

	void addBack(OfcCard card);
	void addMiddle(OfcCard card);
	void addFront(OfcCard card);
	/**
	 * QsQdQh/KsKdKhKc/AsAcAdAh2h
	 * @param hand
	 */
	void setHand(String hand, OfcDeck deck);
	
	int getBackSize();
	int getMiddleSize();
	int getFrontSize();
	
	long getFrontRank();
	long getMiddleRank();
	long getBackRank();

	boolean isComplete();

	// use for pruning the search space
	boolean willBeFouled();

	boolean isFouled();

	int getRoyaltyValue();
	int getFantasylandValue();
	
	Score scoreAgainst(OfcHand other);

	int getStreet();
	
	/**
	 * Convert this hand to a less human readable but more compact string for storage purposes.
	 */
	String toKeyString();
}