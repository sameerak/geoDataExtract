package org.geotools.tutorial.quickstart.util;

//import com.ianturton.cookbook.utilities.GenerateRandomData;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import java.awt.Color;

import org.geotools.data.DataUtilities;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;
import org.geotools.swing.JMapFrame;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

public class DrawLine {
    public Layer getLayerLineByCoord(Coordinate[] coords) throws SchemaException {
        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
        LineString line = geometryFactory.createLineString(coords);
        SimpleFeatureType TYPE = DataUtilities.createType("test", "line", "the_geom:LineString");
        SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder((SimpleFeatureType) TYPE);
        featureBuilder.add(line);
        SimpleFeature feature = featureBuilder.buildFeature("LineString_Sample");

        DefaultFeatureCollection lineCollection = new DefaultFeatureCollection();
        lineCollection.add(feature);

        Style style = SLD.createLineStyle(Color.BLUE, 1);
        return new FeatureLayer(lineCollection, style);
    }

    public static void main(String[] args) throws SchemaException {
        MapContent map = new MapContent();
        map.setTitle("Quickstart");

        DrawLine line = new DrawLine();
        Coordinate[] coords  =
                new Coordinate[] {new Coordinate(138.60912773,-34.89399723), new Coordinate(151.20721503,-33.8879154), new Coordinate(115.80792686,-31.71070802) };
//        LineString ls = GenerateRandomData.createRandomLineString(10);
        Layer layer = line.getLayerLineByCoord(coords/*ls.getCoordinates()*/);
        map.addLayer(layer);


        // Now display the map
        JMapFrame.showMap(map);

    }
}