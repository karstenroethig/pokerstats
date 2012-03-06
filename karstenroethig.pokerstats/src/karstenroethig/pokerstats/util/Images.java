package karstenroethig.pokerstats.util;

import karstenroethig.pokerstats.Activator;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

public class Images {

	public static final String ICON_CALCULATOR = "calculator-16.png";
	
	public static final String ICON_REFRESH = "refresh-16.png";
	
	public static final String IMAGE_BAZINGA = "bazinga.jpg";
	
	private static ImageRegistry imageRegistry = new ImageRegistry();
	
	public static Image getImage( String key ) {
		
		Image image = imageRegistry.get( key );
		
		if( image == null ) {
			ImageDescriptor imgDesc = Activator.getImageDescriptor( getPath( key ) );
			image = imgDesc.createImage();
			imageRegistry.put( key, image );
		}
		
		return image;
	}
	
	private static String getPath( String key ) {
		return "icons/" + key;
	}
}
