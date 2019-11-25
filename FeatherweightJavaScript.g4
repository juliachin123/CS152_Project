grammar FeatherweightJavaScript;


@header { package edu.sjsu.fwjs.parser; }

// Reserved words
IF        : 'if' ;
ELSE      : 'else' ;
WHILE     : 'while' ;
FUNCTION  : 'function' ;
VAR       : 'var' ;
PRINT     : 'print' ;

// Literals
INT       : [1-9][0-9]* | '0' ;
BOOL      : 'true' | 'false' ;
NULL      : 'null' ;

// Symbols
MUL       : '*' ;
DIV       : '/' ;
ADD       : '+' ;
SUB       : '-' ;
MOD       : '%' ;
GT        : '>' ;
LT        : '<' ;
GEQ       : '>=' ;
LEQ       : '<=' ;
EQ        : '==' ;
SEPARATOR : ';' ;
ASSIGN    : '=' ;

//  identifier
IDENTIFIER: [a-zA-Z_][a-zA-Z0-9_]* ;

// Whitespace and comments
NEWLINE   : '\r'? '\n' -> skip ;
LINE_COMMENT  : '//' ~[\n\r]* -> skip ;
BLOCK_COMMENT : '/*' .*? '*/' -> skip ;
WS            : [ \t]+ -> skip ; // ignore whitespace


// ***Paring rules ***

/** The start rule */
prog: stat+ ;

stat: expr SEPARATOR                                    # bareExpr
    | IF '(' expr ')' block ELSE block                  # ifThenElse
    | IF '(' expr ')' block                             # ifThen
    | WHILE '(' expr ')' block                          # while
    | PRINT '(' expr ')' SEPARATOR                      # print
    | SEPARATOR                                         # empty
    ;

expr: expr op=( MUL | DIV | MOD ) expr                  # MulDivMod
    | expr op=( ADD | SUB ) expr                        # AddSub
    | expr op=( LT | LEQ | GT | GEQ | EQ ) expr         # Comparator
    | FUNCTION params block                             # FunctionDeclaration
    | expr args                                         # FunctionApplication
    | VAR IDENTIFIER ASSIGN expr                        # VariableDeclaration
    | IDENTIFIER                                        # Identifier
    | IDENTIFIER ASSIGN expr                            # AssignmentStatement
    | BOOL                                              # bool
    | NULL                                              # null
    | INT                                               # int
    | '(' expr ')'                                      # parens
    ;

block: '{' stat* '}'                                    # fullBlock
     | stat                                             # simpBlock
     ;

params: '(' (IDENTIFIER (',' IDENTIFIER)* )? ')' ;
args: '(' (expr (',' expr)* )? ')' ;