package tools;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.net.URL;

public class FileServer {
    
    public static void main(String args[]) throws IOException, URISyntaxException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000),5);
        
        //setup directory service
        UrlToFilePathMapper dirMapper = new UrlToFilePathMapper(DirectoryListService.CONTEXT,".");
        HttpHandler dirService = new DirectoryListService(dirMapper);
        server.createContext(DirectoryListService.CONTEXT, dirService);
        
        // setup thumbnail service
        URL url = FileServer.class.getClassLoader().getResource("icons/README.md");
        String thRoot = new File(url.toURI()).getParentFile().getCanonicalPath();
        UrlToFilePathMapper thumbnails = new UrlToFilePathMapper("/thumbnails",thRoot);
        HttpHandler thumbnailService = new DirectoryListService(thumbnails);
        server.createContext("/thumbnails", thumbnailService);
        
        server.setExecutor(null);
        server.start();
    }
}
