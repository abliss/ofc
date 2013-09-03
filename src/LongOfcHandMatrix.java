public class LongOfcHandMatrix implements OfcHandMatrix {
	// Size of the deck before we started dealing
	private final int deckSize;
	private final int[] winCount = new int[4];
	// How many cards out of deckSize foul player[i]'s hand
	private final int[] foulCount = new int[2];
	// varies[player][subhand] tells whether that subhand varies.
	private final boolean[][] varies = new boolean[2][3];
	// foul[player][i] is whether that hand is fouled.
	private final boolean[][] isFouled;
	// cached rank[player][cardIndex][subhand], 0 for foul
	private final long[][][] ranks;
	public LongOfcHandMatrix(OfcHand p1, OfcHand p2, OfcCard[] cards) {
		OfcHand[] startHands = new OfcHand[] {p1, p2};
		deckSize = cards.length;
		ranks = new long[2][deckSize][3];
		isFouled = new boolean[2][deckSize]; // TODO:PICKUP
		for (int p = 0; p < 2; p++) {
			varies[p][0] = startHands[p].getFrontSize() < OfcHand.FRONT_SIZE;
			varies[p][1] = startHands[p].getMiddleSize() < OfcHand.MIDDLE_SIZE;
			varies[p][2] = startHands[p].getBackSize() < OfcHand.BACK_SIZE;
			for (int i = cards.length - 1; i >= 0; i--) {
				CompleteOfcHand hand = startHands[p].generateOnlyHand(cards[i]);
				boolean foul = hand.isFouled();
				isFouled[p][i] = foul;
				if (foul) {
					foulCount[p]++;
				}
				for (int handNum = 0; handNum < 3; handNum++) {
					ranks[p][i][handNum] = getRank(hand, handNum);
				}
			}
		}

		for (int i = cards.length - 1; i >= 0; i--) {
			if (isFouled[0][i]) {
				// P1 fouled. Set winCount 0 for everywhere p2 didn't foul.
				winCount[0] += (deckSize - 1 - foulCount[1]);
				// foulCount will be off by one for this if it includes the case
				// where p2 got the same card.
				if (isFouled[1][i]) {
					winCount[0]++;
				}
			} else {
				for (int j = cards.length - 1; j >= 0; j--) {
					if (i != j) {
						if (isFouled[1][j]) {
							winCount[3]++;
						} else {
							int wins = 0;
							for (int handNum = 0; handNum < 3; handNum++) {
								if (ranks[0][i][handNum] >
									ranks[1][j][handNum]) {
									wins++;
								}
							}
							winCount[wins]++;
						}
					}
				}
			}
		}
	}
	
	private static long getRank(OfcHand hand, int handNum) {
		switch(handNum) {
		case 0:
			return hand.getFrontRank();
		case 1:
			return hand.getMiddleRank();
		case 2:
			return hand.getBackRank();
		default:
			throw new IllegalArgumentException("bad handnum " + handNum);
		}

	}
	@Override
	public int numHandsOfRank(int player, int handNum, long rank) {
		// shortcut if hand is always fouled
		if (foulCount[player] == deckSize) {
			return 0;
		}
		int count = 0;
		
		if (!varies[player][handNum]) {
			if (ranks[player][0][handNum] >= rank) {
				return (deckSize - foulCount[player]) * (deckSize - 1);
			} else {
				return 0;
			}
		} else {
			for (int i = 0; i < deckSize; i++) {
				if (!isFouled[player][i] &&
					(ranks[player][i][handNum] >= rank)) {
					count += deckSize - 1;
				}
			}
		}
		return count;
	}
	
	@Override
	public int numP1Wins(int wins) {
		return winCount[wins];
	}
}
