/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.macilias.apps.similaritysearch.logic.algorithms.lexical;


/**
 *
 * @author maciekn
 */
public class Levenshtein extends MatrixBasedCompare {

    public static final boolean verbous = false;
    int[][] d;
    char[] str1;
    char[] str2;

    @Override
    public void printInterna(){
        System.out.println("Levenshtein:");
        if(d!=null&&str1!=null&&str2!=null){
           System.out.print(" ");
           for (int j = 0; j < str2.length; j++){
              System.out.print(str2[j]);
           }
           System.out.println("");
           for (int i = 0; i <d.length-1; ++i) {
              System.out.print(str1[i]);
              for (int j=0; j<d[i].length-1; ++j) {
                System.out.print(d[i][j]);
              }
              System.out.println();
            }
        }else{System.out.println("Levenstein: Variablen nicht initialisiert");}
    }

    @Override
    public int compare(String a, String b){
        d = new int[a.length() + 1][b.length() + 1];
        int i, j, cost;
        str1 = a.toCharArray();
        str2 = b.toCharArray();

        for (i = 0; i <= str1.length; i++){
            d[i][0] = i;
        }
        for (j = 0; j <= str2.length; j++){
            d[0][j] = j;
        }
        for (i = 1; i <= str1.length; i++){
            for (j = 1; j <= str2.length; j++){
               if (str1[i - 1] == str2[j - 1])
                   cost = 0;
               else
                   cost = 1;

               d[i][j] =
                   Math.min(d[i - 1][j] + 1,     // Löschen
                   Math.min(d[i][j - 1] + 1,     // Einfügen
                   d[i - 1][j - 1] + cost));     // Ersetzung

               if ((i > 1) && (j > 1) && (str1[i - 1] == str2[j - 2]) && (str1[i - 2] == str2[j - 1])){
                   d[i][j] = Math.min(d[i][j], d[i - 2][j - 2] + cost);
               }
           }
       }
       return d[str1.length][str2.length];
    }
    //Siegen, Germany(Nordrhein-Westfalen)
    //Ergebnis zwischen 0 und 100
    public static int compare(String a, String b, int tr2){
//        System.out.println("COMPARE "+a+" und "+b+ " beim tr2="+tr2);
        int[][] d = new int[a.length() + 1][b.length() + 1];
        int i, j, cost;
        char[] str1 = a.toCharArray();
        char[] str2 = b.toCharArray();

        for (i = 0; i <= str1.length; i++){
            d[i][0] = i;
        }
        for (j = 0; j <= str2.length; j++){
            d[0][j] = j;
        }
        for (i = 1; i <= str1.length; i++){
            for (j = 1; j <= str2.length; j++){
               if (str1[i - 1] == str2[j - 1])
                   cost = 0;
               else
                   cost = 1;

               d[i][j] =
                   Math.min(d[i - 1][j] + 1,     // Deletion
                   Math.min(d[i][j - 1] + 1,     // Insertion
                   d[i - 1][j - 1] + cost));     // Substitution

               if ((i > 1) && (j > 1) && (str1[i - 1] == str2[j - 2]) && (str1[i - 2] == str2[j - 1])){
                   d[i][j] = Math.min(d[i][j], d[i - 2][j - 2] + cost);
               }

               if(d[i][j]==tr2){
                   if(verbous) System.out.println("Leve - vorzeitiger Abbruch");
                   return 0;
               }
           }
       }
       int lev = d[str1.length][str2.length];
       lev = lev*100;
       int sim = ((tr2*100 - lev)/tr2);
//       System.out.println("RETURN= "+ sim);
       return sim;
    }

    @Override
    public int[][] getMatrix() {
        return d;
    }
}
