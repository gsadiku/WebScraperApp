/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.gencsadiku.webscraperapp;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author gencsadiku
 */
public class WebScraper {
    private String websiteURL;
    private String folderName;
    private ThreadPoolExecutor threadPoolExecutor;
    private final ConcurrentHashMap<String, Boolean> urlsScrapped = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Boolean> resourcesScrapped = new ConcurrentHashMap<>();
    private int progressCounter = 0;
   
    // Constructor is private since we are using Builder class
    private WebScraper(){}
    
     public void start(){
        // Start process of scrapping website
        scrape(websiteURL);
        // wait until everytask is done so we open folder where website is located automatically
        try{
            while(threadPoolExecutor.getTaskCount() != threadPoolExecutor.getCompletedTaskCount()){
                Thread.sleep(1000); // Waiting for all tasks to be completed
            }

            threadPoolExecutor.shutdown();
            threadPoolExecutor.awaitTermination(60, TimeUnit.SECONDS);
            System.out.println("Website is scrapped in this location: " + getPath() + "/");
        }catch(Exception e){
            System.err.println(e.getMessage());
        }
    }
    // Holds counter for pages that are processed
    public synchronized void incrementProgressCounter(){
        progressCounter++;
        System.out.println(progressCounter + " Pages Processed!");
    }
    // Scrape URL
    private void scrape(String url){
        if(urlsScrapped.containsKey(url)){ // if url has been scrapped then we stop the process
            return;
        }
        urlsScrapped.put(url, Boolean.TRUE);

        try{
            // Connect to the link
            Document document = Jsoup.connect(url).timeout(5000).get();
            Elements links = document.select("a[href]");
            Elements images = document.select("img[src]");
            Elements styleSheets = document.select("link[rel=stylesheet]");
            // Fetch all links
            for(Element link : links){
                // Add new task to threadPoolExecutor
                threadPoolExecutor.execute(()->{
                    scrape(link.absUrl("href"));
                });
            }
            // Fetch all images from current link
            for(Element img : images){
                // Add new task to threadPoolExecutor
                threadPoolExecutor.execute(()->{
                    downloadAndSaveResources(img.absUrl("src"));
                });
                
            }
            // Fetch CSS files from current link
            for(Element styleSheet : styleSheets){
                // Add new task to threadPoolExecutor
                threadPoolExecutor.execute(()->{
                    downloadAndSaveResources(styleSheet.absUrl("href"));
                }); 
            }
            // save html file
            saveHtmlFile(document, url);
            incrementProgressCounter(); // update progress counter for everypage that is scrapped

        } catch(Exception e){
            System.err.println(e.getMessage());
        }
    }

    private String getPath() throws IOException{
        String directoryPath = System.getProperty("user.home") + "/Desktop/" + this.folderName;
        if(!Files.exists(Paths.get(directoryPath))){
            Files.createDirectories(Paths.get(directoryPath));
        }
        return  directoryPath;
    }

    // save html localy
    private void saveHtmlFile(Document document, String url) throws IOException{
        // check if directory exists for the document
        String filePath = getPath() + File.separator + refactorURLToLocal(url);
        String dir = filePath.substring(0, filePath.lastIndexOf('/') + 1);
            
        if(!Files.exists(Paths.get(dir))){
            Files.createDirectories(Paths.get(dir));
        }
        
        try(FileWriter fileWrite = new FileWriter(new File(filePath))){
            fileWrite.write(document.html());
        }      
    }

    // downloads and saves resources from urls
    private void downloadAndSaveResources(String url){
        if(resourcesScrapped.containsKey(url)){ // skip if the resource have been scrapped
            return;
        }
        resourcesScrapped.put(url, Boolean.TRUE);
        
        try(InputStream in = new URL(url).openStream()){
            // check if directory exists for the resources
            String filePath = getPath() + File.separator + refactorURLToLocal(url);
            String dir = filePath.substring(0, filePath.lastIndexOf('/') + 1);
            
            if(!Files.exists(Paths.get(dir))){
                Files.createDirectories(Paths.get(dir));
            }

            Files.copy(in, Paths.get(filePath),StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
             System.err.println(ex.getMessage());
        }

    }
    
    private String refactorURLToLocal(String url){
        return url.substring(this.websiteURL.length());
    }

    public static class Builder{
        private final WebScraper instance = new WebScraper();

        public Builder url(String value){
            this.instance.websiteURL = value;
            return this;
        }

        public Builder folderName(String value){
            this.instance.folderName = value;
            return this;
        }

        public Builder limitConcurrencyTo(int value){
            this.instance.threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(value);
            return this;
        }

        public WebScraper build(){
            return this.instance;
        }
    }
}
