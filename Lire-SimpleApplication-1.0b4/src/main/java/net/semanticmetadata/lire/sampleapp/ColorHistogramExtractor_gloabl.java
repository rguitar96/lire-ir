//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package net.semanticmetadata.lire.sampleapp;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import net.semanticmetadata.lire.imageanalysis.features.GlobalFeature;
import net.semanticmetadata.lire.imageanalysis.features.LireFeature;
import net.semanticmetadata.lire.utils.ConversionUtils;
import net.semanticmetadata.lire.utils.ImageUtils;
import net.semanticmetadata.lire.utils.SerializationUtils;

public class ColorHistogramExtractor_gloabl implements GlobalFeature {
    private int[] rgbPixel = new int[3];
    private int[] featureHistogram;

    public ColorHistogramExtractor_gloabl() {
        this.featureHistogram = new int[64];
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
                //ColorConversion.rgb2hsv(this.rgbPixel[0], this.rgbPixel[1], this.rgbPixel[2], this.rgbPixel);

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

        //Get bin for HSV
//        int qH = (int) Math.floor(pixel[0] / 11.25);
//        if(qH == 32){
//            qH --;
//        }
//
//        int qV = pixel[1] / 90;
//        if(qV == 4){
//            qV--;
//        }
//
//        int qS = pixel[2] / 25;
//        if(qS == 4){
//            qS--;
//        }
//
//        return qH * 16 + qV * 4 + qS;

        //Get bin for RGB
        return  (int)Math.round((double)pixel[2] / 85.0D) +
               (int)Math.round((double)pixel[1] / 85.0D) * 4 +
               (int)Math.round((double)pixel[0] / 85.0D) * 4 * 4;

    }

    public double getDistance(LireFeature vd) {
        if (!(vd instanceof ColorHistogramExtractor_gloabl)) {
            throw new UnsupportedOperationException("Wrong descriptor.");
        } else {
            ColorHistogramExtractor_gloabl searchImageFeatures = (ColorHistogramExtractor_gloabl)vd;

            //Euclidean Dist
            double diff_square_sum = 0.0;

            for(int i = 0; i < this.featureHistogram.length; i++){
                diff_square_sum += (this.featureHistogram[i] - searchImageFeatures.featureHistogram[i])
                        *(this.featureHistogram[i] - searchImageFeatures.featureHistogram[i]);
            }

            return Math.sqrt(diff_square_sum);

            //Earth Movers Distance
//             return  new EarthMoversDistance().compute(this.featureHistogram,
//                   searchImageFeatures.featureHistogram);

            //Chi-Squared
            //           return MetricsUtils.chisquare(this.featureHistogram,
            //                  searchImageFeatures.featureHistogram);


            //Normalized Intersection
//            int sum = 0;
//
//            for(int i = 0; i < this.featureHistogram.length; i++){
//                sum += Math.min(this.featureHistogram[i], searchImageFeatures.featureHistogram[i]);
//            }
//
//            return sum / Math.min(sum(this.featureHistogram), sum(this.featureHistogram));

        }
    }


    public String getFeatureName() {
        return "ColorHistogramExtactor";
    }

    public String getFieldName() {
        return "ColorHistogramExtactor";
    }
}
