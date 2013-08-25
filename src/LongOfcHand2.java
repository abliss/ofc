import org.pokersource.game.Deck;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;

public class LongOfcHand2 extends CachedValueOfcHand {

	@VisibleForTesting
	// The format for a subhand is a sequence of bits, from LSB to MSB, of 
	// 2 bits  : suit value 0-3 (if the hand is suited), 0 otherwise
	// 1 bit   : 0 if the hand is suited (or empty), 1 otherwise
	// 4n bits : Rank value 1-13, nonincreasing order, 0 if empty
	// the board packs in Back (15 bits), then Middle (23), then Front (23).
	// Note that an empty board is encoded as 0.
	long board;

	// Packing the subhands into the board...
	// bitshift offsets for the last bit in the hands
	// and masks for the hands, once shifted.
	// Includes suit bits and the unsuited bit.
	private static final int OFF_FRONT = 0;
	private static final long MASK_FRONT = 0x7fff;
	private static final int OFF_MIDDLE = 4*3+3;
	private static final long MASK_MIDDLE = 0x7fffff;
	private static final int OFF_BACK = OFF_MIDDLE + 4*5+3;
	private static final long MASK_BACK = 0x7fffff;

	// Within a subhand...
	// Offset/Mask to pull out the suit
	private static final int OFF_SUIT = 0;
	private static final long MASK_SUIT = 0x3;
	// Offset/Mask to pull out the suited flag
	private static final int OFF_UNSUITED = 2;
	private static final long MASK_UNSUITED = 0x1;
	// Offset/Mask to pull out a single rank
	private static final int OFF_FIRST_RANK = 3;
	private static final int OFF_RANK = 4;
	private static final long MASK_RANK = 0xf;

	// Cache for subhand-mask -> eval-result. Ignores suit.
	static final long[] EVAL_CACHE =
		new long[(int)(MASK_MIDDLE >>> OFF_UNSUITED)];

	public LongOfcHand2() {
		board = 0;
	}
	private LongOfcHand2(long board) {
		this.board = board;
	}	
	private LongOfcHand2(LongOfcHand2 source) {
		super(source);
		this.board = source.board;
	}

	/**
	 * Given a hand, count the number of nonzero card-ranks.  Since the ranks
	 * are listed in nonincreasing order, we can scan from the LSB to the MSB
	 * and stop at the first zero rank.
	 *
	 * Make sure you have masked out any higher bits!
	 */
	private static int countSize(long hand) {
		hand = hand >>> (OFF_FIRST_RANK);
		int count = 0;
		while ((hand & MASK_RANK) != 0) {
			count++;
			hand = hand >>> OFF_RANK;
		}
		return count;
	}

	/**
	 * Extract the ranks of a subhand into an array of descending ints
	 * @throws IllegalStateException if the hand is not full
	 */
	@VisibleForTesting
	static int[] getRanks(long hand, int size) {
		int[] ranks = new int[size];
		hand = hand >>> (OFF_FIRST_RANK);
		for (int i = 0; i < size; i++) {
			int rank = ((int)(hand & MASK_RANK)) - 1;
			if (rank < 0) {
				throw new IllegalStateException("Hand is that big!");
			}
			ranks[i] = rank;
			hand = hand >>> OFF_RANK;
		}
		return ranks;
	}
	
	@VisibleForTesting
	static boolean isSuited(long hand) {
		return ((hand >>> OFF_UNSUITED) & MASK_UNSUITED) == 0;
	}

	/**
	 * @return the suit of a hand, if it is suited. Undefined if not.
	 */
	private static int getSuit(long hand) {
		int suit = (int) ((hand >>> OFF_SUIT) & MASK_SUIT);
		return suit;
	}

	private static String toString(long hand) {
		StringBuffer sb = new StringBuffer();
		if (isSuited(hand)) {
			sb.append(getSuit(hand));
			sb.append(" ");
		} else {
			sb.append("U ");
		}
		int[] ranks = getRanks(hand, countSize(hand));
		for (Integer r : ranks) {
			sb.append(r);
			sb.append(",");
		}
		return sb.toString();
	}
	
