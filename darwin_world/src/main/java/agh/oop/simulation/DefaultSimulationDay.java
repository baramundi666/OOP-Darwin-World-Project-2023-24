package agh.oop.simulation;

import agh.oop.model.map.Earth;
import agh.oop.model.map.Vector2d;
import agh.oop.model.objects.Animal;
import agh.oop.model.objects.inheritance.Mutation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class DefaultSimulationDay extends AbstractSimulationDay {

    public DefaultSimulationDay(Earth earth, HashSet<Animal> animals, HashSet<Vector2d> notGrownFields, int newPlantNumber, int plantEnergy, int reproduceEnergy, AbstractSpawner spawner, Mutation mutation) {
        super(earth, animals, notGrownFields, newPlantNumber, plantEnergy, reproduceEnergy, spawner, mutation);
    }

    @Override
    protected void moveAnimals(){
        var animalMap = earth.getAnimals();
        var toMove = new LinkedList<Animal>();
        for (Vector2d position : animalMap.keySet()) {
            toMove.addAll(animalMap.get(position));
        }
        for (Animal animal : toMove){
            earth.move(animal);
        }
    }
}