package game;

import java.util.Random;

/**
 * General poker related classes. This class encapsulates how cards and
 * hands are stored in memory. Because we are expected to run millions
 * of monte carlo simulations for cards and hands, most of the data is
 * stored as int arrays to be as efficient and compact as possible, both
 * memory-wise and speed of copying.
 * 
 * All enumerations and sorting is done in descending order, so the
 * strongest suit/value/card/hand is placed first.
 * 
 * Cards are represented by int[2] (e.g. int[] card = new int[]{1, 1}),
 * where the first index (i.e. card[VALUE]) represents the value of the
 * card (2 through 10, or jacks, queens, kings, or ace), and the second
 * index (i.e. card[SUIT]) represents the suit of the card (DIAMONDS=0,
 * CLUBS=1, HEARTS=2, SPADES=3).
 * 
 * Hands are constructed with no cards in it. To add cards, you must
 * do hand.addCard(int[] card). Cards added are always added in sorted
 * order. ComboType is the value of the hand (flush, straight, full house),
 * and so the hands are further separated by the combo cards, and the kicker
 * cards (all cards not part of the combo).
 * 
 * Poker.FULL_DECK is the entire 52-card deck in sorted order (int[52][]).
 * This is a global variable and should never be modified by any other class.
 * To shuffle the deck, call Poker.getNextShuffedCardOrder(), and iterate
 * through the indices accordingly. The shuffled order is seeded by
 * Poker.RANDOM_SEED, which can be changed if desired.
 * 
 * This class also has a definition for each card, e.g. Poker.ACE_SPADES,
 * Poker.TEN_CLUBS, Poker.JACK_HEARTS, each of which are int[2]. These
 * cards should also never be modified by any other class.
 * 
 * This class also implements Poker.shuffle(int[] arrayToShuffle), which
 * is a copy of Java's Collections.shuffle(List listToShuffle), modified
 * for int arrays.
 * 
 * @author Kevin Truong
 *
 */
public class Poker {
	public static Random RANDOM_SEED = new Random(0); // non-final, can be changed of desired. 
	
	/********************************************************************************************
	 * Table of contents:
	 *   I. Card
	 *   II. Deck
	 *   III. Hand
	 *   IV. Example use
	 ********************************************************************************************/
	
	
	/********************************************************************************************
	 * I. Card
	 ********************************************************************************************/
	public static final int CARD_SIZE = 2; // all cards are represented by int[CARD_SIZE], i.e. int[2]

	public static final int VALUE = 0; // card[VALUE] returns the card's value
	public static final int SUIT = 1; // card[SUIT] returns the card's suit

	public static final int ACE = 14;
	public static final int KING = 13;
	public static final int QUEEN = 12;
	public static final int JACK = 11;

	public static final int SPADES = 3;
	public static final int HEARTS = 2;
	public static final int CLUBS = 1;
	public static final int DIAMONDS = 0;

