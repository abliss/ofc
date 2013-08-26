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
		assertEquals(0, scorer.getFrontValue(FIVES_FRONT));
		assertEquals(1, scorer.getFrontValue(SIXES_FRONT));
		assertEquals(6, scorer.getFrontValue(JACKS_FRONT));
		assertEquals(7, scorer.getFrontValue(QUEENS_FRONT));
		assertEquals(10, scorer.getFrontValue(TRIP_2_FRONT));
		assertEquals(22, scorer.getFrontValue(TRIP_A_FRONT));		
	}
	
	public void testFouledHandRoyalties() throws Exception {
		assertEquals(0, scorer.getRoyaltyValue(QUEENS_FRONT_FOULED));
	}
}
