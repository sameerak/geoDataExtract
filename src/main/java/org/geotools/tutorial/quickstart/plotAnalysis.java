package org.geotools.tutorial.quickstart;

import com.mongodb.*;
import com.toedter.calendar.JDateChooser;
import com.vividsolutions.jts.geom.*;
import org.anomally.scatterblogs.AnomalyCluster;
import org.anomally.scatterblogs.extractedTerm;
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
import org.geotools.tutorial.quickstart.util.TweetPOJO;
import org.geotools.tutorial.quickstart.util.WrapLayout;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.FilterFactory2;
import org.sameera.geo.concavehull.KelpFusion;
import org.trajectory.clustering.*;

import javax.swing.*;
import java.awt.Component;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Calendar;
import java.util.regex.Pattern;

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
    static JCheckBox enableAnomaly;
    static Layer querylayer = null;
    static Layer trajectorylayer = null;
    static ArrayList<Layer> clusterLayers = null;
    static Layer userlayer = null;
    private static JTextField coordinates;
    public static long miliSeconds_perDay = 24 * 60 * 60 * 1000;
    public static KelpFusion kelpFusion = null;

    private static HashMap<Integer, ArrayList<TweetPOJO>> userSets = new HashMap<Integer, ArrayList<TweetPOJO>>();

    public static Color[] shortColors4Day = {
//            Color.red,
//            Color.orange,
//            Color.yellow,
//            Color.green,
//            Color.blue,
//            Color.CYAN
            //magenta
            Color.red.darker().darker(),
            Color.red.darker(),
            Color.red,
            Color.yellow,
            Color.yellow.darker()/*.darker()*/,
            Color.yellow.darker().darker()/*.darker().darker()*/
    };
    public static Color[] Colors4Day = {
            //magenta
            hex2Rgb("#FFCCE5"),
            hex2Rgb("#FF99CC"),
            hex2Rgb("#FF66B2"),
            hex2Rgb("#FF3399"),
            hex2Rgb("#FF007F"),
            hex2Rgb("#CC0066"),

            //Blue
            hex2Rgb("#CCCCFF"),
            hex2Rgb("#9999FF"),
            hex2Rgb("#6666FF"),
            hex2Rgb("#3333FF"),
            hex2Rgb("#0000FF"),
            hex2Rgb("#0000CC"),

            //Green
            hex2Rgb("#CCFFCC"),
            hex2Rgb("#99FF99"),
            hex2Rgb("#66FF66"),
            hex2Rgb("#33FF33"),
            hex2Rgb("#00FF00"),
            hex2Rgb("#00CC00"),

            //Orange
            hex2Rgb("#FFE5CC"),
            hex2Rgb("#FFCC99"),
            hex2Rgb("#FFB266"),
            hex2Rgb("#FF9933"),
            hex2Rgb("#FF8000"),
            hex2Rgb("#CC6600")
    };
    public static Color[] ClusterColor = {
            Color.red,
            Color.cyan,
            Color.blue,
            Color.yellow,
            Color.pink,
            Color.orange,
            Color.gray,
            Color.green,
            Color.magenta,
            Color.black,
            Color.darkGray,
            Color.lightGray,
            Color.yellow.darker().darker(),
            Color.cyan.darker().darker(),
            Color.orange.darker().darker()
    };

    private static HashMap<String, HashSet<termCluster>> termClusters;

    /**
     *
     * @param colorStr e.g. "#FFFFFF"
     * @return
     */
    public static Color hex2Rgb(String colorStr) {
        return new Color(
                Integer.valueOf( colorStr.substring( 1, 3 ), 16 ),
                Integer.valueOf( colorStr.substring( 3, 5 ), 16 ),
                Integer.valueOf( colorStr.substring( 5, 7 ), 16 ) );
    }


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
        datePanel.add(new JButton(new PlotAnomalyData()));

        toolbar.add(datePanel);

        JPanel userPanel = new JPanel();
        userid = new JTextField("300000, 0.0005, 0.5, 2, 0, 10");//new JTextField("108435619");
        enableAnomaly = new JCheckBox("Plot anomaly", false);
        userPanel.add(userid);
        userPanel.add(enableAnomaly);
        userPanel.add(new JButton(new PlotUserData()));
        userPanel.add(new JButton(new PlotWordData()));
        userPanel.add(new JButton(new PlotDeducedTweetLocation()));

        toolbar.add(userPanel);

        JPanel coorPanel = new JPanel();
        coordinates = new JTextField("144.9673, 144.9675, -37.8154, -37.8152, 10");//144.84903932, -37.66990267
        //144.962, 144.969, -37.819, -37.812
        coorPanel.add(coordinates);
        coorPanel.add(new JButton(new PlotLocationUserData()));

        toolbar.add(coorPanel);

//        toolbar.add(new JButton(new ExportShapefileAction()));

        // Display the map frame. When it is closed the application will exit
        mapFrame.setSize(800, 600);
        mapFrame.setVisible(true);

        // Now display the map
//        mapFrame.showMap(map);

        /*SimpleFeatureTypeBuilder b = new SimpleFeatureTypeBuilder();

        b.setName("MyFeatureType");
        b.setCRS(DefaultGeographicCRS.WGS84);
        b.add("location", TrajectoryPoint.class);
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
            TrajectoryPoint point = geometryFactory.createPoint(new Coordinate(Double.parseDouble(coordinates[0]), Double.parseDouble(coordinates[1])));

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
            trajectorylayer = null;
        }
        if (userlayer != null) {
            map.removeLayer(userlayer);
        }
        if (clusterLayers != null && clusterLayers.size() > 0) {
            for (Layer clusterLayer : clusterLayers) {
                map.removeLayer(clusterLayer);
            }
            clusterLayers = null;
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

            clearLayers();
            clusterLayers = new ArrayList<Layer>();

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


            String temp = coordinates.getText();
            double t = 10;
            String[] coordinates1 = temp.split(", ");
            if (coordinates1.length == 5) {
                double[] coor = {Double.parseDouble(coordinates1[0]), Double.parseDouble(coordinates1[1]),
                        Double.parseDouble(coordinates1[2]), Double.parseDouble(coordinates1[3])};
                List<BasicDBObject> andArray = new ArrayList<BasicDBObject>();
                andArray.add(new BasicDBObject("coordinates.0", BasicDBObjectBuilder.start("$gte", coor[0])
                        .add("$lte", coor[1]).get()));
                andArray.add(new BasicDBObject("coordinates.1", BasicDBObjectBuilder.start("$gte", coor[2])
                        .add("$lte", coor[3]).get()));
                query.put("$and", andArray);

                String area = coordinates1[0] + "," + coordinates1[1] + "," + coordinates1[2] + "," + coordinates1[3];

                if (kelpFusion == null || !area.equals(kelpFusion.getArea())) {
                    kelpFusion = new KelpFusion(area, map.getCoordinateReferenceSystem());
                }

                t = Double.parseDouble(coordinates1[4]);
            }

            DBCursor dbCursor = collection.find(query).sort(new BasicDBObject(/*"userid", 1).append(*/"timestamp",1));

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
            b.add("color", String.class);
            // building the type
            final SimpleFeatureType TYPE = b.buildFeatureType();

            SimpleFeatureBuilder featureBuilder;
            GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
            DefaultFeatureCollection featureCollection = new DefaultFeatureCollection("internal", TYPE);

            DefaultFeatureCollection lineCollection = new DefaultFeatureCollection();
            DefaultFeatureCollection lineCollection1 = new DefaultFeatureCollection();

            int dataLength = dbCursor.count();
            int[] comp = {0, 0, 0, 0, 0, 0};
            userSets = new HashMap<Integer, ArrayList<TweetPOJO>>();
