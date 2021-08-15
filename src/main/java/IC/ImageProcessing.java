package IC;


import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static org.apache.commons.imaging.formats.tiff.constants.TiffTagConstants.ORIENTATION_VALUE_ROTATE_270_CW;
import static org.apache.commons.imaging.formats.tiff.constants.TiffTagConstants.ORIENTATION_VALUE_ROTATE_90_CW;

public class ImageProcessing {
    //Below is the Code (which will convert PPM(byte array to Buffered image and you can save buffered image to the file)

    // Method Call

    //BufferedImage image = ppm(width, height, 255, byte[]);
    //Method Definition

    static public BufferedImage ppm(int width, int height, int maxcolval, byte[] data){
        if(maxcolval<256){
            BufferedImage image=new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
            int r,g,b,k=0,pixel;
            if(maxcolval==255){                                      // don't scale
                for(int y=0;y<height;y++){
                    for(int x=0;(x<width)&&((k+3)<data.length);x++){
                        r=data[k++] & 0xFF;
                        g=data[k++] & 0xFF;
                        b=data[k++] & 0xFF;
                        pixel=0xFF000000+(r<<16)+(g<<8)+b;
                        image.setRGB(x,y,pixel);
                    }
                }//
            }
            else{
                for(int y=0;y<height;y++){
                    for(int x=0;(x<width)&&((k+3)<data.length);x++){
                        r=data[k++] & 0xFF;r=((r*255)+(maxcolval>>1))/maxcolval;  // scale to 0..255 range
                        g=data[k++] & 0xFF;g=((g*255)+(maxcolval>>1))/maxcolval;
                        b=data[k++] & 0xFF;b=((b*255)+(maxcolval>>1))/maxcolval;
                        pixel=0xFF000000+(r<<16)+(g<<8)+b;
                        image.setRGB(x,y,pixel);
                    }
                }
            }
            return image;
        }
        else{


            BufferedImage image=new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
            int r,g,b,k=0,pixel;
            for(int y=0;y<height;y++){
                for(int x=0;(x<width)&&((k+6)<data.length);x++){
                    r=(data[k++] & 0xFF)|((data[k++] & 0xFF)<<8);r=((r*255)+(maxcolval>>1))/maxcolval;  // scale to 0..255 range
                    g=(data[k++] & 0xFF)|((data[k++] & 0xFF)<<8);g=((g*255)+(maxcolval>>1))/maxcolval;
                    b=(data[k++] & 0xFF)|((data[k++] & 0xFF)<<8);b=((b*255)+(maxcolval>>1))/maxcolval;
                    pixel=0xFF000000+(r<<16)+(g<<8)+b;
                    image.setRGB(x,y,pixel);
                }
            }
            return image;
        }
    }
    public static BufferedImage resizeImage(final Image image, int width, int height) {
        final BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        final Graphics2D graphics2D = bufferedImage.createGraphics();
        graphics2D.setComposite(AlphaComposite.Src);
        //below three lines are for RenderingHints for better image quality at cost of higher processing time
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.drawImage(image, 0, 0, width, height, null);
        graphics2D.dispose();
        return bufferedImage;
    }
    public static BufferedImage getScaledImage(BufferedImage src, int w, int h){
        int original_width = src.getWidth();
        int original_height = src.getHeight();
        int bound_width = w;
        int bound_height = h;
        int new_width = original_width;
        int new_height = original_height;

        // first check if we need to scale width
        if (original_width > bound_width) {
            //scale width to fit
            new_width = bound_width;
            //scale height to maintain aspect ratio
            new_height = (new_width * original_height) / original_width;
        }

        // then check if we need to scale even with the new height
        if (new_height > bound_height) {
            //scale height to fit instead
            new_height = bound_height;
            //scale width to maintain aspect ratio
            new_width = (new_height * original_width) / original_height;
        }

        BufferedImage resizedImg = new BufferedImage(new_width, new_height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = resizedImg.createGraphics();
        g2.setBackground(Color.WHITE);
        g2.clearRect(0,0,new_width, new_height);
        g2.drawImage(src, 0, 0, new_width, new_height, null);
        g2.dispose();
        return resizedImg;
    }
    public static Boolean createThumbFromPicture(File file,String tempDir,String thumbName,Integer width,Integer height,Integer orientation)
    {
        String newName="";
        BufferedImage img = null;
        try {
            try
            {
                img = ImageIO.read(file);
            }
            catch(Exception e2)
            {
                // will create a file that can be used for getting a thumbnail
                try {
                    newName = ImageConversion.convertNonJPGFormats(file, tempDir);
                    img = ImageIO.read(file);
                }
                catch(Exception e)
                {
                    return false;
                }
            }
            BufferedImage imgThumb=null;
            try
            {
                imgThumb= ImageProcessing.getScaledImage(img,width,height);
               // System.out.println("Creating thumbnamil");
            }
            catch(Exception e)
            {
                try {
                    System.out.println("Error converting image for thumbnail thumbnamil" + e);
                    newName = ImageConversion.convertNonJPGFormats(file, tempDir);
                    img = ImageIO.read(new File(tempDir + "/" + newName));
                    imgThumb = ImageProcessing.getScaledImage(img, width, height);
                }
                catch(Exception ee)
                {
                    System.out.println("Cannot create thumb for non jPEG formats"+ee);
                    return false;
                }
            }
            if(orientation.equals( ORIENTATION_VALUE_ROTATE_90_CW))
            {

            }
            else if(orientation.equals(ORIENTATION_VALUE_ROTATE_270_CW))
            {

            }
            File outputfile = new File(tempDir+"/"+thumbName);
            ImageIO.write(imgThumb, "jpg", outputfile);

            //upload

            return true;

        } catch (Exception e) {
            System.out.println("Failed to create thumbnail"+e);
            return false;
        }
    }
    public static Boolean createJPGFromPicture(String destRoot,File file,String targetName,Integer width,String areaName)
    {
        String newName="";
        String fName="";
        BufferedImage img = null;
        try {
            fName=file.getName();
            try
            {
                img = ImageIO.read(new File(destRoot+"/"+fName));
            }
            catch(Exception e2)
            {
                // will create a file that can be used for getting a thumbnail
                newName=ImageConversion.convertNonJPGFormats(file,destRoot);
                img = ImageIO.read(new File(destRoot+"/"+newName));
            }
            File outputfile = new File(destRoot+"/"+targetName);
            ImageIO.write(img, "jpg", outputfile);
            System.out.println("picture thumb is :"+targetName);
            //upload
            return true;

        } catch (IOException e) {
            return false;
        }
    }


}


