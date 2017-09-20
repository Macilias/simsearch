/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.macilias.apps.similaritysearch.util;

//import java.net.URI;
import java.util.BitSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.httpclient.util.URIUtil;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;

/**
 *
 * @author Maciej Niemczyk
 */
public final class URITool {

    public static String encode(String String_URL) {
        if (String_URL == null) {
            System.out.println("URI FEHLER: aus null wird null");
        }
        String encodedurl = null;
        //  try{
        try {
//            encodedurl = URIUtil.encode(String_URL, uric_no_slash);
            encodedurl = URIUtil.encode(String_URL, URI.allowed_fragment);
        } catch (URIException ex) {
            System.out.println("Ein Fehler ist bei der Gererierung der URI aufgetretten!");
            System.out.println(ex.toString());
            Logger.getLogger(URITool.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (encodedurl == null || encodedurl.equals("")) {
            System.out.println("URI FEHLER: Die URL " + String_URL + " konnte nicht escaped werden");
        }
        return encodedurl;
    }
    /*
     * Individual erhalten ihre URI's entweder als Ableitung aus der Klassenbeizeichnung (nicht Attribute)
     * oder als eine Transformation deren Wertes (Attribute)
     * XML setzt hierfür folgende Grenzen:
     * In einer XML-Datei lassen sich alle Zeichen des in ISO/IEC 10646 definierten Zeichenvorrats notieren.
     * Dieses Zeichensystem wurde 1993 von der International Organization for Standardization (ISO) entwickelt.
     * Es soll die Zeichen aller natürlichen und symbolischen Sprachen der Welt abdecken.
     * Seit der  Unicode-Version 1.1 entspricht ISO/IEC 10646 dem Unicode-System. Um genau zu sein:
     * Erlaubt sind Unicode-Zeichen mit den Hexadezimalwerten #x20 bis #xD7FF, #xE000 bis #xFFFD und #x10000 bis #x10FFFF.
     * Nicht erlaubt sind lediglich die beiden Zeichen mit den Hexadezimalwerten #xFFFE und #xFFFF,
     * da diese beiden keine Unicode-Zeichen darstellen.
     */
    protected static final BitSet digit = new BitSet(256);
    // Static initializer for digit

    static {
        for (int i = '0'; i <= '9'; i++) {
            digit.set(i);
        }
    }
    protected static final BitSet alpha = new BitSet(256);
    // Static initializer for alpha

    static {
        for (int i = 'a'; i <= 'z'; i++) {
            alpha.set(i);
        }
        for (int i = 'A'; i <= 'Z'; i++) {
            alpha.set(i);
        }
    }
    protected static final BitSet alphanum = new BitSet(256);
    // Static initializer for alphanum

    static {
        alphanum.or(alpha);
        alphanum.or(digit);
    }
    protected static final BitSet percent = new BitSet(256);
    // Static initializer for percent

    static {
        percent.set('%');
    }
    protected static final BitSet hex = new BitSet(256);
    // Static initializer for hex

    static {
        hex.or(digit);
        for (int i = 'a'; i <= 'f'; i++) {
            hex.set(i);
        }
        for (int i = 'A'; i <= 'F'; i++) {
            hex.set(i);
        }
    }
    protected static final BitSet mark = new BitSet(256);
    // Static initializer for mark

    static {
        mark.set('-');
        mark.set('_');
        mark.set('.');
        mark.set('!');
        mark.set('~');
        mark.set('*');
        mark.set('\'');
        mark.set('(');
        mark.set(')');
    }
    protected static final BitSet unreserved = new BitSet(256);
    // Static initializer for unreserved

    static {
        unreserved.or(alphanum);
        unreserved.or(mark);
    }
    protected static final BitSet reserved = new BitSet(256);
    // Static initializer for reserved

    static {
        reserved.set(';');
        reserved.set('/');
        reserved.set('?');
        reserved.set(':');
        reserved.set('@');
        reserved.set('&');
        reserved.set('=');
        reserved.set('+');
        reserved.set('$');
        reserved.set(',');
    }
    protected static final BitSet escaped = new BitSet(256);
    // Static initializer for escaped
    static {
        escaped.or(percent);
        escaped.or(hex);
    }
    protected static final BitSet reg_name = new BitSet(256);
    // Static initializer for reg_name

    static {
        reg_name.or(unreserved);
        reg_name.or(escaped);
        reg_name.set('$');
        reg_name.set(',');
        reg_name.set(';');
        reg_name.set(':');
        reg_name.set('@');
        reg_name.set('&');
        reg_name.set('=');
        reg_name.set('+');
    }
    protected static final BitSet uric_no_slash = new BitSet(256);
    // Static initializer for uric_no_slash

    static {
        uric_no_slash.or(unreserved);
        uric_no_slash.or(escaped);
//        uric_no_slash.set(';');
//        uric_no_slash.set('?');
//        uric_no_slash.set(';');
//        uric_no_slash.set('@');
//        uric_no_slash.set('&');
//        uric_no_slash.set('=');
//        uric_no_slash.set('+');
//        uric_no_slash.set('$');
//        uric_no_slash.set(',');
    }
}
