package tools;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.Matchers;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import tools.file.TestFileSystem;

public class DirectoryListServiceTest {

    private URI listURI;
    // the class we test
    private DirectoryListService directoryListService;
    // mocks
    private HttpExchange httpExchange;
    Headers headers;
    OutputStream outputStream;

    private UrlToFilePathMapper urlMapper;
    private TestFileSystem testFileSystem;

    @Before
    public void setup() {
        listURI = createURI(DirectoryListService.CONTEXT);

        httpExchange = mock(HttpExchange.class);
        headers = mock(Headers.class);
        when(httpExchange.getResponseHeaders()).thenReturn(headers);
        outputStream = mock(OutputStream.class);
        when(httpExchange.getResponseBody()).thenReturn(outputStream);

        try {
            doNothing().when(outputStream).write(any(byte[].class));
        } catch (IOException ex) {
            Logger.getLogger(DirectoryListServiceTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        testFileSystem = new TestFileSystem();
        urlMapper = new UrlToFilePathMapper("/list", ".", testFileSystem);
        directoryListService = new DirectoryListService(urlMapper);
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
    public void shouldAddHeadersWhenRenderingDirectory() throws IOException, URISyntaxException {
        // GIVEN
        when(httpExchange.getRequestURI()).thenReturn(listURI);
        // WHEN
        directoryListService.handle(httpExchange);
        // THEN
        verify(headers).add("Content-Type", "text/html; charset=utf-8");
        verify(httpExchange).sendResponseHeaders(eq(HTTP.OK.status), Matchers.anyInt());
    }

    @Test
    public void shouldRender404IfFileNotExists() throws IOException {
        // GIVEN
        URI notExists = createURI(DirectoryListService.CONTEXT + "/unavailable");
        when(httpExchange.getRequestURI()).thenReturn(notExists);
        // WHEN
        directoryListService.handle(httpExchange);
        // THEN
        verify(httpExchange).sendResponseHeaders(eq(HTTP.NOT_FOUND.status),
                Matchers.anyInt());
    }

    @Test
    public void shouldAddSpecificHeadersWhenRenderingAFile() throws IOException {
        // GIVEN
        URI isAFile = createURI(DirectoryListService.CONTEXT + "/README.md");
        when(httpExchange.getRequestURI()).thenReturn(isAFile);
        // WHEN
        directoryListService.handle(httpExchange);
        // THEN
        verify(headers).add("Content-Type", "application/octet-stream");
    }

    @RunWith(Parameterized.class)
    public static class CanParseExtension {

        @Parameters
        public static Collection<Object[]> data() {
            return Arrays.asList(new Object[][]{
                {"test.png", "png"}, {".profile", "profile"},
                {"no_extension", "no_extension"},
                {"m.u.l", "l"}, {"tiple.", "tiple."}
            });
        }

        String extension;
        File file;

        public CanParseExtension(String fileName, String extension) {
            file = new File(fileName);
            this.extension = extension;
        }

        @Test
        public void shouldMatch() {
            assertEquals(extension, DirectoryListService.getExtension(file));
        }
    }

    @Test
    public void shouldAddImageHeadersWhenRenderingAPNGFile() throws IOException {
        // GIVEN
        URI isAFile = createURI(DirectoryListService.CONTEXT + "/java.png");
        when(httpExchange.getRequestURI()).thenReturn(isAFile);
        // WHEN
        directoryListService.handle(httpExchange);
        // THEN
        verify(headers).add("Content-Type", "image/png");
    }

    @Test
    public void shouldRenderOutputOnList() throws IOException {
        // GIVEN
        when(httpExchange.getRequestURI()).thenReturn(listURI);
        // WHEN
        directoryListService.handle(httpExchange);
        // THEN
        verify(outputStream).write(any(byte[].class));
    }

    @Test
    public void shouldRenderThePathInResponse() {
        // GIVEN
        URI uri = createURI(DirectoryListService.CONTEXT + "/subDirectory");
        // WHEN
        directoryListService.setRequest(uri);
        String resp = directoryListService.renderDirectoryAsHtml().toString();
        // THEN
        assertTrue(resp.contains("/subDirectory"));
    }

    @Test
    public void shouldRenderAllTheFilesInADirectory() {
        // GIVEN
        URI uri = createURI(DirectoryListService.CONTEXT + "/subDirectory/subsub");
        // WHEN
        directoryListService.setRequest(uri);
        String resp = directoryListService.renderDirectoryAsHtml().toString();
        // THEN
        File dir = testFileSystem.createNewFile("subDirectory/subsub");

        for (File f : dir.listFiles()) {
            String typePrefix = "type=\"";
            if (f.isDirectory()) {
                typePrefix += "directory:";
            } else if (f.isFile()) {
                typePrefix += "file:";
            }

            typePrefix = DirectoryListService.getExtension(f);

            typePrefix += "\" ";
            String tagPrefix = "href=\"";
            String urlPart = urlMapper.getRelativeUrlPath() + "/" + f.getName();
            String matchOn = typePrefix + tagPrefix + urlPart;
            if (!resp.contains(matchOn)) {
                fail("String not found : " + matchOn + "\nin:\n" + resp);
            }
        }
    }

    @Test
    public void shouldRenderAFileContent() throws IOException {
        // GIVEN
        URI isAFile = createURI(DirectoryListService.CONTEXT + "/README.md");
        when(httpExchange.getRequestURI()).thenReturn(isAFile);
        final byte[] fileContent = testFileSystem.getFileContent("README.md");
        // WHEN
        directoryListService.handle(httpExchange);
        // THEN
        verify(outputStream).write( //eq(fileContent)
                (byte[]) argThat(new Matcher() {
                    int index = -1;

                    @Override
                    public boolean matches(Object item) {
                        if (item instanceof byte[]) {
                            byte[] buffer = (byte[]) item;

                            for (index = 0; index < fileContent.length; index++) {
                                if (buffer[index] != fileContent[index]) {
                                    return false;
                                }
                            }
                            return true;
                        }
                        return false;
                    }

                    @Override
                    public void _dont_implement_Matcher___instead_extend_BaseMatcher_() {
                        throw new UnsupportedOperationException("Not supported.");
                    }

                    @Override
                    public void describeTo(Description description) {
                        description.appendText("Byte[] Did not match!");
                        description.appendText("at index : " + index);
                    }

                }), eq(0), eq(fileContent.length));
    }
}
