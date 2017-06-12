"use strict";

(function($) {
	swfobject.registerObject("player", "9.0.98", "videos/expressInstall.swf");
	
	function toKey(href) {
		var lnk = "__savedContent_" + href.replace(/\//g, '_');
		if (lnk.substring(lnk.length - 5) === ".html") lnk = lnk.substring(0, lnk.length - 5);
		return lnk;
	}
	
	function captureLinkClick() {
		var self = $(this);
		if (self.data("clc")) return;
		var href = self.attr("href");
		self.data("clc", true);
		if (!href || href.substr(0, 4) === "http") return;
		var ext = href.substr(href.length - 4, 4);
		if (ext === ".xml" || ext === ".jar") return;
		self.on("click", function(evt) {
			evt.preventDefault();
			var key = toKey(window.location.pathname);
			if ($("#" + key).length < 1) {
				var d = $("<div />").attr("id", key).append($("#main-section").contents()).hide();
				$("body").append(d);
			}
			History.pushState({urlPath: href}, self.text(), href);
		});
	}
	
	function collapseMenu() {
		if ($(".navbar-collapse").is(".in")) $(".navbar-toggle").click();
	}
	
	function ajaxFeaturePages() {
		if (!History.enabled) return;
		History.replaceState({urlPath: window.location.pathname}, $("title").text(), History.getState().urlpath);
		
		$("a").each(captureLinkClick);
		
		$(window).on("statechange", function() {
			var hs = History.getState();
			var u = hs.data.urlPath;
			if (u === "/" || u === "") u = "/main.html";
			if (u.substring(u.length - 5) !== ".html") u += ".html";
			if (u.substring(u.length - 8) === "all.html") u = u.substring(0, u.length - 8) + "index.html";
			var key = toKey(u);
			var sc = $("#" + key);
			if (sc.length > 0) {
				var a = $("#main-section");
				sc.show().attr("id", "main-section").attr("class", "container-fluid main-section");
				a.replaceWith(sc);
				collapseMenu();
			} else {
				$.ajax({
					url: u,
					success: function(response) {
						var x = '<div class="container-fluid main-section" id="main-section">';
						var y = '<footer';
						var start = response.indexOf(x);
						var end = response.indexOf(y);
						var newH = $(response.substr(start, end - start));
						$("#main-section").replaceWith(newH);
						collapseMenu();
						$("a").each(captureLinkClick);
					}
				});
			}
		});
	}
	
	$(ajaxFeaturePages);
})($);