	// These cards should never be modified
	public static final int[] ACE_SPADES = new int[]{ACE, SPADES};
	public static final int[] ACE_HEARTS = new int[]{ACE, HEARTS};
	public static final int[] ACE_CLUBS = new int[]{ACE, CLUBS};
	public static final int[] ACE_DIAMONDS = new int[]{ACE, DIAMONDS};
	public static final int[] KING_SPADES = new int[]{KING, SPADES};
	public static final int[] KING_HEARTS = new int[]{KING, HEARTS};
	public static final int[] KING_CLUBS = new int[]{KING, CLUBS};
	public static final int[] KING_DIAMONDS = new int[]{KING, DIAMONDS};
	public static final int[] QUEEN_SPADES = new int[]{QUEEN, SPADES};
	public static final int[] QUEEN_HEARTS = new int[]{QUEEN, HEARTS};
	public static final int[] QUEEN_CLUBS = new int[]{QUEEN, CLUBS};
	public static final int[] QUEEN_DIAMONDS = new int[]{QUEEN, DIAMONDS};
	public static final int[] JACK_SPADES = new int[]{JACK, SPADES};
	public static final int[] JACK_HEARTS = new int[]{JACK, HEARTS};
	public static final int[] JACK_CLUBS = new int[]{JACK, CLUBS};
	public static final int[] JACK_DIAMONDS = new int[]{JACK, DIAMONDS};
	public static final int[] TEN_SPADES = new int[]{10, SPADES};
	public static final int[] TEN_HEARTS = new int[]{10, HEARTS};
	public static final int[] TEN_CLUBS = new int[]{10, CLUBS};
	public static final int[] TEN_DIAMONDS = new int[]{10, DIAMONDS};
	public static final int[] NINE_SPADES = new int[]{9, SPADES};
	public static final int[] NINE_HEARTS = new int[]{9, HEARTS};
	public static final int[] NINE_CLUBS = new int[]{9, CLUBS};
	public static final int[] NINE_DIAMONDS = new int[]{9, DIAMONDS};
	public static final int[] EIGHT_SPADES = new int[]{8, SPADES};
	public static final int[] EIGHT_HEARTS = new int[]{8, HEARTS};
	public static final int[] EIGHT_CLUBS = new int[]{8, CLUBS};
	public static final int[] EIGHT_DIAMONDS = new int[]{8, DIAMONDS};
	public static final int[] SEVEN_SPADES = new int[]{7, SPADES};
	public static final int[] SEVEN_HEARTS = new int[]{7, HEARTS};
	public static final int[] SEVEN_CLUBS = new int[]{7, CLUBS};
	public static final int[] SEVEN_DIAMONDS = new int[]{7, DIAMONDS};
	public static final int[] SIX_SPADES = new int[]{6, SPADES};
	public static final int[] SIX_HEARTS = new int[]{6, HEARTS};
	public static final int[] SIX_CLUBS = new int[]{6, CLUBS};
	public static final int[] SIX_DIAMONDS = new int[]{6, DIAMONDS};
	public static final int[] FIVE_SPADES = new int[]{5, SPADES};
	public static final int[] FIVE_HEARTS = new int[]{5, HEARTS};
	public static final int[] FIVE_CLUBS = new int[]{5, CLUBS};
	public static final int[] FIVE_DIAMONDS = new int[]{5, DIAMONDS};
	public static final int[] FOUR_SPADES = new int[]{4, SPADES};
	public static final int[] FOUR_HEARTS = new int[]{4, HEARTS};
	public static final int[] FOUR_CLUBS = new int[]{4, CLUBS};
	public static final int[] FOUR_DIAMONDS = new int[]{4, DIAMONDS};
	public static final int[] THREE_SPADES = new int[]{3, SPADES};
	public static final int[] THREE_HEARTS = new int[]{3, HEARTS};
	public static final int[] THREE_CLUBS = new int[]{3, CLUBS};
	public static final int[] THREE_DIAMONDS = new int[]{3, DIAMONDS};
	public static final int[] TWO_SPADES = new int[]{2, SPADES};
	public static final int[] TWO_HEARTS = new int[]{2, HEARTS};
	public static final int[] TWO_CLUBS = new int[]{2, CLUBS};
	public static final int[] TWO_DIAMONDS = new int[]{2, DIAMONDS};
	
	/**
	 * @param card1
	 * @param card2
	 * @return compare value for descending order
	 */
	public static int compareCards(int[] card1, int[] card2) {
		if (card1[VALUE] == card2[VALUE]) {
			return card2[SUIT] - card1[SUIT];
		} else {
			return card2[VALUE] - card1[VALUE];
		}
	}

	public static String toStringCardValue(int cardValue) {
		if (cardValue == ACE) {
			return "A";
		} else if (cardValue == KING) {
			return "K";
		} else if (cardValue == QUEEN) {
			return "Q";
		} else if (cardValue == JACK) {
			return "J";
		} else {
			return Integer.toString(cardValue);
		}
	}

	public static String toStringCardSuit(int cardSuit) {
		if (cardSuit == SPADES) {
			return "s";
		} else if (cardSuit == HEARTS) {
			return "h";
		} else if (cardSuit == CLUBS) {
			return "c";
		} else if (cardSuit == DIAMONDS) {
			return "d";
		} else {
			return "?";
		}
	}

	public static String toStringCard(int[] card) {
		return toStringCardValue(card[VALUE]) + toStringCardSuit(card[SUIT]);
	}

