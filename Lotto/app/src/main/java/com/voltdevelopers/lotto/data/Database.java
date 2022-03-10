package com.voltdevelopers.lotto.data;

import java.util.ArrayList;
import java.util.Arrays;

public class Database {

    public final int numOfPulls; //Il numero di estrazioni
    public final double moneyToPay;
    private static Database instance = null;
    private Profile[] players; //i 5 giocatori, ordinati come nel pptx con le istruzioni
    private final int gameCounter;
    private final ArrayList<int[]> rounds;
    private final int[] pullsPerNumber; //n di estrazioni per valore (n di estrazioni di 1 si trova nella cella 0, di 2 nella 1, etc etc)
    private final ArrayList<Integer> pullChronology; //ordine estrazioni (ultimo estratto sta all'indice massimo)

    private Analisys analisys;

    private Database(int numOfPulls, double moneyToPay) {
        this.numOfPulls = numOfPulls;
        this.moneyToPay = moneyToPay;
        analisys = new Analisys();

        initPlayers();
        gameCounter = 0;

        rounds = new ArrayList<>();

        pullsPerNumber = new int[Settings.N_NUMBERS];
        for (int i : pullsPerNumber) pullsPerNumber[i] = 0; //inizializzo array
        pullChronology = new ArrayList<>();
    }

    public static Database getInstance(int numOfPulls, double moneyToPay) {

        if (instance != null)
            return instance;
        instance = new Database(numOfPulls, moneyToPay);
        return instance;

    }

    private void initPlayers() {
        players = new Profile[Settings.N_PLAYERS];
        for (int i = 0; i < Settings.N_PLAYERS; i++) {
            players[i] = new Profile(Settings.PLAYER_NAMES[i]);
        }
    }

    public double getPlayerMoneyWon(int playerN) {
        return players[playerN].getMoneyWon();
    }

    public double getPlayerMoneySpent(int playerN) {
        return players[playerN].getMoneySpent();
    }

    public double getPlayerNet(int playerN) {
        return players[playerN].getNet();
    }

    public int getPlayerWins(int playerN) {
        return players[playerN].getNWins();
    }

    public void addPull(int[] input) {
        rounds.add(input);
        for (int i : input) { //copio i valori ricevuti e ne aumento il numero di estrazioni
            pullsPerNumber[input[i] - 1]++;
            analisys.modChronology(input[i]);
        }

    }

    public void addPlayerBet(int playerN, int[] input) {
        players[playerN].addBet(input);
    }

    public int[] getPlayerBet(int playerN, int n) {
        return players[playerN].getBet(n);
    }

    public int[] getPlayerLastBet(int playerN) {
        return players[playerN].getLastBet();
    }

    public int getPullCount() {
        return gameCounter;
    }


    //----------------------analisys methods--------------------------------------------------------

    public int[] getNMostFrequent(int nRequested) {
        return analisys.getNMostFrequent(nRequested);
    }

    public int[] getLatestN(int nRequested) {
        return analisys.getLatestN(nRequested);
    }


    public int[] getOldestN(int nRequested) {
        return analisys.getOldestN(nRequested);
    }

    //-----------------------log managment----------------------------------------------------------

    @Override
    public String toString() {
        return "Database{" +
                "numOfPulls=" + numOfPulls +
                ", moneyToPay=" + moneyToPay +
                ", players=" + allPlayersToString() +
                ", gameCounter=" + gameCounter +
                ", rounds=" + rounds.toString() +
                ", pullsPerNumber=" + Arrays.toString(pullsPerNumber) +
                ", pullChronology=" + pullChronology +
                ", analisys=" + analisys +
                '}';
    }

    private String allPlayersToString(){

        String out = "";
        for(int i = 0; i < players.length; i++)
            out += playerToString(i);

        return out;

    }

    public String playerToString(int n){

        return players[n].toString();

    }

    private class Analisys {

        public int[] getLatestN(int nRequested) {
            int[] output = new int[nRequested];
            int chronoSize = pullChronology.size();
            for (int i : output) {
                output[i] = pullChronology.get(chronoSize);
                chronoSize--;
            }
            //TODO add log
            return output;
        }

        private void modChronology(int n) {
            if (pullChronology.contains(n))
                pullChronology.remove(pullChronology.lastIndexOf(n));

            pullChronology.add(n);
            //TODO add log
        }

        public int[] getOldestN(int nRequested) {
            int[] output = new int[nRequested];
            for (int i : output) {
                output[i] = pullChronology.get(i);
            }

            //TODO add log
            return output;
        }

        public int[] getNMostFrequent(int nRequested) {
            int[] output = new int[nRequested];
            for (int i : output) {
                int max = 0;
                for (int j : pullsPerNumber) {
                    if (pullsPerNumber[j] >= max && !intArrayContains(output, j + 1)) {
                        max = pullsPerNumber[j];
                        output[i] = j + 1;
                    }
                }
            }
            //TODO add log
            return output;
        }

