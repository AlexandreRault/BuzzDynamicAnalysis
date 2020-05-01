import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ArrayDeque;
import java.util.Collections;

import java.io.File;
import java.io.IOException;

/**
  Copyright (C) Jan 2019, Oct 2019 Ettore Merlo - All rights reserved
*/

public class buzzParseTreeTraceInjectionVisCl implements defsInt {

	buzzParseTableCl astTable = null;

	private Integer nodeIdMax = 0;
	private int temp_id = 0;
    
    public void initParams(buzzParseTableCl parAstTable) {

		astTable = parAstTable;
		
		Set<Integer> keySet = astTable.nodeTypeTable.keySet();

		nodeIdMax = Collections.max(keySet);


	    return;
	}

	//
	// AST insertion methods
	//

	private String createUniqueVarName() {
		return "temp_inject_var_" + temp_id++;
	}

	private Integer createNode(String type, Integer predecessorId, int succesorPosition) {
		
		Integer newNodeId = ++nodeIdMax;
		if (nodeIdMax <= 0) {
			System.err.println("new node id overflowed");
			System.exit(1);
		}

		astTable.nodeTypeTable.put(newNodeId, type);
		astTable.predTable.put(newNodeId, predecessorId);
		List<Integer> siblings = astTable.succTable.get(predecessorId);
		if (siblings == null) {
			siblings = new ArrayList<Integer>();
		}
		siblings.add(succesorPosition, newNodeId);
		astTable.succTable.put(predecessorId, siblings);
		
		return newNodeId;
	}

	private void addBlockToBlockstat(Integer blockstatNodeId) {
		// from:
		// BLOCKSTAT
		//   STAT
		//     ...
		// to:
		// BLOCKSTAT
		//   BLOCK
		//     token <{>
		//     STATLIST
		//       STAT
		//         ..
		//     token <}>

		// TODO: verify empty STAT at the beginning and end of STATLIST usefulness?

		verifyNodeType("BLOCKSTAT", blockstatNodeId);

		List<Integer> children = astTable.succTable.get(blockstatNodeId);

		if (children == null) {
			System.err.println("Expected succesors at node "
								+ blockstatNodeId + ", but found none.");
			System.exit(1);
		}

		if (children.size() != 1) {
			System.err.println("Expected exactly 1 successor at node "
								+ blockstatNodeId + ", but found " + children.size() + ".");
			System.exit(1);
		}

		Integer statNodeId = children.get(0);

		verifyNodeType("STAT", statNodeId);

		// empty BLOCKSTAT successors
		astTable.succTable.put(blockstatNodeId, new ArrayList<Integer>());
		// create BLOCK node
		Integer newBlockNodeId = createNode("BLOCK", blockstatNodeId, 0);
		// create token node
		Integer newTokenNodeId = createNode("token", newBlockNodeId, 0);
		astTable.tkImageTable.put(newTokenNodeId, "{");
		astTable.lineBeginTable.put(newTokenNodeId, -1);
		astTable.columnBeginTable.put(newTokenNodeId, -1);
		// create STATLIST node
		Integer newStatlistNodeId = createNode("STATLIST", newBlockNodeId, 1);
		// create token node
		Integer newTokenNodeId2 = createNode("token", newBlockNodeId, 2);
		astTable.tkImageTable.put(newTokenNodeId2, "}");
		astTable.lineBeginTable.put(newTokenNodeId2, -1);
		astTable.columnBeginTable.put(newTokenNodeId2, -1);
		// link STATE
		List<Integer> statlistChildren = new ArrayList<Integer>();
		statlistChildren.add(statNodeId);
		astTable.succTable.put(newStatlistNodeId, statlistChildren);
		astTable.predTable.put(statNodeId, newStatlistNodeId);

		return;
	}

	private void injectStatLogCall(Integer statlistNodeId, int index, CallInfo callInfo) {
		// STAT
		//   COMMAND
		//     IDREF
		//       token <log> <identifier>
		//       CALL
		//       ACTUAL_PARAMETER_LIST
		//         CONDITION
		//           COMPARISON
		//             EXPRESSION
		//               PRODUCT
		//                 MODULO
		//                   POWER
		//                     BITSHIFT
		//                       BITWISEANDOR
		//                         BITWISENOT
		//                           OPERAND
		//                             token <"probe call callName (l, c)"> 
		//                     POWERREST
		//       token <)>

		// create STAT node
		Integer newStatNodeId = createNode("STAT", statlistNodeId, index);
		// create COMMAND node
		Integer newCommandNodeId = createNode("COMMAND", newStatNodeId, 0);
		// create IDREF node
		Integer newIdrefNodeId = createNode("IDREF", newCommandNodeId, 0);
		// create token node
		Integer newTokenNodeId = createNode("token", newIdrefNodeId, 0);
		astTable.tkImageTable.put(newTokenNodeId, "identifier");
		astTable.tkLiteralTable.put(newTokenNodeId, "log");
		astTable.lineBeginTable.put(newTokenNodeId, -1);
		astTable.columnBeginTable.put(newTokenNodeId, -1);
		// create CALL node
		Integer newCallNodeId = createNode("CALL", newIdrefNodeId, 1);
		// create ACTUAL_PARAMETER_LIST node
		Integer newAPLNodeId = createNode("ACTUAL_PARAMETER_LIST", newIdrefNodeId, 2);
		// create token node
		Integer newTokenNodeId3 = createNode("token", newIdrefNodeId, 3);
		astTable.tkImageTable.put(newTokenNodeId3, ")");
		astTable.lineBeginTable.put(newTokenNodeId3, -1);
		astTable.columnBeginTable.put(newTokenNodeId3, -1);
		// create CONDITION node
		Integer newConditionNodeId = createNode("CONDITION", newAPLNodeId, 0);
		// create COMPARISON node
		Integer newComparisonNodeId = createNode("COMPARISON", newConditionNodeId, 0);
		// create EXPRESSION node
		Integer newExpressionNodeId = createNode("EXPRESSION", newComparisonNodeId, 0);
		// create PRODUCT node
		Integer newProductNodeId = createNode("PRODUCT", newExpressionNodeId, 0);
		// create MODULO node
		Integer newModuloNodeId = createNode("MODULO", newProductNodeId, 0);
		// create POWER node
		Integer newPowerNodeId = createNode("POWER", newModuloNodeId, 0);
		// create BITSHIFT node
		Integer newBitshiftNodeId = createNode("BITSHIFT", newPowerNodeId, 0);
		// create POWERREST node
		Integer newPowerrestNodeId = createNode("POWERREST", newPowerNodeId, 1);
		// create BITWISEANDOR node
		Integer newBitandorNodeId = createNode("BITWISEANDOR", newBitshiftNodeId, 0);
		// create BITWISENOT node
		Integer newBitnotNodeId = createNode("BITWISENOT", newBitandorNodeId, 0);
		// create OPERAND node
		Integer newOperandNodeId = createNode("OPERAND", newBitnotNodeId, 0);
		// create token node
		Integer newTokenNodeId2 = createNode("token", newOperandNodeId, 0);
		astTable.tkLiteralTable.put(newTokenNodeId2, 
									"\"probe call function " + callInfo.name + " (" + callInfo.line + ", " + callInfo.column + ")\"");
		astTable.lineBeginTable.put(newTokenNodeId2, -1);
		astTable.columnBeginTable.put(newTokenNodeId2, -1);
	}

