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
		this.imageWidth  = 640;
		
		// the distance from the image-border to the start of text in pixels
		this.offsetLeft = 10;
		this.offsetRight = 10;
		
		// the position of the additional image if there is any
		this.offsetLeftAdditionalImage = 450;
		this.offsetTopAdditionalImage = 398;
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
    	
    	// series and date are handled as one
        String seriesAndDate = getSeriesAndDate();
   	    	
    	// the author position is fixed
    	authorPosition = 106;
    	this.g.setFont(this.fontBold);
        
        // calculate how many lines may be used for each text category
        int authorLines 		= this.calculateLineCount(this.g, this.author, this.offsetLeft, this.maxTextWidth-130);
    	
    	
	    this.drawString(this.g, this.author, this.offsetLeft, authorPosition, this.maxTextWidth-130, 2);
	    // the institution position is relative to the author position
        if (authorLines <= 1) {
            institutionPosition = 140;
        } else {
            institutionPosition = 170;
        }
	    this.g.setFont(this.fontItalic);
        int institutionLines 	= this.calculateLineCount(this.g, this.institution, this.offsetLeft, this.maxTextWidth);
	   	this.drawString(this.g, this.institution, this.offsetLeft, institutionPosition, this.maxTextWidth, 2);

	   	// title
	  	this.g.setFont(this.fontRegular);
	  	// we need to calculate the line count of the series and date lines in regard to the used font
        int seriesAndDateLines 	= this.calculateLineCount(this.g, seriesAndDate, this.offsetLeft, this.maxTextWidth);
        int titleLines 	= this.calculateLineCount(this.g, this.title, this.offsetLeft, this.maxTextWidth);


        // choose upper or lower position
        if ((authorLines + institutionLines) <=3 && (titleLines>=3 && seriesAndDateLines>=2) 
        		|| ((authorLines + institutionLines) <=2 && titleLines>=3)) {
        	titlePosition = 206;
        }else {
        	titlePosition = 240;
        }
        
        if ((seriesAndDateLines + institutionLines + authorLines) <6) {
		  	this.drawString(this.g, this.title, this.offsetLeft, titlePosition, this.maxTextWidth, 3);
	  	} else {
		  	this.drawString(this.g, this.title, this.offsetLeft, titlePosition, this.maxTextWidth, 2);
	  	}

	  	// the series/date position is fixed
	  	if (seriesAndDateLines > 1 ) {
	  		seriesPosition = 312;
	  	} else {
    	    seriesPosition = 344;
	  	}
	   	this.drawString(this.g, seriesAndDate, this.offsetLeft, seriesPosition, this.maxTextWidth, 2);
	   	
	   	// if there is an additional Image, draw it
	   	if (this.additionalImage != null) {
	   		this.additionalImage = this.scaleImage(this.additionalImage, 165, 58);
		   	this.drawAdditionalImage(this.g, this.additionalImage, this.offsetLeftAdditionalImage, this.offsetTopAdditionalImage);
	   	}
	   	
	    // clean up
	    this.g.dispose();
    	
    	return image;
    }

}