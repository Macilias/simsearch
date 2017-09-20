/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.macilias.apps.similaritysearch.logic.algorithms.graph;


import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.ObjectProperty;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.macilias.apps.similaritysearch.logic.algorithms.lexical.Levenshtein;
import com.macilias.apps.similaritysearch.util.SoutConfig;
import org.apache.log4j.Logger;

/**
 *
 * @author Maciej Niemczyk
 */
public class TreeSimilarity_Relations {
    static Logger logger = Logger.getLogger(TreeSimilarity_Relations.class);

    //Konfiguration
    public static int[]    tr1 = ModelManagement.getTr1();
    public static int      tr2 = ModelManagement.getTr2();
    public static int      tr3 = ModelManagement.getTr3();

    private static boolean calculation = SoutConfig.getCalculation_TS();
    private static boolean aBitVerbous = SoutConfig.getaBitVerbous_TS();
    private static boolean verbous = SoutConfig.getVerbous_TS();
    private static boolean khadija = SoutConfig.getKhadija_TS();
    private static boolean beQuiet = SoutConfig.getTSQuietMode();

    public static int[] createTR1Avarages(int[] tr1){
        int[] tra = new int[tr1.length];
        int lastV = 100;
        for (int n=0; n<tr1.length; n++) {
            tra[n]=(lastV+tr1[n])/2;
            lastV = tr1[n];
        }
        return tra;
    }

    public static HashMap<String,HashSet<Individual>>[] prepareAttributs(ArrayList<Individual> attribute,
            int[] tr1, int tr2, int tr3){
        OntModel m = ModelManagement.getInstance().getModel();
        String  NSAS = m.getNsPrefixURI("aS");
        String  NSAD = m.getNsPrefixURI("aD");
        HashMap<String,HashSet<Individual>>[] simSets = new HashMap[tr1.length];
        for(int n=0; n<tr1.length; n++){
            simSets[n] = new HashMap<String,HashSet<Individual>>();
        }
        int g = 0;
        int q = 0;
        int next = 0;
        if(beQuiet){
            if(attribute.size()>78){
                q = attribute.size()/78;
            }else{
                q = 78/attribute.size();
            }
            System.out.println("Attribute vorbereiten_________________________________________________________");
            next = q+1;
        }
        for (Iterator<Individual> it = attribute.iterator(); it.hasNext();g++) {
            Individual individual = it.next();
            if(verbous||aBitVerbous) System.out.println("__________Prepare Attributes - AUSGANGSPUNKT:"+individual.getURI());
            OntClass   css        = individual.getOntClass(true);
            String maxValue       = "";
            String minValue       = "";
            if(css.hasProperty(m.getProperty(NSAS+"maxValue"))){
                maxValue = css.getPropertyValue(m.getProperty(NSAS+"maxValue")).toString();
                if(verbous) System.out.println("maxValue von "+css.getLocalName()+" = "+maxValue);
            }
            if(css.hasProperty(m.getProperty(NSAS+"minValue"))){
                minValue = css.getPropertyValue(m.getProperty(NSAS+"minValue")).toString();
                if(verbous) System.out.println("minValue von "+css.getLocalName()+" = "+minValue);
            }
            int mLD = maxValue.length() - minValue.length();
            //Gib mir die Attribute dieser Klasse
            for(ExtendedIterator<Individual> it2 = m.listIndividuals(css);it2.hasNext();) {
                Individual individual2 = it2.next();
                int sim = 0;
                if(!individual.equals(individual2)){
                    sim = SSC(individual, individual2, tr2, tr3, mLD);
                    if(verbous||(aBitVerbous && calculation)) System.out.println("Ähnlichekit zu "+individual2.getURI()+" SIM = "+sim+"%");
                    if(sim==100){
                        //Gleichheit - hier passiert nichts bei den Attributen
                        if(verbous) System.out.println("GLEICHEIT");
                    }else{
                        int schranke = 100;
                        for(int n=0; n<tr1.length;n++){
                            int tr = tr1[n];
                            if(schranke>sim && sim>=tr){
                                HashSet<Individual> simSet = (HashSet<Individual>)simSets[n].get(individual.getURI());
                                if(simSet==null) simSet = new HashSet(); simSets[n].put(individual.getURI(), simSet);
                                if(verbous) System.out.println("Ähnlichkeit "+(n+1)+"ten Grades = "+sim+" beim "+individual.getURI()+" und "+individual2.getURI());
                                if(simSet.add(individual2)&&verbous) System.out.println("War noch nicht drin-> "+individual2.getURI()+" wird eingefügt");
                            }
                            schranke = tr;
                        }
                    }
                }
            }
            if(beQuiet){
                if(g>next){
                    next += q;
                    System.out.print("*");
                }
            }
        }
        if(beQuiet) System.out.println("\nAttribute vorbereitet_________________________________________________________");
        return simSets;
    }

    public static int SSC(Individual a, Individual b,
            int tr2, int tr3 ,int mLD){
        OntModel m = ModelManagement.getInstance().getModel();
        String  NSAS = m.getNsPrefixURI("aS");
        //Similarity
        int relaLD = 0;
        String valueA = a.getPropertyValue(m.getProperty(NSAS+"hasValue")).toString();
        String valueB = b.getPropertyValue(m.getProperty(NSAS+"hasValue")).toString();
        int LD = Math.abs(valueA.length()-valueB.length());
        if(mLD == 0){
            relaLD=100;
           }else{
            relaLD=(((mLD*100)-(LD*100))/(mLD));
        }
        if(LD<=tr2){
            int leve = Levenshtein.compare(valueA,valueB,tr2);
            int ret  = (((relaLD * tr3)/100) + ((leve * (100-tr3))/100));
            if(verbous && calculation){
                System.out.println("COMPARE "+a+" und "+b+ " !(LD>TR2)-> SSC return: "+ret);
                System.out.println("(relaLD("+relaLD+") * tr3("+tr3+"))/100 = "+((relaLD * tr3)/100));
                System.out.println("(leve("+leve+") * (100-tr3("+tr3+")))/100 = "+((leve * (100-tr3))/100));
            }else{
                if(aBitVerbous && calculation){
                    System.out.println("COMPARE mit "+b+ " LD<TR1: "+((relaLD * tr3)/100)+" + "+((leve * (100-tr3))/100)+" = "+ret);
                }
            }
            return ret;
        }else{
            if(verbous&& calculation||aBitVerbous&& calculation) System.out.println("COMPARE "+a+" und "+b+ " (LD>TR2)-> SSC return relaLD("+relaLD+")*TR3("+tr3+"): "+relaLD*tr3);
            return (relaLD*tr3)/100;
        }
    }

