/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.demo.tree.skins;

import java.util.List;

import javafx.css.PseudoClass;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

import org.eclipse.emf.common.command.CompoundCommand;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.domain.EditingDomain;

import de.tesis.dynaware.grapheditor.GConnectorSkin;
import de.tesis.dynaware.grapheditor.GNodeSkin;
import de.tesis.dynaware.grapheditor.demo.GraphEditorDemo;
import de.tesis.dynaware.grapheditor.model.GConnection;
import de.tesis.dynaware.grapheditor.model.GConnector;
import de.tesis.dynaware.grapheditor.model.GModel;
import de.tesis.dynaware.grapheditor.model.GNode;
import de.tesis.dynaware.grapheditor.model.GraphFactory;
import de.tesis.dynaware.grapheditor.model.GraphPackage;

/**
 * Node skin for a 'tree-like' graph.
 *
 * <p>
 * Not part of the graph editor library, only used in the {@link GraphEditorDemo} application.
 * </p>
 */
public class TreeNodeSkin extends GNodeSkin {

    private static final String STYLE_CLASS_BORDER = "tree-node-border";
    private static final String STYLE_CLASS_BACKGROUND = "tree-node-background";
    private static final String STYLE_CLASS_SELECTION_HALO = "tree-node-selection-halo";
    private static final String STYLE_CLASS_BUTTON = "tree-node-button";
    private static final String STYLE_CLASS_ICON = "icon";

    private static final PseudoClass PSEUDO_CLASS_SELECTED = PseudoClass.getPseudoClass("selected");

    private static final double HALO_OFFSET = 5;
    private static final double HALO_CORNER_SIZE = 10;

    private static final double MIN_WIDTH = 81;
    private static final double MIN_HEIGHT = 61;

    // Child nodes will be added this far below their parent.
    private static final double CHILD_Y_OFFSET = 80;

    private static final int PLUS_ICON = 0xf067;

    private static final EReference NODES = GraphPackage.Literals.GMODEL__NODES;
    private static final EReference CONNECTIONS = GraphPackage.Literals.GMODEL__CONNECTIONS;
    private static final EReference CONNECTOR_CONNECTIONS = GraphPackage.Literals.GCONNECTOR__CONNECTIONS;

    private static final double VIEW_PADDING = 15;

    private final Rectangle selectionHalo = new Rectangle();
    private final Button addChildButton = new Button();

    private GConnectorSkin inputConnectorSkin;
    private GConnectorSkin outputConnectorSkin;

    /**
     * Creates a new {@link TreeNodeSkin} instance.
     *
     * @param node the {link GNode} this skin is representing
     */
    public TreeNodeSkin(final GNode node) {

        super(node);

        getRoot().getBorderRectangle().getStyleClass().setAll(STYLE_CLASS_BORDER);
        getRoot().getBackgroundRectangle().getStyleClass().setAll(STYLE_CLASS_BACKGROUND);
        getRoot().setMinSize(MIN_WIDTH, MIN_HEIGHT);

        addSelectionHalo();
        addSelectionListener();
        addButton();
    }

    @Override
    public void setConnectorSkins(final List<GConnectorSkin> connectorSkins) {

        removeConnectors();

        if (connectorSkins == null || connectorSkins.isEmpty() || connectorSkins.size() > 2) {
            return;
        }

        for (final GConnectorSkin skin : connectorSkins) {
            if (TreeSkinConstants.TREE_OUTPUT.equals(skin.getConnector().getType())) {
                outputConnectorSkin = skin;
                getRoot().getChildren().add(skin.getRoot());
            } else if (TreeSkinConstants.TREE_INPUT.equals(skin.getConnector().getType())) {
                inputConnectorSkin = skin;
                getRoot().getChildren().add(skin.getRoot());
            }
        }
    }

    @Override
    public void layoutConnectors() {
        layoutTopAndBottomConnectors();
        layoutSelectionHalo();
    }

    @Override
    public Point2D getConnectorPosition(final GConnectorSkin connectorSkin) {

        final Node connectorRoot = connectorSkin.getRoot();

        final double x = connectorRoot.getLayoutX() + connectorSkin.getWidth() / 2;
        final double y = connectorRoot.getLayoutY() + connectorSkin.getHeight() / 2;

        return new Point2D(x, y);
    }

    /**
     * Lays out the connectors. Inputs on top, outputs on the bottom.
     */
    private void layoutTopAndBottomConnectors() {

        if (inputConnectorSkin != null) {

            final double inputX = (getRoot().getWidth() - inputConnectorSkin.getWidth()) / 2;
            final double inputY = -inputConnectorSkin.getHeight() / 2;

            inputConnectorSkin.getRoot().setLayoutX(inputX);
            inputConnectorSkin.getRoot().setLayoutY(inputY);
        }

        if (outputConnectorSkin != null) {

            final double outputX = (getRoot().getWidth() - outputConnectorSkin.getWidth()) / 2;
            final double outputY = getRoot().getHeight() - outputConnectorSkin.getHeight() / 2;

            outputConnectorSkin.getRoot().setLayoutX(outputX);
            outputConnectorSkin.getRoot().setLayoutY(outputY);
        }
    }

    /**
     * Adds the selection halo and initializes some of its values.
     */
    private void addSelectionHalo() {

        getRoot().getChildren().add(selectionHalo);

        selectionHalo.setManaged(false);
        selectionHalo.setMouseTransparent(false);
        selectionHalo.setVisible(false);

        selectionHalo.setLayoutX(-HALO_OFFSET);
        selectionHalo.setLayoutY(-HALO_OFFSET);

        selectionHalo.getStyleClass().add(STYLE_CLASS_SELECTION_HALO);
    }

