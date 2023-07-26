package dev.dfonline.codeclient;

import dev.dfonline.codeclient.config.Config;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class FileManager {
    /**
     * Verify the existence and get the mod data folder.
     * @return
     */
    public static Path Path(){
        Path path = CodeClient.MC.runDirectory.toPath().resolve(CodeClient.MOD_ID);
        path.toFile().mkdir();
        return path;
    }

    public static Path writeFile(String fileName, String content) throws IOException {
        return writeFile(fileName, content, true);
    }
    public static Path writeFile(String fileName, String content, boolean doCharSet) throws IOException {
        Path path = Path().resolve(fileName);
        Files.deleteIfExists(path);
        Files.createFile(path);
        if(doCharSet) {
            Files.write(path, content.getBytes(Config.getConfig().SaveCharSet.charSet), StandardOpenOption.WRITE);
        }
        else {
            Files.write(path, content.getBytes(), StandardOpenOption.WRITE);
        }
        return path;
    }

    public static String readFile(String fileName, Charset charset) throws IOException {
        return Files.readString(Path().resolve(fileName), charset);
    }

    public static boolean exists(String fileName) {
        return Files.exists(Path().resolve(fileName));
    }

    /**
     * Reads a file with the configured charset.
     * Will load the config if it isn't.
     */
    public static String readFile(String fileName) throws IOException {
        return readFile(fileName, Config.getConfig().FileCharSet.charSet);
    }
}