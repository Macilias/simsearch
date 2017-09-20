/*
 * Ziel des Recivers ist das Umwandeln der Zwischenergebnisse in folgende Form:
 * <div class="item">
 *   <img class="content" src="pic/xml256_256.png" title="Mini Example 3" href="test_data/mini_example3.xml"/>
 * </div>
 *
 */
package com.macilias.apps.similaritysearch;

import java.util.TreeMap;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Literal;
import java.awt.Desktop;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.macilias.apps.similaritysearch.logic.algorithms.graph.ModelManagement;
import com.macilias.apps.similaritysearch.util.SoutConfig;
import com.macilias.apps.similaritysearch.util.V2List;

/**
 *
 * @author Maciej Niemczyk
 */
public class ResultReciver {

    V2List<Result> results = new V2List<Result>();
    LinkedList<String> scripts = new LinkedList<String>();
    public LinkedList<Individual> thresh = new LinkedList<Individual>();
    int count = 0;
    boolean copyFound = false;
    static boolean calculation = SoutConfig.getCalculation_RR();
    static boolean aBitVerbous = SoutConfig.getaBitVerbous_RR();
    static boolean verbous = SoutConfig.getVerbous_RR();
    static boolean khadija = SoutConfig.getKhadija_RR();
    static boolean beQuiet = SoutConfig.getRRQuietMode();
    public final String PIC_CURRENT = "pic/xml_current.png";
    public final String PIC_DELETED = "pic/xml_deleted.png";
    public final String PIC_CANDIDATE = "pic/xml_candidate.png";
    ClassLoader classLoader = getClass().getClassLoader();

    private static void resetOutPrint() {
        calculation = SoutConfig.getCalculation_RR();
        aBitVerbous = SoutConfig.getaBitVerbous_RR();
        verbous = SoutConfig.getVerbous_RR();
        khadija = SoutConfig.getKhadija_RR();
        beQuiet = SoutConfig.getRRQuietMode();
    }

    public ResultReciver() {
        try {
            initialiseScript();
            resetOutPrint();
            Desktop.getDesktop().open(new File(classLoader.getResource("result_data/liveResults.html").getFile()));
        } catch (IOException io) {
            System.out.println("Es gab ein I/O Problem - Schreibrechte setzen!");
            System.out.println("Der Teilordner \"result_data\" nicht beschreibar");
        }

    }

