/*
 * $Id$
 *
 * This file is part of the iText (R) project.
 * Copyright (c) 1998-2011 1T3XT BVBA
 * Authors: Balder Van Camp, Emiel Ackermann, et al.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License version 3
 * as published by the Free Software Foundation with the addition of the
 * following permission added to Section 15 as permitted in Section 7(a):
 * FOR ANY PART OF THE COVERED WORK IN WHICH THE COPYRIGHT IS OWNED BY 1T3XT,
 * 1T3XT DISCLAIMS THE WARRANTY OF NON INFRINGEMENT OF THIRD PARTY RIGHTS.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA, 02110-1301 USA, or download the license from the following URL:
 * http://itextpdf.com/terms-of-use/
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public License,
 * a covered work must retain the producer line in every PDF that is created
 * or manipulated using iText.
 *
 * You can be released from the requirements of the license by purchasing
 * a commercial license. Buying such a license is mandatory as soon as you
 * develop commercial activities involving the iText software without
 * disclosing the source code of your own applications.
 * These activities include: offering paid services to customers as an ASP,
 * serving PDFs on the fly in a web application, shipping iText with a closed
 * source product.
 *
 * For more information, please contact iText Software Corp. at this
 * address: sales@itextpdf.com
 */
package com.itextpdf.tool.xml.pipeline;

import com.itextpdf.tool.xml.CustomContext;
import com.itextpdf.tool.xml.NoCustomContextException;
import com.itextpdf.tool.xml.Pipeline;
import com.itextpdf.tool.xml.PipelineException;
import com.itextpdf.tool.xml.ProcessObject;
import com.itextpdf.tool.xml.Tag;
import com.itextpdf.tool.xml.WorkerContext;
import com.itextpdf.tool.xml.exceptions.LocaleMessages;

/**
 * Abstract class with default implementations. Override this instead of
 * implementing Pipeline and let your pipeline override only the methods
 * relevant to your implementation.
 *
 * @author redlab_b
 * @param <T> the type of CustomContext
 *
 */
public abstract class AbstractPipeline<T extends CustomContext> implements Pipeline<T> {

	private final ThreadLocal<WorkerContext> wc = new ThreadLocal<WorkerContext>();
	private Pipeline<?> next;

	/**
	 * @param next the pipeline that's next in the sequence.
	 *
	 */
	public AbstractPipeline(final Pipeline<?> next) {
		setNext(next);
	}
	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.itextpdf.tool.xml.pipeline.Pipeline#setContext(com.itextpdf
	 * .tool.xml.pipeline.WorkerContext)
	 */
	public final void setContext(final WorkerContext context) {
		this.wc.set(context);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.itextpdf.tool.xml.pipeline.Pipeline#getNext()
	 */
	public Pipeline<?> getNext() {
		return next;
	}

	/**
	 * @return the WorkerContext
	 */
	public final WorkerContext getContext() {
		return wc.get();
	}
	/**
	 * Just calls getNext.
	 *
	 */
	public Pipeline<?> open(final Tag t, final ProcessObject po) throws PipelineException {
		return getNext();
	}

	/**
	 * Just calls getNext.
	 *
	 */
	public Pipeline<?> content(final Tag t, final  byte[] content, final ProcessObject po) throws PipelineException {
		return getNext();
	}

	/**
	 * Just calls getNext.
	 *
	 */
	public Pipeline<?> close(final Tag t, final ProcessObject po) throws PipelineException {
		return getNext();
	}

	/* (non-Javadoc)
	 * @see com.itextpdf.tool.xml.pipeline.Pipeline#getCustomContext()
	 */
	public T getNewCustomContext() throws NoCustomContextException {
		throw new NoCustomContextException();
	}

	@SuppressWarnings("unchecked")
	public T getLocalContext() throws PipelineException {
		try {
			CustomContext cc = getContext().get(this.getClass().getName());
			if (null != cc) {
				return (T) cc;
			}
			throw new PipelineException(String.format(LocaleMessages.getInstance().getMessage(LocaleMessages.OWN_CONTEXT_404), this.getClass().getName()));
		} catch (NoCustomContextException e) {
			throw new PipelineException(String.format(LocaleMessages.getInstance().getMessage(LocaleMessages.OWN_CONTEXT_404), this.getClass().getName()), e);
		}
	}
	/**
	 * setNext method. When using this while parsing one can make live
	 * changes the pipeline structure. Use with caution.
	 *
	 * @param next set the next pipeline
	 */
	public void setNext(final Pipeline<?> next) {
		this.next = next;
	}

	/**
	 * Defaults to the fully qualified class name of the object.
	 */
	public String getContextKey() {
		return this.getClass().getName();
	}
}
