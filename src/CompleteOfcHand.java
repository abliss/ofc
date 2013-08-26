public interface CompleteOfcHand extends OfcHand {

	boolean isFouled();

	int getRoyaltyValue();
	int getFantasylandValue();
	
	Score scoreAgainst(CompleteOfcHand other);
}