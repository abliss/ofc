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
		 * Score a square matrix of final hands. Evaluates all possible pairs
		 * except the diagonal; returns the sum of first against second.
		 */
		int score(CompleteOfcHand[] first, CompleteOfcHand[] second);

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
		protected abstract Map<Long, Integer> getBackRoyaltyMap();

		// NOTE: actually never mind, these must be the same. Don't mess with
		// this
		public String getKey() {
			return getCacheFile();
		}

		public final int getBackValue(CompleteOfcHand hand) {
			Map<Long, Integer> backRoyaltyMap = getBackRoyaltyMap();

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
			
			/*
			if (backRoyaltyMap.containsKey(rank)) {
				return backRoyaltyMap.get(rank);
			}
			*/
			return 0;
		}
		
		public final int getMiddleValue(CompleteOfcHand hand) {
			Map<Long, Integer> backRoyaltyMap = getBackRoyaltyMap();

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

			/*
			 * if (backRoyaltyMap.containsKey(rank)) {
			 
				return backRoyaltyMap.get(rank) * 2;
			}
			*/
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
		
		public final int score(CompleteOfcHand[] first, CompleteOfcHand[] second) {
			int numHands = first.length;
			if (numHands != second.length) {
				throw new IllegalArgumentException("Need square matrix!");
			}
			int total = 0;
			for (int i = numHands - 1; i >= 0; i--) {
				if (first[i].isFouled()) {
					for (int j = numHands - 1; j >= 0; j--) {
						if (i != j) {
							if (!second[j].isFouled()) {
								total += SCOOPED_VALUE - getRoyaltyValue(second[j])
									- getFantasylandValue(second[j]);
							}
						}
					}
				} else {
					total += (numHands - 1) * 
						(getRoyaltyValue(first[i]) + getFantasylandValue(first[i]));
					for (int j = numHands - 1; j >= 0; j--) {
						if (i != j) {
							if (second[j].isFouled()) {
								total += SCOOPING_VALUE;
							} else {
								total -= getRoyaltyValue(second[j]);
								total -= getFantasylandValue(second[j]);

								int wins = 0;
								if (first[i].getBackRank() > second[j].getBackRank()) {
									wins++;
								}
								if (first[i].getMiddleRank() > second[j].getMiddleRank()) {
									wins++;
								}
								if (first[i].getFrontRank() > second[j].getFrontRank()) {
									wins++;
								}

								switch (wins) {
								case 0:
									total += SCOOPED_VALUE;
									break;
								case 1:
									total -= 1;
									break;
								case 2:
									total += 1;
									break;
								case 3:
									total += SCOOPING_VALUE;
									break;
								default:
									throw new IllegalStateException("wtf");
								}
							}
						}
					}
				}
			}
			return total;
		}
	}

	static class OldScorer extends AbstractScorer {
		protected Map<Long, Integer> getBackRoyaltyMap() {
			return OLD_BACK_ROYALTY_MAP;
		}

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
		protected Map<Long, Integer> getBackRoyaltyMap() {
			return NEW_BACK_ROYALTY_MAP;
		}

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
