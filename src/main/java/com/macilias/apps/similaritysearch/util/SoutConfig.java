/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.macilias.apps.similaritysearch.util;

import java.io.BufferedOutputStream;

/**
 *
 * @author Maciej Niemczyk
 */
public class SoutConfig {
    //globale Einstellungen
    public static boolean calculation = false;
    public static boolean aBitVerbous = false;
    public static boolean verbous = false;
    public static boolean khadija = false;

    //lokale Einstellungen
    //XML2OWL (Parser)
    public static short calculation_X2O = -1;
    public static short aBitVerbous_X2O = -1;
    public static short verbous_X2O = -1;
    public static short khadija_X2O = -1;
    public static short printOutXML = -1;

    //StructureManagement (Vertikalen generation/Management)
    public static short aBitVerbous_SM = -1;
    public static short verbous_SM = -1;
//    public static short calculation_SM = 1;
//    public static short khadija_SM = 1;

    //TreeSimilarity & ContentSimilarity
    public static short calculation_TS = -1;
    public static short aBitVerbous_TS = -1;
    public static short verbous_TS = -1;
    public static short khadija_TS = -1;
    
    //ResultReciver
    public static short calculation_RR = -1;
    public static short aBitVerbous_RR = -1;
    public static short verbous_RR = -1;
    public static short khadija_RR = -1;

    public static void setaBitVerbous(boolean aBitVerbous) {
        SoutConfig.aBitVerbous = aBitVerbous;
    }

    public static void setaBitVerbous_SM(short aBitVerbous_SM) {
        SoutConfig.aBitVerbous_SM = aBitVerbous_SM;
    }

    public static void setaBitVerbous_TS(short aBitVerbous_TS) {
        SoutConfig.aBitVerbous_TS = aBitVerbous_TS;
    }

    public static void setaBitVerbous_X2O(short aBitVerbous_X2O) {
        SoutConfig.aBitVerbous_X2O = aBitVerbous_X2O;
    }

    public static void setaBitVerbous_RR(short aBitVerbous_RR) {
        SoutConfig.aBitVerbous_RR = aBitVerbous_RR;
    }

    public static void setCalculation(boolean calculation) {
        SoutConfig.calculation = calculation;
    }

    public static void setCalculation_TS(short calculation_TS) {
        SoutConfig.calculation_TS = calculation_TS;
    }

    public static void setCalculation_X2O(short calculation_X2O) {
        SoutConfig.calculation_X2O = calculation_X2O;
    }

    public static void setCalculation_RR(short calculation_RR) {
        SoutConfig.calculation_RR = calculation_RR;
    }

    public static void setKhadija(boolean khadija) {
        SoutConfig.khadija = khadija;
    }

    public static void setKhadija_TS(short khadija_TS) {
        SoutConfig.khadija_TS = khadija_TS;
    }

    public static void setKhadija_X2O(short khadija_X2O) {
        SoutConfig.khadija_X2O = khadija_X2O;
    }

    public static void setKhadija_RR(short khadija_RR) {
        SoutConfig.khadija_RR = khadija_RR;
    }

    public static void setPrintOutXML(short printOutXML) {
        SoutConfig.printOutXML = printOutXML;
    }

    public static void setVerbous(boolean verbous) {
        SoutConfig.verbous = verbous;
    }

    public static void setVerbous_SM(short verbous_SM) {
        SoutConfig.verbous_SM = verbous_SM;
    }

    public static void setVerbous_TS(short verbous_TS) {
        SoutConfig.verbous_TS = verbous_TS;
    }

    public static void setVerbous_X2O(short verbous_X2O) {
        SoutConfig.verbous_X2O = verbous_X2O;
    }

    public static void setVerbous_RR(short verbous_RR) {
        SoutConfig.verbous_RR = verbous_RR;
    }


    public static boolean getPrintOutXML() {
        boolean ret = calculation;
        if(printOutXML==0){
            ret = false;
        }else{
            if(printOutXML==1) ret = true;
        }
        return ret;
    }

