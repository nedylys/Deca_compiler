package fr.ensimag.deca.tree;

import java.io.PrintStream;

/**
 * Exception corresponding to an error at a particular location in a file.
 *
 * @author gl43
 * @date 01/01/2026
 */
public class LocationException extends Exception {
    public Location getLocation() {
        return location;
    }

    public void display(PrintStream s) {
        Location loc = getLocation();
        String line;
        String column;
        String file ;
        if (loc == null) {
            line = "<unknown>";
            file = "<unknown>";
            column = "";
        } else {
            file = (loc.getFilename() == null) ? "<unknown>" : loc.getFilename();
            line = Integer.toString(loc.getLine());
            column = ":" + loc.getPositionInLine();
        }
        s.println(file + ":" + line + column + ": " + getMessage());
    }

    private static final long serialVersionUID = 7628400022855935597L;
    protected Location location;

    public LocationException(String message, Location location) {
        super(message);
        assert(location == null || location.getFilename() != null);
        this.location = location;
    }

}
