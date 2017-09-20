/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.macilias.apps.similaritysearch.logic.algorithms.graph;

import com.hp.hpl.jena.ontology.OntModel;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.macilias.apps.similaritysearch.util.SoutConfig;
import com.macilias.apps.similaritysearch.data.XML2OWL;
import com.macilias.apps.similaritysearch.data.XML2OWL.*;
import com.macilias.apps.similaritysearch.logic.algorithms.graph.StructureClasses.*;

/**
 *
 * @author Maciej Niemczyk
 */
public final class StructureManagement {
    //globale Vertikale
    private Vector<String>   gvVS = new Vector<String>();
    private Vector<Vertikale> gvV = new Vector<Vertikale>();
//    //globale Positionen
//    private Vector<Position> gPos = new Vector<Position>();
//    private int consolidateOffset = 0;
    public OntModel m = ModelManagement.getInstance().getModel();
    protected String NSAS = m.getNsPrefixURI("aS");;

    public boolean verbous = SoutConfig.getVerbous_SM();
    public boolean aBitVerbous = SoutConfig.getaBitVerbous_SM();

    //Singelton:
    private static StructureManagement instance;

    public synchronized static StructureManagement getInstance(){
        if(instance==null) instance = new StructureManagement();
        instance.resetOutPrint();
        return instance;
    }

    private StructureManagement(){

    }
    //ENDE Singelton

    private void resetOutPrint(){
        verbous = SoutConfig.getVerbous_SM();
        aBitVerbous = SoutConfig.getaBitVerbous_SM();
    }


//    public Vector<Position> getgPos() {
//        return gPos;
//    }

    public Vector<Vertikale> getGvV() {
        return gvV;
    }

    public Vector<String> getGvVS() {
        return gvVS;
    }
    public int getOffset(){
//        return this.gPos.size();
        return this.gvV.size();
    }

