package gitlet;

// TODO: any imports you need here

import java.io.Serializable;
import java.util.Date; // TODO: You'll likely use this in this class
import java.util.TreeMap;

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Commit implements Serializable {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    private String message;
    private Date date;
    private transient Commit parent1 = null;
    private transient Commit parent2 = null;

    TreeMap<String, String> mapping;

    public Commit(String msg){
        message = msg;
        date = new Date();
    }


//    public static void main(String[] args) {
//        System.out.println(new Date(0));
//    }
}
