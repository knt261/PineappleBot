package game;

import game.Poker.ComboType;
import game.Poker.Hand;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of open-faced crazy pineapple poker. A pineapple game is composed
 * of Player classes, which store the state of the player such as their score and
 * decision type.
 * 
 * Player decision types:
 * 
 * 	SCANNER means the player's play decision is governed by the input stream of the scanner --
 * 	if its System.in, then you type in what to play. If you set a player's decision type to
 * 	SCANNER, remember to also set the player's scanner with the setScanner() method.
 * 	See main() for example.
 * 
 * 	RANDOM is random card decision.
 * 
 * 	MONTE_CARLO_10000 is iterating through every possible move, simulating 10,000 possible
 * 	simulations per possible move and choosing the move with the highest expected score. A
 * 	simulation clones all visible state (e.g. faced up cards) up to the current point, then
 * 	makes every player choose cards with RANDOM decision type and plays until the end.
 * 
 * 	HEURISTIC_MONTE_CARLO_10000 is MONTE_CARLO_10000, but with the first turn being
 * 	decided by a heuristic strategy that I myself use, rather than monte carlo simulations.
 * 	If the first turn is in fantasy land, it simply enumerates all possible moves and choose
 * 	the one with the highest score. After the first turn, it behaves exactly like MONTE_CARLO_10000.
 * 	This option is created because MONTE_CARLO_10000 doesn't perform well on the first move, and it
 * 	takes a very long time to run.
 * 
 * Currently, since all output is printed to the same stream, everyone see the three cards
 * dealt to each player with DecisionType == SCanner every turn, thus sees which card that
 * player discards. Until we print to multiple difference output streams, I don't think we
 * have a way around this.
 * 
 * A pineapple round is composed of many turns, where every turn a player is dealt 3 cards (except
 * for the first turn). A round ends when all players have 3 cards in their top hand, 5 cards in their
 * middle hand, and 5 cards in their bottom hand. At the end of the round, the points are scored
 * and added to the player.
 * 
 * A pineapple game is composed of many rounds, where the points are accumulated across rounds.
 * You can start another round with the game.startNewRound() function. printToSystemOut is a switch
 * that prints the game information to System.out. debugPrintToSystemOut prints debug information
 * about the bots in addition to printToSystemOut.
 * 
 * @author Kevin Truong
 *
 */
public class Pineapple {
	private static final Logger LOGGER = Logger.getLogger(Pineapple.class.getName());

	/********************************************************************************************
	 * Table of contents:
	 *   I. Pineapple rules, constants, and helper functions
	 *   II. Player
	 *   III. Round
	 *   IV. Game
	 *   V. Example use
	 ********************************************************************************************/

	/********************************************************************************************
	 * I. Pineapple rules, constants, and helper functions
	 ********************************************************************************************/
	public static final int NUM_CARDS_DEALT_FIRST_TURN = 5;
	public static final int NUM_CARDS_DISCARDED_FIRST_TURN = 0;
	public static final int NUM_CARDS_DEALT_PER_TURN = 3;
	public static final int NUM_CARDS_DISCARDED_PER_TURN = 1;
	public static final int FANTASY_LAND_NUM_CARDS_TO_DEAL = 14;
	public static final int FANTASY_LAND_NUM_CARDS_TO_DISCARD = 1;

	private static final int WEAK_VALUE = 6;
	private static final int MEDIUM_VALUE = 10;
	
	public static final int MAX_TOP_HAND_SIZE = 3;
	public static final int MAX_MIDDLE_HAND_SIZE = 5;
	public static final int MAX_BOTTOM_HAND_SIZE = 5;
	
	public static final int NUM_HAND_POSITIONS = 3;
	public static final int TOP = 0;
	public static final int MIDDLE = 1;
	public static final int BOTTOM = 2;
	public static final int DISCARD = 3;

	private static final int[] topHandPairBonusScore = new int[Poker.ACE + 1];
	private static final int[] topHandThreeOfAKindBonusScore = new int[Poker.ACE + 1];
	private static final int[][] handPositionAndComboType2BonusScore = new int[NUM_HAND_POSITIONS][ComboType.values().length];
	static {
		topHandPairBonusScore[6] = 1;
		topHandPairBonusScore[7] = 2;
		topHandPairBonusScore[8] = 3;
		topHandPairBonusScore[9] = 4;
		topHandPairBonusScore[10] = 5;
		topHandPairBonusScore[Poker.JACK] = 6;
		topHandPairBonusScore[Poker.QUEEN] = 7;
		topHandPairBonusScore[Poker.KING] = 8;
		topHandPairBonusScore[Poker.ACE] = 9;
		topHandThreeOfAKindBonusScore[2] = 10;
		topHandThreeOfAKindBonusScore[3] = 11;
		topHandThreeOfAKindBonusScore[4] = 12;
		topHandThreeOfAKindBonusScore[5] = 13;
		topHandThreeOfAKindBonusScore[6] = 14;
		topHandThreeOfAKindBonusScore[7] = 15;
		topHandThreeOfAKindBonusScore[8] = 16;
		topHandThreeOfAKindBonusScore[9] = 17;
		topHandThreeOfAKindBonusScore[10] = 18;
		topHandThreeOfAKindBonusScore[Poker.JACK] = 19;
		topHandThreeOfAKindBonusScore[Poker.QUEEN] = 20;
		topHandThreeOfAKindBonusScore[Poker.KING] = 21;
		topHandThreeOfAKindBonusScore[Poker.ACE] = 22;
		
		handPositionAndComboType2BonusScore[MIDDLE][ComboType.ROYAL_FLUSH.ordinal()] = 50;
		handPositionAndComboType2BonusScore[MIDDLE][ComboType.STRAIGHT_FLUSH.ordinal()] = 30;
		handPositionAndComboType2BonusScore[MIDDLE][ComboType.FOUR_OF_A_KIND.ordinal()] = 20;
		handPositionAndComboType2BonusScore[MIDDLE][ComboType.FULL_HOUSE.ordinal()] = 12;
		handPositionAndComboType2BonusScore[MIDDLE][ComboType.FLUSH.ordinal()] = 8;
		handPositionAndComboType2BonusScore[MIDDLE][ComboType.STRAIGHT.ordinal()] = 4;
		handPositionAndComboType2BonusScore[MIDDLE][ComboType.THREE_OF_A_KIND.ordinal()] = 2;

		handPositionAndComboType2BonusScore[BOTTOM][ComboType.ROYAL_FLUSH.ordinal()] = 25;
		handPositionAndComboType2BonusScore[BOTTOM][ComboType.STRAIGHT_FLUSH.ordinal()] = 15;
		handPositionAndComboType2BonusScore[BOTTOM][ComboType.FOUR_OF_A_KIND.ordinal()] = 10;
		handPositionAndComboType2BonusScore[BOTTOM][ComboType.FULL_HOUSE.ordinal()] = 6;
		handPositionAndComboType2BonusScore[BOTTOM][ComboType.FLUSH.ordinal()] = 4;
		handPositionAndComboType2BonusScore[BOTTOM][ComboType.STRAIGHT.ordinal()] = 2;		
	}
	
	public static int getBonusScore(Hand hand, int handPosition) {
		if (handPosition == MIDDLE || handPosition == BOTTOM) {
			return handPositionAndComboType2BonusScore[handPosition][hand.getComboType().ordinal()];
		} else {
			// top hand has more specific rules
			if (hand.getComboType() == ComboType.THREE_OF_A_KIND) {
				return topHandThreeOfAKindBonusScore[hand.getComboCardValue(0)];
			} else if (hand.getComboType() == ComboType.PAIR) {
				return topHandPairBonusScore[hand.getComboCardValue(0)];
			} else {
				return 0;
			}
		}
	}

	public static boolean goesToFantasyLand(Hand topHand) {
		return getBonusScore(topHand, TOP) >= topHandPairBonusScore[Poker.QUEEN];
	}
	
	public static boolean staysInFantasyLand(Hand topHand, Hand middleHand, Hand bottomHand) {
		return (getBonusScore(topHand, TOP) >= topHandThreeOfAKindBonusScore[2] ||
				getBonusScore(middleHand, MIDDLE) >= handPositionAndComboType2BonusScore[MIDDLE][ComboType.FULL_HOUSE.ordinal()] ||
				getBonusScore(bottomHand, BOTTOM) >= handPositionAndComboType2BonusScore[BOTTOM][ComboType.FOUR_OF_A_KIND.ordinal()]);
	}
	
