/*******************************************************************************
 * Copyright (c) 2015 Jeremie Bresson.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jeremie Bresson - initial API and implementation
 ******************************************************************************/
package com.bsiag.geneclipsetoc.maven;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.bsiag.geneclipsetoc.internal.GenerateEclipseTocUtility;

@Mojo(name = "geneclipsetoc")
public class GenerateEclipseTocMojo extends AbstractMojo {

  private static final String SOURCE_FOLDER = "sourceFolder";
  private static final String PAGES = "pages";
  private static final String PAGES_LIST_FILE = "pagesListFile";
  private static final String HELP_CONTEXTS = "helpContexts";
  private static final String HELP_PREFIX = "helpPrefix";
  private static final String OUTPUT_TOC_FILE = "outputTocFile";
  private static final String OUTPUT_CONTEXTS_FILE = "outputContextsFile";

  /**
   * Source folder.
   *
   * @parameter expression="${basedir}/src/main/docs"
   * @required
   */
  @Parameter(property = SOURCE_FOLDER, defaultValue = "${basedir}/src/main/docs")
  protected File sourceFolder;

  /**
   * Ordered list of HTLM pages. If set, {@link #pagesListFile} can not be set.
   */
  @Parameter(property = PAGES)
  protected List<String> pages;

  /**
   * External file containing the ordered list of pages. If set, {@link #pages} can not be set.
   */
  @Parameter(property = PAGES_LIST_FILE)
  protected File pagesListFile;

  /**
   * List of the contexts that needs to be generated.
   */
  @Parameter(property = HELP_CONTEXTS)
  protected List<HelpContext> helpContexts;

  /**
   * Sub-path of the HTML pages in the final help plugin: It is the related position of the HTML pages to the toc file.
   */
  @Parameter(property = HELP_PREFIX)
  protected String helpPrefix;

  /**
   * Output toc file.
   *
   * @parameter expression="${project.build.directory}/generated-toc-file/toc.xml"
   * @required
   */
  @Parameter(property = OUTPUT_TOC_FILE, defaultValue = "${project.build.directory}/generated-toc-file/toc.xml")
  protected File outputTocFile;

  /**
   * Output contexts file.
   *
   * @parameter expression="${project.build.directory}/generated-contexts-file/contexts.xml"
   * @required
   */
  @Parameter(property = OUTPUT_CONTEXTS_FILE, required = true, defaultValue = "${project.build.directory}/generated-contexts-file/contexts.xml")
  private File outputContextsFile;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    List<String> pList;
    if (pages.isEmpty() && pagesListFile == null) {
      throw new MojoFailureException("No pages list defined, add <" + PAGES + "> or <" + PAGES_LIST_FILE + "> in your configuration");
    }
    else if (pagesListFile != null) {
      if (!pages.isEmpty()) {
        throw new MojoFailureException("The pages list is defined using a file (<" + PAGES_LIST_FILE + "> is set),  <" + PAGES + "> configuration can not be used");
      }
      try {
        pList = Files.readAllLines(pagesListFile.toPath(), StandardCharsets.UTF_8);
      }
      catch (IOException e) {
        throw new MojoFailureException("Error while reading the file defined in <" + PAGES_LIST_FILE + ">", e);
      }
    }
    else {
      pList = pages;
    }

    try {
      GenerateEclipseTocUtility.generate(sourceFolder, pList, helpPrefix, outputTocFile, outputContextsFile, helpContexts);
    }
    catch (IOException e) {
      throw new MojoExecutionException("Error while generating the toc file", e);
    }
    getLog().info("Generated toc file: " + outputTocFile);
  }

}
