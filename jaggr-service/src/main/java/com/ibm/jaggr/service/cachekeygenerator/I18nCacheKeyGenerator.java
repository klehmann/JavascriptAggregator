/*
 * (C) Copyright 2012, IBM Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ibm.jaggr.service.cachekeygenerator;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;

import com.ibm.jaggr.service.transport.IHttpTransport;

public final class I18nCacheKeyGenerator implements ICacheKeyGenerator {
	private static final long serialVersionUID = -8488089828754517200L;
	
	private static final String eyecatcher = "i18n"; //$NON-NLS-1$

	private final Collection<String> availableLocales;
	private final boolean provisional;
	
	public I18nCacheKeyGenerator(
			Collection<String> availableLocales,
			boolean provisional) {
		this.availableLocales = availableLocales != null ? 
				Collections.unmodifiableCollection(new HashSet<String>(availableLocales)) : null;
		this.provisional = provisional;
	}
	
	@Override
	public String generateKey(HttpServletRequest request) {
		StringBuffer sb = new StringBuffer();
		@SuppressWarnings("unchecked")
		Collection<String> requestedLocales = (Collection<String>)request.getAttribute(IHttpTransport.REQUESTEDLOCALES_REQATTRNAME);
		if (requestedLocales != null && requestedLocales.size() > 0) {
			int i = 0;
			for (String locale : requestedLocales) {
				if (availableLocales == null) {
					sb.append(i++ > 0 ? "," : "").append(locale); //$NON-NLS-1$ //$NON-NLS-2$
				} else {
					String[] a = locale.split("-"); //$NON-NLS-1$
					String language = a[0].toLowerCase();
					String country = (a.length > 1) ? a[1].toLowerCase() : ""; //$NON-NLS-1$
					String varient = (a.length > 2) ? a[2].toLowerCase() : ""; //$NON-NLS-1$
					if (
							language.length() > 0 && 
							country.length() > 0 &&
							varient.length() > 0 && 
							availableLocales.contains(language+"-"+country+"-"+varient)) { //$NON-NLS-1$ //$NON-NLS-2$
						sb.append(i++ > 0 ? "," : "").append(language+"-"+country+"-"+varient); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
					} else if (
							language.length() > 0 && 
							country.length() > 0 && 
							availableLocales.contains(language+"-"+country)) { //$NON-NLS-1$
						sb.append(i++ > 0 ? "," : "").append(language+"-"+country); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					} else if (
							language.length() > 0 && 
							availableLocales.contains(language)) {
						sb.append(i++ > 0 ? "," : "").append(language); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}
			}
		}
		return sb.length() > 0 ? sb.insert(0, eyecatcher+"{").append("}").toString() : ""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
	
	@Override
	public boolean isProvisional() {
		return provisional;
	}

	@Override
	public ICacheKeyGenerator combine(ICacheKeyGenerator otherKeyGen) {
		I18nCacheKeyGenerator other = (I18nCacheKeyGenerator)otherKeyGen;
		if (provisional || other.provisional) {
			// should never happen
			throw new IllegalStateException();
		}
		Collection<String> combined = null;
		if (availableLocales != null && other.availableLocales != null) {
			combined = new HashSet<String>(availableLocales);
			combined.addAll(other.availableLocales);
		}
		return new I18nCacheKeyGenerator(
				combined,
				false);
	}
	@Override
	public String toString() {
		// Map features into sorted set so we get predictable ordering of
		// items in the output.
		SortedSet<String> set = availableLocales == null ? null : new TreeSet<String>(availableLocales);
		StringBuffer sb = new StringBuffer(eyecatcher).append(":"); //$NON-NLS-1$
		sb.append(set == null ? "null" : set.toString()); //$NON-NLS-1$
		if (isProvisional()) {
			sb.append(":provisional"); //$NON-NLS-1$
		}
		return sb.toString();
	}

	@Override
	public ICacheKeyGenerator[] getCacheKeyGenerators(HttpServletRequest request) {
		return null;
	}
	
	public Collection<String> getLocales() {
		return availableLocales;
	}
}