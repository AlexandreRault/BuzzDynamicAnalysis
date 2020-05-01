import java.util.Iterator;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.ArrayList;

import java.io.File;
import java.io.IOException;

/**
  Copyright (C) 2017, 2018, 2019 Ettore Merlo - All rights reserved
  Copyright (C) 2020 Alexandre Rault - All rights reserved
*/

public class buzzParseTreePrettyPrintMain {

    public static void main(String args[]) {

	buzzParseTableCl astTable = new buzzParseTableCl();
	tableReaderCl tableReader = new tableReaderCl();
	
	tableReader.initParams(astTable);

	if (args.length != 1) {
	    System.err.println("ERROR: missing filename");
	    System.exit(1);
	}

	//String tableStr = "tripleTable.tab";
	String tableStr = args[0];

	//
	// read table
	//

	tableReader.read(tableStr);

	//
	// get AST root
	//

	Integer rootNodeId = null;
	
	if (!astTable.attrTable.containsKey("ast_root")) {
	    System.out.println("ERROR: undefined attribute \"ast_root\"");
	    System.exit(1);
	}
        try {
            rootNodeId = Integer.valueOf(astTable.attrTable.get("ast_root"));
        } catch (NumberFormatException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            System.err.println("ERROR: invalid parent integer " +
                               astTable.attrTable.get("ast_root"));
            System.exit(1);
        }

	buzzParseTreePrettyPrintVisCl buzzParseTreePrettyPrintVis =
	    new buzzParseTreePrettyPrintVisCl();
	buzzParseTreePrettyPrintVis.initParams(astTable);
	buzzParseTreePrettyPrintVis.visit(rootNodeId, 0);

	System.out.println("Successful termination");
	System.exit(0); 
    }

}
