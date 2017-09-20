/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.macilias.apps.similaritysearch.logic.algorithms.lexical;

/**
 *
 * @author maciekn
 */
public class Needleman_Wunsch extends MatrixBasedCompare {

    int[][] d;
    char[] str1;
    char[] str2;

    @Override
    public void printInterna(){
        System.out.println("Needleman/Wunsch:");
        if(d!=null&&str1!=null&&str2!=null){
           System.out.print("  ");
           for (int j = 0; j < str2.length; j++){
              System.out.print(str2[j]);
           }
           System.out.println("");
           for (int i = 0; i <d.length; ++i) {
              if(i==0)
                  System.out.print(" ");
              else
                System.out.print(str1[i-1]);  
                            
              for (int j=0; j<d[i].length; ++j) {
                System.out.print(d[i][j]);
              }
              System.out.println();
            }
        }else{System.out.println("Variablen nicht initialisiert");}
    }

    @Override
    public int compare(String a, String b){
        d = new int[a.length() + 1][b.length() + 1];
        int i, j, score, gapcost1, gapcost2;
        str1 = a.toCharArray();
        str2 = b.toCharArray();
        for (i = 1; i <= str1.length; i++){
            for (j = 1; j <= str2.length; j++){
               if (str1[i - 1] == str2[j - 1])
                   score = 2;
               else
                   score = -1;
               if (Character.isWhitespace(str1[i-1]))
                   gapcost1 = 0;
               else
                   gapcost1 = -2;
               if (Character.isWhitespace(str2[j-1]))
                   gapcost2 = 0;
               else
                   gapcost2 = -2;

               d[i][j] =
                   Math.max(d[i - 1][j - 1] + score, // Mach/Mismatch in diagonal
                   Math.max(d[i][j - 1] + gapcost1,  // gap in str.1
                   d[i - 1][j] + gapcost2));         // gap in str.2
                   System.out.println("------------i="+i+ "j="+j+"------------");
                   System.out.println("Mach/Mismatch in diagonal: "+ d[i - 1][j - 1] + score);
                   System.out.println("gap in str1: "+ d[i][j - 1] + gapcost1);
                   System.out.println("gap in str2: "+ d[i - 1][j] + gapcost2);
                   System.out.println("MAX="+d[i][j]);
            }
       }
       return d[str1.length][str2.length];
    }   
    
    public int compareSimple(String a, String b){
        d = new int[a.length() + 1][b.length() + 1];
        int i, j, score, gapcost1, gapcost2;
        str1 = a.toCharArray();
        str2 = b.toCharArray();
        for (i = 1; i <= str1.length; i++){
            for (j = 1; j <= str2.length; j++){
               if (Character.isWhitespace(str1[i-1]))
                   gapcost1 = 1;
               else
                   gapcost1 = 0;
               if (Character.isWhitespace(str2[j-1]))
                   gapcost2 = 1;
               else
                   gapcost2 = 0;
               if (str1[i - 1] == str2[j - 1])
                   score = 1;
               else
                   score = 0;

               d[i][j] =
                   Math.max(d[i - 1][j - 1] + score, // Mach/Mismatch in diagonal
                   Math.max(d[i][j - 1] + gapcost1,  // gap in str.1
                   d[i - 1][j] + gapcost2));         // gap in str.2
                   System.out.println("------------i="+i+ "j="+j+"------------");
                   System.out.println("Mach/Mismatch in diagonal: "+ d[i - 1][j - 1] + score);
                   System.out.println("gap in str1: "+ d[i][j - 1] + gapcost1);
                   System.out.println("gap in str2: "+ d[i - 1][j] + gapcost2);
                   System.out.println("MAX="+d[i][j]);
            }
       }
       return d[str1.length][str2.length];
    }

    @Override
    public int[][] getMatrix() {
        return d;
    }
}
