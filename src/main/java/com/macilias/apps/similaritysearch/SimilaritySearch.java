/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.macilias.apps.similaritysearch;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.NsIterator;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.macilias.apps.similaritysearch.data.XML2OWL;
import com.macilias.apps.similaritysearch.logic.algorithms.graph.ContentSimilarity;
import com.macilias.apps.similaritysearch.logic.algorithms.graph.ModelManagement;
import com.macilias.apps.similaritysearch.logic.algorithms.graph.TreeSimilarity_Relations;

/**
 *
 * @author Maciej Niemczyk
 */
public class SimilaritySearch {
    ModelManagement mm;
    XML2OWL parser;
    boolean isCorT = mm.isCorT();

    public SimilaritySearch(String file){
        file = file.trim();
        if(file.endsWith(".xml") || file.endsWith(".XML")){
            mm = ModelManagement.getInstance();
            parser = new XML2OWL(file);
            this.runSimSearch();
        }else{
            System.out.println("Das System kann alles lesen was ein XML Parser");
            System.out.println("parsen kann. Falls Ihre Daten auf XML beruhen,");
            System.out.println("aber eine andere Endung haben, ändern Sie diese");
        }
    }

    //visual hat folgende Belegung:
    //-1: keine Visualisierung
    // 0: Tree & Graph
    // 1: nur Tree
    // 2: nur Graph
    public SimilaritySearch(int key){
        mm = ModelManagement.getInstance();
        Set<String> ns = mm.getModel().listImportedOntologyURIs();
        Iterator nsit  = ns.iterator();
        while(nsit.hasNext()){
            System.out.println(nsit.next());
        }

        if(key==810)  parser = new XML2OWL("CD lite");
        if(key==811)  parser = new XML2OWL("CD full");
        if(key==812)  parser = new XML2OWL("Produktstrukturen S");
        if(key==813)  parser = new XML2OWL("Produktstrukturen M");
        if(key==814)  parser = new XML2OWL("Produktstrukturen XXL");
        if(key==8110) parser = new XML2OWL("Baumsignaturen");
        if(key==8111) parser = new XML2OWL("Mini Example 1");
        if(key==8112) parser = new XML2OWL("Mini Example 2");
        if(key==8113) parser = new XML2OWL("Mini Example 3");
        if(key==8115) parser = new XML2OWL("Micro Example A");
        if(key==8116) parser = new XML2OWL("Micro Example B");
        if(key==8117) parser = new XML2OWL("Nano  Example A");
        if(key==8118) parser = new XML2OWL("Nano  Example B");
        if(key==8119) parser = new XML2OWL("XHTML Example");
        if(parser!=null && parser.XML_FILE_NAME!=null){
            this.runSimSearch();
        }else{
            System.out.println("Die Nummer ist nicht zugeordnet");
        }

    }

