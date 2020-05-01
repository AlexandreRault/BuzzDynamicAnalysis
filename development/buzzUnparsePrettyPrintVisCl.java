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
*/

public class buzzUnparsePrettyPrintVisCl implements defsInt {

	buzzParseTableCl astTable = null;
	
    private int curColumn = 0;
	private int curLine = 1;

	private int curTab = 0;
	final private int TAB_SIZE = 4;
    
    public void initParams(buzzParseTableCl parAstTable) {

	    astTable = parAstTable;

	    return;
	}

	//
	// Printing methods
	//

	private void printString(String str) {
		if (curColumn == 0) {
			for(int i = 0; i < curTab * TAB_SIZE; i++) {
				System.out.print(" ");
				curColumn += 1;
			}
		}
		System.out.print(str);
		curColumn += str.length();
	}

	private void newLine(){
		curLine += 1;
		curColumn = 0;
		System.out.println();
	}

	//
	// AST utils
	//

    private void visitChildren(Integer astNodeId, int depth) {

	    List<Integer> children = astTable.succTable.get(astNodeId);
	    if (children != null) {
	        for (Integer childId: children) {
		        visit(childId, depth);
	        }
	    }

	    return;
    }
	
    //
    // Node visitors
	//
	
	private void visit_GENERIC(Integer astNodeId, int depth) {
	    
	    visitChildren(astNodeId, (depth + 1));

	    return;
    }

    private void visit_token(Integer astNodeId, int depth) {

	    String curLiteral = astTable.getTkLiteral(astNodeId);
		String curImg = astTable.getTkImage(astNodeId);
	    
	    int curNodeLineBeg = astTable.lineBeginTable.get(astNodeId);
	    int curNodeColumnBeg = astTable.columnBeginTable.get(astNodeId);
	    
	    if (curLiteral != null && !curLiteral.equals("UNDEFINED")) {
	    	printString(curLiteral);
	    } else {
	        if (!curImg.equals("UNDEFINED")) {
		        printString(curImg);
	        }
	    }
	    
	    visitChildren(astNodeId, (depth + 1));

	    return;
	}

	private void visit_BLOCK(Integer astNodeId, int depth) {

		// BLOCK
        //   token <{>
		//   STATLIST
		// 	 token <}>

		List<Integer> children = astTable.succTable.get(astNodeId);
		
		if (children != null && children.size() > 1) {
			for (int i = 0; i < children.size() - 1; i++) {
				if(i == 1) {
					newLine();
					curTab += 1;
				}
				visit(children.get(i), depth + 1);
			}
			curTab -= 1;
			visit(children.get(children.size() - 1), depth + 1);
			printString(" ");
			// newLine();
			
		} else {
			visitChildren(astNodeId, depth + 1);
		}

		return;
	}

	private void visit_BLOCKSTAT(Integer astNodeId, int depth) {

		// BLOCKSTAT
		//   STAT
		
		// BLOCKSTAT
		//   BLOCK

		List<Integer> children = astTable.succTable.get(astNodeId);

		if (children != null && children.size() == 1 && astTable.getType(children.get(0)).equals("STAT")) {
			newLine();
			curTab += 1;
			visit(children.get(0), depth + 1);
			curTab -= 1;
		} else {
			visitChildren(astNodeId, (depth + 1));
		}


    	return;
	}

    private void visit_CALL(Integer astNodeId, int depth) {

		printString("(");

		visitChildren(astNodeId, (depth + 1));

    	return;
	}

    private void visit_COMMAND(Integer astNodeId, int depth) {
	    
	    List<Integer> children = astTable.succTable.get(astNodeId);
	    if (children.size() != 2) {
	        visitChildren(astNodeId, (depth + 1));
	    } else {
			visit(children.get(0), depth + 1);
			
			printString(" = ");

			visit(children.get(1), depth + 1);

	    }

		printString(";");
		// newLine();

	    return;
	}
	
	private void visit_COMPARISON(Integer astNodeId, int depth) {

		// COMPARISON
		//   EXPRESSION
		//   REL_OP
		//   token <!=>
		//   EXPRESSION

		List<Integer> children = astTable.succTable.get(astNodeId);
		
		if (children != null && children.size() > 1) {
			for (int i = 0; i < children.size(); i++) {
				if (i == 2 || i == 3) {
					printString(" ");
				}
				visit(children.get(i), depth + 1);
			}
		} else {
			visitChildren(astNodeId, (depth + 1));
		}

		return;
	}
    
