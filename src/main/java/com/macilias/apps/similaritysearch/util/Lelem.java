/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.macilias.apps.similaritysearch.util;

/**
 *
 * @author Maciej Niemczyk
 */
import java.io.*;
public class Lelem<E>{

    Lelem next = null;
    Lelem prev = null;
//    int key;
    E o;
    //Aufgabe oa;
    //KarteiKarte ok;
//    V2List<E> myList;

    /** Erstellt eine Lelem und lässt ihm sich am ende einfügen
     * @param o
     * @param k
     * @param myList 
     */


    // Erstellt ein Lelem und fügt ihm von aussen ein
    public Lelem(E o, Lelem prev, Lelem next){
        this.o = o;
        this.next = next;
        this.prev = prev;
//        myList.setCurrent(this);
    }

    public E getObject(){
        return this.o;
    }
    public Lelem getNext(){
        return this.next;
    }
    public void setNext(Lelem next){
        this.next = next;
    }
    public Lelem getPrev(){
        return this.prev;
    }
    public void setPrev(Lelem prev){
        this.prev = prev;
    }
    public boolean hasPrev(){
        if(this.prev!=null){
            return true;
        }else{
            return false;
        }
    }
//    public int getKey(){
//        return key;
//    }
}