	private void injectStatLogBeginFunction(Integer statlistNodeId, int index, FunctionInfo functionInfo) {
		// STAT
		//   COMMAND
		//     IDREF
		//       token <log> <identifier>
		//       CALL
		//       ACTUAL_PARAMETER_LIST
		//         CONDITION
		//           COMPARISON
		//             EXPRESSION
		//               PRODUCT
		//                 MODULO
		//                   POWER
		//                     BITSHIFT
		//                       BITWISEANDOR
		//                         BITWISENOT
		//                           OPERAND
		//                             token <"probe begin functionName"> 
		//                     POWERREST
		//       token <)>

		// create STAT node
		Integer newStatNodeId = createNode("STAT", statlistNodeId, index);
		// create COMMAND node
		Integer newCommandNodeId = createNode("COMMAND", newStatNodeId, 0);
		// create IDREF node
		Integer newIdrefNodeId = createNode("IDREF", newCommandNodeId, 0);
		// create token node
		Integer newTokenNodeId = createNode("token", newIdrefNodeId, 0);
		astTable.tkImageTable.put(newTokenNodeId, "identifier");
		astTable.tkLiteralTable.put(newTokenNodeId, "log");
		astTable.lineBeginTable.put(newTokenNodeId, -1);
		astTable.columnBeginTable.put(newTokenNodeId, -1);
		// create CALL node
		Integer newCallNodeId = createNode("CALL", newIdrefNodeId, 1);
		// create ACTUAL_PARAMETER_LIST node
		Integer newAPLNodeId = createNode("ACTUAL_PARAMETER_LIST", newIdrefNodeId, 2);
		// create token node
		Integer newTokenNodeId3 = createNode("token", newIdrefNodeId, 3);
		astTable.tkImageTable.put(newTokenNodeId3, ")");
		astTable.lineBeginTable.put(newTokenNodeId3, -1);
		astTable.columnBeginTable.put(newTokenNodeId3, -1);
		// create CONDITION node
		Integer newConditionNodeId = createNode("CONDITION", newAPLNodeId, 0);
		// create COMPARISON node
		Integer newComparisonNodeId = createNode("COMPARISON", newConditionNodeId, 0);
		// create EXPRESSION node
		Integer newExpressionNodeId = createNode("EXPRESSION", newComparisonNodeId, 0);
		// create PRODUCT node
		Integer newProductNodeId = createNode("PRODUCT", newExpressionNodeId, 0);
		// create MODULO node
		Integer newModuloNodeId = createNode("MODULO", newProductNodeId, 0);
		// create POWER node
		Integer newPowerNodeId = createNode("POWER", newModuloNodeId, 0);
		// create BITSHIFT node
		Integer newBitshiftNodeId = createNode("BITSHIFT", newPowerNodeId, 0);
		// create POWERREST node
		Integer newPowerrestNodeId = createNode("POWERREST", newPowerNodeId, 1);
		// create BITWISEANDOR node
		Integer newBitandorNodeId = createNode("BITWISEANDOR", newBitshiftNodeId, 0);
		// create BITWISENOT node
		Integer newBitnotNodeId = createNode("BITWISENOT", newBitandorNodeId, 0);
		// create OPERAND node
		Integer newOperandNodeId = createNode("OPERAND", newBitnotNodeId, 0);
		// create token node
		Integer newTokenNodeId2 = createNode("token", newOperandNodeId, 0);
		astTable.tkLiteralTable.put(newTokenNodeId2, 
									"\"probe begin function " + functionInfo.name + " (" + 
									functionInfo.line + ", " + functionInfo.column + ")\"");
		astTable.lineBeginTable.put(newTokenNodeId2, -1);
		astTable.columnBeginTable.put(newTokenNodeId2, -1);
	}

	private void injectStatLogEndFunction(Integer statlistNodeId, int index, FunctionInfo functionInfo) {
		// STAT
		//   COMMAND
		//     IDREF
		//       token <log> <identifier>
		//       CALL
		//       ACTUAL_PARAMETER_LIST
		//         CONDITION
		//           COMPARISON
		//             EXPRESSION
		//               PRODUCT
		//                 MODULO
		//                   POWER
		//                     BITSHIFT
		//                       BITWISEANDOR
		//                         BITWISENOT
		//                           OPERAND
		//                             token <"probe end functionName"> 
		//                     POWERREST
		//       token <)>

		// create STAT node
		Integer newStatNodeId = createNode("STAT", statlistNodeId, index);
		// create COMMAND node
		Integer newCommandNodeId = createNode("COMMAND", newStatNodeId, 0);
		// create IDREF node
		Integer newIdrefNodeId = createNode("IDREF", newCommandNodeId, 0);
		// create token node
		Integer newTokenNodeId = createNode("token", newIdrefNodeId, 0);
		astTable.tkImageTable.put(newTokenNodeId, "identifier");
		astTable.tkLiteralTable.put(newTokenNodeId, "log");
		astTable.lineBeginTable.put(newTokenNodeId, -1);
		astTable.columnBeginTable.put(newTokenNodeId, -1);
		// create CALL node
		Integer newCallNodeId = createNode("CALL", newIdrefNodeId, 1);
		// create ACTUAL_PARAMETER_LIST node
		Integer newAPLNodeId = createNode("ACTUAL_PARAMETER_LIST", newIdrefNodeId, 2);
		// create token node
		Integer newTokenNodeId3 = createNode("token", newIdrefNodeId, 3);
		astTable.tkImageTable.put(newTokenNodeId3, ")");
		astTable.lineBeginTable.put(newTokenNodeId3, -1);
		astTable.columnBeginTable.put(newTokenNodeId3, -1);
		// create CONDITION node
		Integer newConditionNodeId = createNode("CONDITION", newAPLNodeId, 0);
		// create COMPARISON node
		Integer newComparisonNodeId = createNode("COMPARISON", newConditionNodeId, 0);
		// create EXPRESSION node
		Integer newExpressionNodeId = createNode("EXPRESSION", newComparisonNodeId, 0);
		// create PRODUCT node
		Integer newProductNodeId = createNode("PRODUCT", newExpressionNodeId, 0);
		// create MODULO node
		Integer newModuloNodeId = createNode("MODULO", newProductNodeId, 0);
		// create POWER node
		Integer newPowerNodeId = createNode("POWER", newModuloNodeId, 0);
		// create BITSHIFT node
		Integer newBitshiftNodeId = createNode("BITSHIFT", newPowerNodeId, 0);
		// create POWERREST node
		Integer newPowerrestNodeId = createNode("POWERREST", newPowerNodeId, 1);
		// create BITWISEANDOR node
		Integer newBitandorNodeId = createNode("BITWISEANDOR", newBitshiftNodeId, 0);
		// create BITWISENOT node
		Integer newBitnotNodeId = createNode("BITWISENOT", newBitandorNodeId, 0);
		// create OPERAND node
		Integer newOperandNodeId = createNode("OPERAND", newBitnotNodeId, 0);
		// create token node
		Integer newTokenNodeId2 = createNode("token", newOperandNodeId, 0);
		astTable.tkLiteralTable.put(newTokenNodeId2, 
									"\"probe end function " + functionInfo.name + "\"");
		astTable.lineBeginTable.put(newTokenNodeId2, -1);
		astTable.columnBeginTable.put(newTokenNodeId2, -1);
	}

