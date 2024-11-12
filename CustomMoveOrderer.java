package src.pas.chess.moveorder;


// SYSTEM IMPORTS
import edu.bu.chess.search.DFSTreeNode;
import edu.bu.chess.game.move.Move;
import edu.bu.chess.game.move.CaptureMove;
import edu.bu.chess.game.move.MoveType;
import edu.bu.chess.game.piece.Piece;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

// JAVA PROJECT IMPORTS


public class CustomMoveOrderer
    extends Object
{

	/**
	 * By default, I claim that we want to see attacking moves before anything else. However,
	 * this is not a good rule in general, and we may want to make it move-specific OR start incorporating some custom heuristics
	 * @param nodes. The nodes to order (these are children of a DFSTreeNode) that we are about to consider in the search.
	 * @return The ordered nodes.
	 */
	public static List<DFSTreeNode> order(List<DFSTreeNode> nodes)
	{
		// by default get the CaptureMoves first
		List<DFSTreeNode> captureNodes = new LinkedList<DFSTreeNode>();
		List<DFSTreeNode> otherNodes = new LinkedList<DFSTreeNode>();
		List<DFSTreeNode> enPassantNodes = new LinkedList<DFSTreeNode>();
		List<DFSTreeNode> promotePawnNodes = new LinkedList<DFSTreeNode>();
		Map<DFSTreeNode, Integer> nodeToValue = new HashMap<>();
		for(DFSTreeNode node : nodes)
		{
			if(node.getMove() != null)
			{
				switch(node.getMove().getType())
				{ 
				case PROMOTEPAWNMOVE:
					promotePawnNodes.add(node);
					break;
				case ENPASSANTMOVE:
					enPassantNodes.add(node);
					break;
				case CAPTUREMOVE:
					captureNodes.add(node);
					// sort by the MVV/LVA heuristic
					if (node.getMove() instanceof CaptureMove) {
						Move move = node.getMove();

                        CaptureMove captureMove = (CaptureMove) node.getMove();
                        int mvvlva = Piece.getPointValue(node.getGame().getBoard().getPiece(node.getGame().getOtherPlayer(), captureMove.getTargetPieceID()).getType()) - Piece.getPointValue(node.getGame().getBoard().getPiece(node.getGame().getCurrentPlayer(), captureMove.getAttackingPieceID()).getType());
                        nodeToValue.put(node, mvvlva);
                    }
					break;
				default:
					otherNodes.add(node);
					break;
				}
			} else
			{
				otherNodes.add(node);
			}
		}
		// Sort captureNodes based on the values in nodeToValue
        List<Map.Entry<DFSTreeNode, Integer>> sortedEntries = new ArrayList<>(nodeToValue.entrySet());
        sortedEntries.sort(Map.Entry.comparingByValue());

		// Extract sorted nodes
        List<DFSTreeNode> sortedCaptureNodes = new ArrayList<>();
        for (Map.Entry<DFSTreeNode, Integer> entry : sortedEntries) {
            sortedCaptureNodes.add(entry.getKey());
        }
		sortedCaptureNodes.addAll(enPassantNodes);
		sortedCaptureNodes.addAll(promotePawnNodes);
		sortedCaptureNodes.addAll(otherNodes);
		return captureNodes;
	}

}
