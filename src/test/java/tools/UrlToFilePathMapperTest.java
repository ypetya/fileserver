package tools;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import tools.file.FileFactory;
import tools.file.TestFileSystem;

public class UrlToFilePathMapperTest {

    // constants
    private URI URI_LIST;
    // the class we test
    private UrlToFilePathMapper dirMapper;
    // mocks
    FileFactory fileFactory;

    @Before
    public void setup() {
        URI_LIST = createURI(DirectoryListService.CONTEXT);

        fileFactory = new TestFileSystem();
        dirMapper = new UrlToFilePathMapper(DirectoryListService.CONTEXT, ".", fileFactory);
    }

    private URI createURI(String urlEnding) {
        URI ret = null;
        try {
            ret = new URI("http://localhost:8000" + urlEnding);
        } catch (URISyntaxException ex) {
            Logger.getLogger(UrlToFilePathMapperTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    }

    @Test
    public void shouldWorkWithAbsoluteDirectoryPath() {
        // GIVEN
        URI url1 = createURI( DirectoryListService.CONTEXT + "/subDirectory");
        // WHEN
        dirMapper.setInputUrl(url1);
        // THEN
        String abs = dirMapper.getAbsolutePath();

        assertEquals(fileFactory.createNewFile("subDirectory").getAbsolutePath(), abs);
    }

    @Test
    public void shouldCdUpToKeepLinksWorking() {
        // GIVEN
        URI url1 = createURI( DirectoryListService.CONTEXT + "/subDirectory");
        URI url2 = createURI( DirectoryListService.CONTEXT + "/subDirectory/subsub/..");
        // WHEN
        dirMapper.setInputUrl(url1);
        String t1 = dirMapper.getAbsolutePath();
        dirMapper.setInputUrl(url2);
        String t2 = dirMapper.getAbsolutePath();
        // THEN
        assertEquals(fileFactory.createNewFile("subDirectory").getAbsolutePath(),t1);
        assertEquals(t1,t2);
    }

    @Test
    public void shouldNotCdUpperThanTheRootDirectory() throws IOException {
        // GIVEN
        URI url1 = createURI( DirectoryListService.CONTEXT + "/..");
        // WHEN
        dirMapper.setInputUrl(url1);
        // THEN
        assertEquals(fileFactory.createNewFile(".").getCanonicalPath(),dirMapper.getAbsolutePath());
    }

    @Test
    public void shouldReturnRelativePath() {
        // GIVEN
        URI url1 = createURI( DirectoryListService.CONTEXT + "/subDirectory/subsub/grower.sh" );
        // WHEN
        dirMapper.setInputUrl(url1);
        // THEN
        assertEquals(DirectoryListService.CONTEXT + "/subDirectory/subsub/grower.sh", dirMapper.getRelativeUrlPath());
    }

    @Test
    public void shouldTranslateUrlPathToFile() {
         // GIVEN
        URI url1 = createURI( DirectoryListService.CONTEXT + "/subDirectory/subsub/grower.sh" );
        // WHEN
        dirMapper.setInputUrl(url1);
        // THEN
        assertTrue(dirMapper.getMappedFile().isFile());
    }
}
