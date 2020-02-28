package net.semanticmetadata.lire.sampleapp;

import net.semanticmetadata.lire.imageanalysis.features.GlobalFeature;
import net.semanticmetadata.lire.imageanalysis.features.LireFeature;
import net.semanticmetadata.lire.imageanalysis.features.global.CEDD;
import net.semanticmetadata.lire.imageanalysis.features.global.cedd.*;
import net.semanticmetadata.lire.utils.ImageUtils;
import net.semanticmetadata.lire.utils.SerializationUtils;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.ImageObserver;
import java.util.Arrays;

public class ColorHistogram implements GlobalFeature {
    private double T0;
    private double T1;
    private double T2;
    private double T3;
    private boolean Compact = false;
    protected byte[] histogram = new byte[144];
    int tmp;
    private double Result;
    private double Temp1;
    private double Temp2;
    private double TempCount1;
    private double TempCount2;
    private double TempCount3;
    private ColorHistogram tmpFeature;
    private double iTmp1;
    private double iTmp2;

    public ColorHistogram(double Th0, double Th1, double Th2, double Th3, boolean CompactDescriptor) {
        this.T0 = Th0;
        this.T1 = Th1;
        this.T2 = Th2;
        this.T3 = Th3;
        this.Compact = CompactDescriptor;
    }

    public ColorHistogram() {
        this.T0 = 14.0D;
        this.T1 = 0.68D;
        this.T2 = 0.98D;
        this.T3 = 0.98D;
    }

