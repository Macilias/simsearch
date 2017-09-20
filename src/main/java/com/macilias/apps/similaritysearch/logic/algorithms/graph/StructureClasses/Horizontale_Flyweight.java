/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.macilias.apps.similaritysearch.logic.algorithms.graph.StructureClasses;

/**
 *
 * @author Maciej Niemczyk
 */
public class Horizontale_Flyweight {
    public int parentKey;
    public char type;
    public int childcount;
    public int key;
    //eventuell noch der Verweis auf die Vertikale um
    //in der Tiefe nach übereinstimmungen zu suchen.
    public Horizontale_Flyweight(Horizontale h){
        this.parentKey  = h.parentKey;
        this.type       = h.type;
        this.childcount = h.childcount;
        this.key        = h.key;
    }

    public Horizontale_Flyweight(Horizontale h, int parent_position){
        this.parentKey  = parent_position;
        this.type       = h.type;
        this.childcount = h.childcount;
        this.key        = h.key;
    }

    public Horizontale_Flyweight(){

    }
    //Es handelt sich hierbei nur um die gleicheit der in der String Representation
    //Vorhandenen Variablen, die URI sowie die Indizäs der Vertikalten und Horizontalen
    //bleiben hierbei aussen vor -> dient der verlusstbehafteten Kopression
    public boolean equals(Horizontale_Flyweight z){
        if(this.parentKey==z.parentKey&&this.type==z.type&&this.childcount==z.childcount){
            return true;
        }else{
            return false;
        }
    }

    public void setParentKey(int key){
        this.key = key;
    }

    public int getParentKey(){
        return this.parentKey;
    }
}
