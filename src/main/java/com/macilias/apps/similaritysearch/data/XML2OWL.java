package com.macilias.apps.similaritysearch.data;

import java.io.*;
import java.util.List;
import com.macilias.apps.similaritysearch.logic.algorithms.graph.StructureManagementStaticClasses.*;
import com.macilias.apps.similaritysearch.logic.algorithms.graph.StructureClasses.*;
import com.macilias.apps.similaritysearch.logic.algorithms.graph.ModelManagement;
import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.ResourceUtils;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Stack;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;
import com.macilias.apps.similaritysearch.logic.algorithms.graph.StructureManagement;
import com.macilias.apps.similaritysearch.logic.algorithms.graph.TreeSimilarity_Relations;
import com.macilias.apps.similaritysearch.util.SoutConfig;
import com.macilias.apps.similaritysearch.util.URITool;


/**
 *
 * @author Maciej Niemczyk
 */
public class XML2OWL {

    ModelManagement mm = ModelManagement.getInstance();
    public String XML_FILE_NAME;
    public Vector<String> vertikaleS = null;
    public Vector<String> vertikaleSC = null;
    public Vector<Vertikale> vertikaleV = null;
    public int uriLenght = 30;
    static private Writer outWrite;
    private OntModel m = mm.getBase();
    ClassLoader classLoader = getClass().getClassLoader();


    protected String NSAA = m.getNsPrefixURI("aA"); //abstrakte Attribute
    protected String NSAD = m.getNsPrefixURI("aD"); //abstrakte Datentypen
    protected String NSAS = m.getNsPrefixURI("aS"); //abstraktes Datenschema (Kompositum)
    protected String NSSS = m.getNsPrefixURI("sS"); //SimSearch Systemdata
    //general Count (will be serialised in m)
    private long gCount = 0;
    //general Count of readet Files (will be serialised in m)
    private long gCFile = 0;
    //File count will be expressed in a character as suffix for counter
    //A-Z:0-26 AA-AZ:27-53 ZA-ZZ:702-728 AAA-AAZ etc.
    //count actually parsed Elements
    private int count = 0;
    private DataOutputStream out1;

    //Soll weiter gesucht werden?
    public boolean machmal = true;
    public boolean kannteSieSchon = false;
    public String  lastParsedFile = "";
    public String  rootURI = "";

    //Configuration
    private final boolean useBlankNodes  = false;
    private final boolean generateOtherV = true;
    private final boolean generateS  = false;
    private final boolean generateSC = true;

    public final  boolean printoutXML = SoutConfig.getPrintOutXML();
    public static boolean verbous = SoutConfig.getVerbous_X2O();
    public static boolean aBitVerbous = SoutConfig.getaBitVerbous_X2O();
    public static boolean khadija = SoutConfig.getKhadija_X2O();
    public static boolean calculation = SoutConfig.getCalculation_X2O();

    String vertikalenErstellung="";

    public static String getStringCountPrefix(long gCFile){
        if(verbous) System.out.println("Es wird ein CounterPrefix aufgrund der Nummer der Datei berechnet - gCFile: "+gCFile);
        String prefix = "";
        char[] alphab = {'A','B','C','D','E','F','G','H','I','J','K','L','M',
                         'N','O','P','Q','R','S','T','U','V','W','X','Y','Z'};
        boolean ready = false;
        while(!ready){
            int    rest = Integer.parseInt(String.valueOf(gCFile%alphab.length));
            if(verbous && khadija) System.out.println("gCFile="+gCFile+"%"+alphab.length+" = "+rest);
            long  quot = (gCFile/alphab.length);
            if(verbous && khadija) System.out.println("Quotient="+quot);
            prefix = alphab[rest]+prefix;
            if(verbous && khadija) System.out.println("Prefix="+prefix);
            if(quot==0){
                ready = true;
            }else{
                gCFile = quot;
            }
        }
        if(verbous) System.out.println("Der zugewiesene Prefix lautet: "+prefix);
        return prefix;
    }

    public XML2OWL(String daten) {
        if(daten!=null){
            if(daten.equals("CD lite")){
                XML_FILE_NAME = "test_data/cd-catalog_small.xml";
            }else{
            if(daten.equals("CD full")){
                XML_FILE_NAME = "test_data/cd-catalog.xml";
            }else{
            if(daten.equals("Mini Example 1")){
                XML_FILE_NAME = "test_data/mini_example1.xml";
            }else{
            if(daten.equals("Mini Example 2")){
                XML_FILE_NAME = "test_data/mini_example2.xml";
            }else{
            if(daten.equals("Mini Example 3")){
                XML_FILE_NAME = "test_data/mini_example3.xml";
            }else{
            if(daten.equals("Micro Example A")){
                XML_FILE_NAME = "test_data/micro_example_a.xml";
            }else{
            if(daten.equals("Micro Example B")){
                XML_FILE_NAME = "test_data/micro_example_b.xml";
            }else{
            if(daten.equals("Nano  Example A")){
                XML_FILE_NAME = "test_data/nano_example_a.xml";
            }else{
            if(daten.equals("Nano  Example B")){
                XML_FILE_NAME = "test_data/nano_example_b.xml";
            }else{
            if(daten.equals("Baumsignaturen")){
                XML_FILE_NAME = "test_data/Baumsignaturen_XPath.xml";
            }else{
            if(daten.equals("XHTML Example")){
                XML_FILE_NAME = "test_data/xhtmlExample.xhtml";
            }else{
            if(daten.equals("Produktstrukturen S")){
                XML_FILE_NAME = "test_data/Produktstrukturen_small.xml";
            }else{
            if(daten.equals("Produktstrukturen M")){
                XML_FILE_NAME = "test_data/Produktstrukturen_medium.xml";
            }else{
            if(daten.equals("Produktstrukturen XXL")){
                XML_FILE_NAME = "test_data/Produktstrukturen.xml";
            }else{
                XML_FILE_NAME = daten;}}}}}}}}}}}}}
            }
        }
    }

