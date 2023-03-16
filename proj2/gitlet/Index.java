package gitlet;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static gitlet.Utils.*;

public class Index {

    /**
     * 把文件名 fileName以及 parent1, parent2 映射为 sha1 value
     */
    public TreeMap<String, String> mapping = new TreeMap<>();

    public Index(Commit parent1) {
        if (parent1 != null) {
            // 继承当前parent commit的映射
            for (Map.Entry<String, String> entry : parent1.index.mapping.entrySet()) {
                mapping.put(entry.getKey(), entry.getValue());
            }
        }

        // 更新stage area中的文件
        List<String> files = plainFilenamesIn(Repository.STAGE_DIR);
        for (String file : files) {
            File fileObject = join(Repository.STAGE_DIR, file);
            String fileSha1 = sha1(fileObject);
            mapping.put(file, fileSha1);
            writeContents(join(Repository.BLOBS_DIR, fileSha1), readContentsAsString(fileObject));
        }

        // 删除untracked files
        for (String file : Repository.removal) {
            mapping.remove(file);
        }
    }

    public Index() {
    }
}
