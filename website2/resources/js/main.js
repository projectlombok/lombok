"use strict";

(function($) {
	swfobject.registerObject("player", "9.0.98", "videos/expressInstall.swf");

	function toKey(href) {
		var lnk = "__savedContent_" + href.replace(/\//g, '_');
		if (lnk.substring(lnk.length - 5) === ".html") lnk = lnk.substring(0, lnk.length - 5);
		return lnk;
	}

	function ajaxFeaturePages() {
		if (!History.enabled) return;
		History.replaceState({urlPath: window.location.pathname}, $("title").text(), History.getState().urlpath);

		$("a").each(function() {
			var self = $(this);
			var href = self.attr("href");
			if (!href) return;
			if (href.substring(0, 10) !== "/features/") return;
			self.on("click", function(evt) {
				evt.preventDefault();
				var key = toKey(window.location.pathname);
				if ($("#" + key).length < 1) {
					var d = $("<div />").attr("id", key).append($("#featureContent").contents()).hide();
					$("body").append(d);
				}
				History.pushState({urlPath: href}, self.text(), href);
			});
		});

		$(window).on("statechange", function() {
			var hs = History.getState();
			var u = hs.data.urlPath;
			if (u.substring(u.length - 5) !== ".html") u += ".html";
			var key = toKey(u);
			var sc = $("#" + key);
			if (sc.length > 0) {
				var a = $("#featureContent");
				sc.show().attr("id", "featureContent");
				a.replaceWith(sc);
			} else {
				$.ajax({
					url: u,
					success: function(response) {
						$("#featureContent").replaceWith($(response).find("#featureContent"));
					}
				});
			}
		});
	}

	$(ajaxFeaturePages);
})($);