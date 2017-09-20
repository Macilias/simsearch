/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.macilias.apps.similaritysearch.logic.algorithms.graph;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Selector;
import com.hp.hpl.jena.rdf.model.SimpleSelector;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import com.macilias.apps.similaritysearch.ResultReciver;
import com.macilias.apps.similaritysearch.logic.algorithms.lexical.Levenshtein;
import com.macilias.apps.similaritysearch.util.SoutConfig;

/**
 *
 * @author maciekn
 */
public class ContentSimilarity {
    static Logger logger;


    //Konfiguration
    public static int[] tr1 = ModelManagement.getTr1();
    public static int   tr2 = ModelManagement.getTr2();
    public static int   tr3 = ModelManagement.getTr3();

    private static int[] createTR1Avarages(int[] tr1) {
        int[] tra = new int[tr1.length+1];
        int lastV = 100;
        tra[0] = 100;
        for (int n = 1; n < tr1.length+1; n++) {
            tra[n] = (lastV + tr1[n-1]) / 2;
            lastV = tr1[n-1];
        }
        return tra;
    }

    private static boolean calculation = SoutConfig.getCalculation_TS();
    private static boolean aBitVerbous = SoutConfig.getaBitVerbous_TS();
    private static boolean verbous = SoutConfig.getVerbous_TS();
    private static boolean khadija = SoutConfig.getKhadija_TS();
    private static boolean beQuiet = SoutConfig.getTSQuietMode();

    private static ResultReciver reciver;
    private static HashSet<Individual> thresh = new HashSet<Individual>();
    private static HashSet<Individual> threshOption = new HashSet<Individual>();


    private static void resetOutPrint() {
        calculation = SoutConfig.getCalculation_TS();
        aBitVerbous = SoutConfig.getaBitVerbous_TS();
        verbous = SoutConfig.getVerbous_TS();
        khadija = SoutConfig.getKhadija_TS();
        beQuiet = SoutConfig.getTSQuietMode();
    }

    private static ArrayList<CountedIndividual> getAttribute(LinkedList<String> attMIL) {
        OntModel m = ModelManagement.getInstance().getModel();
        ArrayList<CountedIndividual> attribute = new ArrayList<CountedIndividual>();
        HashMap<String, CountedIndividual> tempAttribute = new HashMap<String, CountedIndividual>();
        for (Iterator<String> it = attMIL.iterator(); it.hasNext();) {
            String uri = it.next();
            if (verbous && khadija) {
                System.out.println("CS Besorge mir das Attribut: " + uri);
            }
            Individual ind = m.getIndividual(uri);
            if (tempAttribute.containsKey(ind.getURI())) {
                tempAttribute.get(ind.getURI()).increaseCount();
            } else {
                tempAttribute.put(ind.getURI(), new CountedIndividual(ind));
            }
        }
        for (Iterator<Entry<String, CountedIndividual>> it = tempAttribute.entrySet().iterator(); it.hasNext();) {
            CountedIndividual countedAttribut = it.next().getValue();
            attribute.add(countedAttribut);
        }
        return attribute;
    }

