package json.tools;

import java.nio.charset.StandardCharsets;

import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4UnknownSizeDecompressor;

public class Compress {

	public static String compressB64(String iStr){
	
		byte[] aInputData = iStr.getBytes(StandardCharsets.ISO_8859_1);
		
		LZ4Factory factory = LZ4Factory.fastestInstance();
		
		LZ4Compressor compressor = factory.fastCompressor();
		int maxCompressedLength = compressor.maxCompressedLength(aInputData.length);
		byte[] compressed = new byte[maxCompressedLength];
		int compressedLength = compressor.compress(aInputData, 0, aInputData.length, compressed, 0, maxCompressedLength);
	
		byte[] compressedFinal = new byte[compressedLength];
		System.arraycopy(compressed, 0, compressedFinal, 0, compressedLength);
		
		return java.util.Base64.getEncoder().encodeToString(compressedFinal);
		
	}
	
	public static String decompressB64(String iStr){
		
	
		byte[] aInputData = java.util.Base64.getDecoder().decode(iStr);
		
		LZ4Factory factory = LZ4Factory.fastestInstance();
		
		int primaryLength = aInputData.length*5; 
		byte[] aOutputData = new byte[primaryLength]; 
		
		LZ4UnknownSizeDecompressor decompressor = factory.unknownSizeDecompressor();
		int decompressed = decompressor.decompress(aInputData, 
												   0, aInputData.length, 
												   aOutputData, 0);
				
		return new String(aOutputData,StandardCharsets.ISO_8859_1);
		
	}
	
}
