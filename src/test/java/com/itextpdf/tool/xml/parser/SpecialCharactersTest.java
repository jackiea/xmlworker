/**
 *
 */
package com.itextpdf.tool.xml.parser;

import java.io.UnsupportedEncodingException;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import com.itextpdf.tool.xml.parser.state.InsideTagHTMLState;
import com.itextpdf.tool.xml.parser.state.SpecialCharState;

/**
 * @author itextpdf.com
 *
 */
public class SpecialCharactersTest {

	private String regHtml;
	private int reg;
	private SpecialCharState scState;
	private XMLParser parser;
	private String regStr;
	private InsideTagHTMLState itState;
	private int hex;
	private String e;

	@Before
	public void setUp() {
		parser = new XMLParser();
		scState = new SpecialCharState(parser);
		itState = new InsideTagHTMLState(parser);
		reg = 174;
		regHtml = "&reg";
		regStr = "�";
		hex = 0x00ae;
		e = "Travailleur ou chômeur, ouvrier, employé ou cadre, homme ou femme, jeune ou moins jeune,... au Syndicat libéral vous n'êtes pas un numéro et vous pouvez compter sur l'aide de l'ensemble de nos collaborateurs.";

	}

	@Test
	public void testIntCode() throws UnsupportedEncodingException {
		itState.process(hex);
		String str = new String(parser.memory().current().toByteArray(), "ISO-8859-1");
		System.out.println(str);
		Assert.assertEquals(hex, str.charAt(0));
	}

	@Test
	public void testHtmlChar() throws UnsupportedEncodingException {
		scState.process('r');
		scState.process('e');
		scState.process('g');
		scState.process(';');
		String str = new String(parser.memory().current().toByteArray(), "ISO-8859-1");
		Assert.assertEquals(hex, str.charAt(0));
	}

	@Test
	public void testEéçEtc() throws UnsupportedEncodingException {
		for (int i = 0; i < e.length(); i++) {
			itState.process(e.codePointAt(i));
		}
		String str = new String(parser.memory().current().toByteArray(), "ISO-8859-1");
		Assert.assertEquals(e, str);
	}
}
