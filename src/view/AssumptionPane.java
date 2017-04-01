package view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

public class AssumptionPane extends GridPane {
    Button openButton=new Button("\u2293");
    Button closeButton=new Button("\u2294");
    public AssumptionPane(){
    //this.getChildren().addAll(openButton,closeButton);
    openButton.setMinWidth(100);
    openButton.setMinHeight(40);
    closeButton.setMinWidth(100);
    closeButton.setMinHeight(40);
        this.add(openButton,0,0);
    this.add(closeButton,1,0);

    this.setMargin(openButton, new Insets(5, 5, 5, 5));
    this.setMargin(closeButton, new Insets(5, 5, 5, 5));
    this.setGridLinesVisible(true);
//  this.setAlignment(openButton,Pos.CENTER_LEFT);
//  closeButton.setAlignment(Pos.CENTER_RIGHT);
    }

    public Button getOpenButton(){
        return openButton;
    }

    public Button getCloseButton(){
        return closeButton;
    }
}