    private static Candidates createEQCandidate(Individual root, ArrayList<CountedIndividual> atts) {
        OntModel  m = ModelManagement.getInstance().getModel();
        String NSAS = m.getNsPrefixURI("aS");
        Candidates candidates = new Candidates(createTR1Avarages(tr1));
        System.out.println("-----------------------------------------");
        System.out.println("Suche die Wurzelkomponenten der Attribute");
        System.out.println("-----------------------------------------");
        Property parentProperty = m.getProperty(NSAS + "hasParent");
        for (Iterator<CountedIndividual> it = atts.iterator(); it.hasNext();) {
            CountedIndividual countedAttribut = it.next();
            if(verbous){
                System.out.println(".........................................");
                System.out.println("CS Betrachte das Attribut " + countedAttribut.getI() + " Es ist " + countedAttribut.getCount() + " mal vorhanden");
                System.out.println("CS Schaue nach wie oft es in anderen Dokumenten vorkommt");
            }
            StmtIterator st1 = countedAttribut.getI().listProperties(parentProperty);
            Property componentProp = m.getProperty(NSAS + "isComponentOf");
            while (st1.hasNext()) {
                Statement s = st1.next();
                if(verbous && khadija) System.out.println("CS Statement hierzu: " + s.toString());
                Individual parent = s.getObject().as(Individual.class);
                Property p = m.getProperty(NSAS + "from_XML_FILE");
                if(!parent.hasProperty(p)){
                    NodeIterator st = parent.listPropertyValues(componentProp);
                    while (st.hasNext()) {
                        Individual upperComponent = st.next().as(Individual.class);
                        if (upperComponent.hasProperty(p)) {
                            Individual rootComponent = upperComponent;
                            if(!rootComponent.equals(root)){
                                int times = 0;
                                for(NodeIterator nit = parent.listPropertyValues(m.getProperty(NSAS+"hasChildClass")); nit.hasNext();){
                                    if(countedAttribut.getI().hasOntClass(nit.next().as(OntClass.class))) times ++; 
                                }
                                if (verbous) {
                                    System.out.println("CS Attribut:" + countedAttribut.getI() + " Root: " + rootComponent.getLocalName() +
                                            " (via Parent:" + parent.getLocalName() + ")");
                                }
                                candidates.handleAttribut(countedAttribut, 0, rootComponent, atts.size(), times);
                            }
                        }
                    }
                }else{
                    if(!parent.equals(root)){
                        int times = 0;
                        for(NodeIterator nit = parent.listPropertyValues(m.getProperty(NSAS+"hasChildClass")); nit.hasNext();){
                            if(countedAttribut.getI().hasOntClass(nit.next().as(OntClass.class))) times ++;
                        }
                        if (verbous) {
                            System.out.println("CS Attribut:" + countedAttribut.getI() + " Root: " + parent.getLocalName() +
                                    " (via Parent:" + parent.getLocalName() + ")");
                        }
                        candidates.handleAttribut(countedAttribut, 0, parent, atts.size(), times);
                    }
                }
            }
        }
        return candidates;
    }

    private static Candidates createSIMCandidate(Individual root, HashMap<CountedIndividual, HashSet<Individual>> atts, Candidates candidates, int relation, int target) {
        OntModel  m = ModelManagement.getInstance().getModel();
        String NSAS = m.getNsPrefixURI("aS");
        if(candidates==null) candidates = new Candidates(createTR1Avarages(tr1));
        System.out.println("-----------------------------------------");
        System.out.println("Suche Wurzelkomponenten der SIM"+relation+"Attribute");
        System.out.println("-----------------------------------------");
        boolean skip = false;
        Property parentProperty = m.getProperty(NSAS + "hasParent");
        for (Iterator<CountedIndividual> it = atts.keySet().iterator(); it.hasNext() && !skip;) {
            CountedIndividual countedAttribut = it.next();
            HashSet<Individual> simAtts = atts.get(countedAttribut);
            for (Iterator<Individual> it1 = simAtts.iterator(); it1.hasNext();) {
                Individual simAtt = it1.next();
                if(verbous){
                    System.out.println(".........................................");
                    System.out.println("CS Betrachte das SIM Attribut " + simAtt.getURI()+ " zum CountedAttribut "+countedAttribut.getURI()+" ");
                    System.out.println("CS Schaue nach wie oft es in anderen Dokumenten vorkommt");
                }
                StmtIterator st1 = simAtt.listProperties(parentProperty);
                Property componentProp = m.getProperty(NSAS + "isComponentOf");
                while (st1.hasNext()) {
                    Statement s = st1.next();
                    if(verbous && khadija) System.out.println("Statement hierzu: " + s.toString());
                    Individual parent = s.getObject().as(Individual.class);
                    NodeIterator st = parent.listPropertyValues(componentProp);
                    Property p = m.getProperty(NSAS + "from_XML_FILE");
                    if(!parent.hasProperty(p)){
                        while (st.hasNext()) {
                            Individual upperComponent = st.next().as(Individual.class);
                            if (upperComponent.hasProperty(p)) {
                                Individual rootComponent = upperComponent;
                                if(!rootComponent.equals(root) && !thresh.contains(rootComponent)){
                                    int times = 0;
                                    for(NodeIterator nit = parent.listPropertyValues(m.getProperty(NSAS+"hasChildClass")); nit.hasNext();){
                                        if(simAtt.hasOntClass(nit.next().as(OntClass.class))) times ++;
                                    }
                                    if (verbous) {
                                        System.out.println("CS Attribut:" + simAtt.getURI() + " Root: " + rootComponent.getLocalName() +
                                                " (via Parent:" + parent.getLocalName() + ")");
                                    }
                                    //Hier interessiert das simAtt nicht sondern was es ersetzten soll
                                    candidates.handleAttribut(countedAttribut, relation, rootComponent, target, times);
                                }
                            }
                        }
                    }else{
                        if(!parent.equals(root) && thresh.contains(parent)){
                            int times = 0;
                            for(NodeIterator nit = parent.listPropertyValues(m.getProperty(NSAS+"hasChildClass")); nit.hasNext();){
                                if(simAtt.hasOntClass(nit.next().as(OntClass.class))) times ++;
                            }
                            if (verbous) {
                                System.out.println("CS Attribut:" + simAtt.getURI() + " Root: " + parent.getLocalName() +
                                        " (via Parent:" + parent.getLocalName() + ")");
                            }
                            //Hier interessiert das simAtt nicht sondern was er ersetzten soll
//                            candidates.handleAttribut(countedAttribut, relation, parent, target, times);
                            candidates.handleAttribut(countedAttribut, relation, parent, target, times);
                        }
                    }
                }
            }
        }
        return candidates;
    }

