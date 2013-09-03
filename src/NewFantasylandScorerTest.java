
public class NewFantasylandScorerTest extends ScorerTestCase {

	public NewFantasylandScorerTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		scorer = new Scorers.NewFantasylandScorer();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

}
