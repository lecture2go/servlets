package de.uhh.l2g.util;

import java.awt.image.BufferedImage;

/**
 * Builds a Lecture2Go (lecture2go.uni-hamburg.de)-specific metadata-image to be encoded with a video
 * This is the "speaker-slides" version
 */
public class SpeakerSlidesL2goImageBuilder extends L2goImageBuilder {
	
	/**
	 * The constructor method, sets a default backgroundimage-stream and sizing values
	 * @param author the metadata (author), which will be written on the image
	 * @param institution the metadata (institution), which will be written on the image
	 * @param title the metadata (title), which will be written on the image
	 * @param series the metadata (series), which will be written on the image
	 * @param date the metadata (date), which will be written on the image
	 */
	public SpeakerSlidesL2goImageBuilder(String author, String institution, String title, String series, String date) {
		super(author, institution, title, series, date);
	
		// the image width (in pixels) is used to determine the maximum text length per line
		this.imageWidth  = 632;
		
		// the distance from the image-border to the start of text in pixels
		this.offsetLeft = 10;
		this.offsetRight = 10;
		
	}

	/**
	 * Builds the image from the given metadata and backgroundimage
	 * @return the drawn image 
	 */
    public BufferedImage buildImage() {
        this.prepareImageCreation();
        
    	int fontSize = (int) (this.getfontSize());
		this.lineHeight = fontSize + fontSize/15;
        
        int authorPosition;
		int institutionPosition;
        int titlePosition;
        int seriesPosition;
    	
    	String seriesAndDate = "[ " + this.series + " / " + this.date + " ]"; 
   	    	
    	// the author position is fixed
    	authorPosition = 106;
    	this.g.setFont(this.fontBold);
	    int usedLines = this.drawString(g, this.author, this.offsetLeft, authorPosition, this.maxTextWidth-130, 2);
	    // the institution position is relative to the author position
        if (usedLines <= 1) {
            institutionPosition = 140;
        } else {
            institutionPosition = 170;
        }
	    this.g.setFont(this.fontItalic);
	   	usedLines = this.drawString(this.g, this.institution, this.offsetLeft, institutionPosition, this.maxTextWidth, 2);

    	// the title position is fixed
	    titlePosition = 206;
	  	this.g.setFont(this.fontRegular);
	  	usedLines = this.drawString(this.g, this.title, this.offsetLeft, titlePosition, this.maxTextWidth, 3);
	  	
	    // the series/date position is fixed
	  	if (this.g.getFontMetrics().stringWidth(seriesAndDate) > this.maxTextWidth) {
	  		seriesPosition = 312;
	  	} else {
    	    seriesPosition = 344;
	  	}
	   	this.drawString(this.g, seriesAndDate, this.offsetLeft, seriesPosition, this.maxTextWidth, 2);
	   	
	    // clean up
	    this.g.dispose();
    	
    	return image;
    }

}