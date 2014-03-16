package tools;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import tools.file.FileFactory;
import tools.file.FileSystem;

public class UrlToFilePathMapper {

    private final FileFactory ff;

    private final String uriContext;
    private final File root;

    private URI inputURI;

    private String absolutePath;
    private String relativeUrlPath;
    private File mappedFile;

    UrlToFilePathMapper(String uriContext, String rootDir) {
        this(uriContext, rootDir, null);
    }

    UrlToFilePathMapper(String uriContext, String rootDir, FileFactory ff) {
        this.ff = ff == null ? new FileSystem() : ff;
        this.uriContext = uriContext;
        this.root = this.ff.createNewFile(rootDir);
        if (!root.isDirectory()) {
            throw new IllegalArgumentException("Root is not a directory!");
        }
    }

    void setInputUrl(URI uri) {
        inputURI = uri;
        calculateAbsolutePath();
    }

    String getAbsolutePath() {
        return absolutePath;
    }

    File getMappedFile() {
        return mappedFile;
    }

    String getRelativeUrlPath() {
        return relativeUrlPath;
    }

    private void setMappedFile(File f) {
        mappedFile = f;
        try {
            absolutePath = mappedFile.getCanonicalPath();
        } catch (IOException ex) {
            Logger.getLogger(UrlToFilePathMapper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void calculateAbsolutePath() {
        mappedFile = null;
        String[] path = inputURI.getPath().replace(uriContext, "").split("/");
        setMappedFile(root);

        StringBuilder sb = new StringBuilder(uriContext);
        for (String elem : path) {
            if (!elem.isEmpty()) {
                if (mappedFile.equals(root) && "..".equals(elem)) {
                    continue;
                }
                setMappedFile(ff.createNewFile(mappedFile, elem));

                sb.append("/").append(elem);
                if (!mappedFile.isDirectory()) {
                    break;
                }
            }
        }
        relativeUrlPath = sb.toString();
    }
}
