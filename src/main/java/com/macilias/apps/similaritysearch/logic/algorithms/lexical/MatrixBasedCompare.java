/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.macilias.apps.similaritysearch.logic.algorithms.lexical;

/**
 * @author maciekn
 * Diese Klasse enthält Algorithmen zur
 * Suche nach Überscheinstimmungen in Strings
 * SA: Sequenzalignment bzw. String Alignment
 */
public class MatrixBasedCompare extends MatrixBasedCompareInterface{
    //int aktueller = 0;
    MatrixBasedCompareInterface aktueller;
    public MatrixBasedCompare(){
    }

    @Override
    public int compare(String a, String b) {
        return aktueller.compare(a, b);
    }

    @Override
    public void printInterna() {
        aktueller.printInterna();
    }

    public void setAlgo(int key){
        aktueller = getAliComp(key);
        System.out.println("Gewählt worde: "+aktueller.toString());
    }

    private MatrixBasedCompareInterface getAliComp(int key){
        if(key==1) return new Levenshtein();
        if(key==21) return new Needleman_Wunsch();
        return null;
    }

    @Override
    public int[][] getMatrix() {
        return aktueller.getMatrix();
    }
}