	/********************************************************************************************
	 * II. Deck
	 ********************************************************************************************/
	// This should never be modified
	public static final int[][] FULL_DECK = new int[][]{
		ACE_SPADES,
		ACE_HEARTS,
		ACE_CLUBS,
		ACE_DIAMONDS,
		KING_SPADES,
		KING_HEARTS,
		KING_CLUBS,
		KING_DIAMONDS,
		QUEEN_SPADES,
		QUEEN_HEARTS,
		QUEEN_CLUBS,
		QUEEN_DIAMONDS,
		JACK_SPADES,
		JACK_HEARTS,
		JACK_CLUBS,
		JACK_DIAMONDS,
		TEN_SPADES,
		TEN_HEARTS,
		TEN_CLUBS,
		TEN_DIAMONDS,
		NINE_SPADES,
		NINE_HEARTS,
		NINE_CLUBS,
		NINE_DIAMONDS,
		EIGHT_SPADES,
		EIGHT_HEARTS,
		EIGHT_CLUBS,
		EIGHT_DIAMONDS,
		SEVEN_SPADES,
		SEVEN_HEARTS,
		SEVEN_CLUBS,
		SEVEN_DIAMONDS,
		SIX_SPADES,
		SIX_HEARTS,
		SIX_CLUBS,
		SIX_DIAMONDS,
		FIVE_SPADES,
		FIVE_HEARTS,
		FIVE_CLUBS,
		FIVE_DIAMONDS,
		FOUR_SPADES,
		FOUR_HEARTS,
		FOUR_CLUBS,
		FOUR_DIAMONDS,
		THREE_SPADES,
		THREE_HEARTS,
		THREE_CLUBS,
		THREE_DIAMONDS,
		TWO_SPADES,
		TWO_HEARTS,
		TWO_CLUBS,
		TWO_DIAMONDS
	};
	
	/**
	 * The index of 'card' in Poker.FULL_DECK. For example,
	 * getSortedOrderInDeck(Poker.ACE_SPACES) == 0, since
	 * Poker.ACE_SPACES is the first card in Poker.FULL_DECK.
	 * 
	 * @param card
	 * @return
	 */
	public static int getSortedOrderInDeck(int[] card) {
		return (ACE - card[VALUE]) * (SPADES + 1) + (SPADES - card[SUIT]);
	}
	
	/**
	 * This function allows you to use Poker.FULL_DECK as if it were shuffled,
	 * without shuffling the deck every time. Returns a list of shuffled indices.
	 * 
	 * @return
	 */
	public static int[] getNextShuffedCardOrder() {
		int[] order = new int[FULL_DECK.length];
		for (int i = 0; i < FULL_DECK.length; i++) {
			order[i] = i;
		}
		shuffle(order);
		return order;
	}

    /**
     * Note(Kevin): copied from Collections.shuffle(), modified for int[]
     * 
     * Randomly permutes the specified list using a default source of
     * randomness.  All permutations occur with approximately equal
     * likelihood.
     *
     * <p>The hedge "approximately" is used in the foregoing description because
     * default source of randomness is only approximately an unbiased source
     * of independently chosen bits. If it were a perfect source of randomly
     * chosen bits, then the algorithm would choose permutations with perfect
     * uniformity.
     *
     * <p>This implementation traverses the list backwards, from the last
     * element up to the second, repeatedly swapping a randomly selected element
     * into the "current position".  Elements are randomly selected from the
     * portion of the list that runs from the first element to the current
     * position, inclusive.
     *
     * <p>This method runs in linear time.  If the specified list does not
     * implement the {@link RandomAccess} interface and is large, this
     * implementation dumps the specified list into an array before shuffling
     * it, and dumps the shuffled array back into the list.  This avoids the
     * quadratic behavior that would result from shuffling a "sequential
     * access" list in place.
     *
     * @param  list the list to be shuffled.
     * @throws UnsupportedOperationException if the specified list or
     *         its list-iterator does not support the <tt>set</tt> operation.
     */    
    public static void shuffle(int[] list) {
        int size = list.length;
        for (int i = size; i > 1; i--) {
        	swap(list, i-1, RANDOM_SEED.nextInt(i));
        }
    }
        
    /**
     * Note(Kevin): copied from Collections.shuffle(), modified for int[]
     * 
     * Swaps the elements at the specified positions in the specified list.
     * (If the specified positions are equal, invoking this method leaves
     * the list unchanged.)
     *
     * @param list The list in which to swap elements.
     * @param i the index of one element to be swapped.
     * @param j the index of the other element to be swapped.
     * @throws IndexOutOfBoundsException if either <tt>i</tt> or <tt>j</tt>
     *         is out of range (i &lt; 0 || i &gt;= list.size()
     *         || j &lt; 0 || j &gt;= list.size()).
     * @since 1.4
     */
    private static void swap(int[] list, int i, int j) {
    	int buffer = list[i];
    	list[i] = list[j];
    	list[j] = buffer;
    }