	public static boolean isFouled(Hand topHand, Hand middleHand, Hand bottomHand) {
		return (topHand.compareTo(middleHand) < 0 || middleHand.compareTo(bottomHand) < 0);
	}
	
	
	public static enum DecisionType {SCANNER, RANDOM, MONTE_CARLO_10000, HEURISTIC_MONTE_CARLO_10000;
		public int getNumMonteCarloSamples() {
			if (this == MONTE_CARLO_10000) {
				return 10000;
			} else if (this == HEURISTIC_MONTE_CARLO_10000) {
				return 10000;
			} else {
				return -1;
			}
		}
	};
	
	
	/**
	 * @param currHandPositions
	 * @return false if there is no more possible hand positions
	 */
	private static boolean nextPossibleHandPosition(int[] currHandPositions, int playerIdx, Round round) {
		currHandPositions[currHandPositions.length - 1]++;
		
		int highestPosition;
		if (round.getNumTurn() == 0 && !round.getPlayers()[playerIdx].isInFantasyLand()) {
			highestPosition = BOTTOM;
		} else {
			highestPosition = DISCARD;
		}
		for (int i = currHandPositions.length - 1; i >= 1; i--) {
			if (currHandPositions[i] > highestPosition) {
				currHandPositions[i] = 0;
				currHandPositions[i - 1]++;
			} else {
				break;
			}
		}
		if (currHandPositions[0] > highestPosition) {
			return false;
		}
		return true;
	}

	private static boolean isLegalHandPosition(int[] handPositions, int playerIdx, Round round, boolean printToSystemOut) {
		int numTopHand = 0;
		int numMiddleHand = 0;
		int numBottomHand = 0;
		int numDiscards = 0;
		for (int i = 0; i < handPositions.length; i++) {
			int handPositionToPlay = handPositions[i];
			if (handPositionToPlay == TOP) {
				numTopHand++;
			} else if (handPositionToPlay == MIDDLE) {
				numMiddleHand++;
			} else if (handPositionToPlay == BOTTOM) {
				numBottomHand++;
			} else if (handPositionToPlay == DISCARD) {
				numDiscards++;
			} else {
				if (printToSystemOut) {
					LOGGER.log(Level.WARNING, "Unknown hand position to play \"" + handPositionToPlay + "\". Redoing computeHandPositions() loop");
				}
				continue;
			}
		}

		if (round.getPlayers()[playerIdx].isInFantasyLand()) {
			if (round.getNumTurn() == 0) {
				if (numTopHand != 3 || numMiddleHand != 5 || numBottomHand != 5 || numDiscards != 1) {
					if (printToSystemOut) {
						System.out.println("      You are in fantasy land! Must play 3 cards top hand, 5 cards middle hand, 5 cards bottom hand, and 1 card discarded.");
						System.out.println("      Replay the dealt hands.");
					}
					return false;
				} else {
					return true;
				}
			} else{
				LOGGER.log(Level.SEVERE, "Player " + round.getPlayers()[playerIdx].getId() + " is in fantasy land and is playing a hand after the first turn (numTurn=" + round.getNumTurn() + ")! This should never happen. Returning false...");
				return false;
			}
		} else {
			if (round.getNumTurn() == 0 && numDiscards != NUM_CARDS_DISCARDED_FIRST_TURN) {
				if (printToSystemOut) {
					System.out.println("      Cannot discard any card on first turn.");
					System.out.println("      Replay the dealt hands.");
				}
				return false;
			} else if (round.getNumTurn() > 0 && numDiscards != NUM_CARDS_DISCARDED_PER_TURN) {
				if (printToSystemOut) {
					System.out.println("      Must discard 1 card every turn (after the first turn)");
					System.out.println("      Replay the dealt hands.");
				}
				return false;
			} else if (numTopHand + round.getTopHands()[playerIdx].getNumCards() > 3) {
				if (printToSystemOut) {
					System.out.println("      Too many cards in top hand! (numTopHand=" + numTopHand + ") + (player.topHand.cards.size()=" + round.getTopHands()[playerIdx].getNumCards() + ") > 3!");
					System.out.println("      Replay the dealt hands.");
				}
				return false;
			} else if (numMiddleHand + round.getMiddleHands()[playerIdx].getNumCards() > 5) {
				if (printToSystemOut) {
					System.out.println("      Too many cards in middle hand! (numMiddleHand=" + numMiddleHand + ") + (player.middleHand.cards.size()=" + round.getMiddleHands()[playerIdx].getNumCards() + ") > 5!");
					System.out.println("      Replay the dealt hands.");
				}
				return false;
			} else if (numBottomHand + round.getBottomHands()[playerIdx].getNumCards() > 5) {
				if (printToSystemOut) {
					System.out.println("      Too many cards in bottom hand! (numBottomHand=" + numBottomHand + ") + (player.bottomHand.cards.size()=" + round.getBottomHands()[playerIdx].getNumCards() + ") > 5!");
					System.out.println("      Replay the dealt hands.");
				}
				return false;
			} else {
				return true;
			}
		}
	}

	private static int[] getRandomHandPositions(int[] cardIdxsToDeal, int playerIdx, Round round) {
		int numTopPositionsLeft = MAX_TOP_HAND_SIZE - round.getTopHands()[playerIdx].getNumCards();
		int numMiddlePositionsLeft = MAX_MIDDLE_HAND_SIZE - round.getMiddleHands()[playerIdx].getNumCards();
		int numBottomPositionsLeft = MAX_BOTTOM_HAND_SIZE - round.getBottomHands()[playerIdx].getNumCards();
		int numMovesAvailable = 0;
		if (numTopPositionsLeft > 0) {
			numMovesAvailable++;
		}
		if (numMiddlePositionsLeft > 0) {
			numMovesAvailable++;
		}
		if (numBottomPositionsLeft > 0) {
			numMovesAvailable++;
		}
		
		int[] handPositions = new int[cardIdxsToDeal.length];
		int discardIdx = -1;
		if (round.getNumTurn() > 0 || round.getPlayers()[playerIdx].isInFantasyLand()) {
			discardIdx = Poker.RANDOM_SEED.nextInt(handPositions.length);
		}
		for (int i = 0; i < cardIdxsToDeal.length; i++) {
			if (i == discardIdx) {
				handPositions[i] = DISCARD;
				continue;
			}
			
			int randInt = Poker.RANDOM_SEED.nextInt(numMovesAvailable);
			int nextMove = randInt;
			if (nextMove >= TOP && numTopPositionsLeft == 0) {
				nextMove++;
			}
			if (nextMove >= MIDDLE && numMiddlePositionsLeft == 0) {
				nextMove++;
			}
			assert(nextMove != BOTTOM || numBottomPositionsLeft > 0);
			assert(nextMove <= BOTTOM);
			
			if (nextMove == TOP) {
				assert(numTopPositionsLeft > 0);
				numTopPositionsLeft--;
				if (numTopPositionsLeft == 0) {
					numMovesAvailable--;
				}
			} else if (nextMove == MIDDLE) {
				assert(numMiddlePositionsLeft > 0);
				numMiddlePositionsLeft--;
				if (numMiddlePositionsLeft == 0) {
					numMovesAvailable--;
				}
			} else if (nextMove == BOTTOM ){
				assert(numBottomPositionsLeft > 0);
				numBottomPositionsLeft--;
				if (numBottomPositionsLeft == 0) {
					numMovesAvailable--;
				}
			} else {
				LOGGER.log(Level.SEVERE, "nextMove=" + nextMove + "! This should never happen. Going to ignore this and hope nothing breaks...");
			}
			
			handPositions[i] = nextMove;
		}
		return handPositions;
	}
	
	/********************************************************************************************
	 * II. Player
	 ********************************************************************************************/
	public static class Player {
		private final String id;
		private DecisionType decisionType;
		private int score;
		
		private boolean isInFantasyLand;

		private Scanner scanner = null;
		
		public Player(String id, DecisionType decisionType) {
			this(id, decisionType, 0, false);
		}
		
		public Player(String id, DecisionType decisionType, int score, boolean isInFantasyLand) {
			this.id = id;
			this.decisionType = decisionType;
			this.score = score;
			this.isInFantasyLand = isInFantasyLand;
		}
		
		public Player(Player playerToClone) {
			this.id = playerToClone.id;
			this.decisionType = playerToClone.decisionType;
			this.score = playerToClone.score;
			this.isInFantasyLand = playerToClone.isInFantasyLand;
			this.scanner = playerToClone.scanner;
		}

