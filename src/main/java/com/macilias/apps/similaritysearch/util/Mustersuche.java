/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.macilias.apps.similaritysearch.util;

/**
 *
 * @author Maciej Niemczyk
 */
public class Mustersuche{

	public static String text = new String("efaaaaabaaacdef ");//auf der Mauer, auf der lauer, war abracadabra irgend so ein Zeuchs.
	public static String pattern = new String( "aaabaaac");

	static int n = text.length();
	static int m = pattern.length();
	static int i=0;
	static int j=0;


	public static int[] computeFailFunction(String pattern){
		int m = pattern.length();
		int[] fail = new int[m];
		int i=1, j=0;

		fail[0]=0;

		while (i < m){
			if (pattern.charAt(j) == pattern.charAt(i)){
				fail [i] = j + 1;
				j++;
				i++;
			}else{
				if (j > 0){
					j = fail[j-1];
				}else{
					i++;
				}
			}
		}

		for (int z=0; z< fail.length; z++){
		String y = String.valueOf(fail[z]);
		System.out.println("Fuer: "+pattern.charAt(z)+" Fail["+z+"]= "+y+", ");
		}
		return fail;
	}

	public static int KMPmach(String text, String pattern){
		 int fail[]=computeFailFunction(pattern);
		 int i=0, j=0;
		 int n = text.length(), m = pattern.length();

		while (i < n){
		    if(pattern.charAt(j) == text.charAt(i)){
		    	if (j == m-1){
		    		return i - m + 1;
		    	}
		    	i++;
		    	j++;
		    }else{
		    	if (j>0){
		    		j = fail[j-1];
		    	}else{
		    		i++;
		    	}
		    }
		  }
		  return -1;
	}


	public static int BrutForceMatch(String text, String pattern){

		while (i<n){
			if(pattern.charAt(j)==text.charAt(i+j)){
				if(j == m-1){
					return i;
				}
					j++;
				}
				else{
					i++;
					j=0;
				}
			}
			return -1;
		}

	}

		