	private void injectStatLogReturnEndFunction(Integer statlistNodeId, int index, FunctionInfo functionInfo) {
		// STAT
		//   COMMAND
		//     IDREF
		//       token <log> <identifier>
		//       CALL
		//       ACTUAL_PARAMETER_LIST
		//         CONDITION
		//           COMPARISON
		//             EXPRESSION
		//               PRODUCT
		//                 MODULO
		//                   POWER
		//                     BITSHIFT
		//                       BITWISEANDOR
		//                         BITWISENOT
		//                           OPERAND
		//                             token <"probe return end functionName"> 
		//                     POWERREST
		//       token <)>

		// create STAT node
		Integer newStatNodeId = createNode("STAT", statlistNodeId, index);
		// create COMMAND node
		Integer newCommandNodeId = createNode("COMMAND", newStatNodeId, 0);
		// create IDREF node
		Integer newIdrefNodeId = createNode("IDREF", newCommandNodeId, 0);
		// create token node
		Integer newTokenNodeId = createNode("token", newIdrefNodeId, 0);
		astTable.tkImageTable.put(newTokenNodeId, "identifier");
		astTable.tkLiteralTable.put(newTokenNodeId, "log");
		astTable.lineBeginTable.put(newTokenNodeId, -1);
		astTable.columnBeginTable.put(newTokenNodeId, -1);
		// create CALL node
		Integer newCallNodeId = createNode("CALL", newIdrefNodeId, 1);
		// create ACTUAL_PARAMETER_LIST node
		Integer newAPLNodeId = createNode("ACTUAL_PARAMETER_LIST", newIdrefNodeId, 2);
		// create token node
		Integer newTokenNodeId3 = createNode("token", newIdrefNodeId, 3);
		astTable.tkImageTable.put(newTokenNodeId3, ")");
		astTable.lineBeginTable.put(newTokenNodeId3, -1);
		astTable.columnBeginTable.put(newTokenNodeId3, -1);
		// create CONDITION node
		Integer newConditionNodeId = createNode("CONDITION", newAPLNodeId, 0);
		// create COMPARISON node
		Integer newComparisonNodeId = createNode("COMPARISON", newConditionNodeId, 0);
		// create EXPRESSION node
		Integer newExpressionNodeId = createNode("EXPRESSION", newComparisonNodeId, 0);
		// create PRODUCT node
		Integer newProductNodeId = createNode("PRODUCT", newExpressionNodeId, 0);
		// create MODULO node
		Integer newModuloNodeId = createNode("MODULO", newProductNodeId, 0);
		// create POWER node
		Integer newPowerNodeId = createNode("POWER", newModuloNodeId, 0);
		// create BITSHIFT node
		Integer newBitshiftNodeId = createNode("BITSHIFT", newPowerNodeId, 0);
		// create POWERREST node
		Integer newPowerrestNodeId = createNode("POWERREST", newPowerNodeId, 1);
		// create BITWISEANDOR node
		Integer newBitandorNodeId = createNode("BITWISEANDOR", newBitshiftNodeId, 0);
		// create BITWISENOT node
		Integer newBitnotNodeId = createNode("BITWISENOT", newBitandorNodeId, 0);
		// create OPERAND node
		Integer newOperandNodeId = createNode("OPERAND", newBitnotNodeId, 0);
		// create token node
		Integer newTokenNodeId2 = createNode("token", newOperandNodeId, 0);
		String logText = "probe return end function " + functionInfo.name;
		astTable.tkLiteralTable.put(newTokenNodeId2, "\"" + logText + "\"");
		astTable.lineBeginTable.put(newTokenNodeId2, -1);
		astTable.columnBeginTable.put(newTokenNodeId2, -1);
	}

	private void injectStatVarWithExpression(Integer statlistNodeId, int index, Integer expressionNodeId, String varName) {
		// STAT
		//   VAR
		//     token <var> <variable>
		//     token <varName> <identifier>
		//     EXPRESSION

		verifyNodeType("EXPRESSION", expressionNodeId);

		// create STAT node
		Integer newStatNodeId = createNode("STAT", statlistNodeId, index);
		// create VAR node
		Integer newVarNodeId = createNode("VAR", newStatNodeId, 0);
		// create token node
		Integer newTokenNodeId = createNode("token", newVarNodeId, 0);
		astTable.tkImageTable.put(newTokenNodeId, "variable");
		astTable.tkLiteralTable.put(newTokenNodeId, "var");
		astTable.lineBeginTable.put(newTokenNodeId, -1);
		astTable.columnBeginTable.put(newTokenNodeId, -1);
		// create token node
		Integer newTokenNodeId3 = createNode("token", newVarNodeId, 1);
		astTable.tkImageTable.put(newTokenNodeId3, "identifier");
		astTable.tkLiteralTable.put(newTokenNodeId3, varName);
		astTable.lineBeginTable.put(newTokenNodeId3, -1);
		astTable.columnBeginTable.put(newTokenNodeId3, -1);
		// link EXPRESSION node
		List<Integer> varChildren = astTable.succTable.get(newVarNodeId);
		varChildren.add(expressionNodeId);
		astTable.succTable.put(newVarNodeId, varChildren);
		astTable.predTable.put(expressionNodeId, newVarNodeId);
	}

	private void injectStatVarWithStartIdref(Integer statlistNodeId, int index, Integer idrefNodeId, String varName) {
		// STAT
		//   VAR
		//     token <var> <variable>
		//     token <varName> <identifier>
		//     EXPRESSION
		//       PRODUCT
		//         MODULO
		//           POWER
		//             BITSHIFT
		//               BITWISEANDOR
		//                 BITWISENOT
		//                   OPERAND
		//                     IDREF
		//                       children to ACTUAL_PARAMETER_LIST
		//             POWERREST

		verifyNodeType("IDREF", idrefNodeId);

		// create STAT node
		Integer newStatNodeId = createNode("STAT", statlistNodeId, index);
		// create VAR node
		Integer newVarNodeId = createNode("VAR", newStatNodeId, 0);
		// create token node
		Integer newTokenNodeId = createNode("token", newVarNodeId, 0);
		astTable.tkImageTable.put(newTokenNodeId, "variable");
		astTable.tkLiteralTable.put(newTokenNodeId, "var");
		astTable.lineBeginTable.put(newTokenNodeId, -1);
		astTable.columnBeginTable.put(newTokenNodeId, -1);
		// create token node
		Integer newTokenNodeId3 = createNode("token", newVarNodeId, 1);
		astTable.tkImageTable.put(newTokenNodeId3, "identifier");
		astTable.tkLiteralTable.put(newTokenNodeId3, varName);
		astTable.lineBeginTable.put(newTokenNodeId3, -1);
		astTable.columnBeginTable.put(newTokenNodeId3, -1);
		// create EXPRESSION node
		Integer newExpressionNodeId = createNode("EXPRESSION", newVarNodeId, 2);
		// create PRODUCT node
		Integer newProductNodeId = createNode("PRODUCT", newExpressionNodeId, 0);
		// create MODULO node
		Integer newModuloNodeId = createNode("MODULO", newProductNodeId, 0);
		// create POWER node
		Integer newPowerNodeId = createNode("POWER", newModuloNodeId, 0);
		// create BITSHIFT node
		Integer newBitshiftNodeId = createNode("BITSHIFT", newPowerNodeId, 0);
		// create POWERREST node
		Integer newPowerrestNodeId = createNode("POWERREST", newPowerNodeId, 1);
		// create BITWISEANDOR node
		Integer newBitandorNodeId = createNode("BITWISEANDOR", newBitshiftNodeId, 0);
		// create BITWISENOT node
		Integer newBitnotNodeId = createNode("BITWISENOT", newBitandorNodeId, 0);
		// create OPERAND node
		Integer newOperandNodeId = createNode("OPERAND", newBitnotNodeId, 0);
		// create IDREF node
		Integer newIdrefNodeId = createNode("IDREF", newOperandNodeId, 0);
		// link start of existing idref to new idref
		List<Integer> idrefChildren = astTable.succTable.get(idrefNodeId);
		if (idrefChildren == null) {
			System.err.println("Expected succesors at node "
								+ idrefNodeId + ", but found none.");
			System.exit(1);
		}
		List<Integer> newIdrefChildren = new ArrayList<Integer>();
		for (int i = 0; i < idrefChildren.size(); i++) {
			Integer idrefChild = idrefChildren.get(i);

			astTable.predTable.put(idrefChild, newIdrefNodeId);
			newIdrefChildren.add(idrefChild);

			if (astTable.getType(idrefChild).equals("token")) {
				String curImg = astTable.getTkImage(idrefChild);
				if (curImg != null && curImg.equals(")")) {
					break;
				}
			}
		}
		astTable.succTable.put(newIdrefNodeId, newIdrefChildren);
	}

