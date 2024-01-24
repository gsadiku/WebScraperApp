/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.gencsadiku.webscraperapp;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.Test;
import java.nio.file.Paths;
import static org.junit.jupiter.api.Assertions.assertTrue;
/**
 *
 * @author gencsadiku
 */
public class WebScraperTest {

    @Test
    public void testWebScraperBasicFunctionality() throws IOException {
 
        WebScraper webScraper = new WebScraper.Builder()
               .url("https://books.toscrape.com/")
               .folderName("ScrappedWebsite")
               .limitConcurrencyTo(10)
               .build();

        // Start the scraper
        webScraper.start();
        Path expectedPath = Paths.get(System.getProperty("user.home") + "/Desktop/ScrappedWebsite");
        Path expectedFile = Paths.get(System.getProperty("user.home") + "/Desktop/ScrappedWebsite/index.html");
        // Test if folder is created
        assertTrue(Files.exists(expectedPath));
        assertTrue(Files.size(expectedPath) > 0);
        assertTrue(Files.exists(expectedFile));
        
    }
}