    /*
     * Diese Methode Sortiert die Parent-Menge nach der Anzahl der Abwesenheit
     * komplexer Elemente. An der ersten Stelle sollten somit Elemente mit nur
     * Attribute Knoten als Kinder stehen. Sollte dies nicht der Fall sein, so
     * ist jede weitere Suche gleicher Elemente ausichtslos und somit wertlos.
     */
    public static LinkedList<Individual> prepareParents(LinkedList<Individual> parents, boolean parentsWithAttOnly2Front){
        if(beQuiet) System.out.println("\nParents vorbereiten___________________________________________________________");
        OntModel m = ModelManagement.getInstance().getModel();
        String  NSAS = m.getNsPrefixURI("aS");
        String  NSAD = m.getNsPrefixURI("aD");
        if(verbous || calculation) {
            System.out.print("\nSORTIEREN: ursprüngliche Menge:");
            for (Iterator<Individual> it = parents.iterator(); it.hasNext();) {
                Individual individual = it.next();
                System.out.print(", "+individual.getLocalName());
            }
            System.out.print(" \n");
        }
        LinkedList<Individual> kompChilds =  new LinkedList<Individual>();
        LinkedList<Individual> simpChilds =  new LinkedList<Individual>();
        if(parentsWithAttOnly2Front){
            for(int i=0;i<parents.size();i++){

//                if((aBitVerbous && !verbous && !calculation) || beQuiet){
//                    System.out.print(".");
//                    if(i%78==0){
//                        System.out.println("\n");
//                    }
//                }

                Individual individual = parents.get(i);
                if(verbous) System.out.println("Untersuche die Kinder von <"+individual.getLocalName()+"> auf Abwesendheit Komplexer Kinder");
                if(aBitVerbous && !verbous) System.out.print(".");
//                boolean wasAtt = individual.hasOntClass(NS+"Attribute");
                if(!individual.hasProperty(m.getProperty(NSAS+"hasComplexChild"))){
                    StmtIterator childite = individual.listProperties(m.getProperty(NSAS+"hasChild"));
                    ArrayList<Statement> ll = (ArrayList<Statement>) childite.toList();
                    boolean komplex  = false;
                    for(int j=0; j<ll.size()&& !komplex; j++){
                        Individual child = ll.get(j).getObject().as(Individual.class);
                        if(!child.hasOntClass(m.getOntClass(NSAS+"Attribute"))){
                            individual.addLiteral(m.getProperty(NSAS+"hasComplexChild"), true);
                            if(verbous){
                                System.out.println("Komplexes Kind gefunden! beim: "+individual.getLocalName()+" -> "+child.getLocalName());
                            }else{
                                if(aBitVerbous && !calculation) System.out.print("*");
                            }
                            komplex = true;
                        }else{
                            if(verbous && khadija) System.out.println("<"+child.getURI()+"> ist ein Attribut");
                        }
                    }
                    if(komplex){
                        kompChilds.add(individual);
                    }else{
                        simpChilds.add(individual);
                    }
                }else{
                    if(individual.hasLiteral(m.getDatatypeProperty(NSAS+"hasComplexChild"),true)){
                        if(verbous){
                                System.out.println("Komplexes Kind gefunden! beim: "+individual.getLocalName()+" (via Relation)");
                            }else{
                                if(aBitVerbous && !calculation) System.out.print("*");
                        }
                        kompChilds.add(individual);
                    }else{
                        simpChilds.add(individual);
                    }
                }
//                if(aBitVerbous && !verbous && !calculation) System.out.print("\n");
            }
//            parents = null;
        }else{
            kompChilds = parents;
        }
        //Sortire noch die Komplexen nach ihrer Baumtiefe / Pfadlänge
        if(verbous || calculation) System.out.println("Sortieren");
        if(kompChilds.size()>1){
            int min = -1;
            int max = -1;
            for (Iterator<Individual> it = kompChilds.iterator(); it.hasNext();) {
                Individual individual = it.next();
                Literal pn = (Literal) individual.getPropertyValue(m. getProperty(NSAS+"hasLevel"));
                int ebene  = pn.getInt();
                if(max == -1)   max = ebene;
                if(min == -1)   min = ebene;
                if(max < ebene) max = ebene;
                if(min > ebene) min = ebene;
                if(verbous || calculation) System.out.println("Individual "+individual.getLocalName()+" ist auf Ebene: "+ebene);
            }
            int size = (max - min + 1);
            if(verbous && calculation) System.out.println("MAX="+max+" MIN="+min+" -> "+size+" BUCKETS");
            LinkedList[] buckets = new LinkedList[size];
            for(int i=0;i<buckets.length;i++){
                buckets[i] = new LinkedList<Individual>();
            }
            for (Iterator<Individual> it = kompChilds.iterator(); it.hasNext();) {
                Individual individual = it.next();
                Literal pn = (Literal) individual.getPropertyValue(m.getProperty(NSAS+"hasLevel"));
                int ebene = pn.getInt();
                buckets[ebene-min].add(individual);
            }
            kompChilds = new LinkedList<Individual>();
            for(int i=buckets.length-1;i>=0;i--){
                LinkedList<Individual> level = buckets[i];
                for (Iterator<Individual> it = level.iterator(); it.hasNext();) {
                    Individual individual = it.next();
                    kompChilds.add(individual);
                }
            }
        }
        if(parentsWithAttOnly2Front){
             simpChilds.addAll(kompChilds);
             parents = simpChilds;
        }else{
            parents = kompChilds;
        }
        if(verbous || calculation || aBitVerbous){
            System.out.print("\nSORTIEREN: nachträgliche Menge:");
            for (Iterator<Individual> it = parents.iterator(); it.hasNext();) {
                Individual individual = it.next();
                System.out.print(", "+individual.getLocalName());
            }
            System.out.print(" \n");
            System.out.println("");
        }
        if(beQuiet) System.out.println("\nParents vorbereitet___________________________________________________________");
        return parents;
    }

    public static void resetOutPrint(){
        calculation = SoutConfig.getCalculation_TS();
        aBitVerbous = SoutConfig.getaBitVerbous_TS();
        verbous = SoutConfig.getVerbous_TS();
        khadija = SoutConfig.getKhadija_TS();
        beQuiet = SoutConfig.getTSQuietMode();
    }



