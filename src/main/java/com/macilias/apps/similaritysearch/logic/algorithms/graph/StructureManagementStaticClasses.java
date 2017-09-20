/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.macilias.apps.similaritysearch.logic.algorithms.graph;

import com.macilias.apps.similaritysearch.logic.algorithms.graph.StructureClasses.*;
import java.util.Vector;

/**
 *
 * @author Maciej Niemczyk
 */
public class StructureManagementStaticClasses {

    //Positions Manager der Horizontalen
    /*
     * Diese Funktion wird nur beim Einlesen benutzt
     */
    public static class HPositionManager{
        //Position Parent of Horizontal:
        static Vector<Position> pp = new Vector<Position>();

        public static int getCurrentPos(int key){
            return pp.get(key).currentpos;
        }

        /*
         * Fügt ein neues Element hinzu
         */
        public static int addNewElement(){
            int key = pp.size();
//            System.out.println("pp.size()->"+key);
            Position p = new Position();
            pp.add(p);
            return key;
        }

        public static void updatePosition(int key, int newPos){
            pp.get(key).currentpos = newPos;
        }

    }

    //Positions Manager der Vertikalen
    public static class VLocalPositionManager{
        //Position Vertikale:
        public static Vector<Position> pv = new Vector<Position>();
        private static int offset=0;

        public static int getCurrentPos(int key){
            return pv.get(key-offset).currentpos-offset;
        }

        public static int addNewElement(){
            int key = pv.size()+offset;
            Position p = new Position();
            //TODO wieso wird hier nicht die Position gesetzt?
            pv.add(p);
            return key;
        }

        public static void updatePosition(int key, int newPos){
            pv.get(key).currentpos = newPos+offset;
        }

        //Die String vertikale wird hierdurch auch hinzugefügt
        /*
         * Diese Funktion wird nur beim Einlesen benutzt
         */
        public static int sortInSCVertikale(Vector<String> vertikaleSC, String vSN){
            for(int i=0;i<vertikaleSC.size();i++){
                String vS = vertikaleSC.get(i);
                //VORSORTIERUNG NACH LÄNGE
                if(vSN.length()==vS.length()){
                    if(vSN.equals(vS)){
                        //Die Position innerhalb des Positionsvectors update
                        updatePosition(pv.size()-1,(i));
                        // <0 bedeutet nicht Einfügen, gleicher vorhanden
                        // i wird in Folge für das/die Individuen ausgegeben
                        // von den der Key bezogen werden kann
                        return -i;
                    }
                    //Keine Sorge, gleich lange Elemente werden
                    //richtig Positioniert-Hier wird nur eine einsortiert!
                    //D.h gehe solange weiter bis du einen findest der kleiner ist
                    //sonst wird das Element ausserhalb der schleife eingefügt
                }
                else{
                    if(vSN.length()>vS.length()){
                        vertikaleSC.add(i, vSN);
                        updatePosition(pv.size()-1,(i));
                        return i;
                    }
                }
            }
            vertikaleSC.add(vSN);
            updatePosition(pv.size()-1,vertikaleSC.size()-1);
            return vertikaleSC.size()-1;
        }

        public static void printKeyPositions(){
            String keys="\nVPMKEY:";
            for(int i=0;i<pv.size();i++){
                keys+="["+(i)+"="+pv.get(i).currentpos+"]";
            }
            System.out.println(keys);
        }

        public static void reset(int off){
            pv = new Vector<Position>();
            offset = off;
        }
    }
}
