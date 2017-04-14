package org.sameera.geo.concavehull;

import org.trajectory.clustering.TrajectoryPoint;

public class Triangle {
    private TrajectoryPoint[] vertices;//these should form a clockwise rotation
    private int[] adjacentNeighbours = new int[3];
    private int numOfNeighbours = 0;
    private int pos = -1;

    public Triangle(int pos, TrajectoryPoint[] vertices) {
        this.pos = pos;
        this.vertices = vertices;
    }

    public TrajectoryPoint[] getVertices() {
        return vertices;
    }

    public int[] getAdjacentNeighbours() {
        return adjacentNeighbours;
    }

    public int getNumOfNeighbours() {
        return numOfNeighbours;
    }

    public void addNeighbour(int neighbourID) {
        adjacentNeighbours[numOfNeighbours] = neighbourID;
        ++numOfNeighbours;
    }

    public int getPos() {
        return pos;
    }
}
