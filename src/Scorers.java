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
		int score(OfcHandMatrix matrix);

		String getCacheFile();

		String getKey();
	}
	
	private static abstract class AbstractScorer implements Scorer {
		// NOTE: actually never mind, these must be the same. Don't mess with
		// this
		public String getKey() {
			return getCacheFile();
		}

		// Base royalty value -- use for back, or multiply by 2 for middle.
		public final int getBaseRoyaltyValue(int player, int hand,
											 OfcHandMatrix matrix) {
			int total = 0;
			int count = 0;
			count = matrix.numHandsOfRank(player, hand, StupidEval.STRAIGHT);
			if (count == 0) return total;
			total += 2 * count;  // straight = 2
			count = matrix.numHandsOfRank(player, hand, StupidEval.FLUSH);
			if (count == 0) return total;
			total += 2 * count;  // flush = 4
			count = matrix.numHandsOfRank(player, hand, StupidEval.FULL_HOUSE);
			if (count == 0) return total;
			total += 2 * count; // full house = 6
			count = matrix.numHandsOfRank(player, hand, StupidEval.QUADS);
			if (count == 0) return total;
			total += 4 * count; // quads = 10
			count = matrix.numHandsOfRank(player, hand, StupidEval.STRAIGHT_FLUSH);
			if (count == 0) return total;
			total += 5 * count; // quads = 15
			count = matrix.numHandsOfRank(player, hand, StupidEval.ROYAL_FLUSH);
			if (count == 0) return total;
			total += 10 * count; // royal = 25
			return total;
		}
		
		// Front royalty
		public final int getFrontRoyaltyValue(int player, OfcHandMatrix matrix) {
			int total = 0;
			int count = 0;
			// 1 point for 66, 1 additional point for each rank over
			for (int rank = 4; rank < 13; rank++) {
				count = matrix.numHandsOfRank(player, 0,
											  StupidEval.FRONT_PAIR_RANKS[rank]);
				if (count == 0) return total;
				total += count;
			}
			// 222 is worth 10 points, one more than AA
			// Each additional rank of trips is worth 1 point.
			for (int rank = 0; rank < 13; rank++) {
				count = matrix.numHandsOfRank(player, 0,
											  StupidEval.FRONT_TRIP_RANKS[rank]);
				if (count == 0) return total;
				total += count;
			}
			return total;
		}
		
		public final int score(OfcHandMatrix matrix) {
			int total = 0;
			total += FANTASYLAND_VALUE *
				(matrix.numHandsOfRank(0, 0, StupidEval.FANTASYLAND_THRESHOLD) -
				 matrix.numHandsOfRank(1, 0, StupidEval.FANTASYLAND_THRESHOLD));

			total +=
				getBaseRoyaltyValue(0, 2, matrix) -
				getBaseRoyaltyValue(1, 2, matrix);
			total += 2 * (
				getBaseRoyaltyValue(0, 1, matrix) -
				getBaseRoyaltyValue(1, 1, matrix));
			total +=
				getFrontRoyaltyValue(0, matrix) -
				getFrontRoyaltyValue(1, matrix);
			total += matrix.numP1Wins(0) * SCOOPED_VALUE;
			total += matrix.numP1Wins(1) * -1;
			total += matrix.numP1Wins(2) * 1;
			total += matrix.numP1Wins(3) * SCOOPING_VALUE;

			return total;
		}
	}

	static final class NewFantasylandScorer extends AbstractScorer {
		@Override
		public String getCacheFile() {
			return "new-fantasyland.txt";
		}
	}

	private static final Scorer[] NEW_FL_SCORER = new Scorer[] {
		new NewFantasylandScorer()};
	
	public static Scorer[] getScorers() {
		return NEW_FL_SCORER;
	}
}