    public static void findSimilarAndEquivalentIndividuals(HashSet<String> atts){
        resetOutPrint();
        if(beQuiet) System.out.println("Ähnlichkeitssuche___________________________________________________________");
        OntModel m = ModelManagement.getInstance().getModel();
//        String  NSAS = m.getNsPrefixURI("aP");
        String  NSAS = m.getNsPrefixURI("aS");
        String  NSAD = m.getNsPrefixURI("aD");
        int[] tra = createTR1Avarages(tr1);
        ObjectProperty psim = m.getObjectProperty(NSAS+"similarIndividual").as(ObjectProperty.class);
        for(int n=0; n<tr1.length; n++){
            ObjectProperty psimn = m.createObjectProperty(NSAS+"similarIndividual"+(n+1)).as(ObjectProperty.class).convertToSymmetricProperty();
            psimn.setSuperProperty(psim);
        }
        ArrayList<Individual> attribute = new ArrayList<Individual>();
        LinkedList<Individual> parentme = new LinkedList<Individual>();
        if(atts==null){
            ExtendedIterator<Individual> it = m.listIndividuals(m.getOntClass(NSAS+"Attribute"));
            ArrayList<Individual> att2Check = (ArrayList<Individual>) it.toList();
            for(int i=0;i<att2Check.size();i++){
                Individual ind = att2Check.get(i);
    /*0*/        if(ind.hasLiteral(m.getProperty(NSAS+"check"), true)){
                    StmtIterator st = ind.listProperties(m.getProperty(NSAS+"hasParent"));
                    if(verbous && khadija) System.out.println("1 Adde "+ind.getURI()+" zu Attribut-Menge");
                    List<Statement> parentOfi = st.toList();
    /*1*/           attribute.add(ind);
                    //Für jedes Attribut der Attribut-Menge
                    //nehme die Parent-Elemente in die Parent-Menge auf
    /*2*/           for (int j=0;j<parentOfi.size();j++) {
                        if(parentOfi.get(j).getObject().canAs(Individual.class)){
                            Individual p = parentOfi.get(j).getObject().as(Individual.class);
                            if (!parentme.contains(p)) {
                                if(verbous && khadija) System.out.println("2 Adde "+p.getURI()+" zu Parent-Menge");
                                parentme.add(p);
                            }
                        }
                    }
                }else{
                    if(verbous && khadija) System.out.println("2 Individual: "+ind.getURI()+" war zwar ein Attribut, worde aber nicht als neu eingelesen markiert");
                }
            }
        }else{
            for (Iterator<String> it = atts.iterator(); it.hasNext();) {
                String uri = it.next();
                if(verbous && khadija) System.out.println("1 Besorge mir das Attribut: "+uri);
                Individual ind = m.getIndividual(uri);
                if(ind==null) System.out.println("HILFE m kännt "+uri+" nicht!!!!");
                StmtIterator st = ind.listProperties(m.getProperty(NSAS+"hasParent"));
                if(verbous && khadija) System.out.println("1 Adde "+ind.getURI()+" zu Attribut-Menge");
                List<Statement> parentOfi = st.toList();
/*1*/           attribute.add(ind);
                //Für jedes Attribut der Attribut-Menge
                //nehme die Parent-Elemente in die Parent-Menge auf
/*2*/           for (int j=0;j<parentOfi.size();j++) {
                    if(parentOfi.get(j).getObject().canAs(Individual.class)){
                        Individual p = parentOfi.get(j).getObject().as(Individual.class);
                        if (!parentme.contains(p)) {
                            if(verbous && khadija) System.out.println("2 Adde "+p.getURI()+" zu Parent-Menge");
                            parentme.add(p);
                        }
                    }
                }
            }
            atts = new HashSet();
        }
        
        HashMap<String, HashSet<Individual>>[] atSimSets = prepareAttributs(attribute, tr1, tr2, tr3);
        parentme = prepareParents(parentme, true);

        HashSet<Individual> components2check = new HashSet<Individual>();
        if(beQuiet) System.out.print("X0 \n");
/*3*/   for(int i=0; i<parentme.size(); i++){
            Individual parent = parentme.get(i);
            boolean possible = true;
            Property hasComp = m.getProperty(NSAS+"hasComponent");
            if(verbous && khadija && calculation){
                NodeIterator itComp = parent.listPropertyValues(hasComp);
                System.out.println("3 - der nächste Parent "+parent.getLocalName()+" erreicht folgende Componenten");
                while(itComp.hasNext()){
                    System.out.println("3 --- "+itComp.next().as(Individual.class).getURI());
                }
            }
            for(Iterator<Individual> uncheckedParents = components2check.iterator(); uncheckedParents.hasNext();){
                Individual uncheckedParent  = uncheckedParents.next();
                if(verbous && khadija) System.out.println("3 ? parent: "+parent.getLocalName()+" hasComponent uncheckedParent: "+uncheckedParent.getLocalName()+"? ");
                if(!parent.equals(uncheckedParent) && parent.hasProperty(m.getProperty(NSAS+"hasComponent"), uncheckedParent)){
                    possible = false;
                    if(verbous && khadija) System.out.println("3 Unmöglich. Parent gleicht "+uncheckedParent.getLocalName()+" oder hat diesen als Komponente");
                    break;
                }
            }
        if(possible){
            if(verbous || aBitVerbous) System.out.println("3 Abarbeiten der Parentmenge beim Parent: "+parent.getURI());
/*3.1*/     NodeIterator stme = parent.listPropertyValues(m.getProperty(NSAS+"hasChild"));
            List<RDFNode>cmno = stme.toList();
            HashSet<Individual> cm = new HashSet<Individual>();
            for (Iterator<RDFNode> it1 = cmno.iterator(); it1.hasNext();) {
                RDFNode childnode = it1.next();
                if(childnode.canAs(Individual.class)) cm.add(childnode.as(Individual.class));
            }
            ArrayList<Individual> eq = new ArrayList<Individual>();
            ArrayList<Individual> eqCandidate = new ArrayList<Individual>();
            ArrayList<Integer> counters = new ArrayList<Integer>();
            HashMap<String, HashSet<Individual>>[] cmSimSets = new HashMap[tr1.length];
            for(int n=0;n<tr1.length;n++){
                cmSimSets[n] = new HashMap<String, HashSet<Individual>>();
            }
            Integer target = cm.size();
            //Bilden der Child-Menge:
            for(Iterator<Individual> cmIt = cm.iterator(); cmIt.hasNext();){
                Individual child = cmIt.next();
                if(!child.hasOntClass(m.getOntClass(NSAS+"Attribute"))){
                    //Im Falle von nicht Attributen, prüfe die Relationen
                    if(verbous) System.out.println("3.1 Prüfe ob das Kind: "+child.getLocalName()+" äquivalente Komponenten besitzt, welche noch nicht hinzuaddiert worden sind");
                    StmtIterator eqC = child.listProperties(m.getProperty(NSAS+"equivalentIndividual"));
                    List<Statement>s = eqC.toList();
                    if(s.size()>0){
                        for(int k=0;k<s.size();k++){
                            Individual eqI = null;
                            if(s.get(k).getObject().canAs(Individual.class)){
                                eqI = s.get(k).getObject().as(Individual.class);
                                if(!eqI.equals(child)){
                                    if(verbous) System.out.println("3.1 Ein äquivaltes Kind wird der Menge hinzuadiert falls noch nicht der Äquivalenz oder Child Menge enthalten ist: "+eqI.getLocalName());
                                    if(!cm.contains(eqI) && !eq.contains(eqI)){
                                        if(verbous) System.out.println("3.1 war noch nicht drin");
                                        eq.add(eqI);
                                    }else{
                                        if(verbous) System.out.println("3.1 war schon drin");
                                    }
                                }else{
                                    if(verbous && khadija) System.out.println("3.1 Es war das Kind");
                                }
                            }else{
                                if(verbous && khadija) System.out.println("3.1 Das Kind konnte nicht als Individual interpretiert werden");
                            }
                        }
                    }else{
                        if(verbous) System.out.println("3.1 Es besitzt keine äquivalenten Komponenten");
                    }
                    for(int n=0;n<tr1.length;n++){
                        if(verbous) System.out.println("3.1 Prüfe ob das System ähnliche Komponenten "+(n+1)+"ten Grades kennt, welche noch nicht übernommen woredn sind");
                        StmtIterator simIte = child.listProperties(m.getProperty(NSAS+"similarIndividual"+(n+1)));
                        List<Statement>sSim = simIte.toList();
                        if(!cmSimSets[n].containsKey(child.getURI())) cmSimSets[n].put(child.getURI(), new HashSet<Individual>());
                        if(sSim.size()>0){
                            HashSet<Individual> sim = cmSimSets[n].get(child.getURI());
                            for(int k=0;k<sSim.size();k++){
                                Individual simI = null;
                                if(sSim.get(k).getObject().canAs(Individual.class)){
                                    simI = sSim.get(k).getObject().as(Individual.class);
//                                    Property synonym = m.getProperty(NSAS+"synonymIndividual");
//                                    && !child.hasProperty(synonym, simI)
                                    if(!simI.equals(child)){
                                        if(verbous) System.out.println("3.1 Ein ähnliches Kind wird der Child-Menge hinzuadiert: "+simI.getLocalName());
                                        if(!cm.contains(simI)){
                                            if(verbous) System.out.println("3.1 war noch nicht drin");
                                            sim.add(simI);
                                        }else{
                                            if(verbous) System.out.println("3.1 war schon drin");
                                        }
                                    }
                                }
                            }
                        }else{
                            if(verbous) System.out.println("3.1 Es besitzt keine ähnlichen Komponenten i.s.v similarIndividual"+(n+1));
                        }
                    }
                }
//                else{
//                    //Bei Attributen füge die restlichen Ebenen der Att-Menge hinzu
//                    //VORSICHT - zu hoher Speicherverbrauch!
//                    for(int n=0;n<tr1.length;n++){
//                        if(atSimSets[n].containsKey(child.getURI())){
//                            if(!cmSimSets[n].containsKey(child.getURI()))   cmSimSets[n].put(child.getURI(), new HashSet<Individual>());
//                            HashSet<Individual> sim = cmSimSets[n].get(child.getURI());
//                            HashSet<Individual> sia = atSimSets[n].get(child.getURI());
//                            sim.addAll(sia);
//                        }
//                    }
//                }
            }
            cm.addAll(eq);
            HashMap<String,HashSet<Individual>>[] simCandidate = new HashMap[tr1.length];
            for(int n=0; n<tr1.length; n++){
                simCandidate[n]=new HashMap<String,HashSet<Individual>>();
            }
            int iter = 0;
/*3.2*/     for(Iterator<Individual> cmIt = cm.iterator(); cmIt.hasNext();){
                Individual child = cmIt.next();
                if(verbous || aBitVerbous){
                    iter++;
                    System.out.println("3.2."+iter+" beginne mit den Kind "+child.getURI());
                }
                StmtIterator stc = child.listProperties(m.getProperty(NSAS+"hasParent"));
                Property synonym = m.getProperty(NSAS+"synonymIndividual");
                while(stc.hasNext()){
                    Statement st = stc.next();
                    Individual p = null;
                    if(st.getObject().canAs(Individual.class)) p = st.getObject().as(Individual.class);
                    if(p!=null && !p.equals(parent)){
                        if(!parent.hasProperty(synonym, p)){
                            if(verbous) System.out.println("3.2 Noch nicht betrachtet also betrachte dessen anderen Parent: "+p.getLocalName());
                            boolean found= false;
                            for(int k=0; !found && k<eqCandidate.size(); k++){
                                if(eqCandidate.get(k).equals(p)){
                                    Integer count = counters.get(k);
                                    count++;
                                    counters.remove(k);
                                    counters.add(k, count);
                                    if(verbous) System.out.println("3.2 Parent bereits in der EqualCandidate Menge - counter hochzählen auf "+count);
                                    found=true;
                                }
                            }
                            if(!found){
                                if(verbous) System.out.println("3.2 Parent wird als Kandidat neu eingefügt ");
                                eqCandidate.add(p);
                                counters.add(new Integer(1));
                            }
                        }else{
                            if(verbous) System.out.println("3.2 Parent: "+p.getLocalName()+" bereits gegen das aktuelle Element ausgewertet");
                            if(khadija && verbous && calculation && !aBitVerbous){
                                System.out.println("3.2 Parent hat die synonymitäts Relationen:");
                                StmtIterator stp = parent.listProperties(synonym);
                                while(stp.hasNext()){
                                    Statement state = stp.next();
                                    if(state.getObject().equals(p)){
                                        System.out.println("3.2 S: "+state.getSubject().getLocalName()+" P: "+state.getPredicate().getLocalName()+" O: "+state.getObject().as(Individual.class).getLocalName());
                                    }
                                }
                            }
                        }
                    }
                }
                for(int n=0; n<tr1.length; n++){
                    //Weitere Ebenen enthalten Sets mit Schlüssel=URI & Set=CounterListen
                    HashSet<Individual> simChilds;
                    if(atSimSets[n].containsKey(child.getURI())){
                        simChilds = atSimSets[n].get(child.getURI());
                        if(cmSimSets[n].containsKey(child.getURI())){
                            if(simChilds!=null){
                                simChilds.addAll(cmSimSets[n].get(child.getURI()));
                            }else{
                                simChilds = cmSimSets[n].get(child.getURI());
                            }
                        }
                    }else{
                        simChilds = cmSimSets[n].get(child.getURI());
                    }
                    if(simChilds!=null){
                        HashMap<String, HashSet<Individual>> simParentsCandidate = simCandidate[n];
                        HashSet<Individual>  doneVotes = new HashSet();
                        for (Iterator<Individual> it1 = simChilds.iterator(); it1.hasNext();) {
                            Individual simChild = it1.next();
                            if(verbous) System.out.println("3.2 fahre fort mit ähnlichen Kind der "+(n+1)+"ten Ähnlichkeitsrelation: "+simChild.getURI());
                            StmtIterator stSim = simChild.listProperties(m.getProperty(NSAS+"hasParent"));
                            while(stSim.hasNext()){
                                Statement st = stSim.next();
                                Individual p = null;
                                if(st.getObject().canAs(Individual.class)) p = st.getObject().as(Individual.class);
                                if(p!=null && !p.equals(parent) && !doneVotes.contains(p)){
                                    if(verbous) System.out.println("3.2 Betrachte dessen anderen Parent: "+p.getLocalName());
                                    HashSet set = null;
                                    if(simParentsCandidate.containsKey(p.getURI())){
                                        set = simParentsCandidate.get(p.getURI());
                                        if(verbous && khadija) System.out.println("3.2 Andere Parent bereits in der SimCandidate["+(n+1)+"] Menge - versuche dieser den SimChild hinzuzufügen");
                                    }else{
                                        set = new HashSet<Individual>();
                                        if(verbous && khadija) System.out.println("3.2 Andere Parent noch nicht in der SimCandidate["+(n+1)+"] Menge - Parent wird neu eingefügt und SimChild seiner Counter Menge hinzuaddiert");
                                    }
                                    if(!set.add(simChild)){
                                        if(verbous && khadija) System.out.println("3.2 erfolglos. Das Kind war bereits drin");
                                        if(aBitVerbous && calculation) System.out.println("3.2 ((HashSet) SimCandidate["+(n+1)+"]).get("+p.getLocalName()+") -=X=- Schon DRIN ");
                                    }else{
                                        if(verbous && khadija) System.out.println("3.2 erfolgreich");
                                        if(aBitVerbous && calculation) System.out.println("3.2 ((HashSet) SimCandidate["+(n+1)+"]).get("+p.getLocalName()+").add("+simChild.getURI().substring(NSAD.length())+")");
                                    }
                                    simParentsCandidate.put(p.getURI(), set);
                                    doneVotes.add(p);
                                }
                            }
                        }
                    }
                }
            }
/*3.3*/     if(verbous) System.out.println("3.3 Target = "+target);
            Property synonym = m.getProperty(NSAS+"synonymIndividual");
            for(int j=0;j<eqCandidate.size();j++){
                Individual candidate = eqCandidate.get(j);
//                System.out.println("Candidate:"+candidate.getLocalName()+" vom typ "+candidate.getOntClass().getLocalName());
                if(!parent.hasProperty(synonym, candidate)){
                    int        childcount = ((Literal) candidate.getPropertyValue(m.getProperty(NSAS+"hasChildCount"))).getInt();
                    LinkedList<Individual> checkThouse = new LinkedList<Individual>();
                    if(counters.get(j).equals(target)){
                        if(verbous || aBitVerbous) System.out.println("3.3 Der Kadndidat: "+candidate.getLocalName()+" hat alle Attribute des Parents: "+parent.getLocalName());
                        if(childcount==target.intValue()){
                            if(verbous || aBitVerbous) System.out.println("3.4 Es hat auch keine weiteren, somit sind sie gleich i.s.i.G");
                            if(beQuiet) System.out.print("EQ ");
                            parent.addProperty(m.getProperty(NSAS+"equivalentIndividual"), candidate);
                            StmtIterator st = candidate.listProperties(m.getProperty(NSAS+"hasParent"));
                            List<Statement> parentOfk = st.toList();
                            for (int k=0;k<parentOfk.size();k++) {
                                if(parentOfk.get(k).getObject().canAs(Individual.class)){
                                    Individual p = parentOfk.get(k).getObject().as(Individual.class);
                                    checkThouse.add(p);
                                    if(verbous) System.out.println("3.6 Parent "+p.getLocalName()+" des Kandidaten "+candidate.getLocalName()+" wird der nächsten Parent Menge hinzugefügt (falls nicht bereits drin)");
                                }
                            }
                        }else{
                            if(verbous){
                                int mehr = (childcount - target.intValue());
                                String weitere = "weitere";
                                if(mehr==1) weitere = "ein weiteres";
                                System.out.println("3.4 Es hat aber noch "+weitere+", somit NICHT gleich i.s.i.G");
                            }
                        }
                    }else{
                        //Suche nach Ähnlichkeit!
                        int sumSimScore = counters.get(j)*100;
                        int childs2SLef = childcount - counters.get(j);
                        String outprint = "3.5 Berechnung: ("+counters.get(j)+" * 100%";
                        for(int n=0; n<tr1.length && childs2SLef>0; n++){
                            if(simCandidate[n].containsKey(candidate.getURI())){
                                int counter = simCandidate[n].get(candidate.getURI()).size();
                                if(counter>childs2SLef){
                                    counter = childs2SLef;
                                }
                                if(counter>0){
                                    sumSimScore += counter*tra[n];
                                    if((verbous || calculation) && n!=tr1.length-1){
                                        outprint +=  " + "+counter+" * "+tra[n]+"% + ";
                                    }else{
                                        outprint +=  " + "+counter+" * "+tra[n]+"%";
                                    }
                                    childs2SLef -= counter;
                                }
                                //sumSimScore enthält das engültige Ergebniss - die HashSet kann
                                //und muss weg, sonst werden die Componenten noch mal betrachtet
                                simCandidate[n].remove(candidate.getURI());
                            }
                        }
                        int sim = sumSimScore / Math.max(childcount, target.intValue());
                        if(verbous || calculation) outprint += ") / "+Math.max(childcount, target.intValue());
                        for(int n=0; n<tr1.length; n++){
                            if(sim>=tr1[n]){
                                if(verbous || aBitVerbous)  System.out.println("3.5 Der Parent "+parent.getLocalName()+" entspricht dem Individual "+candidate.getLocalName()+" zu "+sim+"% -> "+(n+1)+"te Ähnlichkeit (aufgerundet="+tra[n]+")");
                                if(verbous || calculation)  System.out.println(outprint+" = "+sim+"%");
                                if(beQuiet) System.out.print("S"+n+" ");
                                parent.addProperty(m.getProperty(NSAS+"similarIndividual"+(n+1)), candidate);
                                StmtIterator st = candidate.listProperties(m.getProperty(NSAS+"hasParent"));
                                List<Statement> parentOfk = st.toList();
                                for (int k=0;k<parentOfk.size();k++) {
                                    if(parentOfk.get(k).getObject().canAs(Individual.class)){
                                        Individual p = parentOfk.get(k).getObject().as(Individual.class);
                                        checkThouse.add(p);
                                        if(verbous && khadija) System.out.println("3.6 Parent "+p.getLocalName()+" des Kandidaten "+candidate.getLocalName()+" wird der nächsten Parent Menge hinzugefügt (falls nicht bereits drin)");
                                    }
                                }
                                break;
                            }
                        }
                    }
                    if(checkThouse.size()>0){
                        StmtIterator st = parent.listProperties(m.getProperty(NSAS+"hasParent"));
                        List<Statement> parentOfk = st.toList();
                        for (int k=0;k<parentOfk.size();k++) {
                            if(parentOfk.get(k).getObject().canAs(Individual.class)){
                                Individual p = parentOfk.get(k).getObject().as(Individual.class);
                                checkThouse.add(p);
                                if(verbous && khadija) System.out.println("3.6 Parent "+p.getLocalName()+" des Parents "+parent.getLocalName()+" wird der nächsten Parent Menge hinzugefügt (falls nicht bereits drin)");
                            }
                        }
                        components2check.addAll(checkThouse);
                    }
                }else{
                    if(verbous) System.out.println("3.3 Eine Synonymitäts-Relation (Gleichheit o. Ähnlichkeit) bestand bereites zwischen: "+parent.getLocalName()+" und: "+candidate.getLocalName());
                }
            }
            for(int n=0; n<tr1.length; n++){
                HashMap<String,HashSet<Individual>> simCandidats = simCandidate[n];
                for(String candidateURI : simCandidats.keySet()){
                    Individual candidate = m.getIndividual(candidateURI);
                    if(!parent.hasProperty(synonym , candidate)){
                        LinkedList<Individual> checkThouse = new LinkedList<Individual>();
                        int childcount = ((Literal) candidate.getPropertyValue(m.getProperty(NSAS+"hasChildCount"))).getInt();
                        int counter = simCandidats.get(candidateURI).size();
                        int sumSimScore = (counter * tra[n]);
                        int childs2SLef = childcount - counter;
                        String outprint = "3.5 Berechnung: ("+counter+" * "+tra[n]+"%";
                        for(int n2=n+1; n2<tr1.length && childs2SLef>0; n2++){
                            if(simCandidate[n2].containsKey(candidateURI)){
                                int counter2 = simCandidate[n].get(candidateURI).size();
                                if(counter2>childs2SLef){
                                    counter2 = childs2SLef;
                                }else{
                                    if(counter2>0){
                                        sumSimScore += counter2*tra[n2];
                                        if((verbous || calculation) && n2!=tr1.length-1){
                                            outprint +=  " + "+counter2+" * "+tra[n2]+"% + ";

                                        }else{
                                            outprint +=  " + "+counter2+" * "+tra[n2]+"%";
                                        }
                                        childs2SLef -= counter;
                                    }
                                }
                                //sumSimScore enthält das engültige Ergebniss - das HashSet kann
                                //und muss weg, sonst werden die Componenten noch mal betrachtet
                                simCandidate[n2].remove(candidateURI);
                            }
                        }
                        int sim = sumSimScore / Math.max(childcount, target.intValue());
                        if(verbous || calculation) outprint += ") / "+Math.max(childcount, target.intValue());
                        for(int n3=0; n3<tr1.length; n3++){
                            if(sim>=tr1[n3]){
                                if(verbous || aBitVerbous) System.out.println("3.5 Der Parent "+parent.getLocalName()+" entspricht dem Individual "+candidate.getLocalName()+" zu "+sim+"% -> "+(n3+1)+"te Ähnlichkeit (aufgerundet="+tra[n3]+")");
                                if(verbous || calculation) System.out.println(outprint+" = "+sim+"% ");
                                parent.addProperty(m.getProperty(NSAS+"similarIndividual"+(n3+1)), candidate);
                                StmtIterator st = candidate.listProperties(m.getProperty(NSAS+"hasParent"));
                                List<Statement> parentOfk = st.toList();
                                for (int k=0;k<parentOfk.size();k++) {
                                    if(parentOfk.get(k).getObject().canAs(Individual.class)){
                                        Individual p = parentOfk.get(k).getObject().as(Individual.class);
                                        checkThouse.add(p);
                                        if(verbous && khadija) System.out.println("3.6 Parent "+p.getLocalName()+" des Parents wird der nächsten Parent Menge hinzugefügt (falls nicht bereits drin))");
                                    }
                                }
                                break;
                            }
                        }
                        if(checkThouse.size()>0){
                            StmtIterator st = parent.listProperties(m.getProperty(NSAS+"hasParent"));
                            List<Statement> parentOfk = st.toList();
                            for (int k=0;k<parentOfk.size();k++) {
                                if(parentOfk.get(k).getObject().canAs(Individual.class)){
                                    Individual p = parentOfk.get(k).getObject().as(Individual.class);
                                    checkThouse.add(p);
                                    if(verbous && khadija) System.out.println("3.6 Parent "+p.getLocalName()+" des Kandidaten wird der nächsten Parent Menge hinzugefügt (falls nicht bereits drin)");
                                }
                            }
                            components2check.addAll(checkThouse);
                        }
                    }
                }
            }
        }else{
                if(verbous || aBitVerbous) System.out.println("3 -> X Die Suche bei dem Parent wird später aufgenommen, da dieser über Komponenten verfügt, welche noch geprüft werden müssen");
        }

        }

/*4.X*/ int iteration = 1;
        boolean searchForMoore = true;
        while(searchForMoore){
            if(beQuiet) System.out.print("\nX"+iteration+" \n");
            LinkedList<Individual> parentsNEW = new LinkedList<Individual>();
            if(verbous||aBitVerbous) System.out.println("Komponenten zu weiterer überprüfung:");
            for (Iterator<Individual> i = components2check.iterator(); i.hasNext();) {
                Individual ind = i.next();
                parentsNEW.add(ind);
                if(verbous||aBitVerbous) System.out.println(ind.getLocalName());
            }
            parentsNEW = prepareParents(parentsNEW, false);
            components2check = findSimilarAndEquivalentIndividuals(m,NSAS,parentsNEW,atSimSets, iteration, tra);
            if(components2check.size()==0){
                searchForMoore = false;
            }else{
                iteration++;
                if(verbous||aBitVerbous) System.out.println("");
            }
        }
        if(beQuiet) System.out.println("\nGleichheit und Ähnlichkeit berechnet__________________________________________");
        System.out.println("Analysse fertig!");
        //Zum Schluss hacke die alle ab
        if(atts==null) m.removeAll(null, m.getProperty(NSAS+"check"), null);

    }
    