	/********************************************************************************************
	 * III. Hand
	 ********************************************************************************************/
	public static enum ComboType {ROYAL_FLUSH, STRAIGHT_FLUSH, FOUR_OF_A_KIND, FULL_HOUSE, FLUSH, STRAIGHT, THREE_OF_A_KIND, TWO_PAIRS, PAIR, HIGH_CARD};
	private static final int MAX_CARDS_PER_HAND = 5;
	public static class Hand implements Comparable<Hand> {
		private int[] cards; // int[10], a bunch of cards (int[2]) appended together
		private int numCards;

		private ComboType comboType;
		private int[] comboCards; // int[10], a bunch of cards (int[2]) appended together
		private int numComboCards;
		private int[] kickers; // int[10], a bunch of cards (int[2]) appended together
		private int numKickerCards;

		public Hand() {
			this.cards = new int[MAX_CARDS_PER_HAND * CARD_SIZE];
			this.numCards = 0;
			this.comboType = null;
			this.comboCards = new int[MAX_CARDS_PER_HAND * CARD_SIZE];;
			this.numComboCards = 0;
			this.kickers = new int[MAX_CARDS_PER_HAND * CARD_SIZE];;
			this.numKickerCards = 0;
		}
		
		public Hand(Hand handToClone) {
			this.cards = new int[handToClone.cards.length];
			System.arraycopy(handToClone.cards, 0, this.cards, 0, handToClone.cards.length);
			this.numCards = handToClone.numCards;
			
			this.comboType = handToClone.comboType;
			this.comboCards = new int[handToClone.comboCards.length];
			System.arraycopy(handToClone.comboCards, 0, this.comboCards, 0, handToClone.comboCards.length);
			this.numComboCards = handToClone.numComboCards;
			this.kickers = new int[handToClone.kickers.length];
			System.arraycopy(handToClone.kickers, 0, this.kickers, 0, handToClone.kickers.length);
			this.numKickerCards = handToClone.numKickerCards;
		}
		
