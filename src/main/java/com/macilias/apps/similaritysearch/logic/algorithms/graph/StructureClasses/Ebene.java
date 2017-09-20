/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.macilias.apps.similaritysearch.logic.algorithms.graph.StructureClasses;

import com.macilias.apps.similaritysearch.logic.algorithms.graph.StructureManagementStaticClasses.*;
import java.util.Vector;

/**
 *
 * @author Maciej Niemczyk
 */
public class Ebene{
    public Vector<Horizontale_Flyweight> e;
    public int maxCCount = 0;
    /*
     * This one adds a Horizontal to Vector of Horizontals
     * and updates the maximal count of children count.
     * Further it updates the key in the HPositionManager,
     * for making it easy to sort the global verical Structure.
     * Because sorting is already done global, it is not needet
     * by derivated Vericales generated
     */
    public void addAndUpdate(Horizontale_Flyweight h){
        if(h.childcount!=0){
            if(h.childcount>maxCCount) maxCCount = h.childcount;
        }
        HPositionManager.updatePosition(h.key, e.size());
        e.add(h);
    }

    public void add(Horizontale_Flyweight h){
//            if(h.childcount!=0){
//                if(h.childcount>maxCCount) maxCCount = h.childcount;
//            }
        e.add(h);
    }

    public void updateMaxCCount(int childcount){
        if(childcount!=0){
            if(childcount>maxCCount) maxCCount = childcount;
        }
    }
    public Ebene(){
        e = new Vector<Horizontale_Flyweight>();
    }
    //TODO nachschauen, ob ein e.maxCCount nicht langt
    public Ebene(Vector<Horizontale_Flyweight>e, int maxCCount){
        this.e = e;
        this.maxCCount = maxCCount;
    }

    public int size(){
        return e.size();
    }

    public Horizontale_Flyweight get(int i){
        return e.get(i);
    }
    //PS* Die Maximale Position des Parents kann auch als die Länge der
    //vorherigen Ebene ausgelesen werden, wodurch sämtliche
    //Vergleiche ersparrt werden
}
