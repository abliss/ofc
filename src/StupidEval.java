import java.util.Arrays;

import org.pokersource.game.Deck;

public class StupidEval {

	public static final long NO_PAIR =                                 0L;
	public static final long ONE_PAIR =                 1000000000000000L;
	public static final long TWO_PAIR =                 2000000000000000L;
	public static final long TRIPS =                    3000000000000000L;
	public static final long STRAIGHT =                 4000000000000000L;
	public static final long FLUSH =                    5000000000000000L;
	public static final long FULL_HOUSE =               6000000000000000L;
	public static final long QUADS =                    7000000000000000L;
	public static final long STRAIGHT_FLUSH =           8000000000000000L;
	public static final long ROYAL_FLUSH =              9000000000000000L;

	// public because OfcHand uses this.  So dumb.
	public static final long PAIR_CONSTANT =             10000000000000L;
	private static final long[] KICKER_MULTIPLIERS =
		new long[] {100000000000L,
					1000000000L,
					10000000L,
					100000L,
					1000L};

	// TODO: so very very sketchy
	public static final long FANTASYLAND_THRESHOLD =
		eval3(new int[] {Deck.RANK_QUEEN, Deck.RANK_QUEEN, Deck.RANK_2});
	
	// 3-card ranks for trips and the lowest possible pair, starting at deuce
	public static final long[] FRONT_PAIR_RANKS = new long[13];
	public static final long[] FRONT_TRIP_RANKS = new long[13];
	static {
		int[] ranks = new int[3];
		for (int rank = 0; rank < 13; rank++) {
			ranks[0] = ranks[1] = ranks[2] = rank;
			FRONT_TRIP_RANKS[rank] = eval3(ranks);
			if (rank == 0) {
				ranks[0] = 1;
			} else {
				ranks[2] = 0;
			}
			FRONT_PAIR_RANKS[rank] = eval3(ranks);
		}
	}
	
	// OfcHand is dependent on implementation details here, don't mess with them
	// TODO(abliss): still true?
	public static long eval3(int[] ranks) {
		if (ranks.length != 3 && ranks[3] != 0) {
			throw new IllegalArgumentException("Are you sure you only have 3 cards?");
		}
		if (ranks[0] == ranks[1] && ranks[1] == ranks[2]) {
			// TODO: kickers, maybe
			return TRIPS + ranks[0];
		}
		
		if (ranks[0] == ranks[1]) {
			return ONE_PAIR + ranks[1] * PAIR_CONSTANT + countKickers(ranks, 2);
		} else if (ranks[1] == ranks[2]) {
			return ONE_PAIR + ranks[1] * PAIR_CONSTANT + countKickers(ranks, 0);
		}
		
		return countKickers(ranks, 0, 1, 2);
	}
	
	// OfcHand is dependent on implementation details here, don't mess with them
	public static long eval(int[] ranks, int[] suits) {
		if (isFlush(suits)) {
			if (isStraight(ranks)) {
				if (ranks[0] == Deck.RANK_ACE){
					return ROYAL_FLUSH;
				}
				return STRAIGHT_FLUSH + ranks[0];
			}
			return FLUSH + countKickers(ranks);
		}

		// TODO: fix for kickers
		if (isQuads(ranks)) {
			return QUADS + ranks[3];
		}
		
		// TODO: fix for kickers
		if (isFullHouse(ranks)) {
			return FULL_HOUSE + ranks[2];
		}
		
		if (isStraight(ranks)) {
			return STRAIGHT + ranks[0];
		}
		
		// TODO: fix for kickers
		if (isTrips(ranks)) {
			return TRIPS + ranks[2];
		}
		
		long result = doCleverTwoPairThings(ranks);
		if (result != 0) {
			return result;
		}

		result = doCleverOnePairThings(ranks);
		if (result != 0) {
			return result;
		}
		
		return countKickers(ranks);
	}