    public static boolean getTSQuietMode(){
        return !(SoutConfig.getCalculation_TS()||
                 SoutConfig.getVerbous_TS()||
                 SoutConfig.getaBitVerbous_TS()||
                 SoutConfig.getKhadija_TS());
    }
    
    public static boolean getRRQuietMode(){
        return !(SoutConfig.getCalculation_RR()||
                 SoutConfig.getVerbous_RR()||
                 SoutConfig.getaBitVerbous_RR()||
                 SoutConfig.getKhadija_RR());
    }

    public static boolean getaBitVerbous_SM() {
        boolean ret = aBitVerbous;
        if(aBitVerbous_SM==0){
            ret = false;
        }else{
            if(aBitVerbous_SM==1) ret = true;
        }
        return ret;
    }

    public static boolean getaBitVerbous_TS() {
        boolean ret = aBitVerbous;
        if(aBitVerbous_TS==0){
            ret = false;
        }else{
            if(aBitVerbous_TS==1) ret = true;
        }
        return ret;
    }

    public static boolean getaBitVerbous_X2O() {
        boolean ret = aBitVerbous;
        if(aBitVerbous_X2O==0){
            ret = false;
        }else{
            if(aBitVerbous_X2O==1) ret = true;
        }
        return ret;
    }

    public static boolean getaBitVerbous_RR() {
        boolean ret = aBitVerbous;
        if(aBitVerbous_RR==0){
            ret = false;
        }else{
            if(aBitVerbous_RR==1) ret = true;
        }
        return ret;
    }

//    public static boolean getCalculation_SM() {
//        boolean ret = calculation;
//        if(calculation_SM==0){
//            ret = false;
//        }else{
//            if(calculation_SM==1) ret = true;
//        }
//        return ret;
//    }

    public static boolean getCalculation_TS() {
        boolean ret = calculation;
        if(calculation_TS==0){
            ret = false;
        }else{
            if(calculation_TS==1) ret = true;
        }
        return ret;
    }

    public static boolean getCalculation_X2O() {
        boolean ret = calculation;
        if(calculation_X2O==0){
            ret = false;
        }else{
            if(calculation_X2O==1) ret = true;
        }
        return ret;
    }

    public static boolean getCalculation_RR() {
        boolean ret = calculation;
        if(calculation_RR==0){
            ret = false;
        }else{
            if(calculation_RR==1) ret = true;
        }
        return ret;
    }

//    public static boolean getKhadija_SM() {
//        boolean ret = khadija;
//        if(khadija_SM==0){
//            ret = false;
//        }else{
//            if(khadija_SM==1) ret = true;
//        }
//        return ret;
//    }

    public static boolean getKhadija_TS() {
        boolean ret = khadija;
        if(khadija_TS==0){
            ret = false;
        }else{
            if(khadija_TS==1) ret = true;
        }
        return ret;
    }

    public static boolean getKhadija_X2O() {
        boolean ret = khadija;
        if(khadija_X2O==0){
            ret = false;
        }else{
            if(khadija_X2O==1) ret = true;
        }
        return ret;
    }

    public static boolean getKhadija_RR() {
        boolean ret = khadija;
        if(khadija_RR==0){
            ret = false;
        }else{
            if(khadija_RR==1) ret = true;
        }
        return ret;
    }

    public static boolean getVerbous_SM() {
        boolean ret = verbous;
        if(verbous_SM==0){
            ret = false;
        }else{
            if(verbous_SM==1) ret = true;
        }
        return ret;
    }

    public static boolean getVerbous_TS() {
        boolean ret = verbous;
        if(verbous_TS==0){
            ret = false;
        }else{
            if(verbous_TS==1) ret = true;
        }
        return ret;
    }

    public static boolean getVerbous_X2O() {
        boolean ret = verbous;
        if(verbous_X2O==0){
            ret = false;
        }else{
            if(verbous_X2O==1) ret = true;
        }
        return ret;
    }

    public static boolean getVerbous_RR() {
        boolean ret = verbous;
        if(verbous_RR==0){
            ret = false;
        }else{
            if(verbous_RR==1) ret = true;
        }
        return ret;
    }


}
