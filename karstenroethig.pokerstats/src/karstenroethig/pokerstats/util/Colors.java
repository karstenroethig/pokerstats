package karstenroethig.pokerstats.util;

import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

public class Colors {
	
	public static final String WHITE = "white";
	public static final String RED = "red";

	private static ColorRegistry colorRegistry = new ColorRegistry();
	
	static {
		colorRegistry.put( WHITE, new RGB( 255, 255, 255 ) );
		colorRegistry.put( RED, new RGB( 255, 0, 0 ) );
	}
	
	public static Color getColor( String symbolicName ) {
		return colorRegistry.get( symbolicName );
	}
	
	public static Color getColor( int red, int green, int blue ) {
		
		String symbolicName = "rgb-" + red + "-" + green + "-" + blue;
		Color color = getColor( symbolicName );
		
		if( color == null ) {
			RGB rgb = new RGB( red, green, blue );
			colorRegistry.put( symbolicName, rgb );
			color = getColor( symbolicName );
		}
		
		return color;
		
	}
}
