package ecologylab.bigsemantics.platformspecifics;

import ecologylab.appframework.PropertiesAndDirectories;
import ecologylab.generic.ReflectionTools;

/**
 * instantiation of ISpecificsPlatformSpeciics
 * 
 * @author shenfeng
 * 
 */

public class SemanticsPlatformSpecifics
{
	private static ISemanticsPlatformSpecifics	iSemanticplatformSpecifics;

	private static boolean											dead	= false;

	public static void set(ISemanticsPlatformSpecifics that)
	{
		iSemanticplatformSpecifics = that;
	}

	public static ISemanticsPlatformSpecifics get()
	{
		if (dead)
			throw new RuntimeException("Can't initialize SemanticsPlatformSpecifics");

		if (iSemanticplatformSpecifics == null)
			synchronized (SemanticsPlatformSpecifics.class)
			{
				String className = null;

				if (iSemanticplatformSpecifics == null)
				{
					if (PropertiesAndDirectories.os() == PropertiesAndDirectories.ANDROID)
						className = "SemanticsPlatformSpecificsAndroid";
					else
						className = "SemanticsPlatformSpecificsSun";
				}

				if (className != null)
				{
					Class platformSpecificsClass;
					try
					{
						platformSpecificsClass = Class.forName("ecologylab.semantics.platformspecifics."
								+ className);
					}
					catch (ClassNotFoundException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
						throw new RuntimeException("Can't initialize FundamentalPlatformSpecifics" + className);
					}
					
					if (platformSpecificsClass != null
					    && ISemanticsPlatformSpecifics.class.isAssignableFrom(platformSpecificsClass))
					{
						iSemanticplatformSpecifics =
						    (ISemanticsPlatformSpecifics) ReflectionTools.getInstance(platformSpecificsClass);
					}
					else
					{
						dead = true;
						throw new RuntimeException("Can't initialize FundamentalPlatformSpecifics");
					}
				}
			}

		return iSemanticplatformSpecifics;
	}
}
