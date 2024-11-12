package src.pas.chess.heuristics;


// SYSTEM IMPORTS
import edu.cwru.sepia.util.Direction;
import edu.bu.chess.game.move.PromotePawnMove;
import edu.bu.chess.game.piece.Piece;
import edu.bu.chess.game.piece.PieceType;
import edu.bu.chess.game.player.Player;
import edu.bu.chess.search.DFSTreeNode;
import edu.bu.chess.utils.Coordinate;
import edu.bu.chess.game.move.CaptureMove;
import edu.bu.chess.game.move.Move;

// JAVA PROJECT IMPORTS


public class CustomHeuristics
    extends Object
{

	/**
	 * Get the max player from a node
	 * @param node
	 * @return
	 */
	public static Player getMaxPlayer(DFSTreeNode node)
	{
		return node.getMaxPlayer();
	}

	/**
	 * Get the min player from a node
	 * @param node
	 * @return
	 */
	public static Player getMinPlayer(DFSTreeNode node)
	{
		return CustomHeuristics.getMaxPlayer(node).equals(node.getGame().getCurrentPlayer()) ? node.getGame().getOtherPlayer() : node.getGame().getCurrentPlayer();
	}


	public static class OffensiveHeuristics extends Object
	{

		public static int getNumberOfPiecesMaxPlayerIsThreatening(DFSTreeNode node)
		{

			int numPiecesMaxPlayerIsThreatening = 0;
			for(Piece piece : node.getGame().getBoard().getPieces(CustomHeuristics.getMaxPlayer(node)))
			{
				numPiecesMaxPlayerIsThreatening += piece.getAllCaptureMoves(node.getGame()).size();
			}
			return numPiecesMaxPlayerIsThreatening;
		}
		public static int getNumberofPointsMaxPlayerIsThreatening(DFSTreeNode node)
		{
			int pointsEarnedInCaptureMoves = 0;
			for(Piece piece : node.getGame().getBoard().getPieces(CustomHeuristics.getMaxPlayer(node)))
			{
				for(Move move : piece.getAllCaptureMoves(node.getGame()))
				{	CaptureMove captureMove = (CaptureMove)move;
					pointsEarnedInCaptureMoves += Piece.getPointValue(piece.getType());
				}
			}
			return pointsEarnedInCaptureMoves;
		}
		public static int getNumberofCenterSquaresMaxPlayerControls(DFSTreeNode node)
		{
			int numCenterSquaresControlled = 0;
			for(Coordinate centerSquare : new Coordinate[] {new Coordinate(5, 5), new Coordinate(5, 4), new Coordinate(4, 5), new Coordinate(4, 4)})
			{
				if(node.getGame().getBoard().isPositionOccupied(centerSquare) && (node.getGame().getBoard().getPieceAtPosition(centerSquare).getPlayer() == CustomHeuristics.getMaxPlayer(node)))
				{
					numCenterSquaresControlled++;
				}
			}
			return numCenterSquaresControlled;
		}
	}

	public static class DefensiveHeuristics extends Object
	{

		public static int getNumberOfMaxPlayersAlivePieces(DFSTreeNode node)
		{
			int numMaxPlayersPiecesAlive = 0;
			for(PieceType pieceType : PieceType.values())
			{
				numMaxPlayersPiecesAlive += node.getGame().getNumberOfAlivePieces(CustomHeuristics.getMaxPlayer(node), pieceType);
			}
			return numMaxPlayersPiecesAlive;
		}

		public static int getNumberOfMinPlayersAlivePieces(DFSTreeNode node)
		{
			int numMaxPlayersPiecesAlive = 0;
			for(PieceType pieceType : PieceType.values())
			{
				numMaxPlayersPiecesAlive += node.getGame().getNumberOfAlivePieces(CustomHeuristics.getMinPlayer(node), pieceType);
			}
			return numMaxPlayersPiecesAlive;
		}

		public static int getClampedPieceValueTotalSurroundingMaxPlayersKing(DFSTreeNode node)
		{
			// what is the state of the pieces next to the king? add up the values of the neighboring pieces
			// positive value for friendly pieces and negative value for enemy pieces (will clamp at 0)
			int maxPlayerKingSurroundingPiecesValueTotal = 0;

			Piece kingPiece = node.getGame().getBoard().getPieces(CustomHeuristics.getMaxPlayer(node), PieceType.KING).iterator().next();
			Coordinate kingPosition = node.getGame().getCurrentPosition(kingPiece);
			for(Direction direction : Direction.values())
			{
				Coordinate neightborPosition = kingPosition.getNeighbor(direction);
				if(node.getGame().getBoard().isInbounds(neightborPosition) && node.getGame().getBoard().isPositionOccupied(neightborPosition))
				{
					Piece piece = node.getGame().getBoard().getPieceAtPosition(neightborPosition);
					int pieceValue = Piece.getPointValue(piece.getType());
					if(piece != null && kingPiece.isEnemyPiece(piece))
					{
						maxPlayerKingSurroundingPiecesValueTotal -= pieceValue;
					} else if(piece != null && !kingPiece.isEnemyPiece(piece))
					{
						maxPlayerKingSurroundingPiecesValueTotal += pieceValue;
					}
				}
			}
			// kingSurroundingPiecesValueTotal cannot be < 0 b/c the utility of losing a game is 0, so all of our utility values should be at least 0
			maxPlayerKingSurroundingPiecesValueTotal = Math.max(maxPlayerKingSurroundingPiecesValueTotal, 0);
			return maxPlayerKingSurroundingPiecesValueTotal;
		}

		public static int getNumberOfPiecesThreateningMaxPlayer(DFSTreeNode node)
		{
			// how many pieces are threatening us?
			int numPiecesThreateningMaxPlayer = 0;
			for(Piece piece : node.getGame().getBoard().getPieces(CustomHeuristics.getMinPlayer(node)))
			{
				numPiecesThreateningMaxPlayer += piece.getAllCaptureMoves(node.getGame()).size();
			}
			return numPiecesThreateningMaxPlayer;
		}
		public static int getPointsMinPlayerisThreatening(DFSTreeNode node)
		{
			int pointsEarnedInCaptureMoves = 0;
			for(Piece piece : node.getGame().getBoard().getPieces(CustomHeuristics.getMinPlayer(node)))
			{
				for(Move move : piece.getAllCaptureMoves(node.getGame()))
				{	CaptureMove captureMove = (CaptureMove)move;
					pointsEarnedInCaptureMoves += Piece.getPointValue(piece.getType());
				}
			}
			return pointsEarnedInCaptureMoves;
		}
	}

	public static double getOffensiveMaxPlayerHeuristicValue(DFSTreeNode node)
	{
		// remember the action has already taken affect at this point, so capture moves have already resolved
		// and the targeted piece will not exist inside the game anymore.
		// however this value was recorded in the amount of points that the player has earned in this node
		double damageDealtInThisNode = node.getGame().getBoard().getPointsEarned(CustomHeuristics.getMaxPlayer(node));

		switch(node.getMove().getType())
		{
		case PROMOTEPAWNMOVE:
			PromotePawnMove promoteMove = (PromotePawnMove)node.getMove();
			damageDealtInThisNode += Piece.getPointValue(promoteMove.getPromotedPieceType());
			break;
		default:
			break;
		}
		// offense can typically include the number of pieces that our pieces are currently threatening
		int numPiecesWeAreThreatening = OffensiveHeuristics.getNumberOfPiecesMaxPlayerIsThreatening(node);
		int numPointsWeAreThreatening = OffensiveHeuristics.getNumberofPointsMaxPlayerIsThreatening(node);
		int numCenterSquaresControlled = OffensiveHeuristics.getNumberofCenterSquaresMaxPlayerControls(node);
		return damageDealtInThisNode + numPiecesWeAreThreatening + numPointsWeAreThreatening + numCenterSquaresControlled;
	}

	public static double getDefensiveMaxPlayerHeuristicValue(DFSTreeNode node)
	{
		// how many pieces exist on our team?
		int numPiecesAlive = DefensiveHeuristics.getNumberOfMaxPlayersAlivePieces(node);

		// what is the state of the pieces next to the king? add up the values of the neighboring pieces
		// positive value for friendly pieces and negative value for enemy pieces (will clamp at 0)
		int kingSurroundingPiecesValueTotal = DefensiveHeuristics.getClampedPieceValueTotalSurroundingMaxPlayersKing(node);

		// how many pieces are threatening us?
		int numPiecesThreateningUs = DefensiveHeuristics.getNumberOfPiecesThreateningMaxPlayer(node);

		// Reward less pieces threatening us by subtracting the number of pieces threatening us
		return numPiecesAlive + kingSurroundingPiecesValueTotal - numPiecesThreateningUs - DefensiveHeuristics.getPointsMinPlayerisThreatening(node);
	}

	public static double getNonlinearPieceCombinationMaxPlayerHeuristicValue(DFSTreeNode node)
	{
		// both bishops are worth more together than a single bishop alone
		// same with knights...we want to encourage keeping pairs of elements
		double multiPieceValueTotal = 0.0;

		double exponent = 1.5; // f(numberOfKnights) = (numberOfKnights)^exponent

		// go over all the piece types that have more than one copy in the game (including pawn promotion)
		for(PieceType pieceType : new PieceType[] {PieceType.BISHOP, PieceType.KNIGHT, PieceType.ROOK, PieceType.QUEEN})
		{
			multiPieceValueTotal += Math.pow(node.getGame().getNumberOfAlivePieces(CustomHeuristics.getMaxPlayer(node), pieceType), exponent);
		}

		return multiPieceValueTotal;
	}

	public static double getMaxPlayerHeuristicValue(DFSTreeNode node)
	{
		double offenseHeuristicValue = CustomHeuristics.getOffensiveMaxPlayerHeuristicValue(node);
		double defenseHeuristicValue = CustomHeuristics.getDefensiveMaxPlayerHeuristicValue(node);
		double nonlinearHeuristicValue = CustomHeuristics.getNonlinearPieceCombinationMaxPlayerHeuristicValue(node);

		return offenseHeuristicValue + defenseHeuristicValue + nonlinearHeuristicValue;
	}

}