    // Speichern der Vertikalen in einer Datei (vertikale.txt)
    public void writeOutVerticale(){
        DataOutputStream out1 = null;
        try {
            File file = new File("Ontologie/gVertikale.txt");
            out1 = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
            if (gvVS != null) {
                for (int i = 0; i < gvVS.size(); i++) {
                    String vString = "CVertikale" + (i) + ": " + gvVS.get(i);
                    Vertikale v = gvV.get(i);
                    for(int j=0; j<v.getURIs().size(); j++){
                        vString += v.getURIs().get(j) +"\n";
                    }
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
//                out1.writeBytes(printKeyPositions());
                out1.flush();
            } catch (IOException ex) {
                Logger.getLogger(XML2OWL.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(StructureManagement.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                out1.close();
            } catch (IOException ex) {
                Logger.getLogger(StructureManagement.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

//    public String printKeyPositions(){
//        String keys="gPosKeys:";
//        for(int i=0;i<gPos.size();i++){
//            keys+="["+(i)+"="+gPos.get(i).currentpos+"]";
//            if(gPos.get(i).currentpos<gvV.size()){
//                Vertikale v = gvV.get(gPos.get(i).currentpos);
//                keys+="key="+v.key+" ";
//            }else{
//                keys+="KEY OUT OF RANGE";
//            }
//        }
//        System.out.println(keys);
//        return keys;
//    }

//    public void updatePosition(int pos, int newPos){
//        System.out.println("Das Element mit den pos "+pos+" ist nun auf der Position "+newPos);
//        gPos.get(pos).currentpos = newPos;
//    }

//    private void updatePositionWhileImport(int pos, int newPos, int offset){
//        if(pos+consolidateOffset>gPos.size()){
//            System.out.println("FEHLER: Versuchter Zugriff auf das Element: "+(pos+consolidateOffset)+" bei einer Länger der gPos von "+gPos.size());
//            System.out.println("key="+pos+" newPos="+newPos+" consolidateOffset="+consolidateOffset);
//        }else{
//            int oldPosition = gPos.get(pos+consolidateOffset).currentpos;
//            System.out.println("key="+pos+" newPos="+newPos+" offset="+offset+" consolidateOffset="+consolidateOffset+" oldPosition="+oldPosition+" gPos.size="+gPos.size());
//            gPos.get(pos+consolidateOffset).currentpos = newPos;
//            int till=gPos.size()-consolidateOffset;
//            int count = 0;
//            for(int i=pos+1;i<till;i++){
//                if(gPos.get(i+consolidateOffset).currentpos==oldPosition){
//                    if(verbous)System.out.println("Das Element mit den key "+(pos+consolidateOffset)+" ist nun auf der Position "+newPos+" - consolidateOffset="+consolidateOffset);
//                    if(verbous)System.out.println("i="+i+ " & till="+(gPos.size()-consolidateOffset));
//                    gPos.get(i+consolidateOffset).currentpos = newPos;
//                    count++;
//                }
//            }
//            consolidateOffset+=count;
//        }
////        printKeyPositions();
//        System.out.println("-----------------------------------");
//    }

//    private void updateAndSyncPositionWhileImport(int pos, int newPos){
//        if(pos>gPos.size()){
//            System.out.println("FEHLER: Versuchter Zugriff auf das Element: "+pos+" bei einer Länger der gPos von "+gPos.size());
//            System.out.println("key="+pos+" newPos="+newPos);
//        }else{
//            int oldPosition = gPos.get(pos).currentpos;
//            int oldKey = gvV.get(gPos.get(pos).currentpos).key;
//            int till=gPos.size();
//            for(int i=pos+1;i<till;i++){
//
//                if(gvV.get(gPos.get(i).currentpos).key==oldPosition){
//                    gPos.get(i).currentpos = newPos;
//                    if(verbous) System.out.println("Dubletten werden ersetzt gPos["+i+"] mit der aktuellen Position gPos["+gPos.get(i).currentpos+"] den key="+gvV.get(gPos.get(i).currentpos).key+" ebenfalls als verticalKey haben");
//                }
//            }
//        }
//        printKeyPositions();
//        System.out.println("-----------------------------------");
//    }

    public void updatePositionKey(Vertikale v, int newPos){
//        for(int i=0; i<v.getURIs().size();i++){
//            String uri = v.getURIs().get(i);
//            Individual ci = m.getIndividual(uri);
//            Statement hv = ci.getRequiredProperty(m.getProperty(NSAS+"hasVertical"));
//            if(verbous) System.out.println("UPDATE POSITION bei "+uri+ " NEW POSITIONKEY = "+newPos+" ALTE POSITION:"+hv.getString());
//            hv.changeObject(String.valueOf(newPos));
//        }
    }

    public void updateFollowingPositionKeys(int from){
//        if(verbous) System.out.println("UPDATE FOLLOWING");
//        for(int i=from; i<gvV.size(); i++){
//            Vertikale v = gvV.get(i);
//            updatePositionKey(v, i);
//        }
    }
    /*
     * Diese Methode soll zur der globalen Verwaltung der Vertikalen
     * die Vertikalen des neu eingelesenen Dokumentes hinzufügen.
     * Foraussetzungen: 
     * 1. vV und vSG sind gleich lang und gleich geordnet
     * 2. die Elemente sind berreits vorsortiert! -> Reisschlussverfahren*
     */
    public void addNewVerticales(Vector<Vertikale> vVN, Vector<String>vVSN){
        if(gvV.size()==0&&gvVS.size()==0){
            System.out.println("OK neue Vertikalen");
            gvV.addAll(vVN);
            gvVS.addAll(vVSN);
        }else{

        //Improtiere die Abblildungstabelle pos->
//        int offset = gPos.size();
//        for(int z = 0;z<VLocalPositionManager.pv.size();z++){
//            gPos.add(VLocalPositionManager.pv.get(z));
//        }
        if(verbous || aBitVerbous){
            for(int t=0;t<vVSN.size();t++){
                System.out.print("V"+(t+1)+" KEY="+vVN.get(t).key+  ":"+vVSN.get(t));
            }
        }
//        printKeyPositions();
//        System.out.println("vVN Content:");
//        for(int p=0;p<vVN.size();p++){
//            Vertikale v = vVN.get(p);
//            System.out.print("V"+(p+1)+"(");
//            for(int r=0;r<v.getEbenen().size();r++){
//                System.out.print("E"+(r+1)+"(");
//                Ebene e = v.getEbenen().get(r);
//                for(int q=0;q<e.size();q++){
//                    Horizontale_Flyweight h = e.get(q);
//                    String pos = "";
//                    if(h.parentKey!=-1) pos = String.valueOf(h.parentKey);
//                    System.out.print("("+pos+h.type+h.childcount+")");
//                }
//                System.out.print(")");
//            }
//            System.out.print(")");
//            System.out.println("");
//        }
        boolean pfound = false;
        int lastj = 0;
        //Reisschlussverfahren
        for(int i=0;i<vVSN.size();){
            if(verbous||aBitVerbous) System.out.println("i="+i);
            pfound = false;
            String   vSN = vVSN.get(i);
            Vertikale vN = vVN.get(i);
            if(lastj==gvVS.size()){
                if(verbous) System.out.println(vVSN.get(i)+"wird am ende einfefügt bei i="+i+" und lastj="+lastj);
                updatePositionKey(vN, gvV.size());
                gvV.add(vN);
               gvVS.add(vSN);
               i++;
               lastj++;
            }else{
                for(int j=lastj;!pfound&&j<gvVS.size();){
                    String vSG = gvVS.get(j);
                    if(vSN.length()==vSG.length()){
                        if(vSN.equals(vSG)){
                            if(verbous) System.out.println("Gleichheit");
                            //Es wird kein neues Element hinzugefügt
                            //TODO Vermerken dass es eine Horizontale
                            //mehr gibt die auf diese Vertikale zeigt
//                            updateAndSyncPositionWhileImport(j+offset,j);
//                            updatePositionWhileImport(j+offset,j,offset);
                            updatePositionKey(gvV.get(j),j);
                            i++;
                            pfound=true;
                            lastj = j;
                        }else{
                            if(verbous) System.out.println("nur gleich Lang");
                            //Sortierreihenfolge von gross nach klein
                            //weshalb erst wieder eingefügt werden kann,
                            //wenn ein kleineres altes Element gesichtet wird
                            j++;
                            lastj = j;
                        }
                    }else{
                        if(vSN.length()>vSG.length()){
                            if(verbous) System.out.println("Grösser");
                            //Das neue ist grösser und kann an der Stelle j
                            //hinterlegt werden
//                            updateAndSyncPositionWhileImport(j+offset,j);
//                            updatePositionWhileImport(j+offset,j,offset);
//                            updatePositionKey(gvV.get(j),j);
                             gvV.add(j, vN);
                            gvVS.add(j, vSN);
                            updateFollowingPositionKeys(j);
                            i++;
                            pfound=true;
                            lastj = j + 1;
                        }else{
                            if(verbous) System.out.println("Kleiner");
                            //Sortierreihenfolge von gross nach klein
                            //weshalb erst wieder eingefügt werden kann,
                            //wenn ein kleineres altes Element gesichtet wird
                            j++;
                            lastj = j;
                        }
                    }
                }
            }
        }
    }
        }
}


/*
 * *Reisschlussverfahren:
 * ist der globale Vertikalen Vektor des Models sortiert und werden diesen
 * neue sortierte Vertikalen hinzugefügt, so reicht ein einziger durchlauf
 * aus um die beiden zu verschmälzen. an jeder Position muss geprüft werden
 * neueVertikale(j).size() >= globaleVertikale(j).size()
 * In einer äusseren Schleife können die neuen Vertikalen durchiteriert werden,
 * in einer inneren die globalen.
 *
 */