/*
 * Copyright (C) 2009 The Project Lombok Authors.
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
package lombok.installer;

import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

/**
 * Mac OS X specific code to gussy up the GUI a little bit, mostly with a nice dock icon. Well, nicer than
 * the standard icon, at any rate.
 */
class AppleNativeLook {
	public static void go() throws Exception {
		Class<?> appClass = Class.forName("com.apple.eawt.Application");
		Object app = appClass.getMethod("getApplication").invoke(null);
		appClass.getMethod("removeAboutMenuItem").invoke(app);
		appClass.getMethod("removePreferencesMenuItem").invoke(app);
		
		BufferedImage image = ImageIO.read(AppleNativeLook.class.getResource("lombokIcon.png"));
		appClass.getMethod("setDockIconImage", Image.class).invoke(app, image);
	}
}
