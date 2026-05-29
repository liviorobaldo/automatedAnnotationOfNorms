import java.io.*;
import java.util.*;
import org.jdom2.*;
import org.jdom2.input.*;
import java.nio.charset.*;
import org.apache.jena.atlas.json.*;

public class Phase2_BuildFineTuningCorpusAndBatchFile 
{
    private static String shortSystemRole =
    "You are given: (1) one or more deontic statements annotated in JSON; (2) one or more legislative subsections separated by \";;;\" and "+
    "containing additional conditions affecting those statements. Your task is to extend the deontic statements by adding the relevant "+
    "conditions. Rules: (1) Do not modify the original JSON annotation. (2) Only add new condition-related JSON fields (e.g., \"SUBJECT TO\", "+
    "\"IF\", \"UNLESS\"). (3) Add a single \"CONDITIONS-ADDED-FROM\" field listing the subsection(s) introducing the conditions. (4) Return "+
    "only the final JSON annotation.";

    private static File CORPUS = new File("../CORPUS/Phase2");
    private static File forTheBatch = new File("../CORPUS/Phase1/Children Act 1989/part12.json");
    
    private static File toFineTuneOpenAI_training = new File("../PYTHON/Phase2/toFineTuneOpenAI_training.json");
    private static File toFineTuneOpenAI_validation = new File("../PYTHON/Phase2/toFineTuneOpenAI_validation.json");
    private static File batchForGPT4_1 = new File("../PYTHON/Phase2/batchForOpenAI.jsonl");
    
    public static void main(String[] args)
    {        
        try
        {
            if(toFineTuneOpenAI_training.exists())toFineTuneOpenAI_training.delete();
            if(toFineTuneOpenAI_validation.exists())toFineTuneOpenAI_validation.delete();
            if(batchForGPT4_1.exists())batchForGPT4_1.delete();
            
                //The next "for" cycle is to collect all supervised samples that goes in the fine-tuning corpus.
            ArrayList<File> jsons = collectJSONfiles();
            Hashtable<String,String> user2assistant = new Hashtable<String,String>();
            for(File json:jsons)
            {
                Document document = getAKNfile(json);
                File jsonCounterpart = getJSONcounterpart(json);
                JsonObject object = null;
                JsonObject objectCounterpart = null;
                try(InputStream in=new FileInputStream(json)){object=JSON.parse(in);}
                try(InputStream in=new FileInputStream(jsonCounterpart)){objectCounterpart=JSON.parse(in);}
                
                Hashtable<String,Hashtable<String,String>> ModifiedSubsections2ModifyingSubsections = getModifiedSubsections2ModifyingSubsections(jsonCounterpart);
                ArrayList<String> ModifiedSubsections = new ArrayList<String>(ModifiedSubsections2ModifyingSubsections.keySet());

                for(String ModifiedSubsection:ModifiedSubsections)
                {
                    String user = getJsonArraySingleLine(objectCounterpart,ModifiedSubsection);
                    if(user.isEmpty())continue;
                    
                    ArrayList<String> additionals = getSubsectionsTextsEIds(document, ModifiedSubsections2ModifyingSubsections.get(ModifiedSubsection));
                    for(String additional:additionals)user+=(" ;;; "+additional);
                    String assistant = getJsonArraySingleLine(object,ModifiedSubsection);
                    user2assistant.put(user,assistant);
                }
            }
            
            List<String> keys = new ArrayList<>(user2assistant.keySet());
            Collections.shuffle(keys, new Random());
            int trainSize = (int)(keys.size()*0.8);
            for(int i=0;i<keys.size();i++)addToFineTuneOpenAI(keys.get(i),user2assistant.get(keys.get(i)),i<trainSize);
            
                //Now we build the batch...
            Hashtable<String,Hashtable<String,String>> ModifiedSubsections2ModifyingSubsections = getModifiedSubsections2ModifyingSubsections(forTheBatch);
                //...if there is at least one modified subsection.
            if(ModifiedSubsections2ModifyingSubsections.isEmpty()==true){System.out.println("The file "+forTheBatch.getName()+" does not contain any \"ADD-CONDITIONS-TO\"");System.exit(0);}
            
            Document document = getAKNfile(forTheBatch);
            JsonObject objectCounterpart = null;
            try(InputStream in=new FileInputStream(getJSONcounterpart(forTheBatch))){objectCounterpart=JSON.parse(in);}
            ArrayList<String> ModifiedSubsections = new ArrayList<>(ModifiedSubsections2ModifyingSubsections.keySet());
            for(String ModifiedSubsection:ModifiedSubsections)
            {
                String user = getJsonArraySingleLine(objectCounterpart,ModifiedSubsection);
                ArrayList<String> additionals = getSubsectionsTextsEIds(document, ModifiedSubsections2ModifyingSubsections.get(ModifiedSubsection));
                for(String additional:additionals)user=user.trim()+" ;;; "+additional.trim();
                addToBatchForOpenAI(user);
            }
        }
        catch(Exception e)
        {
            System.out.println("Exception: "+e.getMessage());
        }
    }
    
/*******************************************************************************************************************************************/
// UTILITIES TO GET THE INPUT FILES
/*******************************************************************************************************************************************/

