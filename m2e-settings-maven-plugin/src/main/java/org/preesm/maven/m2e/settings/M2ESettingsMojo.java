package org.preesm.maven.m2e.settings;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "m2e-settings", defaultPhase = LifecyclePhase.INITIALIZE)
public final class M2ESettingsMojo extends AbstractMojo {

	/**
	 * additional generic configuration files for eclipse
	 */
	@Parameter
	private EclipseConfigFile[] additionalConfig;

	@Override
	public final void execute() {
		final Log log = getLog();
		log.error("should not execute this goal");
	}
}
