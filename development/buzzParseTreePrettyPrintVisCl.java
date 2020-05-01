import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.ArrayDeque;

import java.io.File;
import java.io.IOException;

/**
  Copyright (C) Jan 2019, Oct 2019 Ettore Merlo - All rights reserved
  Copyright (C) 2020 Alexandre Rault - All rights reserved
*/

public class buzzParseTreePrettyPrintVisCl implements defsInt {

    //int cfgNodeSeed = 0;
    //Set<Integer> visited = null;
    //Map<Integer, ArrayList<Integer>> adjTable = null;
    buzzParseTableCl astTable = null;

    private int curIndent = 0;
    
    public void initParams(buzzParseTableCl parAstTable) {

	//visited = parVisited;
	//adjTable = parAdjTable;
	astTable = parAstTable;

	return;
    }

    private String getIndentStr() {
	StringBuffer indentStr = new StringBuffer();
	for (int i = 0; i < curIndent; ++i) {
	    indentStr.append("   ");
	}
	return indentStr.toString();
    }

    private void visitChildren(Integer astNodeId, int depth) {

	List<Integer> children = astTable.succTable.get(astNodeId);
	if (children != null) {
	    for (Integer childId: children) {
		visit(childId, depth);
	    }
	}

	return;
    }
	
	private void visit_GENERIC(Integer astNodeId, int depth) {

		String type = astTable.getType(astNodeId);

		System.out.println(getIndentStr() +
							   type +
							   " (" +
							   astNodeId +
							   " " +
							   depth+
							   ")");
	
		curIndent++;
		visitChildren(astNodeId, (depth + 1));
		curIndent--;
	
		return;
	}

    //
    // special nodes
    //

    private void visit_token(Integer astNodeId, int depth) {

		Integer curTkType = astTable.getTkType(astNodeId);
		if (curTkType == null) {
			System.err.println("ERROR: missing tk type for node " +
					astNodeId);
			System.exit(1);
		}

		System.out.print(getIndentStr() +
				"token" +
				" (" +
				astNodeId +
				" " +
				depth+
				") " +
				curTkType);

		String curLiteral = astTable.getTkLiteral(astNodeId);
		if (curLiteral != null) {
			if (!curLiteral.equals("UNDEFINED")) {
			System.out.print(" <" +
					curLiteral +
					">");
			}
		}

		String curImg = astTable.getTkImage(astNodeId);
		if (curImg != null) {
			if (!curImg.equals("UNDEFINED")) {
			System.out.print(" <" +
					curImg +
					">");
			}
		}
		System.out.println();

		curIndent++;
		visitChildren(astNodeId, (depth + 1));
		curIndent--;

		return;
    }


    public Integer visit(Integer astNodeId, int depth) {

		Integer retVal = null;

		//System.out.println(astTable);
		//System.out.println(astNodeId);
		//System.out.println(astTable.getType(astNodeId));
		

		String curType = astTable.getType(astNodeId);
		if (curType == null) {
			System.err.println("ERROR: missing node " +
					astNodeId);
			System.exit(1);
		}

		switch (curType) {

			case "ACTUAL_PARAMETER_LIST":
			case "BINARY_ARITHM_OP":
			case "BINARY_LOGIC_OP":
			case "BITSHIFT":
			case "BITWISEANDOR":
			case "BITWISENOT":
			case "BLOCK":
			case "BLOCKSTAT":
			case "CALL":	
			case "COMMAND":
			case "COMPARISON":
			case "CONDITION":
			case "CONDITION_LIST":
			case "EXPRESSION":
			case "FORMAL_PARAMETER_LIST":
			case "FUNCTION":
			case "IDLIST":
			case "IDREF":
			case "IF":
			case "LAMBDA":
			case "LIST":
			case "LIST_STATEMENT":
			case "MODULO":
			case "OPERAND":
			case "PARSE":
			case "POWER":
			case "POWERREST":
			case "PRODUCT":
			case "REL_OP":
			case "RETURN":
			case "SCRIPT":
			case "STAT":
			case "STATLIST":
			case "UNARY_ARITHM_OP":
			case "UNARY_LOGIC_OP":
			case "VAR":
			case "WHILE":
				visit_GENERIC(astNodeId, depth);
				break;
			
			case "token":
				visit_token(astNodeId, depth);
				break;
			
			default:
				System.err.println("ERROR: invalid type " +
						curType +
						" for node " +
						astNodeId);
				System.exit(1);
		}

		return(retVal);
    }
}
