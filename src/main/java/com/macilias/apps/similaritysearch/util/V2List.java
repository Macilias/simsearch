/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.macilias.apps.similaritysearch.util;

import java.util.Collection;
import java.util.Iterator;


/**
 *
 * @author Maciej Niemczyk
 */
public class V2List<E> implements Collection<E>, Iterable<E>{


    Lelem<E> head = null;
    Lelem<E> tail = null;
    public Lelem<E> current = null;
    private Lelem<E> remembered = null;
    protected Lelem<E> remembered_while_iteration = null;
    int lelemCounter = 0;
    int findposition = 0;

    /** Creates a new instance of Liste */
    public V2List() {
    }


    private int doKey(){
        return lelemCounter;
    }
    public int getFindPos(){
        return this.findposition;
    }
    public Lelem<E> getHead(){
        return this.head;
    }
    private void setHead(Lelem<E> l){
        this.head = l;
    }
    public Lelem<E> getTail(){
        return tail;
    }
    private void setTail(Lelem<E> t){
        this.tail = t;
    }
    public Lelem<E> getCurrent(){
        return this.current;
    }
    public void setCurrent(Lelem<E> c){
        this.current = c;
    }
    private void einfuegen(E o){
//        System.out.println("V2 Einfügen");
        if(this.getHead()!=null){
            this.lelemCounter++;
            Lelem nuev = new Lelem(o, this.getTail(),null);
            this.getTail().setNext(nuev);
            this.setCurrent(nuev);
            this.setTail(nuev);
        }else{
            this.lelemCounter++;
            Lelem nuev = new Lelem(o,null,null);
            this.setHead(nuev);
            this.setTail(nuev);
            this.setCurrent(nuev);
        }
    }
    private void einfuegen(Lelem o){
//        System.out.println("V2 Einfügen Lelem");
        if(this.getHead()!=null){
            this.lelemCounter++;
            o.setPrev(this.getTail());
            this.getTail().setNext(o);
            this.setCurrent(o);
            this.setTail(o);
        }else{
            this.lelemCounter++;
            Lelem nuev = new Lelem(o,null,null);
            o.setPrev(null);
            o.setNext(null);
            this.setHead(o);
            this.setTail(o);
            this.setCurrent(o);
        }
    }
    protected Lelem<E> currentLoeschen(){
       if(this.getHead() == null){
            System.out.println("Fehler, die Liste hat keine Elemente!");
            return null;
       }else{
            if(this.getCurrent() == null){
                System.out.println("Fehler, die Liste hat kein current Element!");
                return null;
            }else{
                if(this.getCurrent() == this.getHead()){
                    //Nur ein Listenelement (Kopf und Schwanz gleichzietig):
                    if (this.getCurrent() == this.getTail()){
                        this.setHead(null);
                        this.setTail(null);
                        this.lelemCounter = 0;
                        this.setCurrent(null);
                        return null;
                    }
                    //Liste hat mehrere Elemente und es ist der Kopf:
                    else{
                        Lelem ret = this.getCurrent();
                        this.setHead(this.getCurrent().getNext());
                        this.getHead().prev = null;
                        this.lelemCounter--;
                        this.setCurrent(this.getHead());
                        return ret;
                    }
                }
                else{
                     //Liste hat mehrere Elemente und es ist nicht der Kopf und nicht der Schwanz:
                     if(this.getCurrent() != this.getTail()){
                        Lelem ret = this.getCurrent();
                        this.getCurrent().getPrev().setNext(this.getCurrent().getNext());
                        this.getCurrent().getNext().setPrev(this.getCurrent().getPrev());
                        this.lelemCounter --;
                        Lelem nextTemp = this.getCurrent().getNext();
                        this.setCurrent(null);
                        this.setCurrent(nextTemp);
                        return ret;
                     }
                     //Liste hat mehrere Elemente und es ist der Schwanz:
                     else{
                        Lelem ret = this.getCurrent();
                        this.getCurrent().getPrev().setNext(null);
                        this.setTail(this.getCurrent().getPrev());
                        this.setCurrent(this.getTail());
                        this.lelemCounter --;
                        return ret;
                    }
                }
            }
        }
    }

