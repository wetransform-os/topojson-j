import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.geotools.referencing.CRS;
import org.junit.Test;

import json.topojson.api.TopojsonApi;


public class TestShpToTopojson {

	@Test
	public void testShpFromHale() throws IOException {

		// Specify the relative path as a string
        String relativePathString = "./web/topojson_hale.json";

        String crsSpec = "EPSG:4326";

        // Decode the text using UTF-8 encoding (or the appropriate encoding)
        byte[] crsBytes = crsSpec.getBytes(StandardCharsets.UTF_8);
        String decodedCRS = new String(crsBytes, StandardCharsets.UTF_8);
        
        try {
			TopojsonApi.shpToTopojsonFile("./data/example.shp", CRS.decode(decodedCRS),
					relativePathString, 
					"Topology", 
					0, 
					4, 
					false);		
        } catch (Exception e) {
            // Handle parsing exceptions, e.g., log or rethrow
            e.printStackTrace();
        }
			
		// Convert the relative path to an absolute path
	    Path absolutePath = Paths.get(relativePathString).toAbsolutePath();

		// Use Files.exists() to check if the file was created
	    assertTrue(Files.exists(absolutePath));
	}	
	

}
