package tools.file;

import java.io.File;

public class FileSystem implements FileFactory {

    @Override
    public File createNewFile(String fileName) {
        return new File(fileName);
    }

    @Override
    public File createNewFile(File parent, String child) {
        return new File(parent, child);
    }
    
}