    public void extract(BufferedImage image) {
        image = ImageUtils.get8BitRGBImage(image);
        Fuzzy10Bin Fuzzy10 = new Fuzzy10Bin(false);
        Fuzzy24Bin Fuzzy24 = new Fuzzy24Bin(false);
        RGB2HSV HSVConverter = new RGB2HSV();
        int[] HSV = new int[3];
        double[] Fuzzy10BinResultTable = new double[10];
        double[] Fuzzy24BinResultTable = new double[24];
        double[] CEDD = new double[144];
        int width = image.getWidth();
        int height = image.getHeight();
        double[][] ImageGrid = new double[width][height];
        double[][] PixelCount = new double[2][2];
        int[][] ImageGridRed = new int[width][height];
        int[][] ImageGridGreen = new int[width][height];
        int[][] ImageGridBlue = new int[width][height];
        int NumberOfBlocks = -1;
        if (Math.min(width, height) >= 80) {
            NumberOfBlocks = 1600;
        }

        if (Math.min(width, height) < 80 && Math.min(width, height) >= 40) {
            NumberOfBlocks = 400;
        }

        if (Math.min(width, height) < 40) {
            NumberOfBlocks = -1;
        }

        int Step_X = 2;
        int Step_Y = 2;
        if (NumberOfBlocks > 0) {
            Step_X = (int)Math.floor((double)width / Math.sqrt((double)NumberOfBlocks));
            Step_Y = (int)Math.floor((double)height / Math.sqrt((double)NumberOfBlocks));
            if (Step_X % 2 != 0) {
                --Step_X;
            }

            if (Step_Y % 2 != 0) {
                --Step_Y;
            }
        }

        int[] Edges = new int[6];
        MaskResults MaskValues = new MaskResults();
        Neighborhood PixelsNeighborhood = new Neighborhood();

        int pixel;
        for(pixel = 0; pixel < 144; ++pixel) {
            CEDD[pixel] = 0.0D;
        }

        BufferedImage image_rgb = new BufferedImage(width, height, 4);
        image_rgb.getGraphics().drawImage(image, 0, 0, (ImageObserver)null);
        int[] pixels = ((DataBufferInt)image_rgb.getRaster().getDataBuffer()).getData();

        for(int x = 0; x < width; ++x) {
            for(int y = 0; y < height; ++y) {
                pixel = pixels[y * width + x];
                int b = pixel >> 16 & 255;
                int g = pixel >> 8 & 255;
                int r = pixel & 255;
                ImageGridRed[x][y] = r;
                ImageGridGreen[x][y] = g;
                ImageGridBlue[x][y] = b;
                ImageGrid[x][y] = 0.114D * (double)b + 0.587D * (double)g + 0.299D * (double)r;
            }
        }

        int[] CororRed = new int[Step_Y * Step_X];
        int[] CororGreen = new int[Step_Y * Step_X];
        int[] CororBlue = new int[Step_Y * Step_X];
        int[] CororRedTemp = new int[Step_Y * Step_X];
        int[] CororGreenTemp = new int[Step_Y * Step_X];
        int[] CororBlueTemp = new int[Step_Y * Step_X];
        int TempSum = 0;

        double Max = 0.0D;
        int TemoMAX_X = Step_X * (int)Math.floor((double)(image.getWidth() >> 1));
        int TemoMAX_Y = Step_Y * (int)Math.floor((double)(image.getHeight() >> 1));
        if (NumberOfBlocks > 0) {
            TemoMAX_X = Step_X * (int)Math.sqrt((double)NumberOfBlocks);
            TemoMAX_Y = Step_Y * (int)Math.sqrt((double)NumberOfBlocks);
        }

        int T;
        int i;
        for(int y = 0; y < TemoMAX_Y; y += Step_Y) {
            for(int x = 0; x < TemoMAX_X; x += Step_X) {
                int MeanRed = 0;
                int MeanGreen = 0;
                int MeanBlue = 0;
                PixelsNeighborhood.Area1 = 0.0D;
                PixelsNeighborhood.Area2 = 0.0D;
                PixelsNeighborhood.Area3 = 0.0D;
                PixelsNeighborhood.Area4 = 0.0D;
                Edges[0] = -1;
                Edges[1] = -1;
                Edges[2] = -1;
                Edges[3] = -1;
                Edges[4] = -1;
                Edges[5] = -1;

                for(T = 0; T < 2; ++T) {
                    for(i = 0; i < 2; ++i) {
                        PixelCount[T][i] = 0.0D;
                    }
                }

                TempSum = 0;

                for(T = y; T < y + Step_Y; ++T) {
                    for(i = x; i < x + Step_X; ++i) {
                        CororRed[TempSum] = ImageGridRed[i][T];
                        CororGreen[TempSum] = ImageGridGreen[i][T];
                        CororBlue[TempSum] = ImageGridBlue[i][T];
                        CororRedTemp[TempSum] = ImageGridRed[i][T];
                        CororGreenTemp[TempSum] = ImageGridGreen[i][T];
                        CororBlueTemp[TempSum] = ImageGridBlue[i][T];
                        ++TempSum;
                        if (i < x + Step_X / 2 && T < y + Step_Y / 2) {
                            PixelsNeighborhood.Area1 += ImageGrid[i][T];
                        }

                        if (i >= x + Step_X / 2 && T < y + Step_Y / 2) {
                            PixelsNeighborhood.Area2 += ImageGrid[i][T];
                        }

                        if (i < x + Step_X / 2 && T >= y + Step_Y / 2) {
                            PixelsNeighborhood.Area3 += ImageGrid[i][T];
                        }

                        if (i >= x + Step_X / 2 && T >= y + Step_Y / 2) {
                            PixelsNeighborhood.Area4 += ImageGrid[i][T];
                        }
                    }
                }

                PixelsNeighborhood.Area1 = (double)((int)(PixelsNeighborhood.Area1 * (4.0D / (double)(Step_X * Step_Y))));
                PixelsNeighborhood.Area2 = (double)((int)(PixelsNeighborhood.Area2 * (4.0D / (double)(Step_X * Step_Y))));
                PixelsNeighborhood.Area3 = (double)((int)(PixelsNeighborhood.Area3 * (4.0D / (double)(Step_X * Step_Y))));
                PixelsNeighborhood.Area4 = (double)((int)(PixelsNeighborhood.Area4 * (4.0D / (double)(Step_X * Step_Y))));
                MaskValues.Mask1 = Math.abs(PixelsNeighborhood.Area1 * 2.0D + PixelsNeighborhood.Area2 * -2.0D + PixelsNeighborhood.Area3 * -2.0D + PixelsNeighborhood.Area4 * 2.0D);
                MaskValues.Mask2 = Math.abs(PixelsNeighborhood.Area1 * 1.0D + PixelsNeighborhood.Area2 * 1.0D + PixelsNeighborhood.Area3 * -1.0D + PixelsNeighborhood.Area4 * -1.0D);
                MaskValues.Mask3 = Math.abs(PixelsNeighborhood.Area1 * 1.0D + PixelsNeighborhood.Area2 * -1.0D + PixelsNeighborhood.Area3 * 1.0D + PixelsNeighborhood.Area4 * -1.0D);
                MaskValues.Mask4 = Math.abs(PixelsNeighborhood.Area1 * Math.sqrt(2.0D) + PixelsNeighborhood.Area2 * 0.0D + PixelsNeighborhood.Area3 * 0.0D + PixelsNeighborhood.Area4 * -Math.sqrt(2.0D));
                MaskValues.Mask5 = Math.abs(PixelsNeighborhood.Area1 * 0.0D + PixelsNeighborhood.Area2 * Math.sqrt(2.0D) + PixelsNeighborhood.Area3 * -Math.sqrt(2.0D) + PixelsNeighborhood.Area4 * 0.0D);
                Max = Math.max(MaskValues.Mask1, Math.max(MaskValues.Mask2, Math.max(MaskValues.Mask3, Math.max(MaskValues.Mask4, MaskValues.Mask5))));
                MaskValues.Mask1 /= Max;
                MaskValues.Mask2 /= Max;
                MaskValues.Mask3 /= Max;
                MaskValues.Mask4 /= Max;
                MaskValues.Mask5 /= Max;

                T = 1;

                if (Max < this.T0) {
                    Edges[0] = 0;
                    T = 0;
                } else {
                    T = -1;
                    if (MaskValues.Mask1 > this.T1) {
                        ++T;
                        Edges[T] = 1;
                    }

                    if (MaskValues.Mask2 > this.T2) {
                        ++T;
                        Edges[T] = 2;
                    }

                    if (MaskValues.Mask3 > this.T2) {
                        ++T;
                        Edges[T] = 3;
                    }

                    if (MaskValues.Mask4 > this.T3) {
                        ++T;
                        Edges[T] = 4;
                    }

                    if (MaskValues.Mask5 > this.T3) {
                        ++T;
                        Edges[T] = 5;
                    }
                }

                for(i = 0; i < Step_Y * Step_X; ++i) {
                    MeanRed += CororRed[i];
                    MeanGreen += CororGreen[i];
                    MeanBlue += CororBlue[i];
                }

                MeanRed /= Step_Y * Step_X;
                MeanGreen /= Step_Y * Step_X;
                MeanBlue /= Step_Y * Step_X;
                HSV = HSVConverter.ApplyFilter(MeanRed, MeanGreen, MeanBlue);
                int j;
                if (!this.Compact) {
                    Fuzzy10BinResultTable = Fuzzy10.ApplyFilter((double)HSV[0], (double)HSV[1], (double)HSV[2], 2);
                    Fuzzy24BinResultTable = Fuzzy24.ApplyFilter((double)HSV[0], (double)HSV[1], (double)HSV[2], Fuzzy10BinResultTable, 2);

                    for(i = 0; i <= T; ++i) {
                        for(j = 0; j < 24; ++j) {
                            if (Fuzzy24BinResultTable[j] > 0.0D) {
                                CEDD[24 * Edges[i] + j] += Fuzzy24BinResultTable[j];
                            }
                        }
                    }
                } else {
                    Fuzzy10BinResultTable = Fuzzy10.ApplyFilter((double)HSV[0], (double)HSV[1], (double)HSV[2], 2);

                    for(i = 0; i <= T; ++i) {
                        for(j = 0; j < 10; ++j) {
                            if (Fuzzy10BinResultTable[j] > 0.0D) {
                                CEDD[10 * Edges[i] + j] += Fuzzy10BinResultTable[j];
                            }
                        }
                    }
                }
            }
        }

        double Sum = 0.0D;

        for(T = 0; T < 144; ++T) {
            Sum += CEDD[T];
        }

        for(T = 0; T < 144; ++T) {
            CEDD[T] /= Sum;
        }

        double[] qCEDD;
        if (!this.Compact) {
            qCEDD = new double[144];
            CEDDQuant quants = new CEDDQuant();
            qCEDD = quants.Apply(CEDD);
        } else {
            qCEDD = new double[60];
            CompactCEDDQuant quants = new CompactCEDDQuant();
            qCEDD = quants.Apply(CEDD);
        }

        for(i = 0; i < qCEDD.length; ++i) {
            this.histogram[i] = (byte)((int)qCEDD[i]);
        }

    }