    private static HashMap<CountedIndividual, HashSet<Individual>>[] generateAttritueSimSets(ArrayList<CountedIndividual> attribute) {
        OntModel m = ModelManagement.getInstance().getModel();
        String NSAS = m.getNsPrefixURI("aS");
        HashMap<CountedIndividual, HashSet<Individual>>[] simSets = new HashMap[tr1.length];
        for (int n = 0; n < tr1.length; n++) {
            simSets[n] = new HashMap<CountedIndividual, HashSet<Individual>>();
        }
        int g = 0;
        int q = 0;
        int next = 0;
        if (beQuiet) {
            if (attribute.size() > 78) {
                q = attribute.size() / 78;
            } else {
                q = 78 / attribute.size();
            }
            System.out.println("Attribute vorbereiten_________________________________________________________");
            next = q + 1;
        }
        for (Iterator<CountedIndividual> it = attribute.iterator(); it.hasNext(); g++) {
            CountedIndividual individual = it.next();
            if (verbous || aBitVerbous) {
                System.out.println("__________Prepare Attributes - AUSGANGSPUNKT:" + individual.getURI());
            }
            OntClass css  = individual.getI().getOntClass(true);
//            m.getOntClass(NSSS+"systemdata")
            while(css.equals(m.getOntClass(NSAS+"Attribute"))){
                css  = individual.getI().getOntClass(true); 
            }
            String maxValue = "";
            String minValue = "";
            boolean allesok = true;

            if (css.hasProperty(m.getProperty(NSAS + "maxValue"))) {
                maxValue = css.getPropertyValue(m.getProperty(NSAS + "maxValue")).toString();
                if (verbous) {
                    System.out.println("CS maxValue von " + css.getLocalName() + " = " + maxValue);
                }
            }else{
                allesok = false;
            }
            if (css.hasProperty(m.getProperty(NSAS + "minValue"))) {
                minValue = css.getPropertyValue(m.getProperty(NSAS + "minValue")).toString();
                if (verbous) {
                    System.out.println("CS minValue von " + css.getLocalName() + " = " + minValue);
                }
            }else{
                allesok = false;
            }
            if(allesok == true){
                int mLD = maxValue.length() - minValue.length();
                //Gib mir die Attribute dieser Klasse
                for (ExtendedIterator<Individual> it2 = m.listIndividuals(css); it2.hasNext();) {
                    Individual individual2 = it2.next();
                    int sim = 0;
                    if (!individual.getI().equals(individual2)) {
//                        System.out.println("Vergleiche "+individual.getI()+" mit "+individual2.getURI());
                        sim = SSC(individual.getI(), individual2, mLD);
                        if (sim>100 || sim<0){
                            if (verbous || (aBitVerbous && calculation)){
                                System.out.println("CS FEHLER!!! SSC hat dem Rahmen verlassen.  Maximale Längendifferenz="+mLD+"");
                                System.out.println("CS maxValue ("+maxValue+") und/oder minValue ("+minValue+") für die abstrakte Datenklasse ("+css.getLocalName()+") muss falsch gewesen sein.");
                            }
                            logger.warn("Vergleich mit fehlgeschlagen bei "+individual.getURI()+" und "+individual2.getURI());
                            logger.warn("SSC hat dem Rahmen verlassen.  Maximale Längendifferenz="+mLD+"");
                            logger.warn("maxValue ("+maxValue+") und/oder minValue ("+minValue+") für die abstrakte Datenklasse ("+css.getLocalName()+") muss falsch gewesen sein.");
                            if(sim>100) sim = 100;
                            if(sim<0)   sim = 0;
                        }
                        if (verbous || (aBitVerbous && calculation)) {
                            System.out.println("CS Ähnlichekit zu " + individual2.getURI() + " SIM = " + sim + "%");
                        }
                        if (sim == 100) {
                            //Gleichheit - hier passiert nichts bei den Attributen.
                            if(verbous || aBitVerbous) System.out.println("CS GLEICHEIT?! Das Attribut "+individual.getURI()+" und das Attriubt "+individual2.getURI()+" waren nicht gleich laut equals - weichen aber aber wertmässig nicht ab!");
                            logger.warn("GLEICHEIT?! Das Attribut "+individual.getURI()+" und das Attriubt "+individual2.getURI()+" waren nicht gleich laut equals - weichen aber aber wertmässig nicht ab!");
                        } else {
                            int schranke = 100;
                            for (int n = 0; n < tr1.length; n++) {
                                int tr = tr1[n];
                                if (schranke > sim && sim >= tr) {
                                    HashSet<Individual> simSet = (HashSet<Individual>) simSets[n].get(individual);
                                    if (simSet == null) {
                                        simSet = new HashSetPlus();
                                    }
                                    simSets[n].put(individual, simSet);
                                    if (verbous) {
                                        System.out.println("CS Ähnlichkeit " + (n + 1) + "ten Grades = " + sim + " beim " + individual.getURI() + " und " + individual2.getURI());
                                    }
                                    if (simSet.add(individual2) && verbous) {
                                        System.out.println("CS War noch nicht drin-> " + individual2.getURI() + " wird eingefügt");
                                    }
                                }
                                schranke = tr;
                            }
                        }
                    }
                }
                if (beQuiet) {
                    if (g > next) {
                        next += q;
                        System.out.print("*");
                    }
                }
            }else{
                logger.warn("Die Klasse ("+css.getURI()+") des Ausgangsabttributs "+individual.getURI()+" hatte kein max oder min Wert");
            }
        }
        if (beQuiet) {
            System.out.println("\nAttribute vorbereitet_________________________________________________________");
        }
        return simSets;
    }

