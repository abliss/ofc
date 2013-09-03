/**
 * Represents all possible 28*27=756 possible outcomes of dealing the last
 * street, and allows these to be queried in efficient ways.
 */
public interface OfcHandMatrix {
	/**
	 * How many times does the given player's hand reach at least the given
	 * rank?
	 * @param player 0 or 1 for p1 or p2, respectively
	 * @param hand 0, 1, or 2 for front, middle, or back, respectively
	 * @param rank A rank value as returned from StupidEval
	 * @return a value in [0, 756] representing how many possible final dealouts
	 * give at least the desired result. Fouled boards will not be counted.
	 */
	int numHandsOfRank(int player, int hand, long rank);

	/**
	 * How many times does P1 beat P2 the given number of times?
	 * @param wins a number in [0,3].
	 * P1 fouling but P2 not fouling will be counted in wins=0.
	 * P2 fouling but P1 not fouling will be counted in wins=3.
	 * Both players fouling will not be counted.
	 */
	int numP1Wins(int wins);
}