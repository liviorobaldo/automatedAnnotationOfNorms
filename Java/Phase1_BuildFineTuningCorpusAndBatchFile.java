import java.io.*;
import java.util.*;
import org.jdom2.*;
import org.jdom2.input.*;
import java.nio.charset.*;
import org.apache.jena.atlas.json.*;

public class Phase1_BuildFineTuningCorpusAndBatchFile 
{
    private static String shortSystemRole =
    "You are given one legislative subsection from UK legislation. Your task is to extract deontic statements and conditions and represent "+
    "them in a structured JSON template. Rules: (1) Preserve the original wording as much as possible, but reorder and minimally rewrite "+
    "phrases to fit the template structure. (2) If the subsection contains a deontic statement, output a JSON template encoding it. (3) "+
    "If the subsection only introduces conditions affecting other subsections, output an \"ADD-CONDITIONS-TO\" JSON template; the value of "+
    "\"ADD-CONDITIONS-TO\" must be a JSON array of strings identifying the affected subsection(s). (4) Return only the final JSON output.";

    private static File CORPUS = new File("../CORPUS/Phase1");
    private static File forTheBatch = new File("../CORPUS/Phase1/Investigatory Powers Act 2016/part9-ukpga-2016-25.akn");
    
    private static File toFineTuneOpenAI_training = new File("../PYTHON/Phase1/toFineTuneOpenAI_training.json");
    private static File toFineTuneOpenAI_validation = new File("../PYTHON/Phase1/toFineTuneOpenAI_validation.json");
    private static File batchForGPT4_1 = new File("../PYTHON/Phase1/batchForOpenAI.jsonl");
    
    public static void main(String[] args)
    {
        try
        {
            if(toFineTuneOpenAI_training.exists())toFineTuneOpenAI_training.delete();
            if(toFineTuneOpenAI_validation.exists())toFineTuneOpenAI_validation.delete();
            if(batchForGPT4_1.exists())batchForGPT4_1.delete();
            
                //The next "for" cycle is to create the fine-tuning corpus.
            ArrayList<File> akns = collectAKNfiles();
            for(File akn:akns)
            {
                JsonObject JsonAnnotations = getJsonFile(akn);
                LinkedHashMap<String,String> eId2text = extractTextsFromSubsections(akn,JsonAnnotations);
                
                    //We collect the texts of all <subsection>(s) that have a corresponding keys in the JSON;
                    //those are those who were annotated.
                LinkedHashMap<String,String> eId2textForFineTuning = new LinkedHashMap<String,String>();
                for(Map.Entry<String,String> entry:eId2text.entrySet())
                    if(JsonAnnotations.hasKey(entry.getKey()))
                        eId2textForFineTuning.put(entry.getKey(),entry.getValue());
                
                    //Now we randomly divide the set of annotated samples in 80% training and 20% validation, 
                    //and we build the corresponding files.
                List<Map.Entry<String,String>> entries = new ArrayList<>(eId2textForFineTuning.entrySet());
                Collections.shuffle(entries, new Random());
                int trainSize = (int)(entries.size()*0.8);
                for(int i=0;i<entries.size();i++) 
                    addToFineTuneOpenAI(entries.get(i).getKey()+":\r\n"+entries.get(i).getValue(),extractJSONannotationInStringFormat(JsonAnnotations,entries.get(i).getKey()),i<trainSize);
            }
            
            LinkedHashMap<String,String> eId2text = extractTextsFromSubsections(forTheBatch,getJsonFile(forTheBatch));
            for(Map.Entry<String,String> entry:eId2text.entrySet())addToBatchForOpenAI(entry.getKey()+":\r\n"+entry.getValue());
        }
        catch(Exception e)
        {
            System.out.println("Exception: "+e.getMessage());
        }
    }

    
    
/*******************************************************************************************************************************************/
// UTILITIES TO EXTRACT THE TEXT FROM THE XML ELEMENTS <subsection>
/*******************************************************************************************************************************************/
    private static LinkedHashMap<String,String> extractTextsFromSubsections(File aknFile, JsonObject JsonAnnotations)throws Exception
    {
        LinkedHashMap<String,String> ret = new LinkedHashMap<String,String>();
        
        SAXBuilder saxBuilder = new SAXBuilder();
        Document document = saxBuilder.build(aknFile);
        Element root = document.getRootElement();
        ArrayList<Element> elements = new ArrayList<Element>();
        ArrayList<Element> subsections = new ArrayList<Element>();
        elements.add(root);
        while(elements.isEmpty()==false)
        {
            Element subsection = elements.remove(0);
            if(subsection.getName().compareToIgnoreCase("subsection")==0)subsections.add(subsection);
            else for(Element e:subsection.getChildren())elements.add(e);
        }

        JsonArray toExclude = JsonAnnotations.get("to-exclude").getAsArray();
        Hashtable<String,String> temp = new Hashtable<String,String>();
        for(JsonValue value:toExclude)if((value!=null)&&(value.isString()))temp.put(value.getAsString().value(),"");
        
        for(Element subsection:subsections)
            if((subsection.getAttributeValue("eId")!=null)&&(temp.get(subsection.getAttributeValue("eId"))==null))
                ret.put(subsection.getAttributeValue("eId"),extractTextFromSubsection(subsection));
        
        return ret;
    }
    
