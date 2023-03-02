package gitlet;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;

import static gitlet.Utils.*;

// TODO: any imports you need here

/**
 * Represents a gitlet repository.
 * TODO: It's a good idea to give a description here of what else this Class
 * does at a high level.
 *
 * @author TODO
 */
public class Repository {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /**
     * The current working directory.
     */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /**
     * The .gitlet directory.
     */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    public static final File HEAD = join(CWD, "HEAD");


    public static final File COMMITS_DIR = join(GITLET_DIR, "commits");
    public static final File STAGE_DIR = join(GITLET_DIR, "stage");
    public static final File TRACKED_DIR = join(GITLET_DIR, "tracked");
    public static final File BLOBS_DIR = join(GITLET_DIR, "blobs");

    private static HashSet<String> removal = null;

    public static void initRepo() {
        if (GITLET_DIR.exists()) {
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            System.exit(0);
        }
        try {
            removal = new HashSet<String>();
            GITLET_DIR.mkdir();
            STAGE_DIR.mkdir();
            COMMITS_DIR.mkdir();
            TRACKED_DIR.mkdir();
            BLOBS_DIR.mkdir();
        } catch (Exception e) {
            throw new GitletException("can't create .gitlet or stage or commits directory");
        }
        Commit initialCommit = new Commit("initial commit");
    }

    public static void add(String fileName) {
        File file = join(CWD, fileName);
        if (!file.exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        }
        String str = sha1(file);
        /**
         *  last commit 有这个文件
         */
        if (true) {

        } else {
            Utils.writeObject(join(STAGE_DIR, fileName), file);
        }
    }

    public static void commit() {
        String file = Utils.plainFilenamesIn(STAGE_DIR).get(0);

    }
}
