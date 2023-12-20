package agh.oop.model.objects;

import agh.oop.model.map.Vector2d;

import java.util.Objects;
import java.util.UUID;

public class Plant implements WorldElement {

    private final UUID plantId;
    private final Vector2d position;
    private final int energy;
    private final boolean isPoisonous;

    public Plant(Vector2d position, int energy, boolean isPoisonous) {
        this.position = position;
        this.energy = energy;
        this.isPoisonous = isPoisonous;
        this.plantId = UUID.randomUUID();
    }

    public UUID getId() {
        return plantId;
    }
    @Override
    public Vector2d getPosition() {
        return position;
    }
    public int getEnergy() {
        return energy;
    }
    @Override
    public boolean isAt(Vector2d position) {
        return this.position.equals(position);
    }

    @Override
    public String toString() {
        // to do (if isPoisonous...)
        return "P";
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Plant))
            return false;
        Plant that = (Plant) other;
        return that.getId() == this.getId();
    }
    @Override
    public final int hashCode() {
        return Objects.hash(plantId);
    }
}
