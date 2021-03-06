import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class WordCounter {

    // The following are the ONLY variables we will modify for grading.
    // The rest of your code must run with no changes.
    public static final Path FOLDER_OF_TEXT_FILES  = Paths.get("src/test_files3"); // path to the folder where input text files are located
    public static final Path WORD_COUNT_TABLE_FILE = Paths.get("src/final_file/chebeoi.txt"); // path to the output plain-text (.txt) file
    public static final int  NUMBER_OF_THREADS     = 3;                // max. number of threads to spawn

    private static Map<String, Map<String,Integer>>mapOfAllFileMaps = new TreeMap<String, Map<String,Integer>>(); //global master map of maps (<name of file, contents of file>)
    public static int filesLeftToAssign = FOLDER_OF_TEXT_FILES.toFile().listFiles().length;
    private static int threadCountDown = NUMBER_OF_THREADS;
    private static int whichFiles = 0;
    private static ArrayList<threadMaker>myThreads = new ArrayList<threadMaker>();
    private static Map<String, Integer>mapOfWords = new TreeMap<String, Integer>();

    /**
     * statically called within run() and determines which files a thread will be assigned with
     *
     * @param fa the number of files this thread is to be assigned
     * @param fc the empty TreeMap <String, String> which holds the content of each file
     */
    public static synchronized void assignFilesToThread(int fa, Map<String, String> fc){
        int stopForThread = whichFiles + fa;
        for(int f = whichFiles; f < stopForThread; f++){ //this loop determines which files the thread is to work on and adds them to a list
            String content = "";
            try {
                content = new String(Files.readAllBytes(Paths.get(FOLDER_OF_TEXT_FILES.toFile().listFiles()[f].getAbsolutePath()))); //reads the content of a file into String?
            }
            catch(Exception e){
            }
            fc.put(FOLDER_OF_TEXT_FILES.toFile().listFiles()[f].getName().substring(0, FOLDER_OF_TEXT_FILES.toFile().listFiles()[f].getName().length() - 4), content); //add the file's content into the map (gives me a map<filename, file content>)
            whichFiles++;
        }
    }

    /**
     * traverses mapOfAllFiles and appends every word into mapOfWords (used for total count/length of longest word)
     *
     * @return the length of the longest word encountered (for file output formatting)
     */
    public static int makeMapFindLongest(){
        int longestWordLength = 0;
        int sizeOfFileMap;
        String fileKey;
        for(int ft = 0; ft < mapOfAllFileMaps.size(); ft++){
            sizeOfFileMap = ((Map<String, Integer>)(mapOfAllFileMaps.values().toArray()[ft])).size();
            for(int fc = 0; fc < sizeOfFileMap; fc++){
                fileKey = (String)((Map<String, Integer>)(mapOfAllFileMaps.values().toArray()[ft])).keySet().toArray()[fc];
                if(mapOfWords.containsKey(fileKey)){
                    if(fileKey.length() > longestWordLength)
                        longestWordLength = fileKey.length();
                    mapOfWords.put(fileKey, mapOfWords.get(fileKey) + 1);
                }
                else{
                    if(fileKey.length() > longestWordLength)
                        longestWordLength = fileKey.length();
                    mapOfWords.put(fileKey, 1);
                }
            }
        }
        return longestWordLength;
    }

    static class threadMaker extends Thread{
        private Map<String, String>fileContent;
        private Map<String, Map<String, Integer>>counts = new TreeMap<String, Map<String, Integer>>(); //COUNTS IS HERE!!!
        private int filesAssigned = 0;

        /**
         * constructor for thread object (assigns the number of files/"tasks" to a thread)
         */
        public threadMaker(){
            this.fileContent = new TreeMap<String, String>(); //a TreeMap which holds each file as a their name and a String representation of their contents
            if(filesLeftToAssign >= 2 * threadCountDown) { //this if-else block determines how many files to add to each thread but not WHICH files
                this.filesAssigned = filesLeftToAssign / threadCountDown;
                filesLeftToAssign -= filesAssigned;
                threadCountDown--;
            }
            else {
                this.filesAssigned = 1;
                filesLeftToAssign--;
                threadCountDown--;
            }
            myThreads.add(this);
        }

        /**
         * iterates through each of this thread's assigned files and counts each word occurrence, placing the counts
         * into a map of this thread's file's word counts (also prints out the time of this thread's execution)
         */
        @Override
        public void run() {
            long startRun = System.nanoTime();

            assignFilesToThread(filesAssigned, fileContent);
            for(int iterate = 0; iterate < fileContent.size(); iterate++){ //iterates for each file in the thread
                Map<String,Integer>countingWords = new TreeMap<String, Integer>();
                String currentFile = (String) fileContent.values().toArray()[iterate];
                String[]fileWords = currentFile.toLowerCase().replaceAll("[^a-zA-Z ]", "").split(" ");
                for(int fw = 0; fw < fileWords.length; fw++){ //traverses each word of the file and counts the occurrences of words
                    if(countingWords.containsKey(fileWords[fw])){ //if the word exists in the map for the current file
                        countingWords.put(fileWords[fw], countingWords.get(fileWords[fw]) + 1); //increment the existing map value for that word
                    }
                    else{ //if the word doesn't exist in the map for the current file
                        countingWords.put(fileWords[fw], 1);
                    }
                }
                counts.put((String) fileContent.keySet().toArray()[iterate], countingWords); //puts the map<filename, Map<word, counts>> into this thread's map
            }
            long endRun = System.nanoTime();
            System.out.println("Thread runtime: " + (endRun - startRun) + " ");
        }
    }

    public static void main(String...args) throws InterruptedException {
        long totalStart = System.nanoTime();

        /**
         * loops through each thread (if needed) and starts each thread. Runs normally if NUMBER_OF_THREADS is 0
         */
        if(NUMBER_OF_THREADS != 0) {
            int limit;
            if (FOLDER_OF_TEXT_FILES.toFile().listFiles().length < NUMBER_OF_THREADS)
                limit = FOLDER_OF_TEXT_FILES.toFile().listFiles().length;
            else
                limit = NUMBER_OF_THREADS;
            long start1 = System.nanoTime();
            for (int t = 0; t < limit; t++) {
                threadMaker wordCounter = new threadMaker();
                wordCounter.start();
            }
            long end1 = System.nanoTime();
            System.out.println("Kickstart time: " + (end1 - start1) + " ");
        }
        else{
            long startRun = System.nanoTime();
            Map<String, String>fileContent = new TreeMap<String, String>();
            assignFilesToThread(filesLeftToAssign, fileContent);
            for(int iterate = 0; iterate < fileContent.size(); iterate++){ //iterates for each file in the thread
                Map<String,Integer>countingWords = new TreeMap<String, Integer>();
                String currentFile = (String) fileContent.values().toArray()[iterate];
                String[]fileWords = currentFile.toLowerCase().replaceAll("[^a-zA-Z ]", "").split(" ");
                for(int fw = 0; fw < fileWords.length; fw++){ //traverses each word of the file and counts the occurrences of words
                    if(countingWords.containsKey(fileWords[fw])){ //if the word exists in the map for the current file
                        countingWords.put(fileWords[fw], countingWords.get(fileWords[fw]) + 1); //increment the existing map value for that word
                    }
                    else{ //if the word doesn't exist in the map for the current file
                        countingWords.put(fileWords[fw], 1);
                    }
                }
                mapOfAllFileMaps.put((String) fileContent.keySet().toArray()[iterate], countingWords); //puts the map<filename, Map<word, counts>> into this thread's map
            }
            long endRun = System.nanoTime();
            System.out.println("Main runtime: " + (endRun - startRun) + " ");
        }

        /**
         * joins all the threads together after each thread has completed its task
         */
        for(int congregate = 0; congregate < myThreads.size(); congregate++){
            myThreads.get(congregate).join();
        }

        /**
         * combines each thread's map of maps into 1 large maps of all file maps
         */
        for(int combine = 0; combine < myThreads.size(); combine++){
            mapOfAllFileMaps.putAll(myThreads.get(combine).counts); //add individual maps into global list of maps
        }

        int widthOfFirstColumn = makeMapFindLongest() + 1; //stores the width of the first column (1 larger than largest word)

        /**
         * finds the longest name of a file and stores as the width of each column
         */
        File[] arrOfFiles = FOLDER_OF_TEXT_FILES.toFile().listFiles(); //creates a list of the files
        int fileColumnWidth = 0;
        for(int f = 0; f < arrOfFiles.length; f++){ //obtains the length of the fileColumnWidth (for the end)
            if(arrOfFiles[f].getName().substring(0, arrOfFiles[f].getName().length() - 4).length() > fileColumnWidth)
                fileColumnWidth = arrOfFiles[f].getName().length() - 4;
        }
        fileColumnWidth++;

        /**
         * writing to output file
         */
        FileWriter fw;
        try {
            if (Files.exists(Paths.get(String.valueOf(WORD_COUNT_TABLE_FILE)))) {
                fw = new FileWriter(String.valueOf(WORD_COUNT_TABLE_FILE), false);
            } else {
                File outputFile = new File(String.valueOf(WORD_COUNT_TABLE_FILE));
                fw = new FileWriter(outputFile, true);
            }
            //used to create top row of output.txt
            String topRow = String.format("%"+widthOfFirstColumn +"s", " ");
            int spacesLeft;
            for(int fn = 0; fn < mapOfAllFileMaps.size(); fn++) {
                spacesLeft = fileColumnWidth - ((String)(mapOfAllFileMaps.keySet().toArray()[fn])).length();
                topRow += ((String)mapOfAllFileMaps.keySet().toArray()[fn]) + String.format("%"+spacesLeft+"s", " ");
            }
            spacesLeft = fileColumnWidth - 5;
            topRow += "total" + String.format("%"+spacesLeft+"s", " ");
            fw.write(topRow);
            fw.flush();
            fw.append(System.lineSeparator());

            String row;
            for(int words = 0; words < mapOfWords.size(); words++){
                row = mapOfWords.keySet().toArray()[words] + String.format("%"+(widthOfFirstColumn - ((String)(mapOfWords.keySet().toArray()[words])).length())+"s", " ");
                for(int f = 0; f < mapOfAllFileMaps.size(); f++){
                    spacesLeft = fileColumnWidth - (String.valueOf(mapOfWords.values().toArray()[f])).length();
                    if((mapOfAllFileMaps.get(mapOfAllFileMaps.keySet().toArray()[f])).get(mapOfWords.keySet().toArray()[words]) == null)
                        row += (0 + String.format("%"+spacesLeft+"s", " "));
                    else
                        row += (mapOfAllFileMaps.get(mapOfAllFileMaps.keySet().toArray()[f])).get(mapOfWords.keySet().toArray()[words]) + String.format("%"+spacesLeft+"s", " ");
                }
                row += mapOfWords.get(mapOfWords.keySet().toArray()[words]) + String.format("%"+(fileColumnWidth - String.valueOf(mapOfWords.get(mapOfWords.keySet().toArray()[words])).length())+"s", " ");
                fw.write(row);
                fw.flush();
                fw.append(System.lineSeparator());
            }
        }
        catch (IOException ioe){
            System.out.println("Could not create output.txt");
        }

        long totalEnd = System.nanoTime();
        System.out.print("Total program runtime: " + (totalEnd - totalStart));
    }
}