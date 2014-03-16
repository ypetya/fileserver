package tools.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is about injecting src/test/resources/test_root as the root
 * filesystem for the unit tests
 */
public class TestFileSystem implements FileFactory {

    final Logger logger = Logger.getLogger(TestFileSystem.class.getName());
    File root;

    public TestFileSystem() {
        URL url = TestFileSystem.class.getClassLoader().getResource("test_root/README.md");
        try {
            root = new File(url.toURI()).getParentFile();
        } catch (URISyntaxException ex) {
            Logger.getLogger(TestFileSystem.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public File createNewFile(String fileName) {
        return new File(root, fileName);
    }

    @Override
    public File createNewFile(File parent, String child) {
        return new File(parent, child);
    }

    public byte[] getFileContent(String fileName) {
        FileInputStream fis = null;
        byte[] ret = null;
        try {
            File f = createNewFile(fileName);

            ret = new byte[(int) f.length()];
            fis = new FileInputStream(f);
            fis.read(ret);

        } catch (FileNotFoundException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        } finally {
            try {
                fis.close();
            } catch (IOException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }
        return ret;
    }
}
