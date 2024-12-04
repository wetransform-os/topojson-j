package json.geojson.objects;


import java.awt.Color;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

import org.apache.commons.lang3.ArrayUtils;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;

import json.algorithm.DouglasPeucker;
import json.graphic.Colorifier;
import json.graphic.Display;
import json.tools.Toolbox;
import json.topojson.algorithm.ArcMap;
import json.topojson.geom.Object;
import json.topojson.geom.sub.Entity;
import json.topojson.geom.sub.Ring;


public class Polygon extends Shape {

	//double _Xmin, _Ymin, _Xmax, _Ymax;
	Bounding _bnd;
	int[] _parts;
	Point[] _points;
	Vector<Entity> _entities; // needed for topojson
	
	public static int sKink;
	public static int sMin;
	
	static {
		
		sKink = 100;
		sMin = 10;
		
	}
	
	public Polygon(double Xmin, double Ymin, double Xmax, double Ymax, int[] iParts, Point[] iPoints){
		
		_bnd = new Bounding(Xmin, Ymin,Xmax,Ymax);

		
		_parts = iParts;
		_points = iPoints;
		
		_entities = new Vector<Entity>();
	}

	public static Polygon readPolygon(DataInputStream iStream, CoordinateReferenceSystem crs) {

		double Xmin = 0, Ymin = 0, Xmax = 0, Ymax = 0;

		byte[] aIBuffer = new byte[4];
		byte[] aDBuffer = new byte[8];
		
		int[] parts = null;
		
		Point[] points = null;

		try {
			
			// skip bounding
			iStream.read(aDBuffer);
			//Xmin = Toolbox.getDoubleFromByte(aDBuffer);
			
			iStream.read(aDBuffer);
			//Ymin = Toolbox.getDoubleFromByte(aDBuffer);
			
			//Point2D.Double aRes = Toolbox.convertLatLong(Xmin, Ymin);
			//Xmin = aRes.x;
			//Ymin = aRes.y;
			
			
			iStream.read(aDBuffer);
			//Xmax = Toolbox.getDoubleFromByte(aDBuffer);
			
			iStream.read(aDBuffer);
			//Ymax = Toolbox.getDoubleFromByte(aDBuffer);
		
			//aRes = Toolbox.convertLatLong(Xmax, Ymax);
			//Xmax = aRes.x;
			//Ymax = aRes.y;
			
			/*
			double swap;
			if (Xmin>Xmax) {
				swap = Xmin;
				Xmin = Xmax;
				Xmax = swap;
			}
			
			if (Ymin>Ymax) {
				swap = Ymin;
				Ymin = Ymax;
				Ymax = swap;
			}*/
			
			iStream.read(aIBuffer);
			int aNumparts = Toolbox.little2big(aIBuffer);
			parts = new int[aNumparts];
			
			iStream.read(aIBuffer);
			int aNumPoints = Toolbox.little2big(aIBuffer);
			points = new Point[aNumPoints];
			
			for (int i=0; i<aNumparts; i++) {
				iStream.read(aIBuffer);
				parts[i] = Toolbox.little2big(aIBuffer);
			}

			for (int i = 0; i < aNumPoints; i++) {

				points[i] = Point.readPoint(iStream, crs);
				if (i == 0) {

					Xmax = points[i].x;
					Ymax = points[i].y;
					Xmin = points[i].x;
					Ymin = points[i].y;
				
				} else {
				
					if (points[i].x>Xmax) Xmax = points[i].x;
					if (points[i].x<Xmin) Xmin = points[i].x;
					
					if (points[i].y>Ymax) Ymax = points[i].y;
					if (points[i].y<Ymin) Ymin = points[i].y;
					
				}
				
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		return new Polygon(Xmin, Ymin, Xmax, Ymax, parts, points);
		
	}
	
	public Polygon reducePolygonIntern(int iKink){
		
		Vector<Point> aRecPoints = new Vector<Point>();
		Vector<Integer> aRecParts = new Vector<Integer>();
		
		int lastBegin = 0;
		for (int i=0; i<_parts.length; i++) {
			
			int min = _parts[i];
			int max = (i==_parts.length-1?_points.length:_parts[i+1]);
			
			Point[] aPoints = new Point[max-min];
			
			int count = 0;
			for (int j=min; j<max; j++) {
				aPoints[count] = _points[j];
				count++;
			}
			
			aPoints = DouglasPeucker.GDouglasPeucker(aPoints, iKink);
		
			if (aPoints.length!=0) {
				aRecParts.add(lastBegin);
				for (int j=0; j<aPoints.length; j++) {
					aRecPoints.add(aPoints[j]);
				}
				lastBegin += aPoints.length;
			}
			
		}
		
		Integer[] aTabObject = new Integer[aRecParts.size()];
		aTabObject = aRecParts.toArray(aTabObject);
		
		Point[] aPoints = new Point[aRecPoints.size()];
		aPoints =  aRecPoints.toArray(aPoints);
		
		int[] aResTab = new int[aTabObject.length];
		for (int i=0;i<aResTab.length; i++){
			aResTab[i] = aTabObject[i].intValue();
		}

		//System.out.println("Reduced "+aPoints.length+"/"+_points.length);
		return new Polygon(_bnd.minx, _bnd.miny, _bnd.maxx, _bnd.maxy, aResTab, aPoints );
	}
	
	public Polygon reducePolygon(int iKink, int iMin){
	
		int iStartKink = iKink;
		Polygon aRes = reducePolygonIntern(iStartKink);
		while (aRes._points.length<iMin) {
			aRes = reducePolygonIntern(iStartKink);
			iStartKink-=2;
		}
		return aRes;
	}	
		
	@Override
	public String toJson() {
		
		Polygon aPoly = this;
		if (sKink!=0) aPoly = reducePolygon(sKink, sMin);
		// TODO Auto-generated method stub
		StringBuffer aBuffer = new StringBuffer();
		
		aBuffer.append("{ \"type\": \"Polygon\", \"coordinates\": [ ");
		
		//     [ [100.0, 0.0], [101.0, 0.0], [101.0, 1.0], [100.0, 1.0], [100.0, 0.0] ]
		
		for (int i=0; i<aPoly._parts.length; i++) {
		
			int min = aPoly._parts[i];
			int max = (i==aPoly._parts.length-1?aPoly._points.length:aPoly._parts[i+1]);
			aBuffer.append("[");
			for (int j=min; j<max; j++) {
			
				aBuffer.append("[");
	
				aBuffer.append(String.format("%.4f", aPoly._points[j].x));
				aBuffer.append(",");
				aBuffer.append(String.format("%.4f", aPoly._points[j].y));
				aBuffer.append("]");
				if (j!=max-1) aBuffer.append(",");
				
			}
			aBuffer.append("]");
			if (i!=aPoly._parts.length-1) aBuffer.append(",");
			
		}
		
		aBuffer.append(" ] }");

		
		return aBuffer.toString();
	}
	
	public boolean partlyIn(Bounding iBnd){
		return _bnd.partlyIn(iBnd);
	}

	@Override
	public Bounding getBounding() {
		// TODO Auto-generated method stub
		return _bnd;
	}

	@Override
	public List<Entity> extract() {
		
		_entities.clear();
		
		for (int i=0; i<_parts.length; i++) {
			
			Vector<json.topojson.geom.sub.Position> aPoints = new Vector<json.topojson.geom.sub.Position>(); 
			
			int min = _parts[i];
			int max = (i==_parts.length-1?_points.length:_parts[i+1]);
			for (int j=min; j<max; j++) {
			
				aPoints.add(new json.topojson.geom.sub.Position(_points[j].x,
														 _points[j].y));
			}
			
			json.topojson.geom.sub.Position[] aPoints_Arr = new json.topojson.geom.sub.Position[aPoints.size()];
			aPoints_Arr = aPoints.toArray(aPoints_Arr); 
			
			_entities.add(new Ring(aPoints_Arr,_bnd));
			
		}
		
		return _entities;
	}
	
	@Override
	public int[] arcs(){
		int[] aAll = {};
		for (Entity aEnt:_entities) {
			aAll = ArrayUtils.addAll(aAll,aEnt.getIndexes());
		}
		return aAll;
	}

	@Override
	public void draw(Display iDisp, Color iColor) {
		
		for (int i=0; i<_parts.length; i++) {
			
			int min = _parts[i];
			int max = (i==_parts.length-1?_points.length:_parts[i+1]);
			for (int j=min; j<max-1; j++) {
			
				iDisp.drawLine(_points[j].x,_points[j].y,
							   _points[j+1].x,_points[j+1].y, Color.WHITE);
			}
			
		}
		
	}
	
	@Override
	public void fill(Display iDisplay, Colorifier iColor) {
		
		for (int i=0; i<_parts.length; i++) {
			
			int min = _parts[i];
			int max = (i==_parts.length-1?_points.length:_parts[i+1]);
			
			double[] x = new double[max-min+1];
			double[] y = new double[max-min+1];
			
			int count = 0;
			for (int j=min; j<max; j++) {
				x[count] = _points[j].x;
				y[count] = _points[j].y;
				count++;
			}
			
			iDisplay.fillPolygons(x, y, max-min+1, iColor.getColor(null));
			
		}
		
	}

	@Override
	public json.topojson.geom.Object toTopology() {
		return new json.topojson.geom.Polygon(_entities); 
	}

	@Override
	public json.geojson.objects.Object clone() {
		
		Vector<Entity> aEntities = new Vector<Entity>();
		for (Entity aEnt:_entities) {
			aEntities.add(aEnt.clone());
		}
		
		Point[] aP = new Point[_points.length];
		for (int i=0;i<_points.length;i++){
			aP[i] = (Point) _points[i].clone();
		}
		
		Polygon aPolygon = new Polygon(_bnd.minx, _bnd.miny, _bnd.maxx, _bnd.maxy,
									   _parts.clone(), aP);
		aPolygon._entities = aEntities;
		
		return aPolygon;
	}

	@Override
	public void rebuildIndexes(ArcMap iMap) {
		for (Entity aEnt:_entities){
			aEnt.rebuildIndexes(iMap);
		}
	}

	
}