	/**
	 * Add a card to a subhand and return the new subhand.
	 */
	private static long addCardToSubhand(long hand, OfcCard card) {
		if (hand == 0) {
			// special case for adding the first card to an empty hand
			hand = (long) (card.getRank() + 1) << OFF_FIRST_RANK;
			hand |= ((card.getSuit() & MASK_SUIT) << OFF_SUIT);
			return hand;
		} else {
			long newHand = 0;
			// Step 1: does adding this card break the hand's suitedness?
			int suit = getSuit(hand);
			if (card.getSuit() != suit) {
				// set the unsuited bit to 1, leave suit as 0
				newHand |= (MASK_UNSUITED << OFF_UNSUITED);
			} else {
				// copy the existing unsuited bit and suit bits
				long copyMask = ((MASK_UNSUITED << OFF_UNSUITED) |
								 (MASK_SUIT << OFF_SUIT));
				newHand = hand & copyMask;
			}
			// Step 2: scan for the place to insert this card in rank-order
			// all higher-rank cards get copied over
			int rank = card.getRank() + 1;
			int off = OFF_FIRST_RANK;
			hand = hand >>> off;
			while ((hand & MASK_RANK) > rank) {
				newHand |= (hand & MASK_RANK) << off;
				off += OFF_RANK;
				hand = hand >>> OFF_RANK;
			}
			// Insert the new card
			newHand |= rank << off;
			off += OFF_RANK;
			//  Copy the remaining cards afterwards
			while ((hand & MASK_RANK) != 0) {
				newHand |= (hand & MASK_RANK) << off;
				off += OFF_RANK;
				hand = hand >>> OFF_RANK;
			}
			return newHand;
		}
	}
			
		
	@Override
	public LongOfcHand2 copy() {
		return new LongOfcHand2(this);
	}

	@Override
	public int getBackSize() {
		return countSize(getBackMask());
	}
	
	@Override
	public int getMiddleSize() {
		return countSize(getMiddleMask());
	}
	
	@Override
	public int getFrontSize() {
		return countSize(getFrontMask());
	}

	@VisibleForTesting
	long getBackMask() {
		long back = board >>> OFF_BACK;
		back = back & MASK_BACK;
		return back;
		//return (board >>> OFF_BACK) & MASK_BACK;
	}

	@VisibleForTesting
	long getMiddleMask() {
		return (board >>> OFF_MIDDLE) & MASK_MIDDLE;
	}
	
	@VisibleForTesting
	long getFrontMask() {
		return (board >>> OFF_FRONT) & MASK_FRONT;
	}

	@Override
	public void addBack(OfcCard card) {
		if (getBackSize() >= BACK_SIZE) {
			throw new IllegalStateException("Back already full");
		}
		long backMask = addCardToSubhand(getBackMask(), card);
		board &= ~ (MASK_BACK << OFF_BACK);
		board |= backMask << OFF_BACK;
		if (getBackSize() == BACK_SIZE) {
			completeBack();
		}
	}

	@Override
	public void addMiddle(OfcCard card) {
		if (getMiddleSize() >= MIDDLE_SIZE) {
			throw new IllegalStateException("Middle already full");
		}
		long middleMask = addCardToSubhand(getMiddleMask(), card);
		board &= ~ (MASK_MIDDLE << OFF_MIDDLE);
		board |= middleMask << OFF_MIDDLE;
		if (getMiddleSize() == MIDDLE_SIZE) {
			completeMiddle();
		}
	}

	@Override
	public void addFront(OfcCard card) {
		if (getFrontSize() >= FRONT_SIZE) {
			throw new IllegalStateException("Front already full");
		}
		long frontMask = addCardToSubhand(getFrontMask(), card);
		board &= ~ (MASK_FRONT << OFF_FRONT);
		board |= frontMask << OFF_FRONT;
		if (getFrontSize() == FRONT_SIZE) {
			completeFront();
		}
	}

	@Override
	public boolean isComplete() {
		return getFrontSize() == FRONT_SIZE &&
				getMiddleSize() == MIDDLE_SIZE &&
				getBackSize() == BACK_SIZE;
	}

	@Override
	public boolean willBeFouled() {
		return willBeFouled;
	}

