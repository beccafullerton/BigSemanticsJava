package ecologylab.semantics.metametadata.test;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import ecologylab.semantics.collecting.MetaMetadataRepositoryInit;
import ecologylab.semantics.metadata.scalar.types.SemanticsTypes;
import ecologylab.semantics.metametadata.MetaMetadataCompositeField;
import ecologylab.semantics.metametadata.MetaMetadataField;
import ecologylab.semantics.metametadata.MetaMetadataRepository;
import ecologylab.semantics.metametadata.MetaMetadataScalarField;
import ecologylab.semantics.metametadata.MetaMetadataTranslationScope;
import ecologylab.semantics.metametadata.NestedMetaMetadataFieldTranslationScope;
import ecologylab.serialization.ElementState.FORMAT;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.TranslationScope;
import ecologylab.serialization.TranslationScope.GRAPH_SWITCH;

public class TestRepositoryInJSON
{
	
	private static final int				BUFFER_SIZE	= 4096;

	private static TranslationScope	mmdTScope		= null;
	
	private void translateRepositoryIntoJSON(File srcDir, File destDir)
	{
		translateRepositoryDirIntoJSON(srcDir, destDir);
		translateRepositoryDirIntoJSON(new File(srcDir, "repositorySources"), new File(destDir, "repositorySources"));
		translateRepositoryDirIntoJSON(new File(srcDir, "powerUser"), new File(destDir, "powerUser"));
	}
	
