package view;
import java.awt.event.ActionEvent;

import java.util.*;

import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import model.BoxReference;
import model.Proof;
import model.ProofListener;


/***
 * The ProofView panes consists of two VBoxes
 *
 * One for lineNumbers and one for the rows with TextFields.
 *
 * LineNo | rows
 * -------------------
 * 1      | BorderPane
 * 2      | BorderPane
 * 3      | BorderPane
 * 4      | BorderPane
 *
 * The row is a BorderPane and consists of two TextFields and a BorderPane 
 *
 * Row
 * =============
 * Left - a TextField for the expression
 * Center - a TextField for the rule
 * 
 * These can be reached by calling the BorderPanes method's getLeft(), getCenter() and getRight().
 * Remember to cast these to TextFields.
 * E.g. (TextField) rList.get(rList.size()-1).getCenter()
 * 
 * Right - a BorderPane the consists of three Textfields for the rule prompt
 *
 * Keep in mind, that each child of rows is not a BorderPane. Boxes are additional VBoxes, with styling.
 * E.g.
 * rows
 * ====
 * Box
 *      BorderPane
 *      BorderPane
 *      Box
 *          BorderPane
 *      BorderPane
 * BorderPane
 */
public class ProofView implements ProofListener, View{
	/*
	 * These are magic constants that decide the lineNo padding.
	 * Margin can't be changed as a property, so the solution is to take into account how much the border
	 * and the padding increases the distance between rows and add the padding to the line numbers accordingly.
	 */
	static final int carryAddOpen = 3;
	static final int carryAddClose = 5;


	// TextFields of the premises and conclusion for quick access
	private TextField premises;
	private TextField conclusion;

	private Stack<VBox> curBoxDepth = new Stack<>();

	// This is a list of RowPanes, which are the "lines" of the proof.
	private List<RowPane> rList = new ArrayList<>();
	private int counter = 1;
	//private int carry = 0;

	private VBox lineNo;
	private VBox rows;

	//The tab object of this view
	private Tab tab;

	//The proof displayed in this view
	private Proof proof;

	//The patth where this proof was loaded/should be saved
	private String path;

	//Name of the proof/file of this view
	private String name;

	private TextField lastFocusedTf;
	private int caretPosition;

	private TextField lastTf;

	/**
	 * This ia listener that is applied to the last textField. It creates a new row, each time the value of the textField is changed.
	 */
	private ChangeListener<? extends String> lastTfListener = (ov, oldValue, newValue) -> {
		newRow();
	};

	private AnchorPane createProofPane() {
		lineNo = new VBox();
		rows = new VBox();
		lineNo.setFillWidth(true);
		rows.setFillWidth(true);
		HBox hb = new HBox();
		hb.setHgrow(rows, Priority.ALWAYS);
		hb.getChildren().addAll(lineNo, rows);
		hb.setPadding(new Insets(5, 5, 5, 5));
		ScrollPane sp = new ScrollPane(hb);
		sp.getStyleClass().add("fit");
		hb.heightProperty().addListener((ov, oldValue, newValue) -> {
			if (newValue.doubleValue() > oldValue.doubleValue()) { // Change this to only trigger on new row!!
				sp.setVvalue(1.0);                                 // Otherwise it will scroll down when you insert a row in the middle
			}
		});
		AnchorPane proofPane = new AnchorPane(sp);
		proofPane.setTopAnchor(sp, 0.0);
		proofPane.setRightAnchor(sp, 0.0);
		proofPane.setLeftAnchor(sp, 0.0);
		proofPane.setBottomAnchor(sp, 0.0);

		return proofPane;
	}


	/**
	 * Adds content to the TabPane in the proof and adds listeners to the premise and conclusion. 
	 * Vad är det som metoden gör med proof? lägg gärna till extra beskrivning om detta!!
	 * @param tabPane
	 * @param premisesAndConclusion
	 */
	public ProofView(TabPane tabPane, Proof proof, HBox premisesAndConclusion) {
		this.proof = proof;
		this.proof.registerProofListener(this);
		this.premises = (TextField) premisesAndConclusion.getChildren().get(0);
		this.conclusion = (TextField) premisesAndConclusion.getChildren().get(2);
		this.conclusion.textProperty().addListener((ov, oldValue, newValue) -> {
			proof.updateConclusion(newValue);
		});
		proof.updateConclusion(this.conclusion.getText());

		this.premises.focusedProperty().addListener((observable, oldValue, newValue) -> {
			lastFocusedTf = this.premises;
			caretPosition = this.premises.getCaretPosition();
		});
		this.conclusion.focusedProperty().addListener((observable, oldValue, newValue) -> {
			lastFocusedTf = this.conclusion;
			caretPosition = this.conclusion.getCaretPosition();
		});
		SplitPane sp = new SplitPane(premisesAndConclusion, createProofPane());
		sp.setOrientation(Orientation.VERTICAL);
		sp.setDividerPosition(0, 0.1);
		AnchorPane anchorPane = new AnchorPane(sp);
		anchorPane.setTopAnchor(sp, 0.0);
		anchorPane.setRightAnchor(sp, 0.0);
		anchorPane.setLeftAnchor(sp, 0.0);
		anchorPane.setBottomAnchor(sp, 0.0);
		ProofTab tab = new ProofTab("Proof", this);
		this.tab = tab;
		tab.setContent(anchorPane);
		tabPane.getTabs().add(tab);
		tabPane.getSelectionModel().select(tab); // Byt till den nya tabben
		newRow();
	}

