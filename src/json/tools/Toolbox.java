package json.tools;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.api.referencing.operation.MathTransform;

public class Toolbox {

//	static Projection sProj;

//	static {
//
//		sProj = ProjectionFactory.getNamedPROJ4CoordinateSystem("esri:102003");
//
//	}
//
//	// TODO change this as not thread safe
//	public static void setCoordinateSystem(String iCoordinate) {
//		sProj = ProjectionFactory.getNamedPROJ4CoordinateSystem(iCoordinate);
//	}

	public static Point2D.Double convertLatLong(double X, double Y,
			CoordinateReferenceSystem sourceCrs) {
//		Projection sProj = ProjectionFactory.getNamedPROJ4CoordinateSystem(crs.toLowerCase());

//		Point2D.Double aSrc = new Point2D.Double(X, Y);
		Point2D.Double aDst = new Point2D.Double();

		GeometryFactory gf = new GeometryFactory();
		Point p = gf.createPoint(new Coordinate(X, Y));
		try {
			MathTransform transform = CRS.findMathTransform(sourceCrs, CRS.decode("EPSG:4326"));
			p = (Point) JTS.transform(p, transform);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}

//		sProj.inverseTransform(aSrc, aDst);
		// aDst.x = aSrc.x;
		// aDst.y = aSrc.y;

		aDst.x = p.getX();
		aDst.y = p.getY();

		return aDst;
		
	}
	
	public static int little2big(byte[] iBytes) {
		return ByteBuffer.wrap(iBytes).order(ByteOrder.LITTLE_ENDIAN ).getInt();
	}

	public static double getDoubleFromByte(byte[] aDoubleBuffer){
		return ByteBuffer.wrap(aDoubleBuffer).order(ByteOrder.LITTLE_ENDIAN ).getDouble();
	}
	
	public static void writeFile(String iFile, String iData){
		
		FileOutputStream aStream;
		try {
			aStream = new FileOutputStream(new File(iFile));
			aStream.write(iData.getBytes());
			aStream.flush();
			aStream.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	
}
