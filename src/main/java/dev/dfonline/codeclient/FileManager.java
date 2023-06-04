package dev.dfonline.codeclient;

import dev.dfonline.codeclient.config.Config;

import java.io.*;
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

    public static void writeFile(String fileName, String content) throws IOException {
        Path path = Path().resolve(fileName);
        Files.deleteIfExists(path);
        Files.createFile(path);
        Files.write(path,content.getBytes(), StandardOpenOption.WRITE);
    }

    public static String readFile(String fileName, Charset charset) throws IOException {
        return Files.readString(Path().resolve(fileName), charset);

    }

    /**
     * Reads a file with the configured charset.
     * Will load the config if it isn't.
     */
    public static String readFile(String fileName) throws IOException {
        return readFile(fileName, Config.getConfig().FileCharSet.charSet);
    }
}