    public static HashSet<Individual> findSimilarAndEquivalentIndividuals(OntModel m, String NSAS, LinkedList<Individual> components2check, HashMap<String, HashSet<Individual>>[] atSimSets, int iteration, int[] tra){
        int schritt = iteration+3;
        HashSet<Individual> components2checkNEW = new HashSet<Individual>();
/*X*/   for(Iterator<Individual> c2cIT = components2check.iterator(); c2cIT.hasNext();){
            Individual parent = c2cIT.next();
            boolean possible = true;
            Property hasComp = m.getProperty(NSAS+"hasComponent");
            if(verbous && khadija){
                NodeIterator itComp = parent.listPropertyValues(hasComp);
                System.out.println(schritt+" - der nächste Parent erreicht folgende Componenten");
                while(itComp.hasNext()){
                    System.out.println(schritt+" -- "+itComp.next().as(Individual.class).getURI());
                }
            }
            for(Iterator<Individual> uncheckedParents = components2checkNEW.iterator(); uncheckedParents.hasNext();){
                Individual uncheckedParent  = uncheckedParents.next();
                if(verbous && khadija) System.out.println(schritt+" ? parent: "+parent.getLocalName()+" has unchecked Component : "+uncheckedParent.getLocalName()+"? ");
                if(!parent.equals(uncheckedParent) && parent.hasProperty(m.getProperty(NSAS+"hasComponent"), uncheckedParent)){
                    possible = false;
                    if(verbous && khadija) System.out.println(schritt+" Unmöglich. Parent gleicht "+uncheckedParent.getLocalName()+" oder hat diesen als Komponente");
                    break;
                }
            }
        if(possible){
            if(verbous || aBitVerbous) System.out.println(schritt+" Abarbeiten der Parentmenge beim Parent: "+parent.getURI());
/*X.1*/     NodeIterator stme = parent.listPropertyValues(m.getProperty(NSAS+"hasChild"));
            List<RDFNode>cmno = stme.toList();
            HashSet<Individual> cm = new HashSet<Individual>();
            for (Iterator<RDFNode> it1 = cmno.iterator(); it1.hasNext();) {
                RDFNode childnode = it1.next();
                if(childnode.canAs(Individual.class)) cm.add(childnode.as(Individual.class));
            }
            ArrayList<Individual> eq = new ArrayList<Individual>();
            ArrayList<Individual> eqCandidate = new ArrayList<Individual>();
            ArrayList<Integer> counters = new ArrayList<Integer>();
            HashMap<String, HashSet<Individual>>[] cmSimSets = new HashMap[tr1.length];
            for(int n=0;n<tr1.length;n++){
                cmSimSets[n] = new HashMap<String, HashSet<Individual>>();
            }
            Integer target = cm.size();
            //Bilden der Child-Menge:
            for(Iterator<Individual> cmIt = cm.iterator(); cmIt.hasNext();){
                Individual child = cmIt.next();
                if(!child.hasOntClass(m.getOntClass(NSAS+"Attribute"))){
                    //Im Falle von nicht Attributen, prüfe die Relationen
                    if(verbous) System.out.println(schritt+".1 Prüfe ob das Kind: "+child.getLocalName()+" äquivalente Komponenten besitzt, welche noch nicht hinzuaddiert worden sind");
                    StmtIterator eqC = child.listProperties(m.getProperty(NSAS+"equivalentIndividual"));
                    List<Statement>s = eqC.toList();
                    if(s.size()>0){
                        for(int k=0;k<s.size();k++){
                            Individual eqI = null;
                            if(s.get(k).getObject().canAs(Individual.class)){
                                eqI = s.get(k).getObject().as(Individual.class);
                                if(verbous && khadija) System.out.println(schritt+".1 betrachte "+eqI.getLocalName());
                                if(!eqI.equals(child)){
                                    if(verbous) System.out.println(schritt+".1 Ein äquivaltes Kind wird der Child-Menge hinzuadiert falls noch nicht in dieser oder Äquivalenz-Menge enthalten ist: "+eqI.getLocalName());
                                    if(!cm.contains(eqI) && !eq.contains(eqI)){
                                        if(verbous) System.out.println(schritt+".1 war noch nicht drin");
                                        eq.add(eqI);
                                    }else{
                                        if(verbous) System.out.println(schritt+".1 war schon drin");
                                    }
                                }else{
                                    if(verbous && khadija) System.out.println(schritt+".1 Es war das Kind");
                                }
                            }else{
                                if(verbous && khadija) System.out.println(schritt+".1 Das Kind konnte nicht als Individual interpretiert werden");
                            }
                        }
                    }else{
                        if(verbous) System.out.println(schritt+".1 Es besitzt keine äquivalenten Komponenten");
                    }
//                    if(child.hasOntClass(NSAS+"Attribute")){
                        for(int n=0;n<tr1.length;n++){
                            if(verbous) System.out.println(schritt+".1 Prüfe ob das System ähnliche Komponenten "+(n+1)+"ten Grades kennt, welche noch nicht übernommen worden sind");
                            StmtIterator simIte = child.listProperties(m.getProperty(NSAS+"similarIndividual"+(n+1)));
                            List<Statement>sSim = simIte.toList();
                            if(!cmSimSets[n].containsKey(child.getURI())) cmSimSets[n].put(child.getURI(), new HashSet<Individual>());
                            if(sSim.size()>0){
                                HashSet<Individual> sim = cmSimSets[n].get(child.getURI());
                                for(int k=0;k<sSim.size();k++){
                                    Individual simI = null;
                                    if(sSim.get(k).getObject().canAs(Individual.class)){
                                        simI = sSim.get(k).getObject().as(Individual.class);
                                        if(verbous && khadija) System.out.println(schritt+".1 betrachte "+simI.getLocalName());
                                        if(!simI.equals(child)){
                                            if(verbous) System.out.println(schritt+".1 Ein ähnliches Kind wird der Child-Menge hinzuadiert: "+simI.getLocalName());
                                            if(!cm.contains(simI)){
                                                if(verbous && khadija) System.out.println(schritt+".1 erfolgreich");
                                                sim.add(simI);
                                            }else{
                                                if(verbous && khadija) System.out.println(schritt+".1 war schon drin");
                                            }
                                        }
                                    }
                                }
                            }else{
                                if(verbous) System.out.println(schritt+".1 Es besitzt keine ähnlichen Komponenten i.s.v similarIndividual"+(n+1));
                            }
                        }
//                    }
                }
//                else{
//                    //Bei Attributen füge die restlichen Ebenen der Att-Menge hinzu
//                    for(int n=0;n<tr1.length;n++){
//                        if(atSimSets[n].containsKey(child.getURI())){
//                            if(!cmSimSets[n].containsKey(child.getURI()))   cmSimSets[n].put(child.getURI(), new HashSet<Individual>());
//                            HashSet<Individual> sim = cmSimSets[n].get(child.getURI());
//                            HashSet<Individual> sia = atSimSets[n].get(child.getURI());
//                            sim.addAll(sia);
//                        }
//                    }
//                }
            }
            cm.addAll(eq);
            HashMap<String,HashSet<Individual>>[] simCandidate = new HashMap[tr1.length];
            for(int n=0; n<tr1.length; n++){
                simCandidate[n]=new HashMap<String,HashSet<Individual>>();
            }
            int iter = 0;
/*X.2*/     for(Iterator<Individual> cmIt = cm.iterator(); cmIt.hasNext();){
                Individual child = cmIt.next();
                if(verbous || aBitVerbous){
                    iter++;
                    System.out.println(schritt+".2."+iter+" beginne mit den Kind "+child.getURI());
                }
                StmtIterator stc = child.listProperties(m.getProperty(NSAS+"hasParent"));
                Property synonym = m.getProperty(NSAS+"synonymIndividual");
                while(stc.hasNext()){
                    Statement st = stc.next();
                    Individual p = null;
                    if(st.getObject().canAs(Individual.class)) p = st.getObject().as(Individual.class);
                    if(p!=null && !p.equals(parent)){
                        if(!parent.hasProperty(synonym, p)){
                            if(verbous) System.out.println(schritt+".2 Betrachte dessen anderen Parent: "+p.getLocalName());
                            boolean found= false;
                            for(int k=0; !found && k<eqCandidate.size(); k++){
                                if(eqCandidate.get(k).equals(p)){
                                    Integer count = counters.get(k);
                                    count++;
                                    counters.remove(k);
                                    counters.add(k, count);
                                    if(verbous) System.out.println(schritt+".2 Parent bereits in der EqualCandidate Menge - counter hochzählen auf "+count);
                                    found=true;
                                }
                            }
                            if(!found){
                                if(verbous) System.out.println(schritt+".2 Parent wird als Kandidat neu eingefügt ");
                                eqCandidate.add(p);
                                counters.add(new Integer(1));
                            }
                        }else{
                            if(verbous) System.out.println(schritt+".2 Parent: "+p.getLocalName()+" bereits gegen das aktuelle Element ausgewertet");
                        }
                    }
                }
                for(int n=0; n<tr1.length; n++){
                    //Weitere Ebenen enthalten Sets mit Schlüssel=URI & Set=CounterListen
//                    HashSet<Individual> simChilds = cmSimSets[n].get(child.getURI());
                    HashSet<Individual> simChilds;
                    if(atSimSets[n].containsKey(child.getURI())){
                        simChilds = atSimSets[n].get(child.getURI());
                        if(cmSimSets[n].containsKey(child.getURI())){
                            if(simChilds!=null){
                                simChilds.addAll(cmSimSets[n].get(child.getURI()));
                            }else{
                                simChilds = cmSimSets[n].get(child.getURI());
                            }
                        }
                    }else{
                        simChilds = cmSimSets[n].get(child.getURI());
                    }
                    if(simChilds!=null){
                        HashMap<String, HashSet<Individual>> simParentsCandidate = simCandidate[n];
                        HashSet<Individual>  doneVotes = new HashSet();
                        for (Iterator<Individual> it1 = simChilds.iterator(); it1.hasNext();) {
                            Individual simChild = it1.next();
                            if(verbous) System.out.println(schritt+".2 fahre fort mit ähnlichen Kind der "+(n+1)+"ten Ähnlichkeitsrelation: "+simChild.getURI());
                            StmtIterator stSim = simChild.listProperties(m.getProperty(NSAS+"hasParent"));
                            while(stSim.hasNext()){
                                Statement st = stSim.next();
                                Individual p = null;
                                if(st.getObject().canAs(Individual.class)) p = st.getObject().as(Individual.class);
                                if(p!=null && !p.equals(parent) && !doneVotes.contains(p)){
                                    if(verbous) System.out.println(schritt+".2 Betrachte dessen anderen Parent: "+p.getURI());
                                    HashSet set = null;
                                    if(simParentsCandidate.containsKey(p.getURI())){
                                        set = simParentsCandidate.get(p.getURI());
                                        if(verbous && khadija) System.out.println(schritt+".2 Andere Parent bereits in der SimCandidate["+(n+1)+"] Menge - versuche dieser den SimChild hinzuzufügen");
                                    }else{
                                        set = new HashSet<Individual>();
                                        if(verbous && khadija) System.out.println(schritt+".2 Andere Parent noch nicht in der SimCandidate["+(n+1)+"] Menge - Parent wird neu eingefügt und SimChild seiner Counter Menge hinzuaddiert");

                                    }
                                    //Füge das Child zum Counter-Set hinzu
                                    if(set.add(simChild)){
                                        if(verbous && khadija) System.out.println(schritt+".2 erfolgreich");
                                        if(aBitVerbous && calculation) System.out.println(schritt+".2 ((HashSet) SimCandidate["+(n+1)+"]).get("+p.getLocalName()+").add("+simChild.getURI().substring(m.getNsPrefixURI("aD").length())+")");
                                    }else{
                                        if(verbous && khadija) System.out.println(schritt+".2 erfolglos denn dieses Kind war bereits drin");
                                        if(aBitVerbous && calculation) System.out.println(schritt+".2 ((HashSet) SimCandidate["+(n+1)+"]).get("+p.getLocalName()+") - > bereits drin");
                                    }
                                    simParentsCandidate.put(p.getURI(), set);
                                    doneVotes.add(p);
                                }
                            }
                        }
                    }
                }
            }
/*X.3*/     if(verbous) System.out.println(schritt+".3 Target = "+target);
            Property synonym = m.getProperty(NSAS+"synonymIndividual");
            for(int j=0;j<eqCandidate.size();j++){
                Individual candidate = eqCandidate.get(j);
                if(!parent.hasProperty(synonym, candidate)){
                    int        childcount = ((Literal) candidate.getPropertyValue(m.getProperty(NSAS+"hasChildCount"))).getInt();
                    LinkedList<Individual> checkThouse = new LinkedList<Individual>();
                    if(counters.get(j).equals(target)){
                        if(verbous || aBitVerbous) System.out.println(schritt+".3 Der Parent: "+parent.getLocalName()+" hat alle Attribute des Individuals: "+candidate.getLocalName());
                        if(childcount==target.intValue()){
                            //TODO: eqProved und eqGlobal können weg!!!
                            if(verbous || aBitVerbous) System.out.println(schritt+".4 Es hat auch keine weiteren, somit sind sie gleich i.s.i.G");
                            if(beQuiet) System.out.print("EQ ");
                            parent.addProperty(m.getProperty(NSAS+"equivalentIndividual"), candidate);
                            StmtIterator st = candidate.listProperties(m.getProperty(NSAS+"hasParent"));
                            List<Statement> parentOfk = st.toList();
                            for (int k=0;k<parentOfk.size();k++) {
                                if(parentOfk.get(k).getObject().canAs(Individual.class)){
                                    Individual p = parentOfk.get(k).getObject().as(Individual.class);
                                    checkThouse.add(p);
                                    if(verbous && khadija) System.out.println(schritt+".6 Parent "+p.getLocalName()+" des Kandidaten wird der nächsten Parent Menge hinzugefügt (falls nicht bereits drin)");
                                }
                            }
                        }else{
                            if(verbous){
                                int mehr = (childcount - target.intValue());
                                String weitere = "weitere";
                                if(mehr==1) weitere = "weteres";
                                System.out.println(schritt+".4 Es hat aber noch "+weitere+", somit NICHT gleich i.s.i.G");
                            }
                        }
                    }else{
                        //Suche nach Ähnlichkeit!
                        int sumSimScore = counters.get(j)*100;
                        int childs2SLef = childcount - counters.get(j);
                        String outprint = schritt+".5 Berechnung: ("+counters.get(j)+" * 100%";
                        for(int n=0; n<tr1.length && childs2SLef>0; n++){
                            if(simCandidate[n].containsKey(candidate.getURI())){
                                int counter = simCandidate[n].get(candidate.getURI()).size();
                                if(counter>childs2SLef){
                                    counter = childs2SLef;
                                }
                                if(counter>0){
                                    sumSimScore += counter*tra[n];
                                    if((verbous || calculation) && n!=tr1.length-1){
                                        outprint +=  " + "+counter+" * "+tra[n]+"% + ";
                                    }else{
                                        outprint +=  " + "+counter+" * "+tra[n]+"%";
                                    }
                                    childs2SLef -= counter;
                                }
                                //sumSimScore enthält das engültige Ergebniss - die HashSet kann
                                //und muss weg, sonst werden die Componenten noch mal betrachtet
                                simCandidate[n].remove(candidate.getURI());
                            }
                        }
                        int sim = sumSimScore / Math.max(childcount, target.intValue());
                        if(verbous || calculation) outprint += ") / "+Math.max(childcount, target.intValue());
                        for(int n=0; n<tr1.length; n++){
                            if(sim>=tr1[n]){
                                if(verbous || aBitVerbous) System.out.println(schritt+".5 Der Parent "+parent.getLocalName()+" entspricht dem Individual "+candidate.getLocalName()+" zu "+sim+"% -> "+(n+1)+"te Ähnlichkeit (aufgerundet="+tra[n]+")");
                                if(verbous || calculation) System.out.println(outprint+" = "+sim+"%");
                                if(beQuiet) System.out.print(" S"+n);
                                parent.addProperty(m.getProperty(NSAS+"similarIndividual"+(n+1)), candidate);
                                StmtIterator st = candidate.listProperties(m.getProperty(NSAS+"hasParent"));
                                List<Statement> parentOfk = st.toList();
                                for (int k=0;k<parentOfk.size();k++) {
                                    if(parentOfk.get(k).getObject().canAs(Individual.class)){
                                        Individual p = parentOfk.get(k).getObject().as(Individual.class);
                                        checkThouse.add(p);
                                        if(verbous && khadija) System.out.println(schritt+".0 Parent "+p.getLocalName()+" des Kandidaten wird der nächsten Parent Menge hinzugefügt (falls nicht bereits drin)");
                                    }
                                }
                                break;
                            }
                        }
                    }
                    if(checkThouse.size()>0){
                        StmtIterator st = parent.listProperties(m.getProperty(NSAS+"hasParent"));
                        List<Statement> parentOfk = st.toList();
                        for (int k=0;k<parentOfk.size();k++) {
                            if(parentOfk.get(k).getObject().canAs(Individual.class)){
                                Individual p = parentOfk.get(k).getObject().as(Individual.class);
                                checkThouse.add(p);
                                if(verbous && khadija) System.out.println(schritt+".0 Parent "+p.getLocalName()+" des Parents wird der nächsten Parent Menge hinzugefügt (falls nicht bereits drin)");
                            }
                        }
                        components2checkNEW.addAll(checkThouse);
                    }
                }else{
                    if(verbous) System.out.println(schritt+".3 Eine Synonymitäts-Relation (Gleichheit o. Ähnlichkeit) bestand bereites zwischen: "+parent.getLocalName()+" und: "+candidate.getLocalName());
                }
            }
            for(int n=0; n<tr1.length; n++){
                HashMap<String,HashSet<Individual>> simCandidats = simCandidate[n];
                for(String candidateURI : simCandidats.keySet()){
                    Individual candidate = m.getIndividual(candidateURI);
                    if(!parent.hasProperty(synonym , candidate)){
                        LinkedList<Individual> checkThouse = new LinkedList<Individual>();
                        int childcount = ((Literal) candidate.getPropertyValue(m.getProperty(NSAS+"hasChildCount"))).getInt();
                        int counter = simCandidats.get(candidateURI).size();
                        int sumSimScore = (counter * tra[n]);
                        int childs2SLef = childcount - counter;
                        String outprint = schritt+".5 Berechnung: ("+counter+" * "+tra[n]+"%";
                        for(int n2=n+1; n2<tr1.length && childs2SLef>0; n2++){
                            if(simCandidate[n2].containsKey(candidateURI)){
                                int counter2 = simCandidate[n].get(candidate.getURI()).size();
                                if(counter2>childs2SLef){
                                    counter2 = childs2SLef;
                                }
                                if(counter2>0){
                                    sumSimScore += counter2*tra[n2];
                                    if((verbous || calculation) && n2!=tr1.length-1){
                                        outprint +=  " + "+counter2+" * "+tra[n2]+"% + ";

                                    }else{
                                        outprint +=  " + "+counter2+" * "+tra[n2]+"%";
                                    }
                                    childs2SLef -= counter2;
                                }
                                //sumSimScore enthält das engültige Ergebniss - die HashSet kann
                                //und muss weg, sonst werden die Componenten noch mal betrachtet
                                simCandidate[n2].remove(candidate.getURI());
                            }
                        }
                        int sim = sumSimScore / Math.max(childcount, target.intValue());
                        if(verbous || calculation) outprint += ") / "+Math.max(childcount, target.intValue());
                        for(int n3=0; n3<tr1.length; n3++){
                            if(sim>=tr1[n3]){
                                if(verbous || aBitVerbous) System.out.println(schritt+".5 Der Parent "+parent.getLocalName()+" entspricht dem Individual "+candidate.getLocalName()+" zu "+sim+"% -> "+(n+1)+"te Ähnlichkeit (aufgerundet="+tra[n]+")");
                                if(verbous || calculation) System.out.println(outprint+" = "+sim+"% ");
                                parent.addProperty(m.getProperty(NSAS+"similarIndividual"+(n3+1)), candidate);
                                StmtIterator st = candidate.listProperties(m.getProperty(NSAS+"hasParent"));
                                List<Statement> parentOfk = st.toList();
                                for (int k=0;k<parentOfk.size();k++) {
                                    if(parentOfk.get(k).getObject().canAs(Individual.class)){
                                        Individual p = parentOfk.get(k).getObject().as(Individual.class);
                                        checkThouse.add(p);
                                        if(verbous) System.out.println(schritt+".0 Parent "+p.getLocalName()+" des Parents wird der nächsten Parent Menge hinzugefügt (falls nicht bereits drin))");
                                    }
                                }
                                break;
                            }
                        }
                        if(checkThouse.size()>0){
                            StmtIterator st = parent.listProperties(m.getProperty(NSAS+"hasParent"));
                            List<Statement> parentOfk = st.toList();
                            for (int k=0;k<parentOfk.size();k++) {
                                if(parentOfk.get(k).getObject().canAs(Individual.class)){
                                    Individual p = parentOfk.get(k).getObject().as(Individual.class);
                                    checkThouse.add(p);
                                    if(verbous && khadija) System.out.println(schritt+".0 Parent "+p.getLocalName()+" des Kandidaten wird der nächsten Parent Menge hinzugefügt (falls nicht bereits drin)");
                                }
                            }
                            components2checkNEW.addAll(checkThouse);
                        }
                    }
                }
            }
        }else{
                if(verbous || aBitVerbous) System.out.println(schritt+" -> X Die Suche bei dem Parent wird später aufgenommen, da dieser über Komponenten verfügt, welche noch geprüft werden müssen");
        }
        }
        return components2checkNEW;
    }