	private void translateRepositoryDirIntoJSON(File srcDir, File destDir)
	{
		if (!destDir.exists())
			destDir.mkdir();
		FileFilter filter = new FileFilter() {
			@Override
			public boolean accept(File pathname)
			{
				return pathname.getName().endsWith("xml");
			}
		};
		for (File f : srcDir.listFiles(filter))
		{
			try
			{
				MetaMetadataRepository repo = (MetaMetadataRepository) mmdTScope.deserialize(f);
				String json = repo.serialize(FORMAT.JSON).toString();
				if (json != null && json.length() > 0)
				{
					File jsonRepoFile = new File(destDir, f.getName().replace((CharSequence) ".xml", (CharSequence) ".json"));
					FileWriter writer = new FileWriter(jsonRepoFile);
					writer.write(json);
					writer.close();
				}
			}
			catch (SIMPLTranslationException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		TranslationScope.graphSwitch	= GRAPH_SWITCH.ON;
		MetaMetadataRepository.initializeTypes();
		new SemanticsTypes();
		
		// ********* each time, run one of the following methods: *********
		
//		testLoadingAndSavingXmlRepository();
//		testSavedAgainXmlRepository();
//		testConvertingRepositoryFromXmlToJson();
		testJsonRepository(destJsonRepoDir);
		
//		testLoadAndSaveJsonRepository(destJsonRepoDir, dest2JsonRepoDir);
//		testJsonRepository(dest2JsonRepoDir);
	}
	
	private static File	srcXmlRepoDir			= new File("../ecologylabSemantics/repository/");

	private static File	destXmlRepoDir		= new File("/tmp/repository/");

	private static File	destJsonRepoDir		= new File("../ecologylabSemantics/repositoryInJSON");

	private static File	dest2JsonRepoDir	= new File("/tmp/jsonAgain");

	private static void testLoadingAndSavingXmlRepository()
	{
		// replace MetaMetadataCollectionField with MetaMetadataCollectionFieldChildComposite
		TranslationScope.get(NestedMetaMetadataFieldTranslationScope.NAME, new Class[] {
				MetaMetadataField.class,
				MetaMetadataScalarField.class,
				MetaMetadataCompositeField.class,
				MetaMetadataCollectionFieldWithoutChildComposite.class,
		});
		mmdTScope = MetaMetadataTranslationScope.get();
		
		// load and save the repository again
		testLoadAndSaveXmlRepositoryDir(srcXmlRepoDir, destXmlRepoDir);
		testLoadAndSaveXmlRepositoryDir(new File(srcXmlRepoDir, "repositorySources"), new File(destXmlRepoDir, "repositorySources"));
		testLoadAndSaveXmlRepositoryDir(new File(srcXmlRepoDir, "powerUser"), new File(destXmlRepoDir, "powerUser"));
	}

	private static void testLoadAndSaveXmlRepositoryDir(File srcDir, File destDir)
	{
		FileFilter filter = new FileFilter() {
			@Override
			public boolean accept(File pathname)
			{
				return pathname.getName().endsWith(".xml");
			}
		};
		for (File xmlFile : srcDir.listFiles(filter))
		{
			try
			{
				MetaMetadataRepository repo = (MetaMetadataRepository) mmdTScope.deserialize(xmlFile);
				repo.serialize(new File(destDir, xmlFile.getName()));
			}
			catch (SIMPLTranslationException e1)
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private static void testLoadAndSaveJsonRepository(File srcDir, File destDir)
	{
		// replace MetaMetadataCollectionField with MetaMetadataCollectionFieldChildComposite
		TranslationScope.get(NestedMetaMetadataFieldTranslationScope.NAME, new Class[] {
				MetaMetadataField.class,
				MetaMetadataScalarField.class,
				MetaMetadataCompositeField.class,
				MetaMetadataCollectionFieldWithoutChildComposite.class,
		});
		mmdTScope = MetaMetadataTranslationScope.get();
		
		// load and save the repository again
		testLoadAndSaveJsonRepositoryDir(destJsonRepoDir, dest2JsonRepoDir);
		testLoadAndSaveJsonRepositoryDir(new File(destJsonRepoDir, "repositorySources"), new File(dest2JsonRepoDir, "repositorySources"));
		testLoadAndSaveJsonRepositoryDir(new File(destJsonRepoDir, "powerUser"), new File(dest2JsonRepoDir, "powerUser"));
	}

	private static void testLoadAndSaveJsonRepositoryDir(File srcDir, File destDir)
	{
		FileFilter filter = new FileFilter() {
			@Override
			public boolean accept(File pathname)
			{
				return pathname.getName().endsWith(".json");
			}
		};
		for (File jsonFile : srcDir.listFiles(filter))
		{
			try
			{
				StringBuilder json = new StringBuilder();
				char[] buffer = new char[BUFFER_SIZE];
				FileReader reader = new FileReader(jsonFile);
				while (true)
				{
					int n = reader.read(buffer, 0, BUFFER_SIZE);
					if (n < 0)
						break;
					json.append(buffer, 0, n);
				}
				reader.close();
				MetaMetadataRepository repo = (MetaMetadataRepository) mmdTScope.deserializeCharSequence(json, FORMAT.JSON);
				File newJsonFile = new File(destDir, jsonFile.getName());
				if (!newJsonFile.exists())
				{
					newJsonFile.getParentFile().mkdirs();
					newJsonFile.createNewFile();
				}
				repo.serialize(new FileOutputStream(newJsonFile), FORMAT.JSON);
			}
			catch (SIMPLTranslationException e1)
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private static void testSavedAgainXmlRepository()
	{
		// use json repository for NewMmTest
		MetaMetadataRepositoryInit.DEFAULT_REPOSITORY_LOCATION = destXmlRepoDir.getAbsolutePath();
		MetaMetadataRepositoryInit.DEFAULT_REPOSITORY_FILE_SUFFIX = ".xml";
		MetaMetadataRepositoryInit.DEFAULT_REPOSITORY_FILE_LOADER = MetaMetadataRepository.XML_FILE_LOADER;
		
		tryNewMmTest();
	}

	private static void testConvertingRepositoryFromXmlToJson()
	{
		// replace MetaMetadataCollectionField with MetaMetadataCollectionFieldChildComposite
		TranslationScope.get(NestedMetaMetadataFieldTranslationScope.NAME, new Class[] {
				MetaMetadataField.class,
				MetaMetadataScalarField.class,
				MetaMetadataCompositeField.class,
				MetaMetadataCollectionFieldWithoutChildComposite.class,
		});
		mmdTScope = MetaMetadataTranslationScope.get();
		
		// convert repository to json
		TestRepositoryInJSON trij = new TestRepositoryInJSON();
		trij.translateRepositoryIntoJSON(srcXmlRepoDir, destJsonRepoDir);
	}
	
	private static void testJsonRepository(File jsonRepoDir)
	{
		mmdTScope = MetaMetadataTranslationScope.get();
		
		// use json repository for NewMmTest
		MetaMetadataRepositoryInit.DEFAULT_REPOSITORY_LOCATION = jsonRepoDir.getAbsolutePath();
		MetaMetadataRepositoryInit.DEFAULT_REPOSITORY_FILE_SUFFIX = ".json";
		MetaMetadataRepositoryInit.DEFAULT_REPOSITORY_FILE_LOADER = MetaMetadataRepository.JSON_FILE_LOADER;
		
		tryNewMmTest();
	}

	private static void tryNewMmTest()
	{
		// use a set of URLs to test extraction
		String[] testUrls = new String[] {
			"http://www.dlese.org/dds/services/ddsws1-1?verb=UserSearch&q=water+on+mars&s=0&n=10&client=ddsws10examples",
			"http://www.dlese.org/dds/services/ddsws1-0?verb=GetRecord&id=DLESE-000-000-000-001",
			"http://where.yahooapis.com/geocode?gflags=R&q=-96.28616666666667,30.604833333333332",
			"http://news.blogs.cnn.com/2011/04/14/predator-dinosaurs-may-have-been-night-hunters/?hpt=C2",
			"http://remodelista.com/products/victoria-and-albert-wessex-bath",
			"http://portal.acm.org/citation.cfm?id=1416955",
			"http://buzzlog.yahoo.com/feeds/buzzsportm.xml",
			"http://www.informaworld.com/smpp/content~db=all?content=10.1080/10447310802142243",
			"http://rss.cnn.com/rss/cnn_topstories.rss",
			"//",
		};
		NewMmTest mmTest;
		try
		{
			mmTest = new NewMmTest("NewMmTest");
			mmTest.collect(testUrls);
		}
		catch (SIMPLTranslationException e)
		{
			e.printStackTrace();
		}
	}

}