	@Override
	public boolean isFouled() {
		if (!isComplete()) {
			throw new IllegalStateException("Hand not complete");
		}
		// TODO: Be damn sure about this.  Making an assumption that when the hand is complete, willBeFouled is always populated
		// via the completeXXX methods
		return willBeFouled;
	}

	@Override
	public long getFrontRank() {
		if (frontValue == UNSET) {
			long frontMask = getFrontMask();
			int handInt = (int) (frontMask >> OFF_UNSUITED);
			long cached = EVAL_CACHE[handInt];
			if (cached > 0) {
				return cached;
			}

			if (countSize(frontMask) != FRONT_SIZE) {
				throw new IllegalStateException("Front not complete");
			}
			int[] ranks = getRanks(frontMask, FRONT_SIZE);
			frontValue = StupidEval.eval3(ranks);
			EVAL_CACHE[handInt] = frontValue;
		}
		return frontValue;
	}

	@Override
	public long getMiddleRank() {
		if (middleValue == UNSET) {
			long middleMask = getMiddleMask();
			int handInt = (int) (middleMask >> OFF_UNSUITED);
			long cached = EVAL_CACHE[handInt];
			if (cached > 0) {
				return cached;
			}
			if (countSize(middleMask) != MIDDLE_SIZE) {
				throw new IllegalStateException("Middle not complete");
			}
			int[] ranks = getRanks(middleMask, MIDDLE_SIZE);
			boolean isFlush = isSuited(middleMask);
			middleValue = StupidEval.eval(ranks, isFlush);
			EVAL_CACHE[handInt] = middleValue;
		}
		return middleValue;
	}

	@Override
	public long getBackRank() {
		if (backValue == UNSET) {
			long backMask = getBackMask();
			int handInt = (int) (backMask >> OFF_UNSUITED);
			long cached = EVAL_CACHE[handInt];
			if (cached > 0) {
				return cached;
			}
			if (countSize(backMask) != BACK_SIZE) {
				throw new IllegalStateException("Back not complete");
			}
			int[] ranks = getRanks(backMask, BACK_SIZE);
			boolean isFlush = isSuited(backMask);
			backValue = StupidEval.eval(ranks, isFlush);
			EVAL_CACHE[handInt] = backValue;
		}
		return backValue;
	}

	@Override
	public int getStreet() {
		return getFrontSize() + getMiddleSize() + getBackSize() + 1;
	}

	@Override
	public String toKeyString() {
		if (willBeFouled()) {
			return new FouledOfcHand(this).toKeyString();
		}
		StringBuilder sb = new StringBuilder();
		sb.append(board);
		return sb.toString();
	}

	public static OfcHand fromKeyString(String s) {
		if (s.startsWith("F")) {
			// Fouled hand.
			return FouledOfcHand.fromKeyString(s);
		}
		return new LongOfcHand2(Long.parseLong(s));
	}
	
	public void setHand(String handString, OfcDeck deck) {
		String[] hands = handString.split("/");
		if (hands.length != 3) {
			throw new IllegalArgumentException("Must have 3 hands");
		}
		int index = 0;
		while (index < hands[0].length()) {
			String cardString = hands[0].substring(index, index + 2);
			addFront(new OfcCard(cardString));
			deck.removeCard(cardString);
			index += 2;
		}
		index = 0;
		while (index < hands[1].length()) {
			String cardString = hands[1].substring(index, index + 2);
			addMiddle(new OfcCard(cardString));
			deck.removeCard(cardString);
			index += 2;
		}
		index = 0;
		while (index < hands[2].length()) {
			String cardString = hands[2].substring(index, index + 2);
			addBack(new OfcCard(cardString));
			deck.removeCard(cardString);
			index += 2;
		}
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(toString(getFrontMask()));
		sb.append("/");
		sb.append(toString(getMiddleMask()));
		sb.append("/");
		sb.append(toString(getBackMask()));
		return sb.toString();
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		long back = getBackMask();
		long middle = getMiddleMask();
		long front = getFrontMask();
		int result = 1;
		result = prime * result + (int) (back ^ (back >>> 32));
		result = prime * result + (int) (front ^ (front >>> 32));
		result = prime * result + (int) (middle ^ (middle >>> 32));
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
		LongOfcHand2 other = (LongOfcHand2) obj;
		if (board != other.board)
			return false;
		return true;
	}

}
