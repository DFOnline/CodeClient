package dev.dfonline.codeclient;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

public class FileManager {
    public static Path Path(){
        Path path = CodeClient.MC.gameDirectory.toPath().resolve(CodeClient.MOD_ID);
        path.toFile().mkdir();
        return path;
    }

    public static void writeFile(String fileName, String content) throws IOException {
        File file = Path().resolve(fileName).toFile();
        FileWriter fileWriter = new FileWriter(file);
        fileWriter.write(content);
        fileWriter.close();
    }
}