package ecologylab.bigsemantics.logging;

import java.util.Date;

import ecologylab.bigsemantics.Utils;
import ecologylab.bigsemantics.metadata.builtins.PersistenceMetaInfo;
import ecologylab.concurrent.DownloadableLogRecord;
import ecologylab.generic.HashMapArrayList;
import ecologylab.net.ParsedURL;
import ecologylab.serialization.annotations.Hint;
import ecologylab.serialization.annotations.simpl_collection;
import ecologylab.serialization.annotations.simpl_composite;
import ecologylab.serialization.annotations.simpl_hints;
import ecologylab.serialization.annotations.simpl_inherit;
import ecologylab.serialization.annotations.simpl_scalar;

/**
 * 
 * @author quyin
 */
@simpl_inherit
public class DocumentLogRecord extends DownloadableLogRecord
{

  @simpl_scalar
  @simpl_hints(Hint.XML_LEAF)
  ParsedURL                       documentLocation;

  @simpl_collection("phase")
  HashMapArrayList<String, Phase> phases;

  @simpl_composite
  PersistenceMetaInfo             persistenceMetaInfo;

  @simpl_scalar
  int                             downloadStatusCode;

  @simpl_composite
  DocumentErrorRecord             errorRecord;

  public ParsedURL getDocumentLocation()
  {
    return documentLocation;
  }

  public void setDocumentLocation(ParsedURL documentUrl)
  {
    this.documentLocation = documentUrl;
  }

  public void beginPhase(String name)
  {
    getOrAddPhase(name).beginTime = new Date();
  }

  public void endPhase(String name)
  {
    Phase phase = getPhase(name);
    if (phase != null)
    {
      phase.endTime = new Date();
      phase.timeInMs = phase.endTime.getTime() - phase.beginTime.getTime();
    }
  }

  public long getTotalMs(String name)
  {
    Phase phase = getPhase(name);
    return phase == null ? -1 : phase.timeInMs;
  }

  public Date getBeginTime(String name)
  {
    Phase phase = getPhase(name);
    return phase == null ? null : phase.beginTime;
  }

  public Date getEndTime(String name)
  {
    Phase phase = getPhase(name);
    return phase == null ? null : phase.endTime;
  }

  public Phase getPhase(String name)
  {
    return phases == null ? null : phases.get(name);
  }

  public Phase getOrAddPhase(String name)
  {
    Phase result = null;

    if (phases == null)
    {
      synchronized (this)
      {
        if (phases == null)
        {
          phases = new HashMapArrayList<String, Phase>();
        }
      }
    }

    if (!phases.containsKey(name))
    {
      synchronized (phases)
      {
        if (!phases.containsKey(name))
        {
          Phase phase = new Phase();
          phase.name = name;
          phases.put(name, phase);
          result = phase;
        }
      }
    }

    if (result == null)
    {
      result = phases.get(name);
    }

    return phases.get(name);
  }

  public PersistenceMetaInfo getPersistenceMetaInfo()
  {
    return persistenceMetaInfo;
  }

  public void setPersistenceMetaInfo(PersistenceMetaInfo persistenceMetaInfo)
  {
    this.persistenceMetaInfo = persistenceMetaInfo;
  }

  public int getDownloadStatusCode()
  {
    return downloadStatusCode;
  }

  public void setDownloadStatusCode(int downloadStatusCode)
  {
    this.downloadStatusCode = downloadStatusCode;
  }

  public DocumentErrorRecord getErrorRecord()
  {
    return errorRecord;
  }

  public void setErrorRecord(DocumentErrorRecord errorRecord)
  {
    this.errorRecord = errorRecord;
  }

  public void setErrorRecord(String message, Throwable throwable)
  {
    this.errorRecord = new DocumentErrorRecord();
    this.errorRecord.message = message;
    this.errorRecord.stackTrace = Utils.getStackTraceAsString(throwable);
  }

}
