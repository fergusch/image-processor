import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.RescaleOp;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Random;

import javax.imageio.ImageIO;

class ColorPos {
	
	private Color color;
	private double position;
	
	public ColorPos(Color color, double position) {
		this.color = color;
		this.position = (position > 1.0) ? 1.0 : (position < 0) ? 0 : position;
	}
	
	public ColorPos(int red, int green, int blue, double position) {
		this.color = new Color(red, green, blue);
		this.position = (position > 1.0) ? 1.0 : (position < 0) ? 0 : position;
	}
	
	public ColorPos(int red, int green, int blue, int alpha, double position) {
		this.color = new Color(red, green, blue, alpha);
		this.position = (position > 1.0) ? 1.0 : (position < 0) ? 0 : position;
	}
	
	public Color getColor() {
		return this.color;
	}
	
	public double getPosition() {
		return this.position;
	}
	
}

/**
 * Module for basic image processing functions.
 * All functions return a new ImageProcessor
 * object for easy chaining of functions.
 * @author @fergusch
 */
public class ImageProcessor {
	
	/**
	 * Directions for flipping or rotating an image.
	 */
	public static enum Direction {
		HORIZONTAL, VERTICAL, CLOCKWISE, COUNTER_CLOCKWISE;
	}
	
	// base image to be used by this object
	private BufferedImage baseImage;
	
