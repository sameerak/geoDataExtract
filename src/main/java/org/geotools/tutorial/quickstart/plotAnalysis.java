package org.geotools.tutorial.quickstart;

import com.mongodb.*;
import com.toedter.calendar.JDateChooser;
import com.vividsolutions.jts.geom.*;
import org.anomally.scatterblogs.AnomalyCluster;
import org.anomally.scatterblogs.termCluster;
import org.geotools.data.DataUtilities;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.styling.*;
import org.geotools.swing.JMapFrame;
import org.geotools.swing.action.SafeAction;
import org.geotools.tutorial.quickstart.util.WrapLayout;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.FilterFactory;

import javax.swing.*;
import java.awt.Component;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class plotAnalysis {
    // display a data store file chooser dialog for shapefiles
    static String WorldMapFile = "." + File.separator + "src" + File.separator + "main" + File.separator + "resources"
            + File.separator + "50m_cultural" + File.separator + "ne_50m_admin_0_countries.shp";
    static String roadMapFile = "." + File.separator + "src" + File.separator + "main" + File.separator + "resources"
            + File.separator + "ne_10m_roads" + File.separator + "ne_10m_roads.shp";
    static int count = 0;
    static int daycount = 0;
    static DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    static DatePanel startdatePanel;
    static DatePanel enddatePanel;
    static JTextField userid;
    static MapContent map;
    static Layer querylayer = null;
    static Layer trajectorylayer = null;
    private static JTextField coordinates;

    private static HashMap<String, HashSet<termCluster>> termClusters;

    /**
     * GeoTools Quickstart demo application. Prompts the user for a shapefile and displays its
     * contents on the screen in a map frame
     */
    public static void main(String[] args) throws Exception {
        startdatePanel = new DatePanel(df.parse("2012-03-06 00:00:00"));
        enddatePanel = new DatePanel(df.parse("2012-04-23 00:00:00"));


        /*File file = JFileDataStoreChooser.showOpenFile("shp", null);
        if (file == null) {
            return;
        }*/

        File file = new File(WorldMapFile);

        FileDataStore store = FileDataStoreFinder.getDataStore(file);
        SimpleFeatureSource featureSource = store.getFeatureSource();

        // Create a map content and add our shapefile to it
        map = new MapContent();
        map.setTitle("Quickstart");

        Style style = SLD.createSimpleStyle(featureSource.getSchema());
        Layer layer = new FeatureLayer(featureSource, style);
        map.addLayer(layer);

        file = new File(roadMapFile);

        store = FileDataStoreFinder.getDataStore(file);
        featureSource = store.getFeatureSource();

        Layer layer2 = new FeatureLayer(featureSource, style);
        map.addLayer(layer2);

        JMapFrame mapFrame;
        mapFrame = new JMapFrame(map);
        mapFrame.enableToolBar(true);
        mapFrame.enableStatusBar(true);

        JToolBar toolbar = mapFrame.getToolBar();
        toolbar.addSeparator();

        JPanel datePanel = new JPanel();
        datePanel.add(startdatePanel);
        datePanel.add(enddatePanel);
        datePanel.add(new JButton(new ValidateGeometryAction()));

        toolbar.add(datePanel);

        JPanel userPanel = new JPanel();
        userid = new JTextField("108435619");
        userPanel.add(userid);
        userPanel.add(new JButton(new PlotUserData()));

        toolbar.add(userPanel);

        JPanel coorPanel = new JPanel();
        coordinates = new JTextField("144.84903932, -37.66990267");
        coorPanel.add(coordinates);
        coorPanel.add(new JButton(new PlotLocationUserData()));

        toolbar.add(coorPanel);

        toolbar.add(new JButton(new PlotAnomalyData()));
//        toolbar.add(new JButton(new ExportShapefileAction()));

        // Display the map frame. When it is closed the application will exit
        mapFrame.setSize(800, 600);
        mapFrame.setVisible(true);

        // Now display the map
//        mapFrame.showMap(map);

        /*SimpleFeatureTypeBuilder b = new SimpleFeatureTypeBuilder();

        b.setName("MyFeatureType");
        b.setCRS(DefaultGeographicCRS.WGS84);
        b.add("location", Point.class);
        // building the type
        final SimpleFeatureType TYPE = b.buildFeatureType();

        SimpleFeatureBuilder featureBuilder;
        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
        DefaultFeatureCollection featureCollection = new DefaultFeatureCollection("internal", TYPE);

        //connecting mongoDB on local machine.
        MongoClient mongoClient = new MongoClient("localhost", 27017);
        //connecting to database named test
        DB db = mongoClient.getDB("test");
        // getting collection of all files
        DBCollection collection = db.getCollection("tweetid");

//        BasicDBObject query = new BasicDBObject();
//        query.put("date", BasicDBObjectBuilder.start("$gte", fromDate).add("$lte", toDate).get());

        DBCursor dbCursor = collection.find().sort(new BasicDBObject("timestamp", 1));

        Date oldDate = null,checkdate;

        while (dbCursor.hasNext() *//*&& count < 6*//*  && daycount < 3) {

            BasicDBObject basicObject = (BasicDBObject) dbCursor.next();

            checkdate = new Date(basicObject.getLong("timestamp"));

            if (*//*oldDate == null || *//*(oldDate != null && oldDate.getDate() != checkdate.getDate()) && daycount < 3) {
//                if (bw != null) {
//                    bw.close();
//                }
                Style styles = SLD.createSimpleStyle(TYPE, Color.green);
                Layer layers = new FeatureLayer(featureCollection, styles);

                map.addLayer(layers);
                featureCollection = new DefaultFeatureCollection("internal", TYPE);
//                break;
                daycount++;
            }

            String tmp = basicObject.get("coordinates").toString();
            tmp = tmp.substring(2, tmp.length() - 1);
//            System.out.println("" + tmp);
            String[] coordinates = tmp.split(",");
            Point point = geometryFactory.createPoint(new Coordinate(Double.parseDouble(coordinates[0]), Double.parseDouble(coordinates[1])));

            featureBuilder = new SimpleFeatureBuilder(TYPE);
            featureBuilder.add(point);
            SimpleFeature feature = featureBuilder.buildFeature(null);
            featureCollection.add(feature);

            count++;
            System.out.println(count);

            oldDate = checkdate;
        }

        Style styles = SLD.createSimpleStyle(TYPE, Color.green);
        Layer layers = new FeatureLayer(featureCollection, styles);

        map.addLayer(layers);*/
    }

    public static void clearLayers() {
        if (querylayer != null) {
            map.removeLayer(querylayer);
        }
        if (trajectorylayer != null) {
            map.removeLayer(trajectorylayer);
        }
    }

    public static JPanel buildDatePanel(String label, Date value) {
        JPanel datePanel = new JPanel();

        JDateChooser dateChooser = new JDateChooser();
        if (value != null) {
            dateChooser.setDate(value);
        }
        for (Component comp : dateChooser.getComponents()) {
            if (comp instanceof JTextField) {
                ((JTextField) comp).setColumns(50);
                ((JTextField) comp).setEditable(false);
            }
        }

        datePanel.add(dateChooser);

        SpinnerModel model = new SpinnerDateModel();
        JSpinner timeSpinner = new JSpinner(model);
        JComponent editor = new JSpinner.DateEditor(timeSpinner, "HH:mm:ss");
        timeSpinner.setEditor(editor);
        if(value != null) {
            timeSpinner.setValue(value);
        }

        datePanel.add(timeSpinner);
        datePanel.setLayout(new WrapLayout());

        return datePanel;
    }

    static class DatePanel extends JPanel {
        private JDateChooser dateChooser;
        private JSpinner timeSpinner;

        public DatePanel (Date value) {
            super();
            this.dateChooser = new JDateChooser();
            if (value != null) {
                dateChooser.setDate(value);
            }
            for (Component comp : dateChooser.getComponents()) {
                if (comp instanceof JTextField) {
                    ((JTextField) comp).setColumns(50);
                    ((JTextField) comp).setEditable(false);
                }
            }

            this.add(dateChooser);

            SpinnerModel model = new SpinnerDateModel();
            this.timeSpinner = new JSpinner(model);
            JComponent editor = new JSpinner.DateEditor(timeSpinner, "HH:mm:ss");
            timeSpinner.setEditor(editor);
            if(value != null) {
                timeSpinner.setValue(value);
            }

            this.add(timeSpinner);
            this.setLayout(new WrapLayout());
        }

        public Date GetDate() {
            DateFormat dfdate = new SimpleDateFormat("yyyy-MM-dd");
            DateFormat dftime = new SimpleDateFormat("HH:mm:ss");
            DateFormat dfreal = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            Date date = dateChooser.getDate();
            Date time = (Date) timeSpinner.getValue();

            String val = dfdate.format(date) + " " + dftime.format(time);
            dfreal.setTimeZone(TimeZone.getTimeZone("UTC"));

            Date ret = new Date();
            try {
                ret = dfreal.parse(val);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            return ret;

//            Date date = dateChooser.getDate();
//            Date time = (Date) timeSpinner.getValue();
//            Calendar ret = Calendar.getInstance();
//            ret.setTimeZone(TimeZone.getTimeZone("UTC"));
//
//            ret.setTimeInMillis(date.getTime() + time.getTime());
//
//            return ret.getTime();
        }
    }

    static Layer addPoints(double[][] coordinates) {
        SimpleFeatureTypeBuilder b = new SimpleFeatureTypeBuilder();

        b.setName("MyFeatureType");
        b.setCRS(DefaultGeographicCRS.WGS84);
        b.add("location", Point.class);
        // building the type
        final SimpleFeatureType TYPE = b.buildFeatureType();

        SimpleFeatureBuilder featureBuilder;
        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
        DefaultFeatureCollection featureCollection = new DefaultFeatureCollection("internal", TYPE);

        for (int i = 0; i < 10; i++) {
            Point point = geometryFactory.createPoint(new Coordinate( coordinates[i][0], coordinates[i][1]));
            featureBuilder = new SimpleFeatureBuilder(TYPE);
            featureBuilder.add(point);
            SimpleFeature feature = featureBuilder.buildFeature(null);
            featureCollection.add(feature);
        }

        Style style = SLD.createSimpleStyle(TYPE, Color.green);
        Layer layer = new FeatureLayer(featureCollection, style);
        return layer;
    }


    static Layer addPoint(double latitude, double longitude) {
        SimpleFeatureTypeBuilder b = new SimpleFeatureTypeBuilder();

        b.setName("MyFeatureType");
        b.setCRS(DefaultGeographicCRS.WGS84);
        b.add("location", Point.class);
        // building the type
        final SimpleFeatureType TYPE = b.buildFeatureType();

        SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(TYPE);
        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
        Point point = geometryFactory.createPoint(new Coordinate( latitude, longitude));
        featureBuilder.add(point);
        SimpleFeature feature = featureBuilder.buildFeature(null);
        DefaultFeatureCollection featureCollection = new DefaultFeatureCollection("internal", TYPE);
        featureCollection.add(feature);
        Style style = SLD.createSimpleStyle(TYPE, Color.red);

        Layer layer = new FeatureLayer(featureCollection, style);
        return layer;
    }

    static class ValidateGeometryAction extends SafeAction {

        ValidateGeometryAction() {
            super("Run");
            putValue(Action.SHORT_DESCRIPTION, "Check each geometry");
        }
        public void action(ActionEvent e) throws Throwable {
            Date startDate = startdatePanel.GetDate();
            Date endDate = enddatePanel.GetDate();

            long diff = startDate.getTime() - endDate.getTime();

            if (querylayer != null) {
                map.removeLayer(querylayer);
            }


            //connecting mongoDB on local machine.
            MongoClient mongoClient = new MongoClient("localhost", 27017);
            //connecting to database named test
            DB db = mongoClient.getDB("test");
            // getting collection of all files
            DBCollection collection = db.getCollection("correctTweetId");
            List<Integer> list = new ArrayList<Integer>();
            list.add(427657635);
            list.add(425795784);
            list.add(427675375);
            list.add(464843705);
            list.add(464866980);
            list.add(467427303);
            list.add(467478489);
            list.add(467483686);
            list.add(467487202);
            list.add(467491344);
            list.add(61043461);
            list.add(61043172);
            list.add(416661318);
            list.add(85636450);

            //after creating MOBILITY measure
            list.add(379712969);
            list.add(88054194);
            list.add(108435619);
            list.add(25251712);
            list.add(198412791);
            list.add(123791748);
            list.add(124026965);
            list.add(203345009);
            list.add(194494717);
            list.add(114681958);

            //temp remove
            list.add(96142209);

            BasicDBObject query = new BasicDBObject();
            query.put("timestamp", BasicDBObjectBuilder.start("$gte", startDate.getTime()).add("$lte", endDate.getTime()).get());
            query.put("userid",  BasicDBObjectBuilder.start("$not", new BasicDBObject("$in", list)).get());

            DBCursor dbCursor = collection.find(query).sort(new BasicDBObject("timestamp", 1));

            Date oldDate = null,checkdate;

            SimpleFeatureTypeBuilder b = new SimpleFeatureTypeBuilder();

            b.setName("MyFeatureType");
            b.setCRS(DefaultGeographicCRS.WGS84);
            b.add("location", Point.class);
            b.add("text", String.class);
            b.add("userid", Integer.class);
            b.add("username", String.class);
            b.add("created_at", String.class);
            b.add("tweet_id", Double.class);
            // building the type
            final SimpleFeatureType TYPE = b.buildFeatureType();

            SimpleFeatureBuilder featureBuilder;
            GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
            DefaultFeatureCollection featureCollection = new DefaultFeatureCollection("internal", TYPE);

            int dataLength = dbCursor.count();

            while (dbCursor.hasNext() /*&& count < 6  && daycount < 3*/) {
                BasicDBObject basicObject = (BasicDBObject) dbCursor.next();

                String tmp = basicObject.get("coordinates").toString();
                tmp = tmp.substring(2, tmp.length() - 1);
//            System.out.println("" + tmp);
                String[] coordinates = tmp.split(",");
                Point point = geometryFactory.createPoint(new Coordinate(Double.parseDouble(coordinates[0]), Double.parseDouble(coordinates[1])));

                featureBuilder = new SimpleFeatureBuilder(TYPE);
                featureBuilder.add(point);

                featureBuilder.add(basicObject.getString("text"));
                featureBuilder.add(basicObject.getInt("userid"));
                featureBuilder.add(basicObject.getString("screen_name"));
                featureBuilder.add(basicObject.getString("created_at"));
                featureBuilder.add(basicObject.getString("tweet_id"));

                SimpleFeature feature = featureBuilder.buildFeature(null);
                featureCollection.add(feature);
            }

            mongoClient.close();
            DateFormat df1 = new SimpleDateFormat("EEE MMM dd HH:mm:ss ZZZZZ yyyy");

            Style style = createPointStyle();
            querylayer = new FeatureLayer(featureCollection, style);
            map.addLayer(querylayer);

            int numInvalid = 1;
            String msg;
            if (numInvalid == 0) {
                msg = "All feature geometries are valid";
            } else {
                msg = "start: " + df1.format(startDate) + " end: " + df1.format(endDate) + " Data size = " + dataLength;
            }
            JOptionPane.showMessageDialog(null, msg, "Geometry results",
                    JOptionPane.INFORMATION_MESSAGE);
        }


        /**
         * Create a Style to draw point features as circles with blue outlines
         * and cyan fill
         */
        private Style createPointStyle() {
            StyleFactory styleFactory = CommonFactoryFinder.getStyleFactory();
            FilterFactory filterFactory = CommonFactoryFinder.getFilterFactory();
            Graphic gr = styleFactory.createDefaultGraphic();

            Mark mark = styleFactory.getCircleMark();

            mark.setStroke(styleFactory.createStroke(
                    filterFactory.literal(Color.BLUE), filterFactory.literal(1)));

            mark.setFill(styleFactory.createFill(filterFactory.literal(Color.MAGENTA)));

            gr.graphicalSymbols().clear();
            gr.graphicalSymbols().add(mark);
            gr.setSize(filterFactory.literal(5));

        /*
         * Setting the geometryPropertyName arg to null signals that we want to
         * draw the default geomettry of features
         */
            PointSymbolizer sym = styleFactory.createPointSymbolizer(gr, null);

            Rule rule = styleFactory.createRule();
            rule.symbolizers().add(sym);
            FeatureTypeStyle fts = styleFactory.createFeatureTypeStyle(new Rule[]{rule});
            Style style = styleFactory.createStyle();
            style.featureTypeStyles().add(fts);

            return style;
        }

    }

    static class PlotUserData extends SafeAction {

        PlotUserData() {
            super("Run");
            putValue(Action.SHORT_DESCRIPTION, "Check each geometry");
        }

        public void action(ActionEvent e) throws Throwable {
            Date startDate = startdatePanel.GetDate();
            Date endDate = enddatePanel.GetDate();

            clearLayers();

            //connecting mongoDB on local machine.
            MongoClient mongoClient = new MongoClient("localhost", 27017);
            //connecting to database named test
            DB db = mongoClient.getDB("test");
            // getting collection of all files
            DBCollection collection = db.getCollection("correctTweetId");

            BasicDBObject query = new BasicDBObject();
            int useridint = Integer.parseInt(userid.getText());
            query.put("userid", useridint);
            query.put("timestamp", BasicDBObjectBuilder.start("$gte", startDate.getTime()).add("$lte", endDate.getTime()).get());

            DBCursor dbCursor = collection.find(query).sort(new BasicDBObject("timestamp", 1));

            SimpleFeatureTypeBuilder b = new SimpleFeatureTypeBuilder();

            b.setName("PlotUserDataFeatureType");
            b.setCRS(DefaultGeographicCRS.WGS84);
            b.add("location", Point.class);
            b.add("text", String.class);
            b.add("userid", Integer.class);
            b.add("username", String.class);
            b.add("created_at", String.class);
            b.add("tweet_id", Double.class);
            // building the type
            final SimpleFeatureType TYPE = b.buildFeatureType();

            SimpleFeatureBuilder featureBuilder;
            GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
            DefaultFeatureCollection featureCollection = new DefaultFeatureCollection("internal", TYPE);

            int dataLength = dbCursor.count();
            int count = 0;

            Coordinate[] coords  = new Coordinate[dataLength];

            while (dbCursor.hasNext() /*&& count < 6  && daycount < 3*/) {
                BasicDBObject basicObject = (BasicDBObject) dbCursor.next();

                String tmp = basicObject.get("coordinates").toString();
                tmp = tmp.substring(2, tmp.length() - 1);
//            System.out.println("" + tmp);
                String[] coordinates = tmp.split(",");
//                Point point = geometryFactory.createPoint(new Coordinate(Double.parseDouble(coordinates[0]), Double.parseDouble(coordinates[1])));
                Coordinate coord = new Coordinate(Double.parseDouble(coordinates[0]), Double.parseDouble(coordinates[1]));

                Point point = geometryFactory.createPoint(coord);
                coords[count] = coord;

                featureBuilder = new SimpleFeatureBuilder(TYPE);
                featureBuilder.add(point);
                featureBuilder.add(basicObject.getString("text"));
                featureBuilder.add(useridint);
                featureBuilder.add(basicObject.getString("screen_name"));
                featureBuilder.add(basicObject.getString("created_at"));
                featureBuilder.add(basicObject.getLong("tweet_id"));

                SimpleFeature feature = featureBuilder.buildFeature("" + /*basicObject.getLong("tweet_id")*/count);
                featureCollection.add(feature);
                count++;
            }

            mongoClient.close();

            Style style = createPointStyle();
            querylayer = new FeatureLayer(featureCollection, style);
            map.addLayer(querylayer);

            trajectorylayer = getLayerLineByCoord(coords);
            map.addLayer(trajectorylayer);

            int numInvalid = 1;
            String msg;
            if (numInvalid == 0) {
                msg = "All feature geometries are valid";
            } else {
                msg = " Data size = " + dataLength;
            }
            JOptionPane.showMessageDialog(null, msg, "Geometry results",
                    JOptionPane.INFORMATION_MESSAGE);
        }

        private Layer getLayerLineByCoord(Coordinate[] coords) throws SchemaException {
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


        /**
         * Create a Style to draw point features as circles with blue outlines
         * and cyan fill
         */
        private Style createPointStyle() {
            StyleFactory styleFactory = CommonFactoryFinder.getStyleFactory();
            FilterFactory filterFactory = CommonFactoryFinder.getFilterFactory();
            Graphic gr = styleFactory.createDefaultGraphic();

            Mark mark = styleFactory.getCircleMark();

            mark.setStroke(styleFactory.createStroke(
                    filterFactory.literal(Color.BLUE), filterFactory.literal(1)));

            mark.setFill(styleFactory.createFill(filterFactory.literal(Color.CYAN)));

            gr.graphicalSymbols().clear();
            gr.graphicalSymbols().add(mark);
            gr.setSize(filterFactory.literal(5));

        /*
         * Setting the geometryPropertyName arg to null signals that we want to
         * draw the default geomettry of features
         */
            PointSymbolizer sym = styleFactory.createPointSymbolizer(gr, null);

            Rule rule = styleFactory.createRule();
            rule.symbolizers().add(sym);
            FeatureTypeStyle fts = styleFactory.createFeatureTypeStyle(new Rule[]{rule});
            Style style = styleFactory.createStyle();
            style.featureTypeStyles().add(fts);

            return style;
        }

    }

    static class PlotLocationUserData extends SafeAction {

        PlotLocationUserData() {
            super("Run");
            putValue(Action.SHORT_DESCRIPTION, "Check each geometry");
        }

        public void action(ActionEvent e) throws Throwable {
            Date startDate = startdatePanel.GetDate();
            Date endDate = enddatePanel.GetDate();

            if (querylayer != null) {
                map.removeLayer(querylayer);
            }


            //connecting mongoDB on local machine.
            MongoClient mongoClient = new MongoClient("localhost", 27017);
            //connecting to database named test
            DB db = mongoClient.getDB("test");
            // getting collection of all files
            DBCollection collection = db.getCollection("correctTweetId");

            BasicDBObject query = new BasicDBObject();
            String temp = coordinates.getText();

            String[] coordinates1 = temp.split(", ");
            double[] coor = {Double.parseDouble(coordinates1[0]), Double.parseDouble(coordinates1[1])};
            query.put("coordinates", coor);
            query.put("timestamp", BasicDBObjectBuilder.start("$gte", startDate.getTime()).add("$lte", endDate.getTime()).get());

            DBCursor dbCursor = collection.find(query).sort(new BasicDBObject("timestamp", 1));

            SimpleFeatureTypeBuilder b = new SimpleFeatureTypeBuilder();

            b.setName("PlotUserDataFeatureType");
            b.setCRS(DefaultGeographicCRS.WGS84);
            b.add("location", Point.class);
            b.add("text", String.class);
            b.add("userid", Integer.class);
            b.add("username", String.class);
            b.add("created_at", String.class);
            b.add("tweet_id", Double.class);
            // building the type
            final SimpleFeatureType TYPE = b.buildFeatureType();

            SimpleFeatureBuilder featureBuilder;
            GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
            DefaultFeatureCollection featureCollection = new DefaultFeatureCollection("internal", TYPE);

            int dataLength = dbCursor.count();
            int count = 0;

            while (dbCursor.hasNext() /*&& count < 6  && daycount < 3*/) {

                BasicDBObject basicObject = (BasicDBObject) dbCursor.next();

//                String tmp = basicObject.get("coordinates").toString();
//                tmp = tmp.substring(2, tmp.length() - 1);
//            System.out.println("" + tmp);
//                String[] coordinates = tmp.split(",");
                Coordinate coord = new Coordinate(coor[0], coor[1]);

                Point point = geometryFactory.createPoint(coord);

                featureBuilder = new SimpleFeatureBuilder(TYPE);
                featureBuilder.add(point);
                featureBuilder.add(basicObject.getString("text"));
                featureBuilder.add(basicObject.getInt("userid"));
                featureBuilder.add(basicObject.getString("screen_name"));
                featureBuilder.add(basicObject.getString("created_at"));
                featureBuilder.add(basicObject.getLong("tweet_id"));

                SimpleFeature feature = featureBuilder.buildFeature("" + /*basicObject.getLong("tweet_id")*/count);
                featureCollection.add(feature);
                count++;
            }

            mongoClient.close();

            Style style = createPointStyle();
            querylayer = new FeatureLayer(featureCollection, style);
            map.addLayer(querylayer);

            int numInvalid = 1;
            String msg;
            if (numInvalid == 0) {
                msg = "All feature geometries are valid";
            } else {
                msg = " Data size = " + dataLength;
            }
            JOptionPane.showMessageDialog(null, msg, "Geometry results",
                    JOptionPane.INFORMATION_MESSAGE);
        }


        /**
         * Create a Style to draw point features as circles with blue outlines
         * and cyan fill
         */
        private Style createPointStyle() {
            StyleFactory styleFactory = CommonFactoryFinder.getStyleFactory();
            FilterFactory filterFactory = CommonFactoryFinder.getFilterFactory();
            Graphic gr = styleFactory.createDefaultGraphic();

            Mark mark = styleFactory.getCircleMark();

            mark.setStroke(styleFactory.createStroke(
                    filterFactory.literal(Color.BLUE), filterFactory.literal(1)));

            mark.setFill(styleFactory.createFill(filterFactory.literal(Color.CYAN)));

            gr.graphicalSymbols().clear();
            gr.graphicalSymbols().add(mark);
            gr.setSize(filterFactory.literal(5));

        /*
         * Setting the geometryPropertyName arg to null signals that we want to
         * draw the default geomettry of features
         */
            PointSymbolizer sym = styleFactory.createPointSymbolizer(gr, null);

            Rule rule = styleFactory.createRule();
            rule.symbolizers().add(sym);
            FeatureTypeStyle fts = styleFactory.createFeatureTypeStyle(new Rule[]{rule});
            Style style = styleFactory.createStyle();
            style.featureTypeStyles().add(fts);

            return style;
        }

    }



    static class PlotAnomalyData extends SafeAction {

        PlotAnomalyData() {
            super("AnomalyRun");
            putValue(Action.SHORT_DESCRIPTION, "Check each geometry");
        }

        public void action(ActionEvent e) throws Throwable {
            Date startDate = startdatePanel.GetDate();
            Date endDate = enddatePanel.GetDate();

            int power = Integer.parseInt(userid.getText());

            if (querylayer != null) {
                map.removeLayer(querylayer);
            }

            if (termClusters == null) {
                termClusters = AnomalyCluster.getAnomalyset(startDate, endDate);
//                termClusters = AnomalyCluster.getAnomalyset(startDate, endDate);
            }

            SimpleFeatureTypeBuilder b = new SimpleFeatureTypeBuilder();

            b.setName("PlotUserDataFeatureType");
            b.setCRS(DefaultGeographicCRS.WGS84);
            b.add("location", Point.class);
            b.add("text", String.class);
            b.add("power", Integer.class);
//            b.add("username", String.class);
//            b.add("created_at", String.class);
//            b.add("tweet_id", Double.class);
            // building the type
            final SimpleFeatureType TYPE = b.buildFeatureType();

            SimpleFeatureBuilder featureBuilder;
            GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
            DefaultFeatureCollection featureCollection = new DefaultFeatureCollection("internal", TYPE);

            int dataLength = termClusters.size();
            int count = 0;

            for (String term: termClusters.keySet()) {
                for (termCluster clust: termClusters.get(term)) {
                    if (clust.getReg().size() > power) {

//                String tmp = basicObject.get("coordinates").toString();
//                tmp = tmp.substring(2, tmp.length() - 1);
//            System.out.println("" + tmp);
//                String[] coordinates = tmp.split(",");
                        Point point = geometryFactory.createPoint(new Coordinate(clust.getCentroid()[0], clust.getCentroid()[1]));

                        featureBuilder = new SimpleFeatureBuilder(TYPE);
                        featureBuilder.add(point);
                        featureBuilder.add(term);
                featureBuilder.add(clust.getReg().size());
//                featureBuilder.add(basicObject.getString("screen_name"));
//                featureBuilder.add(basicObject.getString("created_at"));
//                featureBuilder.add(basicObject.getLong("tweet_id"));

                        SimpleFeature feature = featureBuilder.buildFeature("" + /*basicObject.getLong("tweet_id")*/count);
                        featureCollection.add(feature);
                        count++;
                    }
                }
            }

            Style style = createPointStyle();
            querylayer = new FeatureLayer(featureCollection, style);
            map.addLayer(querylayer);

            int numInvalid = 1;
            String msg;
            if (numInvalid == 0) {
                msg = "All feature geometries are valid";
            } else {
                msg = " Data size = " + dataLength;
            }
            JOptionPane.showMessageDialog(null, msg, "Geometry results",
                    JOptionPane.INFORMATION_MESSAGE);
        }


        /**
         * Create a Style to draw point features as circles with blue outlines
         * and cyan fill
         */
        private Style createPointStyle() {
            StyleFactory styleFactory = CommonFactoryFinder.getStyleFactory();
            FilterFactory filterFactory = CommonFactoryFinder.getFilterFactory();
            Graphic gr = styleFactory.createDefaultGraphic();

            Mark mark = styleFactory.getCircleMark();

            mark.setStroke(styleFactory.createStroke(
                    filterFactory.literal(Color.BLUE), filterFactory.literal(1)));

            mark.setFill(styleFactory.createFill(filterFactory.literal(Color.RED)));

            gr.graphicalSymbols().clear();
            gr.graphicalSymbols().add(mark);
            gr.setSize(filterFactory.literal(5));

        /*
         * Setting the geometryPropertyName arg to null signals that we want to
         * draw the default geomettry of features
         */
            PointSymbolizer sym = styleFactory.createPointSymbolizer(gr, null);

            Rule rule = styleFactory.createRule();
            rule.symbolizers().add(sym);
            FeatureTypeStyle fts = styleFactory.createFeatureTypeStyle(new Rule[]{rule});
            Style style = styleFactory.createStyle();
            style.featureTypeStyles().add(fts);

            return style;
        }

    }
}