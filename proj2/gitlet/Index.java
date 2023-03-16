package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.*;

import static gitlet.Repository.
import static gitlet.Utils.*;

public class Index implements Serializable {
    /**
     * ���ļ��� fileName�Լ� parent1, parent2 ӳ��Ϊ sha1 value
     */
    public HashMap<String, String> staged;
    public HashMap<String, String> removed;

    private Index() {
        staged = new HashMap<>();
        removed = new HashMap<>();
    }

    public boolean isEmpty() {
        return staged.isEmpty() && removed.isEmpty();
    }

    public void clear() {
        staged.clear();
        removed.clear();
    }

    public void save(){
        writeObject();
    }
}
