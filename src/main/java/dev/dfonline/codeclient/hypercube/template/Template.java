package dev.dfonline.codeclient.hypercube.template;

import dev.dfonline.codeclient.CodeClient;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.zip.GZIPInputStream;

public class Template {
    public ArrayList<TemplateBlock> blocks;

    /**
     * Parse base64+gzip data
     */
    public static Template parse64(String data) {
        try {
            return parse(Base64.getDecoder().decode(data));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Parse gzip data
     */
    public static Template parse(byte[] data) throws IOException {
        return parse(new String(decompress(data)));
    }

    /**
     * Uncompressed JSON
     */
    public static Template parse(String data) {
        return CodeClient.gson.fromJson(data, Template.class);
    }

    private static byte[] decompress(byte[] compressedData) throws IOException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(compressedData);
             GZIPInputStream gis = new GZIPInputStream(bis);
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = gis.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }

            return bos.toByteArray();
        }
    }

    public int getLength() {
        int length = 0;
        for (var block : blocks) length += block.getLength();
        return length;
    }
}
