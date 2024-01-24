/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Project/Maven2/JavaApp/src/main/java/${packagePath}/${mainClassName}.java to edit this template
 */

package com.gencsadiku.webscraperapp;

/**
 *
 * @author gencsadiku
 */
public class WebScraperApp {

    public static void main(String[] args) {

        WebScraper webScraper = new WebScraper.Builder()
                .url("https://books.toscrape.com/")
                .folderName("ScrappedWebsite")
                .limitConcurrencyTo(10)
                .build();
        
        webScraper.start(); 
    }
}