	private void injectReplaceStartIdrefWithToken(Integer idrefNodeId, String varName) {
		verifyNodeType("IDREF", idrefNodeId);

		List<Integer> idrefChildren = astTable.succTable.get(idrefNodeId);
		if (idrefChildren == null) {
			System.err.println("Expected succesors at node "
								+ idrefNodeId + ", but found none.");
			System.exit(1);
		}
		int i = 0;
		for (; i < idrefChildren.size(); i++) {
			Integer idrefChild = idrefChildren.get(i);
			if (astTable.getType(idrefChild).equals("token")) {
				String curImg = astTable.getTkImage(idrefChild);
				if (curImg != null && curImg.equals(")")) {
					break;
				}
			}
		}
		i++;
		List<Integer> finalIdrefChildren = new ArrayList<Integer>();
		for (; i < idrefChildren.size(); i++) {
			finalIdrefChildren.add(idrefChildren.get(i));
		}

		// create token node
		Integer newTokenNodeId = createNode("token", idrefNodeId, 0);
		astTable.tkImageTable.put(newTokenNodeId, "identifier");
		astTable.tkLiteralTable.put(newTokenNodeId, varName);
		astTable.lineBeginTable.put(newTokenNodeId, -1);
		astTable.columnBeginTable.put(newTokenNodeId, -1);

		finalIdrefChildren.add(0, newTokenNodeId);

		astTable.succTable.put(idrefNodeId, finalIdrefChildren);
	}

	private Integer injectReplaceExpressionInReturn(Integer returnNodeId, String varName) {
		// RETURN
		//   CONDITION
		//     COMPARISON
		//       EXPRESSION
		//         PRODUCT
		//           MODULO
		//             POWER
		//               BITSHIFT
		//                 BITWISEANDOR
		//                   BITWISENOT
		//                     OPERAND
		//                       IDREF
		//                         token <varName> <identifier>
		//               POWERREST

		verifyNodeType("RETURN", returnNodeId);

		List<Integer> returnChildren = astTable.succTable.get(returnNodeId);

		if (returnChildren == null || returnChildren.size() == 0) {
			System.err.println("Expected succesors at node "
								+ returnNodeId + ", but found none.");
			System.exit(1);
		}

		if (returnChildren.size() != 1) {
			System.err.println("Expected exactly 1 successor at node "
								+ returnNodeId + ", but found " + returnChildren.size() + ".");
			System.exit(1);
		}

		Integer conditionNodeId = returnChildren.get(0);
		verifyNodeType("CONDITION", conditionNodeId);

		List<Integer> conditionChildren = astTable.succTable.get(conditionNodeId);

		if (conditionChildren == null || conditionChildren.size() == 0) {
			System.err.println("Expected succesors at node "
								+ conditionNodeId + ", but found none.");
			System.exit(1);
		}

		Integer comparisonNodeId = conditionChildren.get(0);
		verifyNodeType("COMPARISON", comparisonNodeId);

		List<Integer> comparisonChildren = astTable.succTable.get(comparisonNodeId);
		if (comparisonChildren == null || comparisonChildren.size() == 0) {
			System.err.println("Expected succesors at node "
								+ comparisonNodeId + ", but found none.");
			System.exit(1);
		}

		Integer expressionNodeId = comparisonChildren.get(0);
		verifyNodeType("EXPRESSION", expressionNodeId);

		// unlink expression
		astTable.succTable.put(comparisonNodeId, new ArrayList<Integer>());

		// create EXPRESSION node
		Integer newExpressionNodeId = createNode("EXPRESSION", comparisonNodeId, 0);
		// create PRODUCT node
		Integer newProductNodeId = createNode("PRODUCT", newExpressionNodeId, 0);
		// create MODULO node
		Integer newModuloNodeId = createNode("MODULO", newProductNodeId, 0);
		// create POWER node
		Integer newPowerNodeId = createNode("POWER", newModuloNodeId, 0);
		// create BITSHIFT node
		Integer newBitshiftNodeId = createNode("BITSHIFT", newPowerNodeId, 0);
		// create POWERREST node
		Integer newPowerrestNodeId = createNode("POWERREST", newPowerNodeId, 1);
		// create BITWISEANDOR node
		Integer newBitandorNodeId = createNode("BITWISEANDOR", newBitshiftNodeId, 0);
		// create BITWISENOT node
		Integer newBitnotNodeId = createNode("BITWISENOT", newBitandorNodeId, 0);
		// create OPERAND node
		Integer newOperandNodeId = createNode("OPERAND", newBitnotNodeId, 0);
		// create IDREF node
		Integer newIdrefNodeId = createNode("IDREF", newOperandNodeId, 0);
		// create token node
		Integer newTokenNodeId = createNode("token", newIdrefNodeId, 0);
		astTable.tkImageTable.put(newTokenNodeId, "identifier");
		astTable.tkLiteralTable.put(newTokenNodeId, varName);
		astTable.lineBeginTable.put(newTokenNodeId, -1);
		astTable.columnBeginTable.put(newTokenNodeId, -1);

		return expressionNodeId;
	}


	//
	// AST utils
	//

	private void verifyNodeType(String type, Integer astNodeId) {
		if (!astTable.getType(astNodeId).equals(type)) {
			System.err.println("Expected an " + type + " node at node " + astNodeId + ".");
			System.exit(1);
		}
		return;
	}

