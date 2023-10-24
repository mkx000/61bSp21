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
        HashSet<String> removals = getRemovals();
        HashMap<String, String> tree = getTree();
        String blobID = headCommit.blobID(fileName);
        if (removals.contains(fileName)) {
            removals.remove(fileName);
            writeObject(REMOVALS, removals);
        }
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
            commit = commit.getFirstParentCommit();
        }
    }

    public static void globalLog() {
        for (File file : Objects.requireNonNull(COMMITS_DIR.listFiles())) {
            for (File fileInFile : Objects.requireNonNull(file.listFiles())) {
                Commit commit = Commit.read(file.getName() + fileInFile.getName());
                System.out.println(commit);
            }
        }
    }

    public static void find(String msg) {
        boolean found = false;
        for (File file : Objects.requireNonNull(COMMITS_DIR.listFiles())) {
            for (File fileInFile : Objects.requireNonNull(file.listFiles())) {
                Commit commit = Commit.read(file.getName() + fileInFile.getName());
                if (commit.getMessage().equals(msg)) {
                    System.out.println(commit.getSha1());
                    found = true;
                }
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
        for (File file : Objects.requireNonNull(BRANCHES_DIR.listFiles())) {
            branches.add(file.getName());
        }
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
        System.out.println();
        printHeaders("Untracked Files");
        System.out.println();
    }

    private boolean isTracked(String relativeFileName) {
        HashSet<String> untrackedFiles = getUntrackedFiles();
        String absoluteFileName = join(CWD, relativeFileName).toString();
        return !untrackedFiles.contains(absoluteFileName);
    }

    private static HashSet<String> getUntrackedFiles() {
        HashSet<String> untrackedFiles = new HashSet<>(Objects.requireNonNull(plainFilenamesIn(CWD)));
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
        File file = join(COMMITS_DIR, commitID.substring(0, 2));
        file = join(file, commitID.substring(2));
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
        checkoutFromCommitID(commitID);
        writeContents(HEAD, branch);
    }

    private static void checkoutFromCommitID(String commitID) {
        Commit commit = Commit.read(commitID);
        HashSet<String> trackedFiles = commit.getTrackedFiles();
        HashSet<String> untrackedFiles = getUntrackedFiles();
        HashSet<String> trackedInCurrent = getHeadCommit().getTrackedFiles();
        for (String fileName : trackedFiles) {
            if (untrackedFiles.contains(fileName)) {
                Utils.exitWithMessage("There is an untracked file in the way; delete it or add and commit it first.");
            }
        }
        for (String fileName : trackedFiles) {
            checkoutFileFromCommit(commitID, fileName);
        }
        for (String fileName : trackedInCurrent) {
            if (!trackedFiles.contains(fileName)) {
                File file = join(CWD, fileName);
                Utils.restrictedDelete(file);
            }
        }
    }

    private static String convertShortID(String commitID) {
        if (commitID.length() == 40) {
            return commitID;
        }
        for (File file : Objects.requireNonNull(COMMITS_DIR.listFiles())) {
            for (File fileInFile : Objects.requireNonNull(file.listFiles())) {
                if ((file.getName() + fileInFile.getName()).startsWith(commitID)) {
                    return file.getName() + fileInFile.getName();
                }
            }
        }
        Utils.exitWithMessage("No commit with that id exists.");
        return null;
    }

    public static void checkout(String[] args) {
        if (args.length == 2) {
            checkoutBranch(args[1]);
            clearStagingArea();
        } else if (args.length == 3) {
            String headCommitID = getHeadCommit().getSha1();
            checkoutFileFromCommit(headCommitID, args[2]);
        } else if (args.length == 4) {
            checkoutFileFromCommit(Objects.requireNonNull(convertShortID(args[1])), args[3]);
        }
    }

    public static void branch(String branchName) {
        File file = join(BRANCHES_DIR, branchName);
        if (file.exists()) {
            Utils.exitWithMessage("A branch with that name already exists.");
        }
        String headCommitID = readContentsAsString(join(BRANCHES_DIR, readContentsAsString(HEAD)));
        writeContents(file, headCommitID);
    }

    public static void rmBranch(String branchName) {
        File file = join(BRANCHES_DIR, branchName);
        if (!file.exists()) {
            Utils.exitWithMessage("A branch with that name does not exist.");
        }
        String headBranch = readContentsAsString(HEAD);
        if (branchName.equals(headBranch)) {
            Utils.exitWithMessage("Cannot remove the current branch.");
        }
        file.delete();
    }

    public static void reset(String commitID) {
        File file = join(COMMITS_DIR, commitID.substring(0, 2));
        file = join(file, commitID.substring(2));
        if (!file.exists()) {
            Utils.exitWithMessage("No commit with that id exists.");
        }
        checkoutFromCommitID(commitID);
        clearStagingArea();
        writeContents(join(BRANCHES_DIR, readContentsAsString(HEAD)), commitID);
    }

    private static void checkoutFilesAfterCheck(HashMap<String, String> filesFromCommit) {
        HashSet<String> untrackedFiles = getUntrackedFiles();
        for (String fileName : filesFromCommit.keySet()) {
            if (untrackedFiles.contains(fileName)) {
                Utils.exitWithMessage("There is an untracked file in the way; delete it,  or add and commit it first.");
            }
        }
        for (Map.Entry<String, String> entry : filesFromCommit.entrySet()) {
            String fileName = entry.getKey();
            String blobID = entry.getValue();
            Blob blob = Blob.read(blobID);
            String content = blob.getContents();
            writeContents(join(CWD, fileName), content);
            Repository.add(fileName);
        }
    }

    private static String conflict(String fileName, String headBlobID, String givenBlobID) {

        String headContent = headBlobID == null ? "" : Blob.read(headBlobID).getContents();
        String givenContent = givenBlobID == null ? "" : Blob.read(givenBlobID).getContents();
        String content = "<<<<<<< HEAD\n" + headContent + "=======\n" + givenContent + ">>>>>>>\n";
        writeContents(join(CWD, fileName), content);
        Blob blob = new Blob(content);
        return blob.commit();
    }

    public static void merge(String branchName) {
        HashSet<String> additions = getAdditions();
        HashSet<String> removals = getRemovals();
        if (!additions.isEmpty() || !removals.isEmpty()) {
            Utils.exitWithMessage("You have uncommitted changes.");
        }
        File file = join(BRANCHES_DIR, branchName);
        if (!file.exists()) {
            Utils.exitWithMessage("A branch with that name does not exist.");
        } else if (file.getName().equals(readContentsAsString(HEAD))) {
            Utils.exitWithMessage("Cannot merge a branch with itself.");
        }

        Commit givenBranch = Commit.read(readContentsAsString(file));
        Commit splitPoint = getHeadCommit().latestCommonAncestorCommit(readContentsAsString(file));
        if (splitPoint == null) {
            Utils.exitWithMessage("Split point is null");
        }
        if (splitPoint.getSha1().equals(givenBranch.getSha1())) {
            Utils.exitWithMessage("Given branch is an ancestor of the current branch.");
        } else if (splitPoint.getSha1().equals(readContentsAsString(HEAD))) {
            checkoutFromCommitID(readContentsAsString(join(BRANCHES_DIR, branchName)));
            Utils.exitWithMessage("Current branch fast-forwarded.");
        }
        Commit headCommit = getHeadCommit();
        // for files tracked in the splitPoint
        HashSet<String> allFiles = new HashSet<>();
        allFiles.addAll(splitPoint.getTrackedFiles());
        allFiles.addAll(headCommit.getTrackedFiles());
        allFiles.addAll(givenBranch.getTrackedFiles());

        HashSet<String> checkoutFiles = new HashSet<>();
        HashSet<String> conflictFiles = new HashSet<>();
        HashSet<String> fileToRemove = new HashSet<>();
        for (String fileName : allFiles) {
            String split = splitPoint.blobID(fileName);
            String head = headCommit.blobID(fileName);
            String given = givenBranch.blobID(fileName);
            if (split == null) {
                // not present at the split point
                if (head == null) {
                    // present in given branch but not in current branch
                    checkoutFiles.add(fileName);
                } else if (given == null) {
                    // present in current branch but not in given branch
                    continue;
                } else if (!head.equals(given)) {
                    // present in both branches
                    // different contents
                    conflictFiles.add(fileName);
                }

            } else {
                // present in the split point
                if (head == null && given == null) {
                    // modified in the same way
                    continue;
                } else if (head != null && given == null) {
                    // deleted in the given branch
                    if (!split.equals(head)) {
                        // unmodified in the current branch, and absent in the given branch
                        conflictFiles.add(fileName);
                    } else {
                        //the contents of one are changed in the current and the other file is delete
                        fileToRemove.add(fileName);
                    }
                } else if (head == null) {
                    // present in the given branch, absent in the current branch
                    if (!split.equals(given)) {
                        // the contents of one are changed and the other file is deleted
                        conflictFiles.add(fileName);
                    }
                    // unmodified in the given branch, should remain absent
                } else {
                    // present both in current and given branch
                    if (split.equals(head) && !split.equals(given)) {
                        // modified in the given branch not modified in the current branch
                        checkoutFiles.add(fileName);
                    } else if (!split.equals(head) && split.equals(given)) {
                        // modified in the current branch not modified in the given branch should stay as they are
                        continue;
                    } else if (!split.equals(head)) {
                        // different contents
                        if (!head.equals(given)) {
                            conflictFiles.add(fileName);
                        }
                    }
                }
            }
        }
        Commit mergeCommit = new Commit("Merged " + branchName + " into " + readContentsAsString(HEAD) + ".", new Date(), getHeadCommit(), givenBranch);
        for (String fileName : fileToRemove) {
            mergeCommit.remove(fileName);
            Utils.restrictedDelete(join(CWD, fileName));
        }
        if (!conflictFiles.isEmpty()) {
            System.out.println("Encountered a merge conflict.");
        }
        for (String fileName : conflictFiles) {
            String headBlobID = headCommit.blobID(fileName);
            String givenBlobID = givenBranch.blobID(fileName);
            String sha1 = conflict(fileName, headBlobID, givenBlobID);
            mergeCommit.add(fileName, sha1);
        }
        HashSet<String> untrackedFiles = getUntrackedFiles();
        for (String fileName : checkoutFiles) {
            if (untrackedFiles.contains(fileName)) {
                Utils.exitWithMessage("There is an untracked file in the way; delete it,  or add and commit it first.");
            }
            checkoutFileFromCommit(givenBranch.getSha1(), fileName);
            mergeCommit.add(fileName, givenBranch.blobID(fileName));
        }
        String sha1 = mergeCommit.save();
        writeContents(join(BRANCHES_DIR, readContentsAsString(HEAD)), sha1);
        clearStagingArea();
    }
}