        private boolean intArrayContains(int[] arr, int n) {
            for (int i : arr) {
                if (arr[i] == n)
                    //TODO add log
                    return true;
            }
            //TODO add log
            return false;
        }
    }
}

class Profile {
    private String name;
    private double moneyWon, moneySpent;
    private final int nWins;
    private final ArrayList<int[]> betList;

    public Profile() {
        moneyWon = 0;
        moneySpent = 0;
        nWins = 0;
        betList = new ArrayList<>();
    }

    public Profile(String name) {
        this();
        this.name = name;
    }

    public void addBet(int[] input) {
        betList.add(new int[Settings.get().getnOfPulls()]);
        for (int i : betList.get(betList.size() - 1)) {
            betList.get(betList.size() - 1)[i] = input[i];
        }
    }

    public int[] getBet(int n) {
        return betList.get(n);
    }

    public int[] getLastBet() {
        return betList.get(betList.size() - 1);
    }

    public int[] getSelectedBet(int n) {
        return betList.get(n);
    }

    public double getMoneyWon() {
        return moneyWon;
    }

    public void addToMoneyWon(double moneyWon) {
        this.moneyWon += moneyWon;
    }

    public double getMoneySpent() {
        return moneySpent;
    }

    public void addToMoneySpent(double moneySpent) {
        this.moneySpent += moneySpent;
    }

    public double getNet() {
        return moneyWon - moneySpent;
    }

    public int getNWins() {
        return nWins;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        if (name != null)
            return name;
        return "name not set";
    }

    @Override
    public String toString() {
        return "Profile{" +
                "name='" + name + '\'' +
                ", moneyWon=" + moneyWon +
                ", moneySpent=" + moneySpent +
                ", nWins=" + nWins +
                //", betList=" + betList +
                '}';
    }
}
=======

public class Database {

    private static Database instance = null;
    private Profile[] players; //i 5 giocatori, ordinati come nel pptx con le istruzioni
    private int[] gameCounter;
    private ArrayList<Integer[]> rounds;
    private int[] pullsPerNumber; //n di estrazioni per valore (n di estrazioni di 1 si trova nella cella 0, di 2 nella 1, etc etc)
    private int[] pullChronology; //ordine estrazioni (ultimo estratto sta in arr[89])
    private String log;             //log usato per la console

    private Database() {

        initPlayers();
        gameCounter = new int[2]; //il primo è per le partite "storiche", il secondo è per quelle in cui partecipano i giocatori
        gameCounter[0] = 0;
        gameCounter[1] = 0;

        rounds = new ArrayList<Integer[]>();

        pullsPerNumber = new int[90];
        for (int i : pullsPerNumber) pullsPerNumber[i] = 0; //inizializzo array
        pullChronology = new int[90];

        log = "";

        //TODO add log edit

    }

    public static Database get() {

        if (instance != null)
            return instance;
        instance = new Database();
        return instance;

        //TODO add log edit
    }

    private void initPlayers(){

        //TODO initialize players and set player names via constructor if needed
        //TODO add log edit

    }

    public int getPlayerWinnings(int playerN){

        return players[playerN].getWinnings();
        //TODO add log edit

    }

    public int getPlayerSpendings(int playerN){

        return players[playerN].getSpendings();
        //TODO add log edit

    }

    public int getPlayerNet(int playerN){

        return players[playerN].getNet();
        //TODO add log edit

    }

    public int getPlayerWins(int playerN){

        return players[playerN].getWins();
        //TODO add log edit

    }

    public void addPull(int[] input) {

        rounds.add(new Integer[5]);
        for (int i : rounds.get(rounds.size() - 1)) { //copio i valori ricevuti e ne aumento il numero di estrazioni
            rounds.get(rounds.size() - 1)[i] = input[i];
            pullsPerNumber[input[i] - 1]++;
            //TODO modifico la cronologia dei valori estratti
            //TODO add log edit
        }

    }

    public void addPlayerBet(int playerN, int[] input){

        players[playerN].addPull(input);

    }

    public int getTotalPulls() {
        return gameCounter[0] + gameCounter[1];
        //TODO add log edit
    }

    public int getHistoricPulls() {
        return gameCounter[0];
        //TODO add log edit
    }

    public int getPlayedPulls() {
        return gameCounter[1];
        //TODO add log edit
    }

    /*public int getMostFrequent(int nRequested){

        //TODO return most pulled
        //TODO add log edit

    }*/

    /*public int[] getLatestN(int nRequested){

        //TODO return latest n pulled form pullChronology
        //TODO add log edit

    }*/

    /*public int[] getOldestN(int nRequested){

        //TODO return oldest n pulled form pullChronology
        //TODO add log edit

    }*/

    private void addToString(String in) { //aggiunge un azione al log

        log += in + "\n";
        //TODO add log edit

    }

    public String pushString() { //ritorna il log senza pulirlo

        return log;
        //TODO add log edit

    }

    public String pushString(boolean clear) { //ritorna il log, pulisce se input vero

        if (clear) {

        }
        String out = log;
        log = "";
        return out;
        //TODO add log edit

    }

}