		public String getId() {
			return this.id;
		}

		public int getScore() {
			return this.score;
		}
		
		public boolean isInFantasyLand() {
			return this.isInFantasyLand;
		}
		
		public void setIsInFantasyLand(boolean isInFantasyLand) {
			this.isInFantasyLand = isInFantasyLand;
		}
		
		public void addScore(int scoreDelta) {
			this.score += scoreDelta;
		}
		
		public DecisionType getDecisionType() {
			return this.decisionType;
		}
		
		public void setScanner(Scanner scanner) {
			this.scanner = scanner;
		}
		
		public void setDecisionType(DecisionType decisionType) {
			this.decisionType = decisionType;
		}
		
		/**
		 * The strategy of the player. Given the visible board state, decides which
		 * cards to play in what hand, governed by the player's DecisionType.
		 * 
		 * @param cardIdxsToDeal
		 * @param playerIdx
		 * @param round
		 * @return
		 */
		public int[] computeHandPositions(int[] cardIdxsToDeal, int playerIdx, Round round) {
			if (this.decisionType == DecisionType.SCANNER) {
				if (round.getPrintToSystemOut()) {
					System.out.println("      Play cards, one number per card dealt. 0 = top hand, 1 = middle hand, 2 = bottom hand, 3 = discard card. Example: 1 2 2");
				}
				assert(this.scanner != null);
				int[] handPositions = new int[cardIdxsToDeal.length];
				int handPositionsIdx = 0;
				while (handPositionsIdx < cardIdxsToDeal.length && this.scanner.hasNextInt()) {
					int handPositionToPlay = this.scanner.nextInt();
					if (handPositionToPlay == TOP) {
						handPositions[handPositionsIdx] = handPositionToPlay;
						handPositionsIdx++;
					} else if (handPositionToPlay == MIDDLE) {
						handPositions[handPositionsIdx] = handPositionToPlay;
						handPositionsIdx++;
					} else if (handPositionToPlay == BOTTOM) {
						handPositions[handPositionsIdx] = handPositionToPlay;
						handPositionsIdx++;
					} else if (handPositionToPlay == DISCARD) {
						handPositions[handPositionsIdx] = DISCARD;
						handPositionsIdx++;
					} else {
						if (round.getPrintToSystemOut()) {
							System.out.println("      You can only choose between hand positions 0, 1, or 2, or discard with 3! You picked \"" + handPositionToPlay + "\"");
						}
					}
				}
				return handPositions;
			} else if (this.decisionType == DecisionType.RANDOM) {
				int[] handPositions = getRandomHandPositions(cardIdxsToDeal, playerIdx, round);
				if (round.getPrintToSystemOut() && round.getDebugPrintToSystemOut()) {
					System.out.println("      Player " + this.id + " (" + this.decisionType.toString() + ") hand positions: " + Arrays.toString(handPositions));
				}
				return handPositions;
			} else if (this.decisionType == DecisionType.MONTE_CARLO_10000) {
				int numSamples = this.decisionType.getNumMonteCarloSamples();
				Map<Integer, Double> handPositionsHash2TotalHandPositionsScore = new HashMap<Integer, Double>();
				Map<Integer, Integer> handPositionsHash2Count = new HashMap<Integer, Integer>();
				
				int[] currHandPositions = new int[cardIdxsToDeal.length];
				while (nextPossibleHandPosition(currHandPositions, playerIdx, round)) {
					if (!isLegalHandPosition(currHandPositions, playerIdx, round, false)) {
						continue;
					}

					for (int i = 0; i < numSamples; i++) {
						int numPlayers = round.getPlayers().length;
						Player[] randomPlayers = new Player[numPlayers];
						for (int j = 0; j < numPlayers; j++) {
							Player player = round.getPlayers()[j];
							Player clonedPlayer = new Player(player.getId(), DecisionType.RANDOM, 0, player.isInFantasyLand());
							randomPlayers[j] = clonedPlayer;
						}

						Round clonedRound = new Round(round, randomPlayers, false, false, true);

						// Finish the current hand
						clonedRound.playCardsForPlayer(currHandPositions, cardIdxsToDeal, playerIdx);
						if (clonedRound.getNumTurn() == 0) {
							clonedRound.startRound(playerIdx + 1);
						} else {
							clonedRound.nextTurn(playerIdx + 1);
						}

						// Finish the rest of the round
						while (!clonedRound.isFinished()) {
							clonedRound.nextTurn();
						}
						clonedRound.endRound();

						Integer minScore = null;
						for (Player player : randomPlayers) {
							if (minScore == null || player.getScore() < minScore) {
								minScore = player.getScore();
							}
						}
						assert(minScore != null);
						int totalScoreNormalized = 0;
						for (Player player : randomPlayers) {
							totalScoreNormalized += (player.getScore() - minScore);
						}

						double handPositionsScore;
						if (totalScoreNormalized == 0) {
							handPositionsScore = 0;
						} else {
							handPositionsScore = (randomPlayers[playerIdx].getScore() - minScore) / ((double)totalScoreNormalized);
						}

						int handPositionsHash = 0;
						for (int j = 0; j < currHandPositions.length; j++) {
							handPositionsHash = 10 * handPositionsHash + currHandPositions[j];
						}
						Double oldScore = handPositionsHash2TotalHandPositionsScore.get(handPositionsHash);
						if (oldScore == null) {
							oldScore = 0d;
						}
						handPositionsHash2TotalHandPositionsScore.put(handPositionsHash, oldScore + handPositionsScore);

						Integer oldCount = handPositionsHash2Count.get(handPositionsHash);
						if (oldCount == null) {
							oldCount = 0;
						}
						handPositionsHash2Count.put(handPositionsHash, oldCount + 1);
					}
				}
				
				assert(handPositionsHash2TotalHandPositionsScore.size() > 0);
				int[] bestHandPositions = null;
				Double bestScore = null;
				int bestHandPositionsCount = -1;
				for (Map.Entry<Integer, Double> entry : handPositionsHash2TotalHandPositionsScore.entrySet()) {
					Integer handPositionsHash = entry.getKey();
					double totalScore = entry.getValue();
					int count = handPositionsHash2Count.get(handPositionsHash);
					double score = totalScore / count;
					
					int[] handPositions = new int[cardIdxsToDeal.length];
					for (int i = handPositions.length - 1; i >= 0; i --) {  // reverse the hash
						handPositions[i] = handPositionsHash % 10;
						handPositionsHash /= 10;
					}
					assert(handPositionsHash == 0);
					
					if (bestScore == null || score > bestScore) {
						bestScore = score;
						bestHandPositions = handPositions;
						bestHandPositionsCount = count;
					}
				}
				assert(bestHandPositions != null);

				if (round.getPrintToSystemOut() && round.getDebugPrintToSystemOut()) {
					System.out.println("      Player " + this.id + " (" + this.decisionType.toString() + ") hand positions: " + Arrays.toString(bestHandPositions) + " (bestScore=" + bestScore + ", count=" + bestHandPositionsCount + ")");
				}
				return bestHandPositions;
			} else if (this.decisionType == DecisionType.HEURISTIC_MONTE_CARLO_10000) {
				int numSamples = this.decisionType.getNumMonteCarloSamples();
				if (round.getNumTurn() == 0) { // Use heuristics on the first turn. monte carlo itself doesnt do very well even with 100,000 samples, and its very slow
					int[] handPositions = null;
					int[][] dealtCards = new int[cardIdxsToDeal.length][];
					for (int i = 0; i < cardIdxsToDeal.length; i++) {
						dealtCards[i] = round.getFullDeck()[cardIdxsToDeal[i]];
					}
					
					if (round.getPlayers()[playerIdx].isInFantasyLand()) {
						int[] bestHandPositions = null;
						Integer bestHandPositionsScore = null;
						int[] currHandPositions = new int[cardIdxsToDeal.length];
						currHandPositions[currHandPositions.length - 1] = -1; // going to increment in the while check loop, so dont skip the first hand positions
						while (nextPossibleHandPosition(currHandPositions, playerIdx, round)) {
							if (!isLegalHandPosition(currHandPositions, playerIdx, round, false)) {
								continue;
							}
							Hand topHand = new Hand();
							Hand middleHand = new Hand();
							Hand bottomHand = new Hand();
							for (int i = 0; i < currHandPositions.length; i++) {
								int handPositionToPlay = currHandPositions[i];
								if (handPositionToPlay == TOP) {
									topHand.addCard(dealtCards[i]);
								} else if (handPositionToPlay == MIDDLE) {
									middleHand.addCard(dealtCards[i]);
								} else if (handPositionToPlay == BOTTOM) {
									bottomHand.addCard(dealtCards[i]);									
								} else if (handPositionToPlay == DISCARD) {
									// Don't add it to any hand
								} else {
									if (round.getPrintToSystemOut()) {
										LOGGER.log(Level.WARNING, "Unknown hand position to play \"" + handPositionToPlay + "\". Don't know what to do -- skipping loop and hoping nothing breaks.");
									}
									continue;
								}
							}
							int totalBonusScore;
							if (isFouled(topHand, middleHand, bottomHand)) {
								totalBonusScore = -1; // a non-fouled bonus score of 0 should take priority over a fouled hand
							} else {
								totalBonusScore = getBonusScore(topHand, TOP) + getBonusScore(middleHand, MIDDLE) + getBonusScore(bottomHand, BOTTOM);
							}
							if (bestHandPositionsScore == null || totalBonusScore > bestHandPositionsScore) {
								bestHandPositions = new int[currHandPositions.length];
								System.arraycopy(currHandPositions, 0, bestHandPositions, 0, currHandPositions.length);
								bestHandPositionsScore = totalBonusScore;
							}
						}
						handPositions = bestHandPositions;
					} else {
						assert(dealtCards.length <= 5);
						int[] suit2Count = new int[4];
						for (int[] card : dealtCards) {
							suit2Count[card[Poker.SUIT]]++;
						}
						Integer suitOfMaxSameSuitCards = null;
						Integer maxSameSuitCards = null;
						for (int i = 0; i < suit2Count.length; i++) {
							int count = suit2Count[i];
							if (maxSameSuitCards == null || count > maxSameSuitCards) {
								maxSameSuitCards = count;
								suitOfMaxSameSuitCards = i;
							}
						}
						assert(suitOfMaxSameSuitCards != null && maxSameSuitCards != null);
						
						Hand hand = new Hand();
						for (int[] card : dealtCards) {
							hand.addCard(card);
						}
						
						Integer maxConsecutiveCardLength = null;
						Integer startingIdxOfMaxConsecutiveCardLength = null;
						Integer currConsecutiveCardLength = null;
						Integer currStartingIdxOfConsecutiveCards = null;
						for (int i = 0; i < hand.getNumCards() - 1; i++) {
							if (hand.getCardValue(i) - hand.getCardValue(i + 1) == 1) {
								if (currConsecutiveCardLength == null) {
									currConsecutiveCardLength = 1;
									currStartingIdxOfConsecutiveCards = i;
								} else {
									currConsecutiveCardLength++;
								}
							} else {
								if (currConsecutiveCardLength != null) {
									if (maxConsecutiveCardLength == null ||
											currConsecutiveCardLength > maxConsecutiveCardLength) {
										maxConsecutiveCardLength = currConsecutiveCardLength;
										startingIdxOfMaxConsecutiveCardLength = currStartingIdxOfConsecutiveCards;
									}
									currConsecutiveCardLength = null;
									currStartingIdxOfConsecutiveCards = null;
								}
							}
						}
						if (currConsecutiveCardLength != null) {
							if (maxConsecutiveCardLength == null ||
									currConsecutiveCardLength > maxConsecutiveCardLength) {
								maxConsecutiveCardLength = currConsecutiveCardLength;
								startingIdxOfMaxConsecutiveCardLength = currStartingIdxOfConsecutiveCards;
							}
							currConsecutiveCardLength = null;
							currStartingIdxOfConsecutiveCards = null;
						} else {
							maxConsecutiveCardLength = 0;
						}
						
						
						if (hand.getComboType() == ComboType.ROYAL_FLUSH ||
								hand.getComboType() == ComboType.STRAIGHT_FLUSH) {
							handPositions = new int[cardIdxsToDeal.length];
							for (int i = 0; i < handPositions.length; i++) {
								handPositions[i] = BOTTOM;
							}
						} else if (hand.getComboType() == ComboType.FOUR_OF_A_KIND) {
							handPositions = new int[cardIdxsToDeal.length];
							for (int i = 0; i < dealtCards.length; i++) {
								int value = dealtCards[i][Poker.VALUE];
								if (value == hand.getComboCardValue(0)) {
									handPositions[i] = BOTTOM;
								} else {
									if (value < WEAK_VALUE) {
										handPositions[i] = BOTTOM; // weak cards at the bottom
									} else if (value < MEDIUM_VALUE) {
										handPositions[i] = TOP; // medium cards at the top, don't want it to be too strong for higher chance of fouling
									} else {
										handPositions[i] = MIDDLE;
									}
								}
							}
						} else if (hand.getComboType() == ComboType.FULL_HOUSE ||
								hand.getComboType() == ComboType.FLUSH ||
								hand.getComboType() == ComboType.STRAIGHT) {
							handPositions = new int[cardIdxsToDeal.length];
							for (int i = 0; i < handPositions.length; i++) {
								handPositions[i] = BOTTOM;
							}
						} else if (hand.getComboType() == ComboType.TWO_PAIRS) {
							handPositions = new int[cardIdxsToDeal.length];
							for (int i = 0; i < dealtCards.length; i++) {
								int value = dealtCards[i][Poker.VALUE];
								if (value == hand.getComboCardValue(0) ||
										value == hand.getComboCardValue(hand.getNumComboCards() - 1)) {
									handPositions[i] = BOTTOM;
								} else {
									if (value < MEDIUM_VALUE) {
										handPositions[i] = TOP; // medium cards at the top, don't want it to be too strong for higher chance of fouling
									} else {
										handPositions[i] = MIDDLE;
									}
								}
							}
						} else if (hand.getComboType() == ComboType.THREE_OF_A_KIND) {
							handPositions = new int[cardIdxsToDeal.length];
							for (int i = 0; i < dealtCards.length; i++) {
								int value = dealtCards[i][Poker.VALUE];
								if (value == hand.getComboCardValue(0)) {
									handPositions[i] = BOTTOM;
								} else {
									if (value < MEDIUM_VALUE) {
										handPositions[i] = TOP; // medium cards at the top, don't want it to be too strong for higher chance of fouling
									} else {
										handPositions[i] = MIDDLE;
									}
								}
							}
						} else if (maxSameSuitCards == 4) {
							handPositions = new int[cardIdxsToDeal.length];
							for (int i = 0; i < dealtCards.length; i++) {
								int suit = dealtCards[i][Poker.SUIT];
								if (suit == suitOfMaxSameSuitCards) {
									handPositions[i] = BOTTOM;
								} else {
									int value = dealtCards[i][Poker.VALUE];
									if (value < MEDIUM_VALUE) {
										handPositions[i] = TOP; // medium cards at the top, don't want it to be too strong for higher chance of fouling
									} else {
										handPositions[i] = MIDDLE;
									}
								}
							}
						} else if (maxConsecutiveCardLength == 4) {
							handPositions = new int[cardIdxsToDeal.length];
							for (int i = 0; i < dealtCards.length; i++) {
								if (i >= startingIdxOfMaxConsecutiveCardLength &&
										i < startingIdxOfMaxConsecutiveCardLength + maxConsecutiveCardLength) {
									handPositions[i] = BOTTOM;
								} else {
									int value = dealtCards[i][Poker.VALUE];
									if (value < MEDIUM_VALUE) {
										handPositions[i] = TOP; // medium cards at the top, don't want it to be too strong for higher chance of fouling
									} else {
										handPositions[i] = MIDDLE;
									}
								}
							}
						} else if (maxSameSuitCards == 3) {
							handPositions = new int[cardIdxsToDeal.length];
							for (int i = 0; i < dealtCards.length; i++) {
								int suit = dealtCards[i][Poker.SUIT];
								if (suit == suitOfMaxSameSuitCards) {
									handPositions[i] = BOTTOM;
								} else {
									int value = dealtCards[i][Poker.VALUE];
									if (value < MEDIUM_VALUE) {
										handPositions[i] = TOP; // medium cards at the top, don't want it to be too strong for higher chance of fouling
									} else {
										handPositions[i] = MIDDLE;
									}
								}
							}
						} else if (maxConsecutiveCardLength == 3) {
							handPositions = new int[cardIdxsToDeal.length];
							for (int i = 0; i < dealtCards.length; i++) {
								if (i >= startingIdxOfMaxConsecutiveCardLength &&
										i < startingIdxOfMaxConsecutiveCardLength + maxConsecutiveCardLength) {
									handPositions[i] = BOTTOM;
								} else {
									int value = dealtCards[i][Poker.VALUE];
									if (value < MEDIUM_VALUE) {
										handPositions[i] = TOP; // medium cards at the top, don't want it to be too strong for higher chance of fouling
									} else {
										handPositions[i] = MIDDLE;
									}
								}
							}
						} else if (hand.getComboType() == ComboType.PAIR) {
							handPositions = new int[cardIdxsToDeal.length];
							int numBottomCards = 0;
							int numTopCards = 0;
							for (int i = 0; i < dealtCards.length; i++) {
								int value = dealtCards[i][Poker.VALUE];
								if (value == hand.getComboCardValue(0)) {
									handPositions[i] = BOTTOM;
									numBottomCards++;
								} else {
									if (value < WEAK_VALUE && numBottomCards < 3) {
										handPositions[i] = BOTTOM;
										numBottomCards++;
									} else if (value < MEDIUM_VALUE && numTopCards < 2) {
										handPositions[i] = TOP; // medium cards at the top, don't want it to be too strong for higher chance of fouling
										numTopCards++;
									} else {
										handPositions[i] = MIDDLE;
									}
								}
							}
						} else {
							int numSuitsOfTwo = 0;
							for (int count : suit2Count) {
								if (count >= 2) {
									numSuitsOfTwo++;
								}
							}
							if (numSuitsOfTwo == 1) {
								handPositions = new int[cardIdxsToDeal.length];
								int numTopCards = 0;
								for (int i = 0; i < dealtCards.length; i++) {
									int suit = dealtCards[i][Poker.SUIT];
									if (suit == suitOfMaxSameSuitCards) {
										handPositions[i] = BOTTOM;
									} else {
										int value = dealtCards[i][Poker.VALUE];
										if (value < MEDIUM_VALUE && numTopCards < 2) {
											handPositions[i] = TOP; // medium cards at the top, don't want it to be too strong for higher chance of fouling
											numTopCards++;
										} else {
											handPositions[i] = MIDDLE;
										}
									}
								}
							} else if (numSuitsOfTwo == 2) {
								Integer highestSuitedCardValue = null;
								Integer suitOfHighestSuitedCardValue = null;
								for (int[] card: dealtCards) {
									int suit = card[Poker.SUIT];
									if (suit2Count[suit] == 2) {
										int value = card[Poker.VALUE];
										if (highestSuitedCardValue == null || value > highestSuitedCardValue) {
											highestSuitedCardValue = value;
											suitOfHighestSuitedCardValue = suit;
										}
									}
								}
								assert(highestSuitedCardValue != null);
								assert(suitOfHighestSuitedCardValue != null);
								Integer suitOfLowerSuitedCardValue = null;
								for (int i = 0; i < suit2Count.length; i++) {
									if (i == suitOfHighestSuitedCardValue) {
										continue;
									}
									if (suit2Count[i] == 2) {
										suitOfLowerSuitedCardValue = i;
										break;
									}
								}

								handPositions = new int[cardIdxsToDeal.length];
								for (int i = 0; i < dealtCards.length; i++) {
									int suit = dealtCards[i][Poker.SUIT];
									if (suit == suitOfHighestSuitedCardValue) {
										handPositions[i] = BOTTOM;
									} else if (suit == suitOfLowerSuitedCardValue) {
										handPositions[i] = MIDDLE;
									} else {
										handPositions[i] = TOP;
									}
								}
							}
						}
					}

					assert(handPositions != null);
					if (round.getPrintToSystemOut() && round.getDebugPrintToSystemOut()) {
						System.out.println("      Player " + this.id + " (" + this.decisionType.toString() + ") hand positions: " + Arrays.toString(handPositions) + " (HEURISTIC)");
					}
					return handPositions;
				} else {
					Map<Integer, Double> handPositionsHash2TotalHandPositionsScore = new HashMap<Integer, Double>();
					Map<Integer, Integer> handPositionsHash2Count = new HashMap<Integer, Integer>();

					int[] currHandPositions = new int[cardIdxsToDeal.length];
					currHandPositions[currHandPositions.length - 1] = -1; // going to increment in the while check loop, so dont skip the first hand positions
					while (nextPossibleHandPosition(currHandPositions, playerIdx, round)) {
						if (!isLegalHandPosition(currHandPositions, playerIdx, round, false)) {
							continue;
						}

						for (int i = 0; i < numSamples; i++) {
							int numPlayers = round.getPlayers().length;
							Player[] randomPlayers = new Player[numPlayers];
							for (int j = 0; j < numPlayers; j++) {
								Player player = round.getPlayers()[j];
								Player clonedPlayer = new Player(player.getId(), DecisionType.RANDOM, 0, player.isInFantasyLand());
								randomPlayers[j] = clonedPlayer;
							}

							Round clonedRound = new Round(round, randomPlayers, false, false, true);

							// Finish the current hand
							clonedRound.playCardsForPlayer(currHandPositions, cardIdxsToDeal, playerIdx);
							if (clonedRound.getNumTurn() == 0) {
								clonedRound.startRound(playerIdx + 1);
							} else {
								clonedRound.nextTurn(playerIdx + 1);
							}

							// Finish the rest of the round
							while (!clonedRound.isFinished()) {
								clonedRound.nextTurn();
							}
							clonedRound.endRound();

							Integer minScore = null;
							for (Player player : randomPlayers) {
								if (minScore == null || player.getScore() < minScore) {
									minScore = player.getScore();
								}
							}
							assert(minScore != null);
							int totalScoreNormalized = 0;
							for (Player player : randomPlayers) {
								totalScoreNormalized += (player.getScore() - minScore);
							}

							double handPositionsScore;
							if (totalScoreNormalized == 0) {
								handPositionsScore = 0;
							} else {
								handPositionsScore = (randomPlayers[playerIdx].getScore() - minScore) / ((double)totalScoreNormalized);
							}

							int handPositionsHash = 0;
							for (int j = 0; j < currHandPositions.length; j++) {
								handPositionsHash = 10 * handPositionsHash + currHandPositions[j];
							}
							Double oldScore = handPositionsHash2TotalHandPositionsScore.get(handPositionsHash);
							if (oldScore == null) {
								oldScore = 0d;
							}
							handPositionsHash2TotalHandPositionsScore.put(handPositionsHash, oldScore + handPositionsScore);

							Integer oldCount = handPositionsHash2Count.get(handPositionsHash);
							if (oldCount == null) {
								oldCount = 0;
							}
							handPositionsHash2Count.put(handPositionsHash, oldCount + 1);
						}
					}

					assert(handPositionsHash2TotalHandPositionsScore.size() > 0);
					int[] bestHandPositions = null;
					Double bestScore = null;
					int bestHandPositionsCount = -1;
					for (Map.Entry<Integer, Double> entry : handPositionsHash2TotalHandPositionsScore.entrySet()) {
						Integer handPositionsHash = entry.getKey();
						double totalScore = entry.getValue();
						int count = handPositionsHash2Count.get(handPositionsHash);
						double score = totalScore / count;

						int[] handPositions = new int[cardIdxsToDeal.length];
						for (int i = handPositions.length - 1; i >= 0; i --) {  // reverse the hash
							handPositions[i] = handPositionsHash % 10;
							handPositionsHash /= 10;
						}
						assert(handPositionsHash == 0);

						if (bestScore == null || score > bestScore) {
							bestScore = score;
							bestHandPositions = handPositions;
							bestHandPositionsCount = count;
						}
					}
					assert(bestHandPositions != null);
					if (round.getPrintToSystemOut() && round.getDebugPrintToSystemOut()) {
						System.out.println("      Player " + this.id + " (" + this.decisionType.toString() + ") hand positions: " + Arrays.toString(bestHandPositions) + " (bestScore=" + bestScore + ", count=" + bestHandPositionsCount + ")");
					}
					return bestHandPositions;
				}
			} else {
				return null;
			} 
		}
	}
	