    private static String extractTextFromSubsection(Element subsection) 
    {
        String ret = "";
        
            //Every subSection should start with a <num>, which we remove.
        if(subsection.getChildren().get(0).getName().compareToIgnoreCase("num")==0)subsection.getChildren().remove(0);
        
        boolean levelBefore=false;//after a (a sequence of) <level>(s), we have to add "\r\n". This boolean acts as "traffic light" for that.
        for(Element e:subsection.getChildren())
        {
            if(e.getName().compareToIgnoreCase("level")==0){ret+="\r\n"+extractTextFromLevel(e,1);levelBefore=true;}
            else if(levelBefore==true){ret+="\r\n"+extractTextFromElement(e).trim()+" ";levelBefore=false;}
            else ret+=extractTextFromElement(e).trim()+" ";
        }
        
        
            //Before returning, we remove every space before commas, ”, all spaces but one before “, all spaces after "“", etc.
        String[] punctuationsNoSpaceBefore = new String[]{",", ";", "”", " “", ")"};
        String[] punctuationsNoSpaceAfter = new String[]{"“","("};
        for(String punctuation:punctuationsNoSpaceBefore)
            while(ret.indexOf((" "+punctuation))!=-1)
                ret=ret.substring(0,ret.indexOf(" "+punctuation)).trim()+punctuation+" "+ret.substring(ret.indexOf(" "+punctuation)+(" "+punctuation).length(),ret.length()).trim();
        for(String punctuation:punctuationsNoSpaceAfter)
            while(ret.indexOf((punctuation+" "))!=-1)
                ret=ret.substring(0,ret.indexOf(punctuation+" "))+punctuation+ret.substring(ret.indexOf(punctuation+" ")+(punctuation+" ").length(),ret.length()).trim();
            
        return ret;
    }

        //Handles <level> elements. This method works similarly to extractTextFromSubsection, but with the following differences:
        // - Prefix each line with "\t" according to its depth.
        // - Do not remove the <num> element, but add it to ret.
        // - Recursively process nested <level> elements, increasing the depth at each level.
    private static String extractTextFromLevel(Element level, int depth)
    {
        String ret = "";
                
        boolean levelBefore=false;//after a (a sequence of) <level>(s), we have to add "\r\n". This boolean acts as "traffic light" for that.
        for(Element e:level.getChildren())
        {
            if(e.getName().compareToIgnoreCase("level")==0){ret+="\r\n"+extractTextFromLevel(e,depth+1);levelBefore=true;}
            else if(levelBefore==true){ret+="\r\n"+extractTextFromElement(e).trim()+" ";levelBefore=false;}
            else ret+=extractTextFromElement(e).trim()+" ";
        }        
        
            //Adding \t in front.
        String tabs = "";
        for(int i=0;i<depth;i++)tabs+="\t";        
        ret = tabs+ret;

        return ret;
    }
    
    private static String extractTextFromElement(Element element) 
    {
        String ret = "";
        for(Content c:element.getContent())
            if(c instanceof Text)ret+=((Text)c).getText().trim()+" ";
            else if(c instanceof Element)ret+=extractTextFromElement((Element)c).trim()+" ";
        return ret;
    }
    
/*******************************************************************************************************************************************/
// UTILITIES TO GET THE INPUT FILES
/*******************************************************************************************************************************************/
    
        //Retrieves all AKN files in the CORPUS folder (including its subfolders) that have a corresponding JSON file with annotations.
    private static ArrayList<File> collectAKNfiles()
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
                if(f.getName().lastIndexOf(".akn")!=(f.getName().length()-".akn".length()))continue;
                if(f.getName().indexOf("part")!=0)continue;
                if(f.getName().compareToIgnoreCase(forTheBatch.getName())==0)continue;
                
                String JSONfilename = f.getName().substring(0,f.getName().indexOf("-")).trim()+".json";
                if(new File(f.getParentFile().getAbsolutePath()+"/"+JSONfilename).exists())ret.add(f);
            }
        }
        
        return ret;
    }

    private static JsonObject getJsonFile(File file)throws Exception
    {
        String chunkPrefix = file.getName().substring(0,file.getName().indexOf("-"));
        File jsonFile = new File(file.getParentFile(),chunkPrefix+".json");
        try(InputStream in = new FileInputStream(jsonFile)){return JSON.parse(in);}
    }
    
    private static String extractJSONannotationInStringFormat(JsonObject JsonAnnotations, String key)
    {
        StringBuilder sb = new StringBuilder();
        JsonArray array = JsonAnnotations.get(key).getAsArray();

        sb.append("\"").append(key).append("\":\n");
        sb.append("[\n");
        for(int i=0;i<array.size();i++)
        {
            JsonObject obj = array.get(i).getAsObject();
            ArrayList<String> keys = new ArrayList<>(obj.keys());

            sb.append("\t{\n");
            for (int j=0;j<keys.size();j++)
            {
                String field = keys.get(j);
                JsonValue value = obj.get(field);

                String valStr;
                if(value.isString())valStr=value.getAsString().value();
                else valStr=value.toString();

                sb.append("\t\t\"").append(field).append("\": \"").append(valStr).append("\"");

                if(j<keys.size()-1)sb.append(",");
                sb.append("\n");
            }

            sb.append("\t}");
            if(i<array.size()-1)sb.append(",");
            sb.append("\n");
        }

        sb.append("]");

        return sb.toString();
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