    public double getDistance(LireFeature vd) {
        if (!(vd instanceof CEDD)) {
            throw new UnsupportedOperationException("Wrong descriptor.");
        } else {
            this.tmpFeature = (ColorHistogram)vd;
            if (this.tmpFeature.histogram.length != this.histogram.length) {
                throw new UnsupportedOperationException("Histogram lengths or color spaces do not match");
            } else {
                this.Result = 0.0D;
                this.Temp1 = 0.0D;
                this.Temp2 = 0.0D;
                this.TempCount1 = 0.0D;
                this.TempCount2 = 0.0D;
                this.TempCount3 = 0.0D;

                int i;
                for(i = 0; i < this.tmpFeature.histogram.length; ++i) {
                    this.Temp1 += (double)this.tmpFeature.histogram[i];
                    this.Temp2 += (double)this.histogram[i];
                }

                if (this.Temp1 == 0.0D && this.Temp2 == 0.0D) {
                    return 0.0D;
                } else if (this.Temp1 != 0.0D && this.Temp2 != 0.0D) {
                    for(i = 0; i < this.tmpFeature.histogram.length; ++i) {
                        this.iTmp1 = (double)this.tmpFeature.histogram[i] / this.Temp1;
                        this.iTmp2 = (double)this.histogram[i] / this.Temp2;
                        this.TempCount1 += this.iTmp1 * this.iTmp2;
                        this.TempCount2 += this.iTmp2 * this.iTmp2;
                        this.TempCount3 += this.iTmp1 * this.iTmp1;
                    }

                    this.Result = 100.0D - 100.0D * (this.TempCount1 / (this.TempCount2 + this.TempCount3 - this.TempCount1));
                    return this.Result;
                } else {
                    return 100.0D;
                }
            }
        }
    }