    private void loeschErsten(){
        if (this.getHead() == null){
            if(SoutConfig.getVerbous_RR()) System.out.println("Fehler, die Liste hat keine Elemente!");
        }
        this.setCurrent(this.getHead());
            //Nur ein Listenelement:
            if (this.getCurrent() == getTail()){
                this.setHead(null);
                this.setTail(null);
                this.lelemCounter = 0;
                this.setCurrent(null);
            }
            //Liste hat mehrere Elemente und es ist der Kopf:
            else{
                this.setHead(this.getCurrent().getNext());
                this.getHead().prev = null;
                this.lelemCounter--;
                this.setCurrent(this.getHead());
            }
    }

    public Lelem auslesenVor(){
        if (this.getHead() == null){
            System.out.println("Fehler: Liste leer!");
            return null;
        }
        else{
             if(this.getCurrent() == null){
                this.setCurrent(this.getHead());
                if(SoutConfig.getVerbous_RR()) System.out.println("kein current");
                return null;
             }
             else{
                 if(this.getCurrent().equals(this.getHead())){
                     this.setCurrent(this.getCurrent().getNext());
                     return this.getHead();
                 }
                 //Normalfall: Liste hat Head und ein Currentelement
                 if(this.getCurrent()!= this.getTail()){
                     if(SoutConfig.getVerbous_RR()) System.out.println("AuslesenVor()");
                     Lelem curr = this.getCurrent();
                     this.setCurrent(this.getCurrent().getNext());
                     return curr;
                 }else{
                     this.setCurrent(null);
                     return this.getTail();
                 }
             }
         }
     }


    public Lelem auslesenRueck(){
        if (this.getTail() == null){
            System.out.println("Fehler: Liste leer!");
            return null;
        }
        else{
             if(this.getCurrent() == null){
                this.setCurrent(this.getTail());
                if(SoutConfig.getVerbous_RR()) System.out.println("kein current");
                return null;
             }
             else{
                 if(this.getCurrent().equals(this.getTail())){
                     this.setCurrent(this.getCurrent().getPrev());
                     return this.getTail();
                 }
                 //Normalfall: Liste hat Head und ein Currentelement
                 if(this.getCurrent()!= this.getHead()){
                     if(SoutConfig.getVerbous_RR()) System.out.println("AuslesenRueck()");
                     Lelem curr = this.getCurrent();
                     this.setCurrent(this.getCurrent().getPrev());
                     return curr;
                 }else{
                     this.setCurrent(null);
                     return this.getHead();
                 }
             }
         }
    }

    public Lelem auslesenZiel(int k){
        int pos = 0;
        if(this.getHead()==null || k>=this.size()){
            return null;
        }
        this.current = this.getHead();
//        System.out.println("Err2");
        do  {
            if(k == pos){
                return this.getCurrent();
            }else{
                this.auslesenVor();
                pos++;
            }
        }
        while (pos<this.size());
        return null;
    }

    public Lelem sucheBody(String s, boolean erstsuche){
        if(this.getHead() == null){
            System.out.println("Err1");
            return null;
        }
        Lelem temp = this.getCurrent();
        if(erstsuche){
             this.current = this.getHead();
        }
        do  {
            findposition = Mustersuche.KMPmach(this.getCurrent().o.toString(),s);
            if(findposition >=0){
                System.out.println("Gesucht : "+s+" _Gefunden :"+this.getCurrent().o.toString());
                return this.getCurrent();
            }
            else{
                this.setCurrent(this.auslesenVor());
                System.out.println("nicht gefunden "+s);
            }
        }
        while (this.current.getNext()!=null);
        //War nichts da also gehe zum Ausgangszustand
        this.setCurrent(temp);
        return null;
    }

