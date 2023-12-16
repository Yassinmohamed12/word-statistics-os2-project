import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

// function Runnable  interface have one methode called run it is starting point in every threads
class FileProcessing implements Runnable {

    private boolean scanSubDir;
    private String dirPath;
    private List<File> textFiles;
    private String overallLongestWord;
    private String overallShortestWord;
    private int fileInProcessing = 0;
    private List<Thread> threads;
    private int updateSpeed;
    private FileGUI myGUI;
    private int doneJob = 0;

    public FileProcessing(boolean scanSubDir, String dirPath) {
        this.scanSubDir = scanSubDir;
        this.dirPath = dirPath;
        this.textFiles = new ArrayList<>();
        this.overallLongestWord = "";
        this.overallShortestWord = "";
        this.updateSpeed = 300;
    }

    public void setGUI(FileGUI myGUI) {
        this.myGUI = myGUI;
    }
// this function find the directory path of the file and if no directory print Invalid directory path
    public void initializeTextFiles() throws Exception {
        File directory = new File(dirPath);

        if (directory.exists() && directory.isDirectory()) {
            scanDirectory(directory);
        } else {
            //handle here
            System.err.println("Invalid directory path");
            throw new Exception();
        }
    }
// this function scan the files in the directory and scan that texts in the file or not
    private void scanDirectory(File directory) {
        File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory() && scanSubDir) {
                    // Recursively scan subdirectories if scanSubDir is true
                    scanDirectory(file);
                } else if (file.isFile() && file.getName().toLowerCase().endsWith(".txt")) {
                    // Add text files to the list
                    textFiles.add(file);
                }
            }
        }
    }
// main function that find LongestAndShortestWordsWithCounts by make arraylist and buffer reader to read all words in each line
//and logic for longest word and shortest word and is are you 
    public List<Object> findLongestAndShortestWordsWithCounts(File file) throws InterruptedException {
        List<String> words = new ArrayList<>();
        List<Object> wordInfo = new ArrayList<>();
        String longestWord = "";
        String shortestWord = "";
        wordInfo.add(file.getName()); //filename
        wordInfo.add(0); //wordcount
        wordInfo.add(0); // is
        wordInfo.add(0); // are
        wordInfo.add(0); // you
        wordInfo.add(""); //longest
        wordInfo.add(""); //shortest

        int flag = 0;
// read file on disk each file have many words and iterate in every words fe kol line
        try ( BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Split the line into words using space as a delimiter
                String[] lineWords = line.split("\\s+");
                for (String word : lineWords) {
                    try {
                        Thread.sleep(this.updateSpeed);
                    } catch (InterruptedException ex) {
                    }

                    // Add the word to the list
                    words.add(word);
                    myGUI.updateMyTable(wordInfo, file);
                    if (flag == 0) {
                        shortestWord = words.size() > 0 ? words.get(0) : "";
                        flag++;
                    }

                    wordInfo.set(1, (Integer) wordInfo.get(1) + 1);

                    // Find the longest and shortest words in this file
                    if (word.length() > longestWord.length()) {
                        longestWord = word;
                        wordInfo.set(5, longestWord);
                        //update
                    }
                    myGUI.updateMyTable(wordInfo, file);
                    if (word.length() <= shortestWord.length()) {
                        shortestWord = word;
                        wordInfo.set(6, shortestWord);
                        //update
                    }
                    myGUI.updateMyTable(wordInfo, file);
                    updateOverallLongestAndShortestWords(longestWord, shortestWord);

                    // Count occurrences of specified words
                    if (word.equals("is")) {
                        wordInfo.set(2, (Integer) wordInfo.get(2) + 1);
                        //update
                    } else if (word.equals("are")) {
                        wordInfo.set(3, (Integer) wordInfo.get(3) + 1);
                        //update

                    } else if (word.equals("you")) {
                        wordInfo.set(4, (Integer) wordInfo.get(4) + 1);
                        //update

                    }
                    myGUI.updateMyTable(wordInfo, file);
                    if (Thread.interrupted()) {
                        throw new InterruptedException();
                    }
                }
                myGUI.updateMyTable(wordInfo, file);
            }
        } catch (IOException e) {
            e.printStackTrace();}
        return wordInfo;
    }
