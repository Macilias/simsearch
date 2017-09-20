/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.macilias.apps.similaritysearch.logic.algorithms.graph;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.reasoner.rulesys.FBRuleReasoner;
import com.hp.hpl.jena.reasoner.rulesys.Rule.Parser;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

//import static org.apache.jena.assembler.JA.OntModelSpec;

/**
 *
 * @author maciekn
 */
public final class ModelManagement {


    //Konfiguration
    public static int[]    tr1 = {80,60,40};
    public static int      tr2 =  5;
    public static int      tr3 = 50;
    //Umschalten zwischen Inhaltlichen und Baum Vergleich
    public static boolean cORt = true;


    //Singelton:
    private static ModelManagement instance;

    public synchronized static ModelManagement getInstance(){
        if(instance==null) instance = new ModelManagement();
        return instance;
    }
//    Graph g;//base war: OWL_MEM m war: OWL_MEM_MICRO_RULE_INF
//    private OntModel base = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM_TRANS_INF);
    //ETWAS LANGSAM ABER SPEICHERT KEINE ALBLEITUNGEN UND LÄUFT SUPER!!!!
    private OntModel base = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);

//    private Model base = ModelFactory.createDefaultModel();

    //war OWL_MEM_MICRO_RULE_INF
    private OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF, base);

//    private String SOURCE= "http://www.analisa.eu/abstraktesProduktschema.owl";
//    protected String NS = SOURCE + "#";
    private String SOURCE_DataT  = "http://www.analisa.eu/abstractDatatype";
    protected String NSAD = SOURCE_DataT + "#";
    private String SOURCE_Data   = "http://www.analisa.eu/abstractAttribute";
    protected String NSAA = SOURCE_Data + "#";
    private String SOURCE_Scheme = "http://www.analisa.eu/abstractDatascheme";
    protected String NSAS = SOURCE_Scheme + "#";
    private String SOURCE_Config = "http://www.analisa.eu/SimSearch";
    protected String NSSS = SOURCE_Config + "#";

    public void startWithNewModel() throws FileNotFoundException{

            DataInputStream in = getDataInputStream("Ontologie/abstractDatascheme.owl");

            base.read(in, SOURCE_Scheme);
            base.setNsPrefix("aD", NSAD);
            base.setNsPrefix("aA", NSAA);
            base.setNsPrefix("aS", NSAS);
            base.setNsPrefix("sS", NSSS);
            
//            FBRuleReasoner reasoner = (FBRuleReasoner) m.getReasoner();
//            reasoner.loadAdditionalRules("aP", NS, "Ontologie/similarity.rules");
    }

    public void startWithSerializedModel() throws FileNotFoundException{

            DataInputStream in = getDataInputStream("Ontologie/abstractDatabase.owl");

            base.read(in, SOURCE_Scheme);
            base.setNsPrefix("aD", NSAD);
            base.setNsPrefix("aA", NSAA);
            base.setNsPrefix("aS", NSAS);
            base.setNsPrefix("sS", NSSS);
//            FBRuleReasoner reasoner = (FBRuleReasoner) m.getReasoner();
//            reasoner.loadAdditionalRules("aP", NS, "Ontologie/similarity.rules");

    }

    public static void setCorT(boolean cORt) {
        ModelManagement.cORt = cORt;
    }

    public static boolean isCorT() {
        return cORt;
    }

    public static int[] getTr1() {
        return tr1;
    }

    private DataInputStream getDataInputStream(String name) throws FileNotFoundException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(name).getFile());
        return new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
    }

    public static void setTr1(int[] tr1) {
        ModelManagement.tr1 = tr1;
    }

    public static int getTr2() {
        return tr2;
    }

    public static void setTr2(int tr2) {
        ModelManagement.tr2 = tr2;
    }

    public static int getTr3() {
        return tr3;
    }

    public static void setTr3(int tr3) {
        ModelManagement.tr3 = tr3;
    }

    private ModelManagement() {
    }

    public OntModel getModel() {
        return m;
//        return base;
    }
    //DEFAULT SIEHE OBEN LIEF SUPER! - versuche es noch mit Model statt OntModel als Base
    public OntModel getBase() {
        return base;
    }

//    public Model getBase() {
//        return base;
//    }

    public void saveModels() {
        DataOutputStream out1;
//        DataOutputStream out2;
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(classLoader.getResource("Ontologie/abstractDatabase.owl").getFile());
            out1 = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
//            out1.writeUTF(xmlDoctype);
            //WAR BISLANG:
            out1.writeBytes(xmlDoctype);
            base.write(out1);
           /*
            * Die Alte Funktion hier speichert nicht die abgeleiteten Daten
            * m.write(out2);
            */
            //TODO Häbel Zur Performanz kann man das Auskommentieren:
//            out2 = new DataOutputStream(new BufferedOutputStream(new FileOutputStream("Ontologie/abstraktesProduktschema2.owl")));
//            out2.writeBytes(xmlDoctype);
//            m.writeAll(out2,null,NS);

        } catch (IOException ex) {
            Logger.getLogger(Parser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static String xmlDoctype = "<?xml version=\"1.0\"?>" + "\n"
            + "<!DOCTYPE rdf:RDF [" + "\n"
            + "<!ENTITY owl \"http://www.w3.org/2002/07/owl#\" >" + "\n"
            + "<!ENTITY xsd \"http://www.w3.org/2001/XMLSchema#\" >" + "\n"
            + "<!ENTITY owl2xml \"http://www.w3.org/2006/12/owl2-xml#\" >" + "\n"
            + "<!ENTITY aS \"http://www.analisa.eu/abstractDatascheme#\" >" + "\n"
            + "<!ENTITY aD \"http://www.analisa.eu/abstractDatatype#\" >" + "\n"
            + "<!ENTITY SS \"http://www.analisa.eu/SimilaritySearch#\" >" + "\n"
            + "<!ENTITY rdfs \"http://www.w3.org/2000/01/rdf-schema#\" >" + "\n"
            + "<!ENTITY rdf \"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" >" + "\n"
            + "]>" + "\n";
}