    private void visit_EQUATION(Integer astNodeId, int depth) {
		// EXPRESSION || PRODUCT || MODULO || CONDITION
		//   PRODUCT
		//   PRODUCT
		//   BINARY_ARITHM_OP
		//   token <+>
		//  [PRODUCT
		//	 BINARY_ARITHM_OP
		//   token <+>]*
	    
	    List<Integer> children = astTable.succTable.get(astNodeId);
	    if (children.size() >= 4) {
			visit(children.get(0), depth + 1);
			printString(" ");
	        visit(children.get(3), depth + 1);
			printString(" ");
	        visit(children.get(1), depth + 1);

	        for	(int i = 4; i < children.size(); i += 3) {
				printString(" ");
				visit(children.get(i + 2), depth + 1);
				printString(" ");
				visit(children.get(i), depth + 1);
			}

		} else if (children.size() == 3 && astTable.getType(children.get(1)).equals("UNARY_LOGIC_OP")){
			visit(children.get(2), depth + 1);
			printString(" ");
			visit(children.get(0), depth + 1);
	    } else {
	        visitChildren(astNodeId, depth + 1);
	    }
	        
	    return;
	}
	
	private void visit_FUNCTION(Integer astNodeId, int depth) {

		// FUNCTION
        //   token <function>
        //   token <check_rc_wp> <identifier>
        //   token <(>
        //   FORMAL_PARAMETER_LIST 
        //   token <)>
		//   BLOCK
		
		List<Integer> children = astTable.succTable.get(astNodeId);
		if (children != null) {
			for (int i = 0; i < children.size(); i++) {
				if (i == 1 || i == 5) {
					printString(" ");
				}
				visit(children.get(i), depth + 1);
			}
		} else {
			visitChildren(astNodeId, (depth + 1));
		}

	    return;
    }

    private void visit_IDREF(Integer astNodeId, int depth) {
		List<Integer> children = astTable.succTable.get(astNodeId);
		if (children != null) {
			boolean previousIsToken = false;
			for (int i = 0; i < children.size(); i++) {
				if (astTable.getType(children.get(i)).equals("token")) {
					if (!previousIsToken){
						previousIsToken = true;
					} else {
						printString(".");
					}
				} else {
					previousIsToken = false;
				}
				if (astTable.getType(children.get(i)).equals("EXPRESSION")) {
					printString("[");
				}
				visit(children.get(i), depth + 1);
			}
		}
		return;
	}

	private void visit_IF(Integer astNodeId, int depth) {
		// IF
		//   token <if>
		//   token <(>
		//   CONDITION
		//   token <)>
		//   BLOCKSTAT
		//   ...

		List<Integer> children = astTable.succTable.get(astNodeId);

		int i = 0;
		while(!astTable.getType(children.get(i)).equals("BLOCKSTAT")) {
			if (i == 1) {
				printString(" ");
			}
			visit(children.get(i), depth + 1);
			i++;
		}
		printString(" ");
		visit(children.get(i), depth + 1);
		i++;
		for(; i < children.size(); i++) {
			printString("else ");
			visit(children.get(i), depth + 1);
		}

		return;
	}

	private void visit_OPERAND(Integer astNodeId, int depth) {

		List<Integer> children = astTable.succTable.get(astNodeId);
		if(children.size() == 2
				&& astTable.getType(children.get(1)).equals("token")
				&& astTable.getTkImage(children.get(1)).equals(")")) {
			printString("(");
		}

		visitChildren(astNodeId, (depth + 1));

		return;

	}
    
    private void visit_PARAMETER_LIST(Integer astNodeId, int depth) {
	    
	    List<Integer> children = astTable.succTable.get(astNodeId);
	    if (children != null) {
	        visit(children.get(0), depth + 1);
	        for (int i = 1; i < children.size(); i++) {
	            printString(", ");
		        visit(children.get(i), depth + 1);
	        }
	    }

	    return;
	}

	private void visit_LAMBDA(Integer astNodeId, int depth) {
		
		// LAMBDA
		//   token <function>
		//   token <(>
        //   FORMAL_PARAMETER_LIST 
        //   token <)>
		//   BLOCK

	    List<Integer> children = astTable.succTable.get(astNodeId);
	    if (children != null && children.size() > 0) {
	        for (int i = 0; i < children.size(); i++) {
				if (astTable.getType(children.get(i)).equals("BLOCK")) {
					printString(" ");
				}
				visit(children.get(i), depth + 1);
	        }
	    } else {
			visitChildren(astNodeId, depth + 1);
		}

	    return;
	}

    private void visit_LIST(Integer astNodeId, int depth) {
    	printString("{");

		List<Integer> children = astTable.succTable.get(astNodeId);
		if (children != null) {
			visit(children.get(0), depth + 1);
			int i = 1;
			while (i < children.size() && astTable.getType(children.get(i)).equals("LIST_STATEMENT")) {
				printString(", ");
				visit(children.get(i), depth + 1);
				i++;
			}
			for (; i < children.size(); i++) {
				visit(children.get(i), depth + 1);
			}
		}
		return;

	}