	/********************************************************************************************
	 * III. Round
	 ********************************************************************************************/
	/**
	 * This class is meant to be used in the following pattern:
	 * 
	 *   Round round = new Round(args...);
	 *   round.startRound(); // each player is dealt 5 cards, or 14 if in fantasy land
	 *   while (!round.isFinished()) {
	 *     round.nextTurn(); // each player not in fantasy land is dealt 3 cards
	 *   }
	 *   round.endRound(); // collections the head-to-head scores and updates palyer scores
	 * 
	 * round.startRound(), round.nextTurn(), and round.playTurn() can also accept
	 * an index of the player to start, if you want to skip the first few players.
	 * This is useful if you want to break a turn up into multiple parts, such
	 * as when doing monte carlo simulations.
	 * 
	 * @author Kevin Truong
	 */
	public static class Round {
		
		private final Player[] players;
		private final boolean[] isInFantasyLand;
		private int numPlayersInFantasyLand = 0;
		
		private final int[][] fullDeck;
		private int numCardsDrawn;
		private int numTurn;
		private final int[] shuffledCardOrder;
		private int shuffledCardOrderIdx;
		private final boolean[] visiblePlayedCardsInDeck;

		private final Hand[] topHands;
		private final Hand[] middleHands;
		private final Hand[] bottomHands;
		
