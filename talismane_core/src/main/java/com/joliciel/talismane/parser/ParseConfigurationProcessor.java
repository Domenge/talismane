///////////////////////////////////////////////////////////////////////////////
//Copyright (C) 2014 Joliciel Informatique
//
//This file is part of Talismane.
//
//Talismane is free software: you can redistribute it and/or modify
//it under the terms of the GNU Affero General Public License as published by
//the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.
//
//Talismane is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU Affero General Public License for more details.
//
//You should have received a copy of the GNU Affero General Public License
//along with Talismane.  If not, see <http://www.gnu.org/licenses/>.
//////////////////////////////////////////////////////////////////////////////
package com.joliciel.talismane.parser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.joliciel.talismane.Talismane;
import com.joliciel.talismane.Talismane.BuiltInTemplate;
import com.joliciel.talismane.TalismaneException;
import com.joliciel.talismane.TalismaneSession;
import com.joliciel.talismane.output.FreemarkerTemplateWriter;
import com.joliciel.talismane.utils.ConfigUtils;
import com.typesafe.config.Config;

/**
 * Any class that can process parse configurations generated by the parser. Note
 * that implementations of this interface should provide a default no-argument
 * constructor, and should fill in all dependencies via the setParameters
 * method.
 * 
 * @author Assaf Urieli
 *
 */
public interface ParseConfigurationProcessor extends Closeable {
  public enum ProcessorType {
    output,
    parseFeatureTester,
    transitionLog
  }

  /**
   * Called when the next parse configuration is available for processing.
   * 
   * @throws TalismaneException
   *           if an known exception occurs when processing this parse
   *           configuration
   * @throws IOException
   */
  public void onNextParseConfiguration(ParseConfiguration parseConfiguration) throws TalismaneException, IOException;

  /**
   * Called when parsing is complete.
   * 
   * @throws IOException
   */
  public void onCompleteParse() throws IOException;

  /**
   * Collect the processors specified in the session.
   * 
   * @param writer
   *          if specified, and an output processor is in the session, will be
   *          used for the output processor
   * @param outDir
   *          directory in which to write the various outputs
   * @param session
   * @return
   * @throws IOException
   */
  public static List<ParseConfigurationProcessor> getProcessors(Writer writer, File outDir, TalismaneSession session) throws IOException {
    Config config = session.getConfig();
    Config parserConfig = config.getConfig("talismane.core.parser");

    List<ParseConfigurationProcessor> processors = new ArrayList<>();
    List<ProcessorType> processorTypes = parserConfig.getStringList("output.processors").stream().map(f -> ProcessorType.valueOf(f))
        .collect(Collectors.toList());

    if (outDir != null)
      outDir.mkdirs();

    for (ProcessorType type : processorTypes) {
      switch (type) {
      case output: {
        Reader templateReader = null;
        String configPath = "talismane.core.parser.output.template";
        if (config.hasPath(configPath)) {
          templateReader = new BufferedReader(new InputStreamReader(ConfigUtils.getFileFromConfig(config, configPath)));
        } else {
          String templateName = null;
          BuiltInTemplate builtInTemplate = BuiltInTemplate.valueOf(parserConfig.getString("output.built-in-template"));
          switch (builtInTemplate) {
          case standard:
            templateName = "parser_conll_template.ftl";
            break;
          case with_location:
            templateName = "parser_conll_template_with_location.ftl";
            break;
          case with_prob:
            templateName = "parser_conll_template_with_prob.ftl";
            break;
          case with_comments:
            templateName = "parser_conll_template_with_comments.ftl";
            break;
          default:
            throw new RuntimeException("Unknown builtInTemplate for parser: " + builtInTemplate.name());
          }

          String path = "output/" + templateName;
          InputStream inputStream = Talismane.class.getResourceAsStream(path);
          if (inputStream == null)
            throw new IOException("Resource not found in classpath: " + path);
          templateReader = new BufferedReader(new InputStreamReader(inputStream));
        }

        if (writer == null) {
          File file = new File(outDir, session.getBaseName() + "_dep.txt");
          writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, false), session.getOutputCharset()));
        }
        ParseConfigurationProcessor processor = new FreemarkerTemplateWriter(templateReader, writer);
        processors.add(processor);
        break;
      }
      case parseFeatureTester: {
        File file = new File(outDir, session.getBaseName() + "_parseFeatureTest.txt");
        Writer featureWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, false), session.getOutputCharset()));
        ParseConfigurationProcessor processor = new ParseFeatureTester(session, featureWriter);
        processors.add(processor);
        break;
      }
      case transitionLog: {
        File csvFile = new File(outDir, session.getBaseName() + "_transitions.csv");
        csvFile.delete();
        csvFile.createNewFile();

        Writer csvFileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(csvFile, false), session.getCsvCharset()));
        ParseConfigurationProcessor processor = new TransitionLogWriter(csvFileWriter);
        processors.add(processor);
      }
      }
    }

    return processors;
  }
}
