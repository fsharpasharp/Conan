package model;

import java.io.Serializable;
import java.util.ArrayList;

public class Proof implements Serializable{
    private ArrayList<ProofListener> listeners;
    private ArrayList<ProofRow> rows = new ArrayList();
    private Parser parser;
    private int curDepth;

    public Proof() {
        listeners = new ArrayList();
        parser = new Parser();
        curDepth = 0;
    }

    /***
     * Add the row to the Proof representation. Add null pointer instead of Formula if not well formed.
     * @param formula
     * @param rule
     */
    public void addRow(String formula, String rule) {
        try {
            rows.add(new ProofRow(parser.parse(formula), rule, curDepth));
        } catch(Exception ParseException) {
            rows.add(new ProofRow(null, rule, curDepth));
        }
        for (ProofListener listener : this.listeners) {
            listener.rowInserted();
        }
    }
    public void deleteRow(int rowNumber){}
    public void deleteRow(){
        if (rows.size() >= 0) {
            rows.remove(rows.size() - 1);
            for (ProofListener listener : this.listeners) {
                listener.rowDeleted();
            }
        }

    }
    public void insertRow(String formula, String rule, int rowNumber){}
    public void updateRow(String formula, String rule, int rowNumber){}

    /**
     * Alert the listeners about the row.
     * @param formula
     * @param rowNumber
     */
    public void updateFormulaRow(String formula, int rowNumber){
        int rowIndex = rowNumber-1;
        ProofRow row = rows.get(rowIndex);
        boolean wellFormed = true;
        try {
            row.setFormula(parser.parse(formula));
        } catch(Exception ParseException) {
            wellFormed = false;
        }
        if (!wellFormed && !formula.equals("")) {
            row.setFormula(null);
            for (ProofListener listener : this.listeners) {
                listener.rowUpdated(false, rowNumber);
            }
        } else {
            for (ProofListener listener : this.listeners) {
                listener.rowUpdated(true, rowNumber);
            }
        }
    }
    public void updateRuleRow(String rule, int rowNumber){}
    public void saveProof(String filepath){}
    public void loadProof(String filepath){}
    public boolean verifyProof(int start){return true;}
    public boolean verifyRow(int rowNumber){return true;}
    public void openBox() {
        curDepth++;
        for (ProofListener listener : this.listeners) {
            listener.boxOpened();
        }
    }
    public void closeBox(){
        if (curDepth < 0)
            return;
        --curDepth;
        for (ProofListener listener : this.listeners) {
            listener.boxClosed();
        }
    }
    public void verifyConclusion(){}
    public void registerProofListener(ProofListener listener){
        this.listeners.add(listener);
    }
    public Rule createCustomRule(){return null;}
}