    public static int SSC(Individual a, Individual b, int mLD) {
        OntModel m = ModelManagement.getInstance().getModel();
        String NSAS= m.getNsPrefixURI("aS");
        int relaLD = 0;
//        System.out.println("a="+a.getURI());
//        System.out.println("b="+b.getURI());
        boolean checkable = true;
        if(a==null){
            System.out.println("CS Attribut A ist nicht vorhanden");
            logger.warn("Attribut A == null");
            checkable = false;
        }
        if(b==null){
            System.out.println("CS Attribut B ist nicht vorhanden");
            logger.warn("Attribut B == 0");
            checkable = false;
        }
        if(checkable){
            String valueA="";
            String valueB="";
            if(a.hasProperty(m.getProperty(NSAS + "hasValue"))) valueA = a.getPropertyValue(m.getProperty(NSAS + "hasValue")).toString();
            if(b.hasProperty(m.getProperty(NSAS + "hasValue"))) valueB = b.getPropertyValue(m.getProperty(NSAS + "hasValue")).toString();

            if(valueA.equals("")){
                if(verbous || aBitVerbous) System.out.println("CS Attribut A "+a.getURI()+" hatte keinen Wert!");
                logger.warn("Attribut A "+a.getURI()+" hatte keinen Wert!");
                checkable = false;
            }
            if(valueB.equals("")){
                if(verbous || aBitVerbous) System.out.println("CS Attribut B "+b.getURI()+" hatte keinen Wert!");
                logger.warn("Attribut B "+b.getURI()+" hatte keinen Wert!");
                checkable = false;
            }
            if(checkable){
                int LD = Math.abs(valueA.length() - valueB.length());
                if (mLD == 0) {
                    relaLD = 100;
                } else {
                    relaLD = (((mLD * 100) - (LD * 100)) / (mLD));
                }
                if (LD <= tr2) {
                    int leve = Levenshtein.compare(valueA, valueB, tr2);
                    int ret = (((relaLD * tr3) / 100) + ((leve * (100 - tr3)) / 100));
                    if (verbous && calculation) {
                        System.out.println("CS COMPARE " + a + " und " + b + " !(LD>TR2)-> SSC return: " + ret);
                        System.out.println("CS (relaLD(" + relaLD + ") * tr3(" + tr3 + "))/100 = " + ((relaLD * tr3) / 100));
                        System.out.println("CS (leve(" + leve + ") * (100-tr3(" + tr3 + ")))/100 = " + ((leve * (100 - tr3)) / 100));
                    } else {
                        if (aBitVerbous && calculation) {
                            System.out.println("CS COMPARE mit " + b + " LD<TR1: " + ((relaLD * tr3) / 100) + " + " + ((leve * (100 - tr3)) / 100) + " = " + ret);
                        }
                    }
                    return ret;
                } else {
                    if ((verbous || aBitVerbous) && calculation) {
                        System.out.println("CS COMPARE "+a+" und "+b+ " (LD>TR2)-> SSC return relaLD("+relaLD+")*TR3("+tr3+"): "+relaLD*tr3);
                    }
                    return (relaLD * tr3)/100;
                }
            }else{
                return 0;
            }
        }else{
            return 0;
        }
    }

