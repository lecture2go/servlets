package de.uhh.l2g.util;

import java.awt.FontFormatException;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class L2goImageBuilderServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private static final Map<String, String> additionalImages = new HashMap<String, String>();

	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public L2goImageBuilderServlet() {
        super();
        // create the map for accessing additional images
        additionalImages.put("l2go", "/WEB-INF/l2go_logo_trans.png");
        additionalImages.put("campusinno", "/WEB-INF/ci_logo.png");
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("image/png");
		
		// reads the request parameters or if not given set them to an empty string
		String author = request.getParameter("author")!=null ? request.getParameter("author") : "";
		String institution = request.getParameter("institution")!=null ? request.getParameter("institution") : "";
		String title = request.getParameter("title")!=null ? request.getParameter("title") : "";
		String series = request.getParameter("series")!=null ? request.getParameter("series") : "";
		String date = request.getParameter("date")!=null ? request.getParameter("date") : "";		
		String type = request.getParameter("type")!=null ? request.getParameter("type") : "";
		String downscale = request.getParameter("downscale")!=null ? request.getParameter("downscale") : "";
		String additionalImage = (request.getParameter("additionalimage")!=null || additionalImages.get(request.getParameter("additionalimage"))!=null) ? request.getParameter("additionalimage") : null;

		
		if (type.equals("speakerslides")) {
			SpeakerSlidesL2goImageBuilder imageBuilder = new SpeakerSlidesL2goImageBuilder(author, institution, title, series, date);
			InputStream backgroundimageStream = getServletContext().getResourceAsStream("/WEB-INF/generic_speaker_slides.png");
			imageBuilder.setBackgroundimageStream(backgroundimageStream);
			
			if (additionalImage != null) {
				InputStream additionalImageStream = getServletContext().getResourceAsStream(additionalImages.get(additionalImage));
				imageBuilder.setAdditionalImageStream(additionalImageStream);
			}

			imageBuilder.setFontSize(30.0f);
			initializeFonts(imageBuilder);
			
			// builds a large image with the given data
			BufferedImage image = imageBuilder.buildImage();
		
			// scales the image down to the needed size
			if (downscale.equalsIgnoreCase("true")) {
				image = imageBuilder.scaleImage(image, 320, 240);
			}
			// the old (manual) scaling method is disabled
			//BufferedImage scaledImage = imageBuilder.scaleImage(image, 320, 240, RenderingHints.VALUE_INTERPOLATION_BICUBIC, true);
			
			// finally write the image to the browser as PNG file
	
			ImageIO.write(image, "png", response.getOutputStream());
		} else if (type.equals("speakeronly")) {			
			SpeakerOnlyL2goImageBuilder imageBuilder = new SpeakerOnlyL2goImageBuilder(author, institution, title, series, date);
			InputStream backgroundimageStream = getServletContext().getResourceAsStream("/WEB-INF/generic_speakeronly.png");
			imageBuilder.setBackgroundimageStream(backgroundimageStream);
			
			if (additionalImage != null) {
				InputStream additionalImageStream = getServletContext().getResourceAsStream(additionalImages.get(additionalImage));
				imageBuilder.setAdditionalImageStream(additionalImageStream);
			} else {
		   		// the l2go logo is used if no additional image is set 
				InputStream additionalImageStream = getServletContext().getResourceAsStream(additionalImages.get("l2go"));
				imageBuilder.setAdditionalImageStream(additionalImageStream);
			}

			imageBuilder.setFontSize(40.0f);

			initializeFonts(imageBuilder);
			
			// builds a large image with the given data
			BufferedImage image = imageBuilder.buildImage();
			
			// scales the image down to the needed size
			if (downscale.equalsIgnoreCase("true")) {
				image = imageBuilder.scaleImage(image, 1024, 107);
			}
			// the old (manual) scaling method is disabled
			//BufferedImage scaledImage = imageBuilder.scaleImage(image, 1024, 107, RenderingHints.VALUE_INTERPOLATION_BILINEAR, true);
			
			// finally write the image to the browser as PNG file
			ImageIO.write(image, "png", response.getOutputStream());
		} 
	}
	
	/**
	 * Reads the font files for the different font types (bold italic not implemented)
	 * @param imageBuilder the L2goImageBuilder for which the fonts are set
	 * @throws FontFormatExceptions
	 */
	public void initializeFonts(L2goImageBuilder imageBuilder) {
		try {
			// the regular font type
			InputStream fontRegularStream = getServletContext().getResourceAsStream("/WEB-INF/TheSansUHH_Regular.ttf");
			imageBuilder.setFontRegular(fontRegularStream);
			
			// the bold font type
			InputStream fontBoldStream = getServletContext().getResourceAsStream("/WEB-INF/TheSansUHH_Bold.ttf");
			imageBuilder.setFontBold(fontBoldStream);
			
			// the italic font type
			InputStream fontItalicStream = getServletContext().getResourceAsStream("/WEB-INF/TheSansUHH_Regular_Italic.ttf");
			imageBuilder.setFontItalic(fontItalicStream);			
		} catch(IOException e) {
			e.printStackTrace();
		} catch(FontFormatException e) {
			e.printStackTrace();
		}
	}
}
