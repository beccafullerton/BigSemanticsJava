/**
 * 
 */
package ecologylab.semantics.metametadata.example;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;

import ecologylab.collections.Scope;
import ecologylab.documenttypes.DocumentParser;
import ecologylab.net.ParsedURL;
import ecologylab.semantics.generated.library.WeatherReport;
import ecologylab.semantics.metadata.builtins.Document;
import ecologylab.semantics.metametadata.MetaMetadataRepository;
import ecologylab.semantics.seeding.Seed;
import ecologylab.semantics.seeding.SeedSet;
import ecologylab.semantics.tools.MetadataCompiler;
import ecologylab.xml.TranslationScope;
import ecologylab.xml.XMLTranslationException;

/**
 * This example shows how to use a search as seed to collect data from the Internet.
 * 
 * We start by a google search of weather in Texas, then parse the search result and collect data
 * with meta-metadata library.
 * 
 * @author quyin
 */
public class TestMetaMetadataRuntime
{
	public final static int	COUNT_TARGETS	= 9;

	/**
	 * Before you write your own codes, make sure that use the VM arguments like this project Or the
	 * DownloadMonitor will never start downloading due to limited available memory!
	 * 
	 * @param args
	 * @throws XMLTranslationException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws XMLTranslationException, IOException,
			InterruptedException
	{
		// create the infoCollector
		MyInfoCollector infoCollector = new MyInfoCollector(
				MetadataCompiler.DEFAULT_REPOSITORY_FILEPATH);
		// add the WeatherReportCollector to the listener list, so that we can collect information we
		// need from the metadata
		infoCollector.addListener(WeatherReportCollector.get());

		// seeding
		ParsedURL seedUrl = ParsedURL
				.getAbsolute("http://www.google.com/search?q=texas+site%3Awww.wunderground.com");
		infoCollector.getContainerDownloadIfNeeded(null, seedUrl, null, false, false, false);

		// wait for the infoCollector to finish its downloading job
		// note that the downloadMonitor (contained in infoCollecotr) will wait for new seeds if
		//   downloading is done. so we check the number of collected reports to determine when to finish
		while (WeatherReportCollector.get().list().size() < COUNT_TARGETS)
		{
			Thread.sleep(1000);
		}
		// stop the downloadMonitor
		infoCollector.getDownloadMonitor().stop();

		// output collected data into a .csv file
		OutputStream outs = new FileOutputStream("output.csv");
		PrintWriter writer = new PrintWriter(outs);
		writer.printf("#format:city,weather,picture_url,temperature,humidity,wind_speed\n");
		for (WeatherReport report : WeatherReportCollector.get().list())
		{
			writer.printf(
					"%s,%s,%s,%s,%s,%s\n",
					report.city().getValue().split(",")[0],
					report.weather().getValue(),
					report.picUrl().getValue(),
					report.temperature().getValue(),
					report.humidity().getValue(),
					report.wind().getValue()
					);
		}
		writer.flush();
		writer.close();
		outs.close();
	}

}
