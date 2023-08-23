package module4;

import java.util.ArrayList;
import java.util.List;

import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.GeoJSONReader;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.AbstractShapeMarker;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.marker.MultiMarker;
import de.fhpotsdam.unfolding.providers.MBTilesMapProvider;
import de.fhpotsdam.unfolding.providers.Microsoft;
import de.fhpotsdam.unfolding.utils.MapUtils;
import parsing.ParseFeed;
import processing.core.PApplet;

/** EarthquakeCityMap
 * An application with an interactive map displaying earthquake data with 
 * different kinds of markers for different categories of earthquakes.
 * 
 * Practising polymorphism and inheritance while working on several different classes
 * 
 * Author: UC San Diego Intermediate Software Development MOOC team
 * @author Emircan
 * Date: August 23, 2023
 * */
public class EarthquakeCityMap extends PApplet {
	
	// We will use member variables, instead of local variables, to store the data
	// that the setUp and draw methods will need to access (as well as other methods)
	// You will use many of these variables, but the only one you should need to add
	// code to modify is countryQuakes, where you will store the number of earthquakes
	// per country.
	
	// You can ignore this.  It's to get rid of eclipse warnings
	private static final long serialVersionUID = 1L;

	// IF YOU ARE WORKING OFFILINE, change the value of this variable to true
	private static final boolean offline = false;
	
	/** This is where to find the local tiles, for working without an Internet connection */
	public static String mbTilesString = "blankLight-1-3.mbtiles";
	
	

	//feed with magnitude 2.5+ Earthquakes
	private String earthquakesURL = "https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/2.5_week.atom";
	
	// The files containing city names and info and country names and info
	private String cityFile = "city-data.json";
	private String countryFile = "countries.geo.json";
	
	// The map
	private UnfoldingMap map;
	
	// Markers for each city
	private List<Marker> cityMarkers;
	// Markers for each earthquake
	private List<Marker> quakeMarkers;

	// A List of country markers
	private List<Marker> countryMarkers;
	
	public void setup() {		
		// (1) Initializing canvas and map tiles
		size(900, 700, OPENGL);
		if (offline) {
		    map = new UnfoldingMap(this, 200, 50, 650, 600, new MBTilesMapProvider(mbTilesString));
		    earthquakesURL = "2.5_week.atom";  // The same feed, but saved August 7, 2015
		}
		else {
			// Microsoft.AerialProvider()
			// Google.GoogleMapProvider()
			// OpenStreetMap.OpenStreetMapProvider()
			map = new UnfoldingMap(this, 200, 50, 650, 600, new Microsoft.RoadProvider());
			// IF YOU WANT TO TEST WITH A LOCAL FILE, uncomment the next line
		    //earthquakesURL = "2.5_week.atom";
		}
		MapUtils.createDefaultEventDispatcher(this, map);
		
		// FOR TESTING: Set earthquakesURL to be one of the testing files by uncommenting
		// one of the lines below.  This will work whether you are online or offline
		//earthquakesURL = "test1.atom";
		//earthquakesURL = "test2.atom";
		
		// WHEN TAKING THIS QUIZ: Uncomment the next line
		//earthquakesURL = "quiz1.atom";
		
		
		// (2) Reading in earthquake data and geometric properties
	    //     STEP 1: load country features and markers
		List<Feature> countries = GeoJSONReader.loadData(this, countryFile);
		countryMarkers = MapUtils.createSimpleMarkers(countries);
		
		//     STEP 2: read in city data
		List<Feature> cities = GeoJSONReader.loadData(this, cityFile);
		cityMarkers = new ArrayList<Marker>();
		for(Feature city : cities) {
		  cityMarkers.add(new CityMarker(city));
		}
	    
		//     STEP 3: read in earthquake RSS feed
	    List<PointFeature> earthquakes = ParseFeed.parseEarthquake(this, earthquakesURL);
	    quakeMarkers = new ArrayList<Marker>();
	    
	    for(PointFeature feature : earthquakes) {
		  //check if LandQuake
		  if(isLand(feature)) {
		    quakeMarkers.add(new LandQuakeMarker(feature));
		  }
		  // OceanQuakes
		  else {
		    quakeMarkers.add(new OceanQuakeMarker(feature));
		  }
	    }

	    // could be used for debugging
	    printQuakes();
	 		
	    // (3) Add markers to map
	    //     NOTE: Country markers are not added to the map.  They are used
	    //           for their geometric properties
	    map.addMarkers(quakeMarkers);
	    map.addMarkers(cityMarkers);
	    
	}  // End setup
	
	
	public void draw() {
		background(0);
		map.draw();
		addKey();
		
	}
	
