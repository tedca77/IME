package IC;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import static org.apache.commons.imaging.formats.tiff.constants.TiffTagConstants.*;

public class ImageProcessing {
    //Below is the Code (which will convert PPM(byte array to Buffered image and you can save buffered image to the file)

    // Method Call

    //BufferedImage image = ppm(width, height, 255, byte[]);
    //Method Definition

    static public BufferedImage ppm(int width, int height, int maxcolval, byte[] data){
        BufferedImage image=new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
        if(maxcolval<256){
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
        }
        else{


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
        }
        return image;
    }
    public static BufferedImage getScaledImage(BufferedImage src, int w, int h){
        int original_width = src.getWidth();
        int original_height = src.getHeight();


        int new_width = original_width;
        int new_height = original_height;

        // first check if we need to scale width
        if (original_width > w) {
            //scale width to fit
            new_width = w;
            //scale height to maintain aspect ratio
            new_height = (new_width * original_height) / original_width;
        }

        // then check if we need to scale even with the new height
        if (new_height > h) {
            //scale height to fit instead
            new_height = h;
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

        String newName;
        BufferedImage img ;
        try {
            try
            {
                img = ImageIO.read(file);
            }
            catch(Exception e2)
            {
                // will create a file that can be used for getting a thumbnail
                try {
                 //   newName = ImageConversion.convertNonJPGFormats(file, tempDir);
                    img = ImageIO.read(file);
                }
                catch(Exception e)
                {
                    return false;
                }
            }
            BufferedImage imgThumb;
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
            javaxt.io.Image javaxtImage=new javaxt.io.Image(imgThumb);
            if(orientation.equals( ORIENTATION_VALUE_ROTATE_90_CW))
            {
                javaxtImage.rotateClockwise();
            }
            else if(orientation.equals(ORIENTATION_VALUE_ROTATE_270_CW))
            {
                javaxtImage.rotateCounterClockwise();
            }
            else if(orientation.equals(ORIENTATION_VALUE_ROTATE_180))
            {
              javaxtImage.rotate(180.0d);
            }
            File outputfile = new File(tempDir+"/"+thumbName);
          //  ImageIO.write(imgThumb, "jpg", outputfile);
            javaxtImage.saveAs(outputfile);
            //upload

            return true;

        } catch (Exception e) {
            System.out.println("Failed to create thumbnail"+e);
            return false;
        }
    }
}


