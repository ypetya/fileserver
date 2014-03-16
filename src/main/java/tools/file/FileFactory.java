/**
 * With the help of this interface and implementation,
 * test sourceSet can inject their own filesystem to the UrlToFilePathMapper
 */
package tools.file;

import java.io.File;

public interface FileFactory {

    File createNewFile(String fileName);
    File createNewFile(File parent, String child);
}