	/**
	 * Initializes a new ImageProcessingModule using the given File object.
	 * @param f - File object containing local file to read
	 */
	public ImageProcessor(File f) {
		
		// load image from file
		BufferedImage img = null;
		try {
			img = ImageIO.read(f);
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.baseImage = img;
	}
	
	/**
	 * Initializes a new ImageProcessingModule using the given remote URL.
	 * @param url - URL object
	 */
	public ImageProcessor(URL url) {
		
		try {
			
			// load the image from the URL into a BufferedImage
			URLConnection uc = url.openConnection();
			uc.setRequestProperty("User-Agent", "Mozilla/5.0");
		    this.baseImage = ImageIO.read(uc.getInputStream());
		    
		} catch (IOException e) {
			e.printStackTrace();
		}
	    
	}
	
	/**
	 * Create a new ImageProcessingModule using a BufferedImage.
	 * This constructor is called by all methods in this class to
	 * allow easier chaining of functions.
	 * @param baseImage - baseImage constructed by method
	 * @param type - type from previous baseImage
	 */
	private ImageProcessor(BufferedImage baseImage) {
		this.baseImage = baseImage;
	}
	
	/**
	 * Returns the base image contained by this module.
	 * @return BufferedImage
	 */
	public BufferedImage extract() {
		return this.baseImage;
	}
	
	public void saveAs(File f, String format) throws IOException {
		ImageIO.write(this.baseImage, format, f);
	}
	
	/**
	 * Creates an empty BufferedImage with the dimensions of this module's base image.
	 * @return empty BufferedImage object
	 */
	private BufferedImage createEmptyCopy() {
		
		// return an empty BufferedImage with the same dimensions as the base image
		return new BufferedImage(this.baseImage.getWidth(), this.baseImage.getHeight(), this.baseImage.getType());
		
	}
	
	/**
	 * Overlay an image on top of another.
	 * @param image - image to overlay onto this object's baseImage
	 * @param x - x-translation
	 * @param y - y-translation
	 * @param theta - degrees to rotate image
	 * @param scale - scale factor
	 * @param alpha - overlay opacity
	 * @return ImageProcessingModule
	 */
	public ImageProcessor overlay(BufferedImage image, Point point, double theta, double scale, float alpha) {
		
		// scale the image by given factor
		AffineTransform transformation = AffineTransform.getScaleInstance(scale, scale);
		
		// rotate the overlay image by theta degrees
		transformation.concatenate(AffineTransform.getRotateInstance(Math.toRadians(theta), 
				(image.getWidth() / 2) + (point.x/scale), (image.getHeight() / 2) + (point.y/scale)));
		
		// translate the overlay image to (x,y)
		transformation.concatenate(AffineTransform.getTranslateInstance(point.x/scale, point.y/scale));
		
		// create resulting image
		BufferedImage result = this.createEmptyCopy();
		
		// create graphics object for resulting image
		Graphics2D g = result.createGraphics();
		
		// fill initial rectangle
		g.setComposite(AlphaComposite.Clear);
		g.fillRect(0, 0, this.baseImage.getWidth(), this.baseImage.getHeight());
		
		// draw base image onto result
		g.setComposite(AlphaComposite.SrcOver);
		g.drawImage(this.baseImage, 0, 0, null);
		
		// draw the overlay image
		g.setComposite(AlphaComposite.SrcOver.derive(alpha));
		g.drawImage(image, transformation, null);
		
		// dispose of graphics
		g.dispose();
		
		return new ImageProcessor(result);
		
	}
	
	/**
	 * Scales the samples of each pixel by the given scale factor and offset.
	 * Really just an alias of RescaleOp.filter()
	 * @param scaleFactor - scale factor to use
	 * @param offset - offset to apply after scaling
	 * @return ImageProcessingModule
	 */
	public ImageProcessor scaleSamples(float scaleFactor, float offset) {
		
		// create RescaleOp object
		RescaleOp ro = new RescaleOp(scaleFactor, offset, null);
		
		// create copy of base image
		BufferedImage result = this.baseImage;
		
		// apply scaling operation
		ro.filter(this.baseImage, result);
		
		return new ImageProcessor(result);
		
	}
	
	/**
	 * Tint an image using a certain color.
	 * @param color - color to tint image with
	 * @return - ImageProcessingModule
	 */
	public ImageProcessor tint(Color color, float amount) {
		
		// create resulting image
		BufferedImage result = this.createEmptyCopy();

		// create graphics object for resulting image
		Graphics2D g2 = result.createGraphics();

		// fill initial rectangle
		g2.setComposite(AlphaComposite.Clear);
		g2.fillRect(0, 0, baseImage.getWidth(), baseImage.getHeight());
		
		// iterate through pixel matrix
	    for (int x = 0; x < baseImage.getWidth(); x++) {
	    	
	        for (int y = 0; y < baseImage.getHeight(); y++) {
	            
	        	// get pixel color
	        	Color pixel = new Color(baseImage.getRGB(x, y), true);
	        	
	        	// if pixel is fully transparent, skip to next one
	        	if (pixel.getAlpha() == 0) continue;
	        	
	        	// calculate the new value of each pixel using linear interpolation
	        	int r = (int) (pixel.getRed() + (color.getRed() - pixel.getRed()) * amount);
	        	int g = (int) (pixel.getGreen() + (color.getGreen() - pixel.getGreen()) * amount);
	        	int b = (int) (pixel.getBlue() + (color.getBlue() - pixel.getBlue()) * amount);
	        	
	        	// store original alpha
	        	int a = pixel.getAlpha();
	        	
	        	int rgba = (a << 24) | (r << 16) | (g << 8) | b;
	            
	            // draw tinted pixel onto resulting image
	            result.setRGB(x, y, rgba);
	            
	        }
	        
	    }
	    
	    return new ImageProcessor(result);
	}
	
	public ImageProcessor gradientMap(double amount, ColorPos... colors) {
		
		// create resulting image
		BufferedImage result = this.createEmptyCopy();

		// create graphics object for resulting image
		Graphics2D g2 = result.createGraphics();

		// fill initial rectangle
		g2.setComposite(AlphaComposite.Clear);
		g2.fillRect(0, 0, baseImage.getWidth(), baseImage.getHeight());
		
		// iterate through pixel matrix
	    for (int x = 0; x < baseImage.getWidth(); x++) {
	    	
	        for (int y = 0; y < baseImage.getHeight(); y++) {
	            
	        	// get pixel color
	        	Color pixel = new Color(baseImage.getRGB(x, y), true);
	        	
	        	// if pixel is fully transparent, skip to next one
	        	if (pixel.getAlpha() == 0) continue;
	        	
	        	int r = pixel.getRed();
	        	int g = pixel.getGreen();
	        	int b = pixel.getBlue();
	        	
	        	double l = (r * 0.299) + (g * 0.587) + (b * 0.114);
	        	l /= 255;
	        	
	        	Color mapColor = null;
	        	for (int i = 0; i < colors.length - 1; i++) {
	        		
	        		Color color1 = null;
	        		Color color2 = null;
	        		
	        		if (colors[i].getPosition() <= l && l <= colors[i+1].getPosition()) {
	        			
	        			color1 = colors[i].getColor();
	        			color2 = colors[i+1].getColor();
	        			
	        			mapColor = new Color(
		        				(int) (color1.getRed() + (color2.getRed() - color1.getRed()) * l),
		        				(int) (color1.getGreen() + (color2.getGreen() - color1.getGreen()) * l),
		        				(int) (color1.getBlue() + (color2.getBlue() - color1.getBlue()) * l)
		        			);
	        			
	        			break;
	        		}
	        		
	        	}
	        	
	        	r = (int) (r + (mapColor.getRed() - r) * amount);
	        	g = (int) (g + (mapColor.getGreen() - g) * amount);
	        	b = (int) (b + (mapColor.getBlue() - b) * amount);
	        	
	        	int a = pixel.getAlpha();
	        	
	        	int rgba = (a << 24) | (r << 16) | (g << 8) | b;
	            
	            // draw tinted pixel onto resulting image
	            result.setRGB(x, y, rgba);
	            
	            
	            
	        }
	        
	    }
	    
	    return new ImageProcessor(result);
		
	}
	
	/**
	 * Convert an image to grayscale.
	 * @return ImageProcessingModule
	 */
	public ImageProcessor grayscale() {
		
		// create resulting image
		BufferedImage result = this.createEmptyCopy();
		
		// apply color conversion op
		ColorConvertOp op = new ColorConvertOp(this.baseImage.getColorModel().getColorSpace(), ColorSpace.getInstance(ColorSpace.CS_GRAY),  null);
		op.filter(this.baseImage, result);
		
		return new ImageProcessor(result);
		
	}
	
	/**
	 * Add colored noise to an image.
	 * @param percentage - double between 0 and 1.0
	 * @return ImageProcessingModule
	 */
	public ImageProcessor addNoise(boolean monochrome, double percentage) {
		
		// if amount given is less than one, set noise factor to 1 / amount
		// if it's greater than 1, set the factor to 1
		double noiseFactor = (percentage <= 1) ? (1 / percentage) : 1;
		
		// create random object
		Random random = new Random();
		
		// create resulting image
		BufferedImage result = this.createEmptyCopy();

		// create graphics object for resulting image
		Graphics2D g2 = result.createGraphics();

		// fill initial rectangle
		g2.setComposite(AlphaComposite.Clear);
		g2.fillRect(0, 0, baseImage.getWidth(), baseImage.getHeight());
		
		// iterate through pixel matrix
	    for (int x = 0; x < baseImage.getWidth(); x++) {
	    	
	        for (int y = 0; y < baseImage.getHeight(); y++) {
	        	
	        	Color pixel = new Color(this.baseImage.getRGB(x, y), true);

	        	// if pixel is fully transparent, skip to next one
	        	if (pixel.getAlpha() == 0) continue;
	        	
	        	Color rand = null;
	        	if (!monochrome) {
	        		rand = new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
	        	} else {
	        		int val = random.nextInt(256);
	        		rand = new Color(val, val, val);
	        	}
	        	
	        	int r = (int) (pixel.getRed() + (rand.getRed() - pixel.getRed()) * noiseFactor);
	        	int g = (int) (pixel.getGreen() + (rand.getGreen() - pixel.getGreen()) * noiseFactor);
	        	int b = (int) (pixel.getBlue() + (rand.getBlue() - pixel.getBlue()) * noiseFactor);
	            
	            // preserve pixel alpha
	            int a = pixel.getAlpha();
	            
	            // bitwise color conversion
	            int rgba = (a << 24) | (r << 16) | (g << 8) | b;
	            
	            // draw tinted pixel onto resulting image
	            result.setRGB(x, y, rgba);
	            
	        }
	        
	    }
	    
	    
	    
	    return new ImageProcessor(result);
		
	}
	
	/**
	 * Produces the negative of an image.
	 * @return ImageProcessingModule
	 */
	public ImageProcessor negative() {
		
		// create resulting image
		BufferedImage result = this.createEmptyCopy();
		
		// iterate through pixel matrix
		for (int x = 0; x < this.baseImage.getWidth(); x++) {
			
            for (int y = 0; y < this.baseImage.getHeight(); y++) {
            	
            	// get pixel color
	            Color pixel = new Color(baseImage.getRGB(x, y), true);
	            
	            // invert colors (255 - value)
	            int r = 255 - pixel.getRed();
	            int g = 255 - pixel.getGreen();
	            int b = 255 - pixel.getBlue();
	            
	            // set pixel alpha
	            int a = pixel.getAlpha();
	            
	            // bitwise color conversion
	            int rgba = (a << 24) | (r << 16) | (g << 8) | b;
                
                // set new value
                result.setRGB(x, y, rgba);
                
            }
            
        }
		
		return new ImageProcessor(result);
		
	}
	
	/**
	 * Resizes an image to the given dimensions.
	 * @param width - width to resize to
	 * @param height - height to resize to
	 * @return - ImageProcessingModule
	 */
	public ImageProcessor resizeTo(Integer width, Integer height) {
		
		double scaleX = 1.0;
		double scaleY = 1.0;
		
		// if width or height is null, keep aspect ratio
		if (width == null) {
			scaleY = ((double) height) / ((double) this.baseImage.getHeight());
			scaleX = scaleY;
		} else if (height == null) {
			scaleX = ((double) width) / ((double) this.baseImage.getWidth());
			scaleY = scaleX;
		} else {
			scaleX = ((double) width) / ((double) this.baseImage.getWidth());
			scaleY = ((double) height) / ((double) this.baseImage.getHeight());
		}
		
		// define scale operation
		AffineTransform scaleOperation = AffineTransform.getScaleInstance(scaleX, scaleY);
		
		// create resulting image
		BufferedImage result = new BufferedImage((int) (this.baseImage.getWidth() * scaleX), 
				(int) (this.baseImage.getHeight() * scaleY),
				this.baseImage.getType());
		
		// create graphics object for resulting image
		Graphics2D g2 = result.createGraphics();
		
		// fill result with image
		g2.setComposite(AlphaComposite.Clear);
		g2.fillRect(0, 0, (int) (this.baseImage.getWidth() * scaleX), 
				(int) (this.baseImage.getHeight() * scaleY));
		
		// draw image onto empty image
		g2.setComposite(AlphaComposite.SrcOver);
		g2.drawImage(this.baseImage, scaleOperation, null);
		
		// dispose of graphics
		g2.dispose();
		
		return new ImageProcessor(result);
	}
	
	/**
	 * Mirror an image in the given direction.
	 * @param direction - the {@link Direction} to flip the image.
	 * @return ImageProcessingModule
	 */
	public ImageProcessor mirror(Direction direction) { // test me before continuing
		
		// create empty result image
		BufferedImage result = this.createEmptyCopy();
		
		// if direction is horizontal
		if (direction == Direction.HORIZONTAL) {
			
			// iterate through pixel matrix
			for (int y = 0; y < this.baseImage.getHeight(); y++) {
				
				for (int x = 0; x < this.baseImage.getWidth(); x++) {
					
					// get pixel at (width - 1 - x, y)
					int pixel = this.baseImage.getRGB(this.baseImage.getWidth() - 1 - x, y);
					
					// set result pixel at (x,y)
					result.setRGB(x, y, pixel);
					
				}
				
			}
			
		// if direction is vertical
		} else if (direction == Direction.VERTICAL) {
			
			// iterate through pixel matrix
			for (int x = 0; x < this.baseImage.getWidth(); x++) {
				
				for (int y = 0; y < this.baseImage.getHeight(); y++) {
					
					// get pixel at (x, height - 1 - y)
					int pixel = this.baseImage.getRGB(x, this.baseImage.getHeight() - 1 - y);
					
					// set result pixel at (x,y)
					result.setRGB(x, y, pixel);
					
				}
				
			}
			
		}
		
		return new ImageProcessor(result);
	}
	
	/**
	 * Crop an image (alias of BufferedImage.getSubimage())
	 * @param x - x-coordinate
	 * @param y - y-coordinate
	 * @param w - width
	 * @param h - height
	 * @return ImageProcessingModule
	 */
	public ImageProcessor crop(int x, int y, int w, int h) {
		
		// return cropped image
		return new ImageProcessor(this.baseImage.getSubimage(x, y, w, h));
		
	}
	
	/**
	 * Rotates an image 90 degrees clockwise or counter-clockwise.
	 * @param direction - the {@link Direction} to rotate the image in.
	 * @return ImageProcessingModule
	 */
	public ImageProcessor rotate(Direction direction) {
		
		// determine rotation amount
		double rotation = 0;
		double rotationAnchor = 0;
		
		if (direction == Direction.CLOCKWISE) {
			rotation = 90.0;
			rotationAnchor = this.baseImage.getHeight() / 2;
		} else if (direction == Direction.COUNTER_CLOCKWISE) {
			rotation = -90.0;
			rotationAnchor = this.baseImage.getWidth() / 2;
		}
		
		// create empty result image with swapped dimensions
		BufferedImage result = new BufferedImage(this.baseImage.getHeight(), 
				this.baseImage.getWidth(), this.baseImage.getType());
		Graphics2D g2 = result.createGraphics();
		
		// fill initial rectangle
		g2.setComposite(AlphaComposite.Clear);
		g2.fillRect(0, 0, this.baseImage.getHeight(), this.baseImage.getWidth());
		
		AffineTransform tx = AffineTransform.getRotateInstance(
				Math.toRadians(rotation), rotationAnchor, rotationAnchor);
		
		// draw rotated image
		g2.setComposite(AlphaComposite.SrcOver);
		g2.drawImage(this.baseImage, tx, null);
		
		// dispose of graphics
		g2.dispose();
		
		return new ImageProcessor(result);
		
	}
	
	/**
	 * Draw text on the image.
	 * @param text - Text to draw
	 * @param point - Coordinates to draw the text at.
	 * @param font - Font object to use
	 * @param color - Text color
	 * @return ImageProcessingModule
	 */
	public ImageProcessor drawText(String text, Point point, Font font, Color color) {
		
		// create resulting image
		BufferedImage result = this.createEmptyCopy();
		
		// create graphics object for resulting image
		Graphics2D g2d = result.createGraphics();
		
		// fill initial rectangle
		g2d.setComposite(AlphaComposite.Clear);
		g2d.fillRect(0, 0, this.baseImage.getWidth(), this.baseImage.getHeight());
		
		// draw base image onto result
		g2d.setComposite(AlphaComposite.SrcOver);
		g2d.drawImage(this.baseImage, 0, 0, null);
		
		Graphics g = result.getGraphics();
		
		// prepare to draw text
		g.setColor(color);
		g.setFont(font);
		
		// draw text
		g.drawString(text, point.x, point.y);
		g.dispose();
		
		return new ImageProcessor(result);
		
	}
	
}
