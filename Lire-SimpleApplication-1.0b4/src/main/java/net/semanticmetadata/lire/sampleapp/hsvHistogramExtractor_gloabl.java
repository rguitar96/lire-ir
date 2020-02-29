package net.semanticmetadata.lire.sampleapp;

import net.semanticmetadata.lire.imageanalysis.features.GlobalFeature;
import net.semanticmetadata.lire.imageanalysis.features.LireFeature;
import net.semanticmetadata.lire.imageanalysis.features.global.SimpleColorHistogram;
import net.semanticmetadata.lire.imageanalysis.utils.ColorConversion;
import net.semanticmetadata.lire.utils.ConversionUtils;
import net.semanticmetadata.lire.utils.ImageUtils;
import net.semanticmetadata.lire.utils.MetricsUtils;
import net.semanticmetadata.lire.utils.SerializationUtils;
import org.apache.commons.math3.ml.distance.EarthMoversDistance;

import java.awt.image.*;
import java.util.Arrays;

public class hsvHistogramExtractor_gloabl implements GlobalFeature {

    //reusable variable to store the R, G and B channel values for a pixel
    private int[] rgbPixel = new int[3];

    //int array to store the hsv feature histogram for the image
    private int[] featureHistogram;

    //Hyper parameter to store the number of bins for the histogram
    private final int  NUM_BINS = 1000;

    public hsvHistogramExtractor_gloabl() {
        this.featureHistogram = new int[NUM_BINS];
    }

    public void extract(BufferedImage image) {
        image = ImageUtils.get8BitRGBImage(image);

        //initialize features histogram
        Arrays.fill(this.featureHistogram, 0);

        //iterate through image pixels
        for(int x = 0; x < image.getWidth(); ++x) {
            for(int y = 0; y < image.getHeight(); ++y) {
                image.getRaster().getPixel(x, y, this.rgbPixel);

                //convert to HSV
                ColorConversion.rgb2hsv(this.rgbPixel[0], this.rgbPixel[1], this.rgbPixel[2], this.rgbPixel);
                // get bucket and increment count
                this.featureHistogram[this.getBin(this.rgbPixel)]++;

            }
        }

        //normalize histogram
        int maxValue = 0;

        for(int i = 0; i < featureHistogram.length; ++i) {
            maxValue = Math.max(featureHistogram[i], maxValue);
        }

        for(int j = 0; j < featureHistogram.length; ++j) {
            featureHistogram[j] = featureHistogram[j] * 255 / maxValue;
        }
    }

    public byte[] getByteArrayRepresentation() {
        return SerializationUtils.toByteArray(this.featureHistogram);
    }

    public void setByteArrayRepresentation(byte[] in) {
        this.featureHistogram = SerializationUtils.toIntArray(in);
    }

    public void setByteArrayRepresentation(byte[] in, int offset, int length) {
        this.featureHistogram = SerializationUtils.toIntArray(in, offset, length);
    }

    public double[] getFeatureVector() {
        return ConversionUtils.toDouble(this.featureHistogram);
    }

    private int getBin(int[] pixel) {

        int qH = (int) Math.floor(pixel[0] / 11.25);
        if(qH == 32){
            qH --;
        }

        int qV = pixel[1] / 90;
        if(qV == 4){
            qV--;
        }

        int qS = pixel[2] / 25;
        if(qS == 4){
            qS--;
        }

        return qH * 16 + qV * 4 + qS;

    }

    public double getDistance(LireFeature vd) {
        if (!(vd instanceof SimpleColorHistogram)) {
            throw new UnsupportedOperationException("Wrong descriptor.");
        } else {
            hsvHistogramExtractor_gloabl searchImageFeatures = (hsvHistogramExtractor_gloabl)vd;

            //Euclidean Dist
            double diff_square_sum = 0.0;

            for(int i = 0; i < this.featureHistogram.length; i++){
                diff_square_sum += (this.featureHistogram[i] - searchImageFeatures.featureHistogram[i])
                                    *(this.featureHistogram[i] - searchImageFeatures.featureHistogram[i]);
                }

            return Math.sqrt(diff_square_sum);


//           return  new EarthMoversDistance().compute(intArrayToDouble(this.featureHistogram),
//                   intArrayToDouble(searchImageFeatures.featureHistogram));

        }
    }

    private double[] intArrayToDouble(int[] ia){
        double[] di = new double[ia.length];

        for(int i = 0; i < ia.length; i++){
            di[i] = ia[i];
        }

        return di;
    }


    public String getFeatureName() {
        return "ColorHistogramExtactor";
    }

    public String getFieldName() {
        return "ColorHistogramExtactor";
    }
}