        //Retrieves all JSON files in the CORPUS folder (including its subfolders) that have a corresponding AKN file.
    private static ArrayList<File> collectJSONfiles()
    {
        ArrayList<File> ret = new ArrayList<File>();
        
        ArrayList<File> dirs = new ArrayList<File>();
        dirs.add(CORPUS);
        while(dirs.isEmpty()==false)
        {
            File dir = dirs.remove(0);
            for(File f:dir.listFiles())
            {
                if(f.isDirectory()){dirs.add(f);continue;}
                if(f.getName().lastIndexOf(".json")!=(f.getName().length()-".json".length()))continue;
                ret.add(f);
            }
        }
        
        return ret;
    }

    private static Document getAKNfile(File json)throws Exception
    {
        String filename = json.getName().substring(0,json.getName().indexOf(".json"));
        File folder = new File(json.getParentFile().getAbsolutePath().replaceAll("Phase2","Phase1"));
        for(File f:folder.listFiles())
            if((f.getName().indexOf(filename)==0)&&(f.getName().lastIndexOf(".akn")==(f.getName().length()-".akn".length())))
                return new SAXBuilder().build(f);
        throw new Exception("Cannot find the AKN file corresponding to "+json.getAbsolutePath());
    }
    
    private static File getJSONcounterpart(File json)throws Exception
    {
        File folder = new File(json.getParentFile().getAbsolutePath().replaceAll("Phase2","Phase1"));
        return new File(folder+"/"+json.getName());
    }
    
    private static Hashtable<String,Hashtable<String,String>> getModifiedSubsections2ModifyingSubsections(File jsonFile)throws Exception
    {
        Hashtable<String,Hashtable<String,String>> ret = new Hashtable<String,Hashtable<String,String>>();
        
        JsonObject object = null;
        try(InputStream in=new FileInputStream(jsonFile)){object=JSON.parse(in);}
                
        ArrayList<String> keys = new ArrayList<String>(object.keySet());
        for(String key:keys)
        {
            if(key.compareToIgnoreCase("to-exclude")==0)continue;
            
            JsonArray array = object.get(key).getAsArray();
            for(JsonValue value:array)
            {
                if(value.getAsObject().hasKey("ADD-CONDITIONS-TO")==true)
                {
                    ArrayList<String> modifiedSubsectioneIds = getModifiedSubsectionEIds(value.getAsObject().get("ADD-CONDITIONS-TO").getAsArray(),key,object);
                    for(String modifiedSubsectioneId:modifiedSubsectioneIds)
                    {
                        Hashtable<String,String> temp = ret.get(modifiedSubsectioneId);
                        if(temp==null){temp=new Hashtable<String,String>();ret.put(modifiedSubsectioneId,temp);}
                        temp.put(key,"");
                    }
                }
            }
        }
        
        return ret;
    }
    
    private static ArrayList<String> getModifiedSubsectionEIds(JsonArray eIdsToConvert, String key, JsonObject object)throws Exception
    {
        String sectionEId = key.substring(0,key.indexOf("-",key.indexOf("-")+1));
        ArrayList<String> convertedEids = new ArrayList<String>();
        for(JsonValue eIdToConvert:eIdsToConvert)
        {
            String temp = eIdToConvert.getAsString().value();
            if(temp.indexOf("subsection")==0)convertedEids.add((sectionEId+temp.substring("subsection".length(),temp.length()).trim()).trim());
            else if(temp.indexOf("whole-section")==0)convertedEids.add(sectionEId);//if there is "whole-section" we add the section eId.
            else throw new Exception("I cannot interpret the eIdToConvert "+temp);
        }
        
        ArrayList<String> ret = new ArrayList<String>();
        for(String convertedEid:convertedEids)
        {
            if(object.hasKey(convertedEid)==true)ret.add(convertedEid);
            else if(convertedEid.compareToIgnoreCase(sectionEId)==0)
            {
                    //If it's the section eId, we add all subsection(s) of the section.
                ArrayList<String> temps = new ArrayList<String>(object.keySet());
                for(String temp:temps)if((temp.indexOf(sectionEId+"-")==0)&&(temp.compareToIgnoreCase(key)!=0))ret.add(temp);
            }
        }
        
        return ret;
    }
    
