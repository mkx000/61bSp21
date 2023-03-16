package gitlet;

// TODO: any imports you need here

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date; // TODO: You'll likely use this in this class
import java.util.List;
import java.util.TreeMap;

import static gitlet.Utils.*;

/**
 * Represents a gitlet commit object.
 * TODO: It's a good idea to give a description here of what else this Class
 * does at a high level.
 *
 * @author TODO
 */
public class Commit implements Serializable {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /**
     * The message of this Commit.
     */
    private String message;
    private Date date;
    public transient Commit parent1;
    public transient Commit parent2;

    //working treeµÄ¿ìÕÕ
    public Index index;

    public Commit(String msg, Date date) {
        this.message = msg;
        this.date = new Date();
        index = new Index();
    }

    public Commit(String msg, Date date, Commit parent1) {
        this.message = msg;
        this.date = date;
        this.parent1 = parent1;
        this.parent1 = Repository.getHeadCommit();
        index = new Index(parent1);
    }

    public Commit(String msg, Date date, Commit parent1, Commit parent2) {
        this.message = msg;
        this.date = date;
        this.parent1 = parent1;
        this.parent2 = parent2;
    }

    public String saveCommit() {
        String sha1Value = sha1(this);
        File file = join(Repository.COMMITS_DIR, sha1Value);
        try {
            file.createNewFile();
        } catch (IOException e) {
            throw error("can't create a new file in saveCommit method");
        }
        writeObject(file, this);
        return sha1Value;
    }

    public String experiment() {
        String sha1Value = sha1(this);
        System.out.println(sha1Value);

        File file = join(System.getProperty("user.dir"), sha1Value);
        try {
            file.createNewFile();
        } catch (Exception e) {
            System.out.println();
        }
        writeObject(file, this);
        Commit commit = readObject(file, Commit.class);
        System.out.println(commit.date);
        return "";
    }
}