    public void recive(Individual i, int sim) {
        if (i != null) {
            Result r = new Result(i, sim);
            boolean fordelete = false;
            if (thresh.contains(i)) {
                r.picPath = PIC_DELETED;
                fordelete = true;
            }
            int oldPos = -1;
            int newPos = -1;
            int p = 0;
            /*
             * Empfangenes individeum kann folgendes sein:
             * 1. Ein neues Element
             * 2. Ein bereits empfangenes Element mit gleicher Ähnlichkeit
             * 3. Ein bereits empfangenes Element mit upgedateter Ähnlichkeit
             * Postulate:
             * 2. Elemente, welche vom System entfernt werden sollen haben
             *    die gleiche Ähnlichkeit und Dateinamen wie das ausg. Dokument
             *    -> um diese aufzunehmen muss ein Unterscheidungskriterium her
             * gleicher Pfad und Ähnlichkeit -> ignorieren
             *    -> es sei den r markiert fordelete und r2 NICHT!
             * gleicher Pfad underschiedliche Ähnlichkeit -> Position updaten
             *    -> oldPos == newPos && oldPos != -1 -> ignorieren
             *       -> fordels sollen rechts erscheinden -> oldPos == newPos+1
             *    -> oldPos == -1 -> war bislang nicht drin
             *    -> newPos == -1 -> es gab kein (&&||kleineres) Element
             *       -> oldPos == -1 && newPos != -1 -> einfügen(newPos)
             *       -> oldPos == -1 && newPos == -1 -> einfügen(ende)
             *       -> oldPos != -1 == newPos != -1 -> ignorieren
             *       -> oldPos != -1 != newPos != -1 -> bewegen(oldPos->newPos)
             *       -> oldPos != -1 != newPos == -1 -> bewegen(oldPos->ende)
             */
            for (Iterator<Result> it = results.iterator(); it.hasNext(); p++) {
                //rememberCurrent muss vor .next() aufgerufen werden da iteration
                //mit .next() den curret auf nächsten setzt für hasNext()
                results.rememberCurrentWhileIteration();
                Result r2 = it.next();
                if (r.fullPath.equals(r2.fullPath)) {
                    results.rememberCurrent();
                    oldPos = p;
                    if (r.sim == r2.sim) {
                        if (!fordelete || this.copyFound) {
                            newPos = p;
                            break;
                        } else {
                            oldPos = -1;
                            newPos = p + 1;
                            this.copyFound = true;
                            break;
                        }
                    }
                }
                //r.sim!=0 sorgt dafür das 0Ähnlichkeiten nicht verschoben werden
                if (r.sim > r2.sim && newPos==-1 && r.sim!=0) {
                    if (verbous) System.out.println("RR Setze Position bei "+r.title+" mit Ähnlichkeit = "+r.sim+" auf "+p+" alte Position = "+oldPos);
                    results.rememberCurrent();
                    newPos = p;
                }
            }
            if (!(oldPos == newPos && newPos != -1)) {
                if (oldPos == -1) {
                    if (newPos != -1) {
                        if (calculation) System.out.println("RR einfügen(" + newPos + ") von " + r.i.getLocalName());
                        results.add(r, newPos);
//                        wirteResults();
                    } else {
                        if (calculation) System.out.println("RR einfügen(ende) von " + r.i.getLocalName());
                        results.add(r);
//                        wirteResults();
                    }
                } else {
                    if (newPos != -1) {
                        if (calculation) System.out.println("RR bewegen(" + oldPos + "->" + newPos + ") von " + r.i.getLocalName());
                        if (!results.updateRememberedTo(r, newPos)) {
                            System.out.println("RR FEHLER beim bewegen(" + oldPos + "->" + newPos + ") von " + r.i.getLocalName());
                        } else {
//                            wirteResults();
                        }
                    } else {
                        if (calculation) System.out.println("RR bewegen(" + oldPos + "->ende) von " + r.i.getLocalName());
                        if (!results.updateRememberedEnd(r)) {
                            System.out.println("RR FEHLER beim bewegen(" + oldPos + "->ende) von " + r.i.getLocalName());
                        } else {
//                            wirteResults();
                        }
                    }
                }
            } else {
                if (calculation) System.out.println("RR Ignorieren von " + r.i.getLocalName());
                
            }
            if(calculation) results.printOut();
        } else {
            wirteResults();
            terminate();
        }
    }

