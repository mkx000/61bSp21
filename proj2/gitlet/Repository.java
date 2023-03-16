package gitlet;

import java.io.File;
import java.util.*;

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

    public static final File COMMITS_DIR = join(GITLET_DIR, "commits");
    public static final File STAGE_DIR = join(GITLET_DIR, "stage");
    public static final File BLOBS_DIR = join(GITLET_DIR, "blobs");
    public static final File BRANCHES = join(GITLET_DIR, "branches");

    //存储HEAD or branch对应的commit sha1代码
    public static final File HEAD = join(GITLET_DIR, "HEAD");
    public static final File branch = join(GITLET_DIR, "branch");
    public static final File master = join(BRANCHES, "master");

    //将被删除的文件名
    public static HashSet<String> removal = new HashSet<>();

    public static void initRepo() {
        if (GITLET_DIR.exists()) {
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            System.exit(0);
        }
        try {
            GITLET_DIR.mkdir();
            COMMITS_DIR.mkdir();
            STAGE_DIR.mkdir();
            BLOBS_DIR.mkdir();
            BRANCHES.mkdir();
            HEAD.createNewFile();
            master.createNewFile();
            branch.createNewFile();
        } catch (Exception e) {
            System.out.println("can't create .gitlet or stage or commits directory");
            System.exit(0);
        }
        Commit initialCommit = new Commit("initial commit", new Date(0));
        String sha1Value = initialCommit.saveCommit();
        writeContents(HEAD, sha1Value);
        writeContents(master, sha1Value);
        writeContents(branch, "master");
    }

    public static void add(String fileName) {
        File file = join(CWD, fileName);
        if (!file.exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        }

        Commit currentCommit = getHeadCommit();
        String curSha1 = currentCommit.index.mapping.getOrDefault(fileName, null);
        String thisSha1 = sha1(file);
        if (thisSha1.equals(curSha1)) {
            restrictedDelete(join(STAGE_DIR, fileName)); // stage区删去已经提交的文件
        } else {
            writeContents(join(STAGE_DIR, fileName), readContentsAsString(file)); //内容不一致stage区重写fileName文件
        }
        removal.remove(fileName);
    }

    public static void commit(String msg) {
        if (msg.equals("")) {
            System.out.println("Please enter a commit message.");
            System.exit(0);
        }

        List<String> staged = plainFilenamesIn(STAGE_DIR);
        if (staged.isEmpty()) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }

        Commit newCommit = new Commit(msg, new Date(), getHeadCommit());
        String sha1Value = newCommit.saveCommit();
        writeContents(HEAD, sha1Value);
        writeContents(getBranch(), sha1Value);
        /**
         *  clean stage area
         */
        for (String file : staged) {
            restrictedDelete(join(STAGE_DIR, file));
        }
    }

    public static Commit getHeadCommit() {
        // init repo时HEAD为null
        String head = readContentsAsString(HEAD);
        if (head.isEmpty()) {
            return null;
        }
        return readObject(join(COMMITS_DIR, readContentsAsString(HEAD)), Commit.class);
    }

    public static File getBranch() {
        String branchName = readContentsAsString(branch);
        return join(BRANCHES, branchName);
    }
}