	private void addKey() {	
		fill(255, 250, 240);
		rect(25, 50, 165, 380);
		
			
		fill(0);
		textAlign(LEFT, CENTER);
		textSize(12);
		fill(color(169,24,24));
	    triangle(58, 117, 48, 132, 68, 132);
		fill(color(255,255,255));
	    rect(50, 168, 16, 16);
		ellipse(58,226,19,19); // Ocean
		
		fill(255, 255, 0); // Yellow - Shallow
		ellipse(58,295,14,14);
		
		fill(24,36,126); // Blue - Intermediate
		ellipse(58,330,14,14);
		
		fill(210,0,0); // Red - Deep
		ellipse(58,365,14,14);
		
		fill(0);
		text("Shallow", 79, 293);
		text("Intermediate", 79, 328);
		text("Deep", 79, 362);

		
		fill(0, 0, 0);
		text("City Marker", 75, 124);
		text("Land Quake", 75, 175);
		text("Ocean Quake", 75, 225);
		textSize(16);
		text("Size ~ Magnitude", 38, 262);
		text("Size ~ Magnitude", 38 + 1, 262);
		
		fill(0);
		textAlign(LEFT, CENTER);
		textSize(18);
		text("Earthquake Key", 35, 75);
		text("Earthquake Key", 35 + 1, 75);	
		
		noFill();
		stroke(0); 
		strokeWeight(2); 
		ellipse(58, 395, 13, 13);
		line(58 - 8, 395 - 8, 58 + 8, 395 + 8);
	    line(58 - 8, 395 + 8, 58 + 8, 395 - 8);
	    textSize(12);
	    text("Past Hour", 78, 393);
	}

	
	
	// Checks whether this quake occurred on land.  If it did, it sets the 
	// "country" property of its PointFeature to the country where it occurred
	// and returns true.  Notice that the helper method isInCountry will
	// set this "country" property already.  Otherwise it returns false.
	private boolean isLand(PointFeature earthquake) {
		
		
		// Loop over all the country markers.  
		// For each, check if the earthquake PointFeature is in the 
		// country in m.  Notice that isInCountry takes a PointFeature
		// and a Marker as input.  
		// If isInCountry ever returns true, isLand should return true.
		for (Marker m : countryMarkers) {
	        if (isInCountry(earthquake, m)) {
	            String countryName = m.getStringProperty("name");
	            if (countryName != null) {
	                // Set the "country" property on the earthquake's LandMarker
	                earthquake.addProperty("country", countryName);
	            }
	            return true;
	        }
	    }
	    
	    // Not inside any country
	    return false;
	}
	
	/* prints countries with number of earthquakes as
	 * Country1: numQuakes1
	 * Country2: numQuakes2
	 * ...
	 * OCEAN QUAKES: numOceanQuakes
	 * */
	private void printQuakes() 
	{
		//   One (inefficient but correct) approach is to:
		//   Loop over all of the countries, e.g. using 
		//        for (Marker cm : countryMarkers) { ... }
		//        
		//      Inside the loop, first initialize a quake counter.
		//      Then loop through all of the earthquake
		//      markers and check to see whether (1) that marker is on land
		//     	and (2) if it is on land, that its country property matches 
		//      the name property of the country marker.   If so, increment
		//      the country's counter.
		
		// Here is some code you will find useful:
		// 
		//  * To get the name of a country from a country marker in variable cm, use:
		//     String name = (String)cm.getProperty("name");
		//  * If you have a reference to a Marker m, but you know the underlying object
		//    is an EarthquakeMarker, you can cast it:
		//       EarthquakeMarker em = (EarthquakeMarker)m;
		//    Then em can access the methods of the EarthquakeMarker class 
		//       (e.g. isOnLand)
		//  * If you know your Marker, m, is a LandQuakeMarker, then it has a "country" 
		//      property set.  You can get the country with:
		//        String country = (String)m.getProperty("country");
		
		for (Marker cm : countryMarkers) {
	        int countryQuakeCount = 0; // Initialize quake counter for the country
	        String countryName = (String) cm.getProperty("name"); // Get the country name
	        
	        // Loop through all earthquake markers
	        for (Marker em : quakeMarkers) {
	            EarthquakeMarker eqMarker = (EarthquakeMarker) em;
	            
	            if (eqMarker.isOnLand() && eqMarker.getProperty("country").equals(countryName)) {
	                // Check if the earthquake is on land and its country matches the current country marker
	                countryQuakeCount++; // Increment quake counter for the country
	            }
	        }
	        
	        if (countryQuakeCount > 0) {
	            System.out.println(countryName + ": " + countryQuakeCount);
	        }
	    }
	    
	    // Count and print ocean earthquakes
	    int oceanQuakeCount = 0;
	    for (Marker em : quakeMarkers) {
	        EarthquakeMarker eqMarker = (EarthquakeMarker) em;
	        
	        if (!eqMarker.isOnLand()) {
	            oceanQuakeCount++;
	        }
	    }
	    
	    System.out.println("OCEAN QUAKES: " + oceanQuakeCount);
	}
	
	
	
	// helper method to test whether a given earthquake is in a given country
	// This will also add the country property to the properties of the earthquake 
	// feature if it's in one of the countries.
	private boolean isInCountry(PointFeature earthquake, Marker country) {
	    Location checkLoc = earthquake.getLocation();

	    if (country.getClass() == MultiMarker.class) {
	        for (Marker marker : ((MultiMarker) country).getMarkers()) {
	            if (((AbstractShapeMarker) marker).isInsideByLocation(checkLoc)) {
	                earthquake.addProperty("country", country.getProperty("name"));
	                return true;
	            }
	        }
	    } else if (((AbstractShapeMarker) country).isInsideByLocation(checkLoc)) {
	        earthquake.addProperty("country", country.getProperty("name"));
	        return true;
	    }

	    return false;
	}


}
