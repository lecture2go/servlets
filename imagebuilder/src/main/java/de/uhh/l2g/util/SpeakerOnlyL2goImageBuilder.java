package de.uhh.l2g.util;

import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/**
 * Builds a Lecture2Go (lecture2go.uni-hamburg.de)-specific metadata-image to be encoded with a video
 * This is the "speaker-slides" version
 */
public class SpeakerOnlyL2goImageBuilder extends L2goImageBuilder {
	
	/**
	 * The constructor method, sets a default backgroundimage-stream and sizing values
	 * @param author the metadata (author), which will be written on the image
	 * @param institution the metadata (institution), which will be written on the image
	 * @param title the metadata (title), which will be written on the image
	 * @param series the metadata (series), which will be written on the image
	 * @param date the metadata (date), which will be written on the image
	 */
	public SpeakerOnlyL2goImageBuilder(String author, String institution, String title, String series, String date) {
		super(author, institution, title, series, date);
			
		// the image width (in pixels) is used to determine the maximum text length per line
		this.imageWidth  = 1920;
		
		// the distance from the image-border to the start of text in pixels
		this.offsetLeft = 440;
		this.offsetRight = 347;
	}

	/**
	 * Builds the image from the given metadata and backgroundimage
	 * @return the drawn image 
	 */
    public BufferedImage buildImage()  {    
    	// the image is prepared for editing
    	this.prepareImageCreation();
    	
    	int fontSize = (int) (this.getfontSize());
		this.lineHeight = fontSize + fontSize/10;

        // we declare some position variables
        int authorYPosition;
		int institutionYPosition;
		int institutionXPosition;
        int titleYPosition;
        int seriesYPosition;
    	
    	// series and date are handled as one
        String seriesAndDate = getSeriesAndDate();
   	    
    	// the author position is fixed
    	authorYPosition = 42;
	    this.g.setFont(this.fontBold);
	    int widthAuthor = this.g.getFontMetrics().stringWidth(this.author);
	    this.drawString(g, this.author, this.offsetLeft, authorYPosition, this.maxTextWidth, 1);

	    // used lines for a drawn string
	    int usedLines;
	    // maximum lines to draw for a string
	    int maxLines;
	    
	    // a prefix to fill the gap between author and institution if necessary
	    String institutionPrefix = ", ";

	    this.g.setFont(this.fontItalic);
	    
	    int widthInstitution = this.g.getFontMetrics().stringWidth(this.institution);
	    // if the author and institution fits in one line, do it (separated by prefix)
        if (widthAuthor + widthInstitution <= this.maxTextWidth) {
            institutionYPosition = 42;
            institutionXPosition = this.offsetLeft + widthAuthor;
            usedLines = 1;
        } else {
        	institutionPrefix = "";
        	institutionYPosition = 84;
            institutionXPosition = this.offsetLeft;
            usedLines = 2;
        }
	   	this.drawString(this.g, institutionPrefix + this.institution, institutionXPosition, institutionYPosition, this.maxTextWidth, 1);
    	// the title position
	    if (usedLines <= 1) {
	    	titleYPosition = 96;
	    	maxLines = 2;
	    } else {
	    	titleYPosition = 133;
	    	maxLines = 1;
	    }
	   	
	  	this.g.setFont(this.fontRegular);
	  	usedLines = this.drawString(this.g, this.title, this.offsetLeft, titleYPosition, this.maxTextWidth, maxLines);
	  	
	    // the series/date position is fixed
  		seriesYPosition = 184;
	  	this.g.setFont(this.fontItalic);

	   	this.drawString(this.g, seriesAndDate, this.offsetLeft, seriesYPosition, this.maxTextWidth, 1);
	   	
	    // clean up
	    this.g.dispose();
    	
    	return image;
    }

}