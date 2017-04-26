package model.rules;

import model.Box;
import model.formulas.Formula;

public class Copy extends Rule{
	private Integer rowRef;
	
	@Override
	public boolean hasCompleteInfo() {
		return rowRef != null;
	}

	@Override
	public void updateReference(int index, String newValue) {
		if (index != 1) throw new IllegalArgumentException();
        try {
            rowRef = ReferenceParser.parseIntegerReference(newValue);
        } catch (NumberFormatException e) {
            rowRef = null;
            throw new NumberFormatException();
        }
	}

	@Override
	public boolean verifyReferences(Box data, int rowIndex) {
		return data.isInScopeOf(rowRef, rowIndex);
	}

	@Override
	public boolean verifyRow(Box data, int rowIndex) {
		return data.getRow(rowRef).getFormula().equals(data.getRow(rowIndex).getFormula());
	}

	@Override
	public Formula generateRow(Box data) {
		return data.getRow(rowRef).getFormula();
	}

	@Override
	public String[] getReferenceStrings() {
		return new String[]{rowRef+""};
	}

	@Override
	public String getDisplayName() {
		return "Copy";
	}
	
	@Override
	public String toString(){
		return "Copy "+rowRef;
	}

}
