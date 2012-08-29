package de.varylab.varylab.plugin.lnf;

import java.util.Collections;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.SwingUtilities;

import de.jreality.plugin.basic.View;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.Plugin;
import de.jtem.jrworkspace.plugin.flavor.MenuFlavor;
import de.jtem.jrworkspace.plugin.flavor.PerspectiveFlavor;

public class SubstanceLnFMenu extends Plugin implements MenuFlavor {

	private JMenu 
		substanceMenus = new JMenu("Look and Feel");
	
	public SubstanceLnFMenu() {
	}

	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		View v = c.getPlugin(View.class);
		JFrame frame = (JFrame)SwingUtilities.getWindowAncestor(v.getContentPanel());
		
		substanceMenus
				.add(SubstanceLafChanger
						.getMenuItem(frame, "Autumn",
								"org.pushingpixels.substance.api.skin.SubstanceAutumnLookAndFeel"));
		substanceMenus
				.add(SubstanceLafChanger
						.getMenuItem(frame, "Business",
								"org.pushingpixels.substance.api.skin.SubstanceBusinessLookAndFeel"));
		substanceMenus
				.add(SubstanceLafChanger
						.getMenuItem(frame, "Business Black Steel",
								"org.pushingpixels.substance.api.skin.SubstanceBusinessBlackSteelLookAndFeel"));
		substanceMenus
				.add(SubstanceLafChanger
						.getMenuItem(frame, "Business Blue Steel",
								"org.pushingpixels.substance.api.skin.SubstanceBusinessBlueSteelLookAndFeel"));
		substanceMenus
				.add(SubstanceLafChanger
						.getMenuItem(frame, "Creme",
								"org.pushingpixels.substance.api.skin.SubstanceCremeLookAndFeel"));
		substanceMenus
				.add(SubstanceLafChanger
						.getMenuItem(frame, "Creme Coffee",
								"org.pushingpixels.substance.api.skin.SubstanceCremeCoffeeLookAndFeel"));
		substanceMenus
				.add(SubstanceLafChanger
						.getMenuItem(frame, "Dust",
								"org.pushingpixels.substance.api.skin.SubstanceDustLookAndFeel"));
		substanceMenus
				.add(SubstanceLafChanger
						.getMenuItem(frame, "Dust Coffee",
								"org.pushingpixels.substance.api.skin.SubstanceDustCoffeeLookAndFeel"));
		substanceMenus
				.add(SubstanceLafChanger
						.getMenuItem(frame, "Gemini",
								"org.pushingpixels.substance.api.skin.SubstanceGeminiLookAndFeel"));
		substanceMenus
				.add(SubstanceLafChanger
						.getMenuItem(frame, "Mariner",
								"org.pushingpixels.substance.api.skin.SubstanceMarinerLookAndFeel"));
		substanceMenus
				.add(SubstanceLafChanger
						.getMenuItem(frame, "Moderate",
								"org.pushingpixels.substance.api.skin.SubstanceModerateLookAndFeel"));
		substanceMenus
				.add(SubstanceLafChanger
						.getMenuItem(frame, "Nebula",
								"org.pushingpixels.substance.api.skin.SubstanceNebulaLookAndFeel"));
		substanceMenus
				.add(SubstanceLafChanger
						.getMenuItem(frame, "Nebula Brick Wall",
								"org.pushingpixels.substance.api.skin.SubstanceNebulaBrickWallLookAndFeel"));
		substanceMenus
				.add(SubstanceLafChanger
						.getMenuItem(frame, "Office Black 2007",
								"org.pushingpixels.substance.api.skin.SubstanceOfficeBlack2007LookAndFeel"));
		substanceMenus
				.add(SubstanceLafChanger
						.getMenuItem(frame, "Office Silver 2007",
								"org.pushingpixels.substance.api.skin.SubstanceOfficeSilver2007LookAndFeel"));
		substanceMenus
				.add(SubstanceLafChanger
						.getMenuItem(frame, "Sahara",
								"org.pushingpixels.substance.api.skin.SubstanceSaharaLookAndFeel"));
		substanceMenus.addSeparator();
		substanceMenus
				.add(SubstanceLafChanger
						.getMenuItem(frame, "Office Blue 2007",
								"org.pushingpixels.substance.api.skin.SubstanceOfficeBlue2007LookAndFeel"));
		substanceMenus
				.add(SubstanceLafChanger
						.getMenuItem(frame, "Magellan",
								"org.pushingpixels.substance.api.skin.SubstanceMagellanLookAndFeel"));
		substanceMenus.addSeparator();
		substanceMenus
				.add(SubstanceLafChanger
						.getMenuItem(frame, "Challenger Deep",
								"org.pushingpixels.substance.api.skin.SubstanceChallengerDeepLookAndFeel"));
		substanceMenus
				.add(SubstanceLafChanger
						.getMenuItem(frame, "Emerald Dusk",
								"org.pushingpixels.substance.api.skin.SubstanceEmeraldDuskLookAndFeel"));
		substanceMenus
				.add(SubstanceLafChanger
						.getMenuItem(frame, "Magma",
								"org.pushingpixels.substance.api.skin.SubstanceMagmaLookAndFeel"));
		substanceMenus
				.add(SubstanceLafChanger
						.getMenuItem(frame, "Raven",
								"org.pushingpixels.substance.api.skin.SubstanceRavenLookAndFeel"));
		substanceMenus
				.add(SubstanceLafChanger
						.getMenuItem(frame, "Graphite",
								"org.pushingpixels.substance.api.skin.SubstanceGraphiteLookAndFeel"));
		substanceMenus
				.add(SubstanceLafChanger
						.getMenuItem(frame, "Graphite Glass",
								"org.pushingpixels.substance.api.skin.SubstanceGraphiteGlassLookAndFeel"));
		substanceMenus
				.add(SubstanceLafChanger
						.getMenuItem(frame, "Graphite Aqua",
								"org.pushingpixels.substance.api.skin.SubstanceGraphiteAquaLookAndFeel"));
		substanceMenus
				.add(SubstanceLafChanger
						.getMenuItem(frame, "Twilight",
								"org.pushingpixels.substance.api.skin.SubstanceTwilightLookAndFeel"));
	}
	
	@Override
	public List<JMenu> getMenus() {
		return Collections.singletonList(substanceMenus);
	}

	@Override
	public Class<? extends PerspectiveFlavor> getPerspective() {
		return View.class;
	}

	@Override
	public double getPriority() {
		return 1000;
	}

}
