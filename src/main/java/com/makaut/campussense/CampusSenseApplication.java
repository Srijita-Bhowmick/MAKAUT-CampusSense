package com.makaut.campussense;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

@SpringBootApplication
@EnableAsync
public class CampusSenseApplication {

    static {
        // Avoid Windows dual-stack IPv6 connect hangs to external HTTPS APIs such as Gemini.
        System.setProperty("java.net.preferIPv4Stack", "true");
    }

    public static void main(String[] args) {
        System.setProperty("java.awt.headless", "true");
        loadDotenv();
        SpringApplication app = new SpringApplication(CampusSenseApplication.class);
        app.setHeadless(true);
        app.run(args);
    }

    private static void loadDotenv() {
        for (String filename : List.of(".env.local", ".env")) {
            File file = new File(filename);
            if (file.exists() && file.canRead()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        if (line.isEmpty() || line.startsWith("#") || !line.contains("=")) continue;
                        int eqIdx = line.indexOf('=');
                        String key = line.substring(0, eqIdx).trim();
                        String val = line.substring(eqIdx + 1).trim();
                        if ((val.startsWith("\"") && val.endsWith("\"")) || (val.startsWith("'") && val.endsWith("'"))) {
                            if (val.length() >= 2) {
                                val = val.substring(1, val.length() - 1);
                            }
                        }
                        if (System.getProperty(key) == null && System.getenv(key) == null && !val.isEmpty()) {
                            System.setProperty(key, val);
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        }
    }
}
