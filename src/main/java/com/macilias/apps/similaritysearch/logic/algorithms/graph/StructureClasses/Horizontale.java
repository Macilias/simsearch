/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.macilias.apps.similaritysearch.logic.algorithms.graph.StructureClasses;

import com.macilias.apps.similaritysearch.logic.algorithms.graph.StructureManagementStaticClasses.*;
/**
 *
 * @author Maciej Niemczyk
 */
public class Horizontale extends Horizontale_Flyweight{
    //über parentKey und HPositionManager wird die die parentPosition bestimmt. 
    //Bei übrigen steht als ParentKey die Pos.
    public String uri;
    public int vIndex;
    /*Hierduch wird das 'Umbiegen' der ParentPos vermieden:
     * Wenn eine Horizontale angelegt wird, bekommt sie von PM einen
     * key zugewiesen, welcher ihrer dauerhaften Position innerhalb
     * der Zuordnungtabelle (Vectors<Position>) darstellt.
     * Als ParentPosition/key wird statt der Position innerhalb seiner
     * Ebene (=Vorebene) der key des Parents gespeichert.
     * Die aktuelle Position kann über den HPositionManager abgerufen
     * werden getCurrentPos(int key)
     */
    public Horizontale(){
        this.key = HPositionManager.addNewElement();
    }

    public Horizontale(String dummy){
    }

    public Horizontale(Horizontale source, int parent_position){
        this.parentKey = parent_position;
        this.type = source.type;
        this.childcount = source.childcount;
        this.uri = source.uri;
        this.vIndex = source.vIndex;
        this.key = source.key;
    }

    public Horizontale_Flyweight getFlyweight(){
        return new Horizontale_Flyweight(this);
    }
}