    public Lelem sucheHead(String s, boolean erstsuche){
        if(this.getHead() == null){
            System.out.println("Err1");
            return null;
        }
        Lelem temp = this.getCurrent();
        if(erstsuche){
             this.current = this.getHead();
        }
        do  {

            findposition = Mustersuche.KMPmach(this.getCurrent().o.toString() ,s);
            if(findposition>=0){
                if(SoutConfig.getCalculation_RR()) System.out.println("Gesucht : "+s+" _Gefunden :"+this.getCurrent().o.toString());
                return this.getCurrent();
            }
            else{
                this.setCurrent(this.auslesenVor());
                if(SoutConfig.getCalculation_RR()) System.out.println("nicht gefunden "+s);
            }

        }
        while (this.current.getNext()!=null);
        //War nichts da also gehe zum Ausgangszustand
        this.setCurrent(temp);
        findposition = 0;
        return null;
    }

    public int getAnz(){
        if(SoutConfig.getCalculation_RR() && SoutConfig.getKhadija_RR()) System.out.println("lelemcounter = "+ this.lelemCounter);
        return this.lelemCounter;
    }

    @Override
    public int size() {
        return this.getAnz();
    }

    @Override
    public boolean isEmpty() {
        if(this.size()==0){
            return true;
        }else{
            return false;
        }
    }

    @Override
    public boolean contains(Object o) {
        for(Iterator<E> it = this.iterator(); it.hasNext(); ){
            if(o.equals(it.next())){
                return true;
            }
        }
        return false;
    }

    @Override
    public Iterator<E> iterator() {
        return new V2Iterator<E>(this);
    }
 
    public Iterator<E> descendingIterator() {
        return new DescendingV2Iterator<E>(this);
    }

    @Override //Nützt Nix
    public Object[] toArray() {
        throw new UnsupportedOperationException("Not supported yet. - Nützt Nix");
    }

    @Override //Nützt Nix
    public Object[] toArray(Object[] ts) {
        throw new UnsupportedOperationException("Not supported yet. - Nützt Nix");
    }

    @Override
    public boolean add(E e) {
        this.einfuegen(e);
        //Einfügen in einer V2 Liste geht immer ;)
        return true;
    }

    private void add(Lelem l) {
        this.einfuegen(l);
    }

    public boolean add(E e,int pos) {
        int i = 0;
        Lelem curr = this.getHead();
        if(pos<this.size()){
            do{
                if(i==pos){
                    Lelem nuev;
                    if(!curr.hasPrev()){
                        //dann auch i=0 und l=Head
                        Lelem oldhead = this.getHead();
                        nuev = new Lelem(e,null,oldhead);
                        this.setHead(nuev);
                        oldhead.setPrev(nuev);
                        this.lelemCounter++;
                        return true;
                    }else{
                        //Ein Zwischen Element oder Tail
                        //1. Zwischen Tail wird die Position des bisherigen Tails haben
                        //   ohne selbst zum Tail zu werden.
                        //2. Ein Tail liegt dann vor wenn i == list.size()!
                        //   dann wird ausserhalb der Schleife mit add(e) angehängt
                        Lelem prev = curr.getPrev();
                        nuev = new Lelem(e,prev,curr);
                        prev.setNext(nuev);
                        curr.setPrev(nuev);
                        this.lelemCounter++;
                        return true;
                    }
                }
                i++;
                curr = curr.getNext();
            }while(i<this.size());
        }else{
            this.add(e);
            return true;
        }
        return false;
    }

    private boolean add(Lelem move, int pos){
        int i = 0;
        Lelem curr = this.getHead();
        if(pos<this.size()){
            do{
                if(i==pos){
                    if(!curr.hasPrev()){
                        //Head - dann auch i=0 und curr=Head
                        move.setNext(curr);
                        move.setPrev(null);
                        this.setHead(move);
                        curr.setPrev(move);
                        this.setCurrent(move);
                        this.lelemCounter++;
                        return true;
                    }else{
                        //Ein zwischen Element
                        Lelem prev = curr.getPrev();
                        move.setPrev(prev);
                        move.setNext(curr);
                        curr.setPrev(move);
                        prev.setNext(move);
                        this.setCurrent(move);
                        this.lelemCounter++;
                        return true;
                    }
                }
                i++;
                curr = curr.getNext();
            }   while(i<this.size());
        }else{
            this.add(move);
            return true;
        }
        return false;
    }

