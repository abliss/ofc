import org.pokersource.game.Deck;


public class LongOfcHand2Test extends CachedValueOfcHandTestCase {

	public LongOfcHand2Test(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void initHands() {
		cHand1 = new LongOfcHand2();
		cHand2 = new LongOfcHand2();
	}
	
	public void testHandIsSorted() {
		LongOfcHand2 hand = new LongOfcHand2();
		int[] ranks;
		hand.addBack(new OfcCard("2c"));
		assertEquals("0 /0 /2 0,", hand.toString());
		hand.addBack(new OfcCard("3c"));
		assertEquals("0 /0 /2 1,0,", hand.toString());
		hand.addBack(new OfcCard("6c"));
		assertEquals("0 /0 /2 4,1,0,", hand.toString());
		hand.addBack(new OfcCard("4h"));
		assertEquals("0 /0 /U 4,2,1,0,", hand.toString());
		hand.addBack(new OfcCard("5c"));
		assertEquals("0 /0 /U 4,3,2,1,0,", hand.toString());
		ranks = LongOfcHand2.getRanks(hand.getBackMask(),
									  OfcHand.BACK_SIZE);

		
		assertEquals(Deck.RANK_6, ranks[0]);
		assertEquals(Deck.RANK_5, ranks[1]);
		assertEquals(Deck.RANK_4, ranks[2]);
		assertEquals(Deck.RANK_3, ranks[3]);
		assertEquals(Deck.RANK_2, ranks[4]);

		assertEquals(false, LongOfcHand2.isSuited(hand.getBackMask()));
	}
	
	public void testKeyString() {
		addFront1("2c");
		addFront1("4c");
		addFront1("8d");
		
		addMiddle1("7c");
		addMiddle1("7h");
		addMiddle1("Ac");
		
		addBack1("Kh");
		addBack1("Ah");
		addBack1("Qh");
		addBack1("2h");
		
		String keyString = cHand1.toKeyString();
		OfcHand from = LongOfcHand2.fromKeyString(keyString);
		assertEquals(cHand1, from);
	}
	
	public void testKeyStringFouledHand() {
		foulHand1();
		String keyString = hand1.toKeyString();
		OfcHand from = LongOfcHand2.fromKeyString(keyString);

		// We can't recreate the actual hand, all we know is that it will be fouled
		assertTrue(from.willBeFouled());
	}
}
