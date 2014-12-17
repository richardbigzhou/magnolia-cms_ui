/**
 * This file Copyright (c) 2014 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This file is dual-licensed under both the Magnolia
 * Network Agreement and the GNU General Public License.
 * You may elect to use one or the other of these licenses.
 *
 * This file is distributed in the hope that it will be
 * useful, but AS-IS and WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT.
 * Redistribution, except as permitted by whichever of the GPL
 * or MNA you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or
 * modify this file under the terms of the GNU General
 * Public License, Version 3, as published by the Free Software
 * Foundation.  You should have received a copy of the GNU
 * General Public License, Version 3 along with this program;
 * if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * 2. For the Magnolia Network Agreement (MNA), this file
 * and the accompanying materials are made available under the
 * terms of the MNA which accompanies this distribution, and
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.ui.framework.config;

import info.magnolia.cms.util.ExceptionUtil;
import info.magnolia.config.source.file.DirectoryWatcherService;
import info.magnolia.init.MagnoliaConfigurationProperties;
import info.magnolia.init.MagnoliaInitPaths;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GroovyScriptDefinitionManager.
 */
public class GroovyScriptDefinitionManager  {

    private static Logger log = LoggerFactory.getLogger(GroovyScriptDefinitionManager.class);

    private final Path rootPath;

    private final boolean recursive = true;

    private final boolean followSymlinks = false;

    // ../modules/<modulename>/deftype/<id>.groovy
    private final Pattern pathPattern = Pattern.compile("^.*/modules/(.*)/(.*).groovy$");

    private final DirectoryWatcherService directoryWatcherService; // TODO should be optional

    private final GroovyScriptExecutor scriptExecutor = new GroovyScriptExecutor();

    @Inject
    public GroovyScriptDefinitionManager(MagnoliaInitPaths magnoliaInitPaths, MagnoliaConfigurationProperties mcp)  throws IOException {
        this.rootPath = findRootPath(magnoliaInitPaths, mcp);
        directoryWatcherService = new DirectoryWatcherService(rootPath, true, new DefFileVisitor());
        // TODO we should start this after first call to loadItUp
        directoryWatcherService.start();
    }

    @PostConstruct
    public void loadItUp() {
        log.info("Setting up {} to load templates from {}", getClass().getSimpleName(), rootPath);
        loadAllFrom(rootPath);
    }

    protected void loadAllFrom(Path path) {
        log.info("Loading templates from {}", getClass().getSimpleName(), path);

        try {
            final EnumSet<FileVisitOption> options = followSymlinks ? EnumSet.of(FileVisitOption.FOLLOW_LINKS) : EnumSet.noneOf(FileVisitOption.class);
            final int maxDepth = recursive ? Integer.MAX_VALUE : 0;
            Files.walkFileTree(path, options, maxDepth, new DefFileVisitor());
        } catch (IOException e) {
            throw new RuntimeException(e); // TODO
        }
    }

    protected void loadAndRegister(Path file) throws IOException {
        // TODO assume utf-8 ? or defaultCharset ?
        try (final Reader in = Files.newBufferedReader(file, Charset.forName("UTF-8"))) {
            final Matcher matcher =  pathPattern.matcher(file.toAbsolutePath().toString());
            if (matcher.find()) {
                String idBase = String.format("%s:%s", matcher.group(1), matcher.group(2));
                scriptExecutor.executeScript(in, idBase);
                log.info("Registered {}", idBase);
            }
        }
    }

    protected Path findRootPath(MagnoliaInitPaths magnoliaInitPaths, MagnoliaConfigurationProperties magnoliaConfigurationProperties) {
        final String pathStr = magnoliaConfigurationProperties.getProperty("magnolia.home");
        // TODO: "/templates" obviously hardcoded and to be removed; simply to ease the demo - avoids going through /repository and /WEB-INF for nothing
        final Path path = Paths.get(pathStr, "templates");
        if (!Files.isDirectory(path)) {
            throw new IllegalStateException("magnolia.home is set to " + pathStr + ", which is not a directory.");
        }
        return path;
    }

    private class DefFileVisitor extends SimpleFileVisitor<Path> {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            super.visitFile(file, attrs);
            // TODO relativize from rootPath or path ?
            final String relativePath = rootPath.relativize(file).toString();
            if (pathPattern.matcher(relativePath).matches()) {
                try {
                    loadAndRegister(file);
                } catch (IOException e) {
                    log.error("Could not load {}: {}", file, ExceptionUtil.exceptionToWords(e), e);
                }
            } else {
                log.info("Skipping {}", file);
            }
            return FileVisitResult.CONTINUE;
        }
    }
}
