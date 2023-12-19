package agh.oop.simulation;

import agh.oop.model.map.Earth;
import agh.oop.model.map.Vector2d;
import agh.oop.model.objects.Animal;
import agh.oop.model.objects.Plant;
import agh.oop.presenter.ChangeListener;
import agh.oop.presenter.SimulationPresenter;

import java.util.*;
import java.util.stream.Collectors;

public class SimulationDay{
    private final Earth earth;
    private final HashSet<Animal> animals;
    private final HashSet<Vector2d> notGrownFields;
    private final int reproduceEnergy;
    private final int newPlantNumber;
    private final int plantEnergy;

    public SimulationDay(Earth earth, HashSet<Animal> animals, HashSet<Vector2d> notGrownFields, int newPlantNumber, int plantEnergy, int reproduceEnergy){
        this.earth = earth;
        this.animals = animals;
        this.notGrownFields = notGrownFields;
        this.reproduceEnergy = reproduceEnergy;
        this.newPlantNumber = newPlantNumber;
        this.plantEnergy = plantEnergy;
    }

    public void run(){
        synchronized (this) {
            removeDeadAnimals();
            moveAnimals();
            animalsEat();
            reproduceAnimals();
            spawnPlants();
        }
    }
    private void removeDeadAnimals(){
        var animalMap = earth.getAnimals();
        for(Vector2d position : animalMap.keySet()){
            var animalList = animalMap.get(position);
            for(Animal animal : animalList) {
                if (animal.getEnergy() <= 0) {//remove dead
                    earth.removeAnimal(animal);
                    animals.remove(animal);
                }
            }
        }
    }

    private void moveAnimals(){
        for(Animal animal: animals){
            earth.move(animal);
        }
    }

    private void animalsEat(){
        var animalMap = earth.getAnimals();
        var plantMap = earth.getPlants();
        for(Vector2d position: animalMap.keySet()){
            if(plantMap.containsKey(position)){
                if (!animalMap.get(position).isEmpty()) {
                    List<Animal> strongest = conflict(animalMap.get(position));
                    strongest.get(0).eat(plantMap.get(position));
                    earth.removePlant(plantMap.get(position));
                    notGrownFields.add(position);
                }
            }
        }
    }

    private void reproduceAnimals(){
        var animalMap = earth.getAnimals();
        for(Vector2d position: animalMap.keySet()) {//reproduce
            if (animalMap.get(position).size() > 1) {
                List<Animal> strongest = conflict(animalMap.get(position));
                Animal dad = strongest.get(0);
                Animal mom = strongest.get(1);
                if(mom.getEnergy()>=reproduceEnergy) {//we know that dad.getEnergy()>=reproduceEnergy
                    Animal child = dad.reproduce(mom);
                    earth.placeAnimal(child);
                    animals.add(child);
                    dad.incrementChildrenCount();
                    mom.incrementChildrenCount();
                }
            }
        }
    }
    void spawnPlants(){
        List<Vector2d> notGrownFieldsList = new ArrayList<>(notGrownFields);
        Collections.shuffle(notGrownFieldsList);
        for(int i=0; i<newPlantNumber && i<notGrownFieldsList.size(); i++){
            //what to do when there is no space for all plants
            //to do add poison plants
            Vector2d position = notGrownFieldsList.get(i);
            Plant newPlant = new Plant(position ,plantEnergy,false);
            notGrownFields.remove(position);
            earth.placePlant(newPlant);
        }
    }

    private List<Animal> conflict(HashSet<Animal> animals){//to do in one line
        List<Animal> strongest = animals.stream()
                .sorted(Comparator
                        .comparingInt(Animal::getEnergy)
                        .thenComparingInt(Animal::getLifeLength)
                        .thenComparingInt(Animal::getChildrenCount))
                .collect(Collectors.toList());
        Collections.reverse(strongest);
        return strongest;
    }
}
