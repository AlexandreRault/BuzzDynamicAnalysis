#!/bin/bash

#
# Copyright (C) 2017 - 2019 Ettore Merlo - All rights reserved
#

#source ./bzzConfig.tcsh
#source ../javaConfig.tcsh
#setenv CLASSPATH "$CLASSPATH":..

ALALL='FALSE'
#ALALL='TRUE'

visualDir='../tool'

if [ "$1" == '' ]
then
    echo 'ERROR: missing parameter 1 (current program) for parseProg'
    exit 1
    #return 1
fi
curProg=$1

if [ "$2" == '' ]
then
    echo 'ERROR: missing parameter 2 (db directory) for parseProg'
    exit 1
    #return 1
fi
dbDir=$2

if [ "$3" == '' ]
then
    echo 'ERROR: missing parameter 3 (VIS_CFG) for parseProg'
    exit 1
    #return 1
fi
VIS_CFG=$3

#echo 'VIS_CFG: '"$VIS_CFG"


#
# do not copy source: we are in the db source directory
#
#cp data/refSrc/"$curProg".bzz .
    
#bzzparse "$curProg".bzz prog.asm &> "$wDir"/res1.txt
(cd "$dbDir"; bzzparse "$curProg".bzz prog.asm) &> res1.txt
retVal="$?"
if [ "$retVal" != '0' ]
then
    echo "ERRORS during parsing $curProg"
    exit 1
    #return 1
fi

cp "$dbDir"/ast.txt ast.txt
sed 's/^literal /tk_literal /' ast.txt > delme_ast.txt
cp delme_ast.txt "$curProg".parse.tab

java buzzCompactPrettyParseMain \
     "$curProg".parse.tab &> "$curProg".pp.parse.tab
retVal="$?"
if [ "$retVal" != '0' ]
then
    echo "ERRORS during pretty parse print for $curProg"
    exit 1
    #return 1
fi

java buzzAstCompactXtrMain \
     "$curProg".parse.tab "$curProg".ast.tab &> res2.txt
retVal="$?"
if [ "$retVal" != '0' ]
then
    echo "ERRORS during compact extracting AST for $curProg"
    exit 1
    #return 1
fi

cat "$curProg".ast.tab > delmeAst.tab
java buzzAstCompactPrettyPrintMain delmeAst.tab &> res3.rep
retVal="$?"
if [ "$retVal" != '0' ]
then
    echo "ERRORS during pretty printing AST for $curProg"
    exit 1
    #return 1
fi

java buzzCmdCfgXtrMain "$curProg".ast.tab \
     "$curProg".cfg.tab &> res4.txt
retVal="$?"
if [ "$retVal" != '0' ]
then
    echo "ERRORS during extracting command CFG for $curProg"
    exit 1
    #return 1
fi

grep '^TAB: ' res4.txt | sed 's/^TAB: //' > "$curProg".cfg.tab

if [ "$VIS_CFG" == "TRUE" ]
then
    
    #
    # cfg visualization
    #
    
    #python "$visualDir"/visualizeCfg_c.py \
    python "$visualDir"/visualizeCfg.py \
	   "$curProg".cfg.tab "$curProg".cfg.dot &> dot.txt
    retVal="$?"
    if [ "$retVal" != '0' ]
    then
        echo "ERRORS during cfg visualization for $curProg"
	exit 1
        #return 1
    fi
    
    dot -Teps -o "$curProg".cfg.eps "$curProg".cfg.dot
    retVal="$?"
    if [ "$retVal" != '0' ]
    then
        echo "ERRORS during cfg dot rendering for $curProg"
	exit 1
	#return 1
    fi
    
fi

if [ "$ALALL" == 'TRUE' ]
then
    echo "ALIGNMENT REQUIRED"
    #cp res.txt data/res_1.txt
    
    #cp "$curProg".parse.tab data/refSrc/"$curProg".parse.tab
    #cp "$curProg".pp.parse.tab data/refSrc/"$curProg".pp.parse.tab
    #cp "$curProg".ast.tab data/refSrc/"$curProg".ast.tab
    #cp "$curProg".cfg.tab data/refSrc/"$curProg".cfg.tab

    cp "$curProg".parse.tab data/"$curProg".parse.tab
    cp "$curProg".pp.parse.tab data/"$curProg".pp.parse.tab
    cp "$curProg".ast.tab data/"$curProg".ast.tab
    cp "$curProg".cfg.tab data/"$curProg".cfg.tab

fi

#diff res.txt data/res_1.txt

#diff "$curProg".parse.tab data/refSrc/"$curProg".parse.tab
#diff "$curProg".pp.parse.tab data/refSrc/"$curProg".pp.parse.tab
#diff "$curProg".ast.tab data/refSrc/"$curProg".ast.tab
#diff "$curProg".cfg.tab data/refSrc/"$curProg".cfg.tab

diff "$curProg".parse.tab data/"$curProg".parse.tab
diff "$curProg".pp.parse.tab data/"$curProg".pp.parse.tab
diff "$curProg".ast.tab data/"$curProg".ast.tab
diff "$curProg".cfg.tab data/"$curProg".cfg.tab

exit 0
#return 0
