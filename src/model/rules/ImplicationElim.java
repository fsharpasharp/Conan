package model.rules;

import model.Box;
import model.formulas.Formula;
import model.formulas.Implication;

public class ImplicationElim extends Rule {

    private Integer rowRef1;
    private Integer rowRef2;

    @Override
    public boolean hasCompleteInfo() {
        return rowRef1 != null && rowRef2 != null;
    }

    @Override
    public void updateReference(int index, String newValue) {
        if (index < 1 || index > 2) throw new IllegalArgumentException();

        if (index == 1) {
            try {
                rowRef1 = ReferenceParser.parseIntegerReference(newValue);
            } catch (NumberFormatException e) {
                rowRef1 = null;
                throw new NumberFormatException();
            }
        } else {//index == 2
            try {
                rowRef2 = ReferenceParser.parseIntegerReference(newValue);
            } catch (NumberFormatException e) {
                rowRef2 = null;
                throw new NumberFormatException();
            }
        }
    }

    @Override
    public boolean verifyReferences(Box data, int rowIndex) {
        if (data.isInScopeOf(rowRef1, rowIndex) == false) return false;
        if (data.isInScopeOf(rowRef2, rowIndex) == false) return false;

        Formula referencedRow1 = data.getRow(rowRef1).getFormula();
        Formula referencedRow2 = data.getRow(rowRef2).getFormula();

        //make sure second reference is to an Implication formula and cast it
        if (referencedRow2 instanceof Implication == false) return false;
        Implication implRef = (Implication) referencedRow2;

        //check that the content of referenced rows and row to be verified are in line with the rule
        if (implRef.lhs.equals(referencedRow1) == false) return false;
        return true;
    }

    @Override
    public boolean verifyRow(Box data, int rowIndex) {
        Formula referencedRow2 = data.getRow(rowRef2).getFormula();
        Implication implRef = (Implication) referencedRow2;
        return implRef.rhs.equals(data.getRow(rowIndex).getFormula());
    }

    @Override
    public Formula generateRow(Box data) {
        Formula referencedRow2 = data.getRow(rowRef2).getFormula();
        Implication implRef = (Implication) referencedRow2;
        return implRef.rhs;
    }

    @Override
    public String toString() {
        return String.format("→e, %s, %s", rowRef1 == null ? "" : new Integer(rowRef1+1), rowRef2 == null ? "" : new Integer(rowRef2+1));
    }
    
	@Override
	public String[] getReferenceStrings() {
		String ref1 = rowRef1 == null ? "" : (rowRef1+1)+"";
		String ref2 = rowRef2 == null ? "" : (rowRef2+1)+"";
		return new String[]{ref1, ref2};
	}

	@Override
	public String getDisplayName() {
		return "→E";
	}

}