		private final boolean printToSystemOut;
		private final boolean debugPrintToSystemOut;
		
		private boolean isFinished;

		public Round(Player[] players, int[][] fullDeck, int numCardsDrawn, int numTurn, int[] shuffledCardOrder, boolean[] playedCardsInDeck, boolean printSystemOut, boolean debugPrintToSystemOut) {
			assert(players.length <= 3);
			this.players = players;
			this.isInFantasyLand = new boolean[this.players.length];
			for (int i = 0; i < this.players.length; i++) {
				if (this.players[i].isInFantasyLand()) {
					this.isInFantasyLand[i] = true;
					this.numPlayersInFantasyLand++;
				}
			}
			
			this.fullDeck = fullDeck;
			this.numCardsDrawn = numCardsDrawn;
			this.numTurn = numTurn;
			this.shuffledCardOrder = shuffledCardOrder;
			this.shuffledCardOrderIdx = 0;
			this.visiblePlayedCardsInDeck = playedCardsInDeck;
			
			this.topHands = new Hand[this.players.length];
			this.middleHands = new Hand[this.players.length];
			this.bottomHands = new Hand[this.players.length];
			for (int i = 0; i < this.players.length; i++) {
				this.topHands[i] = new Hand();
				this.middleHands[i] = new Hand();
				this.bottomHands[i] = new Hand();
			}
			
			this.printToSystemOut = printSystemOut;
			this.debugPrintToSystemOut = debugPrintToSystemOut;
			this.isFinished = false;
		}
		