    private static void score2similarity(Individual docRoot, long attCount, Candidates candidates, boolean save, boolean rem_duplicates){
        OntModel m = ModelManagement.getInstance().getModel();
        String NSAS = m.getNsPrefixURI("aS");
//        Literal docRootAttCountL = (Literal) docRoot.getPropertyValue(m.getProperty(NSAS+"hasAttCount"));
        long docRootAttCount = attCount;
        Property p = m.getProperty(NSAS+"from_XML_FILE");
        Literal fileDocRoot = (Literal) docRoot.getPropertyValue(p);
        for (Iterator<Entry<Individual,HashMapPlus<Individual,Integer>>> it = candidates.getMap().entrySet().iterator(); it.hasNext();) {
            Entry<Individual,HashMapPlus<Individual,Integer>> en = it.next();
            ScoredIndividual root = en.getValue().getRoot();
            if(m.containsResource(root.getI())){
                Literal rootAttCountL = (Literal) root.getI().getPropertyValue(m.getProperty(NSAS+"hasAttCount"));
                long rootAttCount  = rootAttCountL.getLong();
                long maxAttCount   = Math.max(docRootAttCount, rootAttCount);
                long similarity    = root.getScore() / maxAttCount;
                if(save){
                    Property synonym;
                    if(similarity==100){
                        System.out.println("CS Der Kandidat "+root.getI().getLocalName()+" ist gleich geblieben zu dem eingelesenen Dokument");
                        synonym = m.getProperty(NSAS+"equivalentIndividual");
                        Literal fileRoot = (Literal) root.getI().getPropertyValue(p);
                        if(rem_duplicates){
                            if(fileDocRoot.equals(fileRoot)){
                                if(verbous) System.out.println("CS "+root.getI().getLocalName()+" wird im Anschluss gelöscht");
                                if(thresh.add(root.getI())) reciver.thresh.add(root.getI());
                            }else{
                                if(verbous) System.out.println("CS "+root.getI().getLocalName()+" wird im Anschluss zum löschen freigegeben (mit Nachfrage) Ähnlichkeit 100%, anderer Dateiname");
                                threshOption.add(root.getI());
                            }
                        }
                    }else{
                        System.out.println("Der Kandidat "+root.getI().getLocalName()+" ist zu "+similarity+"% Ähnlich wie das eingelesene Dokument");
                        synonym = m.getProperty(NSAS+"similarIndividual");
                    }
                    Property simPer€ = m.createProperty(NSAS+similarity+"PercentSimilarIndividual");
                    simPer€.addProperty(m.createProperty("http://www.w3.org/2000/01/rdf-schema#","subPropertyOf"), synonym);
                    simPer€.addProperty(m.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#","type"),
                                           m.getProperty("http://www.w3.org/2002/07/owl#", "SymmetricProperty"));
                    simPer€.addLiteral(m.getProperty(NSAS+"similarity"), similarity);
                    docRoot.addProperty(simPer€, root.getI());
                }else{
                    Literal fileRoot = (Literal) root.getI().getPropertyValue(p);
                    if(rem_duplicates){
                        if(fileDocRoot.equals(fileRoot)){
                            if(similarity==100){
                                System.out.println("CS Der Kandidat "+root.getI().getLocalName()+" ist gleich geblieben zu dem eingelesenen Dokument");
                                if(thresh.add(root.getI())) reciver.thresh.add(root.getI());
                            }else{
                                if(verbous) System.out.println("CS "+root.getI().getLocalName()+" wird im Anschluss zum löschen freigegeben (mit Nachfrage) die Datei hat sich verändert");
                                root.getI().addComment(m.createLiteral("die Datei hat sich verändert"));
                                threshOption.add(root.getI());
                            }
                        }else{
                            if(similarity==100){
                                if(verbous) System.out.println("CS "+root.getI().getLocalName()+" wird im Anschluss zum löschen freigegeben (mit Nachfrage) Ähnlichkeit 100%, anderer Dateiname");                                
                                root.getI().addComment(m.createLiteral("Das Dokument stimmt inhaltlich vollkommen mit dem Dokument "+docRoot.getLocalName()+" aus der Datei "+fileDocRoot.getString()+" überein"));
                                threshOption.add(root.getI());
                            }
                        }
                    }
                }
                reciver.recive(root.getI(), Integer.parseInt(Long.toString(similarity)));
            }
        }
        reciver.wirteResults();
    }