    public static void handleDifferencesBottomUp(String filename){
        OntModel m = ModelManagement.getInstance().getModel();
        String  NSAS = m.getNsPrefixURI("aS");
        List<Resource> resList = m.listSubjectsWithProperty(m.getProperty(NSAS+"from_XML_FILE"), filename).toList();
        Property synonym = m.getProperty(NSAS+"equivalentIndividual");
        for(int i = 0; i<resList.size(); i++){
            Resource ind = resList.get(i);
            System.out.println("Suche nach equivalenen Komponenten zu "+ind.getLocalName());
            for(int j = i+1; j<resList.size(); j++){
                Resource ind2 = resList.get(j);
                if(ind.hasProperty(synonym, ind2)){
                    System.out.println("Die Datei "+filename+" mit der Wurzel "+ind.getLocalName()+" ist komplett gleich geblieben. Deshalb wird deren Kopie mit der Wurzel "+ind2.getLocalName()+" ausradiert!");
                    LinkedList<Individual> rausdamit = new LinkedList<Individual>();
                    StmtIterator ind2Components = ind2.listProperties(m.getProperty(NSAS+"hasComponent"));
                    List<Statement> ind2ComponentsList = ind2Components.toList();
                    if(ind2.canAs(Individual.class)) rausdamit.add(ind2.as(Individual.class));
                    for(int k = 0; k<ind2ComponentsList.size(); k++){
                        Statement statement = ind2ComponentsList.get(k);
                        Individual individual = null;
                        if(statement.getObject().canAs(Individual.class)){
                            individual = statement.getObject().as(Individual.class);
                            if(!individual.hasOntClass(m.getOntClass(NSAS+"Attribute"))){
                                rausdamit.add(individual);
                            }
                        }
                    }
                    removeComponents(rausdamit);
                }else{
                    Property similar = m.getProperty(NSAS+"similarIndividual");
                    if(ind.hasProperty(similar, ind2)){
                        System.out.println("Die Datei weisst immer noch gewisse Ähnlichkeiten auf");
                    }else{
                        System.out.println("Es ist nun was ganz anderes");
                    }
                }
                resList.remove(ind2);
            }
        }
    }

