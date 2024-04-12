package dev.dfonline.codeclient;

import dev.dfonline.codeclient.config.Config;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class FileManager {
    /**
     * Verify the existence and get the mod data folder.
     *
     * @return
     */
    public static Path Path() {
        Path path = CodeClient.MC.runDirectory.toPath().resolve(CodeClient.MOD_ID);
        path.toFile().mkdir();
        return path;
    }

    public static Path templatesPath() {
        Path path = CodeClient.MC.runDirectory.toPath().resolve(CodeClient.MOD_ID).resolve("templates");
        path.toFile().mkdir();
        return path;
    }

    public static Path writeFile(String fileName, String content) throws IOException {
        return writeFile(fileName, content, true);
    }

    public static File getConfigFile() {
        return new File(FabricLoader.getInstance().getConfigDir().toFile(), CodeClient.MOD_ID + ".json");
    }

    public static void writeConfig(String content) throws IOException {
        boolean ignore;
        File file = getConfigFile();
        Files.deleteIfExists(file.toPath());
        Files.createFile(file.toPath());
        if (!file.exists()) ignore = file.createNewFile();
        Files.write(file.toPath(), content.getBytes(), StandardOpenOption.WRITE);
    }

    public static String readConfig() throws IOException {
        return Files.readString(getConfigFile().toPath());
    }

    public static Path writeFile(String fileName, String content, boolean doCharSet) throws IOException {
        Path path = Path().resolve(fileName);
        Files.deleteIfExists(path);
        Files.createFile(path);
        if (doCharSet) {
            Files.write(path, content.getBytes(Config.getConfig().SaveCharSet.charSet), StandardOpenOption.WRITE);
        } else {
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