		/**
		 * The card is always added in sorted order. Also computes the comboType of the hand.
		 * 
		 * @param card
		 */
		public void addCard(int[] card) {
			// add card in hand, make sure its in sorted order
			if (this.numCards == 0) {
				addCard(card, 0);
			} else {
				int firstCardCompare = compareCards(0, card);
				if (firstCardCompare > 0) {
					shiftCardsRight(0);
					addCard(card, 0);
				} else {
					int insertIdx;
					for (insertIdx = 1; insertIdx < this.numCards; insertIdx++) {
						if (compareCards(insertIdx, card) > 0) {
							break;
						}
					}
					shiftCardsRight(insertIdx);
					addCard(card, insertIdx);
				}
			}

			boolean hasFlush = false;
			if (this.numCards == 5) {
				hasFlush = true;
				int suitRef = -1;
				for (int i = 0; i < this.numCards; i++) {
					int suit = getCardSuit(i);
					if (suitRef == -1) {
						suitRef = suit;
					} else {
						if (suitRef != suit) {
							hasFlush = false;
							break;
						}
					}
				}
			}

			boolean hasStraight = false;
			if (this.numCards == 5) {
				hasStraight = true;
				int firstValue = getCardValue(0);
				if (firstValue == ACE &&
						getCardValue(1) == 5 &&
						getCardValue(2) == 4 &&
						getCardValue(3) == 3 &&
						getCardValue(4) == 2) {
					// Currently this.cards is A 5 4 3 2
					// when it should be 5 4 3 2 A
					int[] buffer = new int[]{getCardValue(0), getCardSuit(0)};
					for (int i = 1; i < this.numCards; i++) {
						this.cards[(i - 1) * CARD_SIZE] = this.cards[i * CARD_SIZE];
						this.cards[(i - 1) * CARD_SIZE + 1] = this.cards[i * CARD_SIZE + 1];
					}
					this.cards[(this.numCards - 1) * CARD_SIZE] = buffer[0];
					this.cards[(this.numCards - 1) * CARD_SIZE + 1] = buffer[1];
				} else {
					int prevValue = firstValue;
					for (int i = 1; i < this.numCards; i++) {
						int currValue = getCardValue(i);
						if (prevValue - currValue != 1) {
							hasStraight = false;
							break;
						}
						prevValue = currValue;
					}
				}
			}

			int[] value2Count = new int[ACE + 1];
			boolean[][] value2Skips = new boolean[ACE + 1][this.numCards];
			for (int i = 0; i < this.numCards; i++) {
				int currValue = getCardValue(i);
				value2Count[currValue]++;
				value2Skips[currValue][i] = true;
			}
			int numPairs = 0;
			int numTriples = 0;
			int numQuads = 0;
			for (int i = value2Count.length - 1; i >= 0; i--) {
				int count = value2Count[i];
				if (count == 2) {
					numPairs++;
				} else if (count == 3) {
					numTriples++;
				} else if (count == 4) {
					numQuads++;
				}
			}

			if (this.numCards >= 5 && hasFlush && hasStraight) { // straight flush, royal flush
				if (getCardValue(0) == ACE) {
					this.comboType = ComboType.ROYAL_FLUSH;
				} else {
					this.comboType = ComboType.STRAIGHT_FLUSH;
				}
				this.comboCards = this.cards;
				this.numComboCards = 5;
				this.kickers = new int[0];
				this.numKickerCards = 0;
			} else if (this.numCards >= 4 && numQuads == 1) { // four of a kind
				this.comboType = ComboType.FOUR_OF_A_KIND;
				int quadValue = -1;
				for (int i = 0; i < value2Count.length; i++) {
					if (value2Count[i] == 4) {
						quadValue = i;
						break;
					}
				}
				assert(quadValue != -1);

				this.comboCards = new int[4 * CARD_SIZE];
				this.numComboCards = 0;
				this.kickers = new int[1 * CARD_SIZE];
				this.numKickerCards = 0;
				for (int i = 0; i < this.numCards; i++) {
					int value = getCardValue(i);
					int suit = getCardSuit(i);
					if (value == quadValue) {
						this.comboCards[this.numComboCards * CARD_SIZE] = value;
						this.comboCards[this.numComboCards * CARD_SIZE + 1] = suit;
						this.numComboCards++;
					} else {
						this.kickers[this.numKickerCards * CARD_SIZE] = value;
						this.kickers[this.numKickerCards * CARD_SIZE + 1] = suit;
						this.numKickerCards++;
					}
				}
			} else if (this.numCards >= 5 && numTriples == 1 && numPairs == 1) { // full house
				this.comboType = ComboType.FULL_HOUSE;
				int tripleValue = -1;
				for (int i = 0; i < value2Count.length; i++) {
					if (value2Count[i] == 3) {
						tripleValue = i;
						break;
					}
				}
				assert(tripleValue != -1);
				int pairValue = -1;
				for (int i = 0; i < value2Count.length; i++) {
					if (value2Count[i] == 2) {
						pairValue = i;
						break;
					}
				}
				assert(pairValue != -1);

				this.comboCards = new int[5 * CARD_SIZE];
				this.numComboCards = 0;
				int tripleSize = 0;
				int pairSize = 0;
				this.kickers = new int[0];
				this.numKickerCards = 0;
				for (int i = 0; i < this.numCards; i++) {
					int value = getCardValue(i);
					int suit = getCardSuit(i);
					if (value == tripleValue) {
						this.comboCards[tripleSize * CARD_SIZE] = value;
						this.comboCards[tripleSize * CARD_SIZE + 1] = suit;
						this.numComboCards++;
						tripleSize++;
					} else {
						this.comboCards[(3 + pairSize) * CARD_SIZE] = value;
						this.comboCards[(3 + pairSize) * CARD_SIZE + 1] = suit;
						this.numComboCards++;
						pairSize++;
					}
				}
			} else if (this.numCards >= 5 && hasFlush) { // flush
				this.comboType = ComboType.FLUSH;
				this.comboCards = this.cards;
				this.numComboCards = 5;
				this.kickers = new int[0];
				this.numKickerCards = 0;
			} else if (this.numCards >= 5 && hasStraight) { // straight
				this.comboType = ComboType.STRAIGHT;
				this.comboCards = this.cards;
				this.numComboCards = 5;
				this.kickers = new int[0];
				this.numKickerCards = 0;
			} else if (this.numCards >= 3 && numTriples == 1) { // three of a kind
				this.comboType = ComboType.THREE_OF_A_KIND;
				int tripleValue = -1;
				for (int i = 0; i < value2Count.length; i++) {
					if (value2Count[i] == 3) {
						tripleValue = i;
						break;
					}
				}
				assert(tripleValue != -1);

				this.comboCards = new int[3 * CARD_SIZE];
				this.numComboCards = 0;
				this.kickers = new int[2 * CARD_SIZE];
				this.numKickerCards = 0;
				for (int i = 0; i < this.numCards; i++) {
					int value = getCardValue(i);
					int suit = getCardSuit(i);
					if (value == tripleValue) {
						this.comboCards[this.numComboCards * CARD_SIZE] = value;
						this.comboCards[this.numComboCards * CARD_SIZE + 1] = suit;
						this.numComboCards++;
					} else {
						this.kickers[this.numKickerCards * CARD_SIZE] = value;
						this.kickers[this.numKickerCards * CARD_SIZE + 1] = suit;
						this.numKickerCards++;
					}
				}
			} else if (this.numCards >= 4 && numPairs == 2) { // two pair
				this.comboType = ComboType.TWO_PAIRS;
				int lowerPair = -1;
				int higherPair = -1;
				for (int i = 0; i < value2Count.length; i++) {
					if (value2Count[i] == 2) {
						if (lowerPair == -1) {
							lowerPair = i;
						} else {
							higherPair = i;
							break;
						}
					}
				}
				assert(lowerPair != -1 && higherPair != -1);

				this.comboCards = new int[4 * CARD_SIZE];
				this.numComboCards = 0;
				this.kickers = new int[1 * CARD_SIZE];
				this.numKickerCards = 0;
				for (int i = 0; i < this.numCards; i++) {
					int value = getCardValue(i);
					int suit = getCardSuit(i);
					if (value == lowerPair || value == higherPair) {
						this.comboCards[this.numComboCards * CARD_SIZE] = value;
						this.comboCards[this.numComboCards * CARD_SIZE + 1] = suit;
						this.numComboCards++;
					} else {
						this.kickers[this.numKickerCards * CARD_SIZE] = value;
						this.kickers[this.numKickerCards * CARD_SIZE + 1] = suit;
						this.numKickerCards++;
					}
				}
			} else if (this.numCards >= 2 && numPairs == 1) { // pair
				this.comboType = ComboType.PAIR;
				int pairValue = -1;
				for (int i = 0; i < value2Count.length; i++) {
					if (value2Count[i] == 2) {
						pairValue = i;
						break;
					}
				}
				assert(pairValue != -1);

				this.comboCards = new int[2 * CARD_SIZE];
				this.numComboCards = 0;
				this.kickers = new int[3 * CARD_SIZE];
				this.numKickerCards = 0;
				for (int i = 0; i < this.numCards; i++) {
					int value = getCardValue(i);
					int suit = getCardSuit(i);
					if (value == pairValue) {
						this.comboCards[this.numComboCards * CARD_SIZE] = value;
						this.comboCards[this.numComboCards * CARD_SIZE + 1] = suit;
						this.numComboCards++;
					} else {
						this.kickers[this.numKickerCards * CARD_SIZE] = value;
						this.kickers[this.numKickerCards * CARD_SIZE + 1] = suit;
						this.numKickerCards++;
					}
				}
			} else {
				this.comboType = ComboType.HIGH_CARD;
				this.comboCards = this.cards;
				this.numComboCards = this.numCards;
				this.kickers = new int[0];
				this.numKickerCards = 0;
			}
		}

