all: buzzUnparsePrettyPrintMain buzzParseTableCl buzzUnparsePrettyPrintVisCl \
buzzParseTreeTraceInjectionMain buzzParseTreeTraceInjectionVisCl buzzParseTreeTraceInjectionPrettyPrintMain \
buzzParseTreePrettyPrintMain buzzParseTreePrettyPrintVisCl

buzzUnparsePrettyPrintMain:
	javac -Xlint -Xdiags:verbose buzzUnparsePrettyPrintMain.java

buzzParseTreeTraceInjectionMain:
	javac -Xlint -Xdiags:verbose buzzParseTreeTraceInjectionMain.java

buzzParseTreeTraceInjectionPrettyPrintMain:
	javac -Xlint -Xdiags:verbose buzzParseTreeTraceInjectionPrettyPrintMain.java

buzzParseTreePrettyPrintMain:
	javac -Xlint -Xdiags:verbose buzzParseTreePrettyPrintMain.java

############################################

buzzParseTableCl:
	javac -Xlint -Xdiags:verbose buzzParseTableCl.java

buzzUnparsePrettyPrintVisCl:
	javac -Xlint -Xdiags:verbose buzzUnparsePrettyPrintVisCl.java

buzzParseTreeTraceInjectionVisCl:
	javac -Xlint -Xdiags:verbose buzzParseTreeTraceInjectionVisCl.java

buzzParseTreePrettyPrintVisCl:
	javac -Xlint -Xdiags:verbose buzzParseTreePrettyPrintVisCl.java

clean:
	rm -f *~ *.class
	cd test; make clean