	public ProofView(TabPane tabPane, Proof proof) {
		this(tabPane, proof, CommonPanes.premisesAndConclusion());
	}
	public ProofView(TabPane tabPane, Proof proof, String sPremises, String sConclusion) {
		this(tabPane, proof, CommonPanes.premisesAndConclusion(sPremises, sConclusion));
	}
	/* Controller begin */
	public void openBox() {
		proof.openBox();
	}
	public void closeBox() {
		proof.closeBox();
	}
	public void newRow() {
		proof.addRow();
	}
	public void rowDeleteRow(){
		proof.deleteRow();
	}
	public void insertNewRow(int rowNo, BoxReference br){
		proof.insertNewRow(rowNo, br);
	}
	/* End of controller */

	private void checkAndAdd(Region item) {
		if (curBoxDepth.isEmpty()) {
			rows.getChildren().add(item);
		} else {
			VBox temp = curBoxDepth.peek();
			temp.getChildren().add(item);
		}
	}

	/**
	 * Creates a new row with a textfield for the expression and a textfield
	 * for the rules and adds listeners to both of them.
	 * @return bp, the BorderPane containing two textfields. 
	 */
	private RowPane createRow(boolean isFirstRowInBox, int nrOfClosingBoxes) {
		//borderpane which contains the textfield for the expression and the rule
		RowPane bp = new RowPane(isFirstRowInBox,nrOfClosingBoxes);
		RulePane ruleAndRulePrompt = new RulePane();
		
		TextField tfExpression = new TextField();
		TextField tfRule = new TextField();
		TextField tfRulePromt1 = new TextField();
		TextField tfRulePromt2 = new TextField();
		TextField tfRulePromt3 = new TextField();
		
		//set the rule prompts to invisible
		tfRulePromt1.setVisible(false);
		tfRulePromt2.setVisible(false);
		tfRulePromt3.setVisible(false);
		
		//setting id
		tfExpression.setId("leftTextfield");
		tfRule.setId("rightTextfield");
		tfRulePromt1.setId("rulePromt1tf");
		tfRulePromt2.setId("rulePromt2tf");
		tfRulePromt3.setId("rulePromt3tf");
		
		//setting text style
		tfExpression.getStyleClass().add("myText");
		tfRule.getStyleClass().add("myText");
		tfRulePromt1.getStyleClass().add("myText");
		tfRulePromt2.getStyleClass().add("myText");
		tfRulePromt3.getStyleClass().add("myText");
		
		//setting the width for the textfields
		tfExpression.setPrefWidth(580);
		tfRule.setMaxWidth(100);
		tfRulePromt1.setMaxWidth(80);
		tfRulePromt2.setMaxWidth(80);
		tfRulePromt3.setMaxWidth(80);
		
		//adding listeners to the expression- and rule textfield
		tfExpression.focusedProperty().addListener((observable, oldValue, newValue) -> {
			lastFocusedTf = tfExpression;
			caretPosition = tfExpression.getCaretPosition();
		});
		tfRule.focusedProperty().addListener((observable, oldValue, newValue) -> {
			lastFocusedTf = tfRule;
			caretPosition = tfRule.getCaretPosition();
		});
		
		//adding the textfield for the rule and the rulepromts
		ruleAndRulePrompt.getChildren().add(tfRule);
		ruleAndRulePrompt.getChildren().add(tfRulePromt1);
		ruleAndRulePrompt.getChildren().add(tfRulePromt2);
		ruleAndRulePrompt.getChildren().add(tfRulePromt3);
		
		bp.setCenter(tfExpression);
		bp.setRight(ruleAndRulePrompt);
		
		bp.setCache(true);
		bp.setCacheShape(true);
		bp.setCacheHint(CacheHint.SPEED);
		return bp;
	}