	private void visit_POWERREST(Integer astNodeId, int depth) {
		List<Integer> children = astTable.succTable.get(astNodeId);
		if(children != null && children.size() == 3) {
			visit(children.get(2), depth + 1);
			visit(children.get(0), depth + 1);
		}
		else {
			visitChildren(astNodeId, (depth + 1));
		}

		return;
	}
    
    private void visit_RETURN(Integer astNodeId, int depth) {

		List<Integer> children = astTable.succTable.get(astNodeId);

		if (children != null && children.size() > 0) {
			printString("return ");
			visitChildren(astNodeId, (depth + 1));
			printString(";");
		} else {
			printString("return;");
		}
		// newLine();

	    return;
	}
	
	private void visit_STAT(Integer astNodeId, int depth) {

		List<Integer> children = astTable.succTable.get(astNodeId);
		if (children != null) {// && children.size() > 0){
			for (int i = 0; i < children.size(); i++) {
				visit(children.get(i), depth + 1);
				if (curColumn > 0) {
					newLine();
				}
			}
		} else {
			visitChildren(astNodeId, depth + 1);
		}

	    return;
    }

	private void visit_STATLIST(Integer astNodeId, int depth) {

		List<Integer> children = astTable.succTable.get(astNodeId);
		if (children != null){
			for (int i = 0; i < children.size(); i++) {
				visit(children.get(i), depth + 1);
			}
		}

	    return;
    }

    private void visit_VAR(Integer astNodeId, int depth) {

		// VAR
		//   token <var> <variable>
		//   token <ls> <identifier>
		//   [EXPRESSION]

		List<Integer> children = astTable.succTable.get(astNodeId);
		if (children.size() == 3) {
			visit(children.get(0), depth + 1);
			printString(" ");
			visit(children.get(1), depth + 1);

			printString(" = ");

			visit(children.get(2), depth + 1);

		} else if (children.size() == 2) {
			visit(children.get(0), depth + 1);
			printString(" ");
			visit(children.get(1), depth + 1);
		} else {
			visitChildren(astNodeId, (depth + 1));
		}

		printString(";");
		// newLine();

    	return;
	}

	private void visit_WHILE(Integer astNodeId, int depth) {

		// WHILE
		//   token <while>
		//   token <(>
		//   COMPARISON
		//   token <)>
		//   BLOCK

		List<Integer> children = astTable.succTable.get(astNodeId);
		
		if (children != null && children.size() > 0) {
			for (int i = 0; i < children.size(); i++) {
				if (i == 1|| i == 4) {
					printString(" ");
				}
				visit(children.get(i), depth + 1);
			}
		} else {
			visitChildren(astNodeId, depth + 1);
		}

    	return;
	}

    public Integer visit(Integer astNodeId, int depth) {

	    Integer retVal = null;

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
			case "CONDITION_LIST":
			case "IDLIST":
			case "LIST_STATEMENT":
			case "PARSE":
			case "POWER":
			case "REL_OP":
			case "SCRIPT":
			case "UNARY_ARITHM_OP":
			case "UNARY_LOGIC_OP":
				visit_GENERIC(astNodeId, depth);
				break;
				
			case "BLOCK":
				visit_BLOCK(astNodeId, depth);
				break;
			
			case "BLOCKSTAT":
				visit_BLOCKSTAT(astNodeId, depth);
				break;

			case "CALL":
				visit_CALL(astNodeId, depth);
				break;

			case "COMMAND":
				visit_COMMAND(astNodeId, depth);
				break;
				
			case "COMPARISON":
				visit_COMPARISON(astNodeId, depth);
				break;

			case "CONDITION":
			case "EXPRESSION":
			case "MODULO":
			case "PRODUCT":
				visit_EQUATION(astNodeId, depth);
				break;

			case "FUNCTION":
				visit_FUNCTION(astNodeId, depth);
				break;

			case "IDREF":
				visit_IDREF(astNodeId, depth);
				break;

			case "IF":
				visit_IF(astNodeId, depth);
				break;

			case "ACTUAL_PARAMETER_LIST":
			case "FORMAL_PARAMETER_LIST":
				visit_PARAMETER_LIST(astNodeId, depth);
				break;
			
			case "LAMBDA":
				visit_LAMBDA(astNodeId, depth);
				break;

			case "LIST":
				visit_LIST(astNodeId, depth);
				break;

			case "OPERAND":
				visit_OPERAND(astNodeId, depth);
				break;
				
			case "POWERREST":
				visit_POWERREST(astNodeId, depth);
				break;
			
			case "RETURN":
				visit_RETURN(astNodeId, depth);
				break;  
				
			case "STAT":
				visit_STAT(astNodeId, depth);
				break;
			
			case "STATLIST":
				visit_STATLIST(astNodeId, depth);
				break;  

			case "token":
				visit_token(astNodeId, depth);
				break;

			case "VAR":
				visit_VAR(astNodeId, depth);
				break;

			case "WHILE":
				visit_WHILE(astNodeId, depth);
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
