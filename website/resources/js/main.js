"use strict";

(function($) {
	function clickForVideo() {
		var cfv = $("#clickForVideo");
		var f = function() {
			if (!cfv.is(":visible")) return;
			cfv.hide();
			$("#demoVideo").show().get(0).play();
		};
		
		cfv.css("cursor", "pointer").on("click", f).on("touchstart", function() {
			$(this).data("moved", 0);
		}).on("touchmove", function() {
			$(this).data("moved", 1);
		}).on("touchend", function() {
			if ($(this).data("moved") === 0) f();
		});
	}

	function seekVideo() {
		var t = window.location.hash;
		if (!t) return;
		var s = /^#?(?:(\d\d?):)?(\d\d?):(\d\d?)$/.exec(t);
		if (!s) return;
		var videoj = $("#presentationVideo");
		if (!videoj || videoj.length == 0) return;
		var video = videoj[0];
		var h = parseInt(s[1]);
		if (!h) h = 0;
		var m = parseInt(s[2]);
		var s = parseInt(s[3]);
		video.currentTime = (((h * 60) + m) * 60) + s;
	}

	function aButtonsRespondToSpacebar() {
		$('a[role="button"]').keyup(function(e) {
			if (e.which == 32) e.target.click();
		});
	}

	$(clickForVideo);
	$(seekVideo);
	$(aButtonsRespondToSpacebar);
})($);
