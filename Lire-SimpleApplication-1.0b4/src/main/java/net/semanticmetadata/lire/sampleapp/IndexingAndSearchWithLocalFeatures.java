/*
 * This file is part of the LIRE project: http://lire-project.net
 * LIRE is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * LIRE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LIRE; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * We kindly ask you to refer the any or one of the following publications in
 * any publication mentioning or employing Lire:
 *
 * Lux Mathias, Savvas A. Chatzichristofis. Lire: Lucene Image Retrieval –
 * An Extensible Java CBIR Library. In proceedings of the 16th ACM International
 * Conference on Multimedia, pp. 1085-1088, Vancouver, Canada, 2008
 * URL: http://doi.acm.org/10.1145/1459359.1459577
 *
 * Lux Mathias. Content Based Image Retrieval with LIRE. In proceedings of the
 * 19th ACM International Conference on Multimedia, pp. 735-738, Scottsdale,
 * Arizona, USA, 2011
 * URL: http://dl.acm.org/citation.cfm?id=2072432
 *
 * Mathias Lux, Oge Marques. Visual Information Retrieval using Java and LIRE
 * Morgan & Claypool, 2013
 * URL: http://www.morganclaypool.com/doi/abs/10.2200/S00468ED1V01Y201301ICR025
 *
 * Copyright statement:
 * ====================
 * (c) 2002-2016 by Mathias Lux (mathias@juggle.at)
 *  http://www.semanticmetadata.net/lire, http://www.lire-project.net
 *
 */
package net.semanticmetadata.lire.sampleapp;

import net.semanticmetadata.lire.aggregators.AbstractAggregator;
import net.semanticmetadata.lire.aggregators.BOVW;
import net.semanticmetadata.lire.builders.DocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.features.local.simple.SimpleExtractor;
import net.semanticmetadata.lire.indexers.parallel.ImagePreprocessor;
import net.semanticmetadata.lire.indexers.parallel.ParallelIndexer;
import net.semanticmetadata.lire.searchers.GenericFastImageSearcher;
import net.semanticmetadata.lire.searchers.ImageSearchHits;
import net.semanticmetadata.lire.searchers.ImageSearcher;
import net.semanticmetadata.lire.utils.ImageUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * Simple class showing the process of indexing and searching for local & SIMPLE descriptors.
 * Note that you have to run it with giving the OpenCV library path, eg. "-Djava.library.path="lib\opencv"
 * @author Mathias Lux, mathias@juggle.at
 */
public class IndexingAndSearchWithLocalFeatures {


    public static void main(String[] args) throws IOException {

        //number of images to search for
        int numberOfRuns = 5;
        //date form to be appended to result file name
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");

        //loop for the number of runs
        for(int r = 0; r < numberOfRuns; r++) {

            //run set file method and get the name of the file being used for searching
            String searchFileName =  setUpImages();

            // indexing all images in "testdata"
           index("index", "data/testImages");
            // searching through the images.
            String results = search("index", "data/searchImages");

            //rest image files to the original folder
            resetImageFiles();

            //write search results to file
            PrintWriter pWriter = new PrintWriter("data/searchResults/"+searchFileName+"_"+
                    dateFormatter.format(new Date())+".txt",
                    "UTF-8");
            pWriter.println(results);
            pWriter.close();
        }

        System.out.println("Finished everything :)");
    }

    /**
     * sets up images for processing by selecting one test image at random tom be used as the search image (not included in indexing)
     * @return
     * @throws IOException
     */
    private static String setUpImages() throws IOException {

        Random rand = new Random();

        //get list of files being used
        File[] dirFiles = new File("data/testImages").listFiles();

        //randomly select one image from the test images
        int n = rand.nextInt(dirFiles.length);
        File searchImg = dirFiles[n];

        //copy the test image to the search image folder
        copyFile(searchImg, "data/searchImages");
        System.out.printf("Chosen search file: %s", searchImg.getName());
        System.out.println();

        //remove the search image from the images to be used for indexing
        Files.delete(searchImg.toPath());

        //return the name of the file being used for searching
        return FilenameUtils.removeExtension(searchImg.getName());
    }

    /**
     * returns the image being used for searching to the test image folder
     * @throws IOException
     */
    private static void resetImageFiles() throws IOException{

        //get the list of files in the search image folder
        File[] dirFiles = new File("data/searchImages").listFiles();

        //for each file in the folder move back to the test image folder and remove from the search image folder
        for(File f : dirFiles){
            copyFile(f, "data/testImages");
            Files.delete(f.toPath());
        }
    }

    /**
     * copy a file to a new folder
     * @param fileToMove the file to be moved
     * @param destFolder the folder path for the folder to be moved
     * @throws IOException
     */
    private static void copyFile(File fileToMove, String destFolder) throws IOException{
        //create a new File object to copy the file to
        File destFile = new File(destFolder, fileToMove.getName());
        //copy the file
        Files.copy(fileToMove.toPath(), destFile.toPath());
    }

    /**
     * Linear search on the indexed data.
     * @param indexPath
     * @throws IOException
     */
    public static String search(String indexPath, String searchImageFolder) throws IOException {

        StringBuilder stringBuilder = new StringBuilder();

        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));

        // make sure that this matches what you used for indexing (see below) ...
        ImageSearcher imgSearcher = new GenericFastImageSearcher(10,
                hsvHistogramExtractor_gloabl.class,
                SimpleExtractor.KeypointDetector.CVSURF,
                new BOVW(),
                128, true, reader, indexPath + ".config");

        // loop images in search folder
        File[] dirFiles = new File(searchImageFolder).listFiles();

        System.out.println("Performing search....");

        if(dirFiles.length != 0)
        {
            for(File searchIm : dirFiles)
            {
                ImageSearchHits hits = imgSearcher.search(ImageIO.read(searchIm), reader);

                for (int i=0; i<hits.length(); i++) {

                    String res = String.format("%d: %.2f  %s\n", hits.documentID(i),hits.score(i),
                            reader.document(hits.documentID(i)).getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0]);

                    stringBuilder.append(res);
                }

            }
        }
        else
        {
            System.out.printf("No files found in the search directory %s", searchImageFolder);
        }

        System.out.println("Finished searching");

        return stringBuilder.toString();
    }

    /**
     * Indexing data using OpenCV and SURF as well as CEDD and SIMPLE.
     * @param index
     * @param imageDirectory
     */
    public static void index(String index, String imageDirectory) {
        // Checking if arg[0] is there and if it is a directory.
        boolean passed = false;
        // use ParallelIndexer to index all photos from args[0] into "index".
        int numOfDocsForVocabulary = 500;
        Class<? extends AbstractAggregator> aggregator = BOVW.class;
        int[] numOfClusters = new int[] {128};

        ParallelIndexer indexer = new ParallelIndexer(DocumentBuilder.NUM_OF_THREADS, index, imageDirectory, numOfClusters, numOfDocsForVocabulary, aggregator);
        indexer.setImagePreprocessor(new ImagePreprocessor() {
            @Override
            public BufferedImage process(BufferedImage image) {
                return ImageUtils.createWorkingCopy(image);
            }
        });

        //Custom
        indexer.addExtractor(hsvHistogramExtractor_gloabl.class, SimpleExtractor.KeypointDetector.CVSURF);

        indexer.run();
        System.out.println("Finished indexing.");
    }
}
