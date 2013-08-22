import org.pokersource.game.Deck;


public class LongOfcHandTest extends CachedValueOfcHandTestCase {

	public LongOfcHandTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void initHands() {
		cHand1 = new LongOfcHand();
		cHand2 = new LongOfcHand();
	}
	
	public void testHandIsSorted() {
		LongOfcHand hand = new LongOfcHand();
		
		hand.addBack(new OfcCard("2c"));
		hand.addBack(new OfcCard("3c"));
		hand.addBack(new OfcCard("6c"));
		hand.addBack(new OfcCard("4h"));
		hand.addBack(new OfcCard("5c"));
		
		int[] ranks = new int[5];
		int[] suits = new int[5];
		LongOfcHand.convertForEval(hand.back, ranks, suits, OfcHand.BACK_SIZE);

		assertEquals(Deck.RANK_6, ranks[0]);
		assertEquals(Deck.RANK_5, ranks[1]);
		assertEquals(Deck.RANK_4, ranks[2]);
		assertEquals(Deck.RANK_3, ranks[3]);
		assertEquals(Deck.RANK_2, ranks[4]);

		assertEquals(Deck.SUIT_CLUBS, suits[0]);
		assertEquals(Deck.SUIT_CLUBS, suits[1]);
		assertEquals(Deck.SUIT_HEARTS, suits[2]);
		assertEquals(Deck.SUIT_CLUBS, suits[3]);
		assertEquals(Deck.SUIT_CLUBS, suits[4]);
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
		OfcHand from = LongOfcHand.fromKeyString(keyString);
		assertEquals(cHand1, from);
	}
	public void testKeyStringFoul() {
		addFront1("2c");
		addFront1("8c");
		addFront1("8d");
		
		addMiddle1("7c");
		addMiddle1("7h");
		addMiddle1("Ac");
		addMiddle1("Kc");
		addMiddle1("Qc");
		
		addBack1("Kh");
		addBack1("Ah");
		addBack1("Qh");
		addBack1("2h");
		
		String keyString = cHand1.toKeyString();
		OfcHand from = LongOfcHand.fromKeyString(keyString);
		String keyString2 = from.toKeyString();
		assertEquals(keyString, keyString2);
	}
}