    public static void findSimilarAndEquivalentXMLDocuments(String rootURI, LinkedList<String> attsMIL, boolean rem_dublicates) {
        //BEGIN INITIALISIERUNG
        OntModel m = ModelManagement.getInstance().getModel();
        m.reset();
        resetOutPrint();
        logger = Logger.getLogger("SimSearch");
        reciver = new ResultReciver();
        Individual root = m.getIndividual(rootURI);
        reciver.recive(root, 101);
        //ENDE INITIALISIERUNG

        //BEGIN 1. EQ CANDIDATE (1. und 2. kann parallel erfolgen!)
        ArrayList<CountedIndividual> atts = getAttribute(attsMIL);
        Candidates candidates = createEQCandidate(root,atts);
        score2similarity(root,attsMIL.size(), candidates,false,rem_dublicates);
        //ENDE EQ CANDIDATE

        //BEGIN 2. SIM CANDIDATE GENERIERUNG
        HashMap<CountedIndividual, HashSet<Individual>>[] simChilds = generateAttritueSimSets(atts);
        //END SIM CANDIDATE GENERIERUNG

        //BEGIN SIM CANDIDATE AUSWERTUNG
        for(int n=1; n<tr1.length+1; n++){
            candidates = createSIMCandidate(root,simChilds[n-1],candidates,n, atts.size());
            if(aBitVerbous || verbous || calculation) candidates.printOutCandidate();
            if(n==tr1.length){
                score2similarity(root,attsMIL.size(),candidates,true,false);
            }else{
                score2similarity(root,attsMIL.size(),candidates,false,false);
            }
        }
        //END SIM CANDIDATE AUSWERTUNG

        //AUFRÄUMEN
        reciver.recive(null, 0);
        removeRootComponents(thresh);
        if(threshOption.size()>0) ascForDelate();
        thresh = new HashSet<Individual>();
        threshOption = new HashSet<Individual>();
        //END AUFRÄUMEN
    }
    
