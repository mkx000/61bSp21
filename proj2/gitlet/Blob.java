package gitlet;

import java.io.File;

public class Blob implements Dumpable {
    private String contents;
    private String sha1;

    public void dump() {
        System.out.println("Blob");
    }

    public Blob(String contents) {
        this.contents = contents;
        sha1 = Utils.sha1(contents);
    }

    public String getSha1() {
        return sha1;
    }

    public String getContents() {
        return contents;
    }

    public String stage() {
        File file = Utils.join(Repository.STAGED_DIR, sha1);
        Utils.writeContents(file, contents);
        return Utils.sha1(contents);
    }

    public String commit() {
        File file = Utils.join(Repository.BLOBS_DIR, sha1);
        Utils.writeContents(file, contents);
        return Utils.sha1(contents);
    }

    public static Blob read(String sha1) {
        File file = Utils.join(Repository.BLOBS_DIR, sha1);
        return new Blob(Utils.readContentsAsString(file));
    }

    public static Blob readFromStage(String sha1) {
        File file = Utils.join(Repository.STAGED_DIR, sha1);
        return new Blob(Utils.readContentsAsString(file));
    }
}