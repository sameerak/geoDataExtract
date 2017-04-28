package org.sameera.geo.concavehull;

import com.vividsolutions.jts.geom.Coordinate;
import org.trajectory.clustering.Line;
import org.trajectory.clustering.TrajectoryPoint;

import java.math.BigDecimal;
import java.math.MathContext;

public class Triangle {
    private TrajectoryPoint[] vertices;//these should form a clockwise rotation
    private Line[] edges; //contains 3 edges in order 0-1,1-2,2-0
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

    public Line[] getEdges() {
        return edges;
    }

    public void setEdges(Line[] edges) {
        this.edges = edges;
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

    public void setEquationSK() {
        double A2 = vertices[0].getSquared(),
                B2 = vertices[1].getSquared(),
                C2 = vertices[2].getSquared();

        double yayb = vertices[0].getY() - vertices[1].getY(),
                yayc = vertices[0].getY() - vertices[2].getY(),
                xaxb = vertices[0].getX() - vertices[1].getX(),
                xaxc = vertices[0].getX() - vertices[2].getX();

        double sx, sy;
        sx = 0.5 * (((C2 - A2) * yayb - (B2 - A2) * yayc) / (xaxb * yayc - xaxc * yayb));
        sy = 0.5 * (((C2 - A2) * xaxb - (B2 - A2) * xaxc) / (xaxc * yayb - xaxb * yayc));

        circumcenter = new TrajectoryPoint(sx, sy, 0 ,0);
        Coordinate center = circumcenter.getCoordinate();
        double dist0 = center.distance(vertices[0].getCoordinate()),
                dist1 = center.distance(vertices[1].getCoordinate()),
                dist2 = center.distance(vertices[2].getCoordinate());
//        if (dist0 == dist1 && dist0 == dist2 && dist1 == dist2) {
//            circumradius = dist0;
//            return;
//        }

        MathContext mc2 = MathContext.DECIMAL128,
        mc = MathContext.UNLIMITED;
//                new MathContext(0);
        BigDecimal bdA2 = new BigDecimal(A2, mc),
                bdB2 = new BigDecimal(B2, mc),
                bdC2 = new BigDecimal(C2, mc),
                bdyayb = new BigDecimal(String.valueOf(new BigDecimal(vertices[0].getY(), mc).subtract(new BigDecimal(vertices[1].getY(), mc), mc)), mc),
                bdyayc = new BigDecimal(String.valueOf(new BigDecimal(vertices[0].getY(), mc).subtract(new BigDecimal(vertices[2].getY(), mc), mc)), mc),
                bdxaxb = new BigDecimal(String.valueOf(new BigDecimal(vertices[0].getX(), mc).subtract(new BigDecimal(vertices[1].getX(), mc), mc)), mc),
                bdxaxc = new BigDecimal(String.valueOf(new BigDecimal(vertices[0].getX(), mc).subtract(new BigDecimal(vertices[2].getX(), mc), mc)), mc),
                bdHalf = new BigDecimal(0.5, mc);

        BigDecimal bdSx, bdSy;
        BigDecimal sxUnder = bdxaxb.multiply(bdyayc, mc).subtract(bdxaxc.multiply(bdyayb, mc), mc),
        sxOver = ((bdC2.subtract(bdA2, mc)).multiply(bdyayb, mc))
                .subtract(bdB2.subtract(bdA2, mc).multiply(bdyayc, mc));

        bdSx = bdHalf.multiply((sxOver.divide(sxUnder, mc2)));

        BigDecimal syUnder = bdxaxc.multiply(bdyayb, mc).subtract(bdxaxb.multiply(bdyayc, mc), mc),
                syOver = ((bdC2.subtract(bdA2, mc)).multiply(bdxaxb, mc))
                        .subtract(bdB2.subtract(bdA2, mc).multiply(bdxaxc, mc));

        bdSy = bdHalf.multiply((syOver.divide(syUnder, mc2)));

        TrajectoryPoint circumcenter1 = new TrajectoryPoint(bdSx.doubleValue(), bdSy.doubleValue(), 0 ,0);
    }

    public void setEquationPerpendicularLines() {
        Line line1, line2;
        if (edges == null) {
            line1 = new Line(1, vertices[0], vertices[1]);
            line2 = new Line(2, vertices[1], vertices[2]);
        } else {
            line1 = edges[0];
            line2 = edges[1];
        }

        double a1, b1, c1, a2, b2, c2;

        a1 = line1.getB();
        b1 = line1.getA() * -1;
        c1 = line1.getCPerpendicular();
        a2 = line2.getB();
        b2 = line2.getA() * -1;
        c2 = line2.getCPerpendicular();

        double a2b1MINa1b2 = a2 * b1 - a1 * b2;

        double sx, sy;

        sx = (b2 * c1 - b1 * c2) / a2b1MINa1b2;
        sy = (a1 * c2 - a2 * c1) / a2b1MINa1b2;

        circumcenter = new TrajectoryPoint(sx, sy, 0 ,0);
        Coordinate center = circumcenter.getCoordinate();
        double dist0 = center.distance(vertices[0].getCoordinate()),
                dist1 = center.distance(vertices[1].getCoordinate()),
                dist2 = center.distance(vertices[2].getCoordinate());
//        if (dist0 == dist1 && dist0 == dist2 && dist1 == dist2) {
            circumradius = Math.max(dist2, Math.max(dist0, dist1));
//        }
    }

    @Override
    public String toString() {
        String out = "pos = " + pos + ", " + vertices[0] + " -> " + vertices[1] + " -> " + vertices[2];
        if (edges != null) {
            out += "\n" + edges[0] + " ^ " + edges[1] + " ^ " + edges[2];
        }
        return out;
    }
}