    public static void ascForDelate(){
        BufferedReader din = new BufferedReader(
                             new InputStreamReader(System.in));
        HashSet<Individual> thresh_pos = new HashSet<Individual>();
        for(Iterator<Individual> thopt = threshOption.iterator(); thopt.hasNext(); ){
            Individual schweb = thopt.next();
            System.out.println("Soll die Komponente "+schweb.getLocalName()+" enfert werden? (j/n)");
            System.out.println("Kommentar: "+schweb.getComment(null));
            try {
                String ans = din.readLine();
                if(ans.trim().equalsIgnoreCase("j")){
                    thresh_pos.add(schweb);
                    if(verbous || aBitVerbous) System.out.println(schweb.getLocalName()+" wird gelöscht!");
                }
            } catch (IOException ex) {
                java.util.logging.Logger.getLogger(ContentSimilarity.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            }
        }
        removeRootComponents(thresh_pos);
    }

    public static void handleDifferencesBottomUp(String filename, LinkedList<String> atts) {
        System.out.println("CS Vergleich des Dokumentes mit allen dem System bekannten Dokumenten (Bottom-Up)");
        findSimilarAndEquivalentXMLDocuments(filename, atts, true);

    }

    public static void handleDifferencesTopDown(String filename) {
//        System.out.println("Handle diff TopDown bei: "+filename);
        reciver = new ResultReciver();
        OntModel m = ModelManagement.getInstance().getModel();
        String  NSAS = m.getNsPrefixURI("aS");
        List<Resource> resList = m.listSubjectsWithProperty(m.getProperty(NSAS+"from_XML_FILE"), filename).toList();
        Property equal = m.getProperty(NSAS+"equivalentIndividual");
        Property similar = m.getProperty(NSAS+"similarIndividual");
        for(int i = 0; i<resList.size(); i++){
            Individual individual = resList.get(i).as(Individual.class);
            if(aBitVerbous || verbous) System.out.println("CS Betrachte Individual "+individual.getURI());
            StmtIterator eqComponents = individual.listProperties(equal);
            reciver.recive(individual, 101);
            while(eqComponents.hasNext()) {
                Individual other = eqComponents.next().getObject().as(Individual.class);
                reciver.recive(other, 100);
            }
            StmtIterator simComponents = individual.listProperties(similar);
            while(simComponents.hasNext()) {
                Statement s = simComponents.next();
                if(verbous || calculation) System.out.println("CS Statemt bei similar: "+s);
                Individual other = s.getObject().as(Individual.class);
                Selector selector1 = new SimpleSelector(individual, null, other);
                StmtIterator simProp = m.listStatements(selector1);
                while(simProp.hasNext()){
                    Property sim = simProp.nextStatement().getPredicate();
                    if(sim.hasProperty(m.getProperty(NSAS+"similarity"))){
                        int similarity = sim.getProperty(m.getProperty(NSAS+"similarity")).getInt();
                        if(verbous && khadija) System.out.println("CS ÄHNLICHKEIT GEFUNDEN! "+similarity);
                        reciver.recive(other, similarity);
                    }
                }
            }
        }
        reciver.wirteResults();
        reciver.recive(null, 0);
        if(aBitVerbous || verbous) System.out.println("CS FERTIG");
    }

    public static void removeRootComponents(HashSet<Individual> thresh){
        if(thresh.size()>0){
            if(aBitVerbous || beQuiet) System.out.println("Enfernen alter Kopien");
            OntModel m = ModelManagement.getInstance().getModel();
            String NSAS = m.getNsPrefixURI("aS");
            LinkedList<Individual> rausdamit = new LinkedList<Individual>();
            rausdamit.addAll(thresh);

            for(Iterator<Individual> it = thresh.iterator(); it.hasNext();){
                Individual individual = it.next();
                if(individual!=null){
                    StmtIterator ind2Components = individual.listProperties(m.getProperty(NSAS+"hasComponent"));
                    List<Statement> ind2ComponentsList = ind2Components.toList();
                    if(individual.canAs(Individual.class)) rausdamit.add(individual.as(Individual.class));
                    for(int k = 0; k<ind2ComponentsList.size(); k++){
                        Statement statement = ind2ComponentsList.get(k);
                        Individual individual2 = null;
                        if(statement.getObject().canAs(Individual.class)){
                            individual2 = statement.getObject().as(Individual.class);
                            if(!individual2.hasOntClass(m.getOntClass(NSAS+"Attribute"))){
                                rausdamit.add(individual2);
                            }
                        }
                    }
                }
            }
            for(int i=0; i<rausdamit.size(); i++){
                Individual individual = rausdamit.get(i);
                if(verbous || aBitVerbous) System.out.println("\nEntfernt wird: "+individual.getLocalName());
                Selector selector1 = new SimpleSelector(null, null, individual);
                m.remove(m.listStatements(selector1));
                Selector selector2 = new SimpleSelector(individual, null, (RDFNode)null);
                m.remove(m.listStatements(selector2));
                Selector selector3 = new SimpleSelector(individual, null, (Literal)null);
                m.remove(m.listStatements(selector3));
            }
        }
    }
}

class CountedIndividual {

    public CountedIndividual(Individual i) {
        this.i = i;
        this.count = 1;
    }

    public CountedIndividual(CountedIndividual i) {
        this.i = i.getI();
        //Einer wird subtrahiert, weil count-1
        //Mappings noch gefunden werden können
        this.count = i.getCount()-1;
    }

    Individual i;
    int count;

    public void setCount(int count) {
        this.count = count;
    }

    public void increaseCount() {
        this.count++;
    }


    public boolean decreaseCount() {
        boolean res = false;
        if (this.count > 0) {
            this.count--;
            res = true;
        }
        return res;
    }

    public void addSome(int count){
        this.count += count;
    }

    public void setI(Individual i) {
        this.i = i;
    }

    public String getURI(){
        return this.i.getURI();
    }

    public String getLocalName(){
        return this.i.getLocalName();
    }

    public int getCount() {
        return count;
    }

    public Individual getI() {
        return i;
    }

    public boolean canMapp(){
        if(this.count>0){
            return true;
        }else{
            return false;
        }
    }
}

class ScoredIndividual {

    Individual i;
    long score;
//    int  votes = 0;

    public ScoredIndividual(Individual i) {
        this.i = i;
    }

    public void increaseScore(int tra){
        this.score += Long.valueOf(tra);
//        this.votes += this.votes;
    }

    public void setI(Individual i) {
        this.i = i;
    }

    public long getScore() {
        return this.score;
    }

//    public int getVotesCount() {
//        return this.votes;
//    }

    public Individual getI() {
        return i;
    }

}

class HashSetPlus<E> extends HashSet<E>{

    ScoredIndividual root;

    public void setRoot(ScoredIndividual root) {
        this.root = root;
    }

    public ScoredIndividual getRoot() {
        return root;
    }

}

/*
 * Kandidaten
 * 
 */
class Candidates{

    HashMap<Individual,HashMapPlus<Individual,Integer>> candidates;

