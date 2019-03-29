package de.uhh.l2g.util;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import com.twelvemonkeys.image.ResampleOp;

import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Font;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.FontFormatException;
import java.awt.RenderingHints;


/**
 * Abstract class for building Lecture2Go (lecture2go.uni-hamburg.de)-specific metadata-images to be encoded with a video
 */
public abstract class L2goImageBuilder {
	// the image dimensions in pixels, are manually set at the moment
	int imageHeight;
	int imageWidth;

	// the distance from the image-border to the start of text in pixels
	int offsetLeft;
	int offsetRight;
	
	// the position of the additional image if there is any
	int offsetLeftAdditionalImage;
	int offsetTopAdditionalImage;

	// font configuration
	InputStream fontRegularStream;
	InputStream fontBoldStream;
	InputStream fontItalicStream;
	Font fontRegular;
	Font fontBold;
	Font fontItalic;
	float fontSize;
	int maxTextWidth;

	// the height of a text line, used for text wrapping
	int lineHeight;

	// the background image used
	InputStream backgroundimageStream;
	
	// additional image
	InputStream additionalImageStream;
	
	// the metadata which will be written to the image
	String author;
	String institution;
	String title;
	String series;
	String date;
	
	BufferedImage image;
	BufferedImage additionalImage;
	Graphics2D g;
	
	/**
	 * The constructor method
	 * @param author the metadata (author), which will be written on the image
	 * @param institution the metadata (institution), which will be written on the image
	 * @param title the metadata (title), which will be written on the image
	 * @param series the metadata (series), which will be written on the image
	 * @param date the metadata (date), which will be written on the image
	 */
	public L2goImageBuilder(String author, String institution, String title, String series, String date) {
		this.author 				= author;
		this.institution 			= institution;
		this.title 					= title;
		this.series 				= series;
		this.date 					= date;
	}
	
	abstract public BufferedImage buildImage();
	
