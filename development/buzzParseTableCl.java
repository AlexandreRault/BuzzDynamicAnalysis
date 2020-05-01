/**
  Copyright (C) Dec 2017, Jan 2019 Ettore Merlo - All rights reserved
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.HashSet;

class buzzParseTableCl implements defsInt {

    private int printSeed = 0;

    public HashMap<String, String> attrTable =
        new HashMap<String, String>();

    public HashMap<Integer, String> nodeTypeTable =
	new HashMap<Integer, String>();

    public HashMap<Integer, String> tkLiteralTable =
	new HashMap<Integer, String>();

    public HashMap<Integer, String> tkImageTable =
	new HashMap<Integer, String>();

    public HashMap<Integer, Integer> tkTypeTable =
	new HashMap<Integer, Integer>();
    
    //
    // source line related info
    //

    protected Map<Integer, Integer> lineBeginTable =
	new HashMap<Integer, Integer>();
    protected Map<Integer, Integer> lineEndTable =
	new HashMap<Integer, Integer>();
    protected Map<Integer, Integer> columnBeginTable =
	new HashMap<Integer, Integer>();
    protected Map<Integer, Integer> columnEndTable =
	new HashMap<Integer, Integer>();

    public Map<Integer, Integer> predTable =
	new HashMap<Integer, Integer>();

    public Map<Integer, List<Integer>> succTable =
	new HashMap<Integer, List<Integer>>();

    public String getType(Integer nodeId) {
		return(nodeTypeTable.get(nodeId));
    }

    public void addType(Integer nodeId, String nodeTypeStr) {

		String oldType = nodeTypeTable.put(nodeId, nodeTypeStr);
		if (oldType != null) {
			System.err.println("ERROR: already defined type " +
					oldType +
					" rather than " +
					nodeTypeStr +
					" for node " +
					nodeId);
			System.exit(1);
		}

		return;
    }

    public void changeType(Integer nodeId, String nodeTypeStr) {

		String oldType = nodeTypeTable.put(nodeId, nodeTypeStr);
		if (oldType == null) {
			System.err.println("ERROR: missing type for node " +
					nodeId);
			System.exit(1);
		}

		return;
	}

    public String getTkLiteral(Integer nodeId) {
		return(tkLiteralTable.get(nodeId));
    }

    public String getTkLiteral2(Integer nodeId) {

		String curStr = tkLiteralTable.get(nodeId);
		if (curStr == null) {
			System.err.println("ERROR: missing literal for node " +
					nodeId);
			System.exit(1);
		}

		return(curStr);
    }

    public String getTkImage(Integer nodeId) {
		return(tkImageTable.get(nodeId));
    }

    public String getTkImage2(Integer nodeId) {

		String curStr = tkImageTable.get(nodeId);
		if (curStr == null) {
			System.err.println("ERROR: missing tk image for node " +
					nodeId);
			System.exit(1);
		}

		return(curStr);
    }
    
    public Integer getTkType(Integer nodeId) {
		return(tkTypeTable.get(nodeId));
    }

    public Integer getTkType2(Integer nodeId) {

		Integer curTkType= tkTypeTable.get(nodeId);
		if (curTkType == null) {
			System.err.println("ERROR: missing tk type for node " +
					nodeId);
			System.exit(1);
		}

		return(curTkType);
    }

    public List<Integer> getSuccArr(Integer nodeId) {

		List<Integer> curArr = succTable.get(nodeId);
		if (curArr == null) {
			System.err.println("ERROR: missing successors for node " +
					nodeId);
			System.exit(1);
		}

		return(curArr);
    }

    public List<Integer> getSuccArr(Integer nodeId, int curCard) {

		List<Integer> curArr = succTable.get(nodeId);
		if (curArr == null) {
			System.err.println("ERROR: missing successors for node " +
					nodeId);
			System.exit(1);
		}

		if (curArr.size() != curCard) {
			System.err.println("ERROR: invalid succ size " +
					curArr.size() +
					" for add node " +
					nodeId +
					" (" +
					curCard +
					" expected)");
			System.exit(1);
		}

		return(curArr);
    }

    public Integer getSucc(Integer nodeId,
			   int index,
			   int curCard) {

		List<Integer> curArr = succTable.get(nodeId);
		if (curArr == null) {
			System.err.println("ERROR: missing successors for node " +
					nodeId);
			System.exit(1);
		}

		if (curArr.size() != curCard) {
			System.err.println("ERROR: invalid succ size " +
					curArr.size() +
					" for add node " +
					nodeId +
					" (" +
					curCard +
					" expected)");
			System.exit(1);
		}

		Integer curNode = curArr.get(index - 1);
		if (curNode == null) {
			System.err.println("ERROR: missing succ " +
					index +
					" for node " +
					nodeId);
			System.exit(1);
		}

		return(curNode);
    }

    public Integer getSucc(ArrayList<Integer> curArr,
			   int index) {

		if (curArr == null) {
			System.err.println("ERROR: invalid null successors");
			System.exit(1);
		}

		if ((index <= 0) || (index > curArr.size())) {
			System.err.println("ERROR: index " +
					index + 
					" is out of range [1, " +
					curArr.size() +
					"]");
			System.exit(1);
		}

		Integer curNode = curArr.get(index - 1);
		if (curNode == null) {
			System.err.println("ERROR: missing succ at index " +
					index);
			System.exit(1);
		}

		return(curNode);
    }



    public void readLine_ast_edge(ArrayList<String> lineArr,
				  Integer lineNo) {

		int iVal = UNDEF_VAL;

		if (lineArr.size() != 3) {
			System.err.println("ERROR: inconsistent length " +
					lineArr.size() +
					" for ast edge " +
					lineArr +
					"(3 expected)");
			System.exit(1);
		}

			try {
				iVal = Integer.valueOf(lineArr.get(1));
			} catch (NumberFormatException e) {
				System.err.println(e.getMessage());
				e.printStackTrace();
				System.err.println("ERROR: invalid parent integer " +
								lineArr.get(1));
				System.exit(1);
			}
		Integer parentNodeId = iVal;

			try {
				iVal = Integer.valueOf(lineArr.get(2));
			} catch (NumberFormatException e) {
				System.err.println(e.getMessage());
				e.printStackTrace();
				System.err.println("ERROR: invalid child integer " +
								lineArr.get(2));
				System.exit(1);
			}
		Integer childNodeId = iVal;

		if (!succTable.containsKey(parentNodeId)) {
			ArrayList<Integer> auxArr =
			new ArrayList<Integer> ();
			auxArr.add(childNodeId);
			succTable.put(parentNodeId, auxArr);
		} else {
			succTable.get(parentNodeId).add(childNodeId);
		}

		if (!predTable.containsKey(childNodeId)) {

			/*

			// useless since it's a tree

			ArrayList<Integer> auxArr =
			new ArrayList<Integer> ();
			auxArr.add(childNodeId);
			*/


			predTable.put(childNodeId, parentNodeId);
		} else {

			/*

			// no multiple predecessors allowed in a tree
			*/
			
			System.err.println("ERROR: child node " +
					childNodeId +
					" has already parent " +
					predTable.get(childNodeId));
				System.exit(1);
		}

		return;
    }


    public void readLine_line_begin(ArrayList<String> lineArr,
				    Integer lineNo) {

		tableReaderCl.readLine_func_Integer_Integer(lineArr,
								lineNo,
								lineBeginTable);

		return;
    }

    public void readLine_column_begin(ArrayList<String> lineArr,
				      Integer lineNo) {

		tableReaderCl.readLine_func_Integer_Integer(lineArr,
								lineNo,
								columnBeginTable);

		return;
    }

    public void readLine_node_type(ArrayList<String> lineArr,
				   Integer lineNo) {

		tableReaderCl.readLine_func_Integer_String(lineArr,
							lineNo,
							nodeTypeTable);

		return;
    }

    public void readLine_tk_literal(ArrayList<String> lineArr,
				    Integer lineNo) {

		tableReaderCl.readLine_func_Integer_String(lineArr,
							lineNo,
							tkLiteralTable);

		return;
    }

    public void readLine_tk_image(ArrayList<String> lineArr,
				  Integer lineNo) {

		tableReaderCl.readLine_func_Integer_String(lineArr,
							lineNo,
							tkImageTable);

		return;
    }

    public void readLine_tk_type(ArrayList<String> lineArr,
				 Integer lineNo) {

		tableReaderCl.readLine_func_Integer_Integer(lineArr,
								lineNo,
								tkTypeTable);

		return;
    }

    public void readLine_attr(ArrayList<String> lineArr,
                              Integer lineNo) {

        //
        // func String String
        //

        if (lineArr.size() != 3) {
            System.err.println("ERROR: invalid line length " +
                               lineArr.size() +
                               " at line " +
                               lineNo +
                               " (3 expected) for line: " +
                               lineArr);
            System.exit(1);
        }

        if (attrTable.containsKey(lineArr.get(1))) {
            System.out.println("ERROR: duplicate key at line " +
                               lineNo +
                               ": " +
                               lineArr);
            System.exit(1);
        }

        if (globalOptionsCl.test("printTableReaderTests", "true")) {
            System.out.println("READ: " +
                               lineArr.get(1) +
                               " " +
                               lineArr.get(2));
        }

        attrTable.put(lineArr.get(1), lineArr.get(2));

        return;
    }

  
    public void print() {

		System.out.println("PRINT --->");
		System.out.println();

		System.out.println("ATTRS --->");
		for (String str: sortUtilsCl.getSortedIterable_String(attrTable.keySet())) {

			System.out.println("\t" +
					str +
					" " +
					attrTable.get(str));

		}
		System.out.println("<--- ATTRS");
		System.out.println();


		System.out.println("NODE TYPE --->");
		for (Integer nodeId: sortUtilsCl.getSortedIterable_Integer(nodeTypeTable.keySet())) {

			System.out.println("\t" +
					nodeId +
					" " +
					nodeTypeTable.get(nodeId));

		}
		System.out.println("<--- NODE TYPE");
		System.out.println();


		System.out.println("LINE BEGIN --->");
		for (Integer nodeId: sortUtilsCl.getSortedIterable_Integer(lineBeginTable.keySet())) {

			System.out.println("\t" +
					nodeId +
					" " +
					lineBeginTable.get(nodeId));

		}
		System.out.println("<--- LINE BEGIN");
		System.out.println();


		System.out.println("LINE END --->");
		for (Integer nodeId: sortUtilsCl.getSortedIterable_Integer(lineEndTable.keySet())) {

			System.out.println("\t" +
					nodeId +
					" " +
					lineEndTable.get(nodeId));

		}
		System.out.println("<--- LINE END");
		System.out.println();


		System.out.println("COLUMN BEGIN --->");
		for (Integer nodeId: sortUtilsCl.getSortedIterable_Integer(columnBeginTable.keySet())) {

			System.out.println("\t" +
					nodeId +
					" " +
					columnBeginTable.get(nodeId));

		}
		System.out.println("<--- COLUMN BEGIN");
		System.out.println();


		System.out.println("COLUMN END --->");
		for (Integer nodeId: sortUtilsCl.getSortedIterable_Integer(columnEndTable.keySet())) {

			System.out.println("\t" +
					nodeId +
					" " +
					columnEndTable.get(nodeId));

		}
		System.out.println("<--- COLUMN END");
		System.out.println();


		System.out.println("AST SUCC --->");
		for (Integer parentId: sortUtilsCl.getSortedIterable_Integer(succTable.keySet())) {

			for (Integer childId: sortUtilsCl.getSortedIterable_Integer(succTable.get(parentId))) {

			System.out.println("\tSUCC: " +
					parentId +
					" " +
					childId);

			}

		}
		System.out.println("<--- AST SUCC");
		System.out.println();

		System.out.println("PRED --->");
		for (Integer childId: sortUtilsCl.getSortedIterable_Integer(predTable.keySet())) {

			System.out.println("\tPRED: " +
					childId +
					" " +
					predTable.get(childId));
		}
		System.out.println("<--- PRED");
		System.out.println();

		System.out.println("LITERAL --->");
		for (Integer nodeId: sortUtilsCl.getSortedIterable_Integer(tkLiteralTable.keySet())) {

			System.out.println("\t" +
					nodeId +
					" " +
					tkLiteralTable.get(nodeId));

		}
		System.out.println("<--- LITERAL");
		System.out.println();


		System.out.println("<--- PRINT");
		System.out.println();
		return;
    }
}
