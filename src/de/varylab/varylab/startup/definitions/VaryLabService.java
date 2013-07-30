package de.varylab.varylab.startup.definitions;

import java.awt.EventQueue;
import java.awt.Image;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.LinkedList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.logging.Logger;

import de.jreality.plugin.JRViewer;
import de.jreality.plugin.basic.ConsolePlugin;
import de.jreality.plugin.job.JobMonitorPlugin;
import de.jreality.reader.ReaderOBJ;
import de.jreality.scene.Geometry;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.util.SceneGraphUtility;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.HalfedgeLayer;
import de.jtem.jrworkspace.plugin.Plugin;
import de.jtem.jrworkspace.plugin.simplecontroller.widget.SplashScreen;
import de.varylab.varylab.plugin.VarylabMain;
import de.varylab.varylab.startup.SplashImageHook;
import de.varylab.varylab.startup.VarylabSplashScreen;
import de.varylab.varylab.startup.VarylabStartupDefinition;

public class VaryLabService extends VarylabStartupDefinition {

	private Logger
		log = Logger.getLogger(VaryLabService.class.getName());
	private VarylabSplashScreen
		splash = null;
	private List<String>
		pluginClassNames = null;
	private String
		projectId = "default";
	private List<String>
		modelURLs = new LinkedList<String>(),
		jarPlugins = new LinkedList<String>();
	private static File
		propertiesFolder = null,
		pluginsFolder = null;
	
	static {
		// create varylab preferences folder
		String userHome = System.getProperty("user.home");
		propertiesFolder = new File(userHome + "/.varylab");
		propertiesFolder.mkdirs();
		pluginsFolder = new File(propertiesFolder, "plugins");
		pluginsFolder.mkdirs();
	}
	
	public VaryLabService(List<String> plugins, String projectId, List<String> models, List<String> jarPlugins) {
		super();
		this.pluginClassNames = plugins;
		this.projectId = projectId;
		this.modelURLs = models;
		this.jarPlugins = jarPlugins;
	}

	@Override
	public String getApplicationName() {
		return "VaryLab";
	}
	
	@Override
	public String getPropertyFileName() {
		String prosFileName = "project_" + projectId + ".xml";
		File propsFile = new File(propertiesFolder, prosFileName);
		checkPropertyFile(propsFile);
		return propsFile.getAbsolutePath();
	}
	
	private void checkPropertyFile(File propsFile) {
		if (propsFile.exists()) {
			return;
		}
		log.info("writing default property file to " + propsFile);
		try {
			InputStream in = getClass().getResourceAsStream("VaryLabDefault.xml");
			Reader propsReader = new InputStreamReader(in);
			FileWriter propsWriter = new FileWriter(propsFile);
			char[] buffer = new char[1024];
			int numRead;
			while ((numRead = propsReader.read(buffer)) >= 0) {
				propsWriter.write(buffer, 0, numRead);
			}
			propsWriter.close();
			propsReader.close();
		} catch (Exception e) {
			log.warning("error writing default property file " + e);
		}
	}
	
	@Override
	public VarylabSplashScreen getSplashScreen() {
		if (splash == null) {
			Image lowRes = SplashImageHook.getImage("varylab_ultimate_low_res.png");
			Image highRes = SplashImageHook.getImage("varylab_ultimate_high_res.png");
			splash = new VarylabSplashScreen(lowRes, highRes);
		}
		return splash;
	}
	