    private void initialiseScript() {
        DataOutputStream out1 = null;
        try {
            File scriFile = new File(classLoader.getResource("result_data/" + "script.js").getFile());
            String script = "<script type=\"text/javascript\">\n"
                    + //                            "  alert(\"SSI count=\"+zaehler);\n"+
                                                "  if(zaehler == 0) {\n"+
                                                "     zaehler++;\n"+
                                                "     getContent();\n"+
                                                "  }\n"+
                                                "  pausecomp(10);\n"+
//                    "  zaehler=0;\n"
                     "  reloadScript();\n"+
                     "</script>";
            out1 = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(scriFile)));
            try {
                out1.writeBytes(script);
                if(verbous || aBitVerbous){
                    System.out.println("Script beim Counter =" + count + " INIT");
                    System.out.println(script);
                }

            } catch (IOException ex) {
                Logger.getLogger(ResultReciver.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                out1.flush();
            } catch (IOException ex) {
                Logger.getLogger(ResultReciver.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ResultReciver.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            count = 1;
            if(verbous) System.out.println("RR Zähler zurückgesetzt " + count);
            try {
                if (out1 != null) {
                    out1.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(ResultReciver.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        wirteResults();
    }

    private void addResult(Result r, int newPos) {
        DataOutputStream out1 = null;
        try {
            File file = new File(classLoader.getResource("result_data/" + "script.js").getFile());
            out1 = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
            String rString = "<div class=\"item\">";
            rString = rString + "  <img class=\"content\" src=\"" + r.picPath + "\" title=\"" + r.title + " (" + r.sim + ")" + "\" href=\"../" + r.fullPath + "\"/>";
            rString = rString + "</div>";
//            String rString = "<div class=\"flow\">";
//            rString=rString+ "<a class=\"item\" href=\"../"+r.fullPath+"\"><img class=\"content\" src=\""+r.picPath+"\" title=\""+r.title+" ("+r.sim+")"+"\"/></a>\n";
//            rString=rString+ "</div>";
//            String rString = "<a class=\"item\" href=\"../"+r.fullPath+"\"><img class=\"content\" src=\""+r.picPath+"\" title=\""+r.title+" ("+r.sim+")"+"\"/></a>";
//            String comand = "ajax_cf.addItem(\""+rString+"\","+newPos+");";
//            String comand = "addItem("+rString+","+newPos+");";
//            String comand = "addItem(unescape("+r.title+"),unescape("+r.fullPath+"),unescape("+r.picPath+"),"+r.sim+","+r.sim+","+newPos+");";
//            String comand = "addItem("+r.title+","+r.fullPath+","+r.picPath+","+r.sim+","+r.sim+","+newPos+")";
//            String comand = "addItem(\""+r.title+"\",\""+r.fullPath+"\",\""+r.picPath+"\","+r.sim+","+r.sim+","+newPos+");";
//            String comand = "addItem(\""+r.title+"\",\""+r.fullPath+"\",\""+r.picPath+"\",\""+r.sim+"\",\""+r.sim+"\",\""+newPos+"\");";
//            String comand = "addItem(\""+r.title+"\",\""+r.fullPath+"\",\""+r.picPath+"\","+r.sim+","+r.sim+","+newPos+");";
            String comand = "addItemHTML(" + rString + ");";
            scripts.add(comand);
            String script = "<script type=\"text/javascript\">\n"
                    + //                            "  alert(\"SSW count=\"+zaehler);\n"+
                    "  if(zaehler <= " + count + ") {\n"
                    + "     zaehler_prev = zaehler;\n"
                    + //                            "     zaehler = "+count+";\n";
                    "     zaehler = " + count + ";\n"
                    + "     scripts = new Array(";
            Iterator<String> scrit = scripts.iterator();
            boolean erster = true;
            while (scrit.hasNext()) {
                if (erster) {
                    script += "\"" + scrit.next() + "\"";
//                    script += scrit.next();
                    erster = false;
                } else {
                    script += ",\"" + scrit.next() + "\"";
//                    script += ","+scrit.next();
                }
            }
            script = script + ");\n"
                    + //            script=script + "\n"+
                    "     loadScripts(scripts);\n"
                    + //                            "     loadScripts();\n"+
                    "  }\n"
                    + "  reloadScript();\n"
                    + "</script>";
            try {
                out1.writeBytes(script);
                if(verbous || aBitVerbous){
                    System.out.println("Script beim Counter =" + count + " ADD RESULT");
                    System.out.println(script);
                }
            } catch (IOException ex) {
                Logger.getLogger(ResultReciver.class.getName()).log(Level.SEVERE, null, ex);
            }

            try {
                out1.flush();
            } catch (IOException ex) {
                Logger.getLogger(ResultReciver.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ResultReciver.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                out1.close();
                count++;
            } catch (IOException ex) {
                Logger.getLogger(ResultReciver.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if(verbous) System.out.println("RR Anzahl der Updates:" + count);
    }

    private void updateResults(Result r, int newPos, int oldPos) {
        DataOutputStream out1 = null;
        try {
            File file = new File(classLoader.getResource("result_data/" + "script.js").getFile());
            out1 = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
            String rString = "<div class=\"item\">";
            rString = rString + "  <img class=\"content\" src=\"" + r.picPath + "\" title=\"" + r.title + " (" + r.sim + ")" + "\" href=\"../" + r.fullPath + "\"/>";
            rString = rString + "</div>";
//            String rString = "<div class=\"flow\">";
//            rString=rString+ "  <a class=\"item\" href=\"../"+r.fullPath+"\"><img class=\"content\" src=\""+r.picPath+"\" title=\""+r.title+" ("+r.sim+")"+"\"/>";
//            rString=rString+ "</div>";
//            String rString = "<a class=\"item\" href=\"../"+r.fullPath+"\"><img class=\"content\" src=\""+r.picPath+"\" title=\""+r.title+" ("+r.sim+")"+"\"/></a>";
            String comand = "rmItem(" + oldPos + ");";
//            String comand2= "ajax_cf.addItem(\""+rString+"\","+newPos+");";
//            String comand2= "addItem(unescape("+r.title+"),unescape("+r.fullPath+"),unescape("+r.picPath+"),"+r.sim+","+r.sim+","+newPos+");";
//            String comand2= "addItem(unescape("+r.title+"),unescape("+r.fullPath+"),unescape("+r.picPath+"),"+r.sim+","+r.sim+","+newPos+");";
//            String comand2= "addItem("+r.title+","+r.fullPath+","+r.picPath+","+r.sim+","+r.sim+","+newPos+")";
//            String comand2 = "addItem(\""+r.title+"\",\""+r.fullPath+"\",\""+r.picPath+"\","+r.sim+","+r.sim+","+newPos+");";
//            String comand2 = "addItem(\""+r.title+"\",\""+r.fullPath+"\",\""+r.picPath+"\",\""+r.sim+"\",\""+r.sim+"\",\""+newPos+"\");";
//            String comand2 = "addItem(\""+r.title+"\",\""+r.fullPath+"\",\""+r.picPath+"\","+r.sim+","+r.sim+","+newPos+");";
            String comand2 = "addItemHTML(unescape(\"" + rString + "\"));";
            scripts.add(comand);
            scripts.add(comand2);
            String script = "<script type=\"text/javascript\">\n"
                    + //                            "  alert(\"SSW count=\"+zaehler);\n"+
                    "  if(zaehler <= " + count + ") {\n"
                    + "     zaehler_prev = zaehler;\n"
                    + //                            "     zaehler = "+count+";\n";
                    "     zaehler = " + count + ";\n"
                    + "     scripts = new Array(";
            Iterator<String> scrit = scripts.iterator();
            boolean erster = true;
            while (scrit.hasNext()) {
                if (erster) {
                    script += "\"" + scrit.next() + "\"";
//                    script += scrit.next();
                    erster = false;
                } else {
                    script += ",\"" + scrit.next() + "\"";
//                    script += ","+scrit.next();
                }
            }
            script = script + ");\n"
                    + //            script=script + "\n"+
                    "     loadScripts(scripts);\n"
                    + //                            "     loadScripts();\n"+
                    "  }\n"
                    + "  reloadScript();\n"
                    + "</script>";

            try {
                out1.writeBytes(script);
                if(verbous || aBitVerbous){
                    System.out.println("Script beim Counter =" + count + " Update oldPos=" + oldPos + " newPos=" + newPos);
                    System.out.println(script);
                }
            } catch (IOException ex) {
                Logger.getLogger(ResultReciver.class.getName()).log(Level.SEVERE, null, ex);
            }

            try {
                out1.flush();
            } catch (IOException ex) {
                Logger.getLogger(ResultReciver.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ResultReciver.class.getName()).log(Level.SEVERE, null, ex);
        } finally {

            try {
                out1.close();
                count++;
            } catch (IOException ex) {
                Logger.getLogger(ResultReciver.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if(verbous || aBitVerbous) System.out.println("RR Anzahl der Updates:" + count);
    }

    private void updateContent() {
        DataOutputStream out1 = null;
        try {
            File file = new File(classLoader.getResource("result_data/" + "script.js").getFile());
            out1 = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));

            String script = "<script type=\"text/javascript\">\n"
                    +"  if(zaehler < " + count + ") {\n"
                    + "     zaehler_prev = zaehler;\n"
                    +"      zaehler = "+count+";\n"
                    +"      getContent();\n"
                    +"      pausecomp(50);\n"
                    +"  }\n"
                    + "  reloadScript();\n"
                    + "</script>";

            try {
                out1.writeBytes(script);
//                System.out.println("Script beim Counter =" + count + " Update oldPos=" + oldPos + " newPos=" + newPos);
                if(verbous || aBitVerbous) System.out.println(script);
            } catch (IOException ex) {
                Logger.getLogger(ResultReciver.class.getName()).log(Level.SEVERE, null, ex);
            }

            try {
                out1.flush();
            } catch (IOException ex) {
                Logger.getLogger(ResultReciver.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ResultReciver.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            count++;
            try {
                out1.close();
            } catch (IOException ex) {
                Logger.getLogger(ResultReciver.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if(verbous) System.out.println("RR Anzahl der Updates:" + count);
    }

    public void wirteResults() {
        DataOutputStream out1 = null;
        try {
            File file = new File(classLoader.getResource("result_data/" + "script.js").getFile());
            out1 = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
            if (results != null) {
                for (Iterator<Result> resIt = results.iterator(); resIt.hasNext();) {
                    Result r = resIt.next();
                    String rString = "<div class=\"item\">\n";
                    rString = rString + "  <img class=\"content\" src=\"" + r.picPath + "\" title=\"" + r.title + " (" + r.sim + ")" + "\" href=\"" + "../" + r.fullPath + "\"/>\n";
                    rString = rString + "</div>\n";
                    if(verbous || calculation) System.out.println(rString);
                    try {
                        out1.writeBytes(rString);
                        //UTF Führt hier zu unerwünschten Artefakten
                        //                out1.writeUTF(rString);
                    } catch (IOException ex) {
                        System.out.println("Fehler beim Schreiben");
                        Logger.getLogger(ResultReciver.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            try {
                out1.flush();
            } catch (IOException ex) {
                Logger.getLogger(ResultReciver.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ResultReciver.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                out1.close();
            } catch (IOException ex) {
                Logger.getLogger(ResultReciver.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        updateContent();
    }

    private void terminate() {
        DataOutputStream out1 = null;
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            File scriFile = new File(classLoader.getResource("result_data/" + "script.js").getFile());
            out1 = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(scriFile)));
            String script = "<script type=\"text/javascript\">\n"+
                    "  if(zaehler < "+count+") {\n"+
                    "     getContent();\n"+
                    "  }\n"+
                    "  zaehler = 0;\n"+
                    "</script>";

            try {
                out1.writeBytes(script);
                if(verbous || aBitVerbous){
                    System.out.println("Script beim Counter =" + count + " Terminate");
                    System.out.println(script);
                }
            } catch (IOException ex) {
                Logger.getLogger(ResultReciver.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                out1.flush();
            } catch (IOException ex) {
                Logger.getLogger(ResultReciver.class.getName()).log(Level.SEVERE, null, ex);
            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(ResultReciver.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            count = 0;
            copyFound = false;
            results = new V2List<Result>();
            thresh = new LinkedList<Individual>();
            scripts = new LinkedList<String>();
            if(verbous || aBitVerbous) System.out.println("RR ENDE Anzahl der Updates:" + count);
            try {
                out1.close();
            } catch (IOException ex) {
                Logger.getLogger(ResultReciver.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}

class Result implements Comparable {

    int sim;
    Individual i;
    String title;
    String fullPath;
    String picPath = "pic/xml_candidate.png";

    public Result(Individual i, int sim) {
        OntModel m = ModelManagement.getInstance().getModel();
        String NSAS = m.getNsPrefixURI("aS");
        this.i = i;
        if (sim == 101) {
            sim = 100;
            picPath = "pic/xml_current.png";
        }
        this.sim = sim;
        Literal fpl = (Literal) i.getPropertyValue(m.getProperty(NSAS + "from_XML_FILE_FP"));
        this.fullPath = fpl.getString();
        Literal nl = (Literal) i.getPropertyValue(m.getProperty(NSAS + "from_XML_FILE"));
        this.title = nl.getString().toUpperCase().replace("_", " ");
    }

    @Override
    public int compareTo(Object t) {
        Result rt = (Result) t;
        int res = 0;
        if (i.equals(rt.i)) {
            System.out.println("RR: i=" + i.getLocalName() + " rt.i=" + rt.i.getLocalName() + " EQUALS");
            return 0;
        } else {
            System.out.println("RR: i=" + i.getLocalName() + " rt.i=" + rt.i.getLocalName() + " NOT EQUALS");
            if (sim != rt.sim) {
                if (sim < rt.sim) {
                    return -1;
                } else {
                    return 1;
                }
            } else {
                //-1 steht für platziere es dahinter
                return -1;
            }
        }
    }
//    @Override
//    public int compareTo(Object t) {
//        Result rt = (Result)t;
//        int res = 0;
//        if(this.sim < rt.sim){
//            return -1;
//        }else{
//            if(this.sim > rt.sim){
//                return 1;
//            }else{
//                return title.compareTo(rt.title);
//            }
//        }
//    }
}
