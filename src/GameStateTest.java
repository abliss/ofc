import junit.framework.TestCase;


public class GameStateTest extends TestCase {

	public GameStateTest(String name) {
		super(name);
	}

	
	public void testFileString() {
        OfcDeck deck = new OfcDeck().initialize();
		OfcHand hand1 = new LongOfcHand();
        hand1.setHand("6h2h/Js9s8h/KsKdTsTc2s", deck);
		OfcHand hand2 = new LongOfcHand();
        hand2.setHand("Td2c2d/KcQd7s/AsAdAhJdJh", deck);
        GameState state1 = new GameState(hand1, hand2, deck);
        String fileString1 = state1.toFileString();
        String fileString2 = "6h2h-Js9s8h-KsKdTsTc2s Td2c2d-KcQd7s-AsAdAhJdJh Ac-Kh-Qs-Qc-Qh-Jc-Th-9c-9d-9h-8s-8c-8d-7c-7d-7h-6s-6c-6d-5s-5c-5d-5h-4s-4c-4d-4h-3s-3c-3d-3h";
        assertEquals(fileString1, fileString2);
        GameState state2 = GameState.fromFileString(fileString1);
		
		assertEquals(state1, state2);
	}
	
}
