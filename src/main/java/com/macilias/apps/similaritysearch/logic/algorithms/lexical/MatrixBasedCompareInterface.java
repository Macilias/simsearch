/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.macilias.apps.similaritysearch.logic.algorithms.lexical;


/**
 * @author maciekn
 * Abstrakte Schnittstelle für Alignmentensuche
 */
public abstract class MatrixBasedCompareInterface {
    String typ;
    public abstract int compare(String a, String b);
    public abstract void printInterna();
    public abstract int[][] getMatrix();
}