    public Object parse(){
        if(XML_FILE_NAME!=null){
            Handler handler = new Handler();
            try {
                this.m = mm.getBase();
                // Set up input stream
                File file = new File(classLoader.getResource(XML_FILE_NAME).getFile());
                InputStream inputStream= new FileInputStream(file);
                Reader reader = new InputStreamReader(inputStream,"UTF-8");
                InputSource is = new InputSource(reader);
                is.setEncoding("UTF-8");
                // Set up out stream
                outWrite = new OutputStreamWriter(System.out, "UTF-8");
                SAXParserFactory factory = SAXParserFactory.newInstance();

                //try to get rid of dtd
//                factory.setValidating(false);
//                factory.setFeature("http://xml.org/sax/features/validation", false);
                SAXParser saxParser = factory.newSAXParser();
                saxParser.getXMLReader().setEntityResolver(new EntityResolver() {
                @Override
                public InputSource resolveEntity(String publicId, String systemId)
                        throws SAXException, IOException {
                        return new InputSource(new StringReader(""));
                }
                //try to get rid of dtd

            });
                try{
                    saxParser.parse(is, handler);
                }catch(SAXParseException sx){
                    System.out.println("Es gab einen Fehler beim Parsen. Siehe Logdatei im Verzeichnis logs");
                    System.out.println("Vermutlich kann das Problem durchs Löschen des DTD Verweises in dem");
                    System.out.println("XML Dokument behoben werden!");
                    Logger.getLogger(XML2OWL.class.getName()).log(Level.SEVERE, null, sx);
                }catch(FileNotFoundException fx){
                    System.out.println("Es gab einen Fehler beim Parsen. Siehe Logdatei im Verzeichnis logs");
                    System.out.println("Vermutlich kann das Problem durchs Löschen des DTD Verweises in dem");
                    System.out.println("XML Dokument behoben werden!");
                    Logger.getLogger(XML2OWL.class.getName()).log(Level.SEVERE, null, fx);
                }catch(IOException ex){
                    Logger.getLogger(XML2OWL.class.getName()).log(Level.SEVERE, null, ex);
                }

            } catch (Exception e) {
                System.out.println("FEHLER " + e);
    //            e.toString();
            }
            rootURI = handler.getCurrentRoot();
            if(mm.isCorT()){
                return handler.getAttributeMIL();
            }else{
                return handler.getAttribute();
            }
        }else{
            System.out.println("Es wurde kein gültiger Dateipfad zu einen XML-Dokument übergeben");
            return null;
        }
    }

    public class Handler extends DefaultHandler {
        //Die Anzahl der Elemente auf den Stack gibt auch Auskunft
        //über die Ebene der globalen Vertikalen
        private Stack<Individual> stack = new Stack<Individual>();
        private HashSet<String> att = new HashSet<String>();
        //MIL = Multiple Individual List (Mehrfachvorkommen von Attributen)
        private LinkedList<String> attMIL = new LinkedList<String>();
        private String rootURI = "";
        private int    rootAttCount = 0;
        private StringBuffer textBuffer;
        private Vector<Ebene> v = new Vector<Ebene>();
        StructureManagement sm = StructureManagement.getInstance();
        private String prefix  = "UPS!";

        //Ohne mehrfache Auführung für Baumähnlichkeit
        public HashSet<String> getAttribute(){
            return this.att;
        }

        //Mit mehrfacher Ausführung für inhaltiche Ähnlichkeit
        public LinkedList<String>getAttributeMIL(){
            return this.attMIL;
        }

        public String getCurrentRoot(){
            return rootURI;
        }

        
//        @Override
//        public InputSource resolveEntity(String publicId, String systemId){
//            System.out.println("ignoriere die DTD");   //standalone=\'yes\'
//            return new InputSource(new ByteArrayInputStream("<?xml version=\'1.0\' encoding=\'UTF-8\'?>".getBytes()));
//        }