		public Round(Round roundToClone, boolean printToSystemOut) {
			this(roundToClone, null, printToSystemOut, false, false);
		}
		
		public Round(Round roundToClone, Player[] players, boolean printToSystemOut, boolean debugPrintToSystemOut, boolean onlyIncludeVisibleState) {
			if (players == null) {
				this.players = new Player[roundToClone.players.length];
				for (int i = 0; i < roundToClone.players.length; i++) {
					this.players[i] = new Player(roundToClone.players[i]);
				}
			} else {
				this.players = players;
			}
			this.isInFantasyLand = new boolean[roundToClone.isInFantasyLand.length];
			System.arraycopy(roundToClone.isInFantasyLand, 0, this.isInFantasyLand, 0, roundToClone.isInFantasyLand.length);
			this.numPlayersInFantasyLand = roundToClone.numPlayersInFantasyLand;
			
			this.fullDeck = roundToClone.fullDeck; // Don't clone full deck, for memory optimization it is never modified and is read only
			
			this.numCardsDrawn = roundToClone.numCardsDrawn;
			this.numTurn = roundToClone.numTurn;
			
			if (onlyIncludeVisibleState) {
				this.shuffledCardOrder = Poker.getNextShuffedCardOrder(); // not visible, so create new ordering
				this.shuffledCardOrderIdx = 0;
			} else {
				this.shuffledCardOrder = new int[roundToClone.shuffledCardOrder.length];
				System.arraycopy(roundToClone.shuffledCardOrder, 0, this.shuffledCardOrder, 0, roundToClone.shuffledCardOrder.length);
				this.shuffledCardOrderIdx = roundToClone.shuffledCardOrderIdx;
			}
			
			this.visiblePlayedCardsInDeck = new boolean[roundToClone.visiblePlayedCardsInDeck.length];
			System.arraycopy(roundToClone.visiblePlayedCardsInDeck, 0, this.visiblePlayedCardsInDeck, 0, roundToClone.visiblePlayedCardsInDeck.length);
			
			this.topHands = new Hand[roundToClone.topHands.length];
			for (int i = 0; i < roundToClone.topHands.length; i++) {
				this.topHands[i] = new Hand(roundToClone.topHands[i]);
			}
			this.middleHands = new Hand[roundToClone.middleHands.length];
			for (int i = 0; i < roundToClone.middleHands.length; i++) {
				this.middleHands[i] = new Hand(roundToClone.middleHands[i]);
			}
			this.bottomHands = new Hand[roundToClone.bottomHands.length];
			for (int i = 0; i < roundToClone.bottomHands.length; i++) {
				this.bottomHands[i] = new Hand(roundToClone.bottomHands[i]);
			}
			
			this.printToSystemOut = printToSystemOut;
			this.debugPrintToSystemOut = debugPrintToSystemOut;
		}
		
		public int[][] getFullDeck() {
			return this.fullDeck;
		}
		
		public Player[] getPlayers() {
			return this.players;
		}

		public void startRound() {
			startRound(0);
		}
		
		private void startRound(int startingPlayerIdx) {
			playTurn(NUM_CARDS_DEALT_FIRST_TURN, startingPlayerIdx);
		}

		public void nextTurn() {
			nextTurn(0);
		}
		
		private void nextTurn(int startingPlayerIdx) {
			playTurn(NUM_CARDS_DEALT_PER_TURN, startingPlayerIdx);
		}
		
		public boolean isFinished() {
			return this.isFinished;
		}
		
		public boolean getPrintToSystemOut() {
			return this.printToSystemOut;
		}
		
		public boolean getDebugPrintToSystemOut() {
			return this.debugPrintToSystemOut;
		}
		
		public int getNumTurn() {
			return this.numTurn;
		}
		
		public Hand[] getTopHands() {
			return this.topHands;
		}
		
		public Hand[] getMiddleHands() {
			return this.middleHands;
		}
		
		public Hand[] getBottomHands() {
			return this.bottomHands;
		}
		
		public int getNumCardsDrawn() {
			return this.numCardsDrawn;
		}
		
