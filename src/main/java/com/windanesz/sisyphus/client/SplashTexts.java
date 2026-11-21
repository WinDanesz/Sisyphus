package com.windanesz.sisyphus.client;

import com.windanesz.sisyphus.Settings;
import com.windanesz.sisyphus.SisyphusBackport;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class SplashTexts {

    private static final ResourceLocation OURS = new ResourceLocation(SisyphusBackport.MODID, "texts/splashes.txt");

    private SplashTexts() {
    }

    static List<String> readLines(InputStream stream) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    lines.add(line);
                }
            }
        }
        return lines;
    }

    public static void addTo(List<String> splashes) {
        if (!Settings.extras.enableSplashTexts) {
            return;
        }
        String player = Minecraft.getMinecraft().getSession().getUsername();
        try (IResource resource = Minecraft.getMinecraft().getResourceManager().getResource(OURS)) {
            for (String line : readLines(resource.getInputStream())) {
                splashes.add(line.replace("<Playername>", player));
            }
        } catch (IOException e) {
            // No splashes of ours, perhaps taken out by a resource pack
        }
    }
}