//
//            int previousUserID = 0;
//            ArrayList<Coordinate> userCoords = new ArrayList<Coordinate>();
            ArrayList<TrajectoryPoint> pointSet = new ArrayList<TrajectoryPoint>();

            while (dbCursor.hasNext() /*&& count < 6  && daycount < 3*/) {
                BasicDBObject basicObject = (BasicDBObject) dbCursor.next();

                String tmp = basicObject.get("coordinates").toString();
                tmp = tmp.substring(2, tmp.length() - 1);
//            System.out.println("" + tmp);
                String[] coordinates = tmp.split(",");
                Coordinate userCoord = new Coordinate(Double.parseDouble(coordinates[0]), Double.parseDouble(coordinates[1]));
                Point point = geometryFactory.createPoint(userCoord);

                featureBuilder = new SimpleFeatureBuilder(TYPE);
                featureBuilder.add(point);

                int userid = basicObject.getInt("userid");

//                if (previousUserID == userid) {
//                    userCoords.add(userCoord);
//                }
//                else {
//                    if (userCoords.size() > 1) {
//                        Coordinate[] coords = userCoords.toArray(new Coordinate[userCoords.size()]);
//                        SimpleFeature linefeature = getLineFeatureByCoord(coords);
//                        lineCollection.add(linefeature);
//                    }
//                    previousUserID = userid;
//                }

                featureBuilder.add(basicObject.getString("text"));
                featureBuilder.add(userid);
                featureBuilder.add(basicObject.getString("screen_name"));
                featureBuilder.add(basicObject.getString("created_at"));
                featureBuilder.add(basicObject.getString("tweet_id"));

                long timestamp = basicObject.getLong("timestamp");
                double[] location = {Double.parseDouble(coordinates[0]), Double.parseDouble(coordinates[1])};

                TweetPOJO tweet = new TweetPOJO(location, basicObject.getString("text"),
                        userid, basicObject.getString("screen_name"),
                        basicObject.getString("created_at"), basicObject.getLong("tweet_id"), timestamp);

                TrajectoryPoint trajPoint = new TrajectoryPoint(location[0], location[1],
                        basicObject.getLong("tweet_id"), timestamp);
                pointSet.add(trajPoint);

//                if (userSets.containsKey(userid)) {
//                    userSets.get(userid).add(tweet);
//                } else {
//                    ArrayList<TweetPOJO> userSet = new ArrayList<TweetPOJO>();
//                    userSet.add(tweet);
//                    userSets.put(userid, userSet);
//                }

                Calendar date = Calendar.getInstance();
                date.setTime(new Date(timestamp));
                date.setTimeZone(TimeZone.getTimeZone("Australia/Victoria"));
                int hourOfDay = date.get(Calendar.HOUR_OF_DAY);

                int pos = hourOfDay / 4;
                comp[pos] ++;
                featureBuilder.add(shortColors4Day[pos]);

                SimpleFeature feature = featureBuilder.buildFeature(null);
                featureCollection.add(feature);
            }

            for (int i = 0; i < comp.length; i++) {
                System.out.println(" comp " + i + " = " + comp[i]);
            }

            mongoClient.close();
            DateFormat df1 = new SimpleDateFormat("EEE MMM dd HH:mm:ss ZZZZZ yyyy");

            ArrayList<Line> SPG = kelpFusion.GetShortestPathGraph(pointSet, t);
//            ArrayList<Line> G = kelpFusion.createReachabilityGraph(pointSet);
//            ArrayList<Line> DG = kelpFusion.createDelaunayGraph(pointSet);
            HashMap<String, Line> lineSet = kelpFusion.lineSet;

//            for (int i = 0; i < /*20*/DG.size(); i++) {
//                Line partition = DG.get(i);

                /*Coordinate coord = new Coordinate(partition.getCenterPoint().getX(), partition.getCenterPoint().getY());
                Point point = geometryFactory.createPoint(coord);
                featureBuilder = new SimpleFeatureBuilder(TYPE);
                featureBuilder.add(point);
                featureBuilder.add("DG_ID = " + i);
                featureBuilder.add(1);
                featureBuilder.add("line id = " + partition.getID());
                featureBuilder.add("created_at");
                featureBuilder.add("weight = ");
                featureBuilder.add((partition.getID() == 100) ? Color.magenta : Color.cyan);

                SimpleFeature feature = featureBuilder.buildFeature("line" + i);
                featureCollection.add(feature);*/

//                SimpleFeature linefeature = getLineFeatureByCoord(partition.getCoordinates());
//                lineCollection1.add(linefeature);
//            }

            int n = 0;
            for (Line line : lineSet.values()) {

                Coordinate coord = new Coordinate(line.getCenterPoint().getX(), line.getCenterPoint().getY());
                Point point = geometryFactory.createPoint(coord);
                featureBuilder = new SimpleFeatureBuilder(TYPE);
                featureBuilder.add(point);
                featureBuilder.add("DG_ID = " + n);
                featureBuilder.add(1);
                featureBuilder.add("line id = " + line.getID());
                featureBuilder.add("created_at");
                featureBuilder.add("neighbours = " + line.getAdjacentNeighbours()[0] + " , " +
                        line.getAdjacentNeighbours()[1]);
                featureBuilder.add(Color.cyan);

                SimpleFeature feature = featureBuilder.buildFeature("line" + n);
                featureCollection.add(feature);

                SimpleFeature linefeature = getLineFeatureByCoord(line.getCoordinates());
                lineCollection1.add(linefeature);
                ++n;
            }


            Style linestyle2 = createLineStyle(Color.red);
            clusterLayers.add(new FeatureLayer(lineCollection1, linestyle2));
//            if (enableAnomaly.isSelected()) {
            map.addLayer(clusterLayers.get(0));
//            }

            for (int i = 0; i < /*20*/SPG.size(); i++) {
                Line partition = SPG.get(i);

                /*Coordinate coord = new Coordinate(partition.getCenterPoint().getX(), partition.getCenterPoint().getY());
                Point point = geometryFactory.createPoint(coord);
                featureBuilder = new SimpleFeatureBuilder(TYPE);
                featureBuilder.add(point);
                featureBuilder.add("lineID = " + i);
                featureBuilder.add(1);
                featureBuilder.add("length = " + partition.getOrthodromicDistance());
                featureBuilder.add("created_at");
                featureBuilder.add("weight = " + partition.getWeight());
                featureBuilder.add(Color.cyan);

                SimpleFeature feature = featureBuilder.buildFeature("line" + i);
                featureCollection.add(feature);*/

                SimpleFeature linefeature = getLineFeatureByCoord(partition.getCoordinates());
                lineCollection.add(linefeature);
            }

            Style linestyle1 = createLineStyle(Color.BLUE);
            clusterLayers.add(new FeatureLayer(lineCollection, linestyle1));
            map.addLayer(clusterLayers.get(1));

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
                    filterFactory.literal(Color.BLACK), filterFactory.literal(1)));

