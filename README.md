# sim-search 

This is the system build as POC of my Diploma Thesis. 
The following menu provides functionality to import
arbitrary xml files to internal Ontology. Its outcome
is a presentation of known files within the Ontology
stored in target/classes/Ontology/abstractDatabase.owl 
annotated with similarity distance. 
Since the presentation script seam not to work properly
right now, please consult the abstractDatabase.owl 
it self for the moment.

    ----------------------------------------------
    * neue Datei verleichen (drag a file inside) *
    * oder ins Menü (m) Dateipfad/Menü=m/Ende=99 *
    ----------------------------------------------
    m
    ---------------------------------------------
    Bitte wählen Sie eine der folgenden NRn. aus: 
    ____________Linear Edit Dist (LED)___________
    01 (String) = Levenstein
    ________Lineare Werte Distanzen (LWD)________
    21 (String) = Needleman/Wunsch (GLAOBAL SA)
    ________________Test Suite___________________
    40 (String) = Long to Alphabet Code (base=26)
    41 (String) = String to valid URI #Fragment
    42 (String) = V2List Test
    45 (String) = Gebe alle Statemants des m aus
    ___________Ähnlichkeiten Suche_______________
    50 Baum-Ähnlichkeit oder Inhalt-Ähnlichkeit
    __________Threshold Konfiguration____________
    61 TR1 - Value Gate (maximale Abweichung)
    62 TR2 - Accuracy   (Genauigkeit)
    63 TR3 - Crossfade  (Überblender)
    __________OutPrint Konfiguration_____________
    70 Was soll ausgegeben werden!?
    _____________Beispieldatensätze______________
    810 = SimSearch - CD lite
    811 = SimSearch - CD full
    812 = SimSearch - Produktstrukturen   (57KB)
    813 = SimSearch - Produktstrukturen  (3,6MB)
    814 = SimSearch - Produktstrukturen (21,5MB)
    8110= SimSearch - Baumsignaturen-XPath
    8111= SimSearch - Mini  Example 1
    8112= SimSearch - Mini  Example 2
    8113= SimSearch - Mini  Example 3
    8115= SimSearch - Micro Example A
    8116= SimSearch - Micro Example B
    8117= SimSearch - Nano  Example A
    8118= SimSearch - Nano  Example B
    8119= SimSearch - XHTML Example
    _____________________________________________
    97  = STARTE TEST GUI PRIMITIVE DT. VERFAHREN
    98  = STARTE AJAX GUI SimSearch FRONT END
    99  = ENDE
    100 = MENUE VERLASSEN