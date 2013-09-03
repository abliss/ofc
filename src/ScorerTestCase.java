import junit.framework.TestCase;


public abstract class ScorerTestCase extends TestCase {

	OfcHand hand;
	Scorers.Scorer scorer;
	
	protected static final CompleteOfcHand FIVES_FRONT = LongCompleteOfcHand.createComplete("5c5hAs/7c7h2d3d4d/8c8hAcKcQc");
	protected static final CompleteOfcHand SIXES_FRONT = LongCompleteOfcHand.createComplete("6c6h2c/7c7h2d3d4d/8c8hAcKcQc");
	protected static final CompleteOfcHand JACKS_FRONT = LongCompleteOfcHand.createComplete("JcJd2h/KcKd3h4h5h/AcAh3c4c5c");
	protected static final CompleteOfcHand QUEENS_FRONT = LongCompleteOfcHand.createComplete("QcQd2h/KcKd3h4h5h/AcAh3c4c5c");
	protected static final CompleteOfcHand TRIP_2_FRONT = LongCompleteOfcHand.createComplete("2c2d2s/KdKsKh4c5h/AdAcAh4s3h");
	protected static final CompleteOfcHand TRIP_A_FRONT = LongCompleteOfcHand.createComplete("AcAdAs/KdKsKh2c2h/JdJcJhJs3h");
	
	protected static final CompleteOfcHand QUEENS_FRONT_FOULED = LongCompleteOfcHand.createComplete("QcQd2h/JdJh3h4h5h/AhAd2s3s4s");

	// Dummy matrix wrapping one hand for p1, with ability to mask down to one subhand
	private static class TestMatrix implements OfcHandMatrix {
		public static final int MASK_FRONT_ONLY = 1;
		public static final int MASK_ALL = 7;
		final int handMask;
		final CompleteOfcHand hand;
		/**
		 * @param handMask set bit 0, 1, or 2 to allow scoring of front, middle, back. 
		 */
		public TestMatrix(int handMask, CompleteOfcHand hand) {
			this.handMask = handMask;
			this.hand = hand;
		}
		@Override
		public int numHandsOfRank(int player, int handNum, long rank) {
			if ((player != 0) || (0 == (handMask & (1 << handNum)))) {
				return 0;
			}
			if (hand.isFouled()) {
				return 0;
			}
			long myRank;
			switch (handNum) {
			case 0:
				myRank = hand.getFrontRank();
				break;
			case 1:
				myRank = hand.getMiddleRank();
				break;
			case 2:
				myRank = hand.getBackRank();
				break;
			default:
				throw new RuntimeException("Misusing the dummy");
			}
			return (myRank >= rank) ? 1 : 0;	
		}
		
		@Override
		public int numP1Wins(int wins) {
			return 0;
		}
	}
	int scoreFront(CompleteOfcHand hand) {
		return scorer.score(new TestMatrix(TestMatrix.MASK_FRONT_ONLY, hand));
	}
	int scoreAll(CompleteOfcHand hand) {
		return scorer.score(new TestMatrix(TestMatrix.MASK_ALL, hand));
	}
	public ScorerTestCase(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		hand = new LongOfcHand();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testScoreFront() throws Exception {
		assertEquals(0, scoreFront(FIVES_FRONT));
		assertEquals(1, scoreFront(SIXES_FRONT));
		assertEquals(6, scoreFront(JACKS_FRONT));
		assertEquals(7 + Scorers.FANTASYLAND_VALUE, scoreFront(QUEENS_FRONT));
		assertEquals(10 + Scorers.FANTASYLAND_VALUE, scoreFront(TRIP_2_FRONT));
		assertEquals(22 + Scorers.FANTASYLAND_VALUE, scoreFront(TRIP_A_FRONT));		
	}
	
	public void testFouledHandRoyalties() throws Exception {
		assertEquals(0, scoreAll(QUEENS_FRONT_FOULED));
	}
}
