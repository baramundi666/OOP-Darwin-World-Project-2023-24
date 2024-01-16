package agh.oop.presenter;


import agh.oop.model.map.Boundary;
import agh.oop.model.map.Earth;
import agh.oop.model.map.Vector2d;
import agh.oop.simulation.Simulation;
import agh.oop.simulation.statictics.Statistics;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.*;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class SimulationPresenter implements ChangeListener {

    @FXML
    public GridPane mapGrid;
    @FXML
    private Label infoLabel;
    @FXML
    private Label animalNumber;
    @FXML
    private Label plantNumber;
    @FXML
    private Label freeFields;
    @FXML
    private Label avgEnergy;
    @FXML
    private Label avgLifeLength;
    @FXML
    private Label avgChildrenNumber;
    @FXML
    private Label dominantGenotype;
    private int width;
    private int height;
    private List<Node> steppeImageList;
    private List<Node> specialAreaImageList;
    private Simulation simulationToRun;
    private Statistics statistics;
    private final List<Label> toBeCleared = new LinkedList<>();

    public void setSimulation(Simulation simulationToRun, Earth earth, String mapID, String isSavingStats) {
        this.simulationToRun = simulationToRun;
        this.width = earth.getBounds().upperRight().getX() + 1;
        this.height = earth.getBounds().upperRight().getY() + 1;

        var imageGenerator = new ImageGenerator(width, height, (double) 450 /max(width,height), (double) 450/max(width,height));

        steppeImageList = imageGenerator.generateImageList("steppe.png", 0.85);

        switch (mapID) {
            case "p1":
                specialAreaImageList = imageGenerator.generateImageList("jungle.png", 0.3);
                break;
            case "p2":
                specialAreaImageList = imageGenerator.generateImageList("poisonedArea.png", 0.3);
                break;
            default: throw  new IllegalArgumentException("Invalid mapID");
        }
        statistics = new Statistics(isSavingStats);
    }

    private void setStatistics(){
        animalNumber.setText(String.valueOf(statistics.getNumberOfAnimals()));
        plantNumber.setText(String.valueOf(statistics.getNumberOfPlants()));
        freeFields.setText(String.valueOf(statistics.getNumberOfNotOccupiedFields()));
        avgEnergy.setText(String.format("%.2f", statistics.getAverageEnergy()));
        avgLifeLength.setText(String.format("%.2f", statistics.getAverageLifeLength()));
        avgChildrenNumber.setText(String.format("%.2f",statistics.getAverageNumberOfChildren()));
        dominantGenotype.setText(statistics.getDominantGenotype().map(Object::toString).orElse("No dominant genotype"));
    }

    @Override
    public void mapInitialized(Earth earth, String message) {
        Platform.runLater(() -> {
            drawGrid();
            drawDefaultBackground();
            infoLabel.setText(message);
            setStatistics();
        });
    }

    @Override
    public void mapChanged(Earth earth, String message) {
        Platform.runLater(() -> {
            drawMapElements(earth);
            infoLabel.setText(message);
            setStatistics();
        });
    }

    private boolean inSpecialArea(int i, int j) {
        Vector2d position = new Vector2d(i,j);
        Boundary borders = simulationToRun.getSpecialAreaBorders();
        return position.follows(borders.lowerLeft()) && position.precedes(borders.upperRight());
    }

    public void drawGrid(){
        clearGrid(mapGrid);
        double cellSize = (double) 500 /(max(width,height)+1);

        mapGrid.getColumnConstraints().add(new ColumnConstraints(cellSize));
        mapGrid.getRowConstraints().add(new RowConstraints(cellSize));
        Label axis = new Label("y\\x");
        axis.setTextFill(Paint.valueOf("white"));
        mapGrid.add(axis,0,0);//assume that left upper corner is (0,0)
        GridPane.setHalignment(axis, HPos.CENTER);

        for (int i=0;i<height;i++){
            mapGrid.getRowConstraints().add(new RowConstraints(cellSize));
            Label label = new Label(String.valueOf(i));
            label.setTextFill(Paint.valueOf("white"));
            mapGrid.add(label,0,i+1);
            GridPane.setHalignment(label, HPos.CENTER);
        }
        for (int i=0;i<width;i++){
            mapGrid.getColumnConstraints().add(new ColumnConstraints(cellSize));
            var label = new Label(String.valueOf(i));
            label.setTextFill(Paint.valueOf("white"));
            mapGrid.add(label,i+1,0);
            GridPane.setHalignment(label, HPos.CENTER);
        }
    }

    public void drawDefaultBackground(){
        var steppeImageIterator = steppeImageList.iterator();
        var specialAreaImageIterator = specialAreaImageList.iterator();

        for(int i=0; i<width; i++) {
            for(int j=0; j<height; j++) {
                if (inSpecialArea(i,j)) {
                    var specialAreaImage = specialAreaImageIterator.next();
                    mapGrid.add(specialAreaImage, i+1, j+1);
                    GridPane.setHalignment(specialAreaImage, HPos.CENTER);
                }
                else {
                    var steppeImage = steppeImageIterator.next();
                    mapGrid.add(steppeImage, i+1, j+1);
                    GridPane.setHalignment(steppeImage, HPos.CENTER);
                }
            }
        }
    }

    public void drawMapElements(Earth earth) {
        clearGrid(mapGrid);
        var plantsMap = earth.getPlants();
        var animalsMap = earth.getAnimals();
        for(Vector2d position: plantsMap.keySet()){
            var plant = plantsMap.get(position);
            var plantImage = new Label("\u2022");
            plantImage.setFont(Font.font(30));
            plantImage.setTextFill(Paint.valueOf(plant.getPlantColor()));
            plantImage.setAlignment(Pos.CENTER);
            toBeCleared.add(plantImage);
            mapGrid.add(plantImage, position.getX() + 1, position.getY() + 1);
            GridPane.setHalignment(plantImage, HPos.CENTER);
        }

        for(Vector2d position: animalsMap.keySet()){
            if(!animalsMap.get(position).isEmpty()) {
                int animalCount = animalsMap.get(position).size();
                var firstAnimal = animalsMap.get(position).iterator().next();
                var animalImage = new Label("\u25A0");


                animalImage.setTextFill(Paint.valueOf(firstAnimal.getAnimalColor()));
                animalImage.setAlignment(Pos.CENTER);
                toBeCleared.add(animalImage);
                mapGrid.add(animalImage, position.getX() + 1, position.getY() + 1);
                GridPane.setHalignment(animalImage, HPos.CENTER);
            }
        }
    }

    @FXML
    public void onSimulationStartClicked() {
        simulationToRun.registerListener(statistics);
        simulationToRun.registerListener(this);
        Thread engineThread = new Thread(simulationToRun);
        engineThread.start();
    }

    @FXML
    private void onSimulationStopClicked() {
        //simulationToRun.stop();
    }

    @FXML
    private void onSimulationResumeClicked() {
//        simulationToRun.reset();
//        statistics.reset();
//        drawDefaultBackground();
//        infoLabel.setText("Simulation reset");
    }

    private void clearGrid(GridPane grid) {
        for (Label label : toBeCleared) {
            grid.getChildren().remove(label);
        }
        toBeCleared.clear();
    }

    public void handleGridClick(MouseEvent event) {
        double mouseX = event.getSceneX();
        double mouseY = event.getSceneY();
        double cellWidth = (double) 500 /(width+1);
        double cellHeight = (double) 500 /(height+1);
        int column = (int) (mouseX/cellHeight);
        int row = (int) (mouseY/cellWidth);
        var animals = simulationToRun.getEarth().getAnimals();
        System.out.println(mouseX + " " + mouseY);
        System.out.println(row + " " + column);
        System.out.println(animals.get(new Vector2d(row, column)));
    }

    public void highlightDominantGenotype() {//now higlihts while simulation is running
        var dominantGenotype = statistics.getDominantGenotype();
        if (dominantGenotype.isEmpty()) return;
        var dominantGenotypeList = dominantGenotype.get();
        var animals = simulationToRun.getEarth().getAnimals();
        for (Vector2d position : animals.keySet()) {
            for (var animal : animals.get(position)) {
                if (animal.getGenome().getGeneList().equals(dominantGenotypeList)) {
                    var animalImage = new Label("\u25A0");
                    animalImage.setTextFill(Paint.valueOf("blue"));
                    animalImage.setAlignment(Pos.CENTER);
                    toBeCleared.add(animalImage);
                    mapGrid.add(animalImage, position.getX() + 1, position.getY() + 1);
                    GridPane.setHalignment(animalImage, HPos.CENTER);
                }
            }
        }
    }
}