	/**
	 * This helper method loads the backgroundImage, the graphic context and
	 * sets rendering options
	 */
	protected void prepareImageCreation() {
		try {
			// load the background image on which the text will be written
			this.image = this.loadBackgroundimage();
			
			if (this.additionalImageStream != null) {
				this.additionalImage = this.loadAdditionalImage();
			}
	
			// we need the graphic context
			this.g = this.buildGraphicContextFromImage(image);
	
			// by default the rendering looks not very good, this method sets rendering options accordingly
			this.beautifyRendering(g);
			
			this.maxTextWidth = this.imageWidth - this.offsetLeft - this.offsetRight;
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
	/**
	 * Return the combined series and date string
	 * @return
	 */
	protected String getSeriesAndDate() {
		// return empty String if no series or date are given
		if (this.series.isEmpty() && this.date.isEmpty()) {
			return "";
		}
		
		String seriesAndDate = "[ ";
		        if (!this.series.isEmpty() && !this.date.isEmpty()) {
        	seriesAndDate += this.series + " / " + this.date; 
        } else if (!this.series.isEmpty() && this.date.isEmpty()) {
        	seriesAndDate += this.series; 
        } else if (this.series.isEmpty() && !this.date.isEmpty()) {
        	seriesAndDate += this.date;
        }
		seriesAndDate += " ]";
        return seriesAndDate;
	}

	/**
	 * Loads the background image from a file
	 * @return the background image
	 * @throws IOException
	 */
	public BufferedImage loadBackgroundimage() throws IOException {
		BufferedImage image = ImageIO.read(this.backgroundimageStream);
	    return image;
	}
	
	/**
	 * Loads the additional image from a file
	 * @return the additional image
	 * @throws IOException
	 */
	public BufferedImage loadAdditionalImage() throws IOException {
		BufferedImage image = ImageIO.read(this.additionalImageStream);
	    return image;
	}
	
    /**
     * Draws a string with line wrapping without a line limit
     * @param g the Graphics context
     * @param text the text to be drawn
     * @param x x-coordinate of the position, where the text is drawn
     * @param y y-coordinate of the position, where the text is drawn
     * @param maxTextWidth the maximum length of text in pixels, determines where the line is wrapped
     * @return the amount of lines which were drawn
     */
    protected int drawString(Graphics g, String text, int x, int y, int maxTextWidth) {
        return this.drawString(g, text, x, y, maxTextWidth, Integer.MAX_VALUE);
    }

    /**
     * Draws a string with line wrapping with a line limit
     * If the line limit is reached the last word in the last line will be overwritten with "..."
     * @param g the Graphics context
     * @param text the text to be drawn
     * @param x x-coordinate of the position, where the text is drawn
     * @param y y-coordinate of the position, where the text is drawn
     * @param maxTextWidth the maximum length of text in pixels, determines where the line is wrapped
     * @param maxLines the maximum amount of lines
     * @return the amount of lines which were drawn
     */
    protected int drawString(Graphics g, String text, int x, int y, int maxTextWidth, int maxLines)
    {
        FontMetrics fm = g.getFontMetrics();

        int linesCount = 0;
        int curX = x;
    	int curY = y;

        // we split lines if entry has a  manually specified line break (Attention: this does not work
    	// for unknown reasons, maybe problems with the html-decoding?)
        String[] lines = text.split("\\r?\\n");

        for (String line : lines) {
            linesCount++;

            // we split the words at a space (this might lead to no breaking or truncating
            // if a long word exceeds the given space)
            String[] words = line.split(" ");

        	for (String word : words) {
        		// check the width of the word
        		int wordWidth = fm.stringWidth(word + " ");

        		// if text exceeds the width, then move to next line
        		if (curX + wordWidth >= x + maxTextWidth) {
                    linesCount++;
                    // if there are more lines than allowed, truncate the last word
        			if (linesCount > maxLines) {
                        g.drawString("...", curX, curY);
                        break;
                    } else {
                        curY += this.lineHeight;
                        curX = x;
                    }
        		}
        		g.drawString(word, curX, curY);

        		// move over to the right for next word
        		curX += wordWidth;
        	}

            curY += this.lineHeight;
            curX = x;
        }
        return linesCount;
    }
    
    /**
     * 
     * @param g the Graphics context
     * @param additionalImage the additional image to be drawn
     * @param x x-coordinate of the position, where the image is drawn 
     * @param y y-coordinate of the position, where the text is drawn
     */
    protected void drawAdditionalImage(Graphics g, BufferedImage additionalImage, int x, int y) {
		g.drawImage(additionalImage, x, y, null);
    }

    /**
	 * A helper class to initialize a font for further use
     * @param ge the graphics-enviroment
     * @param fontStream the inputstream from a font file
     * @return the ready-to-use font
     * @throws IOException
     * @throws FontFormatException
     */
	private Font initializeFont(GraphicsEnvironment ge, InputStream fontStream) 
    	throws IOException, FontFormatException {
		Font font = Font.createFont(Font.TRUETYPE_FONT, fontStream);
		ge.registerFont(font);
		font = font.deriveFont(this.fontSize);
    	return font;
    }

	/**
	 * Build the graphic context from the given image for further image editing
	 * @param image the backgroundimage, which will be drawn upon
	 * @return the graphics-context
	 */
    protected Graphics2D buildGraphicContextFromImage (BufferedImage image) {
    	// get graphic context from image
	    Graphics g = image.getGraphics();

	    // cast the Graphic context to Graphics2D context, so we can use antialiasing
	    Graphics2D g2 = (Graphics2D) g;

	    return g2;
    }

    /**
     * By default the graphics rendering looks not very good, this method sets rendering options to 
     * beautify the text/image
     * @param g2 the graphic-context
     */
    protected void beautifyRendering(Graphics2D g2) {
  		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }
    
    /**
     * Resizes an image
     * uses external library (twelvemonkeys imageio)
     * uses original aspect ratio and targetHeight as input
     * @param img the image file
     * @param targetHeight the final height
     * @return
     */
    public BufferedImage scaleImageKeepAspectRatioFromTargetHeight(BufferedImage img, int targetHeight) {
    	// calculate the target width from the target height
    	double imageHeight = (double) img.getHeight();
    	double imageWidth = (double) img.getWidth();

    	double targetWidthDouble = targetHeight * (imageWidth/imageHeight);
    	int targetWidth = (int) Math.round(targetWidthDouble);
    	
    	return scaleImage(img, targetWidth, targetHeight);
    }
    
    /**
     * Resizes an image
     * uses external library (twelvemonkeys imageio)
     * uses original aspect ratio and targetHeight as input
     * @param img the image file
     * @param targetHeight the final height
     * @return
     */
    public BufferedImage scaleImageKeepAspectRatioFromTargetWidth(BufferedImage img, int targetWidth) {
    	// calculate the target height from the target width
    	double imageHeight = (double) img.getHeight();
    	double imageWidth = (double) img.getWidth();

    	double targetHeightDouble = targetWidth * (imageHeight/imageWidth);
    	int targetHeight = (int) Math.round(targetHeightDouble);
    	
    	return scaleImage(img, targetWidth, targetHeight);
    }
    
    /**
     * Resizes an image
     * uses external library (twelvemonkeys imageio)
     * @param img the image file
     * @param targetWidth the final width
     * @param targetHeight the final height
     * @return
     */
    public BufferedImage scaleImage(BufferedImage img, int targetWidth, int targetHeight) {
    	BufferedImageOp resampler = new ResampleOp(targetWidth, targetHeight, ResampleOp.FILTER_LANCZOS);
    	BufferedImage scaledImage = resampler.filter(img, null);
    	return scaledImage;
    }

    /**
     * Resizes an image
     * taken from from http://stackoverflow.com/questions/7951290/re-sizing-an-image-without-losing-quality
     * @param img the image file
     * @param targetWidth the final width
     * @param targetHeight the final height
     * @param hint the rendering hint for interpolation
     * @param higherQuality set to true
     * @return
     */
    public BufferedImage scaleImage(BufferedImage img, int targetWidth, int targetHeight, Object hint, boolean higherQuality) {
    	// this is the old manual resizing, it is replaced by a third party resizing (see above)
    	int type = (img.getTransparency() == Transparency.OPAQUE) ?
		BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
		BufferedImage ret = (BufferedImage)img;
		int w, h;
		if (higherQuality) {
			// Use multi-step technique: start with original size, then
			// scale down in multiple passes with drawImage()
			// until the target size is reached
			w = img.getWidth();
			h = img.getHeight();
		} else {
			// Use one-step technique: scale directly from original
			// size to target size with a single drawImage() call
			w = targetWidth;
			h = targetHeight;
		}
		
		do {
			if (higherQuality && w > targetWidth) {
				w /= 1.6;
				if (w < targetWidth) {
					w = targetWidth;
				}
			}
		
			if (higherQuality && h > targetHeight) {
				h /= 1.6;
				if (h < targetHeight) {
					h = targetHeight;
				}
			}
			
			BufferedImage tmp = new BufferedImage(w, h, type);
			Graphics2D g2 = tmp.createGraphics();
			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
			g2.drawImage(ret, 0, 0, w, h, null);
			g2.dispose();
			
			ret = tmp;
		} while (w != targetWidth || h != targetHeight);
		
		return ret;
	}
	
    /**
     * Sets the backgroundimage-stream
     * @param backgroundimagePath the backgroundimage-stream
     */
    public void setBackgroundimageStream(InputStream backgroundimageStream) {
    	this.backgroundimageStream = backgroundimageStream;
    }
    
    /**
     * Gets the backgroundimage-path
     * @return the backgroundimage-path
     */
    public InputStream getBackgroundimageStream() {
    	return this.backgroundimageStream;
    }
    
    /**
     * Sets the additionalimage-stream
     * @param backgroundimagePath the additionalImage-stream
     */
    public void setAdditionalImageStream(InputStream additionalImageStream) {
    	this.additionalImageStream = additionalImageStream;
    }
    
    /**
     * Gets the additionalImage-path
     * @return the additionalImage-path
     */
    public InputStream getAdditionalImageStream() {
    	return this.additionalImageStream;
    }

    /**
     * Sets the font-stream
     * @param fontStream the stream of the font
     * @throws FontFormatException 
     * @throws IOException 
     */
	public void setFontRegular(InputStream fontStream) throws IOException, FontFormatException {
    	GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		this.fontRegular = this.initializeFont(ge, fontStream);

		fontStream.close();
	}
	
    /**
     * Sets the font-stream
     * @param fontStream the stream of the font
     * @throws FontFormatException 
     * @throws IOException 
     */
	public void setFontBold(InputStream fontStream) throws IOException, FontFormatException {
    	GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		this.fontBold = this.initializeFont(ge, fontStream);

		fontStream.close();
	}
	
    /**
     * Sets the font-stream
     * @param fontStream the stream of the font
     * @throws FontFormatException 
     * @throws IOException 
     */
	public void setFontItalic(InputStream fontStream) throws IOException, FontFormatException {
    	GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		this.fontItalic = this.initializeFont(ge, fontStream);
		fontStream.close();
	}

	/**
	 * Sets the font size
	 * @param fontSize the size of the font
	 */
	public void setFontSize(float fontSize) {
		this.fontSize = fontSize;
	}

	/**
	 * Gets the font size
	 * @return the size of the font
	 */
	public float getfontSize() {
		return this.fontSize;
	}
	
	
}