    public static void handleDifferencesTopDown(String filename){
        System.out.println("NOT IMPLEMENTED JET");
//        OntModel m = ModelManagement.getInstance().getModel();
//        String  NSAS = m.getNsPrefixURI("aS");
//        List<Resource> resList = m.listSubjectsWithProperty(m.getProperty(NSAS+"from_XML_FILE"), filename).toList();
//        Property synonym = m.getProperty(NSAS+"equivalentIndividual");
//        for(int i = 0; i<resList.size(); i++){
//
//        }
    }

    public static void removeComponents(LinkedList<Individual> rausdamit){
        OntModel m = ModelManagement.getInstance().getModel();
        if(verbous || aBitVerbous) System.out.println("\nEntfernt werden:");
        for (Iterator<Individual> it = rausdamit.iterator(); it.hasNext();) {
            Individual individual = it.next();
            if(verbous || aBitVerbous) System.out.println(individual.getLocalName());
            StmtIterator indIt = individual.listProperties();
            m.remove(indIt);
            Selector selector1 = new SimpleSelector(null, null, individual);
            m.remove(m.listStatements(selector1));
//            Selector selector2 = new SimpleSelector(individual, null, (RDFNode)null);
//            m.remove(m.listStatements(selector2));
        }
    }

    public static void removeComponent(Individual rausdamit, boolean childCountCorrection){
        OntModel m = ModelManagement.getInstance().getModel();
        String  NSAS = m.getNsPrefixURI("aS");
        if(verbous || aBitVerbous) System.out.println("\nEntfernt wird:");        
        Individual individual = rausdamit;
        if(verbous || aBitVerbous) System.out.println(individual.getLocalName());
        //Korrigiere die Anzahl der Kinder bei dessen Parrent
        if(childCountCorrection){
            List<Resource> resList = m.listSubjectsWithProperty(m.getProperty(NSAS+"hasChild"), individual).toList();
            for (Iterator<Resource> it = resList.iterator(); it.hasNext();) {
                Individual parent = it.next().as(Individual.class);
                RDFNode  compc = parent.getPropertyValue(m.getProperty(NSAS+"hasChildCount"));
                if(compc!=null){
                    int childcount = ((Literal) compc).getInt();
                    parent.removeProperty(m.getProperty(NSAS+"hasChildCount"),compc);
                    if(verbous || aBitVerbous) System.out.println("Korigiere die Anzahl der Kinder beim Parent: "+parent.getLocalName()+" von "+childcount+" auf "+(childcount-1));
                    parent.addLiteral(m.getProperty(NSAS+"hasChildCount"), (childcount-1));
                }
            }
        }
        Selector selector1 = new SimpleSelector(null, null, individual);
        m.remove(m.listStatements(selector1));
        Selector selector2 = new SimpleSelector(individual, null, (RDFNode)null);
        m.remove(m.listStatements(selector2));
        Selector selector3 = new SimpleSelector(individual, null, (Literal)null);
        m.remove(m.listStatements(selector3));
//        Selector selector4 = new SimpleSelector(null, null, (Literal)individual);
//        m.remove(m.listStatements(selector4));
        StmtIterator indIt = individual.listProperties();
        m.remove(indIt);

    }
    
}