		private void playTurn(int numCardsToDeal, int startingPlayerIdx) {
			if (this.printToSystemOut) {
				System.out.println("  numTurn: " + this.numTurn);
			}
			for (int playerIdx = startingPlayerIdx; playerIdx < this.players.length; playerIdx++) {
				if (this.numTurn > 0 && this.isInFantasyLand[playerIdx]) {
					continue;
				}
				
				Player player = this.players[playerIdx];
				int numCardsToDealForPlayer;
				if (player.isInFantasyLand()) {
					numCardsToDealForPlayer = FANTASY_LAND_NUM_CARDS_TO_DEAL;
				} else {
					numCardsToDealForPlayer = numCardsToDeal;
				}
				assert(this.shuffledCardOrderIdx + numCardsToDealForPlayer < this.shuffledCardOrder.length);
				
				assert(this.numCardsDrawn + numCardsToDealForPlayer < this.fullDeck.length);
				int[] cardIdxsToDeal = new int[numCardsToDealForPlayer];
				for (int i = 0; i < numCardsToDealForPlayer; i++) {
					while (this.visiblePlayedCardsInDeck[this.shuffledCardOrder[this.shuffledCardOrderIdx]]) {
						this.shuffledCardOrderIdx++;
					}
					cardIdxsToDeal[i] = this.shuffledCardOrder[this.shuffledCardOrderIdx];
					this.shuffledCardOrderIdx++;
				}
				this.numCardsDrawn += numCardsToDealForPlayer;
				
				int[] legalHandPositions = null;
				while (legalHandPositions == null) {
					if (this.printToSystemOut) {
						System.out.println("    player " + this.players[playerIdx].getId() + ":");
						if (this.players[playerIdx].getDecisionType() == DecisionType.SCANNER ||
								this.debugPrintToSystemOut) {
							System.out.println(toStringHands(playerIdx));
							StringBuilder dealtCardsSb = new StringBuilder();
							dealtCardsSb.append("[");
							for (int cardIdxToDeal : cardIdxsToDeal) {
								if (dealtCardsSb.length() > 1) {
									dealtCardsSb.append(", ");
								}
								int[] card = this.fullDeck[cardIdxToDeal];
								dealtCardsSb.append(Poker.toStringCard(card));
							}
							dealtCardsSb.append("]");
							System.out.println("      dealtCards: " + dealtCardsSb.toString());
						}
					}
					
					int[] handPositions = player.computeHandPositions(cardIdxsToDeal, playerIdx, this);
					if(isLegalHandPosition(handPositions, playerIdx, this, getPrintToSystemOut())) {
						legalHandPositions = handPositions;
					}
				}

				assert(legalHandPositions != null);
				assert(legalHandPositions.length == numCardsToDealForPlayer);
				playCardsForPlayer(legalHandPositions, cardIdxsToDeal, playerIdx);
				
				if (this.printToSystemOut &&
						this.players[playerIdx].getDecisionType() != DecisionType.SCANNER &&
						!this.debugPrintToSystemOut) {
					System.out.println(toStringHands(playerIdx));
				}
				if (player.isInFantasyLand) {
					assert(this.topHands[playerIdx].getNumCards() + this.middleHands[playerIdx].getNumCards() + this.bottomHands[playerIdx].getNumCards() == FANTASY_LAND_NUM_CARDS_TO_DEAL - FANTASY_LAND_NUM_CARDS_TO_DISCARD);
				} else {
					assert(this.topHands[playerIdx].getNumCards() + this.middleHands[playerIdx].getNumCards() + this.bottomHands[playerIdx].getNumCards() == NUM_CARDS_DEALT_FIRST_TURN + this.numTurn * (NUM_CARDS_DEALT_PER_TURN - NUM_CARDS_DISCARDED_PER_TURN));
				}
			}

			if (getNumCardsDrawn() + this.players.length * NUM_CARDS_DEALT_PER_TURN >= this.fullDeck.length) {
				this.isFinished = true;
			}

			assert((NUM_CARDS_DEALT_FIRST_TURN + this.numTurn * NUM_CARDS_DEALT_PER_TURN) * (this.players.length - this.numPlayersInFantasyLand) + FANTASY_LAND_NUM_CARDS_TO_DEAL * this.numPlayersInFantasyLand == this.numCardsDrawn);
			this.numTurn++;
		}
		
		public void endRound() {
			if (this.printToSystemOut) {
				System.out.println("  Round ended! Final hands: ");
				for (int playerIdx = 0; playerIdx < this.players.length; playerIdx++) {
					System.out.println("    player " + this.players[playerIdx].getId() + ":");
					System.out.println(toStringHands(playerIdx));
					boolean isFouled = isFouled(this.topHands[playerIdx], this.middleHands[playerIdx], this.bottomHands[playerIdx]);
					if (isFouled) {
						System.out.println("      Fouled!");
					}
					Player player = this.players[playerIdx];
					if (!isFouled) {
						if (player.isInFantasyLand()) {
							if (staysInFantasyLand(this.topHands[playerIdx], this.middleHands[playerIdx], this.bottomHands[playerIdx])) {
								if (this.printToSystemOut) {
									System.out.println("      Player " + player.getId() + " stays in fantasy land!");
								}
							} else {
								player.setIsInFantasyLand(false);
							}
						} else {
							if (goesToFantasyLand(this.topHands[playerIdx])) {
								if (this.printToSystemOut) {
									System.out.println("      Player " + player.getId() + " goes to fantasy land!");
								}
								player.setIsInFantasyLand(true);
							}
						}
					}
				}
			}
			
			boolean[] isFouled = new boolean[this.players.length];
			int[] scoreBonusTop = new int[this.players.length];
			int[] scoreBonusMiddle = new int[this.players.length];
			int[] scoreBonusBottom = new int[this.players.length];
			for (int i = 0; i < this.players.length; i++) {
				isFouled[i] = isFouled(this.topHands[i], this.middleHands[i], this.bottomHands[i]);
				if (isFouled[i]) {
					continue;
				}

				scoreBonusTop[i] = getBonusScore(this.topHands[i], TOP);
				scoreBonusMiddle[i] = getBonusScore(this.middleHands[i], MIDDLE);
				scoreBonusBottom[i] = getBonusScore(this.bottomHands[i], BOTTOM);
			}

			int[] scoreDeltas = new int[this.players.length];
			for (int i = 0; i < this.players.length; i++) {
				Player p1 = this.players[i];
				for (int j = i + 1; j < this.players.length; j++) {
					Player p2 = this.players[j];

					if (this.printToSystemOut) {
						System.out.println("    player " + p1.getId() + " vs player " + p2.getId() + ": ");
					}
					
					int scoreDelta;
					if (isFouled[i] && isFouled[j]) {
						if (this.printToSystemOut) {
							System.out.println("      Both players fouled!");
						}
						scoreDelta = 0;
					} else if (isFouled[i]) {
						if (this.printToSystemOut) {
							System.out.println("      Player " + p1.getId() + " fouled!");
						}
					} else if (isFouled[j]) {
						if (this.printToSystemOut) {
							System.out.println("      Player " + p2.getId() + " fouled!");
						}
					}
					
					int topHandComparison;
					if (isFouled[i] && isFouled[j]) {
						topHandComparison = 0;
					} else if (isFouled[i]) {
						topHandComparison = 1;
					} else if (isFouled[j]) {
						topHandComparison = -1;
					} else {
						topHandComparison = this.topHands[i].compareTo(this.topHands[j]);
					}
					int topHandScoreDelta = (int)Math.signum(-1 * topHandComparison) + scoreBonusTop[i] - scoreBonusTop[j];
					if (this.printToSystemOut) {
						System.out.print("      topHand: " + this.topHands[i] + " vs " + this.topHands[j] + " / ");
						System.out.print("bonusPoints: " + scoreBonusTop[i] + " vs " + scoreBonusTop[j] + " / ");
						if (topHandComparison > 0) {
							System.out.println("Player " + p2.getId() + " wins!" + (isFouled[i] ? " (player " + p1.getId() + " fouled)" : "") + " / +" + (-1 * topHandScoreDelta) + " points to player " + p2.getId());
						} else if (topHandComparison < 0) {
							System.out.println("Player " + p1.getId() + " wins!" + (isFouled[j] ? " (player " + p2.getId() + " fouled)" : "") + " / +" + topHandScoreDelta + " points to player " + p1.getId());
						} else {
							System.out.println("tie!" + (isFouled[i] && isFouled[j] ? " (both players fouled)": ""));
						}
					}

					int middleHandComparison;
					if (isFouled[i] && isFouled[j]) {
						middleHandComparison = 0;
					} else if (isFouled[i]) {
						middleHandComparison = 1;
					} else if (isFouled[j]) {
						middleHandComparison = -1;
					} else {
						middleHandComparison = this.middleHands[i].compareTo(this.middleHands[j]);
					}
					int middleHandScoreDelta = (int)Math.signum(-1 * middleHandComparison) + scoreBonusMiddle[i] - scoreBonusMiddle[j];
					if (this.printToSystemOut) {
						System.out.print("      middleHand: " + this.middleHands[i] + " vs " + this.middleHands[j] + " / ");
						System.out.print("bonusPoints: " + scoreBonusMiddle[i] + " vs " + scoreBonusMiddle[j] + " / ");
						if (middleHandComparison > 0) {
							System.out.println("Player " + p2.getId() + " wins!" + (isFouled[i] ? " (player " + p1.getId() + " fouled)" : "") + " / +" + (-1 * middleHandScoreDelta) + " points to player " + p2.getId());
						} else if (middleHandComparison < 0) {
							System.out.println("Player " + p1.getId() + " wins!" + (isFouled[j] ? " (player " + p2.getId() + " fouled)" : "") + " / +" + middleHandScoreDelta + " points to player " + p1.getId());
						} else {
							System.out.println("tie!" + (isFouled[i] && isFouled[j] ? " (both players fouled)": ""));
						}
					}

					int bottomHandComparison;
					if (isFouled[i] && isFouled[j]) {
						bottomHandComparison = 0;
					} else if (isFouled[i]) {
						bottomHandComparison = 1;
					} else if (isFouled[j]) {
						bottomHandComparison = -1;
					} else {
						bottomHandComparison = this.bottomHands[i].compareTo(this.bottomHands[j]);
					}
					int bottomHandScoreDelta = (int)Math.signum(-1 * bottomHandComparison) + scoreBonusBottom[i] - scoreBonusBottom[j];
					if (this.printToSystemOut) {
						System.out.print("      bottomHand: " + this.bottomHands[i] + " vs " + this.bottomHands[j] + " / ");
						System.out.print("bonusPoints: " + scoreBonusBottom[i] + " vs " + scoreBonusBottom[j] + " / ");
						if (bottomHandComparison > 0) {
							System.out.println("Player " + p2.getId() + " wins!" + (isFouled[i] ? " (player " + p1.getId() + " fouled)" : "") + " / +" + (-1 * bottomHandScoreDelta) + " points to player " + p2.getId());
						} else if (bottomHandComparison < 0) {
							System.out.println("Player " + p1.getId() + " wins!" + (isFouled[j] ? " (player " + p2.getId() + " fouled)" : "") + " / +" + bottomHandScoreDelta + " points to player " + p1.getId());
						} else {
							System.out.println("tie!" + (isFouled[i] && isFouled[j] ? " (both players fouled)": ""));
						}
					}

					scoreDelta = topHandScoreDelta + middleHandScoreDelta + bottomHandScoreDelta;

					// Scoop bonus
					if (topHandComparison < 0 && middleHandComparison < 0 && bottomHandComparison < 0) {
						scoreDelta += 3;
						if (this.printToSystemOut) {
							System.out.println("      Scoop bonus! +3 points to player " + p1.getId());
						}
					} else if (topHandComparison > 0 && middleHandComparison > 0 && bottomHandComparison > 0) {
						scoreDelta -= 3;
						if (this.printToSystemOut) {
							System.out.println("      Scoop bonus! +3 points to player " + p2.getId());
						}
					}

					if (this.printToSystemOut) {
						System.out.println("      Head-to-head score delta: " + scoreDelta + " points to player " + p1.getId() + ", and " + (-1 * scoreDelta) + " points to player " + p2.getId());
					}
					scoreDeltas[i] += scoreDelta;
					scoreDeltas[j] -= scoreDelta;
				}
			}

			if (this.printToSystemOut) {
				System.out.println("  Total score deltas:");
			}
			for (int i = 0; i < this.players.length; i++) {
				Player player = this.players[i];
				player.addScore(scoreDeltas[i]);
				if (this.printToSystemOut) {
					System.out.println("    " + scoreDeltas[i] + " points to player " + player.getId() + " (total points: " + player.getScore() + ")");
				}
			}
		}

