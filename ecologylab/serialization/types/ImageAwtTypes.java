/**
 * 
 */
package ecologylab.serialization.types;

import ecologylab.generic.Debug;
import ecologylab.serialization.types.scalar.ColorType;
import ecologylab.serialization.types.scalar.ImageType;
import ecologylab.serialization.types.scalar.RectangleType;

/**
 * This class initializes ScalarTypes that depend on java.awt.*, which does not exist in Android.
 * 
 * @author andruid
 */
public class ImageAwtTypes extends Debug
{
	public static final ScalarType COLOR_TYPE 					= new ColorType();
	
	public static final ScalarType RECTANGLE_TYPE 			= new RectangleType();
	
	public static final ScalarType IMAGE_TYPE 					= new ImageType();


}
