package fr.ensimag.ima.pseudocode;
import java.io.PrintStream;

// Cette classe est utilisée pour les méthodes ASM
// Elle print directement le contenu dans le fichier .ass
// Je ne sais pas si il y a une meilleur implémentation en Java à faire.

public class AsmInstruction extends Instruction{

    private String litteralString;

    public AsmInstruction(String litteralString){
        this.litteralString = litteralString;
    }

    @Override
    void display(PrintStream s){
        String str = litteralString;
        if (str.length() >= 2 && str.charAt(0) == '"' && str.charAt(str.length() - 1) == '"') {
            str = str.substring(1, str.length() - 1); // Verfier que les guillements ont été
                                                     // enleves 
        }
        if (str.startsWith("WSTR \\\"") && str.endsWith("\\\"")) {
        str = "WSTR \"" + str.substring(7, str.length() - 2) + "\""; // On gere le cas du str
        } 
       s.print(str);
    }

    void displayOperands(PrintStream s){
        
    }

}