		private void shiftCardsRight(int startingIdx) {
			assert(this.numCards * CARD_SIZE + 1 < this.cards.length);
			for (int i = this.numCards - 1; i >= startingIdx; i--) {
				this.cards[(i + 1) * CARD_SIZE] = this.cards[i * CARD_SIZE];
				this.cards[(i + 1) * CARD_SIZE + 1] = this.cards[i * CARD_SIZE + 1];
			}
		}

		public int getCardValue(int cardIdx) {
			return this.cards[cardIdx * CARD_SIZE];
		}

		public int getCardSuit(int cardIdx) {
			return this.cards[cardIdx * CARD_SIZE + 1];
		}
		
		public int getNumCards() {
			return this.numCards;
		}
		
		public ComboType getComboType() {
			return this.comboType;
		}

		public int getComboCardValue(int cardIdx) {
			return this.comboCards[cardIdx * CARD_SIZE];
		}

		public int getComboCardSuit(int cardIdx) {
			return this.comboCards[cardIdx * CARD_SIZE + 1];
		}
		
		public int getNumComboCards() {
			return this.numComboCards;
		}

		public int getKickerCardValue(int cardIdx) {
			return this.kickers[cardIdx * CARD_SIZE];
		}

		public int getKickerCardSuit(int cardIdx) {
			return this.kickers[cardIdx * CARD_SIZE + 1];
		}
		
