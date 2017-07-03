/*
 * Copyright (C) 2015-2017 The Project Lombok Authors.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package lombok.core;

import static lombok.core.handlers.Singulars.autoSingularize;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TestSingulars {
	@Test
	public void testSingulars() {
		assertEquals(null, autoSingularize("axes"));
		assertEquals("adjective", autoSingularize("adjectives"));
		assertEquals("bus", autoSingularize("buses"));
		assertEquals("octopus", autoSingularize("octopodes"));
		assertEquals(null, autoSingularize("octopi"));
		assertEquals("elf", autoSingularize("elves"));
		assertEquals("jack", autoSingularize("jacks"));
		assertEquals("colloquy", autoSingularize("colloquies"));
		assertEquals(null, autoSingularize("series"));
		assertEquals("man", autoSingularize("men"));
		assertEquals(null, autoSingularize("highwaymen"));
		assertEquals("caveMan", autoSingularize("caveMen"));
		assertEquals(null, autoSingularize("jackss"));
		assertEquals(null, autoSingularize("virus"));
		assertEquals("quiz", autoSingularize("quizzes"));
		assertEquals("database", autoSingularize("databases"));
		assertEquals("dataBase", autoSingularize("dataBases"));
		assertEquals("Query", autoSingularize("Queries"));
		assertEquals("Movie", autoSingularize("Movies"));
		assertEquals("cafe", autoSingularize("cafes"));
		assertEquals("cave", autoSingularize("caves"));
		assertEquals("leaf", autoSingularize("leaves"));
		assertEquals("autosave", autoSingularize("autosaves"));
	}
}
