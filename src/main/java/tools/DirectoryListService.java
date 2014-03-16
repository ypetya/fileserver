package tools;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

public class DirectoryListService implements HttpHandler {

    public static final String CONTEXT = "/list";

    private final UrlToFilePathMapper dirMapper;
    private byte[] buffer = new byte[4000];

    DirectoryListService(UrlToFilePathMapper dirMapper) {
        this.dirMapper = dirMapper;
    }

    public static String getExtension(File f) {
        String extension = "";
        int index = f.getName().lastIndexOf(".");
        if (index > -1 && index < f.getName().length() - 1) {
            extension = f.getName().substring(index + 1);
        } else {
            extension += f.getName();
        }
        return extension;
    }

    @Override
    public void handle(HttpExchange t) throws IOException {

        URI uri = t.getRequestURI();

        setRequest(uri);

        Headers headers = t.getResponseHeaders();
        File mappedFile = dirMapper.getMappedFile();
        if (mappedFile.isDirectory()) {
            headers.add("Content-Type", "text/html; charset=utf-8");

            final String response = renderDirectoryAsHtml().toString();

            long responseSize = response.getBytes().length;

            t.sendResponseHeaders(HTTP.OK.status, responseSize);

            if (responseSize > 0) {
                OutputStream os = t.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        } else if (mappedFile.isFile()) {

            if ("png".equals(getExtension(mappedFile))) {
                headers.add("Content-Type", "image/png");
            } else {
                headers.add("Content-Type", "application/octet-stream");
            }

            t.sendResponseHeaders(HTTP.OK.status, mappedFile.length());

            OutputStream os = t.getResponseBody();

            BufferedInputStream bis = null;

            try {
                bis = new BufferedInputStream(new FileInputStream(mappedFile));
                int len = bis.read(buffer);
                while (len > -1) {
                    os.write(buffer, 0, len);
                    len = bis.read(buffer);
                }
            } finally {
                bis.close();
                os.close();
            }

        } else {
            t.sendResponseHeaders(HTTP.NOT_FOUND.status, 0);
        }
    }

    void setRequest(URI uri) {
        dirMapper.setInputUrl(uri);
    }

    StringBuilder renderDirectoryAsHtml() {

        StringBuilder sb = new StringBuilder("Path : ").append(dirMapper.getAbsolutePath());

        File dir = dirMapper.getMappedFile();
        if (dir.isDirectory()) {
            sb.append("<ul>");
            for (File entry : dir.listFiles()) {
                sb.append("<li>");

                sb.append("<a ");
                sb.append("type=\"");
                if (entry.isDirectory()) {
                    sb.append("directory:");
                } else if (entry.isFile()) {
                    sb.append("file:");
                }
                String ext=getExtension(entry);
                sb.append(ext);

                sb.append("\" ");
                sb.append("href=\"");
                sb.append(dirMapper.getRelativeUrlPath()).append("/").append(entry.getName());
                sb.append("\">");
                
                sb.append("<img src=\"/thumbnails/").append(ext);
                sb.append(".png\">");
                
                sb.append(entry.getName());
                sb.append("</a></li>");
            }
            sb.append("</ul>");
        }
        return sb;
    }
}
