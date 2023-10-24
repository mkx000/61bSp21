package gitlet;

// TODO: any imports you need here

import java.io.File;
import java.util.*;

/**
 * Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 * @author TODO
 */
public class Commit implements Dumpable {
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
    private final String message;
    private final Date date;

    // can read or write String
    private final String parent1ID;
    private String parent2ID;
    private String sha1;
    // cant' read or write transient fields
    private final transient Commit parent1Commit;
    private transient Commit parent2Commit;

    // 相对路径 -> blob sha1
    private final HashMap<String, String> tree;

    public void dump() {
        System.out.println("Commit");
    }

    public Commit(String msg, Date date, Commit parent) {
        message = msg;
        this.date = date;
        if (parent != null) {
            this.parent1ID = parent.getSha1();
            this.tree = parent.tree;
        } else {
            this.tree = new HashMap<>();
            this.parent1ID = null;
        }
        parent1Commit = parent;
    }

    public Commit(String msg, Date date, Commit parent1, Commit parent2) {
        message = msg;
        this.date = date;
        this.tree = parent1.tree;
        this.tree.putAll(parent2.tree);
        parent1Commit = parent1;
        parent2Commit = parent2;
        this.parent1ID = parent1.getSha1();
        this.parent2ID = parent2.getSha1();
    }

    public void add(String filename, String blobID) {
        tree.put(filename, blobID);
    }

    public void remove(String filename) {
        tree.remove(filename);
    }

    public boolean isFileTracked(String fileName) {
        return tree.containsKey(fileName);
    }

    public static Commit read(String sha1) {
        File dir = Utils.join(Repository.COMMITS_DIR, sha1.substring(0, 2));
        File file = Utils.join(dir, sha1.substring(2));
        return Utils.readObject(file, Commit.class);
    }

    private HashMap<String, Integer> bfs(Commit commit) {
        HashMap<String, Integer> map = new HashMap<>();
        Queue<Commit> queue = new LinkedList<>();
        queue.add(commit);
        map.put(commit.sha1, 0);
        while (!queue.isEmpty()) {
            Commit cur = queue.poll();
            int depth = map.get(cur.sha1);
            if (cur.parent1ID != null) {
                if (!map.containsKey(cur.getFirstParentCommit().sha1)) {
                    queue.add(cur.getFirstParentCommit());
                    map.put(cur.getFirstParentCommit().sha1, depth + 1);
                }
            }
            if (cur.parent2ID != null) {
                if (!map.containsKey(cur.getSecondParentCommit().sha1)) {
                    queue.add(cur.getSecondParentCommit());
                    map.put(cur.getSecondParentCommit().sha1, depth + 1);
                }
            }
        }
        return map;
    }

    public Commit latestCommonAncestorCommit(String commitID) {
        Commit commit = this;
        Commit givenCommit = Commit.read(commitID);
        HashMap<String, Integer> commitMap = bfs(commit);
        HashMap<String, Integer> givenCommitMap = bfs(givenCommit);
        int minDepth = Integer.MAX_VALUE;
        Commit ancestorCommit = null;
        for (Map.Entry<String, Integer> entry : commitMap.entrySet()) {
            String sha1 = entry.getKey();
            int depth = entry.getValue();
            if (givenCommitMap.containsKey(sha1)) {
                if (depth < minDepth) {
                    minDepth = depth;
                    ancestorCommit = Commit.read(sha1);
                }
            }
        }
        return ancestorCommit;
    }

    public String toString() {
        Formatter f = new Formatter();
        f.format("===\n");
        f.format("commit %s\n", sha1);
        if (parent2Commit != null) {
            f.format("Merge: %s %s\n", parent1ID.substring(0, 7), parent2ID.substring(0, 7));
        }
        f.format("Date: %s +0800\n", date.toString().substring(0, 20) + date.toString().substring(24));
        f.format("%s\n", message);
        return f.toString();
    }

    public Commit getFirstParentCommit() {
        if (parent1ID == null) {
            return null;
        }
        File file = Utils.join(Repository.COMMITS_DIR, parent1ID.substring(0, 2), parent1ID.substring(2));
        return Utils.readObject(file, Commit.class);
    }

    private Commit getSecondParentCommit() {
        if (parent2ID == null) {
            return null;
        }
        File file = Utils.join(Repository.COMMITS_DIR, parent2ID.substring(0, 2), parent2ID.substring(2));
        return Utils.readObject(file, Commit.class);
    }

    public String getMessage() {
        return message;
    }

    public String getSha1() {
        return sha1;
    }

    public HashSet<String> getTrackedFiles() {
        return new HashSet<>(tree.keySet());
    }

    public String save() {
        String sha1 = Utils.sha1((Object) Utils.serialize(this));
        this.sha1 = sha1;
        File dir = Utils.join(Repository.COMMITS_DIR, sha1.substring(0, 2));
        if (!dir.exists()) {
            if (!dir.mkdir()) {
                Utils.exitWithMessage("mkdir failed in Commit.save()");
            }
        }
        Utils.writeObject(Utils.join(dir, sha1.substring(2)), this);
        return sha1;
    }

    public String blobID(String relativeFileName) {
        return tree.getOrDefault(relativeFileName, null);
    }
}
