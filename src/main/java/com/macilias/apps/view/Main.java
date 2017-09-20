/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.macilias.apps.view;

import com.hp.hpl.jena.rdf.model.StmtIterator;
import java.awt.Desktop;
import java.io.*;
import java.util.Iterator;

import com.macilias.apps.similaritysearch.SimilaritySearch;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import com.macilias.apps.similaritysearch.algotest.gui.testingGUI;
import com.macilias.apps.similaritysearch.data.XML2OWL;
import com.macilias.apps.similaritysearch.logic.algorithms.graph.ModelManagement;
import com.macilias.apps.similaritysearch.logic.algorithms.lexical.MatrixBasedCompare;
import com.macilias.apps.similaritysearch.util.SoutConfig;
import com.macilias.apps.similaritysearch.util.URITool;
import com.macilias.apps.similaritysearch.util.V2List;

/**
 *
 * @author maciekn
 */
public class Main {
    static Logger loggerBrain = Logger.getLogger(Main.class);
    static Logger logger = Logger.getLogger("SimSearch");
    static SimpleLayout layout = new SimpleLayout();

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        boolean ende = false;
        BasicConfigurator.configure();
        loggerBrain.getRootLogger().removeAllAppenders();
        FileAppender fileAppender = new FileAppender( layout, "logs/Brain.log", false);
        loggerBrain.getRootLogger().addAppender(fileAppender);
        logger.getRootLogger().removeAllAppenders();
        FileAppender fileAppenderS = new FileAppender( layout, "logs/SimSearch.log", false);
        logger.getRootLogger().addAppender(fileAppenderS);
        SimilaritySearch t;
        boolean nochmal = true;
        final String s1 = "GAATTCAGTTA";
        final String s2 = "GGATCGA";
        String strTemp = "";
        int key = 0;
        BufferedReader din = new BufferedReader(
                             new InputStreamReader(System.in));
        boolean readMore = true;
        if(args==null || args.length==0){
            args = new String[2];
            args[0] ="";
            args[1] ="";
        }
        try{
            if(!args[0].equals("skip") && !args[0].equals("skipall")){
                System.out.println("*********************************************");
                System.out.println("***WELLCOME TO SIMILARITY SEARCH FRAMEWORK***");
                System.out.println("***©Maciej Niemczyk @ University of Siegen***");
                System.out.println("*********************************************");
                System.out.println("*                                           *");
                System.out.println("*   Model laden (l) oder neu anlegen (n)?   *");
                String load = din.readLine();
                if(load.contains("l") || load.contains("L") || load.trim().equalsIgnoreCase("j")){
                    try{
                        ModelManagement.getInstance().startWithSerializedModel();
                    }catch(FileNotFoundException e){
                        System.out.println("Es ist kein Datenmodel vorhanden!");
                        System.out.println("Ontologie/abstractDatabase.owl");
                        //Es muss ganz am Anfang beginnen, wir haben noch kein Datenmodel
                        main(args);
                    }
                }else{
                    if(load.equalsIgnoreCase("n")||load.equalsIgnoreCase("N")){
                        System.out.println("VORSICHT! Bestehendes Datenmodel wird dadurch ");
                        System.out.println("gelöscht! Fortfahren? (j/n)");
                        load = din.readLine();
                        if(load.contains("j")||load.contains("J")){
                            try{
                                ModelManagement.getInstance().startWithNewModel();
                            }catch(FileNotFoundException e){
                                System.out.println("Die Datenschemadatei ist nicht vorhanden!");
                                System.out.println("Ontologie/abstractDatascheme.owl");
                                //Es muss ganz am Anfang beginnen, wir haben noch kein Datenmodel
                                main(args);
                            }
                        }else{
                            //Es muss ganz am Anfang beginnen, wir haben noch kein Datenmodel
                            main(args);
                        }
                    }else{
                        System.out.println("Die Eingabe war nicht korrekt! (l/n)");
                        //Es muss ganz am Anfang beginnen, wir haben noch kein Datenmodel
                        main(args);
                    }
                }
            }
            if(!args[0].equals("skipall")){
                if(args[1].equals("")){
                    while(readMore){
                        System.out.println("----------------------------------------------");
                        System.out.println("* neue Datei verleichen (drag a file inside) *");
                        System.out.println("* oder ins Menü (m) Dateipfad/Menü=m/Ende=99 *");
                        System.out.println("----------------------------------------------");
                        String file = din.readLine();
                        if(!file.trim().equalsIgnoreCase("m") && !file.contains("99")){
                            System.out.println("Starte Ähnlichkeitensuche");
                            t = new SimilaritySearch(file);
                            t = null;
                        }else{
                            if(file.contains("99")){
                                readMore=false;
                                nochmal =false;
                                ende    =true;
                            }else{
                                readMore=false;
                            }
                        }
                    }
                }else{
                    //               System.out.println("GOAL");
                    if(!args[1].equals("99")){
                        t = new SimilaritySearch(args[1]);
                        args[0] = "skip";
                        args[1] = "";
                        t = null;
                        main(args);
                    } else {
                        System.exit(0);
                    }
                }
            }
            while(nochmal){
                MatrixBasedCompare code = new MatrixBasedCompare();
                String str1,str2;

                if(!strTemp.trim().equalsIgnoreCase("")&&!strTemp.contains("n")&&!strTemp.contains("y")&&!strTemp.trim().equalsIgnoreCase("99")){
                    try{
                        key =  Integer.valueOf(strTemp);
                    }catch(NumberFormatException e){
                        System.out.println("Bitte als Zahl eingeben!");
                        args[0]="skipall";
                        main(args);
                    }
                }else{
                    System.out.println("---------------------------------------------");
                    System.out.println("Bitte wählen Sie eine der folgenden NRn. aus: ");
                    System.out.println("____________Linear Edit Dist (LED)___________");
                    System.out.println("01 (String) = Levenstein");
                    System.out.println("________Lineare Werte Distanzen (LWD)________");
                    System.out.println("21 (String) = Needleman/Wunsch (GLAOBAL SA)");
                    System.out.println("________________Test Suite___________________");
                    System.out.println("40 (String) = Long to Alphabet Code (base=26)");
                    System.out.println("41 (String) = String to valid URI #Fragment");
                    System.out.println("42 (String) = V2List Test");
                    System.out.println("45 (String) = Gebe alle Statemants des m aus");
                    System.out.println("___________Ähnlichkeiten Suche_______________");
                    System.out.println("50 Baum-Ähnlichkeit oder Inhalt-Ähnlichkeit");
                    System.out.println("__________Threshold Konfiguration____________");
                    System.out.println("61 TR1 - Value Gate (maximale Abweichung)");
                    System.out.println("62 TR2 - Accuracy   (Genauigkeit)");
                    System.out.println("63 TR3 - Crossfade  (Überblender)");
                    System.out.println("__________OutPrint Konfiguration_____________");
                    System.out.println("70 Was soll ausgegeben werden!?");
                    System.out.println("_____________Beispieldatensätze______________");
                    System.out.println("810 = SimSearch - CD lite");
                    System.out.println("811 = SimSearch - CD full");
                    System.out.println("812 = SimSearch - Produktstrukturen   (57KB)");
                    System.out.println("813 = SimSearch - Produktstrukturen  (3,6MB)");
                    System.out.println("814 = SimSearch - Produktstrukturen (21,5MB)");
                    System.out.println("8110= SimSearch - Baumsignaturen-XPath");
                    System.out.println("8111= SimSearch - Mini  Example 1");
                    System.out.println("8112= SimSearch - Mini  Example 2");
                    System.out.println("8113= SimSearch - Mini  Example 3");
                    System.out.println("8115= SimSearch - Micro Example A");
                    System.out.println("8116= SimSearch - Micro Example B");
                    System.out.println("8117= SimSearch - Nano  Example A");
                    System.out.println("8118= SimSearch - Nano  Example B");
                    System.out.println("8119= SimSearch - XHTML Example");
                    System.out.println("_____________________________________________");
                    System.out.println("97  = STARTE TEST GUI PRIMITIVE DT. VERFAHREN");
                    System.out.println("98  = STARTE AJAX GUI SimSearch FRONT END");
                    System.out.println("99  = ENDE");
                    System.out.println("100 = MENUE VERLASSEN");
                    try{
                        key =  Integer.valueOf(din.readLine());
                    }catch(NumberFormatException e){
                        System.out.println("Bitte als Zahl eingeben!");
                        args[0]="skipall";
                        main(args);
                    }
                }
                if(key==100){
                    args[0]="skip";
                    main(args);
                }
                if(key==99){
                    args[1]="99";
                    main(args);
                }
                if(key==98){
                    ClassLoader classLoader = Main.class.getClassLoader();
                    Desktop.getDesktop().open(new File(classLoader.getResource("result_data/liveResults.html").getFile()));
                }
                if(key==42){
                    System.out.println("_________________:Befehle:___________________");
                    System.out.println("Um einen Befehl auszuführen Schreiben Sie ihm");
                    System.out.println("V2List : add(String value)");
                    System.out.println("V2List : add(String value, int position)");
                    System.out.println("int    : size()");
                    System.out.println("String : getHead()");
                    System.out.println("String : getTail()");
                    System.out.println("String : getCurrent()");
                    System.out.println("void   : rememberCurrent()");
                    System.out.println("V2List : moveRememberedTo(int pos)");
                    System.out.println("V2List : remove(int pos)");
                    System.out.println("V2List : descendingIterator()");
                    System.out.println("Befehl()\"-\" unterbindet Iteration____ENDE=100");
                    V2List<String> list = new V2List<String>();
                    boolean run = true;
                    while(run){
                        try{
                            String command = din.readLine();
                            if(command.equals("100")){
                                run = false;
                            }else{
                                if(command.startsWith("add(")){
                                    String sub = command.substring(command.indexOf("(")+1, command.indexOf(")"));
                                    if(sub.contains(",")){
                                        String value = sub.substring(0, sub.indexOf(","));
                                        //                                    System.out.println("value="+value);
                                        int    pos   = Integer.parseInt(sub.substring(sub.indexOf(",")+1));
                                        //                                    System.out.println("pos="+pos);
                                        list.add(value, pos);
                                    }else{
                                        list.add(sub);
                                    }
                                    if(!command.endsWith("-")) list.printOut();
                                }else{
                                    if(command.equals("size()")){
                                        System.out.println(list.size());
                                    }else{
                                        if(command.equals("getHead()")){
                                            System.out.println(list.getHead().getObject());
                                        }else{
                                            if(command.equals("getTail()")){
                                                System.out.println(list.getTail().getObject());
                                            }else{
                                                if(command.equals("rememberCurrent()")){
                                                    list.rememberCurrent();
                                                }else{
                                                    if(command.startsWith("moveRememberedTo(")){
                                                        String sub = command.substring(command.indexOf("(")+1, command.indexOf(")"));
                                                        int pos = Integer.parseInt(sub);
                                                        list.moveRememberedTo(pos);
                                                        if(!command.endsWith("-"))list.printOut();
                                                    }else{
                                                        if(command.startsWith("remove(")){
                                                            String sub = command.substring(command.indexOf("(")+1, command.indexOf(")"));
                                                            int pos = Integer.parseInt(sub);
                                                            list.remove(pos);
                                                            if(!command.endsWith("-"))list.printOut();
                                                        }else{
                                                            if(command.equals("descendingIterator()")){
                                                                for(Iterator<String> it = list.descendingIterator(); it.hasNext(); ){
                                                                    String e = it.next();
                                                                    System.out.print(e.toString()+", ");
                                                                }
                                                                System.out.print("\n");
                                                            }else{
                                                                if(command.equals("getCurrent()")){
                                                                    if(list.getCurrent()!=null){
                                                                        System.out.println(list.getCurrent().getObject());
                                                                    }else{
                                                                        System.out.println("Zur Zeit gibts kein Current");
                                                                        System.out.println("Das kann dashalb so sein, weil bei der");
                                                                        System.out.println("OutPrint Iteration dieser null gesetzt");
                                                                        System.out.println("wurde. Anhängen von \"-\" unterbundindet");
                                                                        System.out.println("OutPrint Iterationen!   z.B. add(XYZ)-");
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }catch(Exception e){
                            System.out.println("Fehler: "+e.toString());
                            e.printStackTrace();
                        }
                    }
                }
                if(key==45){
                    System.out.println("Sollen wirklich alle Statemates des Ontologie Models ausgegeben werden?");
                    String aus = din.readLine();
                    if(aus.trim().equalsIgnoreCase("j")){
                        StmtIterator stIt = ModelManagement.getInstance().getModel().listStatements();
                        while(stIt.hasNext()){
                            System.out.println(stIt.next());
                        }
                    }
                }
                if(key==50){
                    ModelManagement mm = ModelManagement.getInstance();
                    if(mm.isCorT()){
                        System.out.println("Zur Zeit wird der Vergleich auf inhaltlicher");
                        System.out.println("Basis durchgeführt");
                    }else{
                        System.out.println("Zur Zeit Sucht das System nach ähnlichen");
                        System.out.println("Teilbäumen");
                    }
                    System.out.println("möchten Sie das Verfahren ändern? (j/n)");
                    String cORt = din.readLine();
                    if(cORt.trim().equalsIgnoreCase("j")){
                        if(mm.isCorT()){
                            mm.setCorT(false);
                        }else{
                            mm.setCorT(true);
                        }
                    }
                    if(mm.isCorT()){
                        System.out.println("Der Vergleich wird auf inhaltlicher Basis");
                        System.out.println("weiter fortgeführt");
                    }else{
                        System.out.println("Das System sucht nach ähnlichen Teilbäumen");
                    }
                }
                if(key==61){
                    ModelManagement mm = ModelManagement.getInstance();
                    System.out.println("*********************************************");
                    System.out.println("!!SCHRANKEN JE NUR 1 MAL PRO MODEL VERGEBEN!!");
                    System.out.println("*********************************************");
                    System.out.println("Bislang existieren "+mm.getTr1().length+" Value Gate Thresholds");
                    System.out.println("und damit auch "+mm.getTr1().length+" Ähnlichkeitsrelationen. Diese Sind:");
                    int before = 100;
                    int[]  tr1 = mm.getTr1();
                    for(int i=0; i<tr1.length;i++){
                        System.out.println("Ähnlichkeitsrelation "+(i+1)+" umfallst Ähnlichkeiten zwichen "+before+"% und "+tr1[i]+"%");
                        before = tr1[i];
                    }
                    System.out.println("Möchten Sie diese Ähnlichkeitsrelationen ändern? (j/n)");
                    String aendern = din.readLine();
                    if(aendern.trim().equalsIgnoreCase("j")){
                        int[] tr1NEW = new int[100];
                        System.out.println("Bitte geben Sie nun die unteren Schranken für");
                        System.out.println("die Relationen in absteigender Reihenfolge ein");
                        System.out.println("Den Abschluss der Eingabe kennzeichnen Sie mit X");
                        int i = 0;
                        int tr1new = -1;
                        boolean weiter = true;
                        boolean allesOk = false;
                        while(weiter){
                            try{
                                System.out.println("neue Schranke bitte:");
                                str1 = din.readLine();
                                if(str1.trim().equalsIgnoreCase("x")){
                                    weiter = false;
                                }else{
                                    tr1new = Integer.valueOf(str1);
                                }
                            }catch(Exception e){
                                System.out.println("Fehler: "+e.toString());
                                weiter = false;
                                allesOk = false;
                            }
                            if(weiter && i<100){
                                if(tr1new>=0 && tr1new<100){
                                    if(i>0){
                                        if(tr1NEW[i-1]>tr1new){
                                            tr1NEW[i] = tr1new;
                                            allesOk = true;
                                        }else{
                                            System.out.println("Bitte die unteren Schranken absteigend angeben!");
                                            weiter = false;
                                            allesOk = false;
                                        }
                                    }else{
                                        tr1NEW[i] = tr1new;
                                        allesOk = true;
                                    }
                                }else{
                                    System.out.println("Thresholds bitte als Prozent angäben 0-99");
                                    allesOk = true;
                                    if(i>0) i--;
                                }
                                i++;
                            }
                        }
                        if(allesOk){
                            int[] tr1NEWNEW = new int[i];
                            for(int j=0; j<i; j++){
                                tr1NEWNEW[j] = tr1NEW[j];
                            }
                            mm.setTr1(tr1NEWNEW);
                            System.out.println("Die neuen Ähnlicheitsrelationen lauten:");
                            before = 100;
                            for(int k=0; k<tr1NEWNEW.length;k++){
                                System.out.println("Ähnlichkeitsrelation "+(k+1)+" umfallst Ähnlichkeiten zwichen "+before+"% und "+tr1NEWNEW[k]+"%");
                                before = tr1NEWNEW[k];
                            }
                        }
                    }
                }
                if(key==61){
                    ModelManagement mm = ModelManagement.getInstance();
                    System.out.println("*********************************************");
                    System.out.println("!!SCHRANKEN JE NUR 1 MAL PRO MODEL VERGEBEN!!");
                    System.out.println("*********************************************");
                    System.out.println("Bislang ligt der Schwellwert für Genauigkeit ");
                    System.out.println("bei einer max Längendifferenz von "+mm.getTr2()+" Zeichen");
                    System.out.println("Möchten Sie diesen ändern? (j/n)");
                    String aendern = din.readLine();
                    boolean ok = true;
                    int ldint  = 0;
                    if(aendern.trim().equalsIgnoreCase("j")){
                        System.out.println("Bitte geben Sie die neue Längendifferenz an");
                        try{
                            String ld = din.readLine();
                            ldint = Integer.parseInt(ld);
                        }catch(Exception e){
                            System.out.println("Fehler: "+e.toString());
                            ok = false;
                        }
                    }
                    if(ok){
                        mm.setTr2(ldint);
                    }
                }
                if(key==62){
                    ModelManagement mm = ModelManagement.getInstance();
                    System.out.println("*********************************************");
                    System.out.println("!!SCHRANKEN JE NUR 1 MAL PRO MODEL VERGEBEN!!");
                    System.out.println("*********************************************");
                    System.out.println("Bislang ligt der überblendende Schwellwert zwischen");
                    System.out.println("dem Ergebniss der aufgrund der Längen Differen und");
                    System.out.println("dem genauen Ergebniss des Levenstein bei "+mm.getTr3()+"/"+"(100-"+mm.getTr3()+")");
                    System.out.println("Möchten Sie diesen ändern? (j/n)");
                    String aendern = din.readLine();
                    boolean ok = true;
                    int crsint = 0;
                    if(aendern.trim().equalsIgnoreCase("j")){
                        System.out.println("Bitte geben Sie den neuen Anteil der Längendifferenz an:");
                        try{
                            String crs = din.readLine();
                            crsint = Integer.parseInt(crs);
                        }catch(Exception e){
                            System.out.println("Fehler: "+e.toString());
                            ok = false;
                        }
                    }
                    if(ok){
                        mm.setTr3(crsint);
                    }
                }
                if(key==99){
                    nochmal = false;
                    break;
                }
                if(key==97){
                    testingGUI gui = new testingGUI();
                    gui.setVisible(true);
                }
                //HAUPT BEISPIELE
                if(key>800){
                    t = new SimilaritySearch(key);
                    t = null;
                }
                if(key==1||key==21){
                    str1="";
                    //String Alignment bzw. Sequenzalignment sowie Edit und Wertedistanzen
                    //basieren auf Matrizen
                    code.setAlgo(key);
                    System.out.println("bitte geben sie einen String ein:");
                    str1 = din.readLine();
                    if(str1.contains("$1")) str1 = s1;
                    System.out.println("bitte geben sie einen String zum Vergleich ein:");
                    str2 = din.readLine();
                    if(str2.contains("$2")) str2 = s2;
                    System.out.println("Ähnlichkeits stuffe: "+code.compare(str2, str1));
                    code.printInterna();
                }
                if(key==40){
                    str1="";
                    boolean nochmal40 = true;
                    String res = null;
                    while(nochmal40){
                        long count = -1;
                        str1 = "";
                        if(res==null){
                            System.out.println("bitte geben sie die Dokumentennummer ein:");
                            str1 = din.readLine();
                            try{
                                count = Long.valueOf(str1);
                            }catch(Exception e){
                                System.out.println("Die Eigabe war keine gültige Zahl");
                                System.out.println("FEHLER: "+e.toString());
                                break;
                            }
                        }else{
                            if(!res.contains("y")){
                                try{
                                    count = Long.valueOf(res);
                                }catch(Exception e){
                                    System.out.println("Die Eigabe war keine gültige Zahl");
                                    System.out.println("FEHLER: "+e.toString());
                                    break;
                                }
                            }else{
                                System.out.println("bitte geben sie die Dokumentennummer ein:");
                                str1 = din.readLine();
                                try{
                                    count = Long.valueOf(str1);
                                }catch(Exception e){
                                    System.out.println("Die Eigabe war keine gültige Zahl");
                                    System.out.println("FEHLER: "+e.toString());
                                    break;
                                }
                            }
                        }
                        if(count!=-1){
                            str2 = XML2OWL.getStringCountPrefix(count);
                            System.out.println("Die FileNummer "+str1+" wird auf den Alphacode "+str2+" abgebildet");
                            System.out.println("Nochmal Alphacode berechnen? (j/n)");
                            res = din.readLine();
                            if(res.contains("n")) nochmal40 = false; else nochmal40 = true;
                        }
                    }
                }
                if(key==41){
                    boolean nochmal41 = true;
                    String res = null;
                    String uri = "";
                    str1 = "";
                    while(nochmal41){
                        if(res==null){
                            System.out.println("bitte geben sie eine URI ein:");
                            str1 = din.readLine();
                            try{
                                uri = URITool.encode(str1);
                            }catch(Exception e){
                                System.out.println("Die Eigabe konnte nicht übersetzt werden");
                                System.out.println("FEHLER: "+e.toString());
                                break;
                            }
                        }else{
                            if(!res.contains("j")){
                                str1 = res;
                                try{
                                    uri = URITool.encode(str1);
                                }catch(Exception e){
                                    System.out.println("Die Eigabe konnte nicht übersetzt werden");
                                    System.out.println("FEHLER: "+e.toString());
                                    break;
                                }
                            }else{
                                System.out.println("bitte geben sie eine URI ein:");
                                str1 = din.readLine();
                                try{
                                    uri = URITool.encode(str1);
                                }catch(Exception e){
                                    System.out.println("Die Eigabe konnte nicht übersetzt werden");
                                    System.out.println("FEHLER: "+e.toString());
                                    break;
                                }
                            }
                        }
                        if(str1.trim().equalsIgnoreCase(uri)){
                            System.out.println("Die Eingabe war schon eine gültige URI");
                        }else{
                            System.out.println("gültige URI ist: "+uri);
                        }
                        System.out.println("Nochmal URI berechnen? (j/n)");
                        res = din.readLine();
                        if(res.contains("n")) nochmal41 = false; else nochmal41 = true;
                    }
                }
                if(key==70){
                    String level = "";
                    String spezial = "";
                    System.out.println("___________globale Konfiguration_____________");
                    System.out.println("Quiet | Normal | Verbose | Debug | Calculation");
                    System.out.println("Die Level lassen sich sinnvoll kombinieren!");
                    System.out.println("Quiet ist implizit wenn alle andern nicht");
                    System.out.println("Antworten mit j/n s=direkt zu speziellen");
                    System.out.println("Normal (ein weing)? - "+SoutConfig.aBitVerbous);
                    level = din.readLine();
                    if(!level.trim().equals("s")){
                        if(level.contains("j") || level.trim().equals("1")) SoutConfig.setaBitVerbous(true); else SoutConfig.setaBitVerbous(false);
                        System.out.println("Verbose (detalierte Ausgabe)? - "+SoutConfig.verbous);
                        level = din.readLine();
                        if(level.contains("j") || level.trim().equals("1")) SoutConfig.setVerbous(true); else SoutConfig.setVerbous(false);
                        System.out.println("Debug (extrem detaliert)? - "+SoutConfig.khadija);
                        level = din.readLine();
                        if(level.contains("j") || level.trim().equals("1")) SoutConfig.setKhadija(true); else SoutConfig.setKhadija(false);
                        System.out.println("Calculation (Berchnungen darstellten)? - "+SoutConfig.calculation);
                        level = din.readLine();
                        if(level.contains("j") || level.trim().equals("1")) SoutConfig.setCalculation(true); else SoutConfig.setCalculation(false);
                        System.out.println("_____Spezeielle Einstellungen vornehmen?_____");
                        System.out.println("n=nein p=parser s=struktur i=inhalt r=results");
                        spezial = din.readLine();
                    }else{
                        System.out.println("_____Spezeielle Einstellungen vornehmen?_____");
                        System.out.println("n=nein p=parser s=struktur i=inhalt r=results");
                        spezial = din.readLine();
                    }
                    boolean nochmal70 = true;
                    while(nochmal70){
                        if(!spezial.trim().equalsIgnoreCase("n")){
                            if(spezial.trim().equalsIgnoreCase("p")){
                                System.out.println("___________parser Konfiguration______________");
                                System.out.println("Quiet | Normal | Verbose | Debug | Calculation");
                                System.out.println("Die Level lassen sich sinnvoll kombinieren!");
                                System.out.println("Diese Einstellungen überschreiben die globalen");
                                System.out.println("Antworten mit j/n g=globale Einstellungen");
                                System.out.println("Normal (ein weing)? "+SoutConfig.getaBitVerbous_X2O());
                                level = din.readLine();
                                if(!level.trim().equalsIgnoreCase("g")){
                                    if(level.contains("j") || level.trim().equals("1")) SoutConfig.setaBitVerbous_X2O((short)1); else SoutConfig.setaBitVerbous_X2O((short)0);
                                }else{
                                    SoutConfig.setaBitVerbous_X2O((short)-1);
                                }
                                System.out.println("Verbose (detalierte Ausgabe)? - "+SoutConfig.getVerbous_X2O());
                                level = din.readLine();
                                if(!level.trim().equalsIgnoreCase("g")){
                                    if(level.contains("j") || level.trim().equals("1")) SoutConfig.setVerbous_X2O((short)1); else SoutConfig.setVerbous_X2O((short)0);
                                }else{
                                    SoutConfig.setVerbous_X2O((short)-1);
                                }
                                System.out.println("Debug (extrem detaliert)? - "+SoutConfig.getKhadija_X2O());
                                level = din.readLine();
                                if(!level.trim().equalsIgnoreCase("g")){
                                    if(level.contains("j") || level.trim().equals("1")) SoutConfig.setKhadija_X2O((short)1); else SoutConfig.setKhadija_X2O((short)0);
                                }else{
                                    SoutConfig.setKhadija_X2O((short)-1);
                                }
                                System.out.println("Calculation (Berchnungen darstellten)? - "+SoutConfig.getCalculation_X2O());
                                level = din.readLine();
                                if(!level.trim().equalsIgnoreCase("g")){
                                    if(level.contains("j") || level.trim().equals("1")) SoutConfig.setCalculation_X2O((short)1); else SoutConfig.setCalculation_X2O((short)0);
                                }else{
                                    SoutConfig.setCalculation_X2O((short)-1);
                                }
                            }
                            if(spezial.trim().equalsIgnoreCase("s")){
                                System.out.println("_____Struktur Management Konfiguration_______");
                                System.out.println("Quiet | Normal | Verbose");
                                System.out.println("Die Level lassen sich sinnvoll kombinieren!");
                                System.out.println("Diese Einstellungen überschreiben die globalen");
                                System.out.println("Antworten mit j/n g=globale Einstellungen");
                                System.out.println("Normal (ein weing)? - "+SoutConfig.getaBitVerbous_SM());
                                level = din.readLine();
                                if(!level.trim().equalsIgnoreCase("g")){
                                    if(level.contains("j") || level.trim().equals("1")) SoutConfig.setaBitVerbous_SM((short)1); else SoutConfig.setaBitVerbous_SM((short)0);
                                }else{
                                    SoutConfig.setaBitVerbous_SM((short)-1);
                                }
                                System.out.println("Verbose (detalierte Ausgabe)? - "+SoutConfig.getVerbous_SM());
                                level = din.readLine();
                                if(!level.trim().equalsIgnoreCase("g")){
                                    if(level.contains("j") || level.trim().equals("1")) SoutConfig.setVerbous_SM((short)1); else SoutConfig.setVerbous_SM((short)0);
                                }else{
                                    SoutConfig.setVerbous_SM((short)-1);
                                }
                            }
                            if(spezial.trim().equalsIgnoreCase("i")){
                                System.out.println("_____Inhalt Management Konfiguration_________");
                                System.out.println("Quiet | Normal | Verbose | Debug | Calculation");
                                System.out.println("Die Level lassen sich sinnvoll kombinieren!");
                                System.out.println("Diese Einstellungen überschreiben die globalen");
                                System.out.println("Antworten mit j/n g=globale Einstellungen");
                                System.out.println("Normal (ein weing)? - "+SoutConfig.getaBitVerbous_TS());
                                level = din.readLine();
                                if(!level.trim().equalsIgnoreCase("g")){
                                    if(level.contains("j") || level.trim().equals("1")) SoutConfig.setaBitVerbous_TS((short)1); else SoutConfig.setaBitVerbous_TS((short)0);
                                }else{
                                    SoutConfig.setaBitVerbous_TS((short)-1);
                                }
                                System.out.println("Verbose (detalierte Ausgabe)? - "+SoutConfig.getVerbous_TS());
                                level = din.readLine();
                                if(!level.trim().equalsIgnoreCase("g")){
                                    if(level.contains("j") || level.trim().equals("1")) SoutConfig.setVerbous_TS((short)1); else SoutConfig.setVerbous_TS((short)0);
                                }else{
                                    SoutConfig.setVerbous_TS((short)-1);
                                }
                                System.out.println("Debug (extrem detaliert)? - "+SoutConfig.getKhadija_TS());
                                level = din.readLine();
                                if(!level.trim().equalsIgnoreCase("g")){
                                    if(level.contains("j") || level.trim().equals("1")) SoutConfig.setKhadija_TS((short)1); else SoutConfig.setKhadija_TS((short)0);
                                }else{
                                    SoutConfig.setKhadija_TS((short)-1);
                                }
                                System.out.println("Calculation (Berchnungen darstellten)? - "+SoutConfig.getCalculation_TS());
                                level = din.readLine();
                                if(!level.trim().equalsIgnoreCase("g")){
                                    if(level.contains("j") || level.trim().equals("1")) SoutConfig.setCalculation_TS((short)1); else SoutConfig.setCalculation_TS((short)0);
                                }else{
                                    SoutConfig.setCalculation_TS((short)-1);
                                }
                            }
                            if(spezial.trim().equalsIgnoreCase("r")){
                                System.out.println("_____ResultReciver (GUI) Konfiguration_______");
                                System.out.println("Quiet | Normal | Verbose | Debug | Calculation");
                                System.out.println("Die Level lassen sich sinnvoll kombinieren!");
                                System.out.println("Diese Einstellungen überschreiben die globalen");
                                System.out.println("Antworten mit j/n g=globale Einstellungen");
                                System.out.println("Normal (ein weing)? - "+SoutConfig.getaBitVerbous_RR());
                                level = din.readLine();
                                if(!level.trim().equalsIgnoreCase("g")){
                                    if(level.contains("j") || level.trim().equals("1")) SoutConfig.setaBitVerbous_RR((short)1); else SoutConfig.setaBitVerbous_RR((short)0);
                                }else{
                                    SoutConfig.setaBitVerbous_RR((short)-1);
                                }
                                System.out.println("Verbose (detalierte Ausgabe)? - "+SoutConfig.getVerbous_RR());
                                level = din.readLine();
                                if(!level.trim().equalsIgnoreCase("g")){
                                    if(level.contains("j") || level.trim().equals("1")) SoutConfig.setVerbous_RR((short)1); else SoutConfig.setVerbous_RR((short)0);
                                }else{
                                    SoutConfig.setVerbous_RR((short)-1);
                                }
                                System.out.println("Debug (extrem detaliert)? - "+SoutConfig.getKhadija_RR());
                                level = din.readLine();
                                if(!level.trim().equalsIgnoreCase("g")){
                                    if(level.contains("j") || level.trim().equals("1")) SoutConfig.setKhadija_RR((short)1); else SoutConfig.setKhadija_RR((short)0);
                                }else{
                                    SoutConfig.setKhadija_RR((short)-1);
                                }
                                System.out.println("Calculation (Berchnungen darstellten)? - "+SoutConfig.getCalculation_RR());
                                level = din.readLine();
                                if(!level.trim().equalsIgnoreCase("g")){
                                    if(level.contains("j") || level.trim().equals("1")) SoutConfig.setCalculation_RR((short)1); else SoutConfig.setCalculation_RR((short)0);
                                }else{
                                    SoutConfig.setCalculation_RR((short)-1);
                                }
                            }
                            System.out.println("_weitere spezeielle Einstellungen vornehmen?_");
                            System.out.println("n=nein p=parser s=struktur i=inhalt r=results");
                            spezial = din.readLine();
                            if(spezial.trim().equalsIgnoreCase("n")) nochmal70 = false;
                        }else{
                            nochmal70 = false;
                        }
                    }
                }
                //WEICHE - Mehrmaligen Durchlauf unterbinden mit wiederhole = false
                boolean wiederhole = true;
                if(wiederhole && !ende){
                    t = null;
                    System.gc();
                    System.out.println("Nochmal? (j/n) Menue verlassen (100)");
                    String res = din.readLine();
                    if(res.trim().equalsIgnoreCase("n") || res.trim().equalsIgnoreCase("99")){
                        nochmal = false;
                        strTemp = "";
                    } else {
                        if(res.trim().equalsIgnoreCase("j")){
                            nochmal = true;
                            strTemp = "";
                        }else{
                            boolean isNumber = true;
                            try{
                                Integer.parseInt(res);
                            }catch(NumberFormatException nfe) {
                                isNumber = false;
                                //                                System.out.println("TODO War keine Nummer!");
                            }
                            if(!res.trim().equalsIgnoreCase("100") && isNumber){
                                nochmal = true;
                                strTemp = res;
                            }else{
                                //                            System.out.println("TODO war hier schon richtig");
                                wiederhole = false;
                                nochmal = false;
                                strTemp = "";
                                args[0]="skip";
                                args[1]=res;
                                main(args);
                            }
                        }
                    }
                }else{
                    nochmal = false;
                }
            }
        }catch(Exception e){
            System.out.println("Das Programm worde unerwartet beendet");
            System.out.println(e.toString());
            e.printStackTrace();
            logger.warn(e);
        }
    }
}
