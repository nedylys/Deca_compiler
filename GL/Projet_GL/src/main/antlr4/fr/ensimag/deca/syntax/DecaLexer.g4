lexer grammar DecaLexer;

options {
   language=Java;
   // Tell ANTLR to make the generated lexer class extend the
   // the named class, which is where any supporting code and
   // variables will be placed.
   superClass = AbstractDecaLexer ;
}

@members {
}

// Deca lexer rules.
OBRACE : '{' ;
CBRACE : '}' ;
OPARENT : '(';
CPARENT : ')';
PRINTLN : 'println' ;
PRINTX : 'printx';
PRINTLNX : 'printlnx';
PRINT : 'print';
RETURN : 'return';
WHILE : 'while';
ASM : 'asm';
CLASS :'class';
PROTECTED : 'protected';
EXTENDS : 'extends';
INSTANCEOF : 'instanceof';
NEW : 'new';
READFLOAT : 'readFloat';
READINT : 'readInt';

fragment STRING_CAR : ~["\\\n] ;
fragment LETTER : ('a' .. 'z') | ('A'.. 'Z');
fragment FILE_NAME : (LETTER | DIGIT | '-' | '_')+;
STRING : '"' (STRING_CAR | '\\"' | '\\\\')* '"';

fragment DIGIT : '0' .. '9';
fragment POSITIVE_DIGIT : '1' ..  '9';
INT : '0' | POSITIVE_DIGIT DIGIT*;
fragment NUM : DIGIT+;
fragment SIGN : '+' | '-' ; 
fragment EXP : ('E' | 'e') (NUM | SIGN NUM );
fragment DEC : NUM '.' NUM;
fragment FLOATDEC : (DEC | DEC EXP) ('F' | 'f')?;
fragment DIGITHEX : ('0' .. '9') | ('A' .. 'F') | ('a' .. 'f');
fragment NUMHEX : DIGITHEX+;
fragment FLOATHEX :  ('0x' | '0X') NUMHEX '.' NUMHEX ('P' | 'p') SIGN NUM ('F' | 'f')? ;
FLOAT : FLOATHEX | FLOATDEC;

MULTI_LINE_STRING : '"' (STRING_CAR | '\\"' | '\\\\' | '\n')* '"';
SEMI : ';' ;
LINE_COMMENT : '//' (~[\n])* { skip(); };
BLOCK_COMMENT : '/*' .*? '*/' {skip();};
RETOUR_A_LIGNE : '\n' {skip();};
RETOUR_CHARIOT : '\r' {skip();};
TABULATION : '\t' {skip();};
ESPACE : ' ' {skip();};
COMMA : ',';
INCLUDE : '#include' (' ')* '"' (FILE_NAME) '.decah' '"' '\n'{doInclude(getText());};
EQUALS : '=';

PLUS : '+' ;
MINUS : '-' ;
TIMES : '*' ;
SLASH : '/' ;
PERCENT : '%';
EQEQ : '==';
NEQ : '!=';
LEQ : '<=';
GEQ : '>=';
GT : '>';
LT : '<';
EXCLAM : '!';
AND : '&&';
OR : '||';
DOT : '.';


IF : 'if';
ELSE :'else';
TRUE : 'true';
FALSE : 'false';
NULL : 'null';
THIS : 'this';

IDENT : (LETTER | '$' | '_' )(LETTER | DIGIT | '$' | '_')*;
