/*******************************************************************************
 * Copyright (c) 2010 JVM Monitor project. All rights reserved. 
 * 
 * This code is distributed under the terms of the Eclipse Public License v1.0
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.jvmmonitor.core.dump;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.jvmmonitor.core.IEclipseJobElement;
import org.jvmmonitor.core.IThreadElement;
import org.jvmmonitor.internal.core.ThreadDumpSaxEventHandler;
import org.xml.sax.SAXException;

/**
 * The thread dump parser.
 */
public class ThreadDumpParser extends AbstractDumpParser {

    /** The thread list elements. */
    private List<IThreadElement> threadListElements;

    private List<IEclipseJobElement> eclipseJobElements;
    
    /**
     * The constructor.
     * 
     * @param file
     *            the dump file
     * @param threadListElements
     *            The thread list elements
     * @param eclipseJobElements
     *            The eclipse job elements
     * @param monitor
     *            The progress monitor
     */
    public ThreadDumpParser(File file, List<IThreadElement> threadListElements,List<IEclipseJobElement> eclipseJobElements,
            IProgressMonitor monitor) {
        super(monitor);
        Assert.isNotNull(file);

        this.file = file;
        this.threadListElements = threadListElements;
        this.eclipseJobElements = eclipseJobElements;
    }

    /**
     * Parses the thread dump.
     * 
     * @throws SAXException
     *             if creating parser fails
     * @throws ParserConfigurationException
     *             if creating parser fails
     * @throws IOException
     *             if parsing input fails
     */
    public void parse() throws ParserConfigurationException, SAXException,
            IOException {
        parser = SAXParserFactory.newInstance().newSAXParser();
        ThreadDumpSaxEventHandler handler = new ThreadDumpSaxEventHandler(
                threadListElements, eclipseJobElements, monitor);

        parser.parse(file, handler);
        info = handler.getProfileInfo();
    }
}