// function make update for all longest world and shortest world in all files 
    private synchronized void updateOverallLongestAndShortestWords(String longestWord, String shortestWord) {
        if (longestWord.length() > overallLongestWord.length()) {
            overallLongestWord = longestWord;
        }

        if (shortestWord.length() < overallShortestWord.length() || overallShortestWord.isEmpty()) {
            overallShortestWord = shortestWord;
        }

        myGUI.updateShortestAndLongest(overallShortestWord, overallLongestWord);
    }
// function to print all values in GUI
    public synchronized void printFileInfo(List<Object> result) {

        System.out.println("File: " + result.get(0));
        System.out.println("Total Word Count: " + result.get(1));
        System.out.println("Occurrences of 'is': " + result.get(2));
        System.out.println("Occurrences of 'are': " + result.get(3));
        System.out.println("Occurrences of 'you': " + result.get(4));
        System.out.println("Longest Word: " + result.get(5));
        System.out.println("Shortest Word: " + result.get(6));
        System.out.println("Overall Longest Word: " + overallLongestWord);
        System.out.println("Overall Shortest Word: " + overallShortestWord);

    }

//    private synchronized void printEachUpdate(List<Object> result) {
//        System.out.println("File: " + result.get(0) + " Total Word Count: " + result.get(1) + " Occurrences of 'is': " + result.get(2)
//                + " Occurrences of 'are': " + result.get(3) + " Occurrences of 'you': " + result.get(4) + " Longest Word: " + result.get(5) + " Shortest Word: " + result.get(6));
//        System.out.println("Overall Longest Word: " + overallLongestWord + " Overall Shortest Word: " + overallShortestWord);
//        System.out.println("--------------------------------------------");
//    }

    public List<File> getTextFiles() {
        return textFiles;
    }

//    public void setUpdateSpeed(int updateSpeed) {
//        this.updateSpeed = updateSpeed;
//    }
// function run to make each thread take special file in folder 
// and this function  is starting point of threads
    @Override
    public void run() {

        int myfile;

        synchronized (this) {
            myfile = fileInProcessing;
            fileInProcessing++;
        }
        try {
            List<Object> result = findLongestAndShortestWordsWithCounts(textFiles.get(myfile));
            printFileInfo(result);
        } catch (InterruptedException ex) {
            //Do Nothing For now
        }
        synchronized (this) {
            doneJob++;
            if (doneJob == textFiles.size()) {
                myGUI.updateStatus();
            }
        }
    }
//number of threads=number of files
    // if i have 5 files then you have 5 threads to enter each file
    public void readyProcessing() {
        int threadCount = 0;
        threads = new ArrayList<>();
        for (File file : textFiles) {
            Thread thread = new Thread(this, "" + threadCount);
            threadCount++;
            threads.add(thread);
            System.out.println("threadcreated");
        }
    }
//go to each thread and work it 
    public void startProcessing() {
        for (Thread thread : threads) {
            thread.start();
            System.out.println("threadstart");
        }
    }

//    public void stopProcessing() {
//        for (Thread thread : threads) {
//            thread.interrupt();
//        }
//    }

//    public void waitProcessing() {
//        for (Thread thread : threads) {
//            try {
//                thread.join();
//            } catch (InterruptedException ex) {
//                //do nothing for now
//            }
//        }
//    }

    //Main function
    public static void main(String[] args) {
        try {
            // Example usage:
            boolean scanSubDir = false;  // Set to true if you want to scan subdirectories
            String dirPath = "C:\\Users\\ym980\\OneDrive\\Desktop\\os2 project\\projecttest";  // Provide the actual directory path
            FileProcessing fileScanner = new FileProcessing(scanSubDir, dirPath);
            fileScanner.initializeTextFiles();
            fileScanner.readyProcessing();
            fileScanner.startProcessing();
        } catch (Exception ex) {
            Logger.getLogger(FileProcessing.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
