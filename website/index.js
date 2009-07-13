$(function() {
	addGlow();
	fixDownloadLink();
});

function fixDownloadLink() {
	$("#downloadLink").attr("href", "http://projectlombok.googlecode.com/files/lombok.jar")
		.click(function(event) {
			showDownloadInfo();
			event.preventDefault();
	});
	
	$(".backToBar").click(function(event) {
		toggleButtonBar(true);
	});
}

function showDownloadInfo() {
	if ( !$("#downloadInfo").data("filled") ) {
		$("#downloadInfo").data("filled", true);
		$.ajax({
			type: "GET",
			url: "download.html",
			success: function(html) {
				var pos = html.search(/<p\s+id\s*=\s*"downloadHelp"[^>]*>/i);
				if ( pos == -1 ) return;
				html = html.substring(pos);
				pos = html.search(">");
				html = html.substring(pos + 1);
				pos = html.search(/<\s*\/\s*p\s*>/);
				html = html.substring(0, pos);
				var p = $("<p>").html(html).append($("#downloadInfo .downloadActions"));
				$("#downloadInfo span:first-child").replaceWith(p);
				toggleButtonBar(false);
			}
		});
	} else {
		toggleButtonBar(false);
	}
}

function toggleButtonBar(showOriginal) {
	if ( showOriginal ) {
		$("#downloadInfo").hide();
		$("#buttonBar").show();
	} else {
		$("#downloadInfo").show();
		$("#buttonBar").hide();
	}
}

function addGlow() {
	$("a").addClass("js");
	$(".button,.download").addGlow({
		radius: 20,
		textColor: '#00f',
		haloColor: '#00f',
		duration: 500
	});
}