	//should only be called AFTER a new row has been added to rList since it uses rList.size()
	Label createLabel() {
		Label lbl = new Label(""+rList.size());
		lbl.getStyleClass().add("lineNo");
		lbl.setPadding(new Insets(8,2,2,2));
		//lbl.setCache(true);
		//lbl.setCacheShape(true);
		lbl.setCacheHint(CacheHint.SPEED);
		return lbl;
	}

	//Adds a new row at the end of the proof
	public void rowAdded(){
		RowPane rp;
		if(curBoxDepth.isEmpty()){
			rp = createRow(false, 0);
			rList.add(rp);
			rows.getChildren().add(rp);
		}
		else{
			VBox box = curBoxDepth.peek();
			List<Node> children = box.getChildren();
			boolean isFirstRowInBox = (children.isEmpty()) ? true : false;
			rp = createRow(isFirstRowInBox, 0);
			rList.add(rp);
			children.add(rp);
		}
		lineNo.getChildren().add(createLabel());
		updateLabelPaddings(rList.size());
		addListeners(rp);
	}

	//rowNo is which row the reference row is at, BoxReference tells you if you want to add the new row before or after
	public void rowInserted(int rowNo, BoxReference br) {
		RowPane referenceRow;//row we'll use to get a refence to the box where we add the new row
		boolean isFirstRowInBox;
		int nrOfClosingBoxes; //the nr of boxes that closes at line rowNo
		int indexToInsertInParent;
		VBox parentBox; // which vbox to display the new row in
		int rListInsertionIndex = (br == BoxReference.BEFORE) ? rowNo-1 : rowNo;

		if(br == BoxReference.BEFORE){
			referenceRow = rList.get(rowNo-1);
			isFirstRowInBox = referenceRow.getIsFirstRowInBox();
			nrOfClosingBoxes = 0;
			referenceRow.setIsFirstRowInBox(false);
			parentBox = (VBox)referenceRow.getParent();
			indexToInsertInParent = parentBox.getChildren().indexOf(referenceRow);
		}
		else{ //br == BoxReference.AFTER
			referenceRow = rList.get(rowNo-1);
			nrOfClosingBoxes = referenceRow.getNrOfClosingBoxes();
			isFirstRowInBox = false;
			referenceRow.setNrOfClosingBoxes(0);
			parentBox = (VBox)referenceRow.getParent();
			indexToInsertInParent = parentBox.getChildren().indexOf(referenceRow) + 1;
		}
		RowPane rp = createRow(isFirstRowInBox, nrOfClosingBoxes);
		((TextField)rp.getCenter()).setText("*");
		parentBox.getChildren().add(indexToInsertInParent,rp);
		rList.add(rListInsertionIndex, rp);
		lineNo.getChildren().add(createLabel());
		updateLabelPaddings(rowNo);
		addListeners(rp);
		/*
        if (lastTf != null) {
            lastTf.textProperty().removeListener((ChangeListener<? super String>) lastTfListener);
        }
        TextField tempTf = (TextField) bp.getCenter();
        //...tempTf.setText(formula);
        lastTf = tempTf;
        lastTf.textProperty().addListener((ChangeListener<? super String>) lastTfListener);
		 */

	}
	// public void focus() { // Save the last focused textfield here for quick resuming?
	//     Platform.runLater(() -> lastTf.requestFocus());
	// }
	public void boxOpened(){
		VBox vb = new VBox();
		vb.getStyleClass().add("openBox");
		checkAndAdd(vb);
		curBoxDepth.push(vb);
		newRow();
	}


	public void boxClosed(){
		if (!curBoxDepth.isEmpty()) {
			VBox vb = curBoxDepth.pop();

			//Update last row and its padding
			rList.get(rList.size()-1).incrementNrOfClosingBoxes();
			updateLabelPaddings(rList.size()-1);

			vb.getStyleClass().clear();
			vb.getStyleClass().add("closedBox");

		}
	}
	private void applyStyleIf(TextField expression, boolean bool, String style) {
		expression.getStyleClass().removeIf((s) -> s.equals(style));
		if (bool) {
			expression.getStyleClass().add(style);
		}
	}

	public void rowUpdated(boolean wellFormed, int lineNo) {
		System.out.println("RowUpdated");
		TextField expression = (TextField) rList.get(lineNo-1).getCenter();
		applyStyleIf(expression, !wellFormed, "bad");
	}
	public void conclusionReached(boolean correct, int lineNo){
		TextField expression = (TextField) rList.get(lineNo-1).getCenter();
		applyStyleIf(expression, correct, "conclusionReached");
	}


