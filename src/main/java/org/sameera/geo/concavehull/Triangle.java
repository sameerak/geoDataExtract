package org.sameera.geo.concavehull;

import com.vividsolutions.jts.geom.Coordinate;
import org.trajectory.clustering.TrajectoryPoint;

public class Triangle {
    private TrajectoryPoint[] vertices;//these should form a clockwise rotation
    private TrajectoryPoint circumcenter;
    private double circumradius = Double.MAX_VALUE;
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

    public void addNeighbour(int neighbourID) {
        adjacentNeighbours[numOfNeighbours] = neighbourID;
        ++numOfNeighbours;
    }

    public int getPos() {
        return pos;
    }

    public Coordinate getCircumCenter() {
        return circumcenter.getCoordinate();
    }

    public double getCircumRadius() {
        return circumradius;
    }

    public void setEquation() {
        //from http://www.qc.edu.hk/math/Advanced%20Level/circle%20given%203%20points.htm
        //method 7
        //and https://en.wikipedia.org/wiki/Circumscribed_circle#Cartesian_coordinates
        double A2 = vertices[0].getSquared(),
                B2 = vertices[1].getSquared(),
                C2 = vertices[2].getSquared();

        double Sx = .5 * getDeterminant3By3(new double[][]
                {{A2, vertices[0].getY(), 1},
                {B2, vertices[1].getY(), 1},
                {C2, vertices[2].getY(), 1}}),
        Sy = .5 * getDeterminant3By3(new double[][]
                {{vertices[0].getX(), A2, 1},
                {vertices[1].getX(), B2, 1},
                {vertices[2].getX(), C2, 1}}),
        a = getDeterminant3By3(new double[][]
                {{vertices[0].getX(), vertices[0].getY(), 1},
                {vertices[1].getX(), vertices[1].getY(), 1},
                {vertices[2].getX(), vertices[2].getY(), 1}}),
        b = getDeterminant3By3(new double[][]
                {{vertices[0].getX(), vertices[0].getY(), A2},
                {vertices[1].getX(), vertices[1].getY(), B2},
                {vertices[2].getX(), vertices[2].getY(), C2}});

        circumcenter = new TrajectoryPoint(Sx / a, Sy / a, 0 ,0);
        Coordinate center = circumcenter.getCoordinate();
        double dist0 = center.distance(vertices[0].getCoordinate()),
                dist1 = center.distance(vertices[1].getCoordinate()),
                dist2 = center.distance(vertices[2].getCoordinate());
        if (dist0 == dist1 && dist0 == dist2 && dist1 == dist2) {
            circumradius = (Math.sqrt((b / a) + ((Math.pow(Sx, 2) + Math.pow(Sy, 2)) / Math.pow(a, 2))));
        }
    }

    public static double getDeterminant3By3(double[][] matrix) {
        //Utilizes determinant calculation taken from
        //https://en.wikipedia.org/wiki/Determinant
        double a = matrix[0][0],
                d = matrix[1][0],
                g = matrix[2][0],

                b = matrix[0][1],
                e = matrix[1][1],
                h = matrix[2][1],

                c = matrix[0][2],
                f = matrix[1][2],
                i = matrix[2][2];

        return a*e*i + b*f*g + c*d*h - e*c*g - b*d*i - a*f*h;
    }
}
