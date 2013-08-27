package de.varylab.varylab.plugin.grasshopper.data;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;

import org.junit.Assert;
import org.junit.Test;

import de.varylab.varylab.plugin.grasshopper.data.binding.RVLMesh;

public class RVLMeshFactoryTest {
	
	String testXML = 
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?> \n" +
		"	<RVLMesh \n" +
		"		xmlns=\"http://schemas.datacontract.org/2004/07/GHVaryLab\" \n" +
		"		xmlns:i=\"http://www.w3.org/2001/XMLSchema-instance\"> \n" +
		"		<Faces/> \n" +
		"		<Vertices> \n" +
		"			<Vertex> \n" +
		"				<X>0.1234</X> \n" +
		"				<Y>0.5789</Y> \n" +
		"				<Z>0.2345</Z> \n" +
		"			</Vertex> \n" +
		"		</Vertices> \n" +
		"	</RVLMesh>"; 
	
	@Test
	public void testLoadRVLMeshFromString() throws Exception {
		StringReader r2 = new StringReader(testXML);
		RVLMesh mesh = RVLMeshFactory.loadRVLMesh(r2);
		Assert.assertEquals(1, mesh.getVertices().getVertex().size());
		Assert.assertEquals(0, mesh.getFaces().getFace().size());
	}
	
	@Test
	public void testLoadRVLMeshFromInputStream() throws Exception {
		InputStream in = getClass().getResourceAsStream("rvlmesh_test01.xml");
		InputStreamReader reader = new InputStreamReader(in);
		RVLMesh mesh = RVLMeshFactory.loadRVLMesh(reader);
		Assert.assertEquals(256, mesh.getVertices().getVertex().size());
		Assert.assertEquals(225, mesh.getFaces().getFace().size());
	}
	
}
