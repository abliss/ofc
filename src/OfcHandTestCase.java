import junit.framework.TestCase;


public abstract class OfcHandTestCase extends TestCase {

	protected OfcHand hand1;
	protected OfcHand hand2;
	
	protected void setUp() throws Exception {
		super.setUp();
		initHands();
	}
	
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	protected OfcHandTestCase(String name) {
		super(name);
	}

	protected abstract void initHands();
	
	private void assertHashesDifferent() {
		assertTrue(hand1.hashCode() != hand2.hashCode());
	}
	
	public void testHashesSame() {
		hand1.addBack(new OfcCard("8h"));
		hand2.addBack(new OfcCard("8h"));
		assertEquals(hand1.hashCode(), hand2.hashCode());
		hand1.addMiddle(new OfcCard("9d"));
		hand2.addMiddle(new OfcCard("9d"));
		assertEquals(hand1.hashCode(), hand2.hashCode());
	}
	
	public void testHashCodeDifferentCard() {
		hand1.addBack(new OfcCard("As"));
		hand2.addBack(new OfcCard("Ks"));
		assertHashesDifferent();
	} 
	
	public void testHashCodeDifferentSet() {
		hand1.addBack(new OfcCard("7d"));
		hand2.addMiddle(new OfcCard("7d"));
		assertHashesDifferent();
	}

	public void testAddTooManyFrontCards() {
		int startCard = 2;
		for (int i = startCard; i < OfcHand.FRONT_SIZE + startCard; i++) {
			hand1.addFront(new OfcCard(i + "s"));
		}
		try {
			hand1.addFront(new OfcCard("As"));
			fail("Shouldn't be able to add this many cards");
		} catch (Exception expected) {
		}
	}
	
	public void testAddTooManyMiddleCards() {
		int startCard = 2;
		for (int i = startCard; i < OfcHand.MIDDLE_SIZE + startCard; i++) {
			hand1.addMiddle(new OfcCard(i + "s"));
		}
		try {
			hand1.addMiddle(new OfcCard("As"));
			fail("Shouldn't be able to add this many cards");
		} catch (Exception expected) {
		}
	}
	
	public void testAddTooManyBackCards() {
		int startCard = 2;
		for (int i = startCard; i < OfcHand.BACK_SIZE + startCard; i++) {
			hand1.addBack(new OfcCard(i + "s"));
		}
		try {
			hand1.addBack(new OfcCard("As"));
			fail("Shouldn't be able to add this many cards");
		} catch (Exception expected) {
		}
	}
	
	public void testIsComplete() {
		hand1.addFront(new OfcCard("As"));
		assertTrue(!hand1.isComplete());
		hand1.addFront(new OfcCard("Ks"));
		assertTrue(!hand1.isComplete());
		hand1.addFront(new OfcCard("Qs"));
		assertTrue(!hand1.isComplete());
		hand1.addMiddle(new OfcCard("Ad"));
		assertTrue(!hand1.isComplete());
		hand1.addMiddle(new OfcCard("Kd"));
		assertTrue(!hand1.isComplete());
		hand1.addMiddle(new OfcCard("Qd"));
		assertTrue(!hand1.isComplete());
		hand1.addMiddle(new OfcCard("Jd"));
		assertTrue(!hand1.isComplete());
		hand1.addMiddle(new OfcCard("Td"));
		assertTrue(!hand1.isComplete());
		hand1.addBack(new OfcCard("Ah"));
		assertTrue(!hand1.isComplete());
		hand1.addBack(new OfcCard("Kh"));
		assertTrue(!hand1.isComplete());
		hand1.addBack(new OfcCard("Qh"));
		assertTrue(!hand1.isComplete());
		hand1.addBack(new OfcCard("Jh"));
		assertTrue(!hand1.isComplete());
		hand1.addBack(new OfcCard("Th"));
		assertTrue(hand1.isComplete());
	}
}