		public int getNumKickerCards() {
			return this.numKickerCards;
		}

		private int compareCards(int cardIdxInHand, int[] card) {
			if (card[VALUE] == getCardValue(cardIdxInHand)) {
				return card[SUIT] - getCardSuit(cardIdxInHand);
			} else {
				return card[VALUE] - getCardValue(cardIdxInHand);
			}
		}

		private void addCard(int[] card, int idx) {
			this.cards[idx * CARD_SIZE] = card[VALUE];
			this.cards[idx * CARD_SIZE + 1] = card[SUIT];
			this.numCards++;
		}

		@Override
		public int compareTo(Hand o) {
			if (this.comboType != o.comboType) {
				return this.comboType.compareTo(o.comboType);
			}

			int numComboCardsMin = Math.min(this.numComboCards, o.numComboCards);
			for (int i = 0; i < numComboCardsMin; i++) {
				int thisValue = getComboCardValue(i);
				int otherValue = o.getComboCardValue(i);
				if (otherValue != thisValue) {
					return otherValue - thisValue;
				}
			}

			int numKickerCardsMin = Math.min(this.numKickerCards, o.numKickerCards);
			for (int i = 0; i < numKickerCardsMin; i++) {
				int thisValue = getKickerCardValue(i);
				int otherValue = o.getKickerCardValue(i);
				if (otherValue != thisValue) {
					return otherValue - thisValue;
				}
			}

			return 0;
		}

		public String toString() {
			return (this.comboType == null ? "null" : this.comboType.toString()) +
					" (" + toStringHand(this.comboCards, this.numComboCards) + ")" +
					(this.numKickerCards > 0 ? " KICKER (" + toStringHand(this.kickers, this.numKickerCards) + ")" : "");
		}

		private static String toStringHand(int[] hand, int numCardsInHand) {
			StringBuilder sb = new StringBuilder();
			sb.append("[");
			for (int i = 0; i < numCardsInHand; i ++) {
				if (sb.length() > 1) {
					sb.append(", ");
				}
				sb.append(toStringCardValue(hand[i * CARD_SIZE]));
				sb.append(toStringCardSuit(hand[i * CARD_SIZE + 1]));
			}
			sb.append("]");
			return sb.toString();
		}
	}


