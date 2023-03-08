package dev.dfonline.codeclient;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class FileManager {
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

    public static String readFile(String fileName) throws IOException {
        return Files.readString(Path().resolve(fileName));
    }
}