    private void runSimSearch(){
            String XML_FILE_NAME = parser.XML_FILE_NAME;
            String filename = XML_FILE_NAME.substring((XML_FILE_NAME.lastIndexOf("/")+1), XML_FILE_NAME.length());
            boolean allesOk = true;
            boolean machmal = true;
            boolean kannteSieSchon = false;
            OntModel m = mm.getModel();
            BufferedReader din = new BufferedReader(new InputStreamReader(System.in));
            if((m.listSubjectsWithProperty(m.getProperty(m.getNsPrefixURI("aS")+"from_XML_FILE"), filename)).hasNext()){
                kannteSieSchon = true;
//                System.out.println("Die Datei "+filename+" worde bereits eingelesen - soll die Datei dennoch überprüft werden? (j/n)");
                System.out.println("Die Datei "+filename+" ist bereits eingelesen");
//                String str1;
//                try {
//                    str1 = din.readLine();
//                    if(!str1.contains("j")) machmal = false;
//                } catch (IOException ex) {
//                    Logger.getLogger(SimilaritySearch.class.getName()).log(Level.SEVERE, null, ex);
//                }
            }
            if(kannteSieSchon){
//                if(machmal){
                    boolean wiederhole = true;
                    if(!isCorT){
                        while(wiederhole){
                            try {
                                wiederhole = false;
                                String str = "";
                                System.out.println("Soll der Vergleich Top-Down oder Bottom-Up erfolgen?");
                                System.out.println("Top-Down  - Suche durch Relationen (empfohlen)");
                                System.out.println("Bottom-Up - Suche nach Ähnderungen (neu einlesen)");
                                System.out.println("für Top-Down \"t\" und für Bottom-Up \"b\" eingeben");
                                str = din.readLine();
                                if (str.contains("t")) {
                                    //                            saxParser.parse(is, handler);
                                    System.out.println("UNSUPPORTED JET");
                                    System.out.println("(implizit im Bottom-Up drin)");
//                                    parser.parse();
//                                    TreeSimilarity_Relations.handleDifferencesTopDown(filename);
                                } else {
                                    if (str.contains("b")) {
                                        //                                saxParser.parse(is, handler);
                                        HashSet<String> atts = (HashSet<String>) parser.parse();
                                        if(atts.size()>0){
                                            TreeSimilarity_Relations.findSimilarAndEquivalentIndividuals(atts);
                                            TreeSimilarity_Relations.handleDifferencesBottomUp(filename);
                                        }else{
                                            allesOk = false;
                                        }
                                    } else {
                                        System.out.println("Die Eingabe war nicht korrekt (nur t oder b erlaubt)");
                                        wiederhole = true;
                                    }
                                }
                            } catch (IOException ex) {
                                Logger.getLogger(SimilaritySearch.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    }else{
                        wiederhole = false;
                        String str = "";
                        System.out.println("Soll der Vergleich Top-Down oder Bottom-Up erfolgen?");
                        System.out.println("Top-Down  - Suche durch Relationen (empfohlen)");
                        System.out.println("Bottom-Up - Suche nach Ähnderungen (neu einlesen)");
                        System.out.println("für Top-Down \"t\" und für Bottom-Up \"b\" eingeben");
                        try {
                            str = din.readLine();
                        } catch (IOException ex) {
                            Logger.getLogger(SimilaritySearch.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        if (str.contains("b")){
                            LinkedList<String> atts = (LinkedList<String>) parser.parse();
                            if(atts.size()>0){
                                ContentSimilarity.findSimilarAndEquivalentXMLDocuments(parser.rootURI, atts, true);
                            }else{
                                allesOk = false;
                            }
                        }else{
                                ContentSimilarity.handleDifferencesTopDown(filename);
                                allesOk = true;
                        }
                    }
//                }
            }else{
                if(isCorT){
                    LinkedList<String> atts = (LinkedList<String>) parser.parse();
                    if(atts.size()>0){
                        ContentSimilarity.findSimilarAndEquivalentXMLDocuments(parser.rootURI, atts, false);
                    }else{
                        allesOk = false;
                    }
                }else{
                    HashSet<String> atts = (HashSet<String>) parser.parse();
                    if(atts.size()>0){
                        TreeSimilarity_Relations.findSimilarAndEquivalentIndividuals(atts);
                    }else{
                        allesOk = false;
                    }
                }
            }
            parser = null;
            if(allesOk) mm.saveModels();
    }

}
//        if(visual>=0&&visual<10){
//            parser = new XML2OWL(null);
//            //MyDemo d = new MyDemo( mm.getModel(), visual);
//        }
//        if(visual>=10&&visual<100){
//            TreeEquality.findEquivalentIndividuals(mm.getModel());
////            MyDemo d = new MyDemo( mm.getModel(), visual%10);
//        }
//        if(visual>=100&&visual<1000){
//            if(visual==110||visual==210)parser = new XML2OWL("CD");
//            if(visual==120||visual==220)parser = new XML2OWL("Produktstrukturen S");
//            if(visual==130||visual==230)parser = new XML2OWL("Produktstrukturen M");
////            MyDemo d = new MyDemo( mm.getModel(), (visual%10)%10);
//        }
