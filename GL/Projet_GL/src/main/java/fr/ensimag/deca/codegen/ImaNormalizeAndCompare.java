package fr.ensimag.deca.codegen;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImaNormalizeAndCompare {
    
    private static final Pattern FLOAT_TOKEN = Pattern.compile("(?<![\\w.])([+-]?\\d+(?:\\.\\d+)?(?:[eE][+-]?\\d+)?)(?![\\w.])");
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("Normalization requires 2 files as arguments\n Usage : ImaNormalizeAndCompare <file.out> <file.expected>");
            System.exit(1);
        }

        String outContent = new String(Files.readAllBytes(Paths.get(args[0])));
        String expContent = new String(Files.readAllBytes(Paths.get(args[1])));

        String outNorm = normalize(outContent);
        String expNorm = normalize(expContent);

        if (!outNorm.equals(expNorm)) {
            System.err.println("File .out and File .expected are not equal");

            System.err.println("######### ATTENDU (.expected) après Normalisation ##########");
            System.err.println(expNorm);

            System.err.println("############ OUTPUT (.out) après Normalisation ###########");
            System.err.println(outNorm);
            
            System.exit(1); 
        }
            
            System.exit(0);
        
    }

    private static String normalizeLine(String line) {
        Matcher m = FLOAT_TOKEN.matcher(line) ;
        StringBuffer sb = new StringBuffer() ;

        
        while (m.find()) { // a float pattern is found in line 
            String token = m.group() ; // get the first group
            
            // only normalize if a the token represents surely a float
            if (token.contains(".") || token.contains("e") || token.contains("E")) {
                try {
                    BigDecimal bd = new BigDecimal(token) ;
                    // remove 0's at the end and avoid scientific notation
                    String norm = bd.stripTrailingZeros().toPlainString() ;

                    // verify that it still looks like a float 
                    if (!norm.contains(".")) {
                        norm = norm + ".0" ;
                    }
                    m.appendReplacement(sb, Matcher.quoteReplacement(norm)) ;
                }

                catch (NumberFormatException ex) {
                    // keep original token if an error happened
                    m.appendReplacement(sb, Matcher.quoteReplacement(token)) ;
                }
            }

            // integers remain unchanged
            else {
                m.appendReplacement(sb, Matcher.quoteReplacement(token)) ;
            }
        }
        
        m.appendTail(sb) ;          // add here the rest of the line after the last match
        return sb.toString() ;      // normalized line
    }


    private static String normalize(String content){
        StringBuilder normalized = new StringBuilder(); // we use StringBuilder for low cost appends
        for (String line : content.split("\\R")) {
            /* \\R covers \n(Linux, Mac,..) \r(Mac)  \r\n (Windows) etc... donc n'importe quel OS peut l'utiliser */
            String lineWithNoSpace = line.trim() ; // remove spaces from the beginning and end of the line
            normalized.append(normalizeLine(lineWithNoSpace)).append("\n") ;
        }

        return normalized.toString().trim() ; // Delete the \n at the end of the file
    }


}