    @Override
    public boolean remove(Object o) {
        for(Iterator<E> it = this.iterator(); it.hasNext(); ){
            if(o.equals(it.next())){
                it.remove();
                return true;
            }
        }
        return false;
    }

    public boolean remove(int pos) {
        if(auslesenZiel(pos)!=null){
            this.currentLoeschen();
            return true;
        }
        return false;
    }

    public boolean moveRememberedTo(int pos){
        Lelem rem = this.remembered;
        if(rem!=null){
            this.setCurrent(rem);
            this.currentLoeschen();
            this.add(rem, pos);
            this.remembered = null;
            return true;
        }else{
            return false;
        }
    }

    public boolean updateRememberedTo(E o, int pos){
        Lelem rem = this.remembered;
        if(rem!=null){
            this.setCurrent(rem);
            this.currentLoeschen();
            this.add(o, pos);
            this.remembered = null;
            return true;
        }else{
            return false;
        }
    }

    public boolean moveRememberedEnd(){
        Lelem rem = this.remembered;
        if(rem!=null){
            this.setCurrent(rem);
            this.currentLoeschen();
            this.add(rem);
            this.remembered = null;
            return true;
        }else{
            return false;
        }
    }
    
    public boolean updateRememberedEnd(E o){
        Lelem rem = this.remembered;
        if(rem!=null){
            this.setCurrent(rem);
            this.currentLoeschen();
            this.add(o);
            this.remembered = null;
            return true;
        }else{
            return false;
        }
    }

    public void rememberCurrent(){
        if(this.remembered_while_iteration==null){
            this.remembered = this.getCurrent();
        }else{
            this.remembered = this.remembered_while_iteration;
        }
    }

    public void rememberCurrentWhileIteration(){
        this.remembered_while_iteration = this.getCurrent();
    }


    @Override //Nützt Nix
    public boolean containsAll(Collection clctn) {
        throw new UnsupportedOperationException("Not supported yet. - Nützt Nix");
    }

    @Override 
    public boolean addAll(Collection clctn) {
        for(Iterator it = clctn.iterator(); it.hasNext(); ){
            this.add((E)it.next());
        }
        return true;
    }

    @Override //Nützt Nix
    public boolean removeAll(Collection clctn) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override //Nützt Nix
    public boolean retainAll(Collection clctn) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override //Nützt Nix
    public void clear() {
        throw new UnsupportedOperationException("Not supported yet. - Nützt, Nix setzt es doch gleich null!");
    }

    public void printOut(){
        for(Iterator<E> it = this.iterator(); it.hasNext(); ){
            E e = it.next();
            System.out.print(e.toString()+", ");
        }
        System.out.print("\n");
    }

}

class V2Iterator<E> implements Iterator<E>{

    V2List<E> l;

    public V2Iterator(V2List<E> l){
        this.l = l;
        this.l.setCurrent(this.l.getHead());
    }

    @Override
    public boolean hasNext() {
        if(l.getCurrent()!=null){
            if(l.getCurrent().equals(l.getHead())) return true;
            if(l.getCurrent().equals(l.getTail())) return true;
            if(l.getCurrent().getNext()!=null){
                return true;
            }else{
                l.remembered_while_iteration = null;
                return false;
            }
        }else{
            return false;
        }
    }

    @Override
    public E next() {
        return (E) this.l.auslesenVor().o;
    }

    @Override
    public void remove() {
        this.l.currentLoeschen();
    }
}

class DescendingV2Iterator<E> implements Iterator<E>{

    V2List<E> l;

    public DescendingV2Iterator(V2List<E> l){
        this.l = l;
        this.l.setCurrent(this.l.getTail());
    }

    @Override
    public boolean hasNext() {
        if(l.getCurrent()!=null){
            if(l.getCurrent().equals(l.getHead())) return true;
            if(l.getCurrent().equals(l.getTail())) return true;
            if(l.getCurrent().hasPrev()){
                return true;
            }else{
                l.remembered_while_iteration = null;
                return false;
            }
        }else{
            return false;
        }
    }

    @Override
    public E next() {
        return (E) this.l.auslesenRueck().o;
    }

    @Override
    public void remove() {
        this.l.currentLoeschen();
    }
}