    private static String getJsonArraySingleLine(JsonObject object, String key) throws Exception 
    {
        if(object.hasKey(key) && object.get(key).isArray()) 
        {
            JsonArray array = object.get(key).getAsArray();
            for(int i=0;i<array.size();i++)
            {
                if(array.get(i).getAsObject().hasKey("ADD-CONDITIONS-TO"))
                {
                    array.remove(i);
                    i--;
                }
            }
            if(array.size()==0)return "";
        
            String arrayStr = array.toString();
            String flattenedArray = arrayStr.replaceAll("(?s)\\s+(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", "");
            return String.format("\"%s\":%s", key, flattenedArray);
        }
        throw new Exception("Cannot find the key \""+key+"\" in the JSON object");
    }

    private static ArrayList<String> getSubsectionsTextsEIds(Document document, Hashtable<String,String> eIDs)throws Exception
    {
        ArrayList<Element> elements = new ArrayList<Element>();
        ArrayList<Element> subsections = new ArrayList<Element>();
        elements.add(document.getRootElement());
        while(elements.isEmpty()==false)
        {
            Element subsection = elements.remove(0);
            if(subsection.getName().compareToIgnoreCase("subsection")==0)
            {
                if(eIDs.get(subsection.getAttributeValue("eId"))!=null)
                    subsections.add(subsection);
            }
            else for(Element e:subsection.getChildren())elements.add(e);
        }
        
        ArrayList<String> subsectionsTexts = new java.util.ArrayList<>();
        for(Element subsection:subsections)
            if(subsections!=null)subsectionsTexts.add(subsection.getAttributeValue("eId")+": "+subsection.getValue().replaceAll("[\\n\\r\\t]+", " ").replaceAll("\\s+", " ").trim());
            else throw new Exception("Cannot extract the text of section \""+subsection.getAttributeValue("eId")+"\"");
        
        if(subsectionsTexts.isEmpty())throw new Exception("This should not be empty!!!");
        
        return subsectionsTexts;
    }
    
/*******************************************************************************************************************************************/
// UTILITIES TO WRITE THE FINE-TUNING CORPUS AND THE ENTRIES TO PROCESS
/*******************************************************************************************************************************************/
    private static void addToFineTuneOpenAI(String text, String annotatedText, boolean training)throws Exception
    {
        File file = toFineTuneOpenAI_training;
        if(training==false)file=toFineTuneOpenAI_validation;
        
        try(BufferedWriter writer=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file,true),StandardCharsets.UTF_8)))
        {
            // --- SYSTEM ---
            String systemContent = shortSystemRole
                    .replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\r", "")
                    .replace("\n", "\\r\\n")
                    .replace("\t", "\\t");

            // --- USER ---
            String userContent = text
                    .replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\r", "")
                    .replace("\n", "\\r\\n")
                    .replace("\t", "\\t");

            // --- ASSISTANT ---
            String assistantContent = annotatedText
                    .replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\r", "")
                    .replace("\n", "")
                    .replace("\t", "");

            String jsonLine = "{ \"messages\": [ " +
                    "{ \"role\": \"system\", \"content\": \""+systemContent+"\" }, "+
                    "{ \"role\": \"user\", \"content\": \""+userContent+"\" }, "+
                    "{ \"role\": \"assistant\", \"content\": \""+assistantContent+"\" } "+
                    "] }";

            writer.write(jsonLine);
            writer.newLine();
        }
    }
    
    private static void addToBatchForOpenAI(String text)throws Exception
    {
        try(BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(batchForGPT4_1, true),StandardCharsets.UTF_8))) 
        {
            // --- SYSTEM ---
            String systemContent = shortSystemRole
                    .replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\r", "")
                    .replace("\n", "\\r\\n")
                    .replace("\t", "\\t");

            // --- USER ---
            String userContent = text
                    .replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\r", "")
                    .replace("\n", "\\r\\n")
                    .replace("\t", "\\t");
            String jsonLine =
                    "{"
                        + "\"custom_id\":\"" + UUID.randomUUID().toString() + "\","
                        + "\"method\":\"POST\","
                        + "\"url\":\"/v1/chat/completions\","
                        + "\"body\":{"
                            + "\"model\":\"REPLACE-ALL-THE-FINE-TUNED-MODEL\","
                            + "\"messages\":["
                            + "{\"role\":\"system\",\"content\":\"" + systemContent + "\"},"
                            + "{\"role\":\"user\",\"content\":\"" + userContent + "\"}"
                            + "]"
                        + "}"
                    + "}";

            writer.write(jsonLine);
            writer.newLine();
        }
    }
}

