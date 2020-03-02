# Mushroom identification using CBIR

## Overview

This application is a demonstration of how Content-Based Image Retrieval (CBIR) can be used to identify the species of a mushroom. This is done using a RGB color space histogram to describe the features of an image then using euclidean distance to determine how close a search image is to those indexed. 

## Implementation

This project is based on the [LIRE Sample Application](http://www.itec.uni-klu.ac.at/~mlux/lire-release/). To carry out this logic the  LIRE Java library is used to orchestrate the logic calling a custom made class,  ColorHistogramExtractor_global, which performs the extraction of the RGB color from an image and generates a normalized histogram. To ensure the likelihood of the the colour being captured is only of the mushroom CVSURF is used as a keypoint detector to focus only on “blob” shapes in the image, i.e. the cap of the mushroom. 

The application works by reading in the files found in the “data/testImages” folder. It will iterate through each treating it as a search image and the remaining images as the index ones. The default version of this code contains a dataset of 26 mushrooms made up of 13 species with 2 images each. Upon completing the searching action the top 10 matching image identifiers will be stored in a text file with the name <name_of_search_image>_yyy_MM_dd.txt in the “data/searchResults” folder.

To view the code for each section click the following links:

[ColorHistogramExtractor_global](https://github.com/rguitar96/lire-ir/blob/master/Lire-SimpleApplication-1.0b4/src/main/java/net/semanticmetadata/lire/sampleapp/ColorHistogramExtractor_gloabl.java)

[Main Class](https://github.com/rguitar96/lire-ir/blob/master/Lire-SimpleApplication-1.0b4/src/main/java/net/semanticmetadata/lire/sampleapp/IndexingAndSearchWithLocalFeatures.java)


## How to run

To run the application open in Intellij and go to build.gradle runIndexLocalSLocalFeat task definition and define where binaries of your OS. Then go to java src/java folder and right click on IndexingAndSearchWithLocalFeatures to execute its main method.

**Note:** Each time the application is run the “data/searchResults” folder is emptied. 