	/********************************************************************************************
	 * IV. Example use
	 ********************************************************************************************/
	public static void main(String[] args) {
		System.out.println("Testing high card:");
		{
			Hand hand = new Hand();
			System.out.println(hand);
			hand.addCard(TWO_CLUBS);
			System.out.println(hand);
			hand.addCard(FOUR_DIAMONDS);
			System.out.println(hand);
			hand.addCard(FIVE_HEARTS);
			System.out.println(hand);
			hand.addCard(EIGHT_SPADES);
			System.out.println(hand);
			hand.addCard(NINE_CLUBS);
			System.out.println(hand);
		}

		System.out.println("Testing pair:");
		{
			Hand hand = new Hand();
			System.out.println(hand);
			hand.addCard(TWO_CLUBS);
			System.out.println(hand);
			hand.addCard(FOUR_DIAMONDS);
			System.out.println(hand);
			hand.addCard(TWO_HEARTS);
			System.out.println(hand);
			hand.addCard(EIGHT_SPADES);
			System.out.println(hand);
			hand.addCard(NINE_CLUBS);
			System.out.println(hand);
		}

		System.out.println("Testing two pairs:");
		{
			Hand hand = new Hand();
			System.out.println(hand);
			hand.addCard(TWO_CLUBS);
			System.out.println(hand);
			hand.addCard(EIGHT_DIAMONDS);
			System.out.println(hand);
			hand.addCard(TWO_HEARTS);
			System.out.println(hand);
			hand.addCard(EIGHT_SPADES);
			System.out.println(hand);
			hand.addCard(NINE_CLUBS);
			System.out.println(hand);
		}

		System.out.println("Testing triple:");
		{
			Hand hand = new Hand();
			System.out.println(hand);
			hand.addCard(TWO_CLUBS);
			System.out.println(hand);
			hand.addCard(TWO_DIAMONDS);
			System.out.println(hand);
			hand.addCard(TWO_HEARTS);
			System.out.println(hand);
			hand.addCard(EIGHT_SPADES);
			System.out.println(hand);
			hand.addCard(NINE_CLUBS);
			System.out.println(hand);
		}

		System.out.println("Testing straight:");
		{
			Hand hand = new Hand();
			System.out.println(hand);
			hand.addCard(TWO_CLUBS);
			System.out.println(hand);
			hand.addCard(FOUR_DIAMONDS);
			System.out.println(hand);
			hand.addCard(THREE_HEARTS);
			System.out.println(hand);
			hand.addCard(FIVE_SPADES);
			System.out.println(hand);
			hand.addCard(SIX_CLUBS);
			System.out.println(hand);
		}

		System.out.println("Testing straight with ace:");
		{
			Hand hand = new Hand();
			System.out.println(hand);
			hand.addCard(TWO_CLUBS);
			System.out.println(hand);
			hand.addCard(FOUR_DIAMONDS);
			System.out.println(hand);
			hand.addCard(THREE_HEARTS);
			System.out.println(hand);
			hand.addCard(FIVE_SPADES);
			System.out.println(hand);
			hand.addCard(ACE_CLUBS);
			System.out.println(hand);
		}

		System.out.println("Testing flush:");
		{
			Hand hand = new Hand();
			System.out.println(hand);
			hand.addCard(TWO_CLUBS);
			System.out.println(hand);
			hand.addCard(FOUR_CLUBS);
			System.out.println(hand);
			hand.addCard(THREE_CLUBS);
			System.out.println(hand);
			hand.addCard(FIVE_CLUBS);
			System.out.println(hand);
			hand.addCard(NINE_CLUBS);
			System.out.println(hand);
		}

		System.out.println("Testing full house:");
		{
			Hand hand = new Hand();
			System.out.println(hand);
			hand.addCard(TWO_CLUBS);
			System.out.println(hand);
			hand.addCard(FOUR_CLUBS);
			System.out.println(hand);
			hand.addCard(TWO_HEARTS);
			System.out.println(hand);
			hand.addCard(TWO_DIAMONDS);
			System.out.println(hand);
			hand.addCard(FOUR_SPADES);
			System.out.println(hand);
		}

		System.out.println("Testing quads:");
		{
			Hand hand = new Hand();
			System.out.println(hand);
			hand.addCard(TWO_CLUBS);
			System.out.println(hand);
			hand.addCard(FOUR_CLUBS);
			System.out.println(hand);
			hand.addCard(TWO_HEARTS);
			System.out.println(hand);
			hand.addCard(TWO_DIAMONDS);
			System.out.println(hand);
			hand.addCard(TWO_SPADES);
			System.out.println(hand);
		}

		System.out.println("Testing straight flush:");
		{
			Hand hand = new Hand();
			System.out.println(hand);
			hand.addCard(TWO_CLUBS);
			System.out.println(hand);
			hand.addCard(FOUR_CLUBS);
			System.out.println(hand);
			hand.addCard(THREE_CLUBS);
			System.out.println(hand);
			hand.addCard(FIVE_CLUBS);
			System.out.println(hand);
			hand.addCard(SIX_CLUBS);
			System.out.println(hand);
		}

		System.out.println("Testing royal flush:");
		{
			Hand hand = new Hand();
			System.out.println(hand);
			hand.addCard(ACE_CLUBS);
			System.out.println(hand);
			hand.addCard(TEN_CLUBS);
			System.out.println(hand);
			hand.addCard(JACK_CLUBS);
			System.out.println(hand);
			hand.addCard(QUEEN_CLUBS);
			System.out.println(hand);
			hand.addCard(KING_CLUBS);
			System.out.println(hand);
		}
	}
}