    private double scalarMult(double[] a, double[] b) {
        double sum = 0.0D;

        for(int i = 0; i < a.length; ++i) {
            sum += a[i] * b[i];
        }

        return sum;
    }

    public byte[] getByteHistogram() {
        return this.histogram;
    }

    public byte[] getByteArrayRepresentation() {
        int position = -1;

        int length;
        for(length = 0; length < this.histogram.length; ++length) {
            if (position == -1) {
                if (this.histogram[length] == 0) {
                    position = length;
                }
            } else if (position > -1 && this.histogram[length] != 0) {
                position = -1;
            }
        }

        if (position < 0) {
            position = 143;
        }

        length = (position + 1) / 2;
        if ((position + 1) % 2 == 1) {
            length = position / 2 + 1;
        }

        byte[] result = new byte[length];

        for(int i = 0; i < result.length; ++i) {
            this.tmp = this.histogram[i << 1] << 4;
            this.tmp |= this.histogram[(i << 1) + 1];
            result[i] = (byte)(this.tmp - 128);
        }

        return result;
    }

    public void setByteArrayRepresentation(byte[] in) {
        this.setByteArrayRepresentation(in, 0, in.length);
    }

    public void setByteArrayRepresentation(byte[] in, int offset, int length) {
        if (length << 1 < this.histogram.length) {
            Arrays.fill(this.histogram, length << 1, this.histogram.length, (byte)0);
        }

        for(int i = offset; i < offset + length; ++i) {
            this.tmp = in[i] + 128;
            this.histogram[(i - offset << 1) + 1] = (byte)(this.tmp & 15);
            this.histogram[i - offset << 1] = (byte)(this.tmp >> 4);
        }

    }

    public double[] getFeatureVector() {
        return SerializationUtils.castToDoubleArray(this.histogram);
    }

    public String getFeatureName() {
        return "CEDD";
    }

    public String getFieldName() {
        return "CEDD";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(this.histogram.length * 2 + 25);
        byte[] var2 = this.histogram;
        int var3 = var2.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            byte aData = var2[var4];
            sb.append(aData);
            sb.append(' ');
        }

        return "CEDD{" + sb.toString().trim() + "}";
    }
}