//            mark.setFill(styleFactory.createFill(filterFactory.literal(Color.MAGENTA)));
            StyleBuilder sb = new StyleBuilder();
            mark.setFill(styleFactory.createFill(/*filterFactory.literal(Color.CYAN)*/
                    sb.attributeExpression("color")));

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

        private Style createLineStyle(Color color) {
            Style style = SLD.createLineStyle(color, 0.5F);
            return style;
        }

        private SimpleFeature getLineFeatureByCoord(Coordinate[] coords) throws SchemaException {
            GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
            LineString line = geometryFactory.createLineString(coords);
            SimpleFeatureType LINETYPE = DataUtilities.createType("test", "line", "the_geom:LineString");
            SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder((SimpleFeatureType) LINETYPE);
            featureBuilder.add(line);
            SimpleFeature feature = featureBuilder.buildFeature(null);

            return feature;
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
            int useridint = Integer.parseInt(userid.getText());

            SimpleFeatureTypeBuilder b = new SimpleFeatureTypeBuilder();

            b.setName("PlotUserDataFeatureType");
            b.setCRS(DefaultGeographicCRS.WGS84);
            b.add("location", Point.class);
            b.add("text", String.class);
            b.add("userid", Integer.class);
            b.add("username", String.class);
            b.add("created_at", String.class);
            b.add("tweet_id", Double.class);
            b.add("color", String.class);
            b.add("timestamp", Long.class);
            b.add("size", Integer.class);
            // building the type
            final SimpleFeatureType TYPE = b.buildFeatureType();

            SimpleFeatureBuilder featureBuilder;
            GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
            DefaultFeatureCollection featureCollection = new DefaultFeatureCollection("internal", TYPE);

            DefaultFeatureCollection lineCollection = new DefaultFeatureCollection();

            int dataLength = 0;
            int count = 0;

            if (useridint <= 20 ){
                for (int userid: userSets.keySet()) {
                    ArrayList<TweetPOJO> userset = userSets.get(userid);

                    if (userset.size() == useridint)
                        continue;
                    dataLength += userset.size();
                    for (TweetPOJO object: userset) {
                        double[] coordinates = object.getLocation();
                        Coordinate coord = new Coordinate(coordinates[0], coordinates[1]);

                        Point point = geometryFactory.createPoint(coord);

                        featureBuilder = new SimpleFeatureBuilder(TYPE);
                        featureBuilder.add(point);
                        featureBuilder.add(object.getText());
                        featureBuilder.add(userid);
                        featureBuilder.add(object.getScreen_name());
                        featureBuilder.add(object.getCreated_at());
                        featureBuilder.add(object.getTweet_id());

                        long timestamp = object.getTimestamp();

                        Calendar date = Calendar.getInstance();
                        date.setTime(new Date(timestamp));
                        date.setTimeZone(TimeZone.getTimeZone("Australia/Victoria"));
                        int hourOfDay = date.get(Calendar.HOUR_OF_DAY);

                        int pos = hourOfDay / 4;
                        featureBuilder.add(shortColors4Day[pos]);
                        featureBuilder.add(timestamp);
                        featureBuilder.add(5);

                        SimpleFeature feature = featureBuilder.buildFeature("" + /*basicObject.getLong("tweet_id")*/count);
                        featureCollection.add(feature);
                        count++;
                    }
                }
                trajectorylayer = null;
            } else {

                //connecting mongoDB on local machine.
                MongoClient mongoClient = new MongoClient("localhost", 27017);
                //connecting to database named test
                DB db = mongoClient.getDB("test");
                // getting collection of all files
                DBCollection collection = db.getCollection("correctTweetId");

                BasicDBObject query = new BasicDBObject();
                query.put("userid", useridint);
                query.put("timestamp", BasicDBObjectBuilder.start("$gte", startDate.getTime()).add("$lte", endDate.getTime()).get());

                DBCursor dbCursor = collection.find(query).sort(new BasicDBObject("timestamp", 1));

                dataLength = dbCursor.count();

                ArrayList<Coordinate> userCoords = new ArrayList<Coordinate>();
                long previousTimestamp = 0;
                String previousDate = "";
                long timeThreshold = 24 * 3600 * 1000;
                int linecount = 0;

                while (dbCursor.hasNext() /*&& count < 6  && daycount < 3*/) {
                    BasicDBObject basicObject = (BasicDBObject) dbCursor.next();

                    String tmp = basicObject.get("coordinates").toString();
                    tmp = tmp.substring(2, tmp.length() - 1);
//            System.out.println("" + tmp);
                    String[] coordinates = tmp.split(",");
//                TrajectoryPoint point = geometryFactory.createPoint(new Coordinate(Double.parseDouble(coordinates[0]), Double.parseDouble(coordinates[1])));
                    Coordinate coord = new Coordinate(Double.parseDouble(coordinates[0]), Double.parseDouble(coordinates[1]));

                    Point point = geometryFactory.createPoint(coord);

                    featureBuilder = new SimpleFeatureBuilder(TYPE);
                    featureBuilder.add(point);
                    featureBuilder.add(basicObject.getString("text"));
                    featureBuilder.add(useridint);
                    String date1 = basicObject.getString("created_at");
                    featureBuilder.add(basicObject.getString("screen_name"));
                    featureBuilder.add(basicObject.getString("created_at"));
                    featureBuilder.add(basicObject.getLong("tweet_id"));

                    long timestamp = basicObject.getLong("timestamp");

                    Calendar date = Calendar.getInstance();
                    date.setTime(new Date(timestamp));
                    date.setTimeZone(TimeZone.getTimeZone("Australia/Victoria"));
                    int hourOfDay = date.get(Calendar.HOUR_OF_DAY);

                    int pos = hourOfDay / 4;
                    featureBuilder.add(shortColors4Day[pos]);
                    featureBuilder.add(timestamp);
                    featureBuilder.add(10);

                    SimpleFeature feature = featureBuilder.buildFeature("" + /*basicObject.getLong("tweet_id")*/count);
                    featureCollection.add(feature);

                    if (timestamp - previousTimestamp <= timeThreshold) {
                        userCoords.add(coord);
                    }
                    else {
                        if (userCoords.size() > 1) {
                            Coordinate[] coords = userCoords.toArray(new Coordinate[userCoords.size()]);
                            SimpleFeature linefeature = getLineFeatureByCoord(coords);
                            lineCollection.add(linefeature);
                            linecount++;
                        }
                        userCoords = new ArrayList<Coordinate>();
                        userCoords.add(coord);
                    }
                    previousTimestamp = timestamp;
                    previousDate = date1;
                    count++;
                }

                if (userCoords.size() > 1) {
                    Coordinate[] coords = userCoords.toArray(new Coordinate[userCoords.size()]);
                    SimpleFeature linefeature = getLineFeatureByCoord(coords);
                    lineCollection.add(linefeature);
                    linecount++;
                }
                System.out.println("line count = " + linecount);

                mongoClient.close();

//                if (dataLength > 1)
//                    trajectorylayer = getLayerLineByCoord(coords);
            }

            Style style = createPointStyle();
            querylayer = new FeatureLayer(featureCollection, style);
            map.addLayer(querylayer);

            Style linestyle = createLineStyle();
            trajectorylayer = new FeatureLayer(lineCollection, linestyle);

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

        private Style createLineStyle() {
            Style style = SLD.createLineStyle(Color.GREEN, 0.1F);
            return style;
        }

        private SimpleFeature getLineFeatureByCoord(Coordinate[] coords) throws SchemaException {
            GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
            LineString line = geometryFactory.createLineString(coords);
            SimpleFeatureType LINETYPE = DataUtilities.createType("test", "line", "the_geom:LineString");
            SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder((SimpleFeatureType) LINETYPE);
            featureBuilder.add(line);
            SimpleFeature feature = featureBuilder.buildFeature(null);

            return feature;
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
                    filterFactory.literal(Color.BLACK), filterFactory.literal(1)));

