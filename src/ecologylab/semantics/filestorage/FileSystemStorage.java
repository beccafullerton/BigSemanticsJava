/**
 * 
 */
package ecologylab.semantics.filestorage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import ecologylab.generic.Debug;
import ecologylab.io.Files;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.html.utils.StringBuilderUtils;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.formatenums.Format;

/**
 * file system based implementation of FileStorageProvider
 * 
 * @author ajit
 * 
 */
public class FileSystemStorage extends Debug implements FileStorageProvider
{

	private static FileSystemStorage	fsStorageProvider				= null;

	private static String							downloadDirectory;

	private static String							metaFileDirectory;

	public static String							semanticsFileDirectory;

	private static SimplTypesScope		META_TSCOPE							= SimplTypesScope.get("fileMetadata",
																																FileMetadata.class);

	private static int								subdirectoryNameLength	= 3;

	public static void setDownloadDirectory(Properties props)
	{
		downloadDirectory = props.getProperty("LOCAL_DOCUMENT_CACHE_DIR");
		if (downloadDirectory == null)
			throw new RuntimeException("Property LOCAL_DOCUMENT_CACHE_DIR is required!");
		metaFileDirectory = downloadDirectory + "/meta";
		semanticsFileDirectory = downloadDirectory + "/semantics";

		File f = new File(downloadDirectory);
		f.mkdirs();
		f = new File(metaFileDirectory);
		f.mkdir();
		f = new File(semanticsFileDirectory);
		f.mkdir();
	}

	private FileSystemStorage()
	{
	}

	@Override
	public String saveFile(ParsedURL originalPURL, InputStream input)
	{
		File outFile = getDestinationFileAndCreateDirs(downloadDirectory, originalPURL, "html");
		try
		{
			InputStream in = input;
			OutputStream out = new FileOutputStream(outFile);
			byte buf[] = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0)
				out.write(buf, 0, len);
			out.close();
			in.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}

		debug("Saved inputstream to " + outFile.getAbsolutePath());
		return outFile.getAbsolutePath();
	}

	@Override
	public String lookupFilePath(ParsedURL originalPURL)
	{
		File f = getDestinationFileAndCreateDirs(downloadDirectory, originalPURL, "html");
		debug("Checking for cached HTML file at [" + f.getAbsolutePath() + "]: exists? " + f.exists());
		if (f.exists())
			return f.getAbsolutePath();
		else
			return null;
	}

	@Override
	public void saveFileMetadata(FileMetadata fileMetadata)
	{
		File metaFile = getDestinationFileAndCreateDirs(metaFileDirectory, fileMetadata.getLocation(),
				"meta");
		try
		{
			SimplTypesScope.serialize(fileMetadata, metaFile, Format.XML);
		}
		catch (SIMPLTranslationException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public FileMetadata getFileMetadata(ParsedURL location)
	{
		File metaFile = getDestinationFileAndCreateDirs(metaFileDirectory, location, "meta");
		if (!metaFile.exists())
			return null;

		FileMetadata result = null;
		try
		{
			result = (FileMetadata) META_TSCOPE.deserialize(metaFile, Format.XML);
		}
		catch (SIMPLTranslationException e)
		{
			e.printStackTrace();
		}
		if (result == null)
		{
			error("Missing file metadata file at " + metaFile + " for " + location);
		}
		return result;
	}

	@Override
	public void removeFileAndMetadata(ParsedURL location)
	{
		File file = getDestinationFileAndCreateDirs(downloadDirectory, location, "html");
		if (file.exists() && !file.delete())
			warning("cached HTML " + file.getAbsolutePath() + " for url " + location.toString()
					+ " could not be deleted");

		File metaFile = getDestinationFileAndCreateDirs(metaFileDirectory, location, "meta");
		if (metaFile.exists() && !metaFile.delete())
			warning("cached HTML " + metaFile.getAbsolutePath() + " for url " + location.toString()
					+ " could not be deleted");
	}

	public static File getDestinationFileAndCreateDirs(String topdir, ParsedURL originalPURL,
			String suffix)
	{
		String outFileName = SHA256FileNameGenerator.getName(originalPURL) + "." + suffix;
		File subdir = new File(topdir, outFileName.substring(0, subdirectoryNameLength));
		subdir.mkdirs();
		File file = new File(subdir, outFileName);
		return file;
	}

	public static FileStorageProvider getStorageProvider()
	{
		if (fsStorageProvider == null)
		{
			fsStorageProvider = new FileSystemStorage();
		}
		return fsStorageProvider;
	}

}
