package model.rules;

import model.Box;
import model.ProofRow;
import model.formulas.Conjunction;
import model.formulas.Disjunction;
import model.formulas.Formula;

public class DisjunctionIntro implements Rule{

	private Integer premise1;

	public DisjunctionIntro(){}

	public DisjunctionIntro(int premise1Index){
		premise1 = premise1Index;
	}

	
	@Override
    public void updateReference(int refNr, String refStr){
      if(refNr != 1) throw new IllegalArgumentException();
      Integer ref;
      try{
        ref = ReferenceParser.parseIntegerReference(refStr);
      }
      catch(NumberFormatException e){
        ref = null;
        throw new NumberFormatException(); //Still want this to propagate up
      }
      setPremise1(ref);
    }
	
	@Override
	public boolean hasCompleteInfo() {
		return premise1 != null;
	}

	public Integer getPremise1() {
		return premise1;
	}

	public void setPremise1(int premise1) {
		this.premise1 = premise1;
	}

	@Override
	public String toString(){
		String p1 = premise1 == null ? "" : premise1.toString();
		return String.format("∧-I (%s)",p1);
	}

	@Override
	public boolean verify(Box data, int rowIndex) {
		ProofRow rowToVerify = data.getRow( rowIndex );

		// are the references in the rule object in scope of rowIndex?
		// are all the referenced rows verified?
		// ProofData.isInScope should check scope and if the data is verified
		if( data.isInScopeOf( getPremise1(), rowIndex) == false) { //this currently assumes the premise is stored as a rowNr rather than index
			System.out.println("    Scope issue");
			return false;
		}

		// do we have the needed premises/references to make the deduction?
		if(rowToVerify.getFormula() instanceof Disjunction == false) return false;
		return data.getRow( getPremise1() ).isVerified();
	}

}