        @Override
        public void startDocument() throws SAXException {
            //Setzte den PositionsManager der Vertikalen zurück
            VLocalPositionManager.reset(sm.getOffset());
            try {
                OntClass sysmdata = m.createClass(NSSS+"systemdata");
                Individual config = m.createIndividual(NSSS+"system_config", sysmdata);
                Literal compcount = null;
                Literal filecount = null;
                if(config!=null){
                    compcount = (Literal) config.getPropertyValue(m.getProperty(NSSS+"compcount"));
                    filecount = (Literal) config.getPropertyValue(m.getProperty(NSSS+"filecount"));
                }
                if(compcount!=null) gCount = compcount.getLong();
                if(filecount!=null) gCFile = filecount.getLong();
                if(verbous) System.out.println("gCount = "+gCount+" und gCFile = "+gCFile);
                prefix = getStringCountPrefix(gCFile);
                if (printoutXML) {
                    showData("<?xml version='1.0' encoding='UTF-8'?>");
                    newLine();
                }
            } catch (IOException ex) {
                Logger.getLogger(XML2OWL.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        @Override
        public void endDocument() throws SAXException {
            Individual empty = m.getIndividual(NSAD+"empty_element_tag");
            if(empty!=null){
                TreeSimilarity_Relations.removeComponent(empty, false);
                rootAttCount--;
            }else{
                if(verbous) System.out.println("gut! Es gab keine leeren Element Tags");
            }

            //Setzte die Anzahl der Attribute beim Root
            Individual root = m.getIndividual(rootURI);
            if(mm.isCorT()) rootAttCount = attMIL.size();
            root.addLiteral(m.getProperty(NSAS+"hasAttCount"), rootAttCount);
            //END Setzte die Anzahl der Attribute beim Root

            Individual config = m.getIndividual(NSSS+"system_config");
            if(config == null)  config = m.createIndividual(NSSS+"system_config", m.getOntClass(NSSS+"systemdata"));

            gCount += count;
            gCFile++;
            RDFNode gCountLit = null;
            RDFNode gCFileLit = null;
            if(config.hasProperty(m.getProperty(NSSS+"compcount"))) gCountLit = config.getPropertyValue(m.getProperty(NSSS+"compcount"));
            if(config.hasProperty(m.getProperty(NSSS+"filecount"))) gCFileLit = config.getPropertyValue(m.getProperty(NSSS+"filecount"));
            if(gCountLit!=null) config.removeProperty(m.getProperty(NSSS+"compcount"), gCountLit);
            if(gCFileLit!=null) config.removeProperty(m.getProperty(NSSS+"filecount"), gCFileLit);
            config.addLiteral(m.getProperty(NSSS+"compcount"), gCount);
            config.addLiteral(m.getProperty(NSSS+"filecount"), gCFile);
            
            echoText();

            System.out.println("\nDokument Ende, neue Componenten: "+count+" insgesammt: "+gCount);
            System.out.println("Suffix dieser Datei beginnt mit: "+prefix);
            //bucketsortVerticale innerhalb von generateVerticale()
            generateVerticale();
            writeOutVerticale();
//            VLocalPositionManager.printKeyPositions();
            int beforeCount = sm.getGvVS().size();
            sm.addNewVerticales(vertikaleV, vertikaleSC);
            System.out.println("Import fertig - Importiert wurden: "+(sm.getGvVS().size()-beforeCount)+" neue Vertikalen");
            if(verbous || aBitVerbous) sm.writeOutVerticale();
            //Print out Max and Min Values
            if(verbous || aBitVerbous){
                ExtendedIterator<OntClass> it = m.listClasses();
                while(it.hasNext()){
                    OntClass c = it.next();
                    if(c.hasProperty(m.getProperty(NSAS+"maxValue"))) System.out.println("maxValue von "+c.getLocalName()+" = "+c.getPropertyValue(m.getProperty(NSAS+"maxValue")));
                    if(c.hasProperty(m.getProperty(NSAS+"minValue"))) System.out.println("minValue von "+c.getLocalName()+" = "+c.getPropertyValue(m.getProperty(NSAS+"minValue")));
                }
            }
//            VLocalPositionManager.printKeyPositions();
            try {
                if (printoutXML) {
                    newLine();
                }
                outWrite.flush();
            } catch (IOException e) {
                throw new SAXException("I/O error", e);
            }
            //RDF
            //Nothing to Do here
        }

        @Override
        public void startElement(String namespaceURI, String localName,
                String qName, Attributes attrs) throws SAXException {

            echoText();
            if(count%78==0) System.out.println("");
            System.out.print(".");

            try {
                if (printoutXML) {
                    showData("<" + qName);
                }                
                OntClass e = m.createClass(NSAD + URITool.encode(qName.toLowerCase()));
                count++;
                String suffix = prefix+String.valueOf(count);
                String parentURI = "";
                //Das nichtvorhandensein von Attributen im Sinne von XML wird
                //durch "no attributs" = "na" signalisiert
                if(attrs.getLength()==0) suffix = suffix+"na";
                Individual ci;
                Horizontale h;
//                boolean createHori = false;
                ci = m.getIndividual(NSAD + URITool.encode(qName.replace(" ", "_").
                        toLowerCase()) + "_" + suffix);
                if(ci == null){
                    ci = m. createIndividual(NSAD + URITool.encode(qName.replace(" ", "_").
                        toLowerCase()) + "_" + suffix, e);
//                    createHori = true;
                }
                if (!stack.empty()) {
                    Individual parent = stack.peek();
                    parentURI = parent.getURI();
                    parent.addProperty(m.getProperty(NSAS + "hasChild"), ci);
                }

                stack.push(ci);
                ci.addLiteral(m.getProperty(NSAS+"hasLevel"), stack.size());
                h = new Horizontale();
                h.uri = ci.getURI();
                boolean found = false;
                if(!parentURI.equals("")){
                    Ebene vorebene = v.get(stack.size()-2);
                    for(int i=0; i<vorebene.e.size()&&!found; i++){
                        Horizontale pHorizont = (Horizontale)vorebene.e.get(i);
                        if(pHorizont.uri.equals(parentURI)){
                            found = true;
                            h.parentKey = (pHorizont.key);
                        }
                    }
                }
                if(v.size()==0 || v.size()<=stack.size()-1 || v.get(stack.size()-1)==null){
                    Ebene ebene = new Ebene();
                    ebene.maxCCount = 0;
                    ebene.addAndUpdate(h);
                    v.add(ebene);
                }else{
                    Ebene ebene = v.get(stack.size()-1);
                    ebene.addAndUpdate(h);
                }

                if (attrs != null) {
                    for (int i = 0; i < attrs.getLength(); i++) {
                        OntClass a = m.createClass(NSAS + "Attribute");
                        OntClass ac = m.createClass(NSAA + URITool.encode(attrs.getQName(i).
                                replace(" ", "_")).toLowerCase());
                        ac.setSuperClass(a);
                        //Durch das Anhängen des count können Kollisionen zwischen
                        //langen Strings nur noch innerhalb einer Parent-Componente
                        //auftretten. Durch Anhängen von i, wird auch dies unmöglich
                        String wert = attrs.getValue(i);
                        if(wert.length()>uriLenght) wert = wert.substring(0, uriLenght)+"-"+count+"-"+i;
                        wert = URITool.encode(wert.replace(" ", "_").replace("%", "_")).toLowerCase();
                        Individual ai = m.createIndividual(NSAA + wert, ac);
                        ai.addProperty(m.getDatatypeProperty(NSAS + "hasValue"),
                                attrs.getValue(i));
//                        ai.addLiteral(m.getProperty(NS+"check"), true);
                        //statt check funktion werden die atts direkt gesammelt
                        if(verbous && khadija) System.out.println("Adde "+ai.getURI()+" zu atts (from XML atts)");
                        ci.addProperty(m.getProperty(NSAS + "hasChild"), ai);
                        ci.addProperty(m.getProperty(NSAS + "hasChildClass"), ac);
                        if(!mm.isCorT()){
                            att.add(ai.getURI());
                            rootAttCount++;
                        }else{
                            attMIL.add(ai.getURI());
                        }

                        //Update den Maximalen und Minimalen Wert der Klasse
                        if(a.hasProperty(m.getProperty(NSAS+"maxValue"))){
                            Statement  smax = a.getRequiredProperty(m.getProperty(NSAS+"maxValue"));
                            String    smaxS = smax.getObject().toString();
                            if(verbous) System.out.println("HIER KOMMT BISHERIGES MAX (from attrs list): "+smaxS);
                            if(smaxS.length()<wert.length()){
                                smax.changeObject(wert);
                            }else{
                                if(smaxS.length()==wert.length() && smaxS.compareTo(wert)<0){
                                    smax.changeObject(wert);
                                }
                            }
                        }else{
                            if(!wert.trim().equals("")) a.addProperty(m.getProperty(NSAS+"maxValue"), wert);
                        }
                        if(a.hasProperty(m.getProperty(NSAS+"minValue"))){
                            Statement  smin = a.getRequiredProperty(m.getProperty(NSAS+"minValue"));
                            String    sminS = smin.getObject().toString();
                            if(verbous) System.out.println("HIER KOMMT BISHERIGES MIN (from attrs list): "+sminS);
                            if(sminS.length()>wert.length()){
                                smin.changeObject(wert);
                            }else{
                                if(sminS.length()==wert.length() && sminS.compareTo(wert)>0){
                                    smin.changeObject(wert);
                                }
                            }
                        }else{
                            if(!wert.trim().equals("")) a.addProperty(m.getProperty(NSAS+"minValue"), wert);
                        }
                        
                        //Für Attribute i.s.XML wird die setSA()
                        //NICHT aufgerufen, weil für diese endElement nicht aufgerufen wird.
                        //Deshalb muss hier die ganze Horizontale angelegt werden.
                        //Erstelle eine Neue Horizontale
                        Horizontale ha = new Horizontale();
                        ha.uri = ai.getURI();
                        ha.type= 'A';
                        ha.parentKey = h.key;
                        //Erstelle eine Neue Ebene falls noch nicht vorhanden
                        if(v.size()<=stack.size() || v.get(stack.size())==null){
                            Ebene unterebene = new Ebene();
                            unterebene.addAndUpdate(ha);
                            v.add(unterebene);
                        }else{
                            Ebene unterebene = v.get(stack.size());
                            unterebene.addAndUpdate(ha);
                        }
                        if (printoutXML) {
                            showData(" ");
                        }
                        if (printoutXML) {
                            showData(attrs.getQName(i) + "=\"" + attrs.
                                    getValue(i) + "\"");
                        }
                    }
                }

                // Falls Wurzel, lege eine Referenz auf das Ursprungsfile an
                // über die hasComponent Beziehung lassen sich stett Rückschlüsse
                // auf das Ursprungsfile ableiten
                if(stack.size()==1){
                    String fileName = XML_FILE_NAME.substring((XML_FILE_NAME.lastIndexOf("/")+1), XML_FILE_NAME.length());
                    if(verbous || aBitVerbous) System.out.println("Wurzel wurde mit den FILE_NAME bestückt: "+fileName);
                    ci.addProperty(m.getProperty(NSAS+"from_XML_FILE"), fileName);
                    ci.addProperty(m.getProperty(NSAS+"from_XML_FILE_FP"), XML_FILE_NAME);
                    rootURI = ci.getURI();
                }

                if (printoutXML) {
                    showData(">");
                }

            } catch (IOException ex) {
                Logger.getLogger(XML2OWL.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        @Override
        public void endElement(String namespaceURI,
                String localName, String qName) throws SAXException {

            echoText();
            Individual endElement = stack.peek();
                //Ein Element ist dann ein Attribut, wenn während dieses bearbeitet
                //wurde kein weiteres Element angelegt worden ist.
                //Sollte es ferner selbst arributiert worden sein (im Sinne von XML)
                //dann worden diesen mehrere Werte zugewiesen und dann
                //währe es ein komplexes Element.
            if (endElement.getURI().endsWith(prefix+String.valueOf(count)+"na")) {
                //Das Element war ein Attribut weil dazwischen komplexe
                //Komponenten Anzahl nicht erhöht worden ist
                //Entferne die Ebenen Markierung:
                StmtIterator it = endElement.listProperties(m.getProperty(NSAS+"hasLevel"));
                List<Statement> ita = it.toList();
                for(int i=0; i<ita.size();i++) {
                    Statement s = ita.get(i);
                    endElement.removeProperty(s.getPredicate(),s.getObject());
                }
                OntClass clss = endElement.getOntClass();
                //String superc = "";
                if(clss.getSuperClass()!=null){
                    OntClass superc=clss.getSuperClass();
                    //!superc. equals("Hybrid")
                    if(!superc.equals(m.getOntClass(NSAS+"Hybrid"))){
                        //!superc.equals("Komplex")
                        if(!superc.equals(m.getOntClass(NSAS+"Komplex"))){
                            clss.setSuperClass(m.createClass(NSAS + "Attribute"));
//                            endElement.addLiteral(m.getProperty(NS+"check"), true);
//                            att.add(endElement.getURI());
                            if(verbous) System.out.println(" TEMP1 DEBUG -> Attribute no Hybrid no Komplex superc="+superc);
                        }else{
                            clss.setSuperClass(m.createClass(NSAS + "Hybrid"));
                            if(verbous) System.out.println(" TEMP1 DEBUG -> Hybrid");
                            //Entferne die Markierung zur überprüfung des Attributes
//                            RDFNode value = endElement.getPropertyValue(m.getProperty(NS+"check"));
//                            endElement.removeProperty(m.getProperty(NS+"check"), value);
                        }
                    }
                }else{
                    clss.setSuperClass(m.createClass(NSAS + "Attribute"));
//                    endElement.addLiteral(m.getProperty(NS+"check"), true);
//                    att.add(endElement.getURI());
                    if(verbous) System.out.println(" TEMP1 DEBUG -> Attribute no SuperClass");
                }
                //Suche den Wert heraus:
                String wert = "empty_element_tag";
                if(endElement.getPropertyValue(m.
                            getDatatypeProperty(NSAS+"hasValue"))!=null){
                    wert = endElement.getPropertyValue(m.
                            getDatatypeProperty(NSAS+"hasValue")).toString();
                }
                if(wert.length()>uriLenght) wert = wert.substring(0, uriLenght)+"-"+(count)+"-"+0;
                String newName = NSAA+URITool.encode(wert.replace(" ", "_").replace("%", "_").toLowerCase());
                
                //Update die WertKlasse des Attributes beim Parent
                Individual endElementTemp = stack.pop();
                Individual parent = stack.pop();
                parent.addProperty(m.getProperty(NSAS + "hasChildClass"), clss);
                stack.push(parent);
                stack.push(endElementTemp);
                //ENDE Update die WertKlasse des Attributes beim Parent

                if(newName==null) newName="";
                setSA(endElement, true, newName);
                ResourceUtils.renameResource(stack.pop(), newName);
                if(!newName.equals(NSAA+"empty_element_tag")){
                    if(!mm.isCorT()){
                        att.add(newName);
                        rootAttCount++;
                    }else{
                        attMIL.add(newName);
                    }
                    if(verbous && khadija) System.out.println("Adde "+newName+" zu atts");
                }
                //Update den Maximalen und Minimalen Wert der Klasse
                if(clss.hasProperty(m.getProperty(NSAS+"maxValue"))){
                    Statement  smax = clss.getProperty(m.getProperty(NSAS+"maxValue"));
                    String    smaxS = smax.getObject().toString();
                    if(!smaxS.equals("empty_element_tag")){
                        if(verbous) System.out.println("HIER KOMMT BISHERIGES MAX (endElement): |"+smaxS+"|");
                        if(smaxS.length()<wert.length()){
                            smax.changeObject(wert);
                        }else{
                            if(smaxS.length()==wert.length() && smaxS.compareTo(wert)<0){
                                smax.changeObject(wert);
                            }
                        }
                    }else{
                        clss.removeProperty(smax.getPredicate(), smax.getObject());
                    }
                }else{
                    if(!wert.trim().equals("")) clss.addProperty(m.getProperty(NSAS+"maxValue"), wert);
                }
                if(clss.hasProperty(m.getProperty(NSAS+"minValue"))){
                    Statement  smin = clss.getProperty(m.getProperty(NSAS+"minValue"));
                    String    sminS = smin.getObject().toString();
                    if(!sminS.equals("empty_element_tag")){
                        if(verbous) System.out.println("HIER KOMMT BISHERIGES MIN (endElement): |"+sminS+"|");
                        if(sminS.length()>wert.length()){
                            smin.changeObject(wert);
                        }else{
                            if(sminS.length()==wert.length() && sminS.compareTo(wert)>0){
                                smin.changeObject(wert);
                            }
                        }
                    }else{
                        clss.removeProperty(smin.getPredicate(), smin.getObject());
                    }
                }else{
                    if(!wert.trim().equals("")) clss.addProperty(m.getProperty(NSAS+"minValue"), wert);
                }
                //wenns nicht mit na endet
            }else{
                OntClass clss = endElement.getOntClass();
                if(clss.getSuperClass()!=null){
                    OntClass superc=clss.getSuperClass();
                    if(!superc.hasRDFType(NSAS+"Hybrid")){
                        if(!superc.hasRDFType(NSAS+"Attribute")){
                            clss.setSuperClass(m.createClass(NSAS + "Komplex"));
                            if(verbous) System.out.println(" TEMP2 DEBUG -> Komplex");
                        }else{
                            clss.setSuperClass(m.createClass(NSAS + "Hybrid"));
                            if(verbous) System.out.println(" TEMP2 DEBUG -> Hybrid");
                        }
                    }
                }else{
                    clss.setSuperClass(m.createClass(NSAS + "Komplex"));
                            if(verbous) System.out.println(" TEMP2 DEBUG -> Komplex");
                }
                if(clss.hasProperty(m.getProperty(NSAS+"maxValue"))){
                    Statement smax = clss.getProperty(m.getProperty(NSAS+"maxValue"));
                    if(smax!=null && smax.getObject().toString().equals("empty_element_tag")){
                        clss.removeProperty(smax.getPredicate(), smax.getObject());
                    }
                }
                if(clss.hasProperty(m.getProperty(NSAS+"minValue"))){
                    Statement smin = clss.getProperty(m.getProperty(NSAS+"minValue"));
                    if(smin!=null && smin.getObject().toString().equals("empty_element_tag")){
                        clss.removeProperty(smin.getPredicate(), smin.getObject());
                    }
                }
                stack.pop();
                if(!stack.empty()){
                    Individual parent = stack.peek();
                    if((aBitVerbous && calculation) || verbous) System.out.println("Der Vater ("+parent.getLocalName()+") des Individeums "+endElement.getLocalName()+" bekommt die Relation hasComplex Element\n");
                    if(parent.hasProperty(m.getProperty(NSAS+"hasComplexChild"))){
                        if(parent.hasLiteral(m.getDatatypeProperty(NSAS+"hasComplexChild"),false)){
                            if(verbous && khadija) System.out.println("Die Relation war da und wird von false auf true geändert");
                            Statement stat = parent.getRequiredProperty(m.getDatatypeProperty(NSAS+"hasComplexChild"));
                            stat.changeLiteralObject(true);
                        }else{
                            if(verbous && khadija) System.out.println("Die Relation war schon vorhanden und true");
                        }
                    }else{
                        if(verbous && khadija) System.out.println("Die Relation war noch nicht da und wird angelegt");
                        parent.addLiteral(m.getProperty(NSAS+"hasComplexChild"), true);
                    }
                }
                stack.push(endElement);
                setSA(endElement, false, null);
                stack.pop();
            }
            try {
                if (printoutXML) {
                    showData("</" + qName + ">");
                }
            } catch (IOException ex) {
                Logger.getLogger(XML2OWL.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            String as = new String(ch, start, length);
            if(!as.trim().equals("")){
                if (textBuffer == null) {
                    textBuffer = new StringBuffer(as);
                } else {
                    textBuffer.append(as);
                }
            }
            try {
                if (printoutXML) {
                    showData(as);
                }
            } catch (IOException ex) {
                Logger.getLogger(XML2OWL.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        // --------------------------------------------------------------------
        protected void error(String s) {
            throw new RuntimeException(s);
        }

        protected void error(Exception e) {
            throw new RuntimeException(e);
        }

        //===========================================================
        // Helpers Methods
        //===========================================================

        //Zwischenspeichern der Charakters
        private void echoText()
        throws SAXException
        {
          if (textBuffer == null) return;
          String as = ""+textBuffer;
          //Der Stack kann an dieser Stelle nicht leer sein aufgrund
          //der XML-Spezifikation <Element>caracters</Element>
          Individual i = stack.peek();
          i.addProperty(m.getDatatypeProperty(NSAS + "hasValue"), as);
          textBuffer = null;
        }

        // Setzten der vertikalen Struktur-Attribute
        // Erkennung der Collections
        public void setSA(Individual endElement, boolean atr, String newURI){
            boolean isCollection = false;
            int     childCount   = -1;
            Individual empty = m.getIndividual(NSAD+"empty_element_tag");
            //Falls nicht Attribut dann Strickt oder Collection?
            boolean removeEmpty = false;
            if(!atr){
                StmtIterator st = endElement.listProperties(m.getProperty(NSAS+"hasChild"));
                int count = 0;
                HashSet<String> set = new HashSet<String>();
                while(st.hasNext()){
                    Statement stat = st.nextStatement();
                    Individual child = stat.getObject().as(Individual.class);
                    if(empty!=null && child.equals(empty)){
                        removeEmpty = true;
                    }else{
                        count++;
                        if(!set.add(child.getOntClass().getURI())){
                            isCollection = true;
                        }
                    }
                }
                if(removeEmpty){
                    endElement.removeProperty(m.getProperty(NSAS+"hasChild"), empty);
                }
                if(!isCollection){
                    DatatypeProperty collProp = m.createDatatypeProperty(NSAS+"isCollection");
                    endElement.addLiteral(collProp, false);
                    DatatypeProperty anzaProp = m.createDatatypeProperty(NSAS+"hasChildCount");
                    endElement.addLiteral(anzaProp, count);
                }else{
                    DatatypeProperty collProp = m.createDatatypeProperty(NSAS+"isCollection");
                    endElement.addLiteral(collProp, true);
                    DatatypeProperty anzaProp = m.createDatatypeProperty(NSAS+"hasChildCount");
                    endElement.addLiteral(anzaProp, count);
                }
                childCount = count;
            }
            //Setezen der Horizontalen Signatur
            Horizontale hEnd = null;
            Ebene ebene = v.get(stack.size()-1);
            ListIterator i = ebene.e.listIterator();
            boolean found = false;
            while(i.hasNext()&&!found){
                hEnd = (Horizontale)i.next();
                if(hEnd.uri.equals(endElement.getURI())){
                    found = true;
                    if(newURI!=null){
                        hEnd.uri = newURI;
                    }
                }
            }
            if(hEnd!=null){
                if(atr){
                    hEnd.type = 'A';
                }else{
                    if(isCollection){
                        hEnd.type = 'C';
                        hEnd.childcount = childCount;
                    }else{
                        hEnd.type = 'S';
                        hEnd.childcount = childCount;
                    }
                }
            }else{
                System.out.println("FEHLER: die Horizontale von endElement "+endElement.getURI()+" konnte nicht gefunden werden");
            }
            ebene.updateMaxCCount(childCount);
        }

        // Ebenen mit Horizontalen Vector v -> String
        public void vV2vS(Vector<Ebene> vV, int eb, int pos){
            boolean gS  = generateS;
            boolean gSC = generateSC;
            String vS="";
            String vSC ="";
            if(eb==0&&pos==0&&!gSC&&gS){
                vS="V"+v.size()+"(";               
                for(int i=0;i<v.size();i++){
                    Ebene ebene = v.get(i);
                    vS+="E"+(i+1);
                    if(ebene.size()>1) vS+="("; 
                    String parent="";
                    String childc="";
                    for(int j=0;j<ebene.size();j++){
                        Horizontale h=(Horizontale)ebene.get(j);
                        parent="";
                        if(h.getParentKey()!=-1) parent=String.valueOf(HPositionManager.getCurrentPos(h.getParentKey())+1);
                        childc="";
                        if(h.childcount!=0) childc=String.valueOf(h.childcount);
                        vS+="("+parent+h.type+childc+")";
                    }
                    if(ebene.size()>1) vS+=")";
                }
                vS +=")\n";
                //Beim Wurzel Element ist die Vertikale
                //gleich der gesammt Vertikale der XML
                Vertikale vertikale = new Vertikale(v);
                Horizontale wurzel = (Horizontale)v.get(0).get(0);
                int position = VLocalPositionManager.sortInSCVertikale(vertikaleSC, vS);
                if(position!=-1) vertikaleV.add(position,vertikale);
                Individual ci = m.getIndividual(wurzel.uri);
                ci.addProperty(m.createProperty(NSAS+"verticalKey"), String.valueOf(vertikale.key));
                if(verbous) vertikalenErstellung += "Dem Individeum "+ci.getURI()+" wird die Vertikale "+vertikale.key+" zugeordnet \n";
            }else{
                String vSBuffer="";
                String vSCBuffer="";
                int pathleangth = 0;
                Horizontale wurzel = (Horizontale)v.get(eb).get(pos);
                if(wurzel.type!='A'){
                    Horizontale nWurzel= new Horizontale(wurzel,-1);
                    Ebene wurzelEbene  = new Ebene();
                    Vector<Horizontale> parents = new Vector<Horizontale>();
                    Vector<Ebene> nVertikale= new Vector<Ebene>();
                    Ebene childEbene   = null;
                    Ebene nChildEbene   = null;
                    if(wurzel.childcount>0) parents.add(nWurzel);
                    wurzelEbene.add(nWurzel.getFlyweight());
                    pathleangth++;
                    boolean stillHaveChildren = true;
                    nVertikale.add(wurzelEbene);
                    if(gS) vSBuffer +="E1("+wurzel.type+wurzel.childcount+")";
                    if(gSC)vSCBuffer+="E1("+wurzel.type+wurzel.childcount+")";
                    for(int i=eb+1;i<v.size() && stillHaveChildren;i++){
                        Vector<Horizontale> tempParents = new Vector<Horizontale>();
                        childEbene = v.get(i);
                        pathleangth++;
                        int from   = 0;
                        int till   = 0;
                        if(gS) vSBuffer +="E"+(i-eb+1);
                        if(gSC)vSCBuffer+="E"+(i-eb+1);
                        if(childEbene.size()>1&&gS){
                            vSBuffer += "(";
                        }
                        if(childEbene.size()>1&&gSC){
                            vSCBuffer += "(";
                        }
                        nChildEbene = new Ebene();
                        for(int j=0; j<parents.size(); j++){
                            Horizontale parent = parents.get(j);
                            Horizontale   last = new Horizontale("dummy");
                            int     equalCount = 0;
                            int     childCount = 0;
                            till = from + parent.childcount;
                            for(int d=from;d<till;d++){
                                Horizontale h = (Horizontale)childEbene.get(d);
                                //Bei glober Vertikalen ist folgendes stetts der Fall
                                if(h.parentKey == parent.key){
                                    // Der j-te Parent wird grade barbeitet
                                    Horizontale nh = new Horizontale(h, j);
                                    nChildEbene.add(nh.getFlyweight());
                                    if(verbous && SoutConfig.getVerbous_SM()) System.out.println("Neue Horizontale "+nh.key+" wird zur nChildEbene "+nChildEbene.toString() +" hinzugefügt!");
                                    if(nh.childcount>0){
                                        tempParents.add(nh);
                                    }
                                    if(gS){
                                        String childc="";
                                        if(nh.childcount!=0) childc=String.valueOf(nh.childcount);
                                        vSBuffer+="("+(nh.parentKey+1)+nh.type+childc+")";
                                    }
                                    if(gSC){
                                        String childc="";
                                        if(nh.equals(last)){
                                            equalCount++;
                                            childCount+=nh.childcount;
                                            if(d==till-1){
                                                String childcl = "";
                                                vSCBuffer=vSCBuffer.substring(0,vSCBuffer.lastIndexOf('('));
                                                if(childCount!=0) childcl=String.valueOf(last.childcount);
                                                vSCBuffer+=(equalCount+1)+"("+(last.parentKey+1)+last.type+childcl+")";
                                                equalCount=0;
                                            }
                                        }else{
                                            if(equalCount==0){
                                                if(nh.childcount!=0) childc=String.valueOf(nh.childcount);
                                                vSCBuffer+="("+(nh.parentKey+1)+nh.type+childc+")";
                                            }else{
                                                String childcl = "";
                                                vSCBuffer=vSCBuffer.substring(0,vSCBuffer.lastIndexOf('('));
                                                if(childCount!=0) childcl=String.valueOf(last.childcount);
                                                vSCBuffer+=(equalCount+1)+"("+(last.parentKey+1)+last.type+childcl+")";
                                                if(nh.childcount!=0) childc=String.valueOf(nh.childcount);
                                                vSCBuffer+="("+(nh.parentKey+1)+nh.type+childc+")";
                                                equalCount=0;
                                            }
                                            last = nh;
                                        }
                                    }
                                }else{
                                    till++;
                                }
                            }
                        }
                        nVertikale.add(nChildEbene);
                        if(tempParents.size()==0) stillHaveChildren = false;
                        parents = tempParents;
                        if(childEbene.size()>1&&gS)  vSBuffer += ")"; 
                        if(childEbene.size()>1&&gSC) vSCBuffer += ")";
                    }
                    if(gS) vS ="V"+pathleangth+"("+vSBuffer+")\n"; 
                    if(gSC)vSC="V"+pathleangth+"("+vSCBuffer+")\n";
                    if(gS  && !vS.equals(""))  vertikaleS.add(vS);
//                    if(gSC && !vSC.equals("")) vertikaleSC.add(vSC);
                    Vertikale  vertikale  = new Vertikale(nVertikale);
                    int position = VLocalPositionManager.sortInSCVertikale(vertikaleSC, vSC);
                    if(position>-1){
                        vertikaleV.add(position,vertikale);
                    }else{
                        vertikale = vertikaleV.get(-position);
                    }
                    Individual ci = m.getIndividual(wurzel.uri);
                    vertikale.addIndividualURI(wurzel.uri);
//                    if(position!=-1) vertikaleV.add(position,vertikale);
                    ci.addProperty(m.createProperty(NSAS+"hasVertical"), String.valueOf(vertikale.key));
                    if(verbous) vertikalenErstellung += "Dem Individeum "+ci.getURI()+" wird die Vertikale "+vertikale.key+" zugeordnet \n";
                }
                if(pos%78==0) System.out.println(",,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,");
//                System.out.print(",");
//                System.out.print(vS);
//                System.out.print(vSC);
            }
        }

        // BuckedSort
        public void bucketsortVerticale(){
            boolean print = SoutConfig.getVerbous_SM();
            //Sortieren nach ChildCount
            if(print) System.out.print("BSV Sortieren nach ChildCount");
            for (int g=0; g<v.size(); g++) {
                if(print)System.out.println("\nEbene"+(g+1));
                Ebene ebene = v.get(g);
                // histogramm anlegen
                Vector[] buckets = new Vector[ebene.maxCCount+1];
                // aufsummierogram anlegen
                int[] intbuckets = new int[ebene.maxCCount+1];

                for (int i=0; i<ebene.maxCCount+1; i++) {
                    buckets[i] = new Vector();
                }
                for (int i=0; i<ebene.size(); i++) {
                     Horizontale h = (Horizontale)ebene.get(i);
                     if(print)System.out.print(" e("+i+")="+h.parentKey+h.type+h.childcount);
                     ((Vector)buckets[h.childcount]).add(h);
                     intbuckets[h.childcount]++;
                }
                //aufsummieren der anzahl
                for (int i=intbuckets.length-2; i>0; i--){
                    intbuckets[i] = intbuckets[i] + intbuckets[i+1];
                }
                //sortieren
                Vector<Horizontale_Flyweight> sortedebene = new Vector<Horizontale_Flyweight>();
                for (int i=buckets.length; i>0; i--) {
                     Iterator<Horizontale> it = ((Vector)buckets[i-1]).iterator();
                     while (it.hasNext()) {
                            sortedebene.add(it.next());
                     }
                }
                //update Positions
                for (int i=0; i<sortedebene.size(); i++){
                    Horizontale h = (Horizontale)sortedebene.get(i);
                    HPositionManager.updatePosition(h.key, i);
                }
                v.remove(g);
                Ebene sortebene = new Ebene(sortedebene, ebene.maxCCount);
                v.add(g, sortebene);
            }
            if(print)System.out.println("\nBSV Sortieren nach Type 'C''S'||'A'");
            //Sortieren nach Type 'C''S'||'A'
            for (int g=0; g<v.size(); g++) {
                if(print)System.out.println("\nEbene"+(g+1));
                Ebene ebene = v.get(g);
                // C = 0; S = 1; A = 2
                // histogramm anlegen
                Vector[] buckets = new Vector[3];
                // aufsummierogram anlegen
                int[] intbuckets = new int[3];

                for (int i=0; i<3; i++) {
                    buckets[i] = new Vector();
                }
                for (int i=0; i<ebene.size(); i++) {
                     Horizontale h = (Horizontale)ebene.get(i);
                     if(print)System.out.print(" e("+i+")="+h.parentKey+h.type+h.childcount);
                     if(h.type=='C')((Vector)buckets[2]).add(h);intbuckets[2]++;
                     if(h.type=='S')((Vector)buckets[1]).add(h);intbuckets[1]++;
                     if(h.type=='A')((Vector)buckets[0]).add(h);intbuckets[0]++;
                }
                //aufsummieren der anzahl
                for (int i=intbuckets.length-2; i>0; i--){
                    intbuckets[i] = intbuckets[i] + intbuckets[i+1];
                }
                //sortieren
                Vector<Horizontale_Flyweight> sortedebene = new Vector<Horizontale_Flyweight>();
                for (int i=buckets.length; i>0; i--) {
                     Iterator<Horizontale> it = ((Vector)buckets[i-1]).iterator();
                     while (it.hasNext()) {
                            sortedebene.add(it.next());
                     }
                }
                //update Positions
                for (int i=0; i<sortedebene.size(); i++){
                    Horizontale h = (Horizontale)sortedebene.get(i);
                    HPositionManager.updatePosition(h.key, i);
                }
                v.remove(g);
                Ebene sortebene = new Ebene(sortedebene, ebene.maxCCount);
                v.add(g, sortebene);
            }
            //Sortieren nach ParentIndex
            if(print)System.out.println("\nBSV Sortieren nach ParentIndex 'C''S'||'A'");
            for (int g=1; g<v.size(); g++) {
                if(print)System.out.println("\nEbene"+(g+1));
                Ebene ebene = v.get(g);
                int vorebeneCount = v.get(g-1).size();
                // histogramm anlegen
                Vector[] buckets = new Vector[vorebeneCount+1];
                // aufsummierogram anlegen
                int[] intbuckets = new int[vorebeneCount+1];

                for (int i=0; i<vorebeneCount+1; i++) {
                    buckets[i] = new Vector();
                }
                for (int i=0; i<ebene.size(); i++) {
                     Horizontale h = (Horizontale)ebene.get(i);
                     if(print)System.out.print(" e("+i+")="+h.parentKey+h.type+h.childcount);
                     int position  = HPositionManager.getCurrentPos(h.parentKey);
                     ((Vector)buckets[position]).add(h);
                     intbuckets[position]++;
                }
                //aufsummieren der anzahl
                for (int i=intbuckets.length-2; i>0; i--){
                    intbuckets[i] = intbuckets[i] + intbuckets[i+1];
                }
                //sortieren
                Vector<Horizontale_Flyweight> sortedebene = new Vector<Horizontale_Flyweight>();
                for (int i=0; i<buckets.length; i++) {
                     Iterator<Horizontale> it = ((Vector)buckets[i]).iterator();
                     while (it.hasNext()) {
                            sortedebene.add(it.next());
                     }
                }
                //update Positions
                for (int i=0; i<sortedebene.size(); i++){
                    Horizontale h = (Horizontale)sortedebene.get(i);
                    HPositionManager.updatePosition(h.key, i);
                }
                v.remove(g);
                Ebene sortebene = new Ebene(sortedebene, ebene.maxCCount);
                v.add(g, sortebene);
            }
            if(print)System.out.print("Sortieren Fertig");
        }

        // Wrap I/O exceptions in SAX exceptions, to
        // suit handler signature requirements
        private void showData(String s)
                throws SAXException, IOException {
            try {
                outWrite.write(s);
                outWrite.flush();
            } catch (IOException e) {
                throw new SAXException("I/O error", e);
            }
        }

        // Start a new line
        private void newLine()
                throws SAXException {
            String lineEnd = System.getProperty("line.separator");
            try {
                outWrite.write(lineEnd);
            } catch (IOException e) {
                throw new SAXException("I/O error", e);
            }
        }

        // Generation der String Vertikalen
        private void generateVerticale() {
            vertikaleV  = new Vector<Vertikale>();
            if(generateS)  vertikaleS  = new Vector<String>(); 
            if(generateSC) vertikaleSC = new Vector<String>();
            bucketsortVerticale();      
            if(!generateOtherV){
                vV2vS(v,0,0);
            }else{
                generateOtherVerticals();
            }
        }

        // Ableitung aller Anderen Vertikalen aus der Verikalen der Wurzel
        private void generateOtherVerticals() {    
            for(int i=0;i<v.size();i++){
                Ebene e = v.get(i);
                for(int j=0;j<e.size();j++){
                    vV2vS(v, i, j);
                }
            }
        }

        // Speichern der Vertikalen in einer Datei (vertikale.txt)
        public void writeOutVerticale(){
            String fileName = XML_FILE_NAME.substring((XML_FILE_NAME.lastIndexOf("/")+1), (XML_FILE_NAME.length()-4));
            File file = new File(classLoader.getResource("Ontologie/vertikale_"+fileName+".txt").getFile());
            try{
                out1 = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
            }catch(Exception e){
                System.out.println("FEHLER " + e);
                e.printStackTrace();
            }
            if(vertikaleSC!=null){
                for(int i=0;i<vertikaleSC.size();i++){
                        String vString = "CVertikale"+(i)+": "+vertikaleSC.get(i);
                    try {
                        out1.writeBytes(vString);
                        //UTF Führt hier zu unerwünschten Artefakten
                        //                out1.writeUTF(vString);
                    } catch (IOException ex) {
                        Logger.getLogger(XML2OWL.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            if(vertikaleS!=null){
                for(int i=0;i<vertikaleS.size();i++){
                        String vString = "Vertikale"+(i+1)+": "+vertikaleS.get(i);
                    try {
                        out1.writeBytes(vString);
                        //UTF Führt hier zu unerwünschten Artefakten
                        //                out1.writeUTF(vString);
                    } catch (IOException ex) {
                        Logger.getLogger(XML2OWL.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            try {
//                out1.writeBytes(horizontalenErstellung);
                if(verbous) out1.writeBytes(vertikalenErstellung);
                out1.flush();
            } catch (IOException ex) {
                Logger.getLogger(XML2OWL.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    } // end of inner class GraphMLHandler

} // end of class XMLGraphReader

