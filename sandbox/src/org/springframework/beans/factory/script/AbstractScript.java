/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.beans.factory.script;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;

/**
 * 
 * 
 * @author Rod Johnson
 * @version $Id: AbstractScript.java,v 1.1 2004-08-02 17:01:59 johnsonr Exp $
 */
public abstract class AbstractScript implements Script {
	
	protected final Log log = LogFactory.getLog(getClass());

	private String location;

	private ScriptContext context;
	
	/**
	 * List of Class
	 */
	private List interfaces = new LinkedList();
	
	private long lastReloadTime;

	/**
	 * Interfaces is string
	 * @param location
	 * @param interfaces
	 * @param loader
	 */
	public AbstractScript(String location, ScriptContext context) {
		this.location = location;
		
		this.context = context;
	}

	public String getLocation() {
		return location;
	}
	
	public Class[] getInterfaces() {
		return (Class[]) interfaces.toArray(new Class[interfaces.size()]);
	}
	
	public void addInterface(Class intf) {
		interfaces.add(intf);
	}

	/**
	 * @see org.springframework.beans.factory.script.Script#getResourceString()
	 */
	public String getResourceString() {
		return getClass().getName() + ": location='" + location + "'";
	}
	
	/**
	 * @see org.springframework.beans.factory.script.Script#createObject()
	 */
	public Object createObject() throws BeansException {
		InputStream is = null;
		try {
			is = context.getResourceLoader().getResource(location).getInputStream();
			if (is == null) {
				throw new ScriptNotFoundException("No script found at '" + location + "'");
			}
			return createObject(is);
		} 
		catch (FileNotFoundException ex) {
			throw new ScriptNotFoundException("Script from '" + location + "' not found");
		} 
		catch (IOException ex) {
			throw new CompilationException("Error reading script from '" + location + "'", ex);
		} 
		finally {
			lastReloadTime = System.currentTimeMillis();
			try {
				if (is != null) {
					is.close();
				}
			}
			catch (IOException ex) {
				log.warn("Could not close script resource", ex);
			}
		}
	}

	protected abstract Object createObject(InputStream is) throws IOException, BeansException;
	
	/**
	 * @see org.springframework.beans.factory.script.Script#isChanged()
	 */
	public boolean isChanged() {
		try {
			File f = context.getResourceLoader().getResource(location).getFile();
			return f.lastModified() > lastReloadTime;
		}
		catch (IOException ex) {
			log.warn("Could not check resource date", ex);
			return true;
		}
	}

	/**
	 * @see org.springframework.beans.factory.script.Script#getLastReloadTime()
	 */
	public long getLastReloadTime() {
		return lastReloadTime;
	}

}