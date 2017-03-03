
package model.formulas;

public class Disjunction extends Formula {
	public final Formula lhs;
    public final Formula rhs;

    public Disjunction(Formula lhs, Formula rhs){
        this.lhs = lhs;
        this.rhs = rhs;
        super.precedence = 2;
    }
    //TODO: equals()
    
    @Override
    public String toString(){	
    	StringBuilder strB = new StringBuilder();
    	strB.append( lhs.getPrecedence() < 2 ? "("+lhs+")" : lhs+"" );
    	strB.append( " ∨ " );
    	strB.append( rhs.getPrecedence() < 3 ? "("+rhs+")" : rhs+"" );
    	return strB.toString();
    }
}
