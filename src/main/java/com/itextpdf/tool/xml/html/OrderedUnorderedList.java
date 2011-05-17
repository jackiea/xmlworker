/*
 * $Id$
 *
 * This file is part of the iText (R) project. Copyright (c) 1998-2011 1T3XT BVBA Authors: Balder Van Camp, Emiel
 * Ackermann, et al.
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License version 3 as published by the Free Software Foundation with the addition of the following permission
 * added to Section 15 as permitted in Section 7(a): FOR ANY PART OF THE COVERED WORK IN WHICH THE COPYRIGHT IS OWNED BY
 * 1T3XT, 1T3XT DISCLAIMS THE WARRANTY OF NON INFRINGEMENT OF THIRD PARTY RIGHTS.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details. You should have received a copy of the GNU Affero General Public License along with this program; if not,
 * see http://www.gnu.org/licenses or write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA, 02110-1301 USA, or download the license from the following URL: http://itextpdf.com/terms-of-use/
 *
 * The interactive user interfaces in modified source and object code versions of this program must display Appropriate
 * Legal Notices, as required under Section 5 of the GNU Affero General Public License.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public License, a covered work must retain the producer
 * line in every PDF that is created or manipulated using iText.
 *
 * You can be released from the requirements of the license by purchasing a commercial license. Buying such a license is
 * mandatory as soon as you develop commercial activities involving the iText software without disclosing the source
 * code of your own applications. These activities include: offering paid services to customers as an ASP, serving PDFs
 * on the fly in a web application, shipping iText with a closed source product.
 *
 * For more information, please contact iText Software Corp. at this address: sales@itextpdf.com
 */
package com.itextpdf.tool.xml.html;

import java.util.ArrayList;
import java.util.List;

import com.itextpdf.text.Element;
import com.itextpdf.text.ListItem;
import com.itextpdf.tool.xml.Tag;
import com.itextpdf.tool.xml.Writable;
import com.itextpdf.tool.xml.XMLWorkerConfig;
import com.itextpdf.tool.xml.css.CSS;
import com.itextpdf.tool.xml.css.CssUtils;
import com.itextpdf.tool.xml.css.FontSizeTranslator;
import com.itextpdf.tool.xml.css.apply.ListStyleTypeCssApplier;
import com.itextpdf.tool.xml.css.apply.ParagraphCssApplier;
import com.itextpdf.tool.xml.pipeline.WritableElement;

/**
 * @author Emiel Ackermann
 *
 */
public class OrderedUnorderedList extends AbstractTagProcessor {

	/**
	 *
	 */
	private static final FontSizeTranslator fst = FontSizeTranslator.getInstance();
	/**
	 *
	 */
	private static final CssUtils utils = CssUtils.getInstance();

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.itextpdf.tool.xml.TagProcessor#endElement(com.itextpdf.tool.xml.Tag,
	 * java.util.List)
	 */
	@Override
	public List<Writable> end(final Tag tag, final List<Writable> writables) {
		List<Element> listElements = new ArrayList<Element>();
		List<Writable> mywritables = new ArrayList<Writable>();
		for (Writable w : writables) {
			if (w instanceof WritableElement) {
				for (Element e : ((WritableElement) w).elements()) {
					listElements.add(e);
				}
			} else {
				mywritables.add(w);
			}
		}
		if (listElements.size() > 0) {
			com.itextpdf.text.List list = new ListStyleTypeCssApplier(configuration).apply(
					new com.itextpdf.text.List(), tag);
			int i = 0;
			for (Element li : listElements) {
				if (li instanceof ListItem) {
					Tag child = tag.getChildren().get(i);
					if (list.size() == 1) {
						child.getCSS().put(CSS.Property.MARGIN_TOP,
									calculateTopOrBottomSpacing(true, false, tag, child) + "pt");
						float marginBottom = calculateTopOrBottomSpacing(false, false, tag, child);
						child.getCSS().put(CSS.Property.MARGIN_BOTTOM, marginBottom + "pt");
					} else {
						if (i == 0) {
							child.getCSS().put(CSS.Property.MARGIN_TOP,
										calculateTopOrBottomSpacing(true, false, tag, child) + "pt");
						}
						if (i == list.size() - 1) {
							float marginBottom = calculateTopOrBottomSpacing(false, true, tag, child);
							child.getCSS().put(CSS.Property.MARGIN_BOTTOM, marginBottom + "pt");
						}
					}
					list.add(new ParagraphCssApplier(configuration).apply((ListItem) li, child));
				}
				i++;
			}
			mywritables.add(new WritableElement(list));
		}
		return mywritables;
	}

	private float calculateTopOrBottomSpacing(final boolean isTop, final boolean storeMarginBottom, final Tag tag, final Tag child) {
		String end = isTop?"-top":"-bottom";
		float ownFontSize = fst.getFontSize(tag);
		float ownMargin = 0;
		String marginValue = tag.getCSS().get(CSS.Property.MARGIN+end);
		if(marginValue==null) {
			if(null != tag.getParent() && configuration.getRootTags().contains(tag.getParent().getTag())) {
				ownMargin = ownFontSize;
			}
		} else {
			ownMargin = utils.parseValueToPt(marginValue,ownFontSize);
		}
		float ownPadding = tag.getCSS().get(CSS.Property.PADDING+end)!=null?utils.parseValueToPt(tag.getCSS().get(CSS.Property.PADDING+end),ownFontSize):0;
		float totalSpacing = 0;
		float childFontSize = fst.getFontSize(child);
		float childMargin = child.getCSS().get(CSS.Property.MARGIN+end)!=null?utils.parseValueToPt(child.getCSS().get(CSS.Property.MARGIN+end),childFontSize):0;
		//Margin values of this tag and its first child need to be compared if paddingTop or bottom = 0.
		if(ownPadding == 0) {
			float margin = 0;
			if(ownMargin != 0 && childMargin != 0){
				margin = ownMargin>=childMargin?ownMargin:childMargin;
			} else if (ownMargin != 0) {
				margin = ownMargin;
			} else if (childMargin != 0) {
				margin = childMargin;
			}
			if(!isTop && storeMarginBottom){
				configuration.getMemory().put(XMLWorkerConfig.LAST_MARGIN_BOTTOM, margin);
			}
			totalSpacing = margin;
		} else { // ownpadding != 0 and all margins and paddings need to be accumulated.
			totalSpacing = ownMargin+ownPadding+childMargin;
			if(!isTop && storeMarginBottom){
				configuration.getMemory().put(XMLWorkerConfig.LAST_MARGIN_BOTTOM, ownMargin);
			}
		}
		return totalSpacing;
	}

	/* (non-Javadoc)
	 * @see com.itextpdf.tool.xml.TagProcessor#isStackOwner()
	 */
	@Override
	public boolean isStackOwner() {
		return true;
	}

}