	@Override
	public void getPlugins(Set<Class<? extends Plugin>> classes, Set<Plugin> instances) {
		classes.add(VarylabMain.class);
		classes.add(ConsolePlugin.class);
		classes.add(JobMonitorPlugin.class);
		
		// load custom classes
		for (String className : pluginClassNames) {
			try {
				Class<?> clazz = Class.forName(className);
				Object obj = clazz.newInstance();
				if (obj instanceof Plugin) {
					instances.add((Plugin)obj);
				}
			} catch (Exception e) {
				log.warning("could not load plug-in class \"" + className + "\": " + e);
			}
		}
		
		// load jar plugin classes
		registerJarPlugins(jarPlugins, instances);
	}
	
	
	public void registerJarPlugins(List<String> jarPlugins, Set<Plugin> instances) {
		ByteBuffer buffer = ByteBuffer.allocate(1024 * 1024);
		for (String jarURLString : jarPlugins) {
			URL jarURL = null;
			try {
				jarURL = new URL(jarURLString);
			} catch (MalformedURLException e) {
				log.warning("cannot download plugin: " + e);
				continue;
			}
			InputStream in = null;
			try{
				URLConnection con = jarURL.openConnection();
				in = con.getInputStream();
			} catch (IOException e) {
				log.warning("cannot download plugin: " + e);
			}
			
			File nameFile = new File(jarURL.getPath());
			String pluginName = nameFile.getName();
			File pluginFile = new File(pluginsFolder, pluginName);
			if (in != null) {
				log.info("downloading plugin file " + jarURL);
				try {
					pluginFile.createNewFile();
					FileOutputStream fOut = new FileOutputStream(pluginFile);
					ReadableByteChannel inChannel = Channels.newChannel(in);
					WritableByteChannel outChannel = Channels.newChannel(fOut);
					while (inChannel.read(buffer) != -1) {
						outChannel.write(buffer);
						buffer.rewind();
					}
					in.close();
					fOut.close();
				} catch (FileNotFoundException fnfe) {
					log.warning(fnfe.toString());
				} catch (IOException e) {
					log.warning("could not create plugin file " + e);
				}
			}
			if (pluginFile.exists()) {
				URL pluginFileURL = null;
				try {
					pluginFileURL = pluginFile.toURI().toURL();
				} catch (MalformedURLException e) {
					log.warning(e.toString());
					continue;
				}
				URL[] urls = {pluginFileURL};
				URLClassLoader pluginLoader = new URLClassLoader(urls);
				ServiceLoader<Plugin> sl = ServiceLoader.load(Plugin.class, pluginLoader);
				for (Plugin p : sl) {
					instances.add(p);
					log.info("adding jar plugin " + p.getClass().getName());
				}
			} else {
				log.warning("could not load plugin from " + pluginFile);
			}
		}
	}
	
	
	@Override
	protected void postStartup(final JRViewer v, SplashScreen splash) {
		splash.setStatus("loading online models");
		final HalfedgeInterface hif = v.getPlugin(HalfedgeInterface.class);
		for (String modelURL : modelURLs) {
			URL url = null;
			try {
				url = new URL(modelURL);
			} catch (MalformedURLException me) {
				continue;
			}
			ReaderOBJ objreader = new ReaderOBJ();
			int counter = 0;
			try {
				SceneGraphComponent sgc = objreader.read(url);
				Geometry g = SceneGraphUtility.getFirstGeometry(sgc);
				final HalfedgeLayer layer = new HalfedgeLayer(g, hif);
				layer.setName("Online Model " + counter++);
				Runnable addJob = new Runnable() {
					@Override
					public void run() {
						hif.addLayer(layer);
						hif.update();
						v.encompassEuclidean();
					}
				};
				EventQueue.invokeLater(addJob);
			} catch (IOException e) {
				continue;
			}
		}
	}

	
	public static void main(String[] args) throws Exception {
		// read plug-ins
		List<String> plugins = new LinkedList<String>();
		if (args.length >= 1) {
			String pluginNames = args[0];
			for (String name : pluginNames.split(" ")) {
				if (name.trim().isEmpty()) continue;
				plugins.add(name.trim());
			}
		}
		// get project id
		String projectId = "default";
		if (args.length >= 2) {
			projectId = args[1];
		}
		
		List<String> models = new LinkedList<String>();
		if (args.length >= 3) {
			String modelURLs = args[2];
			for (String url : modelURLs.split(" ")) {
				if (url.trim().isEmpty()) continue;
				models.add(url.trim());
			}
		}
		
		List<String> jarPlugins = new LinkedList<String>();
		if (args.length >= 4) {
			String pluginURLs = args[3];
			for (String url : pluginURLs.split(" ")) {
				if (url.trim().isEmpty()) continue;
				jarPlugins.add(url.trim());
			}
		}
		
		new VaryLabService(plugins, projectId, models, jarPlugins).startup();
	}

}