		private void playCardsForPlayer(int[] handPositions, int[] cardIdxsToDeal, int playerIdx) {
			assert(cardIdxsToDeal.length == handPositions.length);
			for (int i = 0; i < handPositions.length; i++) {
				int[] card = this.fullDeck[cardIdxsToDeal[i]];
				int handPosition = handPositions[i];
				if (handPosition == DISCARD) {
					// discard card, so skip
				} else if (handPosition == TOP) {
					this.topHands[playerIdx].addCard(card);
					this.visiblePlayedCardsInDeck[Poker.getSortedOrderInDeck(card)] = true;
				} else if (handPosition == MIDDLE) {
					this.middleHands[playerIdx].addCard(card);
					this.visiblePlayedCardsInDeck[Poker.getSortedOrderInDeck(card)] = true;
				} else if (handPosition == BOTTOM) {
					this.bottomHands[playerIdx].addCard(card);
					this.visiblePlayedCardsInDeck[Poker.getSortedOrderInDeck(card)] = true;
				} else {
					LOGGER.log(Level.SEVERE, "Unkown handPosition=" + handPosition + ". Skipping playing current card with index " + i + ".");
				}
			}
			
		}
		
		public String toStringHands(int playerIdx) {
			return "      topHand: " + this.topHands[playerIdx].toString() +
					"\n      middleHand: " + this.middleHands[playerIdx].toString() +
					"\n      bottomHand: " + this.bottomHands[playerIdx].toString();
		}
	}
	

	/********************************************************************************************
	 * IV. Game
	 ********************************************************************************************/
	/**
	 * Holds the player data, as well as the button index.
	 * 
	 * @author Kevin Truong
	 *
	 */
	public static class Game {
		private final Player[] players;
		private int buttonIdx;
		
		private boolean printToSystemOut;
		private boolean debugPrintToSystemOut;
		
		private int numRounds;
		
		public Game(Player[] players, boolean printToSystemOut, boolean debugPrintToSystemOut) {
			this(players, players.length - 1, printToSystemOut, debugPrintToSystemOut);
		}
		
		public Game(Player[] players, int buttonIdx, boolean printToSystemOut, boolean debugPrintToSystemOut) {
			this.players = players;
			this.buttonIdx = buttonIdx;
			this.printToSystemOut = printToSystemOut;
			this.debugPrintToSystemOut = debugPrintToSystemOut;
			this.numRounds = 0;
		}
		
		public int getNumRounds() {
			return this.numRounds;
		}
		
		public void startNewRound() {
			if (this.printToSystemOut) {
				System.out.println("Round " + this.numRounds + ":");
			}
			Player[] orderedPlayers = new Player[this.players.length]; // reordered due to who has the button
			int numOrderedPlayers = 0;
			for (int i = buttonIdx + 1; i < this.players.length; i++) {
				orderedPlayers[numOrderedPlayers] = this.players[i];
				numOrderedPlayers++;
			}
			for (int i = 0; i < buttonIdx + 1; i++) {
				orderedPlayers[numOrderedPlayers] = this.players[i];
				numOrderedPlayers++;
			}
			assert(numOrderedPlayers == orderedPlayers.length);
			
			Round round = new Round(orderedPlayers, Poker.FULL_DECK, 0, 0, Poker.getNextShuffedCardOrder(), new boolean[Poker.FULL_DECK.length], this.printToSystemOut, this.debugPrintToSystemOut);
			round.startRound();
			while (!round.isFinished()) {
				round.nextTurn();
			}
			round.endRound();
			this.buttonIdx++;
			if (this.buttonIdx >= this.players.length) {
				this.buttonIdx = 0;
			}
			this.numRounds++;
		}
	}

	/********************************************************************************************
	 * V. Example Use
	 ********************************************************************************************/
	/**
	 * Here are four example game set ups. Simply uncomment out the set up
	 * you want to play, and run this class.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		 // SET UP: user input versus a heuristic monte carlo simulator vs a random player
		{
			Player[] players = new Player[]{new Player("user", DecisionType.SCANNER), new Player("monte carlo", DecisionType.HEURISTIC_MONTE_CARLO_10000), new Player("random", DecisionType.RANDOM)};
			Scanner scanner = new Scanner(System.in);
			players[0].setScanner(scanner);

			Game game = new Game(players, true, false);
			int numRounds = 10;
			for (int i = 0; i < numRounds; i++) {
				game.startNewRound();
			}
		}

		// SET UP: three players (three user input players)
//		{
//			Player[] players = new Player[]{new Player("1", DecisionType.SCANNER), new Player("2", DecisionType.SCANNER), new Player("3", DecisionType.SCANNER)};
//			Scanner scanner = new Scanner(System.in);
//			players[0].setScanner(scanner);
//			players[1].setScanner(scanner);
//			players[2].setScanner(scanner);
//
//			Game game = new Game(players, true, false);
//			int numRounds = 10;
//			for (int i = 0; i < numRounds; i++) {
//				game.startNewRound();
//			}
//		}

		// SET UP: a heuristic monte carlo player vs a monte carlo player vs a random player
//		{
//			Player[] players = new Player[]{new Player("heuristic monte carlo", DecisionType.HEURISTIC_MONTE_CARLO_10000), new Player("monte carlo", DecisionType.MONTE_CARLO_10000), new Player("random", DecisionType.RANDOM)};
//			Game game = new Game(players, true, false);
//			int numRounds = 10;
//			for (int i = 0; i < numRounds; i++) {
//				game.startNewRound();
//			}
//		}

		// SET UP: three random players
//		{
//			Player[] players = new Player[]{new Player("1", DecisionType.RANDOM), new Player("2", DecisionType.RANDOM), new Player("3", DecisionType.RANDOM)};
//			Game game = new Game(players, true, false);
//			int numRounds = 10;
//			for (int i = 0; i < numRounds; i++) {
//				game.startNewRound();
//			}
//		}
	}
}