    int[] tra;
    boolean verbous = SoutConfig.getVerbous_TS();
    boolean aBitVerbous = SoutConfig.getaBitVerbous_TS();
    boolean calculation = SoutConfig.getCalculation_TS();


    public Candidates(int[] tra){
        this.tra = tra;
        candidates = new HashMap<Individual,HashMapPlus<Individual,Integer>>();
    }
    // n - Relation, root - Wurzelkomonente des a, a - bei n = 0 das Attribut
    // bei n > 0 das Schlüsselattribut der n-ten Menge (nicht sim Attribut)
    public boolean handleAttribut(CountedIndividual a, int n, Individual root, int target, int times){
        if(verbous) System.out.println("CSC TARGET: "+target+" TIMES: "+times);
        boolean matched = false;
        boolean firstTime = true;
        while(times>0){
            if(firstTime && !candidates.containsKey(root)){
                firstTime = false;
                if(verbous || calculation) System.out.println("CSC Root "+root.getLocalName()+" bekommt ein neues Mapping für das Attribut "+a.getI().getURI()+" ");
                HashMapPlus<Individual,Integer> atts = new HashMapPlus<Individual,Integer>();
                ScoredIndividual rootS = new ScoredIndividual(root);
                rootS.increaseScore(tra[n]);
                atts.setRoot(rootS);
                atts.put(a.getI(), a.getCount()-1);
                Integer count = atts.get(a.getI());
                if(aBitVerbous || verbous || calculation) System.out.println("CSC Das Attribute kann noch "+count+" mal mappen");
                candidates.put(root, atts);
                matched = true;
            }else{
                HashMapPlus<Individual,Integer> atts = candidates.get(root);
                //Es wird gepfüft ob der Kandidat nicht schon alle Zuordnungen hat
                    if(atts.containsKey(a.getI())){
                        Integer count = atts.get(a.getI());
                        if(count > 0){
                            //Es sind noch weitere Mappings für das Attribut möglich");
                            if(verbous || calculation) System.out.println("CSC Root "+root.getLocalName()+" bekommt ein weiteres Mapping für das Attribut "+a.getI().getURI()+" ");
                            count --;
                            if(aBitVerbous || verbous || calculation) System.out.println("CSC Das Attribute kann noch "+count+" mal mappen");
                            atts.put(a.getI(), count);
                            atts.getRoot().increaseScore(tra[n]);
                            matched = true;
                        }else{
                            times = 0;
                        }
                    }else{
                        //Das Dokument war bekannt, für das Attribut aber noch kein Mapping vorhanden");
                        atts.put(a.getI(), a.getCount()-1);
                        if(verbous || calculation) System.out.println("CSC Root "+root.getLocalName()+" bekommt ein neues Mapping für das Attribut "+a.getI().getURI()+" ");
                        Integer count = atts.get(a.getI());
                        if(aBitVerbous || verbous || calculation) System.out.println("CSC Das Attribute kann noch "+count+" mal mappen");
                        atts.getRoot().increaseScore(tra[n]);
                    }
            }
            times--;
        }
        return matched;
    }

    public HashMap<Individual,HashMapPlus<Individual,Integer>> getMap(){
        return this.candidates;
    }

    public void printOutCandidate(){
        System.out.println("-----------------------------------------");
        System.out.println("Die Kandidatenmenge besteht zu Zeit aus:");
        System.out.println("-----------------------------------------");
        for(Iterator<Entry<Individual,HashMapPlus<Individual,Integer>>> it = candidates.entrySet().iterator();it.hasNext();){
            Entry<Individual,HashMapPlus<Individual,Integer>> e = it.next();
            System.out.println("Der Kandidat "+e.getKey().getLocalName()+" hat einen Gesammtscore von "+e.getValue().getRoot().getScore());
            if(e.getValue().size()>0){
                System.out.println("Ledeglich folgedne Attribute bleiben ohne passendes Gegenstück:");
                for(Iterator<Entry<Individual, Integer>> it2 = e.getValue().entrySet().iterator(); it2.hasNext();){
                    Entry<Individual, Integer> e2 = it2.next();
                    if(e2.getValue()>0 || SoutConfig.getKhadija_TS()) System.out.println("Dass Attribut "+e2.getKey().getURI()+" erwartet noch "+e2.getValue()+" passende Gegenstücke");
                }
            }
        }
    }

}

class HashMapPlus<E,O> extends HashMap<E,O>{

    ScoredIndividual root;

    public void setRoot(ScoredIndividual root) {
        this.root = root;
    }

    public ScoredIndividual getRoot() {
        return root;
    }

}

