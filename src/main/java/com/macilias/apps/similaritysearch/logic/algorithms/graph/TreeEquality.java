/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.macilias.apps.similaritysearch.logic.algorithms.graph;


import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Maciej Niemczyk
 */
public class TreeEquality {

    private static final boolean verbous = false;
    private static final boolean aBitVerbous = true;
    //Für Reasoner sollte noch der XML2OWL Model statt Base übergeben werden
    private static final boolean useFBReasoner = false;

    /*
     * Diese Methode Sortiert die Parent-Menge nach der Anzahl der Abwesenheit
     * komplexer Elemente. An der ersten Stelle sollten somit Elemente mit nur
     * Attribute Knoten als Kinder stehen. Sollte dies nicht der Fall sein, so
     * ist jede weitere Suche gleicher Elemente ausichtslos und somit wertlos.
     */
    public static boolean prepare(LinkedList<Individual> parents, OntModel m, String NSAS){
        boolean worth = false;
        if(verbous){
            System.out.print("SORTIEREN: ursprüngliche Menge:");
            for (Iterator<Individual> it = parents.iterator(); it.hasNext();) {
                Individual individual = it.next();
                System.out.print(", "+individual.getLocalName());
            }
            System.out.print(" \n");
        }
        LinkedList<Individual> kompChilds =  new LinkedList<Individual>();
        for(int i=0;i<parents.size();i++){
            Individual individual = parents.get(i);
            StmtIterator childite = individual.listProperties(m.getProperty(NSAS+"hasChild"));
            ArrayList<Statement> ll = (ArrayList<Statement>) childite.toList();
            boolean komplex  = false;
            for(int j=0; j<ll.size()&& !komplex; j++){
                Individual child = ll.get(j).getObject().as(Individual.class);
                if(!child.hasOntClass(m.getOntClass(NSAS+"Attribute"))){
                    individual.addLiteral(m.getProperty(NSAS+"hasComplexChild"), true);
                    if(verbous) System.out.println("Komplexes Kind gefunden! beim: "+individual.getLocalName());
                    kompChilds.add(individual);
                    parents.remove(i);
                    i--;
                    komplex = true;
                }else{
                    worth = true;
                }
            }
        }
        //Sortire noch die Komplexen nach ihrer Baumtiefe / Pfadlänge
        if(worth && kompChilds.size()>1){
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
                if(verbous) System.out.println("Individual "+individual.getLocalName()+" ist auf Ebene: "+ebene);
            }
            int size = (max - min + 1);
            if(verbous) System.out.println("MAX="+max+" MIN="+min+" size="+size);
            LinkedList[] buckets = new LinkedList[size];
            for(int i=0;i<buckets.length;i++){
                buckets[i] = new LinkedList<Individual>();
            }
            for (Iterator<Individual> it = kompChilds.iterator(); it.hasNext();) {
                Individual individual = it.next();
                Literal pn = (Literal) individual.getPropertyValue(m. getProperty(NSAS+"hasLevel"));
                int ebene = pn.getInt();
                buckets[ebene-min].add(individual);
            }
            kompChilds =  new LinkedList<Individual>();
            for(int i=buckets.length-1;i>=0;i--){
                LinkedList<Individual> level = buckets[i];
                for (Iterator<Individual> it = level.iterator(); it.hasNext();) {
                    Individual individual = it.next();
                    kompChilds.add(individual);
                }
            }
        }
        parents.addAll(kompChilds);
        if(verbous || aBitVerbous){
            System.out.print("SORTIEREN: nachträgliche Menge:");
            for (Iterator<Individual> it = parents.iterator(); it.hasNext();) {
                Individual individual = it.next();
                System.out.print(", "+individual.getLocalName());
            }
            System.out.print(" \n");
        }
        //BOF debug
        if(verbous){
            ExtendedIterator<Individual> it = m.listIndividuals(m.getOntClass(NSAS+"Komplex"));
            if(it.hasNext()) System.out.println("Es gibt folgende Komponenten mit komplexen Kindern:");
            while(it.hasNext()){
                Individual ind = it.next();
                StmtIterator it2 = ind.listProperties(m.getProperty(NSAS+"hasComplexChild"));
                while(it2.hasNext())
                System.out.println(it2.next().getSubject().as(Individual.class).getLocalName());
            }
        }
        //EOF debug
        return worth;
    }



    /*
     * Diese Methode sucht nach equivalenten Individuen im Sinne der inhaltlichen Gleichheit
     * Dis dient der Performazsteigerung für spätere Ähnlichkeiten Suche, da auch komplexe
     * Elemente nicht mehr auf gleichheit geprüft werden müssen.
     * //Update
     * Dies ist unnötig! Die Gleichheit muss ledeglich für neu hinzugefügte Elemente geprüft werden.
     * 
     */
    public static void findEquivalentIndividuals(OntModel m){
        
/*
 * SCHLACHTPLAN:
 * 0. Attribute, welche nicht bereits betrachtet wurden rausfiltern
 * 1. Diejenigen mit Mehrfachparents rausfiltern (Attribut-Menge bilden)                        [A2&5,A2&5,A2&5,A2&5,A2&5,A2&5]
 * 2. Die Parents der Attribute der Attribut-Menge zu Parent-Menge zusammenfassen {und sortieren}* [CD2,CD5]
 * 3. Pro Parent: (bei dem die Äquivalenz noch nicht festgestellt wurde)
 *               3.1 - Betrachte die über hasChild erreichbaren Komponenten                     [A2&5,A2&5,A2&5,A2&5,A2&5,A2&5] *vorher stand hier Attribute, es worde aber schon alle Komponenten abgefragt
 *                              -Hintergrund zu den "Doppelgemoppel": Überprüfung ob alle seine Attribute auch einen anderen Parent haben und dieser sonst keine Kinder hat
 *               3.2 - Pro Komponente füge deren Parents einer EqualCandidate Liste hinzu (falls schon vorhanden erhöhe deren counter) EqC[CD2[c=6],CD5[c=6]]
 *                              -{bei nicht Attributen betrachte auch äquivalente Komponenten}*
 *               3.3 - Gehe die EqualCandidate Liste durch, jeder deren counter nicht an die Anzahl der Attribute des Betrachteten Parent-Elementes heranreicht fliegt raus
 *                              -Hintergurnd: der Rest hat genau so viele gleiche Attribute wie das Betrachtete Element
 *               3.4 - Überprüfe ob er noch mehr hat und wenn ja dann fligt er auch raus
 *               3.5 - setelle die "equalIndividual" Bezihung auf den übrigen her (Sie haben genau die gleichen Komponenten)
 *               3.6 - Sichere die EqualCandidate in einer globalen Equvalenz-Menge<EqualCandidate>.add(Equals) für Schritt 4.
 * 4. Schaue ob auf einer höheren Ebenen nach ob noch Übereinstimmungen gefunden werden können. Gehe die Equivalenz-Mengen durch. Pro Element:
 *               4.1 - Füge die Eltern der Knoten bei den die Equivalenz nachgewiesen worde einer nuen Parent-Menge hinzu
 *                              - Hintergrund: Die so ermitellten Equivalenz-Megen Elemente können ihrerseits equivalente Parents besitzten
 *                                Jetzt werden sie aber nicht mehr über mehrere hasParent Bezihungen verfügen
 *               4.2 - 
 *
 *               - Sortiere die Menge nach der Kinderanzahl
 *               - Bei Elementen mit gleicher Anzahl der Kinder erstelle neue Equivalenz-Mengen folgendermassen:
 *                              - nehme die Kinder der ersten Komponente und füge Sie einer neuen Child-Menge hinzu
 *                              -
 */

        String NSAS = m.getNsPrefixURI("aS");
        ExtendedIterator<Individual> it = m.listIndividuals(m.getOntClass(NSAS+"Attribute"));
        ArrayList<Individual> attribute = (ArrayList<Individual>) it.toList();
        LinkedList<Individual> mehrfatt = new LinkedList<Individual>();
        LinkedList<Individual> parentme = new LinkedList<Individual>();
        LinkedList<Individual> eqGlobal = new LinkedList<Individual>();
        for(int i=0;i<attribute.size();i++){
            Individual ind = attribute.get(i);
/*0*/        if(ind.hasLiteral(m.getProperty(NSAS+"check"), true)){
                StmtIterator st = ind.listProperties(m.getProperty(NSAS+"hasParent"));
                List<Statement> parentOfi = st.toList();
/*1*/           if(parentOfi.size()>1){
                    mehrfatt.add(ind);
                    //Für jedes Attribut der Attribut-Menge
                    //nehme die Parent-Elemente in die Parent-Menge auf
/*2*/               for (int j=0;j<parentOfi.size();j++) {
                        if(parentOfi.get(j).getObject().canAs(Individual.class)){
                            Individual p = parentOfi.get(j).getObject().as(Individual.class);
                            if (!parentme.contains(p)) {
                                parentme.add(p);
                            }
                        }
                    }
                }
            }
        }
        if(!prepare(parentme,m,NSAS)) System.out.println("Es wird keine Gleichen geben");
/*3*/   for(int i=0; i<parentme.size(); i++){
            Individual parent = parentme.get(i);
            if(verbous || aBitVerbous) System.out.println("3 Abarbeiten der Parentmenge beim Parent: "+parent.getURI());
        if(!eqGlobal.contains(parentme.get(i))){
/*3.1*/     NodeIterator stme = parent.listPropertyValues(m.getProperty(NSAS+"hasChild"));
            List<RDFNode>cmno = stme.toList();
            ArrayList<Individual> cm = new ArrayList<Individual>();
            for (Iterator<RDFNode> it1 = cmno.iterator(); it1.hasNext();) {
                RDFNode childnode = it1.next();
                if(childnode.canAs(Individual.class)) cm.add(childnode.as(Individual.class));
            }
            ArrayList<Individual> eq = new ArrayList<Individual>();
            ArrayList<Individual> eqCandidate = new ArrayList<Individual>();
            ArrayList<Individual> eqProved = new ArrayList<Individual>();
            ArrayList<Integer> counters = new ArrayList<Integer>();
            Integer target = cm.size();
            for(int j=0; j<cm.size();j++){
                Individual child = cm.get(j);
                if(verbous) System.out.println("3.1 Prüfe ob das Kind: "+child.getURI()+" äquivalente Komponenten besitzt, welche noch nicht hinzuaddiert woredn sind");
                StmtIterator eqC = child.listProperties(m.getProperty(NSAS+"equivalentIndividual"));
                List<Statement>s = eqC.toList();
                if(s.size()>0){
                    for(int k=0;k<s.size();k++){
                        Individual eqI = null;
                        if(s.get(k).getObject().canAs(Individual.class)){
                            eqI = s.get(k).getObject().as(Individual.class);
                            if(!eqI.equals(child)){
                                if(verbous) System.out.println("3.1 Ein äquivaltes Kind wird der Menge hinzuadiert falls noch nicht der Äquivalenz oder Child Menge entalten ist: "+eqI.getURI());
                                if(!cm.contains(eqI) && !eq.contains(eqI)){
                                    if(verbous) System.out.println("3.1 war noch nicht drin");
                                    eq.add(eqI);
                                }else{
                                    if(verbous) System.out.println("3.1 war schon drin");
                                }
                            }
                        }
                    }
                }else{
                    if(verbous) System.out.println("3.1 Es besitzt keine äquivalenten Komponenten");
                }
            }
            cm.addAll(eq);
/*3.2*/     for(int j=0; j<cm.size();j++){
                Individual child = cm.get(j);
                if(verbous) System.out.println("3.2 beginne mit den Kind "+child.getURI());
                StmtIterator stc = child.listProperties(m.getProperty(NSAS+"hasParent"));
                while(stc.hasNext()){
                    Statement st = stc.next();
                    Individual p = null;
                    if(st.getObject().canAs(Individual.class)) p = st.getObject().as(Individual.class);
                    if(p!=null && !p.equals(parent) && !eqGlobal.contains(p)){
                        if(verbous) System.out.println("3.2 Betrachte dessen anderen Parent"+p.getURI());
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
                    }
                }
            }
/*3.3*/     if(verbous) System.out.println("3.3 Target = "+target);
            for(int j=0;j<counters.size();j++){
                //if(verbous) System.out.println("3.3 Counters.get("+j+") = "+counters.get(j));
                if(counters.get(j).equals(target)){
                    Individual candidate = eqCandidate.get(j);
                    if(verbous || aBitVerbous) System.out.println("3.3 Das Individual "+candidate.getURI()+" hat alle Attribute des Individuals "+parent.getURI());
                    StmtIterator stch = candidate.listProperties(m.getProperty(NSAS+"hasChild"));
                    List<Statement>temp = stch.toList();
                    Integer childcount  = temp.size();
/*3.4*/             if(childcount.equals(target)){
                        eqProved.add(candidate);
                        if(verbous) System.out.println("3.4 Es hat auch keine weiteren, somit sind sie gleich i.s.i.G");
                    }else{
                        if(verbous || aBitVerbous){
                            int mehr = (childcount.intValue() - target.intValue());
                            String weitere = "weitere";
                            if(mehr==1) weitere = "weteres";
                            System.out.println("3.4 Es hat aber noch "+mehr+" "+weitere+", somit NICHT gleich i.s.i.G");
                        }
                    }
                }
            }
/*3.5*/     for(int j=0;j<eqProved.size();j++){
                parent.addProperty(m.getProperty(NSAS+"equivalentIndividual"), eqProved.get(j));
            }
/*3.6*/     if(eqProved.size()>0) eqGlobal.add(parent); eqGlobal.addAll(eqProved); 
        }else{
                if(verbous) System.out.println("3 Dieser Parent ist schon in der EquivalentMenge");
        }
        }
        if(verbous||aBitVerbous){
            System.out.println("Equivalenz-Menge:");
            for (int i = 0; i < eqGlobal.size(); i++) {
                System.out.println(eqGlobal.get(i).getURI());
            }
        }
/*4.X*/ int iteration = 1;
        boolean searchForMoore = true;
        while(searchForMoore){
            eqGlobal = findEquivalentIndividuals(m,NSAS,eqGlobal,iteration);
            if(eqGlobal.size()==0){
                searchForMoore = false;
            }else{
                iteration++;
                if(verbous||aBitVerbous){
                    System.out.println("Equivalenz-Menge:");
                    for (int i = 0; i < eqGlobal.size(); i++) {
                        System.out.println(eqGlobal.get(i).getURI());
                    }
                }
            }
        }
        System.out.println("Fertig!");
        //Zum Schluss hacke die alle ab
        m.removeAll(null, m.getProperty(NSAS+"check"), null);


        if(false){
            System.out.println("(Mehrfach-Parent) Attribut-Menge:");
            for (int i = 0; i < mehrfatt.size(); i++) {
                System.out.println(mehrfatt.get(i).getURI());
            }
            System.out.println("Parent-Menge:");
            for (int i = 0; i < parentme.size(); i++) {
                System.out.println(parentme.get(i).getURI());
            }
        }
    }

    public static LinkedList<Individual> findEquivalentIndividuals(OntModel m, String NSAS, LinkedList<Individual> eqGlobal, int iteration){
        int schritt = 3+iteration;
        LinkedList<Individual> eqGlobalNEW = new LinkedList<Individual>();
        LinkedList<Individual> parentsNEW  = new LinkedList<Individual>();
/*X.0*/ for(int i=0;i<eqGlobal.size();i++){
            Individual komponent = eqGlobal.get(i);
            StmtIterator st = komponent.listProperties(m.getProperty(NSAS+"hasParent"));
            List<Statement> parentOfk = st.toList();
            for (int j=0;j<parentOfk.size();j++) {
                if(parentOfk.get(j).getObject().canAs(Individual.class)){
                    Individual p = parentOfk.get(j).getObject().as(Individual.class);
                    if (!parentsNEW.contains(p) && !eqGlobal.contains(p)) {
                        parentsNEW.add(p);
                        if(verbous) System.out.println(schritt+".0 Parent "+p.getURI()+" wird der Parent Menge hinzugefügt");
                    }
                }
            }
        }
/*X.1*/ for(int i=0;i<parentsNEW.size();i++){
            Individual parent = parentsNEW.get(i);
            if(verbous || aBitVerbous) System.out.println(schritt+".1 Abarbeiten der Parentmenge beim Parent: "+parent.getURI());
            if(!eqGlobalNEW.contains(parent)){
                NodeIterator stme = parent.listPropertyValues(m.getProperty(NSAS+"hasChild"));
                List<RDFNode>cmno = stme.toList();
                ArrayList<Individual> cm = new ArrayList<Individual>();
                for (Iterator<RDFNode> it1 = cmno.iterator(); it1.hasNext();) {
                    RDFNode childnode = it1.next();
                    if(childnode.canAs(Individual.class)) cm.add(childnode.as(Individual.class));
                }
                ArrayList<Individual> eq = new ArrayList<Individual>();
                ArrayList<Individual> eqCandidate = new ArrayList<Individual>();
                ArrayList<Individual> eqProved = new ArrayList<Individual>();
                ArrayList<Integer> counters = new ArrayList<Integer>();
                Integer target = cm.size();
                for(int j=0; j<cm.size();j++){
                    Individual child = cm.get(j);
                    if(!child.hasOntClass(NSAS+"Attribute")){
                        if(verbous) System.out.println(schritt+".1 Prüfe ob das Kind: "+child.getURI()+" äquivalente Komponenten besitzt, welche noch nicht hinzuaddiert woredn sind");
                        StmtIterator eqC = child.listProperties(m.getProperty(NSAS+"equivalentIndividual"));
                        List<Statement>s = eqC.toList();
                        if(s.size()>0){
                            for(int k=0;k<s.size();k++){
                                Individual eqI = null;
                                if(s.get(k).getObject().canAs(Individual.class)){
                                    eqI = s.get(k).getObject().as(Individual.class);
                                    if(!eqI.equals(child)){
                                        if(verbous) System.out.println(schritt+".1 Ein äquivaltes Kind wird der Menge hinzuadiert falls noch nicht der Äquivalenz oder Child Menge entalten ist: "+eqI.getURI());
                                        if(!cm.contains(eqI) && !eq.contains(eqI)){
                                            if(verbous) System.out.println(schritt+".1 war noch nicht drin");
                                            eq.add(eqI);
                                        }else{
                                            if(verbous) System.out.println(schritt+".1 war schon drin");
                                        }
                                    }
                                }
                            }
                        }else{
                            if(verbous) System.out.println(schritt+".1 Es besitzt keine äquivalenten Komponenten");
                        }
                    }
                }
                cm.addAll(eq);
/*X.2*/         for(int j=0; j<cm.size();j++){
                    Individual child = cm.get(j);
                    if(verbous) System.out.println(schritt+".2 beginne mit den Kind "+child.getURI());
                    StmtIterator stc = child.listProperties(m.getProperty(NSAS+"hasParent"));
                    while(stc.hasNext()){
                        Statement st = stc.next();
                        Individual p = null;
                        if(st.getObject().canAs(Individual.class)) p = st.getObject().as(Individual.class);
                        if(p!=null && !p.equals(parent) && !eqGlobalNEW.contains(p)){
                            if(verbous) System.out.println(schritt+".2 Betrachte dessen anderen Parent"+p.getURI());
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
                        }
                    }
                }
/*X.3*/         if(verbous) System.out.println(schritt+".3 Target = "+target);
                for(int j=0;j<counters.size();j++){
                //if(verbous) System.out.println("3.3 Counters.get("+j+") = "+counters.get(j));
                    if(counters.get(j).equals(target)){
                        Individual candidate = eqCandidate.get(j);
                        if(verbous || aBitVerbous) System.out.println(schritt+".3 Das Individual "+candidate.getURI()+" hat alle Attribute des Individuals "+parent.getURI());
                        StmtIterator stch = candidate.listProperties(m.getProperty(NSAS+"hasChild"));
                        List<Statement>temp = stch.toList();
                        Integer childcount  = temp.size();
/*X.4*/                 if(childcount.equals(target)){
                            eqProved.add(candidate);
                            if(verbous) System.out.println(schritt+".4 Es hat auch keine weiteren, somit sind sie gleich i.s.i.G");
                        }else{
                            if(verbous || aBitVerbous){
                                int mehr = (childcount.intValue() - target.intValue());
                                String weitere = "weitere";
                                if(mehr==1) weitere = "weteres";
                                System.out.println(schritt+".4 Es hat aber noch "+mehr+" "+weitere+", somit NICHT gleich i.s.i.G");
                            }
                        }
                    }
                }
/*X.5*/         for(int j=0;j<eqProved.size();j++){
                    parent.addProperty(m.getProperty(NSAS+"equivalentIndividual"), eqProved.get(j));
                }
/*X.6*/         if(eqProved.size()>0){
                    eqGlobalNEW.add(parent);
                    eqGlobalNEW.addAll(eqProved);
                }
            }else{
                if(verbous || aBitVerbous) System.out.println(schritt+".1 Schon in der globalen Äquivalenz Menge vorhanden");
            }
        }
        return eqGlobalNEW;
    }
}
