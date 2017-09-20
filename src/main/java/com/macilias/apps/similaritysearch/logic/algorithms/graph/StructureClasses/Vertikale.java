/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.macilias.apps.similaritysearch.logic.algorithms.graph.StructureClasses;

import java.util.Vector;
import com.macilias.apps.similaritysearch.logic.algorithms.graph.StructureManagementStaticClasses.VLocalPositionManager;

/**
 *
 * @author Maciej Niemczyk
 */
public class Vertikale{
    public int key = -1;
    Vector<Ebene> v = null;
    private Vector<String> uris = new Vector<String>();
    public Vertikale(){
        this.key = VLocalPositionManager.addNewElement();
    }
    public Vertikale(Vector<Ebene> v){
        this.key = VLocalPositionManager.addNewElement();
        this.v   = v;
    }
    public Vector<Ebene> getEbenen(){
        return this.v;
    }
    public void addIndividualURI(String URI){
        this.uris.add(URI);
    }
    public Vector<String> getURIs(){
        return this.uris;
    }
}