	// sorted, no better hand, XXYYZ, XXZYY, ZXXYY
	private static long doCleverTwoPairThings(int[] ranks) {
		int kicker = -1;
		if (ranks[0] == ranks[1] && ranks[2] == ranks[3]) {
			kicker = ranks[4];
		}
		if (ranks[0] == ranks[1] && ranks[3] == ranks[4]) {
			kicker = ranks[2];
		}
		if (ranks[1] == ranks[2] && ranks[3] == ranks[4]) {
			kicker = ranks[0];
		}
		if (kicker != -1) {
			return TWO_PAIR + 10000 * ranks[1] + 100 * ranks[3] + kicker;
		}

		return 0;
	}
	
	// sorted, no better hand, etc.
	private static long doCleverOnePairThings(int[] ranks) {
		
		if (ranks[0] == ranks[1]) {
			// XXABC
			return ONE_PAIR + ranks[0] * PAIR_CONSTANT + countKickers(ranks, 2, 3, 4);
		} else if (ranks[1] == ranks[2]) {
			// AXXBC
			return ONE_PAIR + ranks[1] * PAIR_CONSTANT + countKickers(ranks, 0, 3, 4);
		} else if (ranks[2] == ranks[3]) {
			// ABXXC
			return ONE_PAIR + ranks[2] * PAIR_CONSTANT + countKickers(ranks, 0, 1, 4);
		} else if (ranks[3] == ranks[4]) {
			// ABCXX
			return ONE_PAIR + ranks[3] * PAIR_CONSTANT + countKickers(ranks, 0, 1, 2);
		}
		return 0;
	}
	
	// assume 5 sorted cards, either XXXYZ, YXXXZ, or YZXXX, no better hand possible
	// ignores kickers
	private static boolean isTrips(int[] ranks) {
		if (ranks[0] == ranks[1] && ranks[1] == ranks[2] ||
			ranks[1] == ranks[2] && ranks[2] == ranks[3] ||
			ranks[2] == ranks[3] && ranks[3] == ranks[4]) {
			return true;
		}
		return false;
	}
	
	// assume 5 sorted cards, either XXXYY or XXYYY, no better hand possible
	private static boolean isFullHouse(int[] ranks) {
		if (ranks[0] == ranks[1] && ranks[1] == ranks[2] && ranks[3] == ranks[4] ||
			ranks[0] == ranks[1] && ranks[2] == ranks[3] && ranks[3] == ranks[4]) {
			return true;
		}
		return false;
	}
	
	// assume 5 sorted cards, if middle 3 cards match and either end card matches, is quads
	private static boolean isQuads(int[] ranks) {
		if (ranks[1] == ranks[2] && ranks[2] == ranks[3] &&
			(ranks[0] == ranks[1] || ranks[3] == ranks[4])) {
			return true;		
		}
		return false;
	}
	
	private static boolean isFlush(int[] suits) {
		for (int i = 1; i < suits.length; i++) {
			if (suits[i - 1] != suits[i]) {
				return false;
			}
		}
		return true;
	}
	
	private static boolean isStraight(int[] ranks) {
		boolean mayBeHighStraight = true;
		for (int i = 1; i < ranks.length && mayBeHighStraight; i++) {
			if (ranks[i - 1] != ranks[i] + 1) {
				mayBeHighStraight = false;
			}
		}
		if (ranks[0] == Deck.RANK_ACE && ranks[1] == Deck.RANK_5 && ranks[2] == Deck.RANK_4 &&
			ranks[3] == Deck.RANK_3 && ranks[4] == Deck.RANK_2) {
			ranks[0] = Deck.RANK_5;
			return true;
		}
		
		return mayBeHighStraight;
	}

	private static long countKickers(int[] ranks, int... indices) {
		long count = 0;
		for (int i = 0; i < indices.length; i++) {
			count += ranks[indices[i]] * KICKER_MULTIPLIERS[i];
			if (count > PAIR_CONSTANT) {
				System.out.println(Arrays.toString(ranks));
				System.out.println(Arrays.toString(indices));
				System.out.println(count);
				System.out.println(Long.MAX_VALUE);
				throw new IllegalStateException("kickered imo");
			}
		}
		return count;
	}
	
	// all kickers in a 5 card hand
	private static long countKickers(int[] ranks) {
		return countKickers(ranks, 0, 1, 2, 3, 4);
	}
	
}