	//update view to reflect that row with nr rowNr has been deleted
	public void rowDeleted(int rowNr){
		RowPane rp = rList.get(rowNr-1);
		VBox box = (VBox)rp.getParent();
		List<Node> parentComponentList = box.getChildren();
		if(parentComponentList.remove(rp) == false){
			System.out.println("ProofView.rowDeleted: something went wrong!");
			return;
		}
		rList.remove(rp);
		boolean wasOnlyRowInBox = parentComponentList.isEmpty();
		boolean updatePreviousRowLabel = false;

		//deleted row was last row in this box, remove the box
		if(wasOnlyRowInBox){
			removeRecursivelyIfEmpty(box);
		}
		else{
			if(rp.getIsFirstRowInBox()){ // next row is now first in this box
				RowPane nextRow = rList.get(rowNr-1);
				nextRow.setIsFirstRowInBox(rp.getIsFirstRowInBox());
			}
			if(rp.getNrOfClosingBoxes() > 0){ // previous row now closes the boxes
				rList.get(rowNr-2).setNrOfClosingBoxes(rp.getNrOfClosingBoxes());
				updatePreviousRowLabel = true;
			}
		}

		//remove a Label and update paddings in relevant labels
		List<Node> labelList = lineNo.getChildren();
		labelList.remove(labelList.size()-1);
		updateLabelPaddings( updatePreviousRowLabel ? rowNr-1 : rowNr  );
	}

	public Tab getTab(){ return tab;}
	public Proof getProof(){ return proof;}
	public String getPath(){ return path;}
	public void setPath(String path){ this.path = path; }
	public String getName(){ return name;}
	public void setName(String name){ this.name = name; }

	//updates the padding for the labels starting from lineNr all the way down
	//TODO: get rid of magic numbers
	private void updateLabelPaddings(int lineNr){
		List<Node> labelList = this.lineNo.getChildren();
		assert(rList.size() == labelList.size());
		//update the padding for each label by checking the corresponding rowPane
		for(int i = lineNr-1; i < labelList.size() ; i++){
			RowPane rp = rList.get(i);
			int topPadding = rp.getIsFirstRowInBox() ? 11 : 8;
			int bottomPadding = 2 + 5 * rp.getNrOfClosingBoxes();
			Label label = (Label) labelList.get(i);
			label.setPadding(new Insets(topPadding,2,bottomPadding,2));
		}
	}

	//add listeners for the formula and rule textfields in the RowPane at the given rowNr
	private void addListeners(RowPane rp){

		// Updates the Proof object if the textField is updated
		TextField formulaField = (TextField) rp.getCenter();
		RulePane tmprulePane = (RulePane) rp.getRight();
		TextField ruleField = (TextField) tmprulePane.getChildren().get(0);
		
		
		//TODO: add listeners to the rulePromt textfields
		//TextField RulePromt1 = (TextField) rp.getRight()
		formulaField.textProperty().addListener((ov, oldValue, newValue) -> {
			int rpIndex = rList.indexOf(rp);
			proof.updateFormulaRow(newValue, rpIndex+1);
		});
		// Updates the Proof object if the textField is updated
		ruleField.textProperty().addListener((ov, oldValue, newValue) -> {
			int rpIndex = rList.indexOf(rp);
			proof.updateRuleRow(newValue, rpIndex+1);
		});
	}

	//
	private void removeRecursivelyIfEmpty(VBox box){
		Node parentNode = box.getParent();
		assert( box.getChildren().isEmpty() );
		if(parentNode instanceof VBox ){
			((VBox) parentNode).getChildren().remove(box);
			if(box.getStyleClass().toString().equals("openBox")){
				VBox box2 = curBoxDepth.pop();
				assert(box == box2);
			}
			removeRecursivelyIfEmpty((VBox) parentNode);

		}
	}


	/**
	 * Adds the unicode symbol that has been pressed to the caret position in the focused text field.
	 * @param event, the pressed unicode button. 
	 */
	public void addSymbol(javafx.event.ActionEvent event){
		
		if(lastFocusedTf != null && lastFocusedTf.getId() == "leftTextfield"){
			int tmpCaretPosition = caretPosition;
			String[] parts = event.toString().split("'");
			lastFocusedTf.setText(lastFocusedTf.getText().substring(0, caretPosition) + parts[1] 
					+ lastFocusedTf.getText().substring(caretPosition, lastFocusedTf.getLength()));
			lastFocusedTf.requestFocus();
			lastFocusedTf.positionCaret(tmpCaretPosition+1);
		}
	}
	
	/**
	 * Adds the rule symbol that has been pressed to the text field for the rule.
	 * @param event
	 */
	public void addRule(javafx.event.ActionEvent event){
		if(lastFocusedTf != null && lastFocusedTf.getId() == "rightTextfield"){
			int tmpCaretPosition = caretPosition;
			String[] parts = event.toString().split("'");
			lastFocusedTf.setText(parts[1]);
			lastFocusedTf.requestFocus();
			lastFocusedTf.positionCaret(tmpCaretPosition+1);
		}
	}

	public void rowInserted(){}
}