/*
 *    Copyright 2021 E.M.Carroll
 *    ==========================
 *    This file is part of Image Metadata Enhancer (IME).
 *
 *     Image Metadata Enhancer is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Image Metadata Enhancer is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Image Metadata Enhancer.  If not, see <https://www.gnu.org/licenses/>.
 */
package IME;
import org.apache.commons.io.FilenameUtils;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

public class ImageConversion {
    // private String fileName;
    private String requester = "";
    private int imageStartHeight=700;
    private int maxHeight;
    private int maxWidth;
    private float maxScale;
    private String logoFileLocation;
    private int maxLogoHeight;
    private int maxLogoWidth;

    // fonts

    private double leftMargin = 30; //note that zero is on the left of the page...
    // constructor
    public ImageConversion() {
        // this.fileName = fileName;


        maxHeight = 550;
        maxWidth = 530;
        maxScale = 0.1f;
        maxLogoHeight = 100;
        maxLogoWidth = 100;
    }
    public File createImage(File file,String thumbName, String image,String tempDir,String operatingSystem)
            throws IOException {

        try {


            //File file = new File(fileName);


            try
            {
                // this will work with basic formats

                //	Uploader.logger.info("This will convert with basic image formats:"+outputFileName);
            }
            catch(Exception e)
            {
                ImageIO.scanForPlugins();
                try
                {
                    String[] names=ImageIO.getReaderFormatNames();
                    for(String s : names)
                    {
                        //			Uploader.logger.info("ImageIO names - "+s);

                    }
                    Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("TIFF");
                    while (readers.hasNext()) {
                        //		    Uploader.logger.info("reader: " + readers.next());
                    }

                    // try a standard conversion e.g. TIFF using the TGwelve Monkeys library

                    //		Uploader.logger.info("Trying to read as TIFF file "+outputFileName);
                    BufferedImage bim = ImageIO.read(new File(tempDir+"/"+image));
                  //  myImage = LosslessFactory.createFromImage(pdf, bim);
                    //	Uploader.logger.info("read as TIFF file "+outputFileName);
                }
                catch(Exception e2)
                {
                    //	Uploader.logger.info("Standard conversion:"+e2);
                    //	Uploader.logger.info("Was not able to convert usin gstandard Twelve Monkey's library++++"+image);
                    image=convertNonJPGFormats(file,tempDir);


                }

            }

            float scale =0.0f;
            //	PDXObjectImage myImage = new PDJpeg(pdf, fileName);
            try
            {
                    int imageWidth = 0;
                     int imageHeight = 0;

           //    int imageWidth = myImage.getWidth();
           //     int imageHeight = myImage.getHeight();

                scale = (float) imageWidth / (float) maxWidth;

                if (scale < maxScale) {
                    //		Uploader.logger.info("Photo scaling - limiting zoom - scale is: "	+ scale);
                    scale = maxScale;
                }

                int newHeight = (int) (imageHeight / scale);

                if (newHeight > maxHeight) {
                    //	Uploader.logger.info("Adjusting scale as height too big - old scale "	+ scale);
                    scale = scale* ((float)newHeight/(float)maxHeight);
                    //	Uploader.logger.info("Adjusting scale as height too big - new scale: "+ scale);
                    newHeight = (int) (imageHeight / scale);
                }

                int newWidth = (int) (imageWidth / scale);

            }
            catch(Exception e)
            {

                // 	Uploader.logger.info("++++++++++++++FAILED TO ADD IMAGE TO PDF  for+++++"+image);
            }


            //save file to an output stream
            OutputStream os = new FileOutputStream(tempDir+"/"+thumbName);

            os.close();


        } catch (FileNotFoundException fnfex) {
            //	Uploader.logger.info("++++++++++++++NO IMAGE TO CONVERT TO PDF +++++"+image);

        }
        File file2 = new File(tempDir+"/"+thumbName);
        return file;
    }


public static Boolean isRaw(String fname)
{
    try
    {
        String[] pics="TIFF~TIF".toLowerCase().split("~");
        String result=FilenameUtils.getExtension(fname).toLowerCase();
        for(int kk=0;kk<pics.length;kk++)
        {
            if(result.equals(pics[kk]))
            {
                return true;
            }
        }
        return false;
    }
    catch(Exception e)
    {
        return false;
    }
}
    public static String convertNonJPGFormats(File file,String root)
    {
        String fileName="";
        try {
            fileName = file.getName();
        }
        catch(Exception e)
        {
            System.out.println("Cant find filename");
        }
        String ext = FilenameUtils.getExtension(fileName);
        if(isRaw(fileName))
        {
            System.out.println("this is a raw file:"+fileName);
            try
            {
                System.out.println("Converting RAW file"+fileName + ", Root:"+root);


                String outputFileName=FilenameUtils.getBaseName(fileName)+".ppm";
                String newFileName=FilenameUtils.getBaseName(fileName)+"_"+ext+".ppm";
                //	///runDCRAW("dcraw",fileName,root);
                System.out.println("Renaming RAW output file to"+newFileName );
                //	Uploader.renameFileInTemp(outputFileName,newFileName,root);
                fileName=newFileName;
            }
            catch(Exception e2)
            {
                System.out.println("Could not run DCRAW"+e2);

            }

        }
        try
        {
            System.out.println("This is not raw looking for plug ins");
            ImageIO.scanForPlugins();

            Boolean ppmFound=false;
            String[] names=ImageIO.getReaderFormatNames();
            for(String s : names)
            {
                System.out.println("ImageIO names - "+s);
                if(s.equals("PPM"))
                {
                    System.out.println("*********************Found  PPM - "+s);
                    ppmFound=true;
                }
            }

            if(ppmFound==true)
            {
                System.out.println("Can convert to PPM :"+fileName);

                File file2 = new File(root+"/"+fileName);
                BufferedImage image = ImageIO.read(file2);
                File output = new File(root+"/"+FilenameUtils.getBaseName(fileName)+"_"+FilenameUtils.getExtension(fileName)+".jpg");
                //Write the image to the destination as a JPG
                ImageIO.write(image, "jpg", output);
                System.out.println("Conversion successful:"+fileName);
                return output.getName();
            }
            else
            {
                System.out.println("PPM format not available :"+fileName);
                Path path = Paths.get(root+"/"+fileName);


                File file2 = new File(root+"/"+fileName);
                BufferedImage image = ImageIO.read(file2);
                byte[] data = Files.readAllBytes(path);
                BufferedImage imagePPM = ImageProcessing.ppm(image.getWidth(), image.getHeight(), 255, data);

                System.out.println("Read successful:"+fileName);
                //Create a file for the output
                File output = new File(root+"/"+FilenameUtils.getBaseName(fileName)+"_"+ FilenameUtils.getExtension(fileName)+".jpg");


                //Write the image to the destination as a JPG
                ImageIO.write(imagePPM, "jpg", output);
                System.out.println("Conversion successful:"+fileName);
                return output.getName();
            }

        }
        catch(Exception e)
        {
            System.out.println("Error converting to jpg - libraries may not be installed correctly"+e);
            return "";

        }
    }



}