	private boolean isSimpleReturn(Integer returnNodeId) {

		// RETURN (2178 13)
		//   CONDITION (2179 14)
		//      COMPARISON (2180 15)
		//   	  EXPRESSION (2181 16)
		//   		 PRODUCT (2182 17)
		//   			MODULO (2183 18)
		//   			   POWER (2184 19)
		//   				  BITSHIFT (2185 20)
		//   					 BITWISEANDOR (2186 21)
		//   						BITWISENOT (2187 22)
		//   						   OPERAND (2188 23)
		//   							  token (2189 24) 4 <?> <?>
		//                                or
		//                                IDREF (1307 22)
		//                                  token (1308 23) 0 <?> <identifier>*
		//   				  POWERREST (2190 20)
  
		verifyNodeType("RETURN", returnNodeId);

		List<Integer> returnChildren = astTable.succTable.get(returnNodeId);

		if (returnChildren == null || !(returnChildren.size() == 1)) {
			return false;
		}

		Integer conditionNodeId = returnChildren.get(0);

		if (!astTable.getType(conditionNodeId).equals("CONDITION")) {
			return false;
		}

		List<Integer> conditionChildren = astTable.succTable.get(conditionNodeId);

		if (conditionChildren == null || !(conditionChildren.size() == 1)) {
			return false;
		}

		Integer comparisonNodeId = conditionChildren.get(0);

		if (!astTable.getType(comparisonNodeId).equals("COMPARISON")) {
			return false;
		}

		List<Integer> comparisonChildren = astTable.succTable.get(comparisonNodeId);

		if (comparisonChildren == null || !(comparisonChildren.size() == 1)) {
			return false;
		}

		Integer expressionNodeId = comparisonChildren.get(0);

		if (!astTable.getType(expressionNodeId).equals("EXPRESSION")) {
			return false;
		}

		List<Integer> expressionChildren = astTable.succTable.get(expressionNodeId);

		if (expressionChildren == null || !(expressionChildren.size() == 1)) {
			return false;
		}

		Integer productNodeId = expressionChildren.get(0);

		if (!astTable.getType(productNodeId).equals("PRODUCT")) {
			return false;
		}

		List<Integer> productChildren = astTable.succTable.get(productNodeId);

		if (productChildren == null || !(productChildren.size() == 1)) {
			return false;
		}

		Integer moduloNodeId = productChildren.get(0);

		if (!astTable.getType(moduloNodeId).equals("MODULO")) {
			return false;
		}

		List<Integer> moduloChildren = astTable.succTable.get(moduloNodeId);

		if (moduloChildren == null || !(moduloChildren.size() == 1)) {
			return false;
		}

		Integer powerNodeId = moduloChildren.get(0);

		if (!astTable.getType(powerNodeId).equals("POWER")) {
			return false;
		}

		List<Integer> powerChildren = astTable.succTable.get(powerNodeId);

		if (powerChildren == null || !(powerChildren.size() == 2)) {
			return false;
		}

		Integer bitshiftNodeId = powerChildren.get(0);

		if (!astTable.getType(bitshiftNodeId).equals("BITSHIFT")) {
			return false;
		}

		Integer powerrestNodeId = powerChildren.get(1);

		if (!astTable.getType(powerrestNodeId).equals("POWERREST")) {
			return false;
		}

		List<Integer> bitshiftChildren = astTable.succTable.get(bitshiftNodeId);
		
		if (bitshiftChildren == null || !(bitshiftChildren.size() == 1)) {
			return false;
		}

		Integer bitandorNodeId = bitshiftChildren.get(0);

		if (!astTable.getType(bitandorNodeId).equals("BITWISEANDOR")) {
			return false;
		}

		List<Integer> bitandorChildren = astTable.succTable.get(bitandorNodeId);

		if (bitandorChildren == null || !(bitandorChildren.size() == 1)) {
			return false;
		}

		Integer bitnotNodeId = bitandorChildren.get(0);

		if (!astTable.getType(bitnotNodeId).equals("BITWISENOT")) {
			return false;
		}

		List<Integer> bitnotChildren = astTable.succTable.get(bitnotNodeId);

		if (bitnotChildren == null || !(bitnotChildren.size() == 1)) {
			return false;
		}

		Integer operandNodeId = bitnotChildren.get(0);

		if (!astTable.getType(operandNodeId).equals("OPERAND")) {
			return false;
		}

		List<Integer> operandChildren = astTable.succTable.get(operandNodeId);

		if (operandChildren == null || !(operandChildren.size() == 1)) {
			return false;
		}

		Integer operandChildNodeId = operandChildren.get(0);

		// token or IDREF with tokens only
		if (astTable.getType(operandChildNodeId).equals("token")) {
			return true;
		}

		if (astTable.getType(operandChildNodeId).equals("IDREF")) {
			List<Integer> idrefChildren = astTable.succTable.get(operandChildNodeId);
			if (idrefChildren == null) {
				return false;
			}
			for (int i = 0; i < idrefChildren.size(); i++) {
				if (!astTable.getType(idrefChildren.get(i)).equals("token")) {
					return false;
				}
			}
			return true;
		}

		return false;
	}

	private boolean isSimpleIdref(Integer idrefNodeId) {
		// STAT
		//   COMMAND
		//     IDREF

		verifyNodeType("IDREF", idrefNodeId);

		Integer commandNodeId = astTable.predTable.get(idrefNodeId);

		if (!astTable.getType(commandNodeId).equals("COMMAND")) {
			return false;
		}

		if (astTable.succTable.get(commandNodeId).size() != 1) {
			return false;
		}

		Integer statNodeId = astTable.predTable.get(commandNodeId);

		if (!astTable.getType(statNodeId).equals("STAT")) {
			return false;
		}

		return true;
	}

	private List<Integer> getNodesSuccessorOfType(String type, Integer astNodeId) {
		List<Integer> children = astTable.succTable.get(astNodeId);
		if (children == null) {
			return null;
		}
		List<Integer> selectedNodes = new ArrayList<Integer>();
		for(int i = 0; i < children.size(); i++) {
			Integer childNodeId = children.get(i);
			if(astTable.getType(childNodeId).equals(type)) {
				selectedNodes.add(childNodeId);
			}
		}
		return selectedNodes;
	}

	private CallInfo getCallInfo(Integer idrefNodeId, Integer callNodeId) {
		verifyNodeType("IDREF", idrefNodeId);
		verifyNodeType("CALL", callNodeId);

		List<Integer> idrefChildren = astTable.succTable.get(idrefNodeId);
		
		if (idrefChildren == null) {
			System.err.println("Expected succesors at node "
								+ idrefChildren + ", but found none.");
			System.exit(1);
		}

		CallInfo callInfo = new CallInfo();

		boolean previousIsToken = false;
		int i = 0;
		for (; i < idrefChildren.size(); i++) {
			Integer childNodeId = idrefChildren.get(i);

			if (childNodeId == callNodeId) {
				break;
			}

			if (astTable.getType(childNodeId).equals("token")) {
				if (!previousIsToken){
					previousIsToken = true;
				} else {
					callInfo.name += ".";
				}
			} else {
				previousIsToken = false;
			}
			if (astTable.getType(childNodeId).equals("EXPRESSION")) {
				callInfo.name += "[";
			}

			visitCallName(childNodeId, callInfo);
		}
		for (; i < idrefChildren.size(); i++) {
			Integer childNodeId = idrefChildren.get(i);
			if (astTable.getType(childNodeId).equals("token")) {
				String curImg = astTable.getTkImage(childNodeId);

				if (curImg != null && curImg.equals(")")) {
					callInfo.line = astTable.lineBeginTable.get(childNodeId);
					callInfo.column = astTable.columnBeginTable.get(childNodeId);
					break;
				}
			}
		}

		return callInfo;
	}



	private Integer getFirstPredecessorOfTypes(Set<String> types, Integer astNodeId) {
		Integer parentNodeId = astTable.predTable.get(astNodeId);
		while (parentNodeId != null) {
			String curType = astTable.getType(parentNodeId);
			if (curType == null) {
				String error = "Expected a predecessor of types ";
				error += types;
				error += " at node " + astNodeId + ", but found none.";
				System.err.println(error);
				System.exit(1);
			}
			if (types.contains(astTable.getType(parentNodeId))){
				return parentNodeId;
			}
			parentNodeId = astTable.predTable.get(parentNodeId);
		}

		return 0;
	}

	private Integer getFirstPredecessorOfType(String type, Integer astNodeId) {
		Integer parentNodeId = astTable.predTable.get(astNodeId);
		while (parentNodeId != null) {
			String curType = astTable.getType(parentNodeId);
			if (curType == null) {
				System.err.println("Expected a predecessor of type " + type + " at node "
									+ astNodeId + ", but found none.");
				System.exit(1);
			}
			if (astTable.getType(parentNodeId).equals(type)){
				return parentNodeId;
			}
			parentNodeId = astTable.predTable.get(parentNodeId);
		}

		return 0;
	}

	private boolean hasPredecessorOfType(String type, Integer astNodeId) {
		Integer parentNodeId = astTable.predTable.get(astNodeId);
		while (parentNodeId != null) {
			String curType = astTable.getType(parentNodeId);
			if (curType == null) {
				return false;
			}
			if (astTable.getType(parentNodeId).equals(type)){
				return true;
			}
			parentNodeId = astTable.predTable.get(parentNodeId);
		}

		return false;
	}

	private FunctionInfo getFunctionInfo(Integer functionNodeId) {
		verifyNodeType("FUNCTION", functionNodeId);

		List<Integer> children = astTable.succTable.get(functionNodeId);
		if (children == null) {
			System.err.println("Expected successors to node " + functionNodeId + ", but got none.");
			System.exit(1);
		}

		FunctionInfo functionInfo = new FunctionInfo();

		for (int i = 0; i < children.size(); i++) {
			int curFunctionChildId = children.get(i);
			if (astTable.getType(curFunctionChildId).equals("token")) {
				String curLiteral = astTable.getTkLiteral(curFunctionChildId);
				String curImg = astTable.getTkImage(curFunctionChildId);

				if (curImg != null && curImg.equals("identifier") && curLiteral != null) {
					functionInfo.name = curLiteral; 
					functionInfo.line = astTable.lineBeginTable.get(curFunctionChildId);
					functionInfo.column = astTable.columnBeginTable.get(curFunctionChildId);
					break;
				}
			}
		}

		return functionInfo;
	}

	private Integer getFirstSuccOfType(String type, Integer astNodeId) {
		List<Integer> children = astTable.succTable.get(astNodeId);
		if (children == null) {
			System.err.println("Expected succesors at node "
								+ astNodeId + ", but found none.");
			System.exit(1);
		}
		for(int i = 0; i < children.size(); i++) {
			Integer curChild = children.get(i);
			if (astTable.getType(curChild).equals(type)) {
				return curChild;
			}
		}
		System.err.println("Expected a node " + type + " in the succesors of node "
							+ astNodeId + ", but found none.");
		System.exit(1);
		return 0;
	}

	private int getIndexOfNodeInSucc(Integer astNodeId) {
		Integer predNodeId = astTable.predTable.get(astNodeId);
		if (predNodeId == null) {
			System.err.println("Expected a predecessor at node "
								+ astNodeId + ", but found none.");
			System.exit(1);
		}
		List<Integer> children = astTable.succTable.get(predNodeId);
		for (int i = 0; i < children.size(); i++) {
			if (children.get(i).equals(astNodeId)) {
				return i;
			}
		}
		return -1;
	}

	private int getIndexFirstNonEmptyStat(Integer statlistNodeId) {
		verifyNodeType("STATLIST", statlistNodeId);

		List<Integer> children = astTable.succTable.get(statlistNodeId);

		if (children == null) {
			System.err.println("Expected succesors list to exist at STATLIST node "
								+ statlistNodeId + ", but it didn't.");
			System.exit(1);
		}

		for (int i = 0; i < children.size(); i++) {
			int curChild = children.get(i);
			if (astTable.getType(curChild).equals("STAT") 
				&& astTable.succTable.get(curChild) != null 
				&& astTable.succTable.get(curChild).size() != 0) {
				return i;
			}
		}
		return 0;
	}

	private int getIndexLastNonEmptyStat(Integer statlistNodeId) {
		verifyNodeType("STATLIST", statlistNodeId);

		List<Integer> children = astTable.succTable.get(statlistNodeId);
		if (children == null) {
			System.err.println("Expected succesors list to exist at STATLIST node "
								+ statlistNodeId + ", but it didn't.");
			System.exit(1);
		}
		for (int i = children.size() - 1; i >= 0; i--) {
			int curChild = children.get(i);
			if (astTable.getType(curChild).equals("STAT") 
				&& astTable.succTable.get(curChild) != null 
				&& astTable.succTable.get(curChild).size() != 0) {
				return i;
			}
		}
		return 0;
	}


	private void visitChildrenCallName(Integer astNodeId, CallInfo callInfo) {

	    List<Integer> children = astTable.succTable.get(astNodeId);
	    if (children != null) {
	        for (Integer childId: children) {
		        visitCallName(childId, callInfo);
	        }
	    }

	    return;
    }
	
    //
    // Node visitors
	//
	
	private void visitCallName_GENERIC(Integer astNodeId, CallInfo callInfo) {
	    
	    visitChildrenCallName(astNodeId, callInfo);

	    return;
	}
	

	private void visitCallName_token(Integer astNodeId, CallInfo callInfo) {
	    
		String curLiteral = astTable.getTkLiteral(astNodeId);
		String curImg = astTable.getTkImage(astNodeId);

		if (callInfo.column == -1 && callInfo.line == -1) {	    
	    	callInfo.line = astTable.lineBeginTable.get(astNodeId);
			callInfo.column = astTable.columnBeginTable.get(astNodeId);
		}
	    
	    if (curLiteral != null && !curLiteral.equals("UNDEFINED")) {
	    	callInfo.name += curLiteral;
	    } else {
	        if (!curImg.equals("UNDEFINED")) {
		        callInfo.name += curImg;
	        }
	    }
	    
	    visitChildrenCallName(astNodeId, callInfo);

	    return;
	}

	private void visitCallName_CALL(Integer astNodeId, CallInfo callInfo) {
		
		callInfo.name += "(";

		visitChildrenCallName(astNodeId, callInfo);
		
		return;
	}

	private void visitCallName_COMPARISON(Integer astNodeId, CallInfo callInfo) {

		List<Integer> children = astTable.succTable.get(astNodeId);
		
		if (children != null && children.size() > 1) {
			for (int i = 0; i < children.size(); i++) {
				if (i == 2 || i == 3) {
					callInfo.name += " ";
				}
				visitCallName(children.get(i), callInfo);
			}
		} else {
			visitChildrenCallName(astNodeId, callInfo);
		}

		return;
	}
	
	private void visitCallName_EQUATION(Integer astNodeId, CallInfo callInfo) {
	    
		List<Integer> children = astTable.succTable.get(astNodeId);
		if (children == null) {
			System.err.println("Expected succesors at node " +
								astNodeId + " of type " +
								astTable.getType(astNodeId));
			System.exit(1);
		}

	    if (children.size() >= 4) {
			visitCallName(children.get(0), callInfo);
			callInfo.name += " ";
	        visitCallName(children.get(3), callInfo);
			callInfo.name += " ";
	        visitCallName(children.get(1), callInfo);

	        for	(int i = 4; i < children.size(); i += 3) {
				callInfo.name += " ";
				visitCallName(children.get(i + 2), callInfo);
				callInfo.name += " ";
				visitCallName(children.get(i), callInfo);
			}

		} else if (children.size() == 3 && astTable.getType(children.get(1)).equals("UNARY_LOGIC_OP")){
			visitCallName(children.get(2), callInfo);
			callInfo.name += " ";
			visitCallName(children.get(0), callInfo);
	    } else {
	        visitChildrenCallName(astNodeId, callInfo);
	    }
	        
	    return;
	}

	private void visitCallName_IDREF(Integer astNodeId, CallInfo callInfo) {
		List<Integer> children = astTable.succTable.get(astNodeId);
		if (children != null) {
			boolean previousIsToken = false;
			for (int i = 0; i < children.size(); i++) {
				if (astTable.getType(children.get(i)).equals("token")) {
					if (!previousIsToken){
						previousIsToken = true;
					} else {
						callInfo.name += ".";
					}
				} else {
					previousIsToken = false;
				}
				if (astTable.getType(children.get(i)).equals("EXPRESSION")) {
					callInfo.name += "[";
				}
				visitCallName(children.get(i), callInfo);
			}
		}
		return;
	}

	private void visitCallName_LAMBDA(Integer astNodeId, CallInfo callInfo) {
	    List<Integer> children = astTable.succTable.get(astNodeId);
	    if (children != null && children.size() > 0) {
	        for (int i = 0; i < children.size(); i++) {
				if (astTable.getType(children.get(i)).equals("BLOCK")) {
					callInfo.name += " ";
				}
				visitCallName(children.get(i), callInfo);
	        }
	    } else {
			visitChildrenCallName(astNodeId, callInfo);
		}

	    return;
	}

	private void visitCallName_LIST(Integer astNodeId, CallInfo callInfo) {
    	callInfo.name += "{";

		List<Integer> children = astTable.succTable.get(astNodeId);
		if (children != null) {
			visitCallName(children.get(0), callInfo);
			int i = 1;
			while (i < children.size() && astTable.getType(children.get(i)).equals("LIST_STATEMENT")) {
				callInfo.name += ", ";
				visitCallName(children.get(i), callInfo);
				i++;
			}
			for (; i < children.size(); i++) {
				visitCallName(children.get(i), callInfo);
			}
		}
		return;

	}

	private void visitCallName_OPERAND(Integer astNodeId, CallInfo callInfo) {

		List<Integer> children = astTable.succTable.get(astNodeId);
		if(children.size() == 2
				&& astTable.getType(children.get(1)).equals("token")
				&& astTable.getTkImage(children.get(1)).equals(")")) {
			callInfo.name += "(";
		}

		visitChildrenCallName(astNodeId, callInfo);

		return;

	}

	private void visitCallName_PARAMETER_LIST(Integer astNodeId, CallInfo callInfo) {
	    
	    List<Integer> children = astTable.succTable.get(astNodeId);
	    if (children != null) {
	        visitCallName(children.get(0), callInfo);
	        for (int i = 1; i < children.size(); i++) {
	            callInfo.name += ", ";
		        visitCallName(children.get(i), callInfo);
	        }
	    }

	    return;
	}

	private void visitCallName_POWERREST(Integer astNodeId, CallInfo callInfo) {
		List<Integer> children = astTable.succTable.get(astNodeId);
		if(children != null && children.size() == 3) {
			visitCallName(children.get(2), callInfo);
			visitCallName(children.get(0), callInfo);
		}
		else {
			visitChildrenCallName(astNodeId, callInfo);
		}

		return;
	}
	
    //
    // Node instrumentors
	//
	
	private void instrument_FUNCTION(Integer astNodeId) {

		// FUNCTION
        //   token <function>
        //   token <check_rc_wp> <identifier>
        //   token <(>
        //   FORMAL_PARAMETER_LIST 
        //   token <)>
		//   BLOCK
		
		List<Integer> functionChildren = astTable.succTable.get(astNodeId);
		if (functionChildren != null) {
			// find the function name
			FunctionInfo functionInfo = getFunctionInfo(astNodeId);

			// find the block of the function
			Integer blockNodeId = getFirstSuccOfType("BLOCK", astNodeId);

			// find the statlist of the block
			Integer statlistNodeId = getFirstSuccOfType("STATLIST", blockNodeId);

			// find the stat inject index in statlist
			int statInjectIndex = getIndexFirstNonEmptyStat(statlistNodeId);

			// inject probe begin function before the first non empty STAT
			injectStatLogBeginFunction(statlistNodeId, statInjectIndex, functionInfo);

			// find the stat inject index in statlist
			int endStatInjectIndex = getIndexLastNonEmptyStat(statlistNodeId) + 1;

			// inject probe begin function before the first non empty STAT
			injectStatLogEndFunction(statlistNodeId, endStatInjectIndex, functionInfo);
		}

	    return;
	}
	

	private void instrument_IDREF_calls(Integer astNodeId) {

		// IDREF
		//   token
		//  [token*
		//  [EXPRESSION
		//   token <]>]*]*
		//   CALL
		//   ACTUAL_PARAMETER_LIST
		//   token <)>

		List<Integer> callNodes = getNodesSuccessorOfType("CALL", astNodeId);

		boolean isSimpleIdref = isSimpleIdref(astNodeId);

		if (callNodes != null) {
			for(int i = 0; i < callNodes.size(); i++) {
				Integer callNodeId = callNodes.get(i);
				// TODO: verify getCallName is accurate in complex situations
				CallInfo callInfo = getCallInfo(astNodeId, callNodeId);
				Integer statNodeId = getFirstPredecessorOfType("STAT", astNodeId);
				Integer statPredNodeId = astTable.predTable.get(statNodeId);

				if (astTable.getType(statPredNodeId).equals("BLOCKSTAT")) {
					addBlockToBlockstat(statPredNodeId);
					statPredNodeId = astTable.predTable.get(statNodeId);
				}

				verifyNodeType("STATLIST", statPredNodeId);

				int statIndex = getIndexOfNodeInSucc(statNodeId);

				if (isSimpleIdref && i == callNodes.size() -1) {
					injectStatLogCall(statPredNodeId, statIndex, callInfo);
				} else {
					String varName = createUniqueVarName();
					injectStatVarWithStartIdref(statPredNodeId, statIndex, astNodeId, varName);
					injectStatLogCall(statPredNodeId, statIndex, callInfo);
					injectReplaceStartIdrefWithToken(astNodeId, varName);
				}

			}
		}

		return;
	}

	private void instrument_RETURN(Integer astNodeId) {

		// STAT
		//   RETURN
		//     CONDITION
		//       COMPARISON
		//         EXPRESSION
		//           ...
		// becomes:
		// STAT
		//   VAR
		//     token <var> <variable>
		//     token <instrumentation_temp_variable_93847593957> <identifier>
		//     EXPRESSION
	    // STAT
		//   COMMAND
		//     IDREF
		//       token <log> <identifier>
		//       CALL
		//       ACTUAL_PARAMETER_LIST
		//         CONDITION
		//           COMPARISON
		//             EXPRESSION
		//               PRODUCT
		//                 MODULO
		//                   POWER
		//                     BITSHIFT
		//                       BITWISEANDOR
		//                         BITWISENOT
		//                           OPERAND
		//                             token <"probe return end functionName"> 
		//                     POWERREST
		//       token <)>
		// STAT
		//   RETURN
		//     CONDITION
		//       COMPARISON
		//         EXPRESSION
		//           PRODUCT
		//             MODULO
		//               POWER
		//                 BITSHIFT
		//                   BITWISEANDOR
		//                     BITWISENOT
		//                       OPERAND
		//                         IDREF
		//                           token <instrumentation_temp_variable_93847593957> <identifier>
		//                 POWERREST

		Set<String> types = new HashSet<String>();
		types.add("FUNCTION");
		types.add("LAMBDA");
		Integer functionNodeId = getFirstPredecessorOfTypes(types, astNodeId);
		if (astTable.getType(functionNodeId).equals("LAMBDA")) {
			// TODO: what to do with LAMBDAS?
			return;
		}

		FunctionInfo functionInfo = getFunctionInfo(functionNodeId);

		Integer statNodeId = getFirstPredecessorOfType("STAT", astNodeId);
		int statIndex = getIndexOfNodeInSucc(statNodeId);

		Integer statPredNodeId = astTable.predTable.get(statNodeId);

		if (astTable.getType(statPredNodeId).equals("BLOCKSTAT")) {
			addBlockToBlockstat(statPredNodeId);
			statPredNodeId = astTable.predTable.get(statNodeId);
		}

		verifyNodeType("STATLIST", statPredNodeId);

		List<Integer> returnChildren = astTable.succTable.get(astNodeId);
		if (returnChildren == null || returnChildren.size() == 0 || isSimpleReturn(astNodeId)) {
			injectStatLogReturnEndFunction(statPredNodeId, statIndex, functionInfo);
			return;
		}
		
		String varName = createUniqueVarName();

		Integer expressionNodeId = injectReplaceExpressionInReturn(astNodeId, varName);

		injectStatLogReturnEndFunction(statPredNodeId, statIndex, functionInfo);

		injectStatVarWithExpression(statPredNodeId, statIndex, expressionNodeId, varName);

		return;
	}

    public void injectLogs(Integer rootNodeId) {

		List<Integer> nodes = new ArrayList<Integer>();

		updateNodeList(rootNodeId, nodes);

		Collections.reverse(nodes);

		for (Integer nodeId: nodes) {
			String curType = astTable.getType(nodeId);
			if (curType == null) {
				System.err.println("ERROR: missing type for node " +
									nodeId);
				System.exit(1);
			}
			switch (curType) {

				case "ACTUAL_PARAMETER_LIST":
				case "CALL":
				case "BINARY_ARITHM_OP":
				case "BINARY_LOGIC_OP":
				case "BITSHIFT":
				case "BITWISEANDOR":
				case "BITWISENOT":
				case "BLOCK":
				case "BLOCKSTAT":
				case "COMMAND":
				case "COMPARISON":
				case "CONDITION":
				case "CONDITION_LIST":
				case "EXPRESSION":
				case "FORMAL_PARAMETER_LIST":
				case "IDLIST":
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
				case "SCRIPT":
				case "STAT":
				case "STATLIST":
				case "token":
				case "UNARY_ARITHM_OP":
				case "UNARY_LOGIC_OP":
				case "VAR":
				case "WHILE":
					break;
	
				case "FUNCTION":
					instrument_FUNCTION(nodeId);
					break;
				
				case "IDREF":
					instrument_IDREF_calls(nodeId);
					break;
				
				case "RETURN":
					instrument_RETURN(nodeId);
					break;
					
				default:
					System.err.println("ERROR: invalid type " +
							curType +
							" for node " +
							nodeId);
					System.exit(1);
				}
		}

		return;
	}

	public void updateNodeList(Integer astNodeId, List<Integer> nodes) {
		nodes.add(astNodeId);		
		List<Integer> children = astTable.succTable.get(astNodeId);
		if (children != null) {
			for (Integer childId: children) {
				updateNodeList(childId, nodes);
			}
		}
		return;
	}
	
	public void visitCallName(Integer astNodeId, CallInfo callInfo) {

		String curType = astTable.getType(astNodeId);
		if (curType == null) {
			System.err.println("ERROR: missing type for node " +
								astNodeId);
			System.exit(1);
		}
		switch (curType) {

			case "BINARY_ARITHM_OP":
			case "BINARY_LOGIC_OP":
			case "BITSHIFT":
			case "BITWISEANDOR":
			case "BITWISENOT":
			case "BLOCK":
			case "BLOCKSTAT":
			case "COMMAND":
			case "CONDITION_LIST":
			case "FUNCTION":
			case "IDLIST":
			case "IF":
			case "LIST_STATEMENT":
			case "PARSE":
			case "POWER":
			case "REL_OP":
			case "RETURN":
			case "SCRIPT":
			case "STAT":
			case "STATLIST":
			case "UNARY_ARITHM_OP":
			case "UNARY_LOGIC_OP":
			case "VAR":
			case "WHILE":
				visitCallName_GENERIC(astNodeId, callInfo);
				break;

			case "token":
				visitCallName_token(astNodeId, callInfo);
				break;

			case "ACTUAL_PARAMETER_LIST":
			case "FORMAL_PARAMETER_LIST":
				visitCallName_PARAMETER_LIST(astNodeId, callInfo);
				break;

			case "CALL":	
				visitCallName_CALL(astNodeId, callInfo);
				break;

			case "COMPARISON":
				visitCallName_COMPARISON(astNodeId, callInfo);
				break;

			case "CONDITION":
			case "EXPRESSION":
			case "MODULO":
			case "PRODUCT":
				visitCallName_EQUATION(astNodeId, callInfo);
				break;

			case "IDREF":
				visitCallName_IDREF(astNodeId, callInfo);
				break;
					
			case "LAMBDA":
				visitCallName_LAMBDA(astNodeId, callInfo);
				break;

			case "LIST":
				visitCallName_LIST(astNodeId, callInfo);
				break;

			case "OPERAND":
				visitCallName_OPERAND(astNodeId, callInfo);
				break;

			case "POWERREST":
				visitCallName_POWERREST(astNodeId, callInfo);
				break;
					
			default:
				System.err.println("ERROR: invalid type " +
						curType +
						" for node " +
						astNodeId);
				System.exit(1);
		}
		return;
	}

	// Data classes

	private class FunctionInfo {
		public String name = "";
		public int line = -1;
		public int column = -1;

		public FunctionInfo() {
			name = "";
			line = -1;
			column = -1;
		}
	}

	private class CallInfo {
		public String name = "";
		public int line = -1;
		public int column = -1;

		public CallInfo() {
			name = "";
			line = -1;
			column = -1;
		}
	}
}