    /**
     * Lays out the selection halo based on the current width and height of the node skin region.
     */
    private void layoutSelectionHalo() {

        if (selectionHalo.isVisible()) {

            final Rectangle border = getRoot().getBorderRectangle();

            selectionHalo.setWidth(border.getWidth() + 2 * HALO_OFFSET);
            selectionHalo.setHeight(border.getHeight() + 2 * HALO_OFFSET);

            final double cornerLength = 2 * HALO_CORNER_SIZE;
            final double xGap = border.getWidth() - 2 * HALO_CORNER_SIZE + 2 * HALO_OFFSET;
            final double yGap = border.getHeight() - 2 * HALO_CORNER_SIZE + 2 * HALO_OFFSET;

            selectionHalo.setStrokeDashOffset(HALO_CORNER_SIZE);
            selectionHalo.getStrokeDashArray().setAll(cornerLength, yGap, cornerLength, xGap);
        }
    }

    /**
     * Adds a listener to react to whether the node is selected or not and change the style accordingly.
     */
    private void addSelectionListener() {

        selectedProperty().addListener((observable, oldValue, newValue) -> {

            if (newValue) {
                getRoot().getBackgroundRectangle().pseudoClassStateChanged(PSEUDO_CLASS_SELECTED, true);
                selectionHalo.setVisible(true);
                layoutSelectionHalo();
                getRoot().toFront();
            } else {
                getRoot().getBackgroundRectangle().pseudoClassStateChanged(PSEUDO_CLASS_SELECTED, false);
                selectionHalo.setVisible(false);
            }
        });
    }

    /**
     * Removes any input and output connectors from the list of children, if they exist.
     */
    private void removeConnectors() {

        if (inputConnectorSkin != null) {
            getRoot().getChildren().remove(inputConnectorSkin.getRoot());
        }

        if (outputConnectorSkin != null) {
            getRoot().getChildren().remove(outputConnectorSkin.getRoot());
        }
    }

    /**
     * Adds a button to the node skin that will add a child node when pressed.
     */
    private void addButton() {

        StackPane.setAlignment(addChildButton, Pos.BOTTOM_RIGHT);

        addChildButton.getStyleClass().setAll(STYLE_CLASS_BUTTON);
        addChildButton.setCursor(Cursor.DEFAULT);
        addChildButton.setPickOnBounds(false);

        final Text icon = new Text(String.valueOf((char) PLUS_ICON));
        icon.getStyleClass().setAll(STYLE_CLASS_ICON);

        addChildButton.setGraphic(icon);
        addChildButton.setOnAction(event -> addChildNode());

        getRoot().getChildren().add(addChildButton);
    }

    /**
     * Adds a child node with one input and one output connector, placed directly underneath its parent.
     */
    private void addChildNode() {

        final GNode childNode = GraphFactory.eINSTANCE.createGNode();

        childNode.setType(TreeSkinConstants.TREE_NODE);
        childNode.setX(getNode().getX() + (getNode().getWidth() - childNode.getWidth()) / 2);
        childNode.setY(getNode().getY() + getNode().getHeight() + CHILD_Y_OFFSET);

        final GModel model = getGraphEditor().getModel();
        final double maxAllowedY = model.getContentHeight() - VIEW_PADDING;

        if (childNode.getY() + childNode.getHeight() > maxAllowedY) {
            childNode.setY(maxAllowedY - childNode.getHeight());
        }

        final GConnector input = GraphFactory.eINSTANCE.createGConnector();
        final GConnector output = GraphFactory.eINSTANCE.createGConnector();

        input.setType(TreeSkinConstants.TREE_INPUT);
        output.setType(TreeSkinConstants.TREE_OUTPUT);

        childNode.getConnectors().add(input);
        childNode.getConnectors().add(output);

        // This allows multiple connections to be created from the output.
        output.setConnectionDetachedOnDrag(false);

        final GConnector parentOutput = findOutput();
        final GConnection connection = GraphFactory.eINSTANCE.createGConnection();

        connection.setType(TreeSkinConstants.TREE_CONNECTION);
        connection.setSource(parentOutput);
        connection.setTarget(input);

        input.getConnections().add(connection);

        // Set the rest of the values via EMF commands because they touch the currently-edited model.
        final EditingDomain editingDomain = AdapterFactoryEditingDomain.getEditingDomainFor(model);
        final CompoundCommand command = new CompoundCommand();

        command.append(AddCommand.create(editingDomain, model, NODES, childNode));
        command.append(AddCommand.create(editingDomain, model, CONNECTIONS, connection));
        command.append(AddCommand.create(editingDomain, parentOutput, CONNECTOR_CONNECTIONS, connection));

        if (command.canExecute()) {
            editingDomain.getCommandStack().execute(command);
        }
    }

    /**
     * Finds the output connector of this skin's node.
     *
     * <p>
     * Assumes the node has 1 or 2 connectors, and if there are 2 connectors the second is the output. Bit dodgy but
     * only used in the demo.
     * </p>
     */
    private GConnector findOutput() {

        if (getNode().getConnectors().size() == 1) {
            return getNode().getConnectors().get(0);
        } else if (getNode().getConnectors().size() == 2) {
            return getNode().getConnectors().get(1);
        } else {
            return null;
        }
    }
}
