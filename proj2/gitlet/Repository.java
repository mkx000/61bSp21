package gitlet;

import java.io.File;
import java.util.*;

import static gitlet.Utils.*;

// TODO: any imports you need here

/**
 * Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
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
    // commitID -> commit
    public static final File OBJECTS_DIR = join(GITLET_DIR, "objects");
    public static final File COMMITS_DIR = join(OBJECTS_DIR, "commits");
    public static final File BLOBS_DIR = join(OBJECTS_DIR, "blobs");
    public static final File BRANCHES_DIR = join(GITLET_DIR, "branches");
    public static final File STAGED_DIR = join(GITLET_DIR, "staged");
    public static final File HEAD = join(GITLET_DIR, "HEAD");
    // commitID -> commit
    public static final File ADDITIONS = join(GITLET_DIR, "additions");
    public static final File REMOVALS = join(GITLET_DIR, "removals");
    public static final File TREE = join(GITLET_DIR, "tree");

    public static void init() {
        if (GITLET_DIR.exists()) {
            Utils.exitWithMessage("A Gitlet version-control system "
                    + "already exists in the current directory.");
        }
        GITLET_DIR.mkdir();
        OBJECTS_DIR.mkdir();
        COMMITS_DIR.mkdir();
        BLOBS_DIR.mkdir();
        BRANCHES_DIR.mkdir();
        STAGED_DIR.mkdir();
        writeObject(ADDITIONS, new HashSet<>());
        writeObject(REMOVALS, new HashSet<>());
        writeObject(TREE, new HashMap<String, String>());
        Commit initialCommit = new Commit("initial commit", new Date(0), null);
        String sha1 = initialCommit.save();
        writeContents(HEAD, "master");
        writeContents(join(BRANCHES_DIR, "master"), sha1);
    }

    private static Commit getHeadCommit() {
        String branch = readContentsAsString(HEAD);
        String headCommitID = readContentsAsString(join(BRANCHES_DIR, branch));
        return Commit.read(headCommitID);
    }

    private static HashSet<String> getAdditions() {
        return (HashSet<String>) readObject(ADDITIONS, HashSet.class);
    }

    private static HashSet<String> getRemovals() {
        return (HashSet<String>) readObject(REMOVALS, HashSet.class);
    }

    private static HashMap<String, String> getTree() {
        return (HashMap<String, String>) readObject(TREE, HashMap.class);
    }

    private static void clearStagingArea() {
        for (File file : STAGED_DIR.listFiles()) {
            file.delete();
        }
        writeObject(ADDITIONS, new HashSet<>());
        writeObject(REMOVALS, new HashSet<>());
        writeObject(TREE, new HashMap<String, String>());
    }

    public static void add(String fileName) {
        File fileToAdd = join(CWD, fileName);
        if (!fileToAdd.exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        }
        String content = readContentsAsString(fileToAdd);
        Blob blob = new Blob(content);
        Commit headCommit = getHeadCommit();
        // If the file is identical to the version in the current commit,
        // remove it from the staging area if it is already there
        HashSet<String> additions = getAdditions();
        HashMap<String, String> tree = getTree();
        String blobID = headCommit.blobID(fileName);
        if (blobID != null && blobID.equals(blob.getSha1())) {
            if (additions.contains(fileName)) {
                additions.remove(fileName);
                writeObject(ADDITIONS, additions);
            }
        } else {
            blob.stage();
            tree.put(fileName, blob.getSha1());
            writeObject(TREE, tree);
            additions.add(fileName);
            writeObject(ADDITIONS, additions);
        }
    }

    public static void commit(String msg) {
        if (msg == null || msg.isEmpty()) {
            System.out.println("Please enter a commit message.");
            System.exit(0);
        }
        HashSet<String> additions = getAdditions();
        HashSet<String> removals = getRemovals();
        if (additions.isEmpty() && removals.isEmpty()) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }
        HashMap<String, String> tree = getTree();
        Commit newCommit = new Commit(msg, new Date(), getHeadCommit());
        for (String fileName : additions) {
            String sha1 = tree.get(fileName);
            newCommit.add(fileName, sha1);
            Blob blob = Blob.readFromStage(sha1);
            blob.commit();
        }
        for (String fileName : removals) {
            newCommit.remove(fileName);
        }
        String sha1 = newCommit.save();
        writeContents(join(BRANCHES_DIR, readContentsAsString(HEAD)), sha1);
        clearStagingArea();
    }

    public static void rm(String filename) {
        Commit headCommit = getHeadCommit();
        HashSet<String> trackedFiles = headCommit.getTrackedFiles();
        HashSet<String> additions = getAdditions();
        HashSet<String> removals = getRemovals();
        if (!trackedFiles.contains(filename) && !additions.contains(filename)) {
            Utils.exitWithMessage("No reason to remove the file.");
        }
        if (trackedFiles.contains(filename)) {
            removals.add(filename);
            writeObject(REMOVALS, removals);
            File fileToRemove = join(CWD, filename);
            fileToRemove.delete();
        }
        if (additions.contains(filename)) {
            additions.remove(filename);
            writeObject(ADDITIONS, additions);
        }
    }

    public static void log() {
        Commit commit = getHeadCommit();
        while (commit != null) {
            System.out.println(commit);
            commit = commit.getParentCommit();
        }
    }

    public static void globalLog() {
        for (File file : COMMITS_DIR.listFiles()) {
            Commit commit = Commit.read(file.getName());
            System.out.println(commit);
        }
    }

    public static void find(String msg) {
        boolean found = false;
        for (File file : COMMITS_DIR.listFiles()) {
            Commit commit = Commit.read(file.getName());
            if (commit.getMessage().equals(msg)) {
                System.out.println(commit.getSha1());
                found = true;
            }
        }
        if (!found) {
            Utils.exitWithMessage("Found no commit with that message.");
        }
    }

    private static void printHeaders(String section) {
        System.out.println("=== " + section + " ===");
    }

    public static void status() {
        printHeaders("Branches");
        String head = readContentsAsString(HEAD);
        TreeSet<String> branches = new TreeSet<>();
        for (String branchName : branches) {
            if (!branchName.equals(head)) {
                System.out.println(branchName);
            } else {
                System.out.println("*" + branchName);
            }
        }
        System.out.println();
        printHeaders("Staged Files");
        HashSet<String> additions = getAdditions();
        for (String fileName : additions) {
            System.out.println(fileName);
        }
        System.out.println();
        printHeaders("Removed Files");
        HashSet<String> removals = getRemovals();
        for (String fileName : removals) {
            System.out.println(fileName);
        }
        System.out.println();
        printHeaders("Modifications Not Staged For Commit");
        printHeaders("Untracked Files");
    }

    private boolean isTracked(String relativeFileName) {
        HashSet<String> untrackedFiles = getUntrackedFiles();
        String absoluteFileName = join(CWD, relativeFileName).toString();
        return !untrackedFiles.contains(absoluteFileName);
    }

    private static HashSet<String> getUntrackedFiles() {
        HashSet<String> untrackedFiles = new HashSet<>(plainFilenamesIn(CWD));
        HashSet<String> trackedFiles = getHeadCommit().getTrackedFiles();
        HashSet<String> additions = getAdditions();
        for (String file : trackedFiles) {
            if (additions.contains(file) || trackedFiles.contains(file)) {
                untrackedFiles.remove(file);
            }
        }
        return untrackedFiles;
    }

    private static void checkoutFileFromCommit(String commitID, String relativeFileName) {
        File file = join(COMMITS_DIR, commitID);
        if (!file.exists()) {
            Utils.exitWithMessage("No commit with that id exists.");
        }
        Commit commit = Commit.read(commitID);
        String blobID = commit.blobID(relativeFileName);
        if (blobID == null) {
            Utils.exitWithMessage("File does not exist in that commit.");
        }
        Blob blob = Blob.read(blobID);
        String content = blob.getContents();
        // If the file exists, overwrite it. If the file does not exist,
        // create it.
        writeContents(join(CWD, relativeFileName), content);
    }

    private static void checkoutBranch(String branch) {
        File file = join(BRANCHES_DIR, branch);
        if (!file.exists()) {
            Utils.exitWithMessage("No such branch exists.");
        }

        String headBranch = readContentsAsString(HEAD);
        if (branch.equals(headBranch)) {
            Utils.exitWithMessage("No need to checkout the current branch.");
        }

        String commitID = readContentsAsString(file);
        Commit commit = Commit.read(commitID);
        HashSet<String> trackedFiles = commit.getTrackedFiles();
        HashSet<String> untrackedFiles = getUntrackedFiles();
        for (String fileName : trackedFiles) {
            if (untrackedFiles.contains(fileName)) {
                Utils.exitWithMessage("There is an untracked file in the way; delete it or add and commit it first.");
            } else {
                checkoutFileFromCommit(commitID, fileName);
            }
        }
    }

    public static void checkout(String[] args) {
        if (args.length == 2) {
            checkoutBranch(args[1]);
        } else if (args.length == 3) {
            String branch = readContentsAsString(HEAD);
            String headCommitID = readContentsAsString(join(BRANCHES_DIR, branch));
            checkoutFileFromCommit(headCommitID, args[2]);
        } else if (args.length == 4) {
            checkoutFileFromCommit(args[1], args[3]);
        }
    }


}
