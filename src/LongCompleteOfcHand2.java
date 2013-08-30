import org.pokersource.game.Deck;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import java.util.HashMap;

public class LongCompleteOfcHand2 extends LongOfcHand2 implements CompleteOfcHand
	{

	public LongCompleteOfcHand2(long board) {
		super(board);
		if (!super.isComplete()) {
			throw new IllegalStateException("Hand too empty.");
		}
	}

	public LongCompleteOfcHand2(LongOfcHand2 source) {
		super(source);
		if (!source.isComplete()) {
			throw new IllegalStateException("Hand too empty.");
		}
	}
	
	public boolean isComplete() {
		throw new RuntimeException("Why are you asking?!");
		//return true;
	}

	public static boolean isFouled(long board) {
		return (0 != (board & MASK_FOULED));
	}
	public boolean isFouled() {
		return isFouled(this.board);
	}

	public int getStreet() {
		return 14;
	}

	/* 
	 * TODO: this really belongs in AbstractOfcHand
	 * @see OfcHand#getRoyaltyValue()
	 */
	public int getRoyaltyValue() {
		return getRoyaltyValue(this.board);
	}

	public static int getRoyaltyValue(long board) {
		if (isFouled(board)) {
			return 0;
		}
		int value = 0;
		
		// Stupid integer division hack to zero out all the insignificant digits so we can use a map to look up
		// royalty values
		long rank = getBackRank(board) / StupidEval.ONE_PAIR * StupidEval.ONE_PAIR;
		if (backRoyaltyMap.containsKey(rank)) {
			value += backRoyaltyMap.get(rank);
		}
		rank = getMiddleRank(board) / StupidEval.ONE_PAIR * StupidEval.ONE_PAIR;
		if (backRoyaltyMap.containsKey(rank)) {
			value += backRoyaltyMap.get(rank) * 2;
		}
		
		rank = getFrontRank(board);
		if (rank >= StupidEval.TRIPS) {
			rank -= StupidEval.TRIPS;
			// StupidEval implementation is to leave only the rank of the card here.  Deuce = 0, per Deck constants
			// Yes, this is super lame. 15 points for 222, one more for every higher rank.
			value += 15 + rank;
		} else if (rank >= StupidEval.ONE_PAIR) {
			// More stupid implementation dependent details.  Subtract out the ONE_PAIR constant, integer divide
			// the kickers away, get left with the rank of the pair based on Deck constants.  66 = 5.
			rank -= StupidEval.ONE_PAIR;
			rank /= StupidEval.PAIR_CONSTANT;
			if (rank >= 5) {
				value += rank - 4;
			}
		}
	
		return value;
	}

	public int getFantasylandValue() {
		return getFantasylandValue(this.board);
	}

	public static int getFantasylandValue(long board) {
		if (getFrontRank(board) > StupidEval.FANTASYLAND_THRESHOLD) {
			return FANTASYLAND_VALUE;
		}
		return 0;
	}
	
	public static Score scoreAgainst(long board, long otherBoard) {
		if (isFouled(board)) {
			if (isFouled(otherBoard)) {
				return Score.ZERO;
			}
			int score = -6 - getRoyaltyValue(otherBoard);
			return new Score(score, score - getFantasylandValue(otherBoard));
		}
		if (isFouled(otherBoard)) {
			int score = 6 + getRoyaltyValue(board);
			return new Score(score, getFantasylandValue(board));
		}
		int wins = 0;
		if (getBackRank(board) > getBackRank(otherBoard)) {
			wins++;
		}
		if (getMiddleRank(board) > getMiddleRank(otherBoard)) {
			wins++;
		}
		if (getFrontRank(board) > getFrontRank(otherBoard)) {
			wins++;
		}
		
		int score = getRoyaltyValue(board) - getRoyaltyValue(otherBoard);
		int flValue = getFantasylandValue(board) - getFantasylandValue(otherBoard);
		switch (wins) {
			case 0:
				score -= 6;
				break;
			case 1:
				score -= 1;
				break;
			case 2:
				score += 1;
				break;
			case 3:
				score += 6;
				break;
			default:
				throw new IllegalStateException("wtf");
		}
		return new Score(score, score + flValue);

	}
	

}
