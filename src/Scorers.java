import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public class Scorers {
	public static final boolean DEBUG = false;

	private static final Map<Long, Integer> OLD_BACK_ROYALTY_MAP = ImmutableMap
			.<Long, Integer> builder()
			.put(StupidEval.STRAIGHT, 2)
			.put(StupidEval.FLUSH, 4)
			.put(StupidEval.FULL_HOUSE, 6)
			.put(StupidEval.QUADS, 8)
			.put(StupidEval.STRAIGHT_FLUSH, 10)
			.put(StupidEval.ROYAL_FLUSH, 20).build();

	private static final Map<Long, Integer> NEW_BACK_ROYALTY_MAP = ImmutableMap
			.<Long, Integer> builder()
			.put(StupidEval.STRAIGHT, 2)
			.put(StupidEval.FLUSH, 4)
			.put(StupidEval.FULL_HOUSE, 6)
			.put(StupidEval.QUADS, 10)
			.put(StupidEval.STRAIGHT_FLUSH, 15)
			.put(StupidEval.ROYAL_FLUSH, 25).build();

	private static final int SCOOPING_VALUE = 6;
	private static final int SCOOPED_VALUE = -6;
	static final int FANTASYLAND_VALUE = 12;

	public interface Scorer {
		/**
		 * Return the value of p1's hand scored against p2
		 */
		int score(CompleteOfcHand first, CompleteOfcHand second);

		String getCacheFile();

		String getKey();
		
		// These are just to make testing easier
		/**
		 * Return 0 if not a FL scorer
		 */
		int getFantasylandValue(CompleteOfcHand hand);
		int getRoyaltyValue(CompleteOfcHand hand);
		int getFrontValue(CompleteOfcHand hand);
		int getMiddleValue(CompleteOfcHand hand);
		int getBackValue(CompleteOfcHand hand);
	}
	
	private static abstract class AbstractScorer implements Scorer {
		// NOTE: actually never mind, these must be the same. Don't mess with
		// this
		public String getKey() {
			return getCacheFile();
		}

		public final int getBackValue(long hand) {
			// Stupid integer division hack to zero out all the insignificant
			// digits so we can use a map to look up
			// royalty values
			long rank = hand.getBackRank() / StupidEval.ONE_PAIR
					* StupidEval.ONE_PAIR;
			
			if (rank == StupidEval.STRAIGHT) {
				return 2;
			} else if (rank == StupidEval.FLUSH) {
				return 4;
			} else if (rank == StupidEval.FULL_HOUSE){
				return 6;
			} else if (rank == StupidEval.QUADS) {
				return 10;
			} else if (rank == StupidEval.STRAIGHT_FLUSH) {
				return 15;
			} else if (rank == StupidEval.ROYAL_FLUSH) {
				return 25;
			}
			
			return 0;
		}
		
		public final int getMiddleValue(long hand) {
			// Stupid integer division hack to zero out all the insignificant
			// digits so we can use a map to look up
			// royalty values
			long rank = hand.getMiddleRank() / StupidEval.ONE_PAIR
					* StupidEval.ONE_PAIR;
			if (rank == StupidEval.STRAIGHT) {
				return 4;
			} else if (rank == StupidEval.FLUSH) {
				return 8;
			} else if (rank == StupidEval.FULL_HOUSE){
				return 16;
			} else if (rank == StupidEval.QUADS) {
				return 20;
			} else if (rank == StupidEval.STRAIGHT_FLUSH) {
				return 30;
			} else if (rank == StupidEval.ROYAL_FLUSH) {
				return 50;
			}

			return 0;
		}

		public final int getFrontValue(CompleteOfcHand hand) {
			long rank = hand.getFrontRank();
			if (rank >= StupidEval.TRIPS) {
				rank -= StupidEval.TRIPS;
				// StupidEval implementation is to leave only the rank of the
				// card here. Deuce = 0, per Deck constants
				// Yes, this is super lame. 10 points for 222, one more for
				// every higher rank.
				return (int) (10 + rank);
			} else if (rank >= StupidEval.ONE_PAIR) {
				// More stupid implementation dependent details. Subtract out
				// the ONE_PAIR constant, integer divide
				// the kickers away, get left with the rank of the pair based on
				// Deck constants. 66 = 5.
				rank -= StupidEval.ONE_PAIR;
				rank /= StupidEval.PAIR_CONSTANT;
				if (rank >= 4) {
					return (int) (rank - 3);
				}
			}
			return 0;
		}
		
		public final int getRoyaltyValue(CompleteOfcHand hand) {
			if (hand.isFouled()) {
				return 0;
			}

			return getBackValue(hand) + getMiddleValue(hand) + getFrontValue(hand);
		}

		protected final boolean isFantasyland(CompleteOfcHand hand) {
			return !hand.isFouled() && hand.getFrontRank() >= StupidEval.FANTASYLAND_THRESHOLD;
		}
		
		public final int score(CompleteOfcHand first, CompleteOfcHand second) {
			if (first.isFouled()) {
				if (second.isFouled()) {
					return 0;
				}
				return SCOOPED_VALUE - getRoyaltyValue(second)
						- getFantasylandValue(second);
			}
			if (second.isFouled()) {
				return SCOOPING_VALUE + getRoyaltyValue(first)
						+ getFantasylandValue(first);
			}
			int wins = 0;
			if (first.getBackRank() > second.getBackRank()) {
				wins++;
			}
			if (first.getMiddleRank() > second.getMiddleRank()) {
				wins++;
			}
			if (first.getFrontRank() > second.getFrontRank()) {
				wins++;
			}

			int score = getRoyaltyValue(first) - getRoyaltyValue(second);
			int flValue = getFantasylandValue(first) - getFantasylandValue(second);
			switch (wins) {
			case 0:
				score += SCOOPED_VALUE;
				break;
			case 1:
				score -= 1;
				break;
			case 2:
				score += 1;
				break;
			case 3:
				score += SCOOPING_VALUE;
				break;
			default:
				throw new IllegalStateException("wtf");
			}
			return score + flValue;

		}
	}

	static class OldScorer extends AbstractScorer {
		@Override
		public int getFantasylandValue(CompleteOfcHand hand) {
			return 0;
		}

		@Override
		public String getCacheFile() {
			return "old.txt";
		}
	}

	static final class OldFantasylandScorer extends OldScorer {
		@Override
		public int getFantasylandValue(CompleteOfcHand hand) {
			if (isFantasyland(hand)) {
				return FANTASYLAND_VALUE;
			}
			return 0;
		}

		@Override
		public String getCacheFile() {
			return "old-fantasyland.txt";
		}
	}

	static class NewScorer extends AbstractScorer {
		@Override
		public int getFantasylandValue(CompleteOfcHand hand) {
			return 0;
		}

		@Override
		public String getCacheFile() {
			return "new.txt";
		}
	}

	static final class NewFantasylandScorer extends NewScorer {
		@Override
		public int getFantasylandValue(CompleteOfcHand hand) {
			if (isFantasyland(hand)) {
				return FANTASYLAND_VALUE;
			}
			return 0;
		}

		@Override
		public String getCacheFile() {
			return "new-fantasyland.txt";
		}
	}

	private static final List<Scorer> SCORERS = ImmutableList.<Scorer> of(
			new OldScorer(), new OldFantasylandScorer(),
			new NewScorer(), new NewFantasylandScorer());

	private static final List<Scorer> OLD_SCORERS = ImmutableList
			.<Scorer> of(new OldScorer(), new OldFantasylandScorer());

	private static final List<Scorer> NEW_SCORERS = ImmutableList
			.<Scorer>of(new NewScorer(), new NewFantasylandScorer());
	
	private static final Scorer[] NEW_FL_SCORER = new Scorer[] {
		new NewFantasylandScorer()};
	
	public static Scorer[] getScorers() {
		return NEW_FL_SCORER;
	}
}