//            mark.setFill(styleFactory.createFill(filterFactory.literal(Color.MAGENTA)));
            StyleBuilder sb = new StyleBuilder();
            FilterFactory2 ff = sb.getFilterFactory();
            mark.setFill(styleFactory.createFill(/*filterFactory.literal(Color.CYAN)*/
                    sb.attributeExpression("color")));

            gr.graphicalSymbols().clear();
            gr.graphicalSymbols().add(mark);
            gr.setSize(ff.property("size"));

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

    static class PlotWordData extends SafeAction {

        PlotWordData() {
            super("Word");
            putValue(Action.SHORT_DESCRIPTION, "Check each geometry");
        }

        public void action(ActionEvent e) throws Throwable {
            Date startDate = startdatePanel.GetDate();
            Date endDate = enddatePanel.GetDate();
            String temp = coordinates.getText();

            String[] restictionsString = temp.split(", ");
            int[] restrictions = {Integer.parseInt(restictionsString[0]), Integer.parseInt(restictionsString[1])};

            String word = userid.getText();

            clearLayers();

            if (enableAnomaly.isSelected()) {
                if (termClusters == null) {
                    termClusters = AnomalyCluster.getAnomalyset(startDate, endDate);
                }

                SimpleFeatureTypeBuilder b = new SimpleFeatureTypeBuilder();

                b.setName("PlotUserDataFeatureType");
                b.setCRS(DefaultGeographicCRS.WGS84);
                b.add("location", Point.class);
                b.add("text", String.class);
                b.add("color", String.class);
                b.add("power", Integer.class);
                b.add("Score", Float.class);
                b.add("size", Integer.class);
//            b.add("username", String.class);
//            b.add("created_at", String.class);
//            b.add("tweet_id", Double.class);
                // building the type
                final SimpleFeatureType TYPE = b.buildFeatureType();

                SimpleFeatureBuilder featureBuilder;
                GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
                DefaultFeatureCollection featureCollection = new DefaultFeatureCollection("internal", TYPE);

                int dataLength = termClusters.get(word).size();
                int count = 0;

                for (termCluster clust: termClusters.get(word)) {
                        Point point = geometryFactory.createPoint(new Coordinate(clust.getCentroid()[0], clust.getCentroid()[1]));

                        featureBuilder = new SimpleFeatureBuilder(TYPE);
                        featureBuilder.add(point);
                        String text = "";
                        for (extractedTerm term : clust.getReg()){
                            text += "user = " + term.getUserID() + ", text = " + term.getText()  + ", date = " + term.getCreated_at() + "\n";
                            if (text.length() >= 700) {
                                break;
                            }
                        }
                        featureBuilder.add(text);
                        featureBuilder.add((clust.getReg().size() % 2 == 0) ? Color.red : Color.green);
                        featureBuilder.add(clust.getReg().size() / AnomalyCluster.getNumOfAnalyzedDocs());
                        featureBuilder.add(clust.getScore() / AnomalyCluster.getNumOfAnalyzedDocs());
                        featureBuilder.add(50);
//                featureBuilder.add(basicObject.getString("screen_name"));
//                featureBuilder.add(basicObject.getString("created_at"));
//                featureBuilder.add(basicObject.getLong("tweet_id"));

                        SimpleFeature feature = featureBuilder.buildFeature("" + /*basicObject.getLong("tweet_id")*/count);
                        featureCollection.add(feature);
                        count++;
//                    }
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
                return;
            }

            //connecting mongoDB on local machine.
            MongoClient mongoClient = new MongoClient("localhost", 27017);
            //connecting to database named test
            DB db = mongoClient.getDB("test");
            // getting collection of all files
            DBCollection collection = db.getCollection("correctTweetId");

            BasicDBObject query = new BasicDBObject();

            List<Integer> list = new ArrayList<Integer>();
            list.add(504409867);
            list.add(18147028); //vicRoads
            list.add(277717138); //NewZealandRoads

            query.put("text", Pattern.compile(word, Pattern.CASE_INSENSITIVE));
            query.put("timestamp", BasicDBObjectBuilder.start("$gte", startDate.getTime()).add("$lte", endDate.getTime()).get());
            query.put("userid",  BasicDBObjectBuilder.start("$not", new BasicDBObject("$in", list)).get());

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
            b.add("color", String.class);
            b.add("opacity", Integer.class);
            b.add("size", Integer.class);
            // building the type
            final SimpleFeatureType TYPE = b.buildFeatureType();

            SimpleFeatureBuilder featureBuilder;
            GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
            DefaultFeatureCollection featureCollection = new DefaultFeatureCollection("internal", TYPE);

            DefaultFeatureCollection lineCollection = new DefaultFeatureCollection();

            ArrayList<Coordinate> userCoords = new ArrayList<Coordinate>();
            long previousTimestamp = 0;
            String previousDate = "";
            long timeThreshold = 24 * 3600 * 1000;
            int linecount = 0;

            int dataLength = dbCursor.count();
            int count = 0;

            Coordinate[] coords  = new Coordinate[dataLength];
            HashSet<Integer> userids = new HashSet<Integer>();

            while (dbCursor.hasNext() /*&& count < 6  && daycount < 3*/) {
                BasicDBObject basicObject = (BasicDBObject) dbCursor.next();

                String tmp = basicObject.get("coordinates").toString();
                tmp = tmp.substring(2, tmp.length() - 1);
//            System.out.println("" + tmp);
                String[] coordinates = tmp.split(",");
//                TrajectoryPoint point = geometryFactory.createPoint(new Coordinate(Double.parseDouble(coordinates[0]), Double.parseDouble(coordinates[1])));
                Coordinate coord = new Coordinate(Double.parseDouble(coordinates[0]), Double.parseDouble(coordinates[1]));

                Point point = geometryFactory.createPoint(coord);
//                coords[count] = coord;

                featureBuilder = new SimpleFeatureBuilder(TYPE);
                featureBuilder.add(point);
                featureBuilder.add(basicObject.getString("text"));
                int userid = basicObject.getInt("userid");
                long tweetID = basicObject.getLong("tweet_id");
                if (!userids.contains(userid)) {

                    //connecting mongoDB on local machine.
//                    MongoClient mongoClient1 = new MongoClient("localhost", 27017);
                    //connecting to database named test
//                    DB db1 = mongoClient1.getDB("test");
                    // getting collection of all files
//                    DBCollection collection1 = db1.getCollection("correctTweetId");

                    BasicDBObject query1 = new BasicDBObject();
                    query1.put("userid", userid);
                    query1.put("timestamp", BasicDBObjectBuilder.start("$gte", startDate.getTime()).add("$lte", endDate.getTime()).get());

                    DBCursor dbCursor1 = collection.find(query1).sort(new BasicDBObject("timestamp", 1));

                    int dataLength1 = dbCursor1.count();
                    int count1 = 0;


                    while (dbCursor1.hasNext() /*&& count < 6  && daycount < 3*/) {
                        BasicDBObject basicObject1 = (BasicDBObject) dbCursor1.next();

                        String tmp1 = basicObject1.get("coordinates").toString();
                        tmp1 = tmp1.substring(2, tmp1.length() - 1);
//            System.out.println("" + tmp1);
                        String[] coordinates1 = tmp1.split(",");
//                TrajectoryPoint point = geometryFactory.createPoint(new Coordinate(Double.parseDouble(coordinates1[0]), Double.parseDouble(coordinates1[1])));
                        Coordinate coord1 = new Coordinate(Double.parseDouble(coordinates1[0]), Double.parseDouble(coordinates1[1]));

                        Point point1 = geometryFactory.createPoint(coord1);

                        featureBuilder = new SimpleFeatureBuilder(TYPE);
                        featureBuilder.add(point1);
                        featureBuilder.add(basicObject1.getString("text"));
                        featureBuilder.add(userid);
                        featureBuilder.add(basicObject1.getString("screen_name"));
                        featureBuilder.add(basicObject1.getString("created_at"));
                        long tweetID1 = basicObject1.getLong("tweet_id");
                        featureBuilder.add(basicObject1.getLong("tweet_id"));

                        long timestamp = basicObject1.getLong("timestamp");

                        Calendar date = Calendar.getInstance();
                        date.setTime(new Date(timestamp));
                        date.setTimeZone(TimeZone.getTimeZone("Australia/Victoria"));
                        int hourOfDay = date.get(Calendar.HOUR_OF_DAY);

                        int pos = hourOfDay / 4;
                        featureBuilder.add((tweetID == tweetID1) ? Color.GREEN :shortColors4Day[pos]);
                        featureBuilder.add(1);
                        featureBuilder.add(10);

                        SimpleFeature feature = featureBuilder.buildFeature(null);
                        featureCollection.add(feature);
                        count1++;

                        if (timestamp - previousTimestamp <= restrictions[0] &&
                                coord1.distance(userCoords.get(userCoords.size() - 1)) <= restrictions[1]) {
                            userCoords.add(coord1);
                        }
                        else {
                            if (userCoords.size() > 1) {
                                Coordinate[] coords1 = userCoords.toArray(new Coordinate[userCoords.size()]);
                                SimpleFeature linefeature = getLineFeatureByCoord(coords1);
                                lineCollection.add(linefeature);
                                linecount++;
                            }
                            userCoords = new ArrayList<Coordinate>();
                            userCoords.add(coord1);
                        }
                        previousTimestamp = timestamp;
                    }
                    if (userCoords.size() > 1) {
                        Coordinate[] coords1 = userCoords.toArray(new Coordinate[userCoords.size()]);
                        SimpleFeature linefeature = getLineFeatureByCoord(coords1);
                        lineCollection.add(linefeature);
                        linecount++;
                    }

                    userids.add(userid);
                } else {
                    continue;
                }
                featureBuilder.add(userid);
                featureBuilder.add(basicObject.getString("screen_name"));
                featureBuilder.add(basicObject.getString("created_at"));
                featureBuilder.add(basicObject.getLong("tweet_id"));

                previousTimestamp = 0;

//                SimpleFeature feature = featureBuilder.buildFeature("" + /*basicObject.getLong("tweet_id")*/count);
//                featureCollection.add(feature);
                count++;
            }

            mongoClient.close();

            Style style = createPointStyle();
            querylayer = new FeatureLayer(featureCollection, style);
            map.addLayer(querylayer);

            Style linestyle = createLineStyle();
            trajectorylayer = new FeatureLayer(lineCollection, linestyle);
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

        private Style createLineStyle() {
            Style style = SLD.createLineStyle(Color.GREEN, 0.1F);
            return style;
        }

        private SimpleFeature getLineFeatureByCoord(Coordinate[] coords) throws SchemaException {
            GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
            LineString line = geometryFactory.createLineString(coords);
            SimpleFeatureType LINETYPE = DataUtilities.createType("test", "line", "the_geom:LineString");
            SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder((SimpleFeatureType) LINETYPE);
            featureBuilder.add(line);
            SimpleFeature feature = featureBuilder.buildFeature(null);

            return feature;

//            DefaultFeatureCollection lineCollection = new DefaultFeatureCollection();
//            lineCollection.add(feature);
//
//            Style style = SLD.createLineStyle(Color.BLACK, 0.1F);
//            return new FeatureLayer(lineCollection, style);
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

//            mark.setFill(styleFactory.createFill(filterFactory.literal(Color.CYAN)));
            StyleBuilder sb = new StyleBuilder();
            FilterFactory2 ff = sb.getFilterFactory();
            mark.setFill(styleFactory.createFill(/*filterFactory.literal(Color.CYAN)*/
                    sb.attributeExpression("color"), filterFactory.literal(ff.property("opacity")/*0.5*/)));

            gr.graphicalSymbols().clear();
            gr.graphicalSymbols().add(mark);
            gr.setSize(ff.property("size"));

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

            clearLayers();

            //connecting mongoDB on local machine.
            MongoClient mongoClient = new MongoClient("localhost", 27017);
            //connecting to database named test
            DB db = mongoClient.getDB("test");
            // getting collection of all files
            DBCollection collection = db.getCollection("correctTweetId");

            BasicDBObject query = new BasicDBObject();
            String temp = coordinates.getText();
            double t = 3;

            String[] coordinates1 = temp.split(", ");
            double[] coor = {Double.parseDouble(coordinates1[0]), Double.parseDouble(coordinates1[1])};
            if (coordinates1.length == 2) {
                query.put("coordinates", coor);
            }
            else if (coordinates1.length >= 4) {
                double[] coor1 = {Double.parseDouble(coordinates1[2]), Double.parseDouble(coordinates1[3])};
                //144.23, 146.47, -38.71, -36.90
                //144.96, 144.98, -37.82, -37.81
                //144.91, 145.03, -37.84, -37.77
                //144.93, 144.99, -37.83, -37.79 - Melbourne CBD
                //144.96, 144.97, -37.82, -37.81
                //144.962, 144.969, -37.819, -37.812
                //138.46, 138.74, -34.99, -34.83 - Adelaide
                //150.64, 151.42, -34.04, -33.58 - sydney
                //160.55, 184.32, -47.77, -33.93 - NZ
                //141.37, 147.80, -39.39, -35.81
                //144.84, 145.07, -37.88, -37.75
                //150.48, 151.49, -34.13, -33.57
                //144.95, 144.98, -37.82, -37.81
                //144.975, 144.99, -37.822, -37.81 - MCG
                //road 144.955, 144.965, -37.82, -37.80
                //144.65, 144.94, -37.89, -37.74 - Docklands

//                collingwood|magpies|gopies|afl|pies
//                collingwood|magpies|gopies|pies

                List<BasicDBObject> andArray = new ArrayList<BasicDBObject>();
                andArray.add(new BasicDBObject("coordinates.0", BasicDBObjectBuilder.start("$gte", coor[0])
                        .add("$lte", coor[1]).get()));
                andArray.add(new BasicDBObject("coordinates.1", BasicDBObjectBuilder.start("$gte", coor1[0])
                        .add("$lte", coor1[1]).get()));
                query.put("$and", andArray);

                String area = coordinates1[0] + "," + coordinates1[1] + "," + coordinates1[2] + "," + coordinates1[3];

                if (kelpFusion == null || !area.equals(kelpFusion.getArea())) {
                    kelpFusion = new KelpFusion(area, map.getCoordinateReferenceSystem());
                }

                if (coordinates1.length == 5) {
                    t = Double.parseDouble(coordinates1[4]);
                }
            }
            query.put("timestamp", BasicDBObjectBuilder.start("$gte", startDate.getTime()).add("$lte", endDate.getTime()).get());

            DBCursor dbCursor = collection.find(query).sort(new BasicDBObject("userid", 1).append("timestamp", 1));

            SimpleFeatureTypeBuilder b = new SimpleFeatureTypeBuilder();

            b.setName("PlotUserDataFeatureType");
            b.setCRS(DefaultGeographicCRS.WGS84);
            b.add("location", Point.class);
            b.add("text", String.class);
            b.add("color", String.class);
            b.add("userid", Integer.class);
            b.add("username", String.class);
            b.add("created_at", String.class);
            b.add("tweet_id", Double.class);
            b.add("color", String.class);
            b.add("timestamp", Long.class);
            b.add("size", Integer.class);
            // building the type
            final SimpleFeatureType TYPE = b.buildFeatureType();

            SimpleFeatureBuilder featureBuilder;
            GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
            DefaultFeatureCollection featureCollection = new DefaultFeatureCollection("internal", TYPE);
            DefaultFeatureCollection lineCollection = new DefaultFeatureCollection();

            int dataLength = dbCursor.count();
            int count = 0;

            ArrayList<Coordinate> userCoords = new ArrayList<Coordinate>();
            ArrayList<Line> lineSet = new ArrayList<Line>();
            ArrayList<TrajectoryPoint> userPoints = new ArrayList<TrajectoryPoint>();
            long previousTimestamp = 0;
            int previousUserId = 0;
            long previousTweetId = 0;
            TrajectoryPoint previousPoint = null;
            double[] previousLocation = new double[2];
            ArrayList<TrajectoryPoint> pointSet = new ArrayList<TrajectoryPoint>();

            temp = userid.getText();
            String[] params = temp.split(", ");

            long timeThreshold;
            try {
                timeThreshold = Long.parseLong(params[0]);
            } catch (Exception e1) {
                timeThreshold = 3600 * 500;
            }

            double eph;
            try {
                eph = Double.parseDouble(params[1]);
            } catch (Exception e1) {
                eph = 50;
            }

            double sineTheta; //10 degrees = 0.17364817766
            try {
                sineTheta = Double.parseDouble(params[2]);
            } catch (Exception e1) {
                sineTheta = 0.17364817766;
            }

            int minLn;
            try {
                minLn = Integer.parseInt(params[3]);
            } catch (Exception e1) {
                minLn = 2;
            }

            int minIt,maxIt;
            try {
                minIt = Integer.parseInt(params[4]);
            } catch (Exception e1) {
                minIt = 0;
            }
            try {
                maxIt = Integer.parseInt(params[5]);
            } catch (Exception e1) {
                maxIt = 1;
            }



            int linecount = 0;
            int trajectoryLineID = 0;

            while (dbCursor.hasNext() /*&& count < 6  && daycount < 3*/) {

                BasicDBObject basicObject = (BasicDBObject) dbCursor.next();

                int userid = basicObject.getInt("userid");
                long timestamp = basicObject.getLong("timestamp");
                long tweetID = basicObject.getLong("tweet_id");

                Coordinate coord;
                if (coordinates1.length == 2) {
                    coord = new Coordinate(coor[0], coor[1]);
                }
                else {
                String tmp = basicObject.get("coordinates").toString();
                tmp = tmp.substring(2, tmp.length() - 1);
                String[] coordinates = tmp.split(",");

                    double[] Location = new double[2];
                    Location[0] = Double.parseDouble(coordinates[0]);
                    Location[1] = Double.parseDouble(coordinates[1]);

                    coord = new Coordinate(Location[0], Location[1]);
//                    TrajectoryPoint point = new TrajectoryPoint(Location[0], Location[1], tweetID, timestamp);

                    Double distance = getDistance(Location, previousLocation);
                    pointSet.add(new TrajectoryPoint(Location[0], Location[1], tweetID, timestamp));
                    if (previousUserId == userid
                            && timestamp - previousTimestamp <= timeThreshold
                            && distance > 0
                            && distance <= 0.05) {
                        userCoords.add(coord);
                        userPoints.add(new TrajectoryPoint(Location[0], Location[1], tweetID, timestamp));


                        //to create lines for TACLUST without partitioning
//                        Line line = new Line(trajectoryLineID , previousPoint, point);
//                        lineSet.add(line);
//                        ++trajectoryLineID;
                    }
                    else {
                        if (userCoords.size() > 1) {
                            Coordinate[] coords = userCoords.toArray(new Coordinate[userCoords.size()]);
                            //TO create line partitions for TRACLUST
                            if (userCoords.size() >= 2) {
                                //partition 1 or more lines then add to the line set
                                //to test partitioning 144.967, 144.968, -37.816, -37.815
                                ArrayList<Line> partitionSet = TRACLUST.partitionTrajectories(trajectoryLineID, userPoints);
                                lineSet.addAll(partitionSet);
                                trajectoryLineID += partitionSet.size();
                            }
                            SimpleFeature linefeature = getLineFeatureByCoord(coords);
                            lineCollection.add(linefeature);
                            linecount++;
                        }
                        userCoords = new ArrayList<Coordinate>();
                        userCoords.add(coord);
                        userPoints = new ArrayList<TrajectoryPoint>();
                        userPoints.add(new TrajectoryPoint(Location[0], Location[1], tweetID, timestamp));
                    }
                    previousTimestamp = timestamp;
                    previousUserId = userid;
                    previousLocation = Location;
//                    previousPoint = point;
                }

                Point point = geometryFactory.createPoint(coord);

                featureBuilder = new SimpleFeatureBuilder(TYPE);
                featureBuilder.add(point);
                featureBuilder.add(basicObject.getString("text"));
                featureBuilder.add(Color.BLACK);
                featureBuilder.add(userid);
                featureBuilder.add(basicObject.getString("screen_name"));
                featureBuilder.add(basicObject.getString("created_at"));
                featureBuilder.add(tweetID);


                Calendar date = Calendar.getInstance();
                date.setTime(new Date(timestamp));
                date.setTimeZone(TimeZone.getTimeZone("Australia/Victoria"));
                int hourOfDay = date.get(Calendar.HOUR_OF_DAY);

                int pos = hourOfDay / 4;
                featureBuilder.add(shortColors4Day[pos]);
                featureBuilder.add(timestamp);
                featureBuilder.add(5);

                SimpleFeature feature = featureBuilder.buildFeature("" + /*basicObject.getLong("tweet_id")*/count);
                featureCollection.add(feature);
                count++;

            }

            if (coordinates1.length >= 4 && userCoords.size() > 1) {
                Coordinate[] coords = userCoords.toArray(new Coordinate[userCoords.size()]);
                SimpleFeature linefeature = getLineFeatureByCoord(coords);
                lineCollection.add(linefeature);
                linecount++;
            }

//            ArrayList<ArrayList<Integer>> clusterSet = TRACLUST.clusterTrajectories(lineSet, eph, sineTheta, minLn);
//            ArrayList<TCMMmicroCluster> microClusters = TCMM.clusterTrajectories(lineSet, eph);

                    System.out.println("partition count = " + lineSet.size());
            ArrayList<Line> SPG = kelpFusion.GetShortestPathGraph(pointSet, t);
            ArrayList<Line> G = kelpFusion.createDelaunayGraph(pointSet);

//            System.out.println("TRACLUST cluster count = " + clusterSet.size());
//            System.out.println("TCMM cluster count = " + microClusters.size());

            mongoClient.close();



            Style linestyle = createLineStyle(Color.green);
            trajectorylayer = new FeatureLayer(lineCollection, linestyle);
            map.addLayer(trajectorylayer);

            clusterLayers = new ArrayList<Layer>();
            //To visualize TRACLUST results
            /*int iterations = (clusterSet.size() > maxIt) ? maxIt : clusterSet.size();
            for (int i = minIt; i < iterations; i++) {
                ArrayList<Integer> cluster = clusterSet.get(i);

                if (cluster.size() == 0) {
                    continue;
                }

                DefaultFeatureCollection lineCollection1 = new DefaultFeatureCollection();

                for (int lineID : cluster) {
                    Line line = lineSet.get(lineID);
                    SimpleFeature linefeature = getLineFeatureByCoord(line.getCoordinates());
                    lineCollection1.add(linefeature);
                }

                Style linestyle1 = createLineStyle(ClusterColor[i % ClusterColor.length]);
                clusterLayers.add(new FeatureLayer(lineCollection1, linestyle1));
                map.addLayer(clusterLayers.get(i - minIt));
            }*/
            //end of TRACLUST visualisation process

            DefaultFeatureCollection lineCollection1 = new DefaultFeatureCollection(),
                    lineCollection2 = new DefaultFeatureCollection();

            //To visualize trajectory partitions

            for (int i = 0; i < /*20*/G.size(); i++) {
                Line partition = G.get(i);

                SimpleFeature linefeature = getLineFeatureByCoord(partition.getCoordinates());
                lineCollection1.add(linefeature);
            }

            Style linestyle2 = createLineStyle(Color.red);
            clusterLayers.add(new FeatureLayer(lineCollection1, linestyle2));
            if (enableAnomaly.isSelected()) {
                map.addLayer(clusterLayers.get(0));
            }
            //end of visualizing trajectory partitions

            //To visualize TCMM micro cluster results

            //if plot anomaly checkbox is NOT checked
            /*if (!enableAnomaly.isSelected()) { //then display all microcluster representation lines
                for (int i = 0; i < microClusters.size(); i++) {
                    TCMMmicroCluster cluster = microClusters.get(i);
                    ArrayList<Integer> lineIDs = cluster.getLineIDs();

                    if (lineIDs.size() <= minLn) {
                        continue;
                    }


                    //create center point add add to query layer
                    Coordinate coord = new Coordinate(cluster.getCenterPoint().getX(), cluster.getCenterPoint().getY());
                    Point point = geometryFactory.createPoint(coord);

                    featureBuilder = new SimpleFeatureBuilder(TYPE);
                    featureBuilder.add(point);
                    featureBuilder.add("clusterID = " + i);
                    featureBuilder.add(Color.BLACK);
                    featureBuilder.add(1);
                    featureBuilder.add("angle = " + cluster.getTheta());
                    featureBuilder.add("N = " + lineIDs.size());
                    featureBuilder.add(1);
                    featureBuilder.add(Color.cyan);
                    featureBuilder.add(1);
                    featureBuilder.add(6);

                    SimpleFeature feature = featureBuilder.buildFeature("cluster" + i);
                    featureCollection.add(feature);

                    SimpleFeature linefeature = getLineFeatureByCoord(cluster.getCoordinates());
                    lineCollection2.add(linefeature);
                }
            } else { //else display all lines represented by micro cluster specified by minLn param
                TCMMmicroCluster cluster = microClusters.get(minLn);
                ArrayList<Integer> lineIDs = cluster.getLineIDs();
                //create center point add add to query layer
                Coordinate coord = new Coordinate(cluster.getCenterPoint().getX(), cluster.getCenterPoint().getY());
                Point point = geometryFactory.createPoint(coord);

                featureBuilder = new SimpleFeatureBuilder(TYPE);
                featureBuilder.add(point);
                featureBuilder.add("clusterID = " + minLn);
                featureBuilder.add(Color.BLACK);
                featureBuilder.add(1);
                featureBuilder.add("angle = " + cluster.getTheta());
                featureBuilder.add("N = " + lineIDs.size());
                featureBuilder.add(1);
                featureBuilder.add(Color.orange);
                featureBuilder.add(1);
                featureBuilder.add(10);

                SimpleFeature feature = featureBuilder.buildFeature("cluster" + minLn);
                featureCollection.add(feature);

                SimpleFeature linefeature = getLineFeatureByCoord(cluster.getCoordinates());
                lineCollection2.add(linefeature);

                for (int lineID : lineIDs) {
                    Line line = lineSet.get(lineID);

                    //create center point add add to query layer
                    coord = new Coordinate(line.getCenterPoint().getX(), line.getCenterPoint().getY());
                    point = geometryFactory.createPoint(coord);

                    featureBuilder = new SimpleFeatureBuilder(TYPE);
                    featureBuilder.add(point);
                    featureBuilder.add("lineID = " + lineID);
                    featureBuilder.add(Color.BLACK);
                    featureBuilder.add(1);
                    featureBuilder.add("angle = " + line.getTheta());
                    featureBuilder.add("created_at");
                    featureBuilder.add(1);
                    featureBuilder.add(Color.cyan);
                    featureBuilder.add(1);
                    featureBuilder.add(6);

                    feature = featureBuilder.buildFeature("line" + lineID);
                    featureCollection.add(feature);

                    SimpleFeature linefeature1 = getLineFeatureByCoord(line.getCoordinates());
                    lineCollection2.add(linefeature1);
                }
            }*/

//            Style linestyle1 = createLineStyle(Color.red);
//            clusterLayers.add(new FeatureLayer(lineCollection1, linestyle1));
//            map.addLayer(clusterLayers.get(0));
//
//            Style linestyle2 = createLineStyle(Color.blue);
//            clusterLayers.add(new FeatureLayer(lineCollection2, linestyle2));
//            map.addLayer(clusterLayers.get(1));


            //end of TCMM micro cluster visualisation process



            for (int i = 0; i < /*20*/SPG.size(); i++) {
                Line partition = SPG.get(i);

                /*Coordinate coord = new Coordinate(partition.getCenterPoint().getX(), partition.getCenterPoint().getY());
                Point point = geometryFactory.createPoint(coord);
                featureBuilder = new SimpleFeatureBuilder(TYPE);
                featureBuilder.add(point);
                featureBuilder.add("lineID = " + i);
                featureBuilder.add(1);
                featureBuilder.add("length = " + partition.getOrthodromicDistance());
                featureBuilder.add("created_at");
                featureBuilder.add("weight = " + partition.getWeight());
                featureBuilder.add(Color.cyan);*/

//                SimpleFeature feature = featureBuilder.buildFeature("line" + i);
//                featureCollection.add(feature);

                SimpleFeature linefeature = getLineFeatureByCoord(partition.getCoordinates());
                lineCollection2.add(linefeature);
            }

            Style linestyle1 = createLineStyle(Color.BLUE);
            clusterLayers.add(new FeatureLayer(lineCollection2, linestyle1));
            map.addLayer(clusterLayers.get(1));

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
                    filterFactory.literal(Color.BLACK), filterFactory.literal(1)));

//            mark.setFill(styleFactory.createFill(filterFactory.literal(Color.MAGENTA)));
            StyleBuilder sb = new StyleBuilder();
            FilterFactory2 ff = sb.getFilterFactory();
            mark.setFill(styleFactory.createFill(/*filterFactory.literal(Color.CYAN)*/
                    sb.attributeExpression("color")));

            gr.graphicalSymbols().clear();
            gr.graphicalSymbols().add(mark);
            gr.setSize(ff.property("size"));

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

        private Style createLineStyle(Color color) {
            Style style = SLD.createLineStyle(color, 0.1F);
            return style;
        }

        private SimpleFeature getLineFeatureByCoord(Coordinate[] coords) throws SchemaException {
            GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
            LineString line = geometryFactory.createLineString(coords);
            SimpleFeatureType LINETYPE = DataUtilities.createType("test", "line", "the_geom:LineString");
            SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder((SimpleFeatureType) LINETYPE);
            featureBuilder.add(line);
            SimpleFeature feature = featureBuilder.buildFeature(null);

            return feature;

//            DefaultFeatureCollection lineCollection = new DefaultFeatureCollection();
//            lineCollection.add(feature);
//
//            Style style = SLD.createLineStyle(Color.BLACK, 0.1F);
//            return new FeatureLayer(lineCollection, style);
        }

    }



    static class PlotAnomalyData extends SafeAction {

        PlotAnomalyData() {
            super("AnomalyRun");
            putValue(Action.SHORT_DESCRIPTION, "Check each geometry");
        }

        public void action(ActionEvent e) throws Throwable {

            //at the click of a button make query layer visible and not
            if (querylayer != null) {
                querylayer.setVisible(!querylayer.isVisible());
            }
            if (clusterLayers.get(1) != null) {
                clusterLayers.get(1).setVisible(!clusterLayers.get(1).isVisible());
            }
            return;
/*
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
            b.add("color", String.class);
            b.add("power", Integer.class);
            b.add("Score", Float.class);
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
                        featureBuilder.add((clust.getReg().size() % 2 == 0) ? Color.red : Color.green);
                featureBuilder.add(clust.getReg().size());
                featureBuilder.add(clust.getScore());
//                featureBuilder.add(basicObject.getString("screen_name"));
//                featureBuilder.add(basicObject.getString("created_at"));
//                featureBuilder.add(basicObject.getLong("tweet_id"));
*/
                   /*     SimpleFeature feature = featureBuilder.buildFeature("" + *//*basicObject.getLong("tweet_id")*//*count);
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
                    JOptionPane.INFORMATION_MESSAGE);*/
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

            StyleBuilder sb = new StyleBuilder();
            mark.setFill(styleFactory.createFill(/*filterFactory.literal(Color.CYAN)*/
                    sb.attributeExpression("color")));

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

    static class PlotDeducedTweetLocation extends SafeAction {

        PlotDeducedTweetLocation() {
            super("Deduce");
            putValue(Action.SHORT_DESCRIPTION, "Check each geometry");
        }

        public void action(ActionEvent e) throws Throwable {
            clearLayers();

            Date startDate = startdatePanel.GetDate();
            Date endDate = enddatePanel.GetDate();

            long tweetID = Long.parseLong(userid.getText());


            if (termClusters == null) {
                termClusters = AnomalyCluster.getAnomalyset(startDate, endDate);
//                termClusters = AnomalyCluster.getAnomalyset(startDate, endDate);
            }

            //connecting mongoDB on local machine.
            MongoClient mongoClient = new MongoClient("localhost", 27017);
            //connecting to database named test
            DB db = mongoClient.getDB("test");
            // getting collection of all files
            DBCollection collection = db.getCollection("correctTweetId");

            BasicDBObject query = new BasicDBObject();
            query.put("tweet_id", tweetID);

            DBCursor dbCursor = collection.find(query).sort(new BasicDBObject("timestamp", 1));

            BasicDBObject basicObject;
            if (dbCursor.hasNext() /*&& count < 6  && daycount < 3*/) {
                basicObject = (BasicDBObject) dbCursor.next();
            } else {
                System.out.println("there is no data record of id = " + tweetID);
                return;
            }

            //use user's previous locations to deduce user location.
            int userid = basicObject.getInt("userid");
            long timestamp = basicObject.getLong("timestamp");
            HashSet<termCluster> user_locations = termClusters.get("user_" + userid);

            SimpleFeatureTypeBuilder b1 = new SimpleFeatureTypeBuilder();

            b1.setName("PlotUserDataFeatureType");
            b1.setCRS(DefaultGeographicCRS.WGS84);
            b1.add("location", Point.class);
            b1.add("color", String.class);
            b1.add("userid", Integer.class);
            b1.add("size", Integer.class);
            b1.add("minTime", String.class);
            b1.add("maxTime", String.class);
            b1.add("timestamp", Long.class);
            b1.add("power", Integer.class);
            // building the type
            final SimpleFeatureType TYPE1 = b1.buildFeatureType();

            SimpleFeatureBuilder featureBuilder1 = new SimpleFeatureBuilder(TYPE1);
            GeometryFactory geometryFactory1 = JTSFactoryFinder.getGeometryFactory();
            DefaultFeatureCollection featureCollection1 = new DefaultFeatureCollection("internal", TYPE1);

            if (!user_locations.isEmpty()) {
                int max = 0;
                long min_temporal_closeness = Long.MAX_VALUE;
                double[] max_centroid = new double[2];
                for (termCluster cluster : user_locations) {
                    /*if (max < cluster.getReg().size()) {
                        max = cluster.getReg().size();
                        max_centroid = cluster.getCentroid();
                    }*/

                    long temporal_closeness = Math.abs(timestamp - cluster.getTimeCentroid().getTime());
                    if (min_temporal_closeness > temporal_closeness){
                        min_temporal_closeness = temporal_closeness;
                        max_centroid = cluster.getCentroid();
                    }
                    Point point = geometryFactory1.createPoint(new Coordinate(cluster.getCentroid()[0], cluster.getCentroid()[1]));

                    featureBuilder1.add(point);
                    featureBuilder1.add(Color.CYAN);
                    featureBuilder1.add(userid);
                    featureBuilder1.add(10);
                    featureBuilder1.add(df.format(cluster.getMinTime()));
                    featureBuilder1.add(df.format(cluster.getMaxTime()));
                    featureBuilder1.add(cluster.getTimeCentroid().getTime());
                    featureBuilder1.add(cluster.getReg().size());

                    SimpleFeature feature = featureBuilder1.buildFeature("" + /*basicObject.getLong("tweet_id")*/count);
                    featureCollection1.add(feature);
                    count++;
                }

                Point point = geometryFactory1.createPoint(new Coordinate(max_centroid[0], max_centroid[1]));

                featureBuilder1.add(point);
                featureBuilder1.add(Color.red);
                featureBuilder1.add(userid);
                featureBuilder1.add(5);

                SimpleFeature feature = featureBuilder1.buildFeature("" + /*basicObject.getLong("tweet_id")*/count);
                featureCollection1.add(feature);

                count++;

                String tmp = basicObject.get("coordinates").toString();
                tmp = tmp.substring(2, tmp.length() - 1);
//            System.out.println("" + tmp);
                String[] coordinates = tmp.split(",");
//                TrajectoryPoint point = geometryFactory.createPoint(new Coordinate(Double.parseDouble(coordinates[0]), Double.parseDouble(coordinates[1])));
                Coordinate coord = new Coordinate(Double.parseDouble(coordinates[0]), Double.parseDouble(coordinates[1]));

                Point point1 = geometryFactory1.createPoint(coord);

                featureBuilder1.add(point1);
                featureBuilder1.add(Color.green);
                featureBuilder1.add(userid);
                featureBuilder1.add(5);
                featureBuilder1.add(basicObject.getString("created_at"));
                featureBuilder1.add(basicObject.getString("created_at"));
                featureBuilder1.add(basicObject.getLong("timestamp"));
                featureBuilder1.add(1);

                SimpleFeature feature1 = featureBuilder1.buildFeature("" + /*basicObject.getLong("tweet_id")*/count);
                featureCollection1.add(feature1);

                Style style1 = createPointStyle(Color.black);
                userlayer = new FeatureLayer(featureCollection1, style1);
                map.addLayer(userlayer);
                return;
            }

            //use text to deduce tweet location
            String text = basicObject.getString("text");
            List<String> result = AnomalyCluster.tokenizeStopStem(text);

            HashMap<String, HashSet<termCluster>> subset = new HashMap<String, HashSet<termCluster>>();

            HashMap<String, Float> termScores = new HashMap<String, Float>();
            int all = AnomalyCluster.getNumOfAnalyzedDocs();
            float max_score = (float) 0.0;
            String max_term = "";

            for (String term: result) {
                if (termClusters.containsKey(term)) {
                    subset.put(term, (HashSet<termCluster>) termClusters.get(term).clone());
                    float noOfDoc = 0;
                    for (termCluster clust: termClusters.get(term)) {
//                System.out.println("terms = " + clust.getReg().size());
//                System.out.println("terms = " + clust.getReg().size() + ", centroid = [" + clust.getCentroid()[0] + "," + clust.getCentroid()[1] + "]");
                        noOfDoc += clust.getReg().size();
                    }
//            float docFreq = noOfDoc / 16893;
                    float docFreq = all / noOfDoc;

                    if (max_score < docFreq) {
                        max_score = docFreq;
                        max_term = term;
                    }

                    termScores.put(term, docFreq);
                }
            }

            double[] centroid;
            int largest = 0;

            for (termCluster cluster: subset.get(max_term)) {
                if (cluster.getReg().size() > largest) {
                    largest = cluster.getReg().size();
                    centroid = cluster.getCentroid();
                }

            }

            System.out.println(" id = " + tweetID + ", text = " + text + ", NumOfAnalyzedDocs = " + all);


            SimpleFeatureTypeBuilder b = new SimpleFeatureTypeBuilder();

            b.setName("PlotUserDataFeatureType");
            b.setCRS(DefaultGeographicCRS.WGS84);
            b.add("location", Point.class);
            b.add("text", String.class);
            b.add("power", Integer.class);
            b.add("Score", Float.class);
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

            for (String term : subset.keySet()) {
                for (termCluster clust : subset.get(term)) {
                    Point point = geometryFactory.createPoint(new Coordinate(clust.getCentroid()[0], clust.getCentroid()[1]));

                    featureBuilder = new SimpleFeatureBuilder(TYPE);
                    featureBuilder.add(point);
                    featureBuilder.add(max_term);
                    featureBuilder.add(clust.getReg().size());
                    featureBuilder.add(clust.getScore());

                    SimpleFeature feature = featureBuilder.buildFeature("" + /*basicObject.getLong("tweet_id")*/count);
                    featureCollection.add(feature);
                    count++;
                }
            }

            Style style = createPointStyle(Color.green);
            querylayer = new FeatureLayer(featureCollection, style);
            map.addLayer(querylayer);

            DefaultFeatureCollection correct_featureCollection = new DefaultFeatureCollection("internal", TYPE);

            String tmp = basicObject.get("coordinates").toString();
            tmp = tmp.substring(2, tmp.length() - 1);
//            System.out.println("" + tmp);
            String[] coordinates = tmp.split(",");
//                TrajectoryPoint point = geometryFactory.createPoint(new Coordinate(Double.parseDouble(coordinates[0]), Double.parseDouble(coordinates[1])));
            Coordinate coord = new Coordinate(Double.parseDouble(coordinates[0]), Double.parseDouble(coordinates[1]));

            Point point = geometryFactory.createPoint(coord);
            featureBuilder = new SimpleFeatureBuilder(TYPE);
            featureBuilder.add(point);
            featureBuilder.add("Correct : " + max_term);
            featureBuilder.add(1000);
            featureBuilder.add(1000);

            Style style3 = createPointStyle(Color.red);
            SimpleFeature feature = featureBuilder.buildFeature("" + /*basicObject.getLong("tweet_id")*/count);
            correct_featureCollection.add(feature);

            trajectorylayer = new FeatureLayer(correct_featureCollection, style3);
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


        /**
         * Create a Style to draw point features as circles with blue outlines
         * and cyan fill
         */
        private Style createPointStyle(Color fillColor) {
            StyleFactory styleFactory = CommonFactoryFinder.getStyleFactory();
            FilterFactory filterFactory = CommonFactoryFinder.getFilterFactory();
            Graphic gr = styleFactory.createDefaultGraphic();

            Mark mark = styleFactory.getCircleMark();

            mark.setStroke(styleFactory.createStroke(
                    filterFactory.literal(fillColor), filterFactory.literal(1)));

            StyleBuilder sb = new StyleBuilder();
            FilterFactory2 ff = sb.getFilterFactory();
            mark.setFill(styleFactory.createFill(/*filterFactory.literal(Color.CYAN)*/
                    sb.attributeExpression("color")));

            gr.graphicalSymbols().clear();
            gr.graphicalSymbols().add(mark);
            gr.setSize(ff.property("size"));

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

    private static double getDistance(double[] p1, double[] p2) {
        double lat = p1[0] - p2[0];
        double longi = p1[1] - p2[1];

        return Math.sqrt(lat * lat + longi * longi);
    }
}