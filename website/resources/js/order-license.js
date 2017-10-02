"use strict";

(function() {
	var imgDataUrl = null;
	function applyLicense() {
		$("#submit").on("click", function(evt) {
			evt.preventDefault();
			
			var onSuccess = function() {
				alert("Form submitted!");
			};
			
			var onFailure = function() {
				alert("Whoops");
			};
			
			var data = {};
			data.name = $("#name").val();
			var rnd = generateRandom();
			var err = processImageUpload();
			if (err) {
				alert(err);
				$("#logo")[0].value = null;
				return;
			}
			if (imgDataUrl) data.logo = imgDataUrl;
			$.ajax({
				url: "/license-submit/" + rnd,
				method: "PUT",
				contentType: "application/json",
				data: JSON.stringify(data),
				processData: false,
				success: onSuccess,
				failure: onFailure
			});
		});
		
		$("#companyLogo").on("click", function(evt) {
			evt.preventDefault();
			$("#logo").click();
		});
		
		$("#deleteCompanyLogo").on("click", function(evt) {
			evt.preventDefault();
			$("#logo")[0].value = null;
			$("#logoCnt").empty().hide();
			$("#companyLogo").show();
			$("#deleteCompanyLogo").hide();
		});
		
		$("#logo").on("change", function() {
			var err = showImage();
			if (err) {
				alert(err);
				$("#logo")[0].value = null;
			} else {
				$("#companyLogo").hide();
				$("#deleteCompanyLogo").show();
			}
		});
	}
	
	function generateRandom() {
		var buf = new Uint8Array(40);
		window.crypto.getRandomValues(buf);
		return btoa(String.fromCharCode.apply(null, buf)).replace(/\+/g, '_').replace(/\//gi, '-');
	}
	
	function showImage() {
		$("#logoCnt").empty().hide();
		try {
			return processImageUpload(function(dataUrl) {
				var img = $("<img />");
				img.css({
					"max-width": "500px",
					"max-height": "300px"
				});
				$("#logoCnt").append(img).show();
				img.attr("src", dataUrl);
			});
		} catch (e) {
			if (console && console.log) console.log(e);
		}
	}
	
	function processImageUpload(fnc) {
		var f = $("#logo")[0].files[0];
		if (!f) return;
		if (f.size > 10000000) return "Logo too large; please give us a logo below 10MiB in size.";
		var imageType = /^image\//;
		if (!imageType.test(f.type)) return "Please upload an image, for example in PNG format.";
		var reader = new FileReader();
		reader.onload = function(e) {
			imgDataUrl = e.target.result;
			if (fnc) fnc(e.target.result);
		};
		reader.readAsDataURL(f);
		return null;
	}
	
	$(